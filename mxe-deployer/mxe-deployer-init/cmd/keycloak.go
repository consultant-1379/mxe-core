package cmd

import (
	"fmt"

	"mxe.ericsson/mxe-deploy-init/pkg/initialiser"

	log "github.com/sirupsen/logrus"
	"github.com/spf13/cobra"
)

func NewKeycloakInitCommand() *cobra.Command {
	var (
		enabled                 bool
		keycloakRealmConfigFile string
		keycloakTokenConfigFile string
		argoCDOIDCConfigFile    string
		argoCDRBACConfigFile    string
		wildFlyEnabled          bool
	)
	var command = &cobra.Command{
		Use:   "keycloak-realm",
		Short: "Initialise argocd realm",
		Run: func(c *cobra.Command, args []string) {
			fmt.Printf("%v", args)
			if !enabled {
				log.Info("Keycloak init is disabled")
			} else {
				if keycloakRealmConfigFile == "" || keycloakTokenConfigFile == "" || argoCDOIDCConfigFile == "" {
					log.Fatalln("keycloakRealmConfigFile/keycloakTokenConfigFile/argoCDOIDCConfigFile cannot be empty")
				}
				initialiser.SetupArgocdRealm(kubeconfig, namespace, deployerNamespace, keycloakRealmConfigFile, keycloakTokenConfigFile, argoCDOIDCConfigFile, argoCDRBACConfigFile, wildFlyEnabled)
			}
		},
	}
	command.Flags().StringVar(&keycloakRealmConfigFile, "keycloakRealmConfigFile", "", "Location of configuration file containing the realm definition")
	command.Flags().StringVar(&keycloakTokenConfigFile, "keycloakTokenConfigFile", "", "Location of configuration file containing keycloak credentials")
	command.Flags().StringVar(&argoCDOIDCConfigFile, "argoCDOIDCConfigFile", "", "Location of configuration file containing keycloak credentials")
	command.Flags().StringVar(&argoCDRBACConfigFile, "argoCDRBACConfigFile", "", "Location of configuration file containing argocd rbac credentials")
	command.Flags().BoolVar(&wildFlyEnabled, "enableLegacyWildFlySupport", true, "Enable keycloak legacy wildfly support")
	command.Flags().BoolVar(&enabled, "enabled", false, "Command is bypassed if this flag is disabled")
	command.MarkFlagRequired("enabled")
	return command
}
