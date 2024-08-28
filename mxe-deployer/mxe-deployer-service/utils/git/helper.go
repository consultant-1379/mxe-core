package git

import (
	"fmt"
	"net/url"
	"os"
	"time"

	"github.com/argoproj/argo-cd/v2/pkg/apis/application/v1alpha1"
	"github.com/pkg/errors"
	log "github.com/sirupsen/logrus"
	"mxe.ericsson/depmanager/utils/cert"
	"mxe.ericsson/depmanager/utils/retry"
)

const (
	emptyString string = " "
)

func DMGetGitCreds(repo *v1alpha1.Repository) Creds {
	if repo == nil {
		return NopCreds{}
	}
	if repo.Username != "" && repo.Password != "" {
		return NewHTTPSCreds(repo.Username, repo.Password, repo.TLSClientCertData, repo.TLSClientCertKey, repo.IsInsecure())
	}
	if repo.SSHPrivateKey != "" {
		return NewSSHCreds(repo.SSHPrivateKey, getCAPath(repo.Repo), repo.IsInsecure())
	}
	return NopCreds{}
}

func getCAPath(repoURL string) string {
	if IsHTTPSURL(repoURL) {
		if parsedURL, err := url.Parse(repoURL); err == nil {
			if caPath, err := cert.GetCertBundlePathForRepository(parsedURL.Host); err == nil {
				return caPath
			} else {
				log.Warnf("Could not get cert bundle path for host '%s'", parsedURL.Host)
			}
		} else {
			// We don't fail if we cannot parse the URL, but log a warning in that
			// case. And we execute the command in a verbatim way.
			log.Warnf("Could not parse repo URL '%s'", repoURL)
		}
	}
	return ""
}

func (gitClient *nativeGitClient) setUser() error {
	err := gitClient.runCredentialedCmd("git", "config", "--global", "user.name", "depmanager")
	if err != nil {
		return err
	}
	err = gitClient.runCredentialedCmd("git", "config", "--global", "user.email", "depmanager@ericsson.com ")
	if err != nil {
		return err
	}
	return nil
}

func clear(folder string) {
	err := os.RemoveAll(folder) // delete an entire directory
	if err != nil {
		fmt.Println(err)
	}
}

func (gitClient *nativeGitClient) Initialize(revision string, msg string) error {
	retriableFunc := func() error {
		return InitializeRepo(revision, gitClient)
	}

	return retry.Retry(DEFAULT_NO_OF_RETRIES, SECONDS_BETWEEN_RETRIES*time.Second, retriableFunc, msg)
}

func InitializeRepo(revision string, gitClient *nativeGitClient) error {

	clear(gitClient.root)

	err := gitClient.Init()
	if err != nil {
		return errors.Wrapf(err, "Failed while initializing repo %s , code version %s using git client",
			gitClient.repoURL, revision)
	}

	err = gitClient.Fetch()
	if err != nil {
		return errors.Wrapf(err, "Failed while fetching remote tags for repo %s , code version %s using git client",
			gitClient.repoURL, revision)
	}

	err = gitClient.Checkout(revision)
	if err != nil {
		return errors.Wrapf(err, "Failed while checking out repo %s , code version %s using git client",
			gitClient.repoURL, revision)
	}

	commitSHA, err := gitClient.CommitSHA()
	if err != nil {
		log.Error(err, "Non fatal error: Failed to get latest commitSHA after checking out repo %s , code version %s using git client",
			gitClient.repoURL, revision)
	} else {
		log.Info("Checked out ", revision, " rev-parse HEAD ", commitSHA)
	}
	return nil
}

func (gitClient *nativeGitClient) Checkin(sourcePath string, commitMsg string, revision string, commitAuthorName string, commitAuthorEmail string) error {

	err := gitClient.AddFiles([]string{sourcePath})
	if err != nil {
		return errors.Wrapf(err, "Encountered an error while adding changes from", sourcePath, "to repo")
	}

	err = gitClient.Commit(commitAuthorName, commitAuthorEmail, commitMsg)
	if err != nil {
		return errors.Wrapf(err, "Encountered an error while committing dir %v to the repo", sourcePath)
	}

	commitSHA, err := gitClient.CommitSHA()
	if err != nil {
		log.Error(err, "Non fatal error: Failed to get latest commitSHA after commiting changes to repo %s , code version %s using git client", gitClient.repoURL, revision)
	} else {
		log.Info("Commit SHA is ", commitSHA)
	}

	err = PushWithRetry(gitClient, revision, fmt.Sprintf("Push to repo for adding %s folder", sourcePath))
	if err != nil {
		return errors.Wrapf(err, "Encountered an error while pushing files %v to the repo", sourcePath)
	}
	return nil
}

func (gitClient *nativeGitClient) RemoveFiles(appName string, sourcePath string, revision string, commitAuthorName string, commitAuthorEmail string) error {

	commitMsg := CommitMessageForDelete(appName, sourcePath, revision)

	err := gitClient.Remove([]string{sourcePath})
	if err != nil {
		return errors.Wrapf(err, "Encountered an error while deleting", sourcePath)
	}

	err = gitClient.Commit(commitAuthorName, commitAuthorEmail, commitMsg)
	if err != nil {
		return errors.Wrapf(err, "Encountered an error while committing to the repo")
	}

	err = PushWithRetry(gitClient, revision, fmt.Sprintf("Push to repo for removing %s corresponding to app %s", sourcePath, appName))
	if err != nil {
		return errors.Wrapf(err, "Encountered an error while pushing files %v to the repo for app %s", sourcePath, appName)
	}
	return nil
}

func PushWithRetry(gitClient *nativeGitClient, revision string, msg string) error {
	retriableFunc := func() error {
		return gitClient.Push(revision)
	}
	return retry.Retry(DEFAULT_NO_OF_RETRIES, SECONDS_BETWEEN_RETRIES*time.Second, retriableFunc, msg)
}
