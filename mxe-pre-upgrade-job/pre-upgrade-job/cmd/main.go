package main

import (
	"flag"
	"fmt"
	"log"
	"os"
	"strings"

	_ "k8s.io/client-go/plugin/pkg/client/auth"
	"mxe.ericsson/pre-upgrade/pkg/preupgrade"
	"mxe.ericsson/pre-upgrade/utils"
)

var (
	kubeconfig   string
	namespace    string
	logFormat    string
	logLevel     string
	resyncPeriod uint
	jobSelector  string
)

func initConfig() {
	utils.SetLogFormat(logFormat)
	utils.SetLogLevel(logLevel)
}

func GetHome() string {
	home, err := os.UserHomeDir()
	if err != nil {
		panic("User home is not defined")
	}
	return home
}

func main() {

	flag.StringVar(&kubeconfig, "kubeconfig", fmt.Sprintf("%s/.kube/config", GetHome()), "Optional Path to kubeconfig file")
	flag.StringVar(&namespace, "namespace", "", "Namespace where mxe is installed")
	flag.StringVar(&logFormat, "logFormat", "text", "one of text/json")
	flag.StringVar(&logLevel, "logLevel", "info", "one of debug|info|warn|error")
	flag.UintVar(&resyncPeriod, "check-interval", 10, "duration in seconds indicating how often the list of jobs would be resynced")
	flag.StringVar(&jobSelector, "jobSelector", "", "k8s label selector for filtering jobs")
	flag.Parse()

	initConfig()

	if strings.TrimSpace(jobSelector) == "" {
		log.Fatalln("jobSelector is not set")
	}

	preupgrade.WaitUntilAllJobsFinish(kubeconfig, resyncPeriod, jobSelector, namespace)
}
