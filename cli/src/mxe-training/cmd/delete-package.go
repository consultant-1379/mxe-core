package cmd

import (
	"errors"
	"fmt"

	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

var deletePackageId string
var deletePackageVersion string

var deletePackageCmd = &cobra.Command{
	Use:     "package",
	Short:   "Deletes a training package from the cluster.",
	Long:    `Deletes a training package from the cluster. It also deletes the jobs started from the given package.`,
	Example: `mxe-training delete package --id com.ericsson.bdgs.oss.oss.eea.aio --version 0.0.1 `,
	Run:     runPackageDelete,
}

func init() {
	deletePackageCmd.PersistentFlags().StringVar(&deletePackageId, "id", "", "Training package identifier.")
	deletePackageCmd.PersistentFlags().StringVar(&deletePackageVersion, "version", "", "Training package version to delete.")
	deletePackageCmd.MarkPersistentFlagRequired("id")
	deletePackageCmd.MarkPersistentFlagRequired("version")
}

func deletePackageCmdSetArgs(a []string) {
	deleteCmd.SetArgs(a)
}

func runPackageDelete(cmd *cobra.Command, args []string) {
	utils.Verbose = verbose

	if deletePackageVersion != "unknown" && !utils.IsValidModelVersion(deletePackageVersion) {
		exitErrorWithHelp(errors.New("Incorrect version format. Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4\nIn case of the version is 'unknown', use this string as the version."), cmd)
	}

	if !runChecks() {
		utils.Exit(1)
		return
	}

	if !packageDelete(deletePackageId, deletePackageVersion) {
		utils.Exit(1)
		return
	}
}

func packageDelete(id string, version string) bool {
	resp, err := utils.SendHttpRequest("DELETE", fmt.Sprintf("%s/%s/%s", utils.CreateApiUrl("training-packages"), id, version))
	if err != nil {
		utils.LogError(err.Error())
		return false
	}
	defer resp.Body.Close()

	return utils.HandleResponse(resp, cluster)
}
