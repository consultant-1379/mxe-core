package cmd

import (
	"bufio"
	"bytes"
	"io"
	"mime/multipart"
	"os"
	"path/filepath"
	"strings"

	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

var packageSourcePath string

const MXE_META_INF_INFO = "/MXE-META-INF/INFO"

var onboardCmd = &cobra.Command{
	Use:     "onboard",
	Short:   "Onboards a training source to an MXE cluster.",
	Long:    `Onboards a training source to an MXE cluster.`,
	Example: `mxe-training onboard --source /path/to/source`,
	Run:     runPackage,
}

func onboardCmdSetArgs(a []string) {
	onboardCmd.SetArgs(a)
}
func init() {
	onboardCmd.PersistentFlags().StringVar(&packageSourcePath, "source", "", "Directory containing the training package code.")
	onboardCmd.MarkPersistentFlagRequired("source")
}

func runPackage(cmd *cobra.Command, args []string) {
	utils.Verbose = verbose

	if !runChecks() {
		utils.Exit(1)
		return
	}

	err := login()
	if err != nil {
		utils.Exit(1)
		return
	}

	packageInfo, err := loadPropertyFile(packageSourcePath)
	if err != nil {
		utils.Exit(1)
		return
	}

	if !validatePackageInfo(packageInfo) {
		utils.Exit(1)
		return
	}

	path, err := zipTrainingPackageSource(packageSourcePath)
	if err != nil {
		utils.Exit(1)
		return
	}

	if !onboard(path) {
		utils.Exit(1)
		return
	}
}

func login() error {
	err := utils.ProbeTokenValidity()
	if err != nil {
		utils.LogError(err.Error())
		return err
	}
	return nil
}

func validatePackageInfo(packageInfo map[string]string) bool {
	ok := true
	id, idOk := checkPackageInfo("Id", packageInfo, packageSourcePath)
	version, versionOk := checkPackageInfo("Version", packageInfo, packageSourcePath)
	type_, typeOk := checkPackageInfo("Type", packageInfo, packageSourcePath)
	_, titleOk := checkPackageInfo("Title", packageInfo, packageSourcePath)
	_, authorOk := checkPackageInfo("Author", packageInfo, packageSourcePath)
	_, descriptionOk := checkPackageInfo("Description", packageInfo, packageSourcePath)

	if !utils.IsValidModelId(id) {
		utils.LogError("training package Id can only contain lower case alphanumeric characters and dots (.)")
		ok = false
	}
	if !utils.IsValidModelVersion(version) {
		utils.LogError("invalid training package Version format. Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4")
		ok = false
	}
	if type_ != "Training" {
		utils.LogError("invalid training package type. Valid type: Training")
		ok = false
	}

	return ok && idOk && versionOk && typeOk && titleOk && authorOk && descriptionOk
}

func zipTrainingPackageSource(sourcePath string) (string, error) {
	s := utils.StartSpinner("Compressing the given training package source")
	zipFilename, err := utils.CreateZip(sourcePath)
	if err != nil {
		utils.HandleSpinnerErrorMessage("Failed to create zip archive: "+zipFilename, s)
		return "", err
	}

	utils.StopSpinner(s)
	utils.LogInfo("Archive " + zipFilename + " created from training package source.")
	return zipFilename, nil

}

func deleteZipFile(filename string) {
	err := os.Remove(filename)
	if err != nil {
		utils.LogWarn("Failed to delete file: " + filename)
	}
}

func checkPackageInfo(id string, packageInfo map[string]string, path string) (string, bool) {
	resp, ok := packageInfo[id]
	if !ok {
		utils.LogError("training package " + id + " is missing from " + path + MXE_META_INF_INFO)
	}
	return resp, ok
}

func onboard(path string) bool {
	s := utils.StartSpinner("Sending the model training archive to the server")
	var body bytes.Buffer
	writer := multipart.NewWriter(&body)

	file, err := os.Open(path)
	if err != nil {
		utils.HandleSpinnerErrorMessage("Failed to read file "+path, s)
		return false
	}
	defer file.Close()

	partFile, err := writer.CreateFormFile("trainingpackage", filepath.Base(path))
	if err != nil {
		utils.HandleSpinnerErrorMessage("Failed to add file "+path+" to multipart form", s)
		return false
	}
	_, err = io.Copy(partFile, file)
	if err != nil {
		utils.HandleSpinnerErrorMessage("Failed to add file "+path+" to multipart form", s)
		return false
	}
	err = writer.Close()
	if err != nil {
		utils.HandleSpinnerErrorMessage("Creating multipart form object failed.", s)
		return false
	}

	resp, err := utils.SendHttpRequestWithBody("POST", utils.CreateApiUrl("training-packages"), writer.FormDataContentType(), &body)
	if err != nil {
		utils.HandleSpinnerErrorMessage(err.Error(), s)
		return false
	}
	defer resp.Body.Close()

	deleteZipFile(path)

	utils.StopSpinner(s)

	return utils.HandleResponse(resp, cluster)
}

func loadPropertyFile(sourcePath string) (map[string]string, error) {
	file, err := os.Open(sourcePath + MXE_META_INF_INFO)
	if err != nil {
		utils.LogError(err.Error())
		return nil, err
	}
	defer file.Close()

	res := make(map[string]string)
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := scanner.Text()

		index := strings.Index(line, ":")
		if index == -1 || index == len(line)-1 {
			continue
		}

		key := strings.Trim(line[:index], " ")
		value := strings.Trim(line[index+1:], " ")

		if _, contains := res[key]; !contains {
			res[key] = value
		}

	}

	return res, nil
}
