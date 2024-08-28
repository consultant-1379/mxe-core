package utils

import (
	"fmt"
	"io/ioutil"
	"strings"

	"net/url"

	"github.com/Nerzal/gocloak/v13"
	"github.com/argoproj/argo-cd/v2/util/settings"
	log "github.com/sirupsen/logrus"
	apiv1 "k8s.io/api/core/v1"
	"k8s.io/client-go/kubernetes"
	"mxe.ericsson/mxe-deploy-init/pkg/errors"
	"sigs.k8s.io/yaml"
)

type GitURL struct {
	RepoURL      string
	parsedURL    *url.URL
	HostURL      string
	Organisation string
	RepoName     string
}

func (gitURL *GitURL) Parse() {
	u, err := url.Parse(gitURL.RepoURL)
	errors.CheckError(err)

	orgAndRepo := strings.Split(strings.TrimLeft(u.Path, "/"), "/")

	if len(orgAndRepo) != 2 {
		log.Fatal("Received org and repo", u.Path, "has ", len(orgAndRepo), " components. Expected only 2")
	}

	gitURL.parsedURL = u
	gitURL.Organisation = orgAndRepo[0]
	gitURL.RepoName = strings.TrimRight(orgAndRepo[1], ".git")

	hostURL := url.URL{Scheme: u.Scheme, Host: u.Host}
	gitURL.HostURL = hostURL.String()
}

type SecretSelectors struct {
	UsernameSelector      *apiv1.SecretKeySelector `json:"usernameSecret,omitempty"`
	PasswordSelector      *apiv1.SecretKeySelector `json:"passwordSecret,omitempty"`
	SSHPrivateKeySelector *apiv1.SecretKeySelector `json:"sshPrivateKeySecret,omitempty"`
}

type MxeUser struct {
	KeycloakUser *gocloak.User    `json:"keycloakUser,omitempty"`
	Secrets      *SecretSelectors `json:"secrets,omitempty"`
}

func (u *MxeUser) UpdatePassword(namespace string, client *kubernetes.Clientset) {

	userName := u.KeycloakUser.Username
	password := (*u.KeycloakUser.Credentials)[0].Value

	if (userName == nil || *userName == "") && (password == nil || *password == "") {
		if u.Secrets != nil && (u.Secrets.UsernameSelector != nil && u.Secrets.PasswordSelector != nil) {

			log.Debug("Reading username key: ", u.Secrets.UsernameSelector.Key, " from secret: ", u.Secrets.UsernameSelector.Name, "from namespace", namespace)
			user, err := GetSecretRefValue(namespace, client, u.Secrets.UsernameSelector)
			errors.CheckError(err)
			log.Debug("Username is: ", user)

			log.Debug("Reading password key: ", u.Secrets.PasswordSelector.Key, " from secret: ", u.Secrets.PasswordSelector.Name, "from namespace", namespace)
			pass, err := GetSecretRefValue(namespace, client, u.Secrets.PasswordSelector)
			errors.CheckError(err)
			log.Debug("Read password successfully")

			u.KeycloakUser.Username = &user
			(*u.KeycloakUser.Credentials)[0].Value = &pass

		} else {
			log.Fatalln("User/Password selector is not set")
		}
	}
}

type ArgocdRealmConfig struct {
	RealmRepresentation *gocloak.RealmRepresentation `json:"realm,omitempty"`
	ClientScope         *gocloak.ClientScope         `json:"clientScope,omitempty"`
	Client              *gocloak.Client              `json:"client,omitempty"`
	RestClient          *gocloak.Client              `json:"restClient,omitempty"`
	Groups              []*gocloak.Group             `json:"groups,omitempty"`
	Users               []MxeUser                    `json:"users,omitempty"`
}

type TokenOptionsProvider struct {
	HostName         string                   `json:"hostname,omitempty"`
	Realm            string                   `json:"realm,omitempty"`
	ClientID         *string                  `json:"client_id,omitempty"`
	GrantType        *string                  `json:"grant_type,omitempty"`
	UsernameSelector *apiv1.SecretKeySelector `json:"usernameSecret,omitempty"`
	PasswordSelector *apiv1.SecretKeySelector `json:"passwordSecret,omitempty"`
}

