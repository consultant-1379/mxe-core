package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
	"mxe.ericsson/depmanager/dmserver/client/config"

	argocdclient "github.com/argoproj/argo-cd/v2/pkg/apiclient"
	"github.com/argoproj/argo-cd/v2/util/cli"
	argocdconfig "github.com/argoproj/argo-cd/v2/util/config"
	"github.com/argoproj/argo-cd/v2/util/errors"
	//"k8s.io/client-go/tools/clientcmd"
)

func init() {
	cobra.OnInitialize(initConfig)
}

var (
	logFormat string
	logLevel  string
)

func initConfig() {
	cli.SetLogFormat(logFormat)
	cli.SetLogLevel(logLevel)
}

// NewCommand returns a new instance of an argocd command
func NewCommand() *cobra.Command {
	var (
		clientOpts argocdclient.ClientOptions
		/* TODO: uncomment when configuring cluster using contexts from local kubeconfig file */
		//pathOpts   = clientcmd.NewDefaultPathOptions()
	)

	var command = &cobra.Command{
		Use:   cliName,
		Short: fmt.Sprintf("%s controls a MXE Deployer server", cliName),
		Run: func(c *cobra.Command, args []string) {
			c.HelpFunc()(c, args)
		},
		DisableAutoGenTag: true,
	}
	initOpts(&clientOpts)
	command.AddCommand(NewVersionCommand())
	command.AddCommand(NewLoginCommand(&clientOpts))
	command.AddCommand(NewPackageCommand(&clientOpts))

	command.PersistentFlags().StringVar(&logFormat, "logformat", argocdconfig.GetFlag("logformat", "text"), "Set the logging format. One of: text|json")
	command.PersistentFlags().StringVar(&logLevel, "loglevel", argocdconfig.GetFlag("loglevel", "info"), "Set the logging level. One of: debug|info|warn|error")
	return command
}

func initOpts(clientOpts *argocdclient.ClientOptions) {
	defaultLocalConfigPath, err := config.DefaultLocalConfigPath(cliName)
	errors.CheckError(err)
	clientOpts.ConfigPath = defaultLocalConfigPath
	clientOpts.AuthToken = ""
}
