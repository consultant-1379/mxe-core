package cmd

import (
	"testing"

	"mxe.ericsson/utils"
)

func Test_delete_idAndVersionMissing(t *testing.T) {
	utils.TestLogRegex(t, "Error: required flag\\(s\\) \"id\", \"version\" not set.*", func() {
		CmdSetArgs([]string{"delete"})
		Execute()
	})
}

func Test_delete_versionMissing(t *testing.T) {
	utils.TestLogRegex(t, "Error: required flag\\(s\\) \"version\" not set", func() {
		CmdSetArgs([]string{"delete", "--id=test"})
		Execute()
	})
}

func Test_delete_idEmpty(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --id", func() {
		CmdSetArgs([]string{"delete", "--version=1.0.0", "--id"})
		Execute()
	})
}

func Test_delete_versionEmpty(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --version", func() {
		CmdSetArgs([]string{"delete", "--id=test", "--version"})
		Execute()
	})
}

func Test_delete_versionInvalid(t *testing.T) {
	utils.TestLogRegex(t, "Error: Incorrect version format. Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4\nIn case of the version is 'unknown', use this string as the version.", func() {
		CmdSetArgs([]string{"delete", "--id=test", "--version=a.b.c"})
		Execute()
	})
}
