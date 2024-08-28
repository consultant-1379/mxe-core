package session

import (
	"context"

	"github.com/go-kit/kit/endpoint"
	httptransport "github.com/go-kit/kit/transport/http"
	"github.com/gorilla/mux"
	httpUtil "mxe.ericsson/depmanager/utils/http"
)

const (
	basePath    string = "/v1"
	SessionPath string = basePath + "/session"
)

type Endpoints struct {
	CreateSessionEndpoint endpoint.Endpoint
}

func MakeServerEndpoints(s Service) Endpoints {
	return Endpoints{
		CreateSessionEndpoint: MakeCreateSessionEndpoint(s),
	}
}

// MakeCreateSessionEndpoint returns an token via the passed service.
// Primarily useful in a server.
func MakeCreateSessionEndpoint(s Service) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (response interface{}, err error) {
		req := request.(*SessionCreateRequest)
		tokenString, refreshToken, err := s.CreateSession(ctx, req)
		return SessionCreateResponse{Token: tokenString, RefreshToken: refreshToken, Err: err}, nil
	}
}

func RegisterHTTPHandlers(r *mux.Router, e Endpoints, options ...httptransport.ServerOption) {
	r.Methods("POST").Path(SessionPath).Handler(httptransport.NewServer(
		e.CreateSessionEndpoint,
		decodeSessionCreateRequest,
		httpUtil.EncodeJSONResponse,
		options...,
	))

}
