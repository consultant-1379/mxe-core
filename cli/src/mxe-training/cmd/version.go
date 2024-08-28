package cmd

import (
	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

var versionCmd = &cobra.Command{
	Use:   "version",
	Short: "Shows the mxe-training command version.",
	Long:  `Shows the mxe-training command version.`,
	Run:   printVersion,
}

func init() {}

func printVersion(cmd *cobra.Command, args []string) {
	utils.LogInfo("mxe-training version: " + utils.MXE_CLI_VERSION)
}
