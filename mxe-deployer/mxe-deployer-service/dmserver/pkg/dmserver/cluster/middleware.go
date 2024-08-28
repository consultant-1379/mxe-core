package cluster

import (
	"context"
	"time"

	"github.com/go-kit/kit/log"

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

func (mw loggingMiddleware) ListCluster(ctx context.Context, authToken string) (clusterList *v1alpha1.ClusterList, err error) {
	defer func(begin time.Time) {
		//mw.logger.Log("method", "PostDeploy", "id", r.ID, "took", time.Since(begin), "err", err)
	}(time.Now())
	return mw.next.ListCluster(ctx, authToken)
}
