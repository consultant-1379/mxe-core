package cmd

import (
	"encoding/json"
	"strings"

	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

var listJobCmd = &cobra.Command{
	Use:   "jobs",
	Short: "List training jobs.",
	Long:  `List training jobs.`,
	Run:   listJob,
}

func listJobsCmdSetArgs(a []string) {
	listCmd.SetArgs(a)
}

func init() {}

func listJob(cmd *cobra.Command, args []string) {
	utils.Verbose = verbose

	if !listJobs() {
		utils.Exit(1)
		return
	}
}

func listJobs() bool {
	if !runChecks() {
		utils.Exit(1)
		return false
	}

	response := queryJobs()
	if response == nil {
		return false
	}

	prettyPrintJobs(response)

	return true
}

func queryJobs() []utils.ListTrainingJobsResponse {
	resp, err := utils.SendHttpRequest("GET", utils.CreateApiUrl("training-jobs"))
	if err != nil {
		utils.LogError(err.Error())
		return nil
	}
	defer resp.Body.Close()

	if !utils.HandleErrorResponse(resp, cluster) {
		return nil
	}

	var record []utils.ListTrainingJobsResponse
	if err := json.NewDecoder(resp.Body).Decode(&record); err != nil {
		utils.LogError("Failed to decode response from server!")
		return nil
	}

	return record
}

func prettyPrintJobs(elements []utils.ListTrainingJobsResponse) ([]string, [][]string) {
	var header = []string{"CREATED", "ID", "PACKAGE ID", "PACKAGE VERSION", "STATUS", "COMPLETED"}
	var content [][]string
	for _, element := range elements {
		content = append(content, []string{
			utils.FormatCreatedDate(element.Created),
			element.Id,
			element.PackageId,
			element.PackageVersion,
			getJobStatusOrMessage(element),
			utils.FormatCreatedDate(element.Completed),
		})
	}
	utils.PrintTable(header, content)
	return header, content
}

func getJobStatusOrMessage(element utils.ListTrainingJobsResponse) string {
	if strings.Compare(element.Status, "failed") == 0 {
		return "ERROR: " + element.Message
	} else {
		return element.Status
	}
}
