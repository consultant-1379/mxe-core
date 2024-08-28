package initialiser

import (
	"context"
	"fmt"
	"io/ioutil"

	log "github.com/sirupsen/logrus"

	"github.com/Nerzal/gocloak/v13"
	"k8s.io/client-go/kubernetes"
	"mxe.ericsson/mxe-deploy-init/pkg/errors"
	"mxe.ericsson/mxe-deploy-init/utils"
)

const (
	oidcSecretKey   = "oidc.keycloak.clientSecret"
	oidcConfigKey   = "oidc.config"
	rbacConfigKey   = "policy.csv"
	adminEnabledKey = "admin.enabled"
)

func parseConfig(deployerNamespace string, kubeClient *kubernetes.Clientset, configFile string) (*utils.ArgocdRealmConfig, error) {

	result, err := utils.UnmarshalKeycloakConfigForArgoCD(deployerNamespace, kubeClient, configFile)
	if err != nil {
		fmt.Printf("Error is %#v", err)
		return nil, err

	}
	log.Debug("\n Parsed config is:", result)

	return result, nil

}

func parseTokenConfig(namespace string, client *kubernetes.Clientset,
	configFile string) (*utils.TokenConfig, error) {

	result, err := utils.UnmarshalTokenOptions(namespace, client, configFile)
	if err != nil {
		fmt.Printf("Error is %#v", err)
		return nil, err
	}
	log.Debug("\n Parsed config is:", result)

	return result, nil

}

func createResources(ctx context.Context, client *gocloak.GoCloak, accessToken string,
	conf *utils.ArgocdRealmConfig) {

	errorHandler := func(err error, realmIDToDelete string) {
		if err != nil {
			log.Errorf("Could not finish setting up the realm in keycloak. Detected Error.. %#v", err)
			if realmIDToDelete != "" {
				log.Info("Rolling back all changes made to keycloak by deleting the realm:", realmIDToDelete)
				client.DeleteRealm(ctx, accessToken, realmIDToDelete)
			}
			errors.Fatal(errors.ErrorGeneric, fmt.Errorf("keycloak init failed"))
		}
	}

	// Create argocdRealm
	log.WithFields(log.Fields{"ctx": ctx, "accessToken": accessToken}).Info("Creating argocd realm")
	realmID, err := client.CreateRealm(ctx, accessToken, *conf.RealmRepresentation)
	errors.CheckError(err)
	log.Info("Created Realm:", realmID)
	//Create clientScope
	log.Info("Creating client scope")
	clientScopeId, err := client.CreateClientScope(ctx, accessToken, realmID, *conf.ClientScope)
	errorHandler(err, realmID)
	log.Info("Created ClientScope:", clientScopeId)
	//Create client
	log.Info("Creating client")
	clientID, err := client.CreateClient(ctx, accessToken, realmID, *conf.Client)
	errorHandler(err, realmID)
	log.Info("Created client:", clientID)
	//Create rest client
	log.Info("Creating rest client")
	restClientID, err := client.CreateClient(ctx, accessToken, realmID, *conf.RestClient)
	errorHandler(err, realmID)
	log.Info("Created rest client:", restClientID)
	//Create Groups
	for _, group := range conf.Groups {
		log.WithFields(log.Fields{"realmID": realmID, "groupName": *group.Name}).Info("Creating group")
		groupID, err := client.CreateGroup(ctx, accessToken, realmID, *group)
		errorHandler(err, realmID)
		log.Info("Created group:", groupID)
	}
	//Create Users
	for _, user := range conf.Users {
		log.WithFields(log.Fields{"realmID": realmID, "userName": *user.KeycloakUser.Username}).Info("Creating user")
		userID, err := client.CreateUser(ctx, accessToken, realmID, *user.KeycloakUser)
		errorHandler(err, realmID)
		log.Info("Created user:", userID)
	}
}

func realmExists(ctx context.Context, client *gocloak.GoCloak, accessToken string, realmId string) bool {
	realms, err := client.GetRealms(ctx, accessToken)
	errors.CheckError(err)
	for _, realm := range realms {
		if *realm.Realm == realmId {
			log.Info("\n Realm %s already exists. Skipping initialisation..", realmId)
			return true
		}
	}
	return false
}

func getClientSecret(ctx context.Context, client *gocloak.GoCloak, accessToken string, realm string,
	clientIDStr string) string {
	clients, err := client.GetClients(ctx, accessToken, realm, gocloak.GetClientsParams{})
	errors.CheckError(err)

	client_id := ""
	for _, client := range clients {
		if *client.ClientID == clientIDStr {
			client_id = *client.ID
			break
		}
	}

	if client_id == "" {
		log.Fatalf("ClientID %s not found:", clientIDStr)
	}

	secret, err := client.GetClientSecret(ctx, accessToken, realm, client_id)
	errors.CheckError(err)

	fmt.Printf("secret: %v", secret)

	return *secret.Value
}

func modifyArgoCDSecret(clientSet *kubernetes.Clientset, deployerNamespace string, secret string) {
	var needsUpdate bool = true

	argocdSecret, err := utils.GetSecret(deployerNamespace, clientSet, utils.ArgocdSecretSelector)
	errors.CheckError(err)

	if val, ok := argocdSecret.Data[oidcSecretKey]; ok {
		if string(val) == secret {
			log.Info(oidcSecretKey, " is already up-to-date. Skipping argocd-secret patch.")
			needsUpdate = false
		}
	}

	if needsUpdate {
		if argocdSecret.Data == nil {
			argocdSecret.Data = map[string][]byte{}
		}
		argocdSecret.Data[oidcSecretKey] = []byte(secret)
		_, err := utils.UpdateSecret(deployerNamespace, clientSet, argocdSecret)
		errors.CheckError(err)
		log.Info(oidcSecretKey, " is updated in argocd-secret")
	}
}

