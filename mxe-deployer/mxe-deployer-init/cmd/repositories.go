package cmd

import (
	"github.com/spf13/cobra"
	"mxe.ericsson/mxe-deploy-init/pkg/initialiser"
)

func NewRepositoriesInitCommand() *cobra.Command {
	var (
		repositoriesConfigFile string
	)
	var command = &cobra.Command{
		Use:   "repositories",
		Short: "Setup repositories and add to argocd",
		Run: func(c *cobra.Command, args []string) {

			initialiser.SetupRepositories(kubeconfig, deployerNamespace, repositoriesConfigFile)
		},
	}
	command.Flags().StringVar(&repositoriesConfigFile, "repositoriesConfigFile", "", "Location of configuration file containing the repo definition")
	return command
}

func NewGitOpsRepoInitCommand() *cobra.Command {
	var (
		repositoriesConfigFile string
	)
	var command = &cobra.Command{
		Use:   "repositories-init",
		Short: "Init repositories and add to argocd",
		Run: func(c *cobra.Command, args []string) {

			initialiser.SetupArgoCDRepositories(kubeconfig, deployerNamespace, repositoriesConfigFile)
		},
	}
	command.Flags().StringVar(&repositoriesConfigFile, "repositoriesConfigFile", "", "Location of configuration file containing the repo definition")
	return command
}
