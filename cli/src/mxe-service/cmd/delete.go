package cmd

import (
	"fmt"

	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

var deleteServiceName string

var deleteCmd = &cobra.Command{
	Use:     "delete",
	Short:   "Deletes a model service.",
	Long:    `Deletes a model service.`,
	Example: "mxe-service delete --name com-ericsson-bdgs-oss-oss-eea-aio",
	Run:     deleteRun,
}

func init() {
	deleteCmd.PersistentFlags().StringVar(&deleteServiceName, "name", "", "Model service name.")
	deleteCmd.MarkPersistentFlagRequired("name")
}

func deleteRun(cmd *cobra.Command, args []string) {
	utils.Verbose = verbose

	if !runChecks() {
		utils.Exit(1)
		return
	}

	if !deleteModel(cmd) {
		utils.Exit(1)
		return
	}
}

func deleteModel(cmd *cobra.Command) bool {
	resp, err := utils.SendHttpRequest("DELETE", fmt.Sprintf("%s/%s", utils.CreateApiUrlv2("model-services"), deleteServiceName))
	if err != nil {
		utils.LogError(err.Error())
		return false
	}
	defer resp.Body.Close()

	return utils.HandleResponse(resp, cluster)

}