func modifyArgoCDCM(clientSet *kubernetes.Clientset, deployerNamespace string, argoCDOIDCConfigFile string) {
	needsUpdate := true

	argocdCM, err := utils.GetConfigMap(deployerNamespace, clientSet, utils.ArgocdCMLabelSelector)
	errors.CheckError(err)

	oidcConfig, err := ioutil.ReadFile(argoCDOIDCConfigFile)
	errors.CheckError(err)

	if val, ok := argocdCM.Data[oidcConfigKey]; ok {
		if val == string(oidcConfig) {
			log.Info(oidcConfigKey, " is already up-to-date. Skipping argocd-cm patch.")
			needsUpdate = false
		}
	}

	if needsUpdate {
		if argocdCM.Data == nil {
			argocdCM.Data = map[string]string{}
		}
		argocdCM.Data[oidcConfigKey] = string(oidcConfig)
		argocdCM.Data[adminEnabledKey] = string("false")
		_, updateErr := utils.UpdateConfigMap(deployerNamespace, clientSet, argocdCM)
		errors.CheckError(updateErr)
		log.Info(oidcConfigKey, " is updated in argocd-cm")
	}

}

func modifyArgoCDPolicies(clientSet *kubernetes.Clientset, deployerNamespace string,
	argocdRBACConfigFile string) {
	needsUpdate := true

	argocdRBACCM, err := utils.GetConfigMap(deployerNamespace, clientSet, utils.ArgocdRBACCMLabelSelector)
	errors.CheckError(err)

	rbacConfig, err := ioutil.ReadFile(argocdRBACConfigFile)
	errors.CheckError(err)

	if val, ok := argocdRBACCM.Data[rbacConfigKey]; ok {
		if val == string(rbacConfig) {
			needsUpdate = false
			log.Info(rbacConfigKey, " is already up-to-date. Skipping argocd-rbac-cm patch.")
		}
	}

	if needsUpdate {
		if argocdRBACCM.Data == nil {
			argocdRBACCM.Data = map[string]string{}
		}
		argocdRBACCM.Data[rbacConfigKey] = string(rbacConfig)
		_, updateErr := utils.UpdateConfigMap(deployerNamespace, clientSet, argocdRBACCM)
		errors.CheckError(updateErr)
		log.Info(rbacConfigKey, " is updated in argocd-rbac-cm")
	}
}

func setupArgoCDOIDC(clientSet *kubernetes.Clientset, deployerNamespace string,
	secret string, argoCDOIDCConfigFile string, argocdRBACConfigFile string) {
	modifyArgoCDSecret(clientSet, deployerNamespace, secret)
	modifyArgoCDCM(clientSet, deployerNamespace, argoCDOIDCConfigFile)
	modifyArgoCDPolicies(clientSet, deployerNamespace, argocdRBACConfigFile)
}

func SetupArgocdRealm(kubeconfig string, namespace string, deployerNamespace string,
	keycloakConfigFile string, tokenConfigFile string,
	argoCDOIDCConfigFile string, argoCDRBACConfigFile string, wildFlyEnabled bool) {

	log.Info("Received params\n",
		" kubeconfig: ", kubeconfig,
		" deployerNamespace: ", deployerNamespace,
		" namespace: ", namespace,
		" keycloakConfigFile: ", keycloakConfigFile,
		" tokenConfigFile: ", tokenConfigFile,
		" argoCDOIDCConfigFile: ", argoCDOIDCConfigFile,
		" argoCDRBACConfigFile: ", argoCDOIDCConfigFile)

	log.Debug("Creating kubernetes clientset for interacting with the cluster")
	kubeClientSet, err := utils.GetKubeClientSet(kubeconfig)
	errors.CheckError(err)
	log.Debug("Kubernetes clientset is created")

	log.Debug("Parsing keycloak admin token config file: ", tokenConfigFile)
	tokenConf, err := parseTokenConfig(namespace, kubeClientSet, tokenConfigFile)
	errors.CheckError(err)
	log.Debug("keycloak admin token config file:", tokenConfigFile, "is parsed")

	log.Debug("Parsing keycloak realm representation config file: ", keycloakConfigFile)
	keycloakConf, err := parseConfig(deployerNamespace, kubeClientSet, keycloakConfigFile)
	errors.CheckError(err)
	log.Debug("Finished parsing keycloak realm representation config file :", keycloakConfigFile)

	ctx := context.Background()
	log.Debug("Creating keycloak client for hostname ", tokenConf.HostName)
	client := gocloak.NewClient(tokenConf.HostName)
	if wildFlyEnabled {
		gocloak.SetLegacyWildFlySupport()(client)
	}

	log.WithFields(log.Fields{"realm": tokenConf.Realm}).Debug("Requesting keycloak admin token for realm", tokenConf.Realm)
	token, err := client.GetToken(ctx, tokenConf.Realm, *tokenConf.TokenOptions)
	errors.CheckError(err)
	log.Debug("Admin token is retrieved successfully")

	if !realmExists(ctx, client, token.AccessToken, *keycloakConf.RealmRepresentation.Realm) {
		createResources(ctx, client, token.AccessToken, keycloakConf)
	}

	secret := getClientSecret(ctx, client, token.AccessToken, *keycloakConf.RealmRepresentation.Realm, *keycloakConf.Client.ClientID)
	log.Printf("\n Retrived clientSecret %s", secret)
	setupArgoCDOIDC(kubeClientSet, deployerNamespace, secret, argoCDOIDCConfigFile, argoCDRBACConfigFile)
}
