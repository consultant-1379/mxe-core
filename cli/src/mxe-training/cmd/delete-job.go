package cmd

import (
	"errors"
	"fmt"

	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

var deleteJobsPackageId string
var deleteJobsPackageVersion string
var deleteJobsId string

var deleteJobCmd = &cobra.Command{
	Use:     "job",
	Short:   "Deletes training jobs from the cluster.",
	Long:    `Deletes training jobs from the cluster.`,
	Example: `mxe-training delete job --id 31415926 mxe-training delete job --packageId com.ericsson.bdgs.oss.oss.eea.aio --packageVersion 0.0.1`,
	Run:     runJobDelete,
}

func deleteJobCmdSetArgs(a []string) {
	deleteCmd.SetArgs(a)
}

func init() {
	deleteJobCmd.PersistentFlags().StringVar(&deleteJobsId, "id", "", "Training job identifier. If set, other parameters must not be set.")
	deleteJobCmd.PersistentFlags().StringVar(&deleteJobsPackageId, "packageId", "", "Training jobs package identifier. If set, packageVersion must be set.")
	deleteJobCmd.PersistentFlags().StringVar(&deleteJobsPackageVersion, "packageVersion", "", "Training jobs package version to delete. If set, packageId must be set.")
}

func runJobDelete(cmd *cobra.Command, args []string) {
	utils.Verbose = verbose

	if !((deleteJobsPackageVersion != "" && deleteJobsPackageId != "") || deleteJobsId != "") || (deleteJobsPackageVersion != "" && deleteJobsId != "") || (deleteJobsId != "" && deleteJobsPackageId != "") {
		exitErrorWithHelp(errors.New("Incorrect command usage. Valid usage:\nmxe-training delete job --packageId com.ericsson.bdgs.oss.oss.eea.aio --packageVersion 0.0.1 or mxe-training delete job --id 31415926"), cmd)
	}

	if deleteJobsPackageVersion != "" && !utils.IsValidModelVersion(deleteJobsPackageVersion) {
		exitErrorWithHelp(errors.New("Incorrect version format. Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4"), cmd)
	}

	if !runChecks() {
		utils.Exit(1)
		return
	}

	if deleteJobsId != "" {
		if !jobDeleteWithId(deleteJobsId) {
			utils.Exit(1)
			return
		}
	} else {
		if !jobDeleteWithPackageData(deleteJobsPackageId, deleteJobsPackageVersion) {
			utils.Exit(1)
			return
		}
	}
}

func jobDeleteWithId(id string) bool {
	resp, err := utils.SendHttpRequest("DELETE", fmt.Sprintf("%s/%s", utils.CreateApiUrl("training-jobs"), id))
	if err != nil {
		utils.LogError(err.Error())
		return false
	}
	defer resp.Body.Close()

	return utils.HandleResponse(resp, cluster)
}

func jobDeleteWithPackageData(id string, version string) bool {
	resp, err := utils.SendHttpRequest("DELETE", fmt.Sprintf("%s?packageId=%s&packageVersion=%s", utils.CreateApiUrl("training-jobs"), id, version))
	if err != nil {
		utils.LogError(err.Error())
		return false
	}
	defer resp.Body.Close()

	return utils.HandleResponse(resp, cluster)
}
