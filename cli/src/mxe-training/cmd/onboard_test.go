package cmd

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"

	"mxe.ericsson/utils"
)

const errorLogRegex = "Error: training package %s is missing from %s/MXE-META-INF/INFO"

func Test_onboard_sourceMissing(t *testing.T) {
	utils.TestLogRegex(t, "Error: required flag\\(s\\) \"source\" not set.*", func() {
		CmdSetArgs([]string{"onboard", "--cluster=default"})
		Execute()
	})
}

func Test_onboard_packageInfoIdMissingInvalid(t *testing.T) {
	packageSourcePath = "/testSourcePath"
	testPackageInfo := make(map[string]string)
	testPackageInfo["Version"] = "0.0.1"
	testPackageInfo["Type"] = "Training"
	testPackageInfo["Title"] = "Test Title"
	testPackageInfo["Author"] = "tester"
	testPackageInfo["Description"] = "Test Description"

	utils.TestLogRegex(t, fmt.Sprintf(errorLogRegex, "Id", packageSourcePath), func() {
		result := validatePackageInfo(testPackageInfo)
		assert.Equal(t, result, false, "")
	})

	testPackageInfo["Id"] = "Test-ID"
	utils.TestLogRegex(t, "training package Id can only contain lower case alphanumeric characters and dots (.)", func() {
		result := validatePackageInfo(testPackageInfo)
		assert.Equal(t, result, false, "")
	})
}

func Test_onboard_packageInfoFileMissing(t *testing.T) {
	packageSourcePath = "/invalidTestSourcePath"
	testPackageInfo := make(map[string]string)
	testPackageInfo["Id"] = "test.id"
	testPackageInfo["Type"] = "Training"
	testPackageInfo["Title"] = "Test Title"
	testPackageInfo["Author"] = "tester"
	testPackageInfo["Description"] = "Test Description"

	utils.TestLogRegex(t, "Error: open "+packageSourcePath+"/MXE-META-INF/INFO: no such file or directory", func() {
		_, err := loadPropertyFile(packageSourcePath)
		assert.Error(t, err)
	})
}

func Test_onboard_packageInfoVersionMissingInvalid(t *testing.T) {
	packageSourcePath = "/testSourcePath"
	testPackageInfo := make(map[string]string)
	testPackageInfo["Id"] = "test.id"
	testPackageInfo["Type"] = "Training"
	testPackageInfo["Title"] = "Test Title"
	testPackageInfo["Author"] = "tester"
	testPackageInfo["Description"] = "Test Description"

	utils.TestLogRegex(t, fmt.Sprintf(errorLogRegex, "Version", packageSourcePath), func() {
		result := validatePackageInfo(testPackageInfo)
		assert.Equal(t, result, false, "")
	})

	testPackageInfo["Version"] = "a.b.c"
	utils.TestLogRegex(t, "invalid training package Version format. Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4", func() {
		result := validatePackageInfo(testPackageInfo)
		assert.Equal(t, result, false, "")
	})
}

func Test_onboard_packageInfoTypeMissingInvalid(t *testing.T) {
	packageSourcePath = "/testSourcePath"
	testPackageInfo := make(map[string]string)
	testPackageInfo["Id"] = "test.id"
	testPackageInfo["Version"] = "0.0.1"
	testPackageInfo["Title"] = "Test Title"
	testPackageInfo["Author"] = "tester"
	testPackageInfo["Description"] = "Test Description"

	utils.TestLogRegex(t, fmt.Sprintf(errorLogRegex, "Type", packageSourcePath), func() {
		result := validatePackageInfo(testPackageInfo)
		assert.Equal(t, result, false, "")
	})

	testPackageInfo["Type"] = "Model"
	utils.TestLogRegex(t, "invalid training package type. Valid type: Training", func() {
		result := validatePackageInfo(testPackageInfo)
		assert.Equal(t, result, false, "")
	})
}

func Test_onboard_packageInfoTitleMissingInvalid(t *testing.T) {
	packageSourcePath = "/testSourcePath"
	testPackageInfo := make(map[string]string)
	testPackageInfo["Id"] = "test.id"
	testPackageInfo["Version"] = "0.0.1"
	testPackageInfo["Type"] = "Training"
	testPackageInfo["Author"] = "tester"
	testPackageInfo["Description"] = "Test Description"

	utils.TestLogRegex(t, fmt.Sprintf(errorLogRegex, "Title", packageSourcePath), func() {
		result := validatePackageInfo(testPackageInfo)
		assert.Equal(t, result, false, "")
	})
}

func Test_onboard_packageInfoAuthorMissingInvalid(t *testing.T) {
	packageSourcePath = "/testSourcePath"
	testPackageInfo := make(map[string]string)
	testPackageInfo["Id"] = "test.id"
	testPackageInfo["Version"] = "0.0.1"
	testPackageInfo["Type"] = "Training"
	testPackageInfo["Title"] = "Test Title"
	testPackageInfo["Description"] = "Test Description"

	utils.TestLogRegex(t, fmt.Sprintf(errorLogRegex, "Author", packageSourcePath), func() {
		result := validatePackageInfo(testPackageInfo)
		assert.Equal(t, result, false, "")
	})
}

func Test_onboard_packageInfoDescriptionMissingInvalid(t *testing.T) {
	packageSourcePath = "/testSourcePath"
	testPackageInfo := make(map[string]string)
	testPackageInfo["Id"] = "test.id"
	testPackageInfo["Version"] = "0.0.1"
	testPackageInfo["Type"] = "Training"
	testPackageInfo["Title"] = "Test Title"
	testPackageInfo["Author"] = "tester"

	utils.TestLogRegex(t, fmt.Sprintf(errorLogRegex, "Description", packageSourcePath), func() {
		result := validatePackageInfo(testPackageInfo)
		assert.Equal(t, result, false, "")
	})
}
