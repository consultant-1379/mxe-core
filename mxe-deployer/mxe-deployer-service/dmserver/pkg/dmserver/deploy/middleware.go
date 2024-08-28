package deploy

import (
	"context"
	"time"

	"github.com/go-kit/kit/log"
	fileUtils "mxe.ericsson/depmanager/utils/file"

	applicationpkg "github.com/argoproj/argo-cd/v2/pkg/apiclient/application"
	"github.com/argoproj/argo-cd/v2/pkg/apis/application/v1alpha1"
)

type loggingMiddleware struct {
	next   Service
	logger log.Logger
}

// Middleware describes a service (as opposed to endpoint) middleware.
type Middleware func(Service) Service

//LoggingMiddleware describes the logging middleware function
func LoggingMiddleware(logger log.Logger) Middleware {
	return func(next Service) Service {
		return &loggingMiddleware{
			next:   next,
			logger: logger,
		}
	}
}

func (mw loggingMiddleware) PostPackage(ctx context.Context, packageOptions *PackageRequestMeta, archiveFile *fileUtils.InputFile) (application *v1alpha1.Application, err error) {
	defer func(begin time.Time) {
		mw.logger.Log("method", "PostPackage", "took", time.Since(begin), "err", err)
	}(time.Now())
	return mw.next.PostPackage(ctx, packageOptions, archiveFile)
}

func (mw loggingMiddleware) PatchPackage(ctx context.Context, applicationSelector *applicationpkg.ApplicationQuery, archiveFile *fileUtils.InputFile) (application *v1alpha1.Application, err error) {
	defer func(begin time.Time) {
		mw.logger.Log("method", "PatchPackage", "took", time.Since(begin), "err", err)
	}(time.Now())
	return mw.next.PatchPackage(ctx, applicationSelector, archiveFile)
}

func (mw loggingMiddleware) GetPackages(ctx context.Context, applicationSelector *applicationpkg.ApplicationQuery) (applications *v1alpha1.ApplicationList, err error) {
	defer func(begin time.Time) {
		mw.logger.Log("method", "GetPackages", "took", time.Since(begin), "err", err)
	}(time.Now())
	return mw.next.GetPackages(ctx, applicationSelector)
}

func (mw loggingMiddleware) DeletePackage(ctx context.Context, applnName *string, propagationPolicy *string) DeleteApplicationResponse {
	defer func(begin time.Time) {
		mw.logger.Log("method", "DeletePackage", "took", time.Since(begin))
	}(time.Now())
	return mw.next.DeletePackage(ctx, applnName, propagationPolicy)
}

func (mw loggingMiddleware) SyncPackage(ctx context.Context, syncReq *applicationpkg.ApplicationSyncRequest) (*v1alpha1.Application, error) {
	defer func(begin time.Time) {
		mw.logger.Log("method", "SyncPackage", "took", time.Since(begin))
	}(time.Now())
	return mw.next.SyncPackage(ctx, syncReq)
}
