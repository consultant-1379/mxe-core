package session

import (
	"context"

	"github.com/go-kit/kit/log"

	"mxe.ericsson/depmanager/utils/config"
)

type sessionService struct {
	Config *config.Config
	logger log.Logger
}

// Service is a simple CRUD interface for user profiles.
type Service interface {
	CreateSession(ctx context.Context, sessionOpts *SessionCreateRequest) (string, string, error)
}

// New is the service implementation for different endpoints of depmanager deploy service
func New(conf *config.Config, logger log.Logger) Service {
	return &sessionService{
		Config: conf,
		logger: logger,
	}
}
