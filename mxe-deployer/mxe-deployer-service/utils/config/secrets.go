package config

import (
	"strconv"

	appsv1 "github.com/argoproj/argo-cd/v2/pkg/apis/application/v1alpha1"
	v1 "k8s.io/api/core/v1"
)

func (c *Config) secretToRepoCred(secret *v1.Secret) (*appsv1.RepoCreds, error) {
	repository := &appsv1.RepoCreds{
		URL:                        string(secret.Data["url"]),
		Username:                   string(secret.Data["username"]),
		Password:                   string(secret.Data["password"]),
		SSHPrivateKey:              string(secret.Data["sshPrivateKey"]),
		TLSClientCertData:          string(secret.Data["tlsClientCertData"]),
		TLSClientCertKey:           string(secret.Data["tlsClientCertKey"]),
		Type:                       string(secret.Data["type"]),
		GithubAppPrivateKey:        string(secret.Data["githubAppPrivateKey"]),
		GitHubAppEnterpriseBaseURL: string(secret.Data["githubAppEnterpriseBaseUrl"]),
	}

	enableOCI, err := boolOrFalse(secret, "enableOCI")
	if err != nil {
		return repository, err
	}
	repository.EnableOCI = enableOCI

	githubAppID, err := intOrZero(secret, "githubAppID")
	if err != nil {
		return repository, err
	}
	repository.GithubAppId = githubAppID

	githubAppInstallationID, err := intOrZero(secret, "githubAppInstallationID")
	if err != nil {
		return repository, err
	}
	repository.GithubAppInstallationId = githubAppInstallationID

	return repository, nil
}

func boolOrFalse(secret *v1.Secret, key string) (bool, error) {
	val, present := secret.Data[key]
	if !present {
		return false, nil
	}

	return strconv.ParseBool(string(val))
}

func intOrZero(secret *v1.Secret, key string) (int64, error) {
	val, present := secret.Data[key]
	if !present {
		return 0, nil
	}

	return strconv.ParseInt(string(val), 10, 64)
}

func (c *Config) secretToRepository(secret *v1.Secret) (*appsv1.Repository, error) {
	repository := &appsv1.Repository{
		Name:                       string(secret.Data["name"]),
		Repo:                       string(secret.Data["url"]),
		Username:                   string(secret.Data["username"]),
		Password:                   string(secret.Data["password"]),
		SSHPrivateKey:              string(secret.Data["sshPrivateKey"]),
		TLSClientCertData:          string(secret.Data["tlsClientCertData"]),
		TLSClientCertKey:           string(secret.Data["tlsClientCertKey"]),
		Type:                       string(secret.Data["type"]),
		GithubAppPrivateKey:        string(secret.Data["githubAppPrivateKey"]),
		GitHubAppEnterpriseBaseURL: string(secret.Data["githubAppEnterpriseBaseUrl"]),
		Proxy:                      string(secret.Data["proxy"]),
	}

	insecureIgnoreHostKey, err := boolOrFalse(secret, "insecureIgnoreHostKey")
	if err != nil {
		return repository, err
	}
	repository.InsecureIgnoreHostKey = insecureIgnoreHostKey

	insecure, err := boolOrFalse(secret, "insecure")
	if err != nil {
		return repository, err
	}
	repository.Insecure = insecure

	enableLfs, err := boolOrFalse(secret, "enableLfs")
	if err != nil {
		return repository, err
	}
	repository.EnableLFS = enableLfs

	enableOCI, err := boolOrFalse(secret, "enableOCI")
	if err != nil {
		return repository, err
	}
	repository.EnableOCI = enableOCI

	githubAppID, err := intOrZero(secret, "githubAppID")
	if err != nil {
		return repository, err
	}
	repository.GithubAppId = githubAppID

	githubAppInstallationID, err := intOrZero(secret, "githubAppInstallationID")
	if err != nil {
		return repository, err
	}
	repository.GithubAppInstallationId = githubAppInstallationID

	return repository, nil
}
