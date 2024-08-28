package utils

import (
	"context"
	"os"
	"sync"
	"time"

	batchv1 "k8s.io/api/batch/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/cache"
	"k8s.io/client-go/tools/clientcmd"
)

type ClientImpl struct {
	clients *kubernetes.Clientset
}

type JobListFunc func() ([]batchv1.Job, error)

func NewClientImp(kubeconfig string) ClientImpl {
	var config *rest.Config
	var err error
	if _, incluster := os.LookupEnv("KUBERNETES_SERVICE_HOST"); incluster {
		config, err = rest.InClusterConfig()
	} else {
		config, err = clientcmd.BuildConfigFromFlags("", kubeconfig)
	}

	if err != nil {
		panic(err.Error())
	}
	kubeClientSet, err := kubernetes.NewForConfig(config)
	if err != nil {
		panic(err.Error())
	}
	return ClientImpl{clients: kubeClientSet}
}

func (c *ClientImpl) NewRunningJobsSharedInformer(resyncPeriod time.Duration, jobsSelector string, namespace string) JobListFunc {
	var once sync.Once
	var jobListFunc JobListFunc

	once.Do(
		func() {
			restClient := c.clients.BatchV1().RESTClient()
			optionsModifer := func(options *metav1.ListOptions) {
				options.LabelSelector = jobsSelector
			}
			watchList := cache.NewFilteredListWatchFromClient(restClient, "jobs", namespace, optionsModifer)
			informer := cache.NewSharedInformer(watchList, &batchv1.Job{}, resyncPeriod)

			go informer.Run(context.Background().Done())

			jobListFunc = JobListFunc(func() (jobs []batchv1.Job, err error) {
				for _, c := range informer.GetStore().List() {
					job := *(c.(*batchv1.Job))
					if job.Status.Active > 0 {
						jobs = append(jobs, job)
					}
				}
				return jobs, nil
			})
		})

	return jobListFunc
}
