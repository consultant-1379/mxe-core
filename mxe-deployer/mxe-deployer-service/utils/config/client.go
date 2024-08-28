package config

import (
	argocdclient "github.com/argoproj/argo-cd/v2/pkg/apiclient"
	"github.com/sirupsen/logrus"
)

const (
	// Constants for viper variable names. Will be used to set
	// default values as well as to get each value
	clientOptionsKey = "argocdServer.clientOptions"
)

func (c *Config) unmarshalClientOptions() {
	//clientOptionsStr := c.DMProps.GetString(clientOptionsKey)

	var clientOptions *argocdclient.ClientOptions

	err := c.DMProps.UnmarshalKey(clientOptionsKey, &clientOptions)
	if err != nil {
		logrus.Fatalf("failed to unmarshal argocd client options %#v", err)
	}

	c.Store.ArgoCDClientOptions = clientOptions

	logrus.Info("Clientoptions set as per DM Config")
}
