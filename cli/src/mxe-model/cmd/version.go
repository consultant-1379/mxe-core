package cmd

import (
	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

var versionCmd = &cobra.Command{
	Use:   "version",
	Short: "Show the mxe-model command version.",
	Long:  `Shows the mxe-model command version information.`,
	Run:   printVersion,
}

func init() {}

func printVersion(cmd *cobra.Command, args []string) {
	utils.LogInfo("mxe-model version: " + utils.MXE_CLI_VERSION)
}
