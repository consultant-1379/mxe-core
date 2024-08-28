package cmd

import (
	"github.com/spf13/cobra"
	"mxe.ericsson/mxe-deploy-init/pkg/initialiser"
)

func NewGitUsersInitCommand() *cobra.Command {
	var command = &cobra.Command{
		Use:   "create-git-users",
		Short: "Create git users",
		Run: func(c *cobra.Command, args []string) {
			initialiser.CreateGitUsers()
		},
	}
	return command
}
