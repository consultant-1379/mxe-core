package session

import (
	"context"
	"time"

	"github.com/go-kit/kit/log"
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

func (mw loggingMiddleware) CreateSession(ctx context.Context, sessionCreateReq *SessionCreateRequest) (tokem string, refreshToken string, err error) {
	defer func(begin time.Time) {
		mw.logger.Log("method", "SessionCreate", "took", time.Since(begin), "err", err)
	}(time.Now())
	return mw.next.CreateSession(ctx, sessionCreateReq)
}
