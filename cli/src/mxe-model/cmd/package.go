package cmd

import (
	"bufio"
	"embed"
	"errors"
	"fmt"
	"io"
	"io/fs"
	"os"
	"path"
	"path/filepath"
	"strings"
	"text/template"

	"github.com/spf13/cobra"
	"mxe.ericsson/utils"
)

var source string
var privateKeyPath string
var publicKeyPath string
var network string
var obfuscate bool
var dockerUsername string
var dockerPassword string
var envProps map[string]string

type SourceCode int

const (
	Python           SourceCode = 1
	Unknown          SourceCode = 2
	propertyFileName string     = "config.env"
)

type fileMetadata struct {
	path      string
	name      string
	prefix    string
	extension string
}

func getModelBaseImage() string {
	mxePythonBaseImage := os.Getenv("MXE_PYTHON_BASE_IMAGE")
	if mxePythonBaseImage == "" {
		return utils.MXE_PYTHON_BASE_IMAGE
	} else {
		return mxePythonBaseImage
	}
}

//go:embed resources/*.gotmpl
var embeddedFiles embed.FS

func getMode() bool {
	return os.Getenv("EMBEDDED_MODE") == "false"
}

func getFileSystem() fs.FS {
	if getMode() {
		utils.LogDebug("Using OS file system")
		return os.DirFS("resources")
	}
	utils.LogDebug("Using embedded file system")
	fsys, err := fs.Sub(embeddedFiles, "resources")
	if err != nil {
		exitError(err)
	}
	return fsys
}

func printDockerfile(dockerFile string) {
	if verbose {
		utils.LogInfo("Verbose: Dockerfile:")
		dockerFileContent, err := os.ReadFile(dockerFile)
		if err != nil {
			exitError(err)
		}
		utils.LogInfo(string(dockerFileContent))
	}
}

func generateDockerFile(tempDir string) string {
	fs := getFileSystem()
	f, err := fs.Open("Dockerfile.gotmpl")
	if err != nil {
		exitError(err)
	}
	defer f.Close()
	// Read the file content
	dockerFileTmpl, err := io.ReadAll(f)
	if err != nil {
		exitError(err)
	}
	dockerFile, err := template.New("dockerfile").Parse(string(dockerFileTmpl))
	if err != nil {
		exitError(err)
	}
	dockerFilePath := filepath.Join(tempDir, "Dockerfile")
	dockerFileOut, err := os.Create(dockerFilePath)
	if err != nil {
		exitError(err)
	}
	defer dockerFileOut.Close()
	err = dockerFile.Execute(dockerFileOut, envProps)
	if err != nil {
		exitError(err)
	}
	printDockerfile(dockerFilePath)
	return dockerFilePath
}

func dockerBuildCommand(suffix string, source string, name string, dockerfile string) string {
	absPath, err := filepath.Abs(source)
	if err != nil {
		absPath = source
	}
	dockerBuildArgs := fmt.Sprintf("--build-arg PYTHON_BASE_IMAGE=%s", getModelBaseImage())

	return fmt.Sprintf("docker build %s -f %s -t %s %s %s", dockerBuildArgs, dockerfile, name, suffix, absPath)
}

func pythonCommand(name string, tempDir string) string {
	suffix := ""
	if network != "" {
		suffix = fmt.Sprintf("--network %s", network)
	}
	dockerFile := generateDockerFile(tempDir)
	return dockerBuildCommand(suffix, source, name, dockerFile)
}

var packageCmd = &cobra.Command{
	Use:     "package",
	Short:   "Create an MXE model package.",
	Long:    `Builds, signs and saves the MXE model service package that can be started in MXE afterwards.`,
	Example: `mxe-model package --source imagerecognition --privatekey priv.key --publickey pub.key`,
	Run:     packageModel,
}

func packageSetArgs(a []string) {
	packageCmd.SetArgs(a)
}

