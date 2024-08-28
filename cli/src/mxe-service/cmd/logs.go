package cmd

import (
	"encoding/json"
	"errors"
	"fmt"
	"net/url"
	"strconv"

	"mxe.ericsson/utils"

	"github.com/fatih/color"
	"github.com/spf13/cobra"
)

var logsServiceName string
var logsLines uint64
var logsSeconds uint64
var logsLimit uint64

var logCmd = &cobra.Command{
	Use:   "logs",
	Short: "Show model service logs.",
	Long:  `Show model service logs.`,
	Example: `mxe-service logs --name aio-service
mxe-service logs --name aio-service --seconds 60
mxe-service logs --name aio-service --lines 100
mxe-service logs --name aio-service --lines 100 --limit 5000`,
	Run:   getLogs,
}

func init() {
	logCmd.PersistentFlags().StringVar(&logsServiceName, "name", "", "Model service name.")
	logCmd.MarkPersistentFlagRequired("name")
	logCmd.PersistentFlags().Uint64Var(&logsLines, "lines", 0, "Return the specified number of last lines")
	logCmd.PersistentFlags().Uint64Var(&logsSeconds, "seconds", 0, "Return the logs for the specified number of last seconds")
	logCmd.PersistentFlags().Uint64Var(&logsLimit, "limit", 0, "Limit the returned logs (in bytes)")
}

func getLogs(cmd *cobra.Command, args []string) {
	utils.Verbose = verbose

	if logsServiceName == "" {
		exitErrorWithHelp(errors.New("Empty model service name"), cmd)
	}

	if !printLogs() {
		utils.Exit(1)
		return
	}
}

func printLogs() bool {
	if !runChecks() {
		utils.Exit(1)
		return false
	}

	response := queryLogs()
	if response == nil {
		return false
	}

	for containerName, logString := range response {
		color.Green("\nLogs for container <%s>:\n", containerName)
		fmt.Println(logString)
	}

	return true
}

func queryLogs() map[string]string {
	resp, err := utils.SendHttpRequest("GET", generateUrl())
	if err != nil {
		utils.LogError(err.Error())
		return nil
	}
	defer resp.Body.Close()

	if !utils.HandleErrorResponse(resp, cluster) {
		return nil
	}

	var result map[string]string
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		utils.LogError("Failed to decode response from server!")
		return nil
	}

	return result
}

func generateUrl() string {
	baseUrl := utils.CreateApiUrl("model-services/") + logsServiceName + "/logs?"
	return baseUrl + getLogsQueryString()
}

func getLogsQueryString() string {
	q := url.Values{}
	if logsLines > 0 {
		q.Add("lines", strconv.FormatUint(logsLines, 10))
	}
	if logsSeconds > 0 {
		q.Add("seconds", strconv.FormatUint(logsSeconds, 10))
	}
	if logsLimit > 0 {
		q.Add("limit", strconv.FormatUint(logsLimit, 10))
	}

	return q.Encode()
}
