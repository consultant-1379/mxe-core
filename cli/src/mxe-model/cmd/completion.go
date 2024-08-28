package cmd

import (
	"os"

	"github.com/spf13/cobra"
)

var completionLongDescription = `To load completion run

source <(mxe-model completion bash)

To configure your bash shell to load completions for each session add to your bashrc

# ~/.bashrc or ~/.profile
source <(mxe-model completion bash)
`

// completionCmd represents the completion command
var completionCmd = &cobra.Command{
	Use:   "completion",
	Short: "Generates completion scripts",
	Long:  completionLongDescription,
}

var modelBashCompletionCmd = &cobra.Command{
	Use:   "bash",
	Short: "Generates bash completion scripts",
	Long:  completionLongDescription,

	Run: func(cmd *cobra.Command, args []string) {
		rootCmd.GenBashCompletion(os.Stdout)
	},
}

func init() {
	completionCmd.AddCommand(modelBashCompletionCmd)
}
