package cmd

import (
	"testing"

	"github.com/stretchr/testify/assert"

	"mxe.ericsson/utils"
)

func Test_list_unknownCluster(t *testing.T) {
	utils.TestLogRegex(t, "Cluster does not exist: definitely_does_not_exist", func() {
		CmdSetArgs([]string{"list", "jobs", "--cluster=definitely_does_not_exist"})
		Execute()
	})
}

func Test_list_unknownClusterWithArg(t *testing.T) {
	utils.TestLogRegex(t, "Cluster does not exist: definitely_does_not_exist", func() {
		CmdSetArgs([]string{"list", "jobs", "--cluster=definitely_does_not_exist", "vgg16-2"})
		Execute()
	})
}

var trainingPackage1 = utils.ListTrainingPackagesResponse{
	Created:     "2018-12-10T16:56:33Z",
	Id:          "training1",
	Version:     "1.0.0",
	Title:       "Test title",
	Author:      "tester",
	Description: "Test Description",
	Image:       "trainingimage:1.0.0",
	Icon:        "",
	Status:      "created",
	Message:     "",
	ErrorLog:    "",
	Internal:    true,
}

var trainingPackage2 = utils.ListTrainingPackagesResponse{
	Created:     "2018-12-10T16:56:33Z",
	Id:          "training2",
	Version:     "2.0.0",
	Title:       "Test title",
	Author:      "tester",
	Description: "Test Description",
	Image:       "trainingimage:1.0.0",
	Icon:        "",
	Status:      "error",
	Message:     "Test error",
	ErrorLog:    "Test error log",
	Internal:    true,
}

var trainingJob1 = utils.ListTrainingJobsResponse{
	Created:        "2018-12-10T16:56:33Z",
	Id:             "training.job.1",
	PackageId:      "training1",
	PackageVersion: "1.0.0",
	Status:         "completed",
	Message:        "",
	ErrorLog:       "",
	Completed:      "2018-12-10T16:56:33Z",
}

var trainingJob2 = utils.ListTrainingJobsResponse{
	Created:        "2018-12-10T16:56:33Z",
	Id:             "training.job.2",
	PackageId:      "training2",
	PackageVersion: "2.0.0",
	Status:         "failed",
	Message:        "Test error",
	ErrorLog:       "Test error log",
	Completed:      "2018-12-10T16:56:33Z",
}

func Test_ListPackages(t *testing.T) {
	var packages []utils.ListTrainingPackagesResponse
	packages = append(packages, trainingPackage1)
	packages = append(packages, trainingPackage2)

	_, content := prettyPrintPackages(packages)
	assert.Equal(t, len(content), 2, "")
}

func Test_ListJobs(t *testing.T) {
	var jobs []utils.ListTrainingJobsResponse
	jobs = append(jobs, trainingJob1)
	jobs = append(jobs, trainingJob2)

	_, content := prettyPrintJobs(jobs)
	assert.Equal(t, len(content), 2, "")
}
