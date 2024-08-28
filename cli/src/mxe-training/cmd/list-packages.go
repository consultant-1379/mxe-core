package cmd

import (
	"encoding/json"
	"strings"

	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

var listPackageCmd = &cobra.Command{
	Use:   "packages",
	Short: "List training packages.",
	Long:  `List training packages.`,
	Run:   listPackage,
}

func listPackagesCmdSetArgs(a []string) {
	listCmd.SetArgs(a)
}

func init() {}

func listPackage(cmd *cobra.Command, args []string) {
	utils.Verbose = verbose

	if !listPackages() {
		utils.Exit(1)
		return
	}
}

func listPackages() bool {
	if !runChecks() {
		utils.Exit(1)
		return false
	}

	response := queryPackages()
	if response == nil {
		return false
	}

	prettyPrintPackages(response)

	return true
}

func queryPackages() []utils.ListTrainingPackagesResponse {
	resp, err := utils.SendHttpRequest("GET", utils.CreateApiUrl("training-packages"))
	if err != nil {
		utils.LogError(err.Error())
		return nil
	}
	defer resp.Body.Close()

	if !utils.HandleErrorResponse(resp, cluster) {
		return nil
	}

	var record []utils.ListTrainingPackagesResponse
	if err := json.NewDecoder(resp.Body).Decode(&record); err != nil {
		utils.LogError("Failed to decode response from server!")
		return nil
	}

	return record
}

func prettyPrintPackages(elements []utils.ListTrainingPackagesResponse) ([]string, [][]string) {
	var header = []string{"ONBOARDED", "USER", "ID", "VERSION", "IMAGE", "TITLE", "AUTHOR", "STATUS"}
	var content [][]string
	for _, element := range elements {
		content = append(content, []string{
			utils.FormatCreatedDate(element.Created),
			element.User,
			element.Id,
			element.Version,
			element.Image,
			element.Title,
			element.Author,
			getPackageStatusOrMessage(element),
		})
	}
	utils.PrintTable(header, content)
	return header, content
}

func getPackageStatusOrMessage(element utils.ListTrainingPackagesResponse) string {
	if strings.Compare(element.Status, "error") == 0 {
		return "ERROR: " + element.Message
	} else {
		return element.Status
	}
}
