package preupgrade

import (
	"time"

	log "github.com/sirupsen/logrus"
	"mxe.ericsson/pre-upgrade/utils"
)

func WaitUntilAllJobsFinish(kubeconfig string, resyncPeriodSecs uint,
	jobsSelector string, namespace string) {

	resyncPeriod := time.Duration(resyncPeriodSecs) * time.Second
	kubeClientImpl := utils.NewClientImp(kubeconfig)

	jobsListFunc := kubeClientImpl.NewRunningJobsSharedInformer(
		resyncPeriod, jobsSelector, namespace)

	ticker := time.NewTicker(resyncPeriod)
	done := make(chan bool)
	defer close(done)

	go func() {
		for {
			<-ticker.C
			log.Infof("Checking for running jobs matching selector %s", jobsSelector)
			runningJobs, err := jobsListFunc()
			if err != nil {
				log.Fatalf("Failed to list jobs: %v", err)
			}

			if len(runningJobs) == 0 {
				log.Infoln("There are no jobs with selector", jobsSelector, "currently running on the cluster")
				done <- true
			} else {
				for _, job := range runningJobs {
					log.Infoln("Job", job.Name, "is running")
				}
			}
		}

	}()

	<-done
	ticker.Stop()
}
