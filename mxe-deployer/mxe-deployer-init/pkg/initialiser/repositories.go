package initialiser

import (
	"fmt"
	"strings"

	"code.gitea.io/sdk/gitea"
	log "github.com/sirupsen/logrus"
	"sigs.k8s.io/yaml"

	v1 "k8s.io/api/core/v1"
	"k8s.io/client-go/kubernetes"
	"mxe.ericsson/mxe-deploy-init/pkg/errors"
	"mxe.ericsson/mxe-deploy-init/utils"

	"github.com/argoproj/argo-cd/v2/util/settings"
)

const (
	repositoriesKey = "repositories"
	argocdURLKey    = "url"
)

func updateURLInArgoCDCM(clientSet *kubernetes.Clientset, deployerNamespace string, argocdURL string) (*v1.ConfigMap, bool) {

	argocdCM, err := utils.GetConfigMap(deployerNamespace, clientSet, utils.ArgocdCMLabelSelector)
	errors.CheckError(err)

	return updateURLInCM(clientSet, deployerNamespace, argocdURL, argocdCM)
}

func updateURLInCM(clientSet *kubernetes.Clientset, deployerNamespace string, argocdURL string, argocdCM *v1.ConfigMap) (*v1.ConfigMap, bool) {
	var updateURL bool = true

	if url, ok := argocdCM.Data[argocdURLKey]; ok {
		if argocdURL == url {
			updateURL = false
		}
	}

	if updateURL {
		argocdCM.Data[argocdURLKey] = argocdURL
		log.Info("ArgoCD URL is updated in argocd-cm as ", argocdURL)
	}
	return argocdCM, updateURL
}

func updateReposAndURLInArgoCDCM(clientSet *kubernetes.Clientset, deployerNamespace string, ymlStr string, argocdURL string) {
	var updateRepos bool = true

	argocdCM, err := utils.GetConfigMap(deployerNamespace, clientSet, utils.ArgocdCMLabelSelector)
	errors.CheckError(err)

	//If "repositories" section already exists and has data do not update
	if repos, ok := argocdCM.Data[repositoriesKey]; ok {
		if len(repos) > 0 {
			updateRepos = false
		}
	}

	if updateRepos {
		if argocdCM.Data == nil {
			argocdCM.Data = map[string]string{}
		}
		argocdCM.Data[repositoriesKey] = ymlStr
	}
	argocdCM, updateURL := updateURLInCM(clientSet, deployerNamespace, argocdURL, argocdCM)
	if updateURL || updateRepos {
		_, updateErr := utils.UpdateConfigMap(deployerNamespace, clientSet, argocdCM)
		errors.CheckError(updateErr)
		log.Info("repositories and url is updated in argocd-cm")
	}

}

func configToArgoCDRepos(parsedRepos *utils.ArgocdRepositoriesConfig) []*settings.Repository {
	var repositories = []*settings.Repository{}
	repositories = append(repositories, parsedRepos.MXEHelmRepo, parsedRepos.MXEGitOpsRepo.Repo)
	return repositories
}

func argocdReposToYAML(argocdRepos []*settings.Repository) string {
	yml, err := yaml.Marshal(argocdRepos)
	errors.CheckError(err)
	return string(yml)
}

func CreateUser(giteaClient *gitea.Client, userName, userPassword, userEmail string) {
	createUser := true
	mustChangePassword := false
	userOption := gitea.CreateUserOption{Username: userName, Password: userPassword, Email: userEmail, MustChangePassword: &mustChangePassword}

	gitUsers, response, err := giteaClient.AdminListUsers(gitea.AdminListUsersOptions{})
	errors.CheckError(err)
	log.Info("List users command returned status code ", response.StatusCode)

	for _, gitUser := range gitUsers {
		if gitUser.UserName == userName {
			createUser = false
			log.Info("Skipping User creation. User:", userName, " already exists")
			break
		}
	}

	if createUser {
		_, userCreateResponse, err := giteaClient.AdminCreateUser(userOption)
		errors.CheckError(err)
		log.Info("User ", userName, " Creation request returned status: ", userCreateResponse.StatusCode)
	}

}

func CreateUserOrg(giteaClient *gitea.Client, organisation string) {
	createOrg := true
	orgs, response, err := giteaClient.ListMyOrgs(gitea.ListOrgsOptions{})
	errors.CheckError(err)
	log.Info("List user orgs command returned status code ", response.StatusCode)

	for _, org := range orgs {
		if org.UserName == organisation {
			createOrg = false
			log.Info("Skipping Org creation. Org:", organisation, " already exists")
			break
		}
	}

	if createOrg {
		_, orgCreateResponse, err := giteaClient.CreateOrg(gitea.CreateOrgOption{Name: organisation})
		errors.CheckError(err)
		log.Info("Org ", organisation, "Creation request returned status:", orgCreateResponse.StatusCode)

	}
}

