package cmd

import (
	"embed"
	"fmt"
	"io/fs"
	"os"

	log "github.com/sirupsen/logrus"
	"github.com/spf13/cobra"
	"mxe.ericsson/mxe-generate/utils/errors"
	logUtils "mxe.ericsson/mxe-generate/utils/logger"
)

var (
	logFormat       string
	logLevel        string
	MXE_CLI_VERSION string
	renderDeployer  bool
	namespace		string
	// skipValidate	bool
)

const (
	cliName = "mxe-generate"
)

func init() {
	cobra.OnInitialize(initConfig)
}

func initConfig() {
	logUtils.SetLogFormat(logFormat)
	logUtils.SetLogLevel(logLevel)
}

//go:embed resources/*.gotmpl
var embeddedFiles embed.FS

func getMode() bool {
	return os.Getenv("EMBEDDED_MODE") == "false"
}

func getFileSystem() fs.FS {
	if getMode() {
		log.Debug("Using OS file system")
		return os.DirFS("resources")
	}
	log.Debug("Using embedded file system")
	fsys, err := fs.Sub(embeddedFiles, "resources")
	errors.CheckError(err)
	return fsys
}

var templatesMap map[string]string = map[string]string{
	"mxe-values.yaml":          "mxe-values.yaml.gotmpl",
	"mxe-deployer-values.yaml": "mxe-deployer-values.yaml.gotmpl",
}

func render(tplFileKey string) bool {
	if tplFileKey == "mxe-deployer-values.yaml" {
		return renderDeployer
	}
	return true
}

func deployerEnabledStr() string {
	if renderDeployer {
		return "enabled"
	}
	return "disabled"
}

// NewCommand returns a new instance of mxe-values-file-generator command
func NewCommand() *cobra.Command {

	var command = &cobra.Command{
		Use:   cliName,
		Short: fmt.Sprintf("%s generates mxe values files using a configuration file", cliName),
		Run: func(c *cobra.Command, args []string) {
			c.HelpFunc()(c, args)
		},
		DisableAutoGenTag: true,
	}
	command.AddCommand(NewGenerateValuesFromConfigCommand())
	command.AddCommand(NewGetTemplatesCommand())
	command.AddCommand(NewVersionCommand())
	command.AddCommand(NewValidateCommand())

	command.PersistentFlags().StringVar(&logFormat, "logFormat", "text", "one of text/json")
	command.PersistentFlags().StringVar(&logLevel, "logLevel", "info", "one of debug|info|warn|error")
	command.PersistentFlags().BoolVar(&renderDeployer, "mxe-deployer", true, "set to false to skip generation of mxe-deployer-values.yaml")
	command.PersistentFlags().StringVar(&namespace, "namespace", "", "MXE installation namespace")
	command.MarkPersistentFlagRequired("namespace")
	return command
}
