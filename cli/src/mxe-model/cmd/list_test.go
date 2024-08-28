package cmd

import (
	"testing"

	"github.com/stretchr/testify/assert"

	"mxe.ericsson/utils"
)

func Test_list_unknownCluster(t *testing.T) {
	utils.TestLogRegex(t, "Cluster does not exist: definitely_does_not_exist", func() {
		CmdSetArgs([]string{"list", "--cluster=definitely_does_not_exist"})
		Execute()
	})
}

var model1 = utils.ListOnboardedModelResponse{
	Created:     "2018-12-10T16:56:33Z",
	Id:          "img-test",
	Description: "testdescription",
	Version:     "1.0.0",
	Image:       "img.test",
	Icon:        "testicon",
	Status:      "available",
	Message:     "",
	ErrorLog:    "",
	User:        "testUser",
}

var model2 = utils.ListOnboardedModelResponse{
	Created:     "2018-12-10T16:56:33Z",
	Id:          "img-test2",
	Description: "testdescription",
	Version:     "2.0.0",
	Image:       "img.test2",
	Icon:        "testicon",
	Status:      "available",
	Message:     "",
	ErrorLog:    "",
	User:        "testUser",
}

var model3 = utils.ListOnboardedModelResponse{
	Created:     "2018-12-10T16:56:33Z",
	Id:          "img-test3",
	Description: "testdescription",
	Version:     "3.0.0",
	Image:       "img.test3",
	Icon:        "testicon",
	Status:      "available",
	Message:     "",
	ErrorLog:    "",
	User:        "testUser",
}

func TestList(t *testing.T) {
	var models []utils.ListOnboardedModelResponse
	models = append(models, model1)
	models = append(models, model2)
	models = append(models, model3)

	_, content := createModelListTableData(models)
	assert.Equal(t, len(content), 3, "")
}