type TokenConfig struct {
	HostName     string
	Realm        string
	TokenOptions *gocloak.TokenOptions
}

type AdminCredentials struct {
	UserNameSelector *apiv1.SecretKeySelector `json:"usernameSecret,omitempty"`
	PasswordSelector *apiv1.SecretKeySelector `json:"passwordSecret,omitempty"`
}

type GitOpsRepo struct {
	Repo                *settings.Repository `json:"repo,omitempty"`
	UserEmail           string               `json:"userEmail,omitempty"`
	ShouldCreate        bool                 `json:"shouldCreate,omitempty"`
	Private             *bool                `json:"private,omitempty"`
	GitAdminCredentials *AdminCredentials    `json:"gitAdminCredentials,omitempty"`
}

type ArgocdRepositoriesConfig struct {
	MXEHelmRepo   *settings.Repository `json:"mxeHelmRepo,omitempty"`
	MXEGitOpsRepo *GitOpsRepo          `json:"mxeGitopsRepo,omitempty"`
	ArgoCDUIURL   string               `json:"url,omitempty"`
}

func UnmarshalRepositoriesConfigForArgoCD(configFile string) (*ArgocdRepositoriesConfig, error) {
	buf, err := ioutil.ReadFile(configFile)
	log.Info("repos is:", string(buf))
	if err != nil {
		return nil, err
	}

	conf := &ArgocdRepositoriesConfig{}
	err = yaml.Unmarshal(buf, conf)
	if err != nil {
		return nil, fmt.Errorf("in file %q: %v", configFile, err)
	}

	// if config doesn't has Private flag defined, assign default value as true
	if conf.MXEGitOpsRepo.Private == nil {
		private := true
		conf.MXEGitOpsRepo.Private = &private
	}
	return conf, nil
}

func UnmarshalKeycloakConfigForArgoCD(deployerNamespace string, kubeClient *kubernetes.Clientset, configFile string) (*ArgocdRealmConfig, error) {
	log.Info("Reading keycloak realm representation config from: ", configFile)
	buf, err := ioutil.ReadFile(configFile)
	if err != nil {
		return nil, err
	}

	conf := &ArgocdRealmConfig{}
	err = yaml.Unmarshal(buf, conf)
	if err != nil {
		return nil, fmt.Errorf("in file %q: %v", configFile, err)
	}

	for _, user := range conf.Users {
		user.UpdatePassword(deployerNamespace, kubeClient)
	}
	log.Debug("Parsed Realm representation successfully")

	return conf, nil
}

func UnmarshalTokenOptions(namespace string, client *kubernetes.Clientset, configFile string) (*TokenConfig, error) {

	buf, err := ioutil.ReadFile(configFile)
	if err != nil {
		return nil, err
	}

	conf := &TokenOptionsProvider{}
	err = yaml.Unmarshal(buf, conf)
	if err != nil {
		return nil, fmt.Errorf("in file %q: %v", configFile, err)
	}

	log.Debug("Reading username key: ", conf.UsernameSelector.Key, " from secret: ", conf.UsernameSelector.Name, " from namespace ", namespace)
	userName, err := GetSecretRefValue(namespace, client, conf.UsernameSelector)
	if err != nil {
		return nil, err
	}
	log.Debug("Username is", userName)

	log.Debug("Reading password key:", conf.PasswordSelector.Key, " from secret: ", conf.PasswordSelector.Name, " from namespace ", namespace)
	password, err := GetSecretRefValue(namespace, client, conf.PasswordSelector)
	if err != nil {
		return nil, err
	}
	log.Debug("Password is read successfully")

	tokenConfig := TokenConfig{
		conf.HostName,
		conf.Realm,
		&gocloak.TokenOptions{
			ClientID:  conf.ClientID,
			Username:  &userName,
			Password:  &password,
			GrantType: conf.GrantType}}

	log.Debug("Token config is read successfully")

	return &tokenConfig, nil

}
