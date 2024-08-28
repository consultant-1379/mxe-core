package cmd

import (
	"testing"

	"mxe.ericsson/utils"
)

func Test_onboard_allMissing(t *testing.T) {
	utils.TestLogRegex(t, "Error: At least one of the the following flags should be set: archive, docker, source", func() {
		CmdSetArgs([]string{"onboard", "--cluster=default"})
		Execute()
	})
}

func Test_onboard_bothImageSourceGiven(t *testing.T) {
	utils.TestLogRegex(t, "Error: Both docker and source flag are set, only one of them can be used", func() {
		CmdSetArgs([]string{"onboard", "--docker=iotdd:1.0.0", "--source=/source", "--cluster=default"})
		Execute()
		onboardDockerImage = ""
		onboardModelSourcePath = ""
	})
}

func Test_onboard_bothArchiveImageGiven(t *testing.T) {
	utils.TestLogRegex(t, "Error: Both archive and docker flag are set, only one of them can be used", func() {
		CmdSetArgs([]string{"onboard", "--archive=/archive", "--docker=iotdd:1.0.0", "--cluster=default"})
		Execute()
		onboardDockerImage = ""
		onboardModelSourcePath = ""
	})
}

func Test_onboard_bothArchiveSourceGiven(t *testing.T) {
	utils.TestLogRegex(t, "Error: Both archive and source flag are set, only one of them can be used", func() {
		CmdSetArgs([]string{"onboard", "--archive=/archive", "--source=/source", "--cluster=default"})
		Execute()
		onboardDockerImage = ""
		onboardModelSourcePath = ""
	})
}

func Test_onboard_allGiven(t *testing.T) {
	utils.TestLogRegex(t, "Error: Archive, docker, and source flag are set, only one of them can be used", func() {
		CmdSetArgs([]string{"onboard", "--archive=/archive", "--docker=iotdd:1.0.0", "--source=/source", "--cluster=default"})
		Execute()
		onboardDockerImage = ""
		onboardModelSourcePath = ""
	})
}

func Test_onboard_idVersionTitleMissing(t *testing.T) {
	utils.TestLogRegex(t, "Error: required flag\\(s\\) id, version, title not set", func() {
		CmdSetArgs([]string{"onboard", "--docker=iotdd:1.0.0", "--description=This is the IoT device detection model", "--cluster=default"})
		Execute()
	})
}

func Test_onboard_idTitleMissing(t *testing.T) {
	utils.TestLogRegex(t, "Error: required flag\\(s\\) id, title not set", func() {
		CmdSetArgs([]string{})
		CmdSetArgs([]string{"onboard", "--docker=iotdd:1.0.0", "--description=This is the IoT device detection model", "--version=1.0.0", "--cluster=default"})
		Execute()
	})
}

func Test_onboard_idEmpty(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --id", func() {
		CmdSetArgs([]string{"onboard", "--description=This is the IoT device detection model", "--version=1.0.0", "--docker=iotdd:1.0.0", "--title=TestTitle", "--id"})
		Execute()
	})
}

func Test_onboard_idInvalid(t *testing.T) {
	utils.TestLogRegex(t, "Error: The id name_with_underscore shall only contain lower case alphanumeric characters, and \\.\\(dot\\)\\.", func() {
		CmdSetArgs([]string{"onboard", "--description=This is the IoT device detection model", "--id=name_with_underscore", "--version=1.0.0", "--docker=iotdd:1.0.0", "--title=TestTitle"})
		Execute()
	})
}

func Test_onboard_versionEmpty(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --version", func() {
		CmdSetArgs([]string{"onboard", "--description=This is the IoT device detection model", "--id=iot.device.detection", "--docker=iotdd:1.0.0", "--cluster=default", "--title=TestTitle", "--version"})
		Execute()
	})
}

func Test_onboard_dockerImageEmpty(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --docker", func() {
		CmdSetArgs([]string{"onboard", "--description=This is the IoT device detection model", "--id=iot.device.detection", "--version=1.0.0", "--cluster=default", "--title=TestTitle", "--docker"})
		Execute()
	})
}

func Test_onboard_idTooLong(t *testing.T) {
	utils.TestLogRegex(t, "Error: Model ID must be at most 32 characters length.", func() {
		CmdSetArgs([]string{"onboard", "--description=This is the IoT device detection model",
			"--id=xxxxxxxxxxyyyyyyyyyyzzzzzzzzzzaab", "--version=1.0.0", "--docker=iotdd:1.0.0", "--title=TestTitle", "--cluster=default"})
		Execute()
	})
}

func Test_onboard_invalidVersion(t *testing.T) {
	utils.TestLogRegex(t, "Error: Incorrect version format. Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4", func() {
		CmdSetArgs([]string{"onboard", "--description=This is the IoT device detection model",
			"--id=xxx", "--version=1.0.a", "--docker=iotdd:1.0.0", "--title=TestTitle", "--cluster=default"})
		Execute()
	})
}

func Test_onboard_descriptionTooLong(t *testing.T) {
	utils.TestLogRegex(t, "Error: Description must be at most 120 characters length.", func() {
		CmdSetArgs([]string{"onboard",
			"--description=This is the IoT device detection modeThis is the IoT device detection modelThis is the IoT device detection modellThis is the IoT device detection modelThis is the IoT device detection model",
			"--id=xxxx", "--version=1.0.0", "--docker=iotdd:1.0.0", "--title=TestTitle", "--cluster=default"})
		Execute()
	})
}

func Test_onboard_unknownCluster(t *testing.T) {
	utils.TestLogRegex(t, "Cluster does not exist: definitely_does_not_exist", func() {
		CmdSetArgs([]string{"onboard", "--cluster=definitely_does_not_exist", "--description=This is the IoT device detection model",
			"--id=xxx", "--version=1.0.0", "--docker=iotdd:1.0.0", "--title=TestTitle"})
		Execute()
	})
}
