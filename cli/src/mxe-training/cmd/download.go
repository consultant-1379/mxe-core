package cmd

import (
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"

	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

var downloadResultJobId string
var toDir string

var downloadCmd = &cobra.Command{
	Use:     "download-results",
	Short:   "Downloads the results of a given training job.",
	Long:    `Downloads the results of a given training job.`,
	Example: `mxe-training download-results --jobId b16bd04d-9dbe-4d12-9695-52056078895d --toDir /home/username/someplace`,
	Run:     runDownloadResult,
}

func init() {
	downloadCmd.PersistentFlags().StringVar(&downloadResultJobId, "jobId", "", "Training job identifier.")
	downloadCmd.PersistentFlags().StringVar(&toDir, "toDir", "",
		"Target directory where the results should be saved. "+
			"If the directory does not exist then it will be created. Default value is the current working directory.")
	downloadCmd.MarkPersistentFlagRequired("jobId")
	downloadCmd.MarkPersistentFlagDirname("toDir")
}

func runDownloadResult(cmd *cobra.Command, args []string) {
	utils.Verbose = verbose

	if len(strings.TrimSpace(toDir)) == 0 {
		cwd, err := os.Getwd()
		if err != nil {
			utils.LogError(err.Error())
			utils.Exit(1)
		}
		toDir = cwd
	} else {
		if _, err := os.Stat(toDir); os.IsNotExist(err) {
			os.MkdirAll(toDir, os.ModePerm)
		}
	}

	if !runChecks() {
		utils.Exit(1)
		return
	}

	if !downloadJobResult() {
		utils.Exit(1)
		return
	}
}

func downloadJobResult() bool {
	err := utils.ProbeTokenValidity()
	if err != nil {
		utils.LogError(err.Error())
		return false
	}

	spinner := utils.StartSpinner(fmt.Sprintf("Downloading results of the job"))
	resp, err := utils.SendHttpRequest("GET", fmt.Sprintf("%s/%s/result", utils.CreateApiUrl("training-jobs"), downloadResultJobId))

	if err != nil {
		utils.LogError(err.Error())
		return false
	}
	if !utils.HandleErrorResponse(resp, cluster) {
		return false
	}
	success := writeResponseBodyToFile(resp)
	utils.StopSpinner(spinner)
	if success {
		utils.LogSuccess("Download finished")
	}
	return success
}

func getFileNameFromResponse(resp *http.Response) string {
	var fileName = fmt.Sprintf("%s.zip", downloadResultJobId)

	for _, val := range strings.Split(resp.Header.Get("Content-Disposition"), ";") {
		pair := strings.Split(val, "=")
		if len(pair) == 2 && strings.ToLower(strings.TrimSpace(pair[0])) == "filename" && strings.TrimSpace(pair[1]) != "" {
			fileName = strings.ReplaceAll(strings.TrimSpace(pair[1]), "\"", "")
		}
	}
	return fileName
}

func writeResponseBodyToFile(resp *http.Response) bool {
	var fileName = getFileNameFromResponse(resp)

	file, err := os.Create(fmt.Sprintf("%s%s%s", toDir, string(os.PathSeparator), fileName))
	defer file.Close()

	if err != nil {
		utils.LogError(err.Error())
		return false
	}
	_, err = io.Copy(file, resp.Body)
	if err != nil {
		utils.LogError(err.Error())
		return false
	}
	return true
}
