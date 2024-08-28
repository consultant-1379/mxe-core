package cmd

import (
	"encoding/json"
	"strings"

	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

var listCmd = &cobra.Command{
	Use:   "list",
	Short: "Lists the onboarded models.",
	Long:  `Lists the onboarded models.`,
	Run:   listOnboarded,
}

func listCmdSetArgs(a []string) {
	listCmd.SetArgs(a)
}

func init() {}

func listOnboarded(cmd *cobra.Command, args []string) {
	utils.Verbose = verbose

	if !listOnboardedModels() {
		utils.Exit(1)
		return
	}
}

func listOnboardedModels() bool {
	if !runChecks() {
		utils.Exit(1)
		return false
	}

	response := queryOnboardedModels()
	if response == nil {
		return false
	}

	prettyPrintOnboardedModels(response)

	return true
}

func queryOnboardedModels() []utils.ListOnboardedModelResponse {
	resp, err := utils.SendHttpRequest("GET", utils.CreateApiUrl("models"))
	if err != nil {
		utils.LogError(err.Error())
		return nil
	}
	defer resp.Body.Close()

	if !utils.HandleErrorResponse(resp, cluster) {
		return nil
	}

	var record []utils.ListOnboardedModelResponse
	if err := json.NewDecoder(resp.Body).Decode(&record); err != nil {
		utils.LogError("Failed to decode response from server!")
		return nil
	}

	return record
}

func createModelListTableData(elements []utils.ListOnboardedModelResponse) ([]string, [][]string) {
	var header = []string{"ONBOARDED", "USER", "ID", "VERSION", "TITLE", "AUTHOR", "SIGNED BY", "STATUS", "IMAGE_NAME"}
	var content [][]string
	for _, element := range elements {
		content = append(content, []string{
			utils.FormatCreatedDate(element.Created),
			element.User,
			element.Id,
			element.Version,
			element.Title,
			element.Author,
			element.SignedByName,
			getStatusOrMessage(element),
			element.Image,
		})
	}
	return header, content
}

func prettyPrintOnboardedModels(elements []utils.ListOnboardedModelResponse) {
	header, content := createModelListTableData(elements)
	utils.PrintTable(header, content)
}

func getStatusOrMessage(element utils.ListOnboardedModelResponse) string {
	if strings.Compare(element.Status, "error") == 0 {
		return "ERROR: " + element.Message
	} else {
		return element.Status
	}
}
