package cmd

import (
	"fmt"

	"mxe.ericsson/mxe-deploy-init/utils"

	"github.com/spf13/cobra"
)

var (
	kubeconfig        string
	namespace         string
	logFormat         string
	logLevel          string
	deployerNamespace string
)

func init() {
	cobra.OnInitialize(initConfig)
}

func initConfig() {
	utils.SetLogFormat(logFormat)
	utils.SetLogLevel(logLevel)
	utils.SetReportCaller()
}

// NewCommand returns a new instance of an argocd command
func NewCommand() *cobra.Command {

	var command = &cobra.Command{
		Use:   cliName,
		Short: fmt.Sprintf("%s initialises a Deployment Manager server", cliName),
		Run: func(c *cobra.Command, args []string) {
			c.HelpFunc()(c, args)
		},
		DisableAutoGenTag: true,
	}
	command.AddCommand(NewKeycloakInitCommand())
	command.AddCommand(NewRepositoriesInitCommand())
	command.AddCommand(NewGitOpsRepoInitCommand())
	command.AddCommand(NewGitUsersInitCommand())

	command.PersistentFlags().StringVar(&kubeconfig, "kubeconfig", fmt.Sprintf("%s/.kube/config", GetHome()), "Optional Path to kubeconfig file")
	command.PersistentFlags().StringVar(&namespace, "namespace", "", "Namespace where mxe is installed")
	command.PersistentFlags().StringVar(&deployerNamespace, "deployer-namespace", "", "Namespace where mxe-deployer is installed")
	command.PersistentFlags().StringVar(&logFormat, "logFormat", "text", "one of text/json")
	command.PersistentFlags().StringVar(&logLevel, "logLevel", "debug", "one of debug|info|warn|error")
	return command
}
