package config

import (
	"strings"

	appsv1 "github.com/argoproj/argo-cd/v2/pkg/apis/application/v1alpha1"
	"github.com/pkg/errors"
	v1 "k8s.io/api/core/v1"

	"github.com/sirupsen/logrus"
	"mxe.ericsson/depmanager/utils/git"
)

const (
	NO_MATCH = "noMatch"
)

func (c *Config) AddRepository(secret *v1.Secret) {
	c.Store.RepositoryStoreMtx.Lock()
	defer c.Store.RepositoryStoreMtx.Unlock()

	repo, err := c.secretToRepository(secret)
	if err != nil {
		logrus.Error("Error!!", err.Error(), "encountered while converting secret to argocd repository spec")
	}
	c.Store.RepositoryStore[secret.GetName()] = *repo

}

func (c *Config) AddRepositoryCredentials(secret *v1.Secret) {
	c.Store.RepositoryCredsStoreMtx.Lock()
	defer c.Store.RepositoryCredsStoreMtx.Unlock()

	repoCred, err := c.secretToRepoCred(secret)
	if err != nil {
		logrus.Error("Error!!", err.Error(), "encountered while converting secret to argocd repository spec")
	}
	c.Store.RepositoryCredsStore[secret.GetName()] = *repoCred
}

func (c *Config) ModifyRepository(secret *v1.Secret) {
	c.Store.RepositoryStoreMtx.Lock()
	defer c.Store.RepositoryStoreMtx.Unlock()

	repo, err := c.secretToRepository(secret)
	if err != nil {
		logrus.Error("Error!!", err.Error(), "encountered while converting secret to argocd repository spec")
	}

	if _, ok := c.Store.RepositoryStore[secret.GetName()]; ok {
		c.Store.RepositoryStore[secret.GetName()] = *repo
	} else {
		logrus.Error("Repo", repo.Repo, "is not found in repo store. Hence cannot be updated")
	}
}

func (c *Config) ModifyRepositoryCredentials(secret *v1.Secret) {
	c.Store.RepositoryCredsStoreMtx.Lock()
	defer c.Store.RepositoryCredsStoreMtx.Unlock()

	repoCred, err := c.secretToRepoCred(secret)
	if err != nil {
		logrus.Error("Error!!", err.Error(), "encountered while converting secret to argocd repository spec")
	}

	if _, ok := c.Store.RepositoryCredsStore[secret.GetName()]; ok {
		c.Store.RepositoryCredsStore[secret.GetName()] = *repoCred
	} else {
		logrus.Error("Repo", repoCred.URL, "is not found in repo store. Hence cannot be updated")
	}
}

func (c *Config) DeleteRepository(secret *v1.Secret) {
	c.Store.RepositoryStoreMtx.Lock()
	defer c.Store.RepositoryStoreMtx.Unlock()

	if _, ok := c.Store.RepositoryStore[secret.GetName()]; ok {
		delete(c.Store.RepositoryStore, secret.GetName())
	} else {
		logrus.Error("Secret", secret.GetName(), "is not found in repo store. Hence cannot be deleted")
	}
}

func (c *Config) DeleteRepositoryCredentials(secret *v1.Secret) {
	c.Store.RepositoryCredsStoreMtx.Lock()
	defer c.Store.RepositoryCredsStoreMtx.Unlock()

	if _, ok := c.Store.RepositoryCredsStore[secret.GetName()]; ok {
		delete(c.Store.RepositoryCredsStore, secret.GetName())
	} else {
		logrus.Error("Secret", secret.GetName(), "is not found in repo store. Hence cannot be deleted")
	}
}

func (c *Config) LockRepo(repoURL string) {
	c.Store.RepoLocks.Lock(repoURL)
}

func (c *Config) UnlockRepo(repoURL string) {
	c.Store.RepoLocks.Unlock(repoURL)
}

func (c *Config) getRepositoryIndex(repoURL string) string {

	for secretName, repo := range c.Store.RepositoryStore {
		if git.SameURL(repo.Repo, repoURL) {
			return secretName
		}
	}
	return NO_MATCH
}

// getRepositoryCredentialIndex returns the index of the best matching repository credential
// configuration, i.e. the one with the longest match
func (c *Config) getRepositoryCredentialIndex(repoURL string) string {

	var max int = 0
	var secretName string = NO_MATCH
	repoURL = git.NormalizeGitURL(repoURL)
	for i, cred := range c.Store.RepositoryCredsStore {
		credURL := git.NormalizeGitURL(cred.URL)
		if strings.HasPrefix(repoURL, credURL) {
			if len(credURL) > max {
				max = len(credURL)
				secretName = i
			}
		}
	}
	return secretName
}

func (c *Config) copyCredentialsFromRepo(secretName string, repo *appsv1.Repository) error {

	v1Repo := c.Store.RepositoryStore[secretName]
	if HasCredentials(&v1Repo) {
		repo.CopyCredentialsFromRepo(&v1Repo)
	}

	return nil
}

func (c *Config) copyCredentialsFromRepoCreds(secretName string, repo *appsv1.Repository) error {

	repoCreds := c.Store.RepositoryCredsStore[secretName]
	repo.CopyCredentialsFrom(&repoCreds)
	return nil
}

func HasCredentials(repo *appsv1.Repository) bool {
	if repo.HasCredentials() && ((repo.Username != "" && repo.Password != "") || (repo.SSHPrivateKey != "")) {
		return true
	}
	return false
}

func (c *Config) SetCredsForRepo(repo *appsv1.Repository, check_repo_creds_only bool) error {
	c.Store.RepositoryStoreMtx.RLock()
	defer c.Store.RepositoryStoreMtx.RUnlock()

	if !check_repo_creds_only {
		if repoSecretName := c.getRepositoryIndex(repo.Repo); repoSecretName != NO_MATCH {
			// found repo in configured repositories
			err := c.copyCredentialsFromRepo(repoSecretName, repo)
			if err != nil {
				return err
			}
			if HasCredentials(repo) { //credentials are found
				return nil
			}
		}
	}
	if repoCredSecretName := c.getRepositoryCredentialIndex(repo.Repo); repoCredSecretName != NO_MATCH {
		// did not find cred in repo defn, found in credential templates

		return c.copyCredentialsFromRepoCreds(repoCredSecretName, repo)
	}

	return errors.Errorf("Credentials for repository %s is not configured", repo.Repo)
}

func (c *Config) GetCommitterInfoForRepo(repoURL string) CommitAuthor {

	for _, commitAuthor := range c.Store.ConfiguredCommitAuthors {
		if strings.Contains(strings.ToLower(repoURL), strings.ToLower(commitAuthor.Domain)) {
			return commitAuthor
		}
	}

	logrus.Info("Repo", repoURL, " did not match any configured domains. Defaulting commitAuthor to Name:",
		c.Store.DefaultCommitAuthor.Name, " Email:", c.Store.DefaultCommitAuthor.Email)

	return c.Store.DefaultCommitAuthor
}