func init() {
	packageCmd.PersistentFlags().StringVar(&source, "source", "", "Directory containing the model code.")
	packageCmd.PersistentFlags().StringVar(&network, "network", "", "Specify the default Docker Network name to be used for s2i build")
	packageCmd.PersistentFlags().StringVar(&dockerUsername, "docker-username", "", "The username which will be used by Docker to fetch images from Armdocker (armdocker.rnd.ericsson.se)")
	packageCmd.PersistentFlags().StringVar(&dockerPassword, "docker-password", "", "The password for Armdocker")
	packageCmd.MarkPersistentFlagRequired("source")
	packageCmd.PersistentFlags().StringVar(&privateKeyPath, "privatekey", "", "Path to the user private key.")
	packageCmd.MarkPersistentFlagRequired("privatekey")
	packageCmd.PersistentFlags().StringVar(&publicKeyPath, "publickey", "", "Path to the user public key.")
	packageCmd.MarkPersistentFlagRequired("publickey")
}

func createTempDir() string {
	tempDir, err := os.MkdirTemp("", "mxe-packaging-*")
	if err != nil {
		exitError(err)
	}
	return tempDir
}

func cleanupTempDir(tempDir string) {
	if _, err := os.Stat(tempDir); err == nil {
		os.RemoveAll(tempDir)
	}
}

func packageModel(cmd *cobra.Command, args []string) {
	utils.Verbose = verbose

	name, err := getModelNameFromModelInfo()
	if err != nil {
		exitError(err)
	}

	utils.CheckBash()

	sourceType, err := getSourceCodeType()
	if err != nil {
		exitError(err)
	}

	tempDir := createTempDir()
	defer cleanupTempDir(tempDir)

	var command string
	switch sourceType {
	case Python:
		command = pythonCommand(name, tempDir)
	}

	if dockerUsername != "" && dockerPassword != "" {
		dockerLoginCommand := fmt.Sprintf("docker login --username \"%s\" --password \"%s\" armdocker.rnd.ericsson.se", dockerUsername, dockerPassword)

		if utils.RunOnWindows {
			utils.Run(utils.CmdParams{Cmd: dockerLoginCommand, EnvVars: map[string]string{"DOCKER_HOST": utils.DOCKER_HOST}})
		} else {
			utils.Run(utils.CmdParams{Cmd: dockerLoginCommand})
		}
	} else if dockerUsername != "" {
		exitError(errors.New("a password must be specified to login to armdocker.rnd.ericsson.se"))
	} else if dockerPassword != "" {
		exitError(errors.New("a username must be specified to login to armdocker.rnd.ericsson.se"))
	}

	if utils.RunOnWindows {
		utils.Run(utils.CmdParams{Cmd: command, Win: true, EnvVars: map[string]string{"DOCKER_HOST": utils.DOCKER_HOST}})
	} else {
		utils.Run(utils.CmdParams{Cmd: command})
	}

	tarFileName := strings.Replace(name, ":", "_", -1)
	imageFileLocation := filepath.Join(tempDir, utils.PACKAGING_IMAGE_FILENAME)
	savecommand := fmt.Sprintf("docker save -o %s %s", imageFileLocation, name)
	if utils.RunOnWindows {
		utils.Run(utils.CmdParams{Cmd: savecommand, Win: true, EnvVars: map[string]string{"DOCKER_HOST": utils.DOCKER_HOST}})
	} else {
		utils.Run(utils.CmdParams{Cmd: savecommand})
	}

	getidcommand := fmt.Sprintf("docker inspect --format='{{.Id}}' %s", name)
	var imageId string
	if utils.RunOnWindows {
		imageId, _ = utils.Run(utils.CmdParams{Cmd: getidcommand, Win: true, EnvVars: map[string]string{"DOCKER_HOST": utils.DOCKER_HOST}})
	} else {
		imageId, _ = utils.Run(utils.CmdParams{Cmd: getidcommand})
	}

	err = utils.SignAndSave(tarFileName, imageFileLocation, imageId, privateKeyPath, publicKeyPath)
	if err != nil {
		exitError(err)
	}

	os.Remove(tempDir)

	deletecommand := fmt.Sprintf("docker image rm %s", name)
	if utils.RunOnWindows {
		utils.Run(utils.CmdParams{Cmd: deletecommand, Win: true, EnvVars: map[string]string{"DOCKER_HOST": utils.DOCKER_HOST}})
	} else {
		utils.Run(utils.CmdParams{Cmd: deletecommand})
	}

	if obfuscate {
		utils.LogInfo("Success: obfuscated model archive created:")
	} else {
		utils.LogInfo("Success: model archive created:")
	}
	utils.LogInfo(tarFileName + ".tgz")
}

