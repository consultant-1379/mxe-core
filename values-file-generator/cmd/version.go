package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
)

func NewVersionCommand() *cobra.Command {

	var versionCmd = &cobra.Command{
		Use:   "version",
		Short: fmt.Sprintf("Show the %s command version", cliName),
		Long:  fmt.Sprintf("Shows the %s command version information", cliName),
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println(fmt.Sprintf("%s version: %s", cliName, MXE_CLI_VERSION))
		},
	}
	return versionCmd
}
