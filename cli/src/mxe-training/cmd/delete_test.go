package cmd

import (
	"testing"

	"mxe.ericsson/utils"
)

func Test_delete_package_idAndVersionMissing(t *testing.T) {
	utils.TestLogRegex(t, "Error: required flag\\(s\\) \"id\", \"version\" not set.*", func() {
		CmdSetArgs([]string{"delete", "package"})
		Execute()
	})
}

func Test_delete_package_versionMissing(t *testing.T) {
	utils.TestLogRegex(t, "Error: required flag\\(s\\) \"version\" not set", func() {
		CmdSetArgs([]string{"delete", "package", "--id=test"})
		Execute()
	})
}

func Test_delete_package_idEmpty(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --id", func() {
		CmdSetArgs([]string{"delete", "package", "--version=1.0.0", "--id"})
		Execute()
	})
}

func Test_delete_package_versionEmpty(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --version", func() {
		CmdSetArgs([]string{"delete", "package", "--id=test", "--version"})
		Execute()
	})
}

func Test_delete_package_versionInvalid(t *testing.T) {
	utils.TestLogRegex(t, "Error: Incorrect version format. Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4\nIn case of the version is 'unknown', use this string as the version.", func() {
		CmdSetArgs([]string{"delete", "package", "--id=test", "--version=a.b.c"})
		Execute()
	})
}

func Test_delete_job_parametersMissing(t *testing.T) {
	utils.TestLogRegex(t, "Incorrect command usage. Valid usage.*", func() {
		CmdSetArgs([]string{"delete", "job"})
		Execute()
	})
}

func Test_delete_job_packageVersionMissing(t *testing.T) {
	utils.TestLogRegex(t, "Incorrect command usage. Valid usage.*", func() {
		CmdSetArgs([]string{"delete", "job", "--packageId=test"})
		Execute()
		deleteJobsPackageId = ""
	})
}

func Test_delete_job_packageIdMissing(t *testing.T) {
	utils.TestLogRegex(t, "Incorrect command usage. Valid usage.*", func() {
		CmdSetArgs([]string{"delete", "job", "--packageVersion=1.0.0"})
		Execute()
	})
}

func Test_delete_job_versionInvalid(t *testing.T) {
	utils.TestLogRegex(t, "Error: Incorrect version format. Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4", func() {
		CmdSetArgs([]string{"delete", "job", "--packageId=test", "--packageVersion=a.b.c"})
		Execute()
	})
}

func Test_delete_job_idEmpty(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --id", func() {
		CmdSetArgs([]string{"delete", "job", "--id"})
		Execute()
		deleteJobsPackageId = ""
	})
}

func Test_delete_job_packageVersionEmpty(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --packageVersion", func() {
		CmdSetArgs([]string{"delete", "job", "--packageId=test", "--packageVersion"})
		Execute()
		deleteJobsPackageId = ""
	})
}

func Test_delete_job_packageIdEmpty(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --packageId", func() {
		CmdSetArgs([]string{"delete", "job", "--packageVersion=1.0.0", "--packageId"})
		Execute()
	})
}
