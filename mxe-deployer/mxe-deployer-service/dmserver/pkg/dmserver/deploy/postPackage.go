package deploy

import (
	"context"
	"fmt"
	"os"
	"path/filepath"

	argocdclient "github.com/argoproj/argo-cd/v2/pkg/apiclient"
	applicationpkg "github.com/argoproj/argo-cd/v2/pkg/apiclient/application"
	repositorypkg "github.com/argoproj/argo-cd/v2/pkg/apiclient/repository"
	"github.com/argoproj/argo-cd/v2/pkg/apis/application/v1alpha1"
	argoprojio "github.com/argoproj/argo-cd/v2/util/io"
	"github.com/argoproj/argo-cd/v2/util/text/label"
	"github.com/go-kit/kit/log/level"
	"github.com/jinzhu/copier"
	"github.com/pkg/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	errorutils "mxe.ericsson/depmanager/utils/errors"
	fileUtils "mxe.ericsson/depmanager/utils/file"
	"mxe.ericsson/depmanager/utils/git"
	logUtil "mxe.ericsson/depmanager/utils/log"
	pathUtils "mxe.ericsson/depmanager/utils/path"
	annotation "mxe.ericsson/depmanager/utils/text/annotation"
)

const (
	emptyString                = ""
	manifestGenerateAnnotation = "argocd.argoproj.io/manifest-generate-paths"
)

func appSync(ctx context.Context, acdClient argocdclient.Client, appName *string) (*v1alpha1.Application, error) {
	conn, appIf, err := acdClient.NewApplicationClient()
	if err != nil {
		return nil, err
	}
	defer argoprojio.Close(conn)

	syncReq := applicationpkg.ApplicationSyncRequest{
		Name: appName,
	}

	syncReq.Strategy = &v1alpha1.SyncStrategy{Hook: &v1alpha1.SyncStrategyHook{}}
	syncReq.Strategy.Hook.Force = false

	return appIf.Sync(ctx, &syncReq)

}

func (s *deployService) getRepository(ctx context.Context, repoQuery *repositorypkg.RepoQuery, clientOpts *argocdclient.ClientOptions) (*v1alpha1.Repository, error) {

	acdClient, err := argocdclient.NewClient(clientOpts)
	if err != nil {
		return nil, errors.Wrap(err, "Failed to instantiate argocd client")
	}

	repoConn, repoIf, err := acdClient.NewRepoClient()
	if err != nil {
		return nil, errors.Wrap(err, "Failed to instantiate repo client")
	}

	defer argoprojio.Close(repoConn)

	check_repo_creds_only := false
	repo, err := repoIf.Get(ctx, repoQuery)
	if err != nil {
		level.Info(s.logger).Log("Repository", repoQuery.Repo, "msg", " is not configured in Argo-CD. Checking for existence of repository credential")
		repo = &v1alpha1.Repository{Repo: repoQuery.Repo}
		check_repo_creds_only = true
	}
	err = s.Config.SetCredsForRepo(repo, check_repo_creds_only)
	if err != nil {
		return nil, err
	}
	return repo, nil
}

func (s *deployService) getClientOptions(authToken string) (*argocdclient.ClientOptions, error) {
	var clientOpts *argocdclient.ClientOptions = &argocdclient.ClientOptions{}

	err := copier.Copy(&clientOpts, &s.Config.Store.ArgoCDClientOptions)

	if err != nil {
		return nil, err
	}
	clientOpts.AuthToken = authToken

	return clientOpts, nil

}

func parseLabels(userSuppliedLabels []string, defaultLabels []string) (*map[string]string, error) {
	allLabels := append(userSuppliedLabels, defaultLabels...)
	mapLabels, err := label.Parse(allLabels)
	if err != nil {
		return nil, errors.Wrap(err, "Failed to parse labels")
	}
	return &mapLabels, nil
}

func parseAnnotations(userSuppliedAnnotations []string, sourcePath string) (*map[string]string, error) {
	mapAnnotations, err := annotation.Parse(userSuppliedAnnotations)
	if err != nil {
		return nil, errors.Wrap(err, "Failed to parse labels")
	}
	if _, ok := mapAnnotations[manifestGenerateAnnotation]; !ok {
		mapAnnotations[manifestGenerateAnnotation] = fmt.Sprintf("/%s", sourcePath)
	}
	return &mapAnnotations, nil
}

