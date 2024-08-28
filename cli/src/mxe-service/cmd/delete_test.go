package cmd

import (
	"testing"

	"mxe.ericsson/utils"
)

func Test_delete_missingName(t *testing.T) {
	utils.TestLogRegex(t, "required flag\\(s\\) \"name\" not set", func() {
		cluster = utils.DefaultCluster()
		CmdSetArgs([]string{"delete"})
		Execute()
	})
}

func Test_delete_emptyName(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --name", func() {
		cluster = utils.DefaultCluster()
		CmdSetArgs([]string{"delete", "--name"})
		Execute()
	})
}

func Test_delete_unknownCluster_emptyName(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --name", func() {
		CmdSetArgs([]string{"delete", "--cluster=definitely_does_not_exist", "--name"})
		Execute()
	})
}

func Test_delete_invalidCluster(t *testing.T) {
	utils.TestLogRegex(t, "Cluster does not exist: definitely_does_not_exist", func() {
		CmdSetArgs([]string{"delete", "--cluster=definitely_does_not_exist", "--name=vgg16-1"})
		Execute()
	})
}
