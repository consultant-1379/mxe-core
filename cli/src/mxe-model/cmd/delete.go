package cmd

import (
	"errors"
	"fmt"

	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

var deleteId string
var deleteVersion string

var deleteCmd = &cobra.Command{
	Use:     "delete",
	Short:   "Deletes a version of a model from the cluster.",
	Long:    `Deletes a version of a model from the cluster.`,
	Example: `mxe-model delete --id com.ericsson.bdgs.oss.oss.eea.aio --version 0.0.1 `,
	Run:     runDelete,
}

func deleteCmdSetArgs(a []string) {
	deleteCmd.SetArgs(a)
}

func init() {
	deleteCmd.PersistentFlags().StringVar(&deleteId, "id", "", "Model identifier.")
	deleteCmd.PersistentFlags().StringVar(&deleteVersion, "version", "", "Model version to delete.")
	deleteCmd.MarkPersistentFlagRequired("id")
	deleteCmd.MarkPersistentFlagRequired("version")
}

func runDelete(cmd *cobra.Command, args []string) {
	utils.Verbose = verbose

	if deleteVersion != "unknown" && !utils.IsValidModelVersion(deleteVersion) {
		exitErrorWithHelp(errors.New("Incorrect version format. Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4\nIn case of the version is 'unknown', use this string as the version."), cmd)
	}

	if !runChecks() {
		utils.Exit(1)
		return
	}

	if !delete(deleteId, deleteVersion) {
		utils.Exit(1)
		return
	}
}

func delete(name string, version string) bool {
	resp, err := utils.SendHttpRequest("DELETE", fmt.Sprintf("%s/%s/%s", utils.CreateApiUrl("models"), name, version))
	if err != nil {
		utils.LogError(err.Error())
		return false
	}
	defer resp.Body.Close()

	return utils.HandleResponse(resp, cluster)
}
