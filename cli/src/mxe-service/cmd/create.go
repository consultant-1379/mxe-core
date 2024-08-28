package cmd

import (
	"bytes"
	"io"
	"mime/multipart"
	"os"
	"path/filepath"

	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

// removed
var createManifest string

var createCmd = &cobra.Command{
	Use:     "create",
	Short:   "Creates a model service.",
	Long:    `Creates a model service from previously onboarded model(s).`,
	Example: `mxe-service create --manifest ./single-model-manifest.yaml`,

	Run: runStart,
}

func createSetArgs(a []string) {
	createCmd.SetArgs(a)
}

func init() {
	createCmd.PersistentFlags().StringVar(&createManifest, "manifest", "", "The manifest file for model deployment in mxe")
	createCmd.MarkPersistentFlagRequired("manifest")
}

func runStart(cmd *cobra.Command, args []string) {
	utils.Verbose = verbose

	if !runChecks() {
		utils.Exit(1)
	}

	if !create() {
		utils.Exit(1)
	}
}

func create() bool {

	returnbool := createWithManifest(createManifest, "custom_manifest", "Sending Manifest to server")
	return returnbool

}

func createWithManifest(path string, fieldname string, spinnerText string) bool {
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

	resp, err := utils.SendHttpRequestWithBody("POST", utils.CreateApiUrlv2("model-services"), writer.FormDataContentType(), &body)
	if err != nil {
		utils.HandleSpinnerError(err, s)
		return false
	}
	defer resp.Body.Close()

	utils.StopSpinner(s)
	return utils.HandleResponse(resp, cluster)
}
