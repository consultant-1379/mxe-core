package cmd

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"

	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

var startPackageIdentifier string
var startPackageVersion string

var startCmd = &cobra.Command{
	Use:     "start",
	Short:   "Starts the training job.",
	Long:    `Starts an already onboarded training package.`,
	Example: "mxe-training start --packageId com.ericsson.bdgs.oss.oss.eea.aio --packageVersion 0.0.1",
	Run:     runStart,
}

func startCmdSetArgs(a []string) {
	startCmd.SetArgs(a)
}

func init() {
	startCmd.PersistentFlags().StringVar(&startPackageIdentifier, "packageId", "", "Package identifier.")
	startCmd.PersistentFlags().StringVar(&startPackageVersion, "packageVersion", "", "Package version to start.")
	startCmd.MarkPersistentFlagRequired("packageId")
	startCmd.MarkPersistentFlagRequired("packageVersion")
}

func runStart(cmd *cobra.Command, args []string) {

	utils.Verbose = verbose

	if startPackageIdentifier == "" {
		exitErrorWithHelp(errors.New("Empty package identifier"), cmd)
	}

	if startPackageVersion == "" {
		exitErrorWithHelp(errors.New("Empty package version"), cmd)
	}

	if !utils.IsValidModelId(startPackageIdentifier) {
		exitErrorWithHelp(errors.New("Package Id can only contain lower case alphanumeric characters and dots (.)"), cmd)
	}

	if !utils.IsValidModelVersion(startPackageVersion) {
		exitErrorWithHelp(errors.New("Incorrect version format. Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4"), cmd)
	}

	if !runChecks() {
		utils.Exit(1)
	}

	if !start(createStartRequest()) {
		utils.Exit(1)
	}
}

func createStartRequest() *bytes.Buffer {
	buf := new(bytes.Buffer)

	var startReq = utils.StartTrainingJobRequest{
		PackageId:      startPackageIdentifier,
		PackageVersion: startPackageVersion,
	}
	json.NewEncoder(buf).Encode(startReq)
	if utils.Verbose {
		utils.LogInfo(fmt.Sprintf("request body to send: %s", buf))
	}
	return buf
}

func start(startReq *bytes.Buffer) bool {
	resp, err := utils.SendHttpRequestWithBody("POST", utils.CreateApiUrl("training-jobs"), "application/json", startReq)
	if err != nil {
		utils.LogError(err.Error())
		return false
	}
	defer resp.Body.Close()

	if !utils.HandleErrorResponse(resp, cluster) {
		return false
	}

	var record utils.StartTrainingJobsResponse
	if err := json.NewDecoder(resp.Body).Decode(&record); err != nil {
		utils.LogError("Failed to decode response from server!")
		return false
	}

	utils.LogInfo(fmt.Sprintf("Training package started with Id \"%s\"", record.Id))

	return true
}