func getSourceCodeType() (SourceCode, error) {
	var err error
	envProps, err = loadPropertyFile(path.Join(source, propertyFileName), "=")
	if err != nil {
		return Unknown, err
	}
	if verbose {
		utils.LogInfo("Verbose: Read property file: " + propertyFileName)
		utils.LogInfo("Verbose: Properties:")
		for key, value := range envProps {
			utils.LogInfo("Verbose:" + key + "=" + value)
		}
	}

	var fileMetadatas []fileMetadata
	err = filepath.Walk(source, func(path string, info os.FileInfo, err error) error {
		file := info.Name()
		fileExtension := filepath.Ext(file)
		filePrefix := strings.TrimRight(file, fileExtension)
		if !info.IsDir() {
			if fileExtension == ".py" || fileExtension == ".xml" {
				fileMetadata := fileMetadata{path, file, filePrefix, fileExtension}
				fileMetadatas = append(fileMetadatas, fileMetadata)
			}
		}
		return nil
	})

	if err != nil {
		return Unknown, errors.New("no valid source files for python models can be found in " + source)
	}

	modelName, isPythonModel := envProps["MODEL_NAME"]

	for _, fileMetadata := range fileMetadatas {
		if isPythonModel && (fileMetadata.prefix == modelName) {
			utils.LogInfo("Python model is detected. Model main class: " + fileMetadata.name)
			return Python, nil
		}
	}

	return Unknown, errors.New("no supported source file with name " + modelName + " can be found in " + source)
}

func hasValidDependencies(fileMetadatas []fileMetadata) bool {
	utils.LogInfo("Validating dependencies...")
	var stringReplacer = strings.NewReplacer(
		"\r\n", "",
		"\r", "",
		"\n", "",
		" ", "",
	)

	for _, fileMetadata := range fileMetadatas {
		if fileMetadata.name == "pom.xml" {
			file, err := os.ReadFile(fileMetadata.path)
			if err != nil {
				utils.LogInfo("Unable to read file in package. Filename: " + fileMetadata.name)
				return false
			}
			fileString := string(file)
			fileString = stringReplacer.Replace(fileString)
			// TODO: Find a better way to validate dependencies
			if strings.Contains(fileString, "<dependency><groupId>io.seldon.wrapper</groupId><artifactId>seldon-core-wrapper</artifactId>") {
				utils.LogInfo("Seldon dependencies met")
				return true
			}
		}
	}

	return false
}

func isSpringBootConfigurationClass(fileMetadatas []fileMetadata) bool {
	utils.LogInfo("Validating configuration class...")
	for _, fileMetadata := range fileMetadatas {
		if fileMetadata.extension == ".java" {
			file, err := os.ReadFile(fileMetadata.path)
			if err != nil {
				utils.LogInfo("Unable to read file in package. Filename: " + fileMetadata.name)
				return false
			}
			fileString := string(file)
			// TODO: Find a better way to determine valid spring boot configuration class
			if strings.Contains(fileString, "SpringBootApplication") && strings.Contains(fileString, "io.seldon.wrapper.config.AppConfig.class") {
				utils.LogInfo("Package has valid spring boot configuration class")
				return true
			}
		}
	}

	return false
}

func getModelNameFromModelInfo() (string, error) {
	modelInfo, err := loadPropertyFile(source+"/MXE-META-INF/INFO", ":")
	if err != nil {
		return "", err
	}

	id, ok := modelInfo["Id"]
	if !ok {
		return "", errors.New("model Id is missing from " + source + "/MXE-META-INF/INFO")
	}
	if !utils.IsValidModelId(id) {
		return "", errors.New("model Id can only contain lower case alphanumeric characters and dots (.)")
	}

	version, ok := modelInfo["Version"]
	if !ok {
		return "", errors.New("model Version is missing from " + source + "/MXE-META-INF/INFO")
	}
	if !utils.IsValidModelVersion(version) {
		return "", errors.New("invalid model Version format. Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4")
	}

	return id + ":" + version, nil
}

func loadPropertyFile(fileName string, separator string) (map[string]string, error) {
	file, err := os.Open(fileName)
	if err != nil {
		return nil, err
	}
	defer file.Close()

	res := make(map[string]string)
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := scanner.Text()

		index := strings.Index(line, separator)
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
