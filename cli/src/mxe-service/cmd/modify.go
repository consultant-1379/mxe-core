package cmd

import (
	"bytes"
	"errors"
	"fmt"

	"github.com/spf13/cobra"

	"io"
	"mime/multipart"
	"os"
	"path/filepath"

	"mxe.ericsson/utils"
)

var modifyServiceName string
var modifyManifest string

var modifyCmd = &cobra.Command{
	Use:     "modify",
	Short:   "Modifies a model service.",
	Long:    `Modify a service created earlier with the mxe-service create.`,
	Example: `mxe-service modify --name aio-service --manifest ./single-model-manifest.yaml`,

	Run: runModify,
}

func modifySetArgs(a []string) {
	modifyCmd.SetArgs(a)
}

func init() {
	modifyCmd.PersistentFlags().StringVar(&modifyServiceName, "name", "", "Model service name.")
	modifyCmd.MarkPersistentFlagRequired("name")
	modifyCmd.PersistentFlags().StringVar(&modifyManifest, "manifest", "", "The manifest file for model deployment in mxe")
	modifyCmd.MarkPersistentFlagRequired("manifest")
}

func runModify(cmd *cobra.Command, args []string) {
	utils.Verbose = verbose

	if !utils.IsValidModelName(modifyServiceName) {
		exitErrorWithHelp(errors.New("Model service name can only contain lower case alphanumeric characters and dash (-)"), cmd)
	}

	if !runChecks() {
		utils.Exit(1)
	}

	if !postModify() {
		utils.Exit(1)
	}
}

func postModify() bool {

	returnbool := patchWithManifest(modifyManifest, "custom_manifest", "Sending Manifest to server")
	return returnbool

}
func patchWithManifest(path string, fieldname string, spinnerText string) bool {

	reqUrl := fmt.Sprintf("%s/%s", utils.CreateApiUrlv2("model-services"), modifyServiceName)

	err := utils.ProbeTokenValidity()
	if err != nil {
		utils.LogError(err.Error())
		return false
	}

	s := utils.StartSpinner(spinnerText)
	var body bytes.Buffer
	writer := multipart.NewWriter(&body)

	file, err := os.Open(path)
	if err != nil {
		utils.HandleSpinnerErrorMessage("Failed to read manifest file "+path, s)
		return false
	}
	defer file.Close()

	partFile, err := writer.CreateFormFile(fieldname, filepath.Base(path))
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

	resp, err := utils.SendHttpRequestWithBody("PATCH", reqUrl, writer.FormDataContentType(), &body)
	if err != nil {
		utils.HandleSpinnerError(err, s)
		return false
	}
	defer resp.Body.Close()

	utils.StopSpinner(s)
	return utils.HandleResponse(resp, cluster)
}
