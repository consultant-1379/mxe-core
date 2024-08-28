package cmd

import (
	"github.com/spf13/cobra"
)

var listCmd = &cobra.Command{
	Use:   "list",
	Short: "Lists the training packages and training jobs.",
	Long:  `Lists the training packages and training jobs.`,
}

func init() {
	listCmd.AddCommand(listPackageCmd)
	listCmd.AddCommand(listJobCmd)
}
