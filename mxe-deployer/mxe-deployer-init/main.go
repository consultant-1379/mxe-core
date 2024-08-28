package main

import (
	"flag"
	"fmt"
	"os"

	"mxe.ericsson/mxe-deploy-init/cmd"
	"mxe.ericsson/mxe-deploy-init/pkg/errors"

	// load the gcp plugin (required to authenticate against GKE clusters).
	_ "k8s.io/client-go/plugin/pkg/client/auth/gcp"
	// load the oidc plugin (required to authenticate with OpenID Connect).
	_ "k8s.io/client-go/plugin/pkg/client/auth/oidc"
	// load the azure plugin (required to authenticate with AKS clusters).
	_ "k8s.io/client-go/plugin/pkg/client/auth/azure"
)

type runTimeOptions struct {
	setupArgoCDRealm   *bool
	keycloakConfigFile *string
	tokenConfigFile    *string
	namespace          *string
	kubeconfig         *string
}

func getHome() string {
	home, err := os.UserHomeDir()
	if err != nil {
		panic("User home is not defined")
	}
	return home
}

func initialise() runTimeOptions {
	var (
		setupArgoCDRealm = flag.Bool("setupArgoCDRealm", true, "Setup Argocd Realm")
		keyCloakConfig   = flag.String("keycloakConfig", "argocdRealmConfig.yaml", "ArgoCD Realm Configuration")
		tokenConfig      = flag.String("keycloakTokenConfig", "keycloakTokenConfig.yaml", "Token Options")
		namespace        = flag.String("keycloakNamespace", "mxe", "keycloak installation namespace")
		kubeconfig       = flag.String("kubeconfig", fmt.Sprintf("%s/.kube/config", getHome()), "absolute path to the kubeconfig file")
	)
	flag.Parse()

	return runTimeOptions{setupArgoCDRealm: setupArgoCDRealm,
		keycloakConfigFile: keyCloakConfig,
		tokenConfigFile:    tokenConfig,
		namespace:          namespace,
		kubeconfig:         kubeconfig}

}

func main() {
	err := cmd.NewCommand().Execute()
	errors.CheckError(err)
}
