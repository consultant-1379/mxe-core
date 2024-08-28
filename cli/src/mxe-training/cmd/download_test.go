package cmd

import (
	"testing"

	"mxe.ericsson/utils"
)

func Test_download_jobId_flag_missing(t *testing.T) {
	utils.TestLogRegex(t, "Error: required flag\\(s\\) \"jobId\" not set", func() {
		CmdSetArgs([]string{"download-results"})
		Execute()
	})
}

func Test_download_jobId_flag_empty(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --jobId", func() {
		CmdSetArgs([]string{"download-results", "--jobId"})
		Execute()
	})
}

func Test_download_unknownCluster(t *testing.T) {

	utils.TestLogRegex(t, "Cluster does not exist: definitely_does_not_exist", func() {
		CmdSetArgs([]string{"download-results", "--jobId=asdwqe12321", "--cluster=definitely_does_not_exist"})
		Execute()
	})
}
