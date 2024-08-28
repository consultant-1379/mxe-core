package cmd

import (
	"bufio"
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"mime/multipart"
	"net/http"
	"net/textproto"
	"os"
	"path/filepath"
	"strings"

	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

var onboardId string
var onboardVersion string
var onboardTitle string
var onboardDescription string
var onboardAuthor string
var onboardDockerImage string
var onboardModelSourcePath string
var onboardModelArchivePath string
var onboardDockerRegistrySecretName string

var onboardCmd = &cobra.Command{
	Use:     "onboard",
	Short:   "Onboard a model docker image or a model source to an MXE cluster.",
	Long:    "Onboard a model docker image or a model source to an MXE cluster.\n\nKnown issue:\nPackaging restarts in case of errors. In case of restarts, packaging's state can be failure while the packaging can succeed after a while. If the package job's state stays failed for longer time, it can be suspected that it really failed. In this case, logs can be helpful regarding the issues.",
	Example: `mxe-model onboard --id "com.ericsson.iot.devicedetection" --description "This is the IoT device detection model description" --author "Jane Doe" --title "IoT device detection" --version 1.0.0 --docker iotdd:1.0.0 mxe-model onboard --source /home/mxe/models/imagerecognition`,
	Run:     runOnboard,
}

func onboardCmdSetArgs(a []string) {
	onboardCmd.SetArgs(a)
}

func init() {
	onboardCmd.PersistentFlags().StringVar(&onboardId, "id", "", "Unique model ID")
	onboardCmd.PersistentFlags().StringVar(&onboardVersion, "version", "", "Model version. Version format: a.b.c, where a, b, and c are non-negative numbers.")
	onboardCmd.PersistentFlags().StringVar(&onboardTitle, "title", "", "Model title.")
	onboardCmd.PersistentFlags().StringVar(&onboardDescription, "description", "", "Optional short description.")
	onboardCmd.PersistentFlags().StringVar(&onboardAuthor, "author", "", "Optional model author.")
	onboardCmd.PersistentFlags().StringVar(&onboardDockerImage, "docker", "", "Docker image name. If set, source and archive must not be set.")
	onboardCmd.PersistentFlags().StringVar(&onboardModelSourcePath, "source", "", "Directory containing the model code. If set, other parameters must not be set.")
	onboardCmd.PersistentFlags().StringVar(&onboardModelArchivePath, "archive", "", "Path to the model service archive. If set, other parameters must not be set.")
	onboardCmd.PersistentFlags().StringVar(&onboardDockerRegistrySecretName, "docker-registry-secret-name", "", "The secret to login to a Docker registry for model deployment.")
}

func runOnboard(cmd *cobra.Command, args []string) {
	utils.Verbose = verbose

	if onboardModelSourcePath != "" && onboardDockerImage != "" && onboardModelArchivePath != "" {
		exitErrorWithHelp(errors.New("Archive, docker, and source flag are set, only one of them can be used"), cmd)
	}

	if onboardModelSourcePath == "" && onboardDockerImage == "" && onboardModelArchivePath == "" {
		exitErrorWithHelp(errors.New("At least one of the the following flags should be set: archive, docker, source"), cmd)
	}

	if onboardModelSourcePath != "" && onboardDockerImage != "" {
		exitErrorWithHelp(errors.New("Both docker and source flag are set, only one of them can be used"), cmd)
	}

	if onboardModelSourcePath != "" && onboardModelArchivePath != "" {
		exitErrorWithHelp(errors.New("Both archive and source flag are set, only one of them can be used"), cmd)
	}

	if onboardDockerImage != "" && onboardModelArchivePath != "" {
		exitErrorWithHelp(errors.New("Both archive and docker flag are set, only one of them can be used"), cmd)
	}

	if (onboardModelSourcePath != "" || onboardModelArchivePath != "") && onboardDockerRegistrySecretName != "" {
		exitErrorWithHelp(errors.New("Secret name for docker registry can only be given together with docker flag"), cmd)
	}

	if onboardDockerImage != "" {
		flagsToSet := []string{}
		if onboardId == "" {
			flagsToSet = append(flagsToSet, "id")
		}
		if onboardVersion == "" {
			flagsToSet = append(flagsToSet, "version")
		}
		if onboardTitle == "" {
			flagsToSet = append(flagsToSet, "title")
		}
		if len(flagsToSet) != 0 {
			exitErrorWithHelp(errors.New("required flag(s) "+strings.Join(flagsToSet, ", ")+" not set"), cmd)
		}
		if len(onboardId) > 32 {
			exitErrorWithHelp(errors.New("Model ID must be at most 32 characters length."), cmd)
		}
		if len(onboardDescription) > 120 {
			exitErrorWithHelp(errors.New("Description must be at most 120 characters length."), cmd)
		}
		if !utils.IsValidModelVersion(onboardVersion) {
			exitErrorWithHelp(errors.New("Incorrect version format. Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4"), cmd)
		}
		if !utils.IsValidModelId(onboardId) {
			exitErrorWithHelp(errors.New("The id "+onboardId+" shall only contain lower case alphanumeric characters, and .(dot)."), cmd)
		}

		if !runChecks() {
			utils.Exit(1)
			return
		}
		if !onboard(onboardRequestPayloadFromParameters()) {
			utils.Exit(1)
			return
		}
	} else if onboardModelSourcePath != "" {
		if !runChecks() {
			utils.Exit(1)
			return
		}

		onboardModelSourcePath = filepath.Clean(onboardModelSourcePath)
		info, err := os.Stat(onboardModelSourcePath)
		if err != nil || !info.IsDir() {
			exitErrorWithHelp(errors.New("Source parameter must be a directory."), cmd)
		}

		zipFilename, err := zipModelSource(onboardModelSourcePath)
		if err != nil {
			utils.Exit(1)
			return
		}
		if !onboardFile(zipFilename, "sourcefile", "Sending the model source to the server") {
			deleteZipFile(zipFilename)
			utils.Exit(1)
			return
		}
		deleteZipFile(zipFilename)
	} else if onboardModelArchivePath != "" {
		if !runChecks() {
			utils.Exit(1)
			return
		}
		if !onboardFile(onboardModelArchivePath, "archivefile", "Sending the model archive to the server") {
			utils.Exit(1)
			return
		}
	}
}

func zipModelSource(path string) (string, error) {
	s := utils.StartSpinner("Compressing the given model source")
	modelInfo, err := loadPropertyFile(path+"/MXE-META-INF/INFO", ":")
	if err != nil {
		utils.HandleSpinnerError(err, s)
		return "", err
	}

	id, ok := modelInfo["Id"]
	if !ok {
		err := errors.New("model Id is missing from " + source + "/MXE-META-INF/INFO")
		utils.HandleSpinnerError(err, s)
		return "", err
	}
	if !utils.IsValidModelId(id) {
		err := errors.New("model Id can only contain lower case alphanumeric characters and dots (.)")
		utils.HandleSpinnerError(err, s)
		return "", err
	}

	version, ok := modelInfo["Version"]
	if !ok {
		err := errors.New("model Version is missing from " + source + "/MXE-META-INF/INFO")
		utils.HandleSpinnerError(err, s)
		return "", err
	}
	if !utils.IsValidModelVersion(version) {
		err := errors.New("invalid model Version format. Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4")
		utils.HandleSpinnerError(err, s)
		return "", err
	}

	_, ok = modelInfo["Title"]
	if !ok {
		err := errors.New("model Title is missing from " + source + "/MXE-META-INF/INFO")
		utils.HandleSpinnerError(err, s)
		return "", err
	}

	_, ok = modelInfo["Author"]
	if !ok {
		err := errors.New("model Author is missing from " + source + "/MXE-META-INF/INFO")
		utils.HandleSpinnerError(err, s)
		return "", err
	}

	_, ok = modelInfo["Description"]
	if !ok {
		err := errors.New("model Description is missing from " + source + "/MXE-META-INF/INFO")
		utils.HandleSpinnerError(err, s)
		return "", err
	}

	zipFilename, err := utils.CreateZip(path)
	if err != nil {
		err := errors.New("Failed to create zip archive: " + zipFilename)
		utils.HandleSpinnerError(err, s)
		return "", err
	}

	utils.StopSpinner(s)
	utils.LogInfo("Archive " + zipFilename + " created from model source.")
	return zipFilename, nil

}

func deleteZipFile(filename string) {
	err := os.Remove(filename)
	if err != nil {
		utils.LogWarn("Failed to delete file: " + filename)
	}
}

func onboardFileTry(path string, fieldname string, spinnerText string) *http.Response {
	err := utils.ProbeTokenValidity()
	if err != nil {
		utils.LogError(err.Error())
		return nil
	}

	s := utils.StartSpinner(spinnerText)

	bodyReader, bodyWriter := io.Pipe()
	multipartWriter := multipart.NewWriter(bodyWriter)
	contentType := multipartWriter.FormDataContentType()

	errorChan := make(chan error)

	go func() {
		defer bodyWriter.Close()

		partFile, err := multipartWriter.CreateFormFile(fieldname, filepath.Base(path))
		if err != nil {
			errorChan <- err
			return
		}

		file, err := os.Open(path)
		if err != nil {
			errorChan <- err
			return
		}
		defer file.Close()

		bufferedFileReader := bufio.NewReader(file)

		_, err = io.Copy(partFile, bufferedFileReader)
		if err != nil {
			errorChan <- err
			return
		}

		err = multipartWriter.Close()
		if err != nil {
			errorChan <- err
			return
		}
	}()

	resp, respErr := utils.SendLargeHttpRequest("POST", utils.CreateApiUrl("models"), contentType, bodyReader)

	// error handling for the go routine (multipart form creation) & http response
	select {
	case e := <-errorChan:
		utils.HandleSpinnerError(e, s)
	default:
		if respErr != nil {
			utils.HandleSpinnerError(respErr, s)
			return nil
		}
	}

	utils.StopSpinner(s)
	return resp
}

func onboardFile(path string, fieldname string, spinnerText string) bool {

	resp := onboardFileTry(path, fieldname, spinnerText)

	if resp != nil && resp.Body != nil {
		defer resp.Body.Close()
	}

	if resp != nil && (resp.StatusCode == 307 || resp.StatusCode == 303) {
		utils.LogDebug("Authentication token is invalid, Attempting to refresh token and retry")
		resp = onboardFileTry(path, fieldname, spinnerText)
	}

	if resp != nil {
		return utils.HandleResponse(resp, cluster)
	}

	return false
}

func createFormJson(w *multipart.Writer) (io.Writer, error) {
	h := make(textproto.MIMEHeader)
	h.Set("Content-Disposition", fmt.Sprintf(`form-data; name="modeldata"`))
	h.Set("Content-Type", "application/json")
	return w.CreatePart(h)
}

func onboardRequestPayloadFromParameters() *bytes.Buffer {
	var payload = utils.OnboardModelRequest{
		Id:                       onboardId,
		Version:                  onboardVersion,
		Title:                    onboardTitle,
		Author:                   onboardAuthor,
		Description:              onboardDescription,
		Image:                    onboardDockerImage,
		DockerRegistrySecretName: onboardDockerRegistrySecretName,
	}
	buf := new(bytes.Buffer)
	json.NewEncoder(buf).Encode(payload)
	return buf
}

func onboard(payload *bytes.Buffer) bool {
	var body bytes.Buffer
	writer := multipart.NewWriter(&body)

	partJson, err := createFormJson(writer)
	if _, err := io.Copy(partJson, strings.NewReader(payload.String())); err != nil {
		utils.LogError("Creating data json failed.")
		return false
	}

	if err = writer.Close(); err != nil {
		utils.LogError("Creating multipart form object failed.")
		return false
	}

	resp, err := utils.SendHttpRequestWithBody("POST", utils.CreateApiUrl("models"), writer.FormDataContentType(), &body)
	if err != nil {
		utils.LogError(err.Error())
		return false
	}
	defer resp.Body.Close()

	return utils.HandleResponse(resp, cluster)
}
