package cmd

import (
	"encoding/json"
	"fmt"
	"strconv"
	"strings"

	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

const alphabetLetterNumber = 26

var argNameFilter string

var listCmd = &cobra.Command{
	Use:   "list",
	Short: "Lists the started model services.",
	Long:  `Lists the started model services.`,
	Run:   list,
}

func listCmdSetArgs(a []string) {
	listCmd.SetArgs(a)
}

func init() {}

func ResetArgNameFilter() {
	argNameFilter = ""
}

func list(cmd *cobra.Command, args []string) {
	utils.Verbose = verbose

	if len(args) > 0 {
		argNameFilter = args[0]
	}

	if !listServices() {
		utils.Exit(1)
		return
	}
}

func listServices() bool {
	if !runChecks() {
		utils.Exit(1)
		return false
	}

	response := queryServices()
	if response == nil {
		return false
	}

	prettyPrintServices(response)

	return true
}

func filterServiceListByName(response []utils.ListStartedModelResponse, filter string) (ret []utils.ListStartedModelResponse) {
	for _, m := range response {
		if m.Name == filter {
			ret = append(ret, m)
		}
	}
	return
}

func queryServices() []utils.ListStartedModelResponse {
	resp, err := utils.SendHttpRequest("GET", utils.CreateApiUrlv2("model-services"))
	if err != nil {
		utils.LogError(err.Error())
		return nil
	}
	defer resp.Body.Close()

	if !utils.HandleErrorResponse(resp, cluster) {
		return nil
	}

	var record []utils.ListStartedModelResponse
	if err := json.NewDecoder(resp.Body).Decode(&record); err != nil {
		utils.LogError("Failed to decode response from server!")
		return nil
	}

	return record
}

func createContentRow(element utils.ListStartedModelResponse, modelColumns int, tableHasAutoscaling bool) []string {
	var result = []string{
		utils.FormatCreatedDate(element.Created),
		element.Name,
		getReplicasFormStartedModelResponseElement(element),
	}

	if element.AutoScaling != nil {
		var deploymentmetrics []string
		for _, autoscalingMetric := range element.AutoScaling.Metrics {
			_, name, metric := utils.SeparateNameMetric(autoscalingMetric.AutoscalingMetricName)
			deploymentmetrics = append(deploymentmetrics, fmt.Sprintf("%s:%d%s", name, autoscalingMetric.TargetAverageValue, metric))
		}
		result = append(result, strings.Join(deploymentmetrics, ","))
	} else if tableHasAutoscaling {
		result = append(result, "")
	}

	result = append(result, element.Type, element.Status, element.User)

	for _, mn := range element.Models {
		result = append(result, mn.Id+":"+mn.Version)
	}

	result = addPlaceholders(modelColumns, element, result)

	if len(element.Models) > 1 {
		var weights []string
		for _, m := range element.Models {
			if m.Weight != nil {
				weights = append(weights, fmt.Sprintf("%g", *m.Weight))
			}
		}
		result = append(result, strings.Join(weights, ","))
	}

	result = addPlaceholders(modelColumns, element, result)

	result = append(result, "<mxe-host>/model-endpoints/"+element.Name)

	return result
}

func getReplicasFormStartedModelResponseElement(element utils.ListStartedModelResponse) string {
	if element.AutoScaling == nil {
		return strconv.Itoa(element.Replicas)
	} else {
		return fmt.Sprintf("%d-%d", element.AutoScaling.MinReplicas, element.AutoScaling.MaxReplicas)
	}
}

func addPlaceholders(modelColumns int, element utils.ListStartedModelResponse, result []string) []string {
	// Adding placeholder empty strings for the unfilled images in this row, to keep the table structure
	if modelColumns > len(element.Models) {
		for i := 0; i < modelColumns-len(element.Models); i++ {
			result = append(result, "")
		}
	}
	return result
}

func createServiceListTableData(elements []utils.ListStartedModelResponse) ([]string, [][]string) {
	var header = []string{"STARTED", "NAME", "INSTANCES"}

	var modelColumns = 0
	var hasAutoScaling = false
	for _, element := range elements {
		if len(element.Models) > modelColumns {
			modelColumns = len(element.Models)
		}
		if element.AutoScaling != nil {
			hasAutoScaling = true
		}
	}

	if hasAutoScaling {
		header = append(header, "AUTOSCALING")
	}

	header = append(header, "TYPE", "STATUS", "USER")

	if modelColumns == 1 {
		header = append(header, "MODEL")
	} else {
		// Create the image code for the header. e.g. if there are 54 images, the code should be: IMAGE_ZZB
		for i := 0; i < modelColumns; i++ {
			var nextImageString = "MODEL_"
			var tmpImageCounter = i
			for tmpImageCounter >= 0 {
				if tmpImageCounter > alphabetLetterNumber {
					nextImageString += "Z"
				} else {
					nextImageString += string(65 + rune(tmpImageCounter))
				}
				tmpImageCounter = tmpImageCounter - alphabetLetterNumber
			}
			header = append(header, nextImageString)
		}
	}

	if modelColumns > 1 {
		header = append(header, "WEIGHTS")
	}

	header = append(header, "ENDPOINT")

	var content [][]string
	for _, element := range elements {
		content = append(content, createContentRow(element, modelColumns, hasAutoScaling))
	}
	return header, content
}

func prettyPrintServices(elements []utils.ListStartedModelResponse) {
	header, content := createServiceListTableData(elements)
	utils.PrintTable(header, content)
}