func (s *deployService) createAppln(ctx context.Context, appPackageReq *PackageRequestMeta, clientOpts *argocdclient.ClientOptions) (*v1alpha1.Application, error) {

	var project string = "default"
	var err error

	acdClient, err := argocdclient.NewClient(clientOpts)
	if err != nil {
		return nil, errors.Wrap(err, "Failed to instantiate argocd client")
	}

	appConn, appIf, err := acdClient.NewApplicationClient()
	if err != nil {
		return nil, errors.Wrap(err, "Failed to instantiate application client")
	}

	defer argoprojio.Close(appConn)

	var defaultLabels = []string{
		fmt.Sprintf("managedBy=%s", managedBy),
	}

	var ignoreDifferences *[]v1alpha1.ResourceIgnoreDifferences = nil
	if appPackageReq.Project != "" {
		project = appPackageReq.Project
	}

	labels, err := parseLabels(appPackageReq.Labels, defaultLabels)
	if err != nil {
		return nil, err
	}

	annotations, err := parseAnnotations(appPackageReq.Annotations, appPackageReq.Source.Path)
	if err != nil {
		return nil, err
	}

	app := v1alpha1.Application{
		ObjectMeta: metav1.ObjectMeta{
			Name:        appPackageReq.ApplicationName,
			Namespace:   s.Config.GetArgoCDNamespace(),
			Labels:      *labels,
			Annotations: *annotations,
		},
		Spec: v1alpha1.ApplicationSpec{
			Source:      &appPackageReq.Source,
			Destination: appPackageReq.Destination,
			Project:     project,
			SyncPolicy:  appPackageReq.SyncPolicy,
		},
	}
	if ignoreDifferences != nil {
		app.Spec.IgnoreDifferences = *ignoreDifferences
	}

	level.Debug(s.logger).Log("msg", fmt.Sprintf("Submitting application to argocd %s", logUtil.ToString(app)))
	applicationCreateReq := applicationpkg.ApplicationCreateRequest{
		Application: &app,
	}

	appln, err := appIf.Create(ctx, &applicationCreateReq)
	if err != nil {
		return nil, err
	}

	if appPackageReq.SyncPolicy.Automated == nil && appPackageReq.InitSync {
		return appSync(ctx, acdClient, &appln.Name)
	}

	return appln, err
}

func isAppAlreadyDeployed(ctx context.Context, packageRequestOptions *PackageRequestMeta, clientOpts *argocdclient.ClientOptions) error {
	acdClient, err := argocdclient.NewClient(clientOpts)
	if err != nil {
		return errors.Wrap(err, "Failed to instantiate argocd client")
	}

	conn, appIf, err := acdClient.NewApplicationClient()
	if err != nil {
		return errors.Wrap(err, "Failed to instantiate argocd client")
	}

	defer argoprojio.Close(conn)
	apps, err := appIf.List(ctx, &applicationpkg.ApplicationQuery{})
	if err != nil {
		return err
	}
	appsList := apps.Items
	if len(appsList) != 0 {
		for _, app := range appsList {
			if app.Name == packageRequestOptions.ApplicationName {
				return errorutils.Conflict(errors.New(fmt.Sprintf("An application with name %s already exists in the cluster %s",
					app.Name, app.Spec.Destination.Server)))
			}
		}
	}
	return nil
}

func (s *deployService) initialise(ctx context.Context) (*argocdclient.ClientOptions, error) {
	level.Debug(s.logger).Log("Context", logUtil.ToString(ctx))

	authToken := getAuthToken(ctx)
	level.Debug(s.logger).Log("authToken", authToken)

	clientOpts, err := s.getClientOptions(authToken)
	if err != nil {
		return nil, errors.Wrap(err, "Failed to get argocd clientoptions")
	}
	level.Debug(s.logger).Log("ClientOpts", logUtil.ToString(clientOpts))

	return clientOpts, err
}

func (s *deployService) validateRepository(ctx context.Context, repoURL string, clientOptions *argocdclient.ClientOptions) (*v1alpha1.Repository, error) {
	var repoQuery *repositorypkg.RepoQuery = &repositorypkg.RepoQuery{Repo: repoURL}
	return s.getRepository(ctx, repoQuery, clientOptions)
}

func (s *deployService) initialiseGit(repo *v1alpha1.Repository, appName string, targetRevision string) (git.Client, error) {
	gitClient, err := git.NewClient(repo.Repo, git.DMGetGitCreds(repo), repo.Insecure, repo.EnableLFS)
	if err != nil {
		return nil, err
	}

	msg := fmt.Sprintf("cloning git %s for creating app %s", repo.Repo, appName)
	cloneErr := gitClient.Initialize(targetRevision, msg)
	if cloneErr != nil {
		return nil, errors.Wrapf(cloneErr, "Failed while cloning repo %s", repo.Repo)
	}
	return gitClient, nil
}

