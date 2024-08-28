package config

import (
	"github.com/sirupsen/logrus"
	v1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	informersV1 "k8s.io/client-go/informers/core/v1"
	"k8s.io/client-go/tools/cache"
)

func tweakArgocdRepoListOptions(opts *metav1.ListOptions) {
	opts.LabelSelector = labels.SelectorFromSet(labels.Set(map[string]string{
		"argocd.argoproj.io/secret-type": "repository",
	})).String()
}

func (c *Config) StartWatchingArgoCDRepositories(stopCh <-chan struct{}) {

	indexers := cache.Indexers{cache.NamespaceIndex: cache.MetaNamespaceIndexFunc}
	s := informersV1.NewFilteredSecretInformer(c.client, c.argoCDNamespace, 0, indexers, tweakArgocdRepoListOptions)

	handlers := cache.ResourceEventHandlerFuncs{
		AddFunc: func(obj interface{}) {
			secret := obj.(*v1.Secret)
			logrus.Info("received add event!", secret.Name)
			c.AddRepository(secret)
		},
		UpdateFunc: func(oldObj, obj interface{}) {
			secret := obj.(*v1.Secret)
			logrus.Info("received update event!", secret.Name)
			c.ModifyRepository(secret)
		},
		DeleteFunc: func(obj interface{}) {
			secret := obj.(*v1.Secret)
			logrus.Info("received delete event!", secret.Name)
			c.DeleteRepository(secret)
		},
	}
	s.AddEventHandler(handlers)
	s.Run(stopCh)
}

func tweakArgocdRepoCredListOptions(opts *metav1.ListOptions) {
	opts.LabelSelector = labels.SelectorFromSet(labels.Set(map[string]string{
		"argocd.argoproj.io/secret-type": "repo-creds",
	})).String()
}

func (c *Config) StartWatchingArgoCDRepoCreds(stopCh <-chan struct{}) {

	indexers := cache.Indexers{cache.NamespaceIndex: cache.MetaNamespaceIndexFunc}
	s := informersV1.NewFilteredSecretInformer(c.client, c.argoCDNamespace, 0, indexers, tweakArgocdRepoCredListOptions)

	handlers := cache.ResourceEventHandlerFuncs{
		AddFunc: func(obj interface{}) {
			secret := obj.(*v1.Secret)
			logrus.Info("received add event!", secret.Name)
			c.AddRepositoryCredentials(secret)
		},
		UpdateFunc: func(oldObj, obj interface{}) {
			secret := obj.(*v1.Secret)
			logrus.Info("received update event!", secret.Name)
			c.ModifyRepositoryCredentials(secret)
		},
		DeleteFunc: func(obj interface{}) {
			secret := obj.(*v1.Secret)
			logrus.Info("received delete event!", secret.Name)
			c.DeleteRepositoryCredentials(secret)
		},
	}
	s.AddEventHandler(handlers)
	s.Run(stopCh)
}
