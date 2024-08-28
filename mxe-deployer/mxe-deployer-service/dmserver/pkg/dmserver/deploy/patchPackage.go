package deploy

import (
	"context"
	"fmt"
	"path"

	argocdclient "github.com/argoproj/argo-cd/v2/pkg/apiclient"
	applicationpkg "github.com/argoproj/argo-cd/v2/pkg/apiclient/application"
	repositorypkg "github.com/argoproj/argo-cd/v2/pkg/apiclient/repository"
	"github.com/argoproj/argo-cd/v2/pkg/apis/application/v1alpha1"
	argoio "github.com/argoproj/argo-cd/v2/util/io"
	"github.com/go-kit/kit/log/level"
	"github.com/pkg/errors"
	fileUtils "mxe.ericsson/depmanager/utils/file"
	"mxe.ericsson/depmanager/utils/git"
	logUtil "mxe.ericsson/depmanager/utils/log"
	pathUtils "mxe.ericsson/depmanager/utils/path"
)

func (s *deployService) getApplication(ctx context.Context, applicationSelector *applicationpkg.ApplicationQuery, acdClient argocdclient.Client) (*v1alpha1.Application, error) {
	conn, appIf, err := acdClient.NewApplicationClient()
	if err != nil {
		return nil, err
	}
	defer argoio.Close(conn)

	return appIf.Get(ctx, applicationSelector)

}

func replaceArchive(gitClient git.Client, sourcePath string, newArchive *fileUtils.InputFile) error {

	absPath := path.Join(gitClient.Root(), sourcePath)

	err := pathUtils.Cleanup(absPath)
	if err != nil {
		return errors.Wrap(err, "Failed to clean up the existing manifests. Retry..")
	}
	err = fileUtils.UncompressArchive(*newArchive, absPath, sourcePath)
	if err != nil {
		return errors.Wrap(err, "Failed to uncompress the supplied manifest archive for patching")
	}
	return nil
}

//PutPackage processes an update request on an existing application in argo-cd
func (s *deployService) PatchPackage(ctx context.Context, applicationSelector *applicationpkg.ApplicationQuery, updatedManifestArchive *fileUtils.InputFile) (*v1alpha1.Application, error) {

	level.Debug(s.logger).Log("Context is", logUtil.ToString(ctx))

	authToken := getAuthToken(ctx)

	level.Debug(s.logger).Log("authToken is", authToken)

	clientOpts, err := s.getClientOptions(authToken)
	if err != nil {
		return nil, errors.Wrap(err, "Failed to get argocd clientoptions")
	}

	acdClient, err := argocdclient.NewClient(clientOpts)
	if err != nil {
		return nil, errors.Wrap(err, "Failed to instantiate argocd client")
	}

	appln, err := s.getApplication(ctx, applicationSelector, acdClient)
	if err != nil {
		return nil, err
	}

	var repoQuery *repositorypkg.RepoQuery = &repositorypkg.RepoQuery{Repo: appln.Spec.Source.RepoURL}

	repo, err := s.getRepository(ctx, repoQuery, clientOpts)
	if err != nil {
		return nil, errors.Wrapf(err, "Error occured while trying to get repository details for %s", repo.Repo)
	}
	level.Debug(s.logger).Log("\n\nRepo is", repo.Repo)

	err1 := s.pushPatchToGit(repo, appln, updatedManifestArchive)
	if err1 != nil {
		return nil, err1
	}

	syncedAppln, err := appSync(ctx, acdClient, &appln.Name)
	if err != nil {
		return nil, errors.Wrapf(err, "Sync failed for app ", appln.Name)
	}

	return syncedAppln, nil
}

func (s *deployService) pushPatchToGit(repo *v1alpha1.Repository, appln *v1alpha1.Application, updatedManifestArchive *fileUtils.InputFile) error {
	s.Config.LockRepo(repo.Repo)
	defer s.Config.UnlockRepo(repo.Repo)

	gitClient, err := git.NewClient(repo.Repo, git.DMGetGitCreds(repo), repo.Insecure, repo.EnableLFS)
	if err != nil {
		return errors.Wrapf(err, "Failed while initializing git client for repo %s", repo.Repo)
	}

	msg := fmt.Sprintf("cloning git %s for patching app %s", repo.Repo, appln.Name)
	cloneErr := gitClient.Initialize(appln.Spec.Source.TargetRevision, msg)
	if cloneErr != nil {
		return errors.Wrapf(cloneErr, "Failed while cloning repo %s", repo.Repo)
	}

	var resourceName, resourceType, action string = appln.Spec.Source.Path, "manifests", "patch"

	if updatedManifestArchive != nil {
		replaceArchive(gitClient, appln.Spec.Source.Path, updatedManifestArchive)
	} else {
		return errors.New("No manifest/values supplied for patching")
	}

	committerInfo := s.Config.GetCommitterInfoForRepo(repo.Repo)
	commitMessage := git.CommitMessageForPatchPackage(resourceName, resourceType, action, appln.Spec.Source.TargetRevision)
	err = gitClient.Checkin(appln.Spec.Source.Path, commitMessage,
		appln.Spec.Source.TargetRevision, committerInfo.Name, committerInfo.Email)
	if err != nil {
		return errors.Wrapf(err, "Pushing changes to repo %s failed", repo.Repo)
	}
	return nil
}
