package utils

import (
	"encoding/json"
	"fmt"
	"os"
	"runtime"
)

type ClustersConfType struct {
	Default  string `json:"default"`
	Clusters []struct {
		Name        string `json:"name"`
		MxeEndpoint string `json:"mxeEndpoint"`
	} `json:"clusters"`
}

type InstallerConfType struct {
	InstallIntoSystem bool `json:"installIntoSystem"`
}

var apiUrl string
var clustersConf ClustersConfType
var selectedCluster string

var RunOnWindows bool

func init() {
	if runtime.GOOS == "windows" {
		RunOnWindows = true
	} else {
		RunOnWindows = false
	}
}

func loadClustersConf() ClustersConfType {
	var config ClustersConfType
	confFile, err := os.Open(fmt.Sprintf("%s%c%s", MxeDir(), os.PathSeparator, CLUSTER_CONFIG))
	defer confFile.Close()
	if err != nil {
		confFile, err = os.Open(fmt.Sprintf("%s%c%s%c%s", GetPwd(), os.PathSeparator, DATADIR, os.PathSeparator, CLUSTER_CONFIG))
		defer confFile.Close()
		if err != nil {
			// TODO change misleading error message! (There is no $USERHOME/.mxe in the beginning!)
			LogError(fmt.Sprintf("Failed to locate or parse MXE %s file, please reinstall MXE!", CLUSTER_CONFIG))
			Exit(1)
		}
	}
	confParser := json.NewDecoder(confFile)
	err = confParser.Decode(&config)
	if err != nil {
		LogError(fmt.Sprintf("Failed to parse config file: %s Error: %s", CLUSTER_CONFIG, err.Error()))
		Exit(1)
	}
	clustersConf = config
	return clustersConf
}

func GetCluster(cluster string) (bool, string) {
	loadClustersConf()

	if len(cluster) != 0 {
		return !SetCluster(cluster), cluster
	} else {
		return !SetCluster(DefaultCluster()), DefaultCluster()
	}
}

func API_URL() string {
	if len(apiUrl) == 0 {
		for _, element := range clustersConf.Clusters {
			if element.Name == selectedCluster {
				apiUrl = element.MxeEndpoint
			}
		}
		if len(apiUrl) == 0 {
			LogError("Failed to get API URL, there might be an error in the Configuration file?")
		}
		return apiUrl
	} else {
		return apiUrl
	}
}

func DefaultCluster() string {
	return clustersConf.Default
}

func GetSelectedCluster() string {
	return selectedCluster
}

/**
Check if the cluster exists and sets it as selected if it does.
Returns boolean, exists or not.
*/
func SetCluster(cluster string) bool {
	var exists = false
	for _, env := range clustersConf.Clusters {
		if env.Name == cluster {
			selectedCluster = cluster
			exists = true
		}
	}
	return exists
}
