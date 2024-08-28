package deploy

import (
	"context"

	"github.com/go-kit/kit/log"

	"github.com/argoproj/argo-cd/v2/pkg/apiclient/application"
	"github.com/argoproj/argo-cd/v2/pkg/apis/application/v1alpha1"

	"mxe.ericsson/depmanager/utils/config"
	fileUtils "mxe.ericsson/depmanager/utils/file"
)

const (
	packageType string = "system"
	managedBy   string = "eric-mxe-deployer-service"
)

type deployService struct {
	Config *config.Config
	logger log.Logger
}

// Service is a simple CRUD interface for user profiles.
type Service interface {
	PostPackage(ctx context.Context, packageOptions *PackageRequestMeta, manifestArchive *fileUtils.InputFile) (*v1alpha1.Application, error)
	PatchPackage(ctx context.Context, applnData *application.ApplicationQuery, manifestArchive *fileUtils.InputFile) (*v1alpha1.Application, error)
	GetPackages(ctx context.Context, applnData *application.ApplicationQuery) (*v1alpha1.ApplicationList, error)
	DeletePackage(ctx context.Context, applnName *string, propagationPolicy *string) DeleteApplicationResponse
	SyncPackage(ctx context.Context, syncReq *application.ApplicationSyncRequest) (*v1alpha1.Application, error)
}

// New is the service implementation for different endpoints of depmanager deploy service
func New(conf *config.Config, logger log.Logger) Service {
	return &deployService{
		Config: conf,
		logger: logger,
	}
}