func createOrgRepo(giteaClient *gitea.Client, organisation string, repoName string, isPrivate bool) {
	createRepo := true
	repos, response, err := giteaClient.ListOrgRepos(organisation, gitea.ListOrgReposOptions{})
	errors.CheckError(err)
	log.Info("List repos command for org:", organisation, "returned status code ", response.StatusCode)

	for _, repo := range repos {
		if repo.Name == repoName {
			createRepo = false
			log.Info("Skipping Repo creation. Repo:", repoName, " in Org:", organisation, " already exists")
			break
		}
	}

	if createRepo {
		_, repoCreateResponse, err := giteaClient.CreateOrgRepo(organisation, gitea.CreateRepoOption{Name: repoName, AutoInit: true, Private: isPrivate})
		errors.CheckError(err)
		log.Info("Repo ", repoName, "Creation request returned status:", repoCreateResponse.StatusCode)
	}

}

func adminCreateRepo(giteaClient *gitea.Client, organisation string, repoName string, isPrivate bool) {

	_, repoCreateResponse, err := giteaClient.AdminCreateRepo(organisation, gitea.CreateRepoOption{Name: repoName, AutoInit: true, Private: isPrivate})
	if err != nil {
		if strings.Contains(err.Error(), "repo already exists") {
			log.Infof("repo %s under org %s already exists", repoName, organisation)
			return
		}
		errors.CheckError(err)
	}
	log.Info("Repo", repoName, "Creation request returned status:", repoCreateResponse.StatusCode)

}

func createOrgHook(giteaClient *gitea.Client, organisation string, argocdURL string) {

	const (
		json_content_type = "json"
		hookName          = "argocd-webhook"
	)
	createHook := true
	webhookURL := fmt.Sprintf("%s/api/webhook", argocdURL)

	hooks, listResp, listErr := giteaClient.ListOrgHooks(organisation, gitea.ListHooksOptions{})
	log.Info("List org hooks for org: ", organisation, " returned status:", listResp.StatusCode)
	errors.CheckError(listErr)

	// check if hook already exists, skip hook creation
	for _, hook := range hooks {
		if hook.Config["url"] == webhookURL {
			createHook = false
			log.Info("Org Hook for URL ", webhookURL, " already exists for org: ", organisation)
			break
		}
	}

	//createHook if it does not exist
	if createHook {

		// need only push events
		events := []string{"push"}

		hookCfg := map[string]string{
			"url":          webhookURL,
			"content_type": json_content_type,
			"name":         hookName,
		}

		_, createResp, createErr := giteaClient.CreateOrgHook(organisation, gitea.CreateHookOption{
			Type:         gitea.HookTypeGogs,
			BranchFilter: "*",
			Config:       hookCfg,
			Active:       true,
			Events:       events,
		})
		log.Info("OrgWebhook creation request for Org: ", organisation, " URL:", webhookURL, " returned status:", createResp.StatusCode)
		errors.CheckError(createErr)
	}
}

func createRepoInGit(gitOpsRepo utils.GitOpsRepo, deployerNamespace string, k8sClient *kubernetes.Clientset) {

	gitURL := utils.GitURL{RepoURL: gitOpsRepo.Repo.URL}
	gitURL.Parse()

	giteaClient, err := gitea.NewClient(gitURL.HostURL)
	errors.CheckError(err)

	adminUserName, err := utils.GetSecretRefValue(deployerNamespace, k8sClient, gitOpsRepo.GitAdminCredentials.UserNameSelector)
	errors.CheckError(err)
	adminPassword, err := utils.GetSecretRefValue(deployerNamespace, k8sClient, gitOpsRepo.GitAdminCredentials.PasswordSelector)
	errors.CheckError(err)

	giteaClient.SetBasicAuth(adminUserName, adminPassword)

	if gitOpsRepo.Repo.UsernameSecret != nil && gitOpsRepo.Repo.PasswordSecret != nil {
		//create user
		userName, err := utils.GetSecretRefValue(deployerNamespace, k8sClient, gitOpsRepo.Repo.UsernameSecret)
		errors.CheckError(err)
		userPassword, err := utils.GetSecretRefValue(deployerNamespace, k8sClient, gitOpsRepo.Repo.PasswordSecret)
		errors.CheckError(err)

		CreateUser(giteaClient, userName, userPassword, gitOpsRepo.UserEmail)
		giteaClient.SetBasicAuth(userName, userPassword)

		CreateUserOrg(giteaClient, gitURL.Organisation)

		//create repo under org
		createOrgRepo(giteaClient, gitURL.Organisation, gitURL.RepoName, *gitOpsRepo.Private)
	}

	if gitOpsRepo.Repo.UsernameSecret == nil && gitOpsRepo.Repo.PasswordSecret == nil {
		//create repo under org using admin credentials
		adminCreateRepo(giteaClient, gitURL.Organisation, gitURL.RepoName, *gitOpsRepo.Private)
	}
}

