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

func Test_list_unknownClusterWithArg(t *testing.T) {
	utils.TestLogRegex(t, "Cluster does not exist: definitely_does_not_exist", func() {
		CmdSetArgs([]string{"list", "--cluster=definitely_does_not_exist", "vgg16-2"})
		Execute()
	})
}

var deployment1Model1 = utils.ModelDetails{Id: "vmx-eea169:5000/img_vgg16", Version: "0.0.1"}
var deployment1Model2 = utils.ModelDetails{Id: "vmx-eea169:5000/img_inception3", Version: "0.0.1"}
var deployment1 = utils.ListStartedModelResponse{
	Models:   []utils.ModelDetails{deployment1Model1, deployment1Model2},
	Created:  "2018-12-10T16:56:33Z",
	Name:     "img-abtest",
	Replicas: 1,
	Type:     "abtest",
	Status:   "Creating",
}
var deployment2Model1 = utils.ModelDetails{Id: "vmx-eea169:5000/img_vgg16", Version: "0.0.1"}
var deployment2Model2 = utils.ModelDetails{Id: "vmx-eea169:5000/img_inception3", Version: "0.0.1"}
var deployment2 = utils.ListStartedModelResponse{
	Models:   []utils.ModelDetails{deployment2Model1, deployment2Model2},
	Created:  "2018-12-06T12:16:32Z",
	Name:     "img-mab",
	Replicas: 1,
	Type:     "mab",
	Status:   "Creating",
}
var deployment3Model1 = utils.ModelDetails{Id: "vmx-eea169:5000/img_inception3", Version: "0.0.1"}
var deployment3 = utils.ListStartedModelResponse{
	Models:   []utils.ModelDetails{deployment3Model1},
	Created:  "2018-12-07T14:52:56Z",
	Name:     "production-model-img",
	Replicas: 1,
	Type:     "model",
	Status:   "Creating",
}

var deployment4Model1 = utils.ModelDetails{Id: "vmx-eea169:5000/img_inception3", Version: "0.0.1"}
var autoscalingMetric = utils.AutoscalingMetric{AutoscalingMetricName: "cpuMilliCores",
	TargetAverageValue: 10}
var autoScalingData = utils.AutoscalingData{
	MinReplicas: 1,
	MaxReplicas: 2,
	Metrics:     []utils.AutoscalingMetric{autoscalingMetric},
}
var deployment4 = utils.ListStartedModelResponse{
	Models:      []utils.ModelDetails{deployment3Model1},
	Created:     "2018-12-07T14:52:56Z",
	Name:        "production-model-img-autoscale",
	Replicas:    1,
	Type:        "model",
	Status:      "Creating",
	AutoScaling: &autoScalingData,
}

func TestFilterNotMatchingAnyDeployments(t *testing.T) {
	var deployments []utils.ListStartedModelResponse
	deployments = append(deployments, deployment1)
	deployments = append(deployments, deployment2)
	deployments = append(deployments, deployment3)

	_, content := createServiceListTableData(filterServiceListByName(deployments, "no-such-model"))
	assert.Equal(t, len(content), 0, "")
}

func TestFilterNotMatchingOneDeployment(t *testing.T) {
	var deployments []utils.ListStartedModelResponse
	deployments = append(deployments, deployment1)
	deployments = append(deployments, deployment2)
	deployments = append(deployments, deployment3)

	_, content := createServiceListTableData(filterServiceListByName(deployments, "img-mab"))
	assert.Equal(t, len(content), 1, "")
}

func TestFilterNotMatchingAllDeployments(t *testing.T) {
	var deployments []utils.ListStartedModelResponse
	deployments = append(deployments, deployment1)
	deployments = append(deployments, deployment2)
	deployments = append(deployments, deployment3)

	_, content := createServiceListTableData(deployments)
	assert.Equal(t, len(content), 3, "")
}

func TestAutoscalingData(t *testing.T) {
	var deployments []utils.ListStartedModelResponse
	deployments = append(deployments, deployment1)
	deployments = append(deployments, deployment2)
	deployments = append(deployments, deployment3)

	header, content := createServiceListTableData(deployments)
	assert.Equal(t, len(content), 3, "")
	autoScalingHeaderFound := false
	for _, headerElement := range header {
		if headerElement == "AUTOSCALING" {
			autoScalingHeaderFound = true
		}
	}
	assert.False(t, autoScalingHeaderFound, "")

	deployments = append(deployments, deployment4)
	header, content = createServiceListTableData(deployments)
	assert.Equal(t, len(content), 4, "")
	for _, headerElement := range header {
		if headerElement == "AUTOSCALING" {
			autoScalingHeaderFound = true
		}
	}
	assert.True(t, autoScalingHeaderFound, "")
}
