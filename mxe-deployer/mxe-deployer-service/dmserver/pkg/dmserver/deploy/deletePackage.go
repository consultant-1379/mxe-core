package deploy

import (
	"context"
	"fmt"

	argocdclient "github.com/argoproj/argo-cd/v2/pkg/apiclient"
	"github.com/argoproj/argo-cd/v2/pkg/apiclient/application"
	applicationpkg "github.com/argoproj/argo-cd/v2/pkg/apiclient/application"
	repositorypkg "github.com/argoproj/argo-cd/v2/pkg/apiclient/repository"
	"github.com/argoproj/argo-cd/v2/pkg/apis/application/v1alpha1"
	argoprojio "github.com/argoproj/argo-cd/v2/util/io"
	"github.com/go-kit/kit/log/level"
	"github.com/pkg/errors"
	"mxe.ericsson/depmanager/utils/git"
	logUtil "mxe.ericsson/depmanager/utils/log"
)

func (s *deployService) deletePackage(ctx context.Context, acdClient argocdclient.Client, applicationName *string, propagationPolicy *string) error {

	applnConn, applnClient, err := acdClient.NewApplicationClient()
	if err != nil {
		return err
	}

	delRequest := application.ApplicationDeleteRequest{
		Name: applicationName,
	}
	if propagationPolicy != nil {
		delRequest.PropagationPolicy = propagationPolicy
	}

	defer argoprojio.Close(applnConn)
	applnResponse, err := applnClient.Delete(ctx, &delRequest)
	if err != nil {
		return err
	}

	level.Debug(s.logger).Log("Appln response is %s", logUtil.ToString(applnResponse))
	return nil
}

func (s *deployService) deleteFromGit(ctx context.Context, repo *v1alpha1.Repository, appln *v1alpha1.Application, clientOpts *argocdclient.ClientOptions) error {
	s.Config.LockRepo(appln.Spec.Source.RepoURL)
	defer s.Config.UnlockRepo(appln.Spec.Source.RepoURL)

	gitClient, err := git.NewClient(repo.Repo, git.DMGetGitCreds(repo), repo.Insecure, repo.EnableLFS)
	if err != nil {
		return errors.Wrapf(err, "Failed while initializing git client for repo %s", repo.Repo)
	}

	msg := fmt.Sprintf("cloning git %s for deleting app %s", repo.Repo, appln.Name)
	cloneErr := gitClient.Initialize(appln.Spec.Source.TargetRevision, msg)
	if cloneErr != nil {
		return errors.Wrapf(cloneErr, "Failed while cloning repo %s", repo.Repo)
	}

	committerInfo := s.Config.GetCommitterInfoForRepo(repo.Repo)

	deleteErr := gitClient.RemoveFiles(appln.Name,
		appln.Spec.Source.Path, appln.Spec.Source.TargetRevision, committerInfo.Name, committerInfo.Email)
	if deleteErr != nil {
		return errors.Wrapf(err, "Failed while deleting %s from repo %s, target version %s",
			appln.Spec.Source.Path,
			repo.Repo,
			appln.Spec.Source.TargetRevision)
	}
	return nil
}

func (s *deployService) DeletePackage(ctx context.Context, applicationName *string, propagationPolicy *string) DeleteApplicationResponse {

	authToken := getAuthToken(ctx)
	//fmt.Println("called service with token", authToken)
	clientOpts, err := s.getClientOptions(authToken)
	if err != nil {
		return DeleteApplicationResponse{Status: false, Err: errors.Wrap(err, "Could not instantiate connection to argocd with the given connection options and token")}
	}

	acdClient, err := argocdclient.NewClient(clientOpts)
	if err != nil {
		return DeleteApplicationResponse{Status: false, Err: errors.Wrap(err, "Failed to get argocdclient")}
	}

	applicationSelector := &applicationpkg.ApplicationQuery{Name: applicationName}
	application, err := s.getApplication(ctx, applicationSelector, acdClient)
	if err != nil {
		return DeleteApplicationResponse{Status: false, Err: errors.Wrap(err, "Failed to get application to delete")}
	}

	level.Info(s.logger).Log("\nDelete service for", applicationName)

	var deleteStatus = true

	err = s.deletePackage(ctx, acdClient, applicationName, propagationPolicy)

	if err != nil {
		deleteStatus = false
		level.Error(s.logger).Log("Failed to delete app ", applicationName, " due to error ", err, " from ArgoCD", applicationName, err)
	}

	if deleteStatus {
		var repoQuery *repositorypkg.RepoQuery = &repositorypkg.RepoQuery{
			Repo: application.Spec.Source.RepoURL}

		repo, err := s.getRepository(ctx, repoQuery, clientOpts)
		if err != nil {
			return DeleteApplicationResponse{
				Status: false,
				Err:    errors.Wrapf(err, "Error occured while trying to get repository details for %s", repo.Repo),
			}
		}
		level.Debug(s.logger).Log("\n\nRepo is", repo.Repo)

		err = s.deleteFromGit(ctx, repo, application, clientOpts)
		if err != nil {
			msg := "Application Delete successful but Failed to delete from git. Remove folder from git manually"
			level.Error(s.logger).Log(msg)
			return DeleteApplicationResponse{Status: false, Err: errors.Wrap(err, msg)}
		}

	}
	return DeleteApplicationResponse{Status: deleteStatus, Err: err}
}
