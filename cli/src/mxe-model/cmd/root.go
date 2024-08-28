package cmd

import (
	"fmt"

	"github.com/spf13/cobra"

	"mxe.ericsson/utils"
)

var verbose bool

var rootCmd = &cobra.Command{
	Use:     "mxe-model",
	Short:   "MXE model life-cycle management commands.",
	Long:    "",
	Example: "mxe-model list --cluster production",
}

var cluster string

func init() {
	rootCmd.PersistentFlags().StringVar(&cluster, "cluster", "", "The MXE cluster to use. Optional. Default value is based on your configuration.")
	rootCmd.PersistentFlags().BoolVarP(&verbose, "verbose", "v", false, "Makes output verbose, showing command outputs.")
	setCmdHelp(rootCmd, "Help about mxe-model commands.")

	rootCmd.PersistentFlags().BoolP("help", "h", false, "Prints out this help.")
}

func CmdSetArgs(a []string) {
	rootCmd.SetArgs(a)
}

func GenBashCompletion(filename string) error {
	return rootCmd.GenBashCompletionFile(filename)
}

func exitError(err error) {
	utils.LogError(err.Error())
	utils.Exit(1)
}

func exitErrorWithHelp(err error, cmd *cobra.Command) {
	cmd.Help()
	exitError(err)
}

func setCmdHelp(command *cobra.Command, short string) {
	command.SetHelpCommand(&cobra.Command{
		Use:   "help",
		Short: short,
		RunE: func(cmd *cobra.Command, args []string) error {
			return command.Help()
		},
	})
}

// Execute adds all child commands to the root command and sets flags appropriately.
// This is called by main.main(). It only needs to happen once to the rootCmd.
func Execute() {
	if err := rootCmd.Execute(); err != nil {
		utils.LogError(err.Error())
		return
	}
}

func init() {
	rootCmd.AddCommand(listCmd)
	rootCmd.AddCommand(packageCmd)
	rootCmd.AddCommand(versionCmd)
	rootCmd.AddCommand(onboardCmd)
	rootCmd.AddCommand(deleteCmd)
	rootCmd.AddCommand(completionCmd)
}

func runChecks() bool {
	utils.Verbose = verbose
	var clusterErr = false
	clusterErr, cluster = utils.GetCluster(cluster)
	if clusterErr {
		utils.LogError(fmt.Sprintf("Cluster does not exist: %s", cluster))
		return false
	}
	return true
}