/* old way of setting repositories, deprecated*/
func SetupRepositories(kubeconfig string, deployerNamespace string, repositoriesConfigFile string) {

	k8sClient, err := utils.GetKubeClientSet(kubeconfig)
	errors.CheckError(err)

	parsedInfo, err := utils.UnmarshalRepositoriesConfigForArgoCD(repositoriesConfigFile)
	errors.CheckError(err)
	if parsedInfo.MXEGitOpsRepo.ShouldCreate {
		if parsedInfo.MXEGitOpsRepo.Repo.UsernameSecret == nil &&
			parsedInfo.MXEGitOpsRepo.Repo.PasswordSecret == nil &&
			parsedInfo.MXEGitOpsRepo.Repo.SSHPrivateKeySecret != nil {
			errors.CheckError(
				fmt.Errorf("SSH Based access for Internal Git repos cannot be setup currently"))
		}
		createRepoInGit(*parsedInfo.MXEGitOpsRepo, deployerNamespace, k8sClient)
	}

	if parsedInfo.ArgoCDUIURL != "" {
		argocdRepos := configToArgoCDRepos(parsedInfo)
		ymlStr := argocdReposToYAML(argocdRepos)
		updateReposAndURLInArgoCDCM(k8sClient, deployerNamespace, ymlStr, parsedInfo.ArgoCDUIURL)
	}
}

func createRepoInGitWithWebhook(gitOpsRepo utils.GitOpsRepo, deployerNamespace string, k8sClient *kubernetes.Clientset, argocdURL string) {

	gitURL := utils.GitURL{RepoURL: gitOpsRepo.Repo.URL}
	gitURL.Parse()

	giteaClient, err := gitea.NewClient(gitURL.HostURL)
	errors.CheckError(err)

	adminUserName, err := utils.GetSecretRefValue(deployerNamespace, k8sClient, gitOpsRepo.GitAdminCredentials.UserNameSelector)
	errors.CheckError(err)
	adminPassword, err := utils.GetSecretRefValue(deployerNamespace, k8sClient, gitOpsRepo.GitAdminCredentials.PasswordSelector)
	errors.CheckError(err)

	giteaClient.SetBasicAuth(adminUserName, adminPassword)

	if gitOpsRepo.Repo.UsernameSecret != nil && gitOpsRepo.Repo.PasswordSecret != nil {
		//create user
		userName, err := utils.GetSecretRefValue(deployerNamespace, k8sClient, gitOpsRepo.Repo.UsernameSecret)
		errors.CheckError(err)
		userPassword, err := utils.GetSecretRefValue(deployerNamespace, k8sClient, gitOpsRepo.Repo.PasswordSecret)
		errors.CheckError(err)

		CreateUser(giteaClient, userName, userPassword, gitOpsRepo.UserEmail)
		giteaClient.SetBasicAuth(userName, userPassword)

		CreateUserOrg(giteaClient, gitURL.Organisation)

		//create repo under org
		createOrgRepo(giteaClient, gitURL.Organisation, gitURL.RepoName, *gitOpsRepo.Private)

		createOrgHook(giteaClient, gitURL.Organisation, argocdURL)
	}

	if gitOpsRepo.Repo.UsernameSecret == nil && gitOpsRepo.Repo.PasswordSecret == nil {
		//create repo under org using admin credentials
		adminCreateRepo(giteaClient, gitURL.Organisation, gitURL.RepoName, *gitOpsRepo.Private)
	}
}

func SetupArgoCDRepositories(kubeconfig string, deployerNamespace string, repositoriesConfigFile string) {

	k8sClient, err := utils.GetKubeClientSet(kubeconfig)
	errors.CheckError(err)

	parsedInfo, err := utils.UnmarshalRepositoriesConfigForArgoCD(repositoriesConfigFile)
	errors.CheckError(err)
	if parsedInfo.MXEGitOpsRepo.ShouldCreate {
		if parsedInfo.MXEGitOpsRepo.Repo.UsernameSecret == nil &&
			parsedInfo.MXEGitOpsRepo.Repo.PasswordSecret == nil &&
			parsedInfo.MXEGitOpsRepo.Repo.SSHPrivateKeySecret != nil {
			errors.CheckError(
				fmt.Errorf("SSH Based access for Internal Git repos cannot be setup currently"))
		}
		createRepoInGitWithWebhook(*parsedInfo.MXEGitOpsRepo, deployerNamespace, k8sClient, parsedInfo.ArgoCDUIURL)
	}

	if parsedInfo.ArgoCDUIURL != "" {
		argocdCM, updateURL := updateURLInArgoCDCM(k8sClient, deployerNamespace, parsedInfo.ArgoCDUIURL)
		if updateURL {
			_, updateErr := utils.UpdateConfigMap(deployerNamespace, k8sClient, argocdCM)
			errors.CheckError(updateErr)
		}
	}
}
