package cmd

import (
	"testing"

	"mxe.ericsson/utils"
)

func Test_start_idAndversionMissing(t *testing.T) {
	utils.TestLogRegex(t, "Error: required flag\\(s\\) \"packageId\", \"packageVersion\" not set.*", func() {
		CmdSetArgs([]string{"start"})
		Execute()
	})
}

func Test_start_versionMissing(t *testing.T) {
	utils.TestLogRegex(t, "Error: required flag\\(s\\) \"packageVersion\" not set", func() {
		CmdSetArgs([]string{"start", "--packageId=test"})
		Execute()
	})
}

func Test_start_idEmpty(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --packageId", func() {
		CmdSetArgs([]string{"start", "--packageVersion=1.0.0", "--packageId"})
		Execute()
	})
}

func Test_start_versionEmpty(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --packageVersion", func() {
		CmdSetArgs([]string{"start", "--packageId=test", "--packageVersion"})
		Execute()
	})
}

func Test_start_versionInvalid(t *testing.T) {
	utils.TestLogRegex(t, "Error: Incorrect version format. Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4", func() {
		CmdSetArgs([]string{"start", "--packageId=test", "--packageVersion=a.b.c"})
		Execute()
	})
}
