package cmd

import (
	"github.com/spf13/cobra"
)

var deleteCmd = &cobra.Command{
	Use:   "delete",
	Short: "Deletes a training package or a training job.",
	Long:  `Deletes a training package or a training job.`,
}

func init() {
	deleteCmd.AddCommand(deletePackageCmd)
	deleteCmd.AddCommand(deleteJobCmd)
}
