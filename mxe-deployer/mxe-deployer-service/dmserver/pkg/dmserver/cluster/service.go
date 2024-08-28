package cluster

import (
	"context"

	"github.com/argoproj/argo-cd/v2/pkg/apis/application/v1alpha1"

	"mxe.ericsson/depmanager/utils/config"
)

type clusterService struct {
	Config *config.Config
}

// Service is a simple CRUD interface for user profiles.
type Service interface {
	ListCluster(ctx context.Context, authToken string) (*v1alpha1.ClusterList, error)
}

// New is the service implementation for different endpoints of depmanager cluster service
func New(conf *config.Config) Service {
	return &clusterService{
		Config: conf,
	}
}
