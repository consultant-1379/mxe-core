package cmd

import (
	"io/fs"
	"os"

	"path"

	log "github.com/sirupsen/logrus"
	"github.com/spf13/cobra"
	"mxe.ericsson/mxe-generate/utils/errors"
)

func NewGetTemplatesCommand() *cobra.Command {
	var outputDir string
	currentDir, err := os.Getwd()
	errors.CheckError(err)

	var command = &cobra.Command{
		Use:   "templates",
		Short: "Create values files from a config file",
		Run: func(c *cobra.Command, args []string) {
			log.Infof("mxe-deployer-values.yaml file generation is %s", deployerEnabledStr())
			getTemplates(outputDir)
		},
		Hidden: true,
	}
	command.Flags().StringVar(&outputDir, "output-dir", currentDir, "The `DIR` to write the generated files to")
	return command
}

func getTemplates(outputDir string) {
	embedFS := getFileSystem()
	if _, err := os.Stat(outputDir); os.IsNotExist(err) {
		os.MkdirAll(outputDir, os.ModePerm)
	}
	for tplFileKey, tplFile := range templatesMap {
		tplFileName := path.Base(tplFile)
		log.Printf("Renderdeployer %b", renderDeployer)
		if render(tplFileKey) {
			log.Info("Writing template: ", tplFile, " to location: ", outputDir)
			fileContent, error := fs.ReadFile(embedFS, tplFile)
			errors.CheckError(error)
			err := os.WriteFile(path.Join(outputDir, tplFileName), fileContent, os.ModePerm)
			errors.CheckError(err)
		}
	}
}
