package utils

import (
	"regexp"
	"strconv"
	"strings"

	"github.com/spf13/pflag"
)

type Version struct {
	Major uint64
	Minor uint64
	Patch uint64
}

func FlagChanged(name string, fs *pflag.FlagSet) bool {
	flag := fs.Lookup(name)
	if flag == nil {
		return false
	}
	return flag.Changed
}

func SeparateIdAndVersionList(s string) (bool, []string, []string) {

	models := strings.Split(s, ",")
	var modelIds []string
	var modelVersions []string
	for i := 0; i < len(models); i++ {
		result, id, version := SeparateIdAndVersion(strings.TrimSpace(models[i]))
		if !result {
			return false, nil, nil
		}
		modelIds = append(modelIds, id)
		modelVersions = append(modelVersions, version)
	}

	return true, modelIds, modelVersions
}

func SeparateIdAndVersion(s string) (bool, string, string) {
	ind := strings.LastIndex(s, ":")
	if ind == -1 || ind == 0 || ind == len(s)-1 {
		return false, "", ""
	}
	return true, s[:ind], s[ind+1:]
}

func SeparateWeights(s string) (bool, []*float64) {
	weightStrings := strings.Split(s, ",")
	var weights []*float64
	for i := 0; i < len(weightStrings); i++ {
		weight, err := strconv.ParseFloat(weightStrings[i], 64)
		if err != nil {
			return false, nil
		}
		weights = append(weights, &weight)
	}
	return true, weights
}

func SeparateNameMetric(s string) (bool, string, string) {
	switch s {
	case "cpuMilliCores":
		return true, "cpu", "m"
	case "memoryMegaBytes":
		return true, "memory", "Mi"
	default:
		return false, "", ""
	}
}

func IsValidModelIdList(ids []string) bool {
	for i := 0; i < len(ids); i++ {
		result := IsValidModelId(ids[i])
		if !result {
			return false
		}
	}
	return true
}

func IsValidModelId(id string) bool {
	r, err := regexp.MatchString("^[a-z,0-9,\\.]*$", id)
	return err == nil && r
}

func IsValidModelVersionList(versions []string) bool {
	for i := 0; i < len(versions); i++ {
		result := IsValidModelVersion(versions[i])
		if !result {
			return false
		}
	}
	return true
}

func IsValidModelVersion(version string) bool {
	tokens := strings.Split(version, ".")
	if len(tokens) != 3 {
		return false
	}

	for _, token := range tokens {
		_, err := strconv.ParseUint(token, 10, 64)
		if err != nil {
			return false
		}
	}

	return true
}

func IsValidModelName(name string) bool {
	r, err := regexp.MatchString("^[a-z0-9]+(-[a-z0-9]+)*$", name)
	if err != nil || !r {
		return false
	}
	return true
}

func ParseVersion(version string) (bool, Version) {
	s := strings.Split(version, ".")
	var versions []uint64
	if len(s) != 3 {
		return false, Version{0, 0, 0}
	}
	for _, nums := range s {
		version, err := strconv.ParseUint(nums, 10, 64)
		if err != nil {
			return false, Version{0, 0, 0}
		}
		versions = append(versions, version)
	}
	return true, Version{versions[0], versions[1], versions[2]}
}

func IsValidInstanceOrAutoScalingFormat(instances string, minreplicasParam string, maxreplicasParam string, metricParam string, targetAverageValueParam string) (bool, bool, *int, *AutoscalingData) {
	var autoScalingData *AutoscalingData
	var instance *int
	if instances != "auto" {
		validInstance, instance := IsValidInstancesFormat(instances)
		return false, validInstance, instance, autoScalingData
	}
	minReplicas, err := strconv.Atoi(minreplicasParam)
	if err != nil {
		return true, false, instance, autoScalingData
	}
	maxReplicas, err := strconv.Atoi(maxreplicasParam)
	if err != nil {
		return true, false, instance, autoScalingData
	}
	targetAverageValue, err := strconv.Atoi(targetAverageValueParam)
	if err != nil {
		return true, false, instance, autoScalingData
	}
	var metric string
	if metricParam == "cpu" {
		metric = "cpuMilliCores"
	} else if metricParam == "memory" {
		metric = "memoryMegaBytes"
	} else {
		return true, false, instance, autoScalingData
	}

	var autoScalingMetricList = []AutoscalingMetric{}
	var autoScalingMetric = AutoscalingMetric{
		AutoscalingMetricName: metric,
		TargetAverageValue:    targetAverageValue,
	}
	autoScalingMetricList = append(autoScalingMetricList, autoScalingMetric)

	autoScalingData = &AutoscalingData{
		MinReplicas: minReplicas,
		MaxReplicas: maxReplicas,
		Metrics:     autoScalingMetricList,
	}

	return true, true, instance, autoScalingData
}

func IsValidInstancesFormat(i string) (bool, *int) {
	var zeroInt = int(0)
	ins, err := strconv.Atoi(i)
	if err != nil || ins < 1 {
		return false, &zeroInt
	}
	return true, &ins
}

// There should be a better way to do this in go...
func CompareVersions(v1, v2 Version) int {
	if v1.Major > v2.Major {
		return 1
	} else if v1.Major < v2.Major {
		return -1
	}

	if v1.Minor > v2.Minor {
		return 1
	} else if v1.Minor < v2.Minor {
		return -1
	}

	if v1.Patch > v2.Patch {
		return 1
	} else if v1.Patch < v2.Patch {
		return -1
	}

	return 0

}
