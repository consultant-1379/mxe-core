package cmd

import (
	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

var versionCmd = &cobra.Command{
	Use:   "version",
	Short: "Shows the mxe-service command version information.",
	Long:  `Shows the mxe-service command version information.`,
	Run:   printVersion,
}

func init() {}

func printVersion(cmd *cobra.Command, args []string) {
	utils.LogInfo("mxe-service version: " + utils.MXE_CLI_VERSION)
}