func validateDest(root, srcPath string) error {
	fileInfo, err := os.Stat(filepath.Join(root, srcPath))
	if !os.IsNotExist(err) && fileInfo.IsDir() {
		return errorutils.Conflict(errors.New(fmt.Sprintf("Destination folder %s already exists in git repo", srcPath)))
	}
	return nil
}

func (s *deployService) writeManifestArchiveToRepo(repo *v1alpha1.Repository, appName string, manifestArchive fileUtils.InputFile,
	appSource *v1alpha1.ApplicationSource) error {

	s.Config.LockRepo(repo.Repo)
	defer s.Config.UnlockRepo(repo.Repo)

	gitClient, err := s.initialiseGit(repo, appName, appSource.TargetRevision)
	if err != nil {
		return errors.Wrapf(err, "Failed while initializing git client for repo %s", repo.Repo)
	}

	err = validateDest(gitClient.Root(), appSource.Path)
	if err != nil {
		return err
	}

	absDir := filepath.Join(gitClient.Root(), appSource.Path)
	pathUtils.MakeDir(absDir)

	err = fileUtils.UncompressArchive(manifestArchive, absDir, appSource.Path)
	if err != nil {
		return err
	}

	committerInfo := s.Config.GetCommitterInfoForRepo(repo.Repo)
	commitMsg := git.CommitMessageForArchive(appSource.Path, appSource.TargetRevision)
	err = gitClient.Checkin(appSource.Path, commitMsg, appSource.TargetRevision, committerInfo.Name, committerInfo.Email)
	if err != nil {
		return errors.Wrapf(err, "Pushing changes to repo %s failed", appSource.RepoURL)
	}

	if !isHelmArchive(gitClient.Root(), appSource.Path) {
		directoryOpts := &v1alpha1.ApplicationSourceDirectory{
			Recurse: true,
		}
		appSource.Directory = directoryOpts
	}

	return nil

}

func hasChart(appRoot string) bool {
	_, err := os.Stat(filepath.Join(appRoot, "Chart.yaml"))
	return err == nil
}

func isHelmArchive(projectRoot string, sourcePath string) bool {
	appRoot := filepath.Join(projectRoot, sourcePath)
	return hasChart(appRoot)
}

func (s *deployService) deployUserManifest(ctx context.Context, repo *v1alpha1.Repository, packageRequestOptions *PackageRequestMeta,
	manifestArchive *fileUtils.InputFile, clientOptions *argocdclient.ClientOptions) (*v1alpha1.Application, error) {

	err := isAppAlreadyDeployed(ctx, packageRequestOptions, clientOptions)
	if err != nil {
		return nil, err
	}

	err = s.writeManifestArchiveToRepo(repo, packageRequestOptions.ApplicationName, *manifestArchive, &packageRequestOptions.Source)
	if err != nil {
		return nil, err
	}

	application, err := s.createAppln(ctx, packageRequestOptions, clientOptions)

	if err != nil {
		return nil, errors.Wrap(err, "Encountered an error while trying to process the argocd create application request")
	}
	return application, nil
}

func (s *deployService) deployUserCommitedApp(ctx context.Context, repo *v1alpha1.Repository, packageRequestOptions *PackageRequestMeta,
	clientOptions *argocdclient.ClientOptions) (*v1alpha1.Application, error) {

	err := isAppAlreadyDeployed(ctx, packageRequestOptions, clientOptions)
	if err != nil {
		return nil, err
	}

	application, err := s.createAppln(ctx, packageRequestOptions, clientOptions)

	if err != nil {
		return nil, errors.Wrap(err, "Encountered an error while trying to process the argocd create application request")
	}
	return application, nil
}

// PostPackage posts a request to create an application in argo-cd
func (s *deployService) PostPackage(ctx context.Context, packageRequestOptions *PackageRequestMeta,
	manifestArchive *fileUtils.InputFile) (*v1alpha1.Application, error) {

	clientOptions, err := s.initialise(ctx)
	if err != nil {
		return nil, err
	}

	repo, err := s.validateRepository(ctx, packageRequestOptions.Source.RepoURL, clientOptions)
	if err != nil {
		return nil, errors.Wrapf(err, "Error occured while trying to get repository details for %s", packageRequestOptions.Source.RepoURL)
	}
	level.Debug(s.logger).Log("Repo", repo.Repo)

	if manifestArchive != nil {
		return s.deployUserManifest(ctx, repo, packageRequestOptions, manifestArchive, clientOptions)
	} else {
		return s.deployUserCommitedApp(ctx, repo, packageRequestOptions, clientOptions)
	}

}
