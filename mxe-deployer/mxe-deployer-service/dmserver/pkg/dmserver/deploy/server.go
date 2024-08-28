package deploy

import (
	"context"
	"strings"

	"github.com/go-kit/kit/endpoint"
	httptransport "github.com/go-kit/kit/transport/http"
	kithttp "github.com/go-kit/kit/transport/http"
	"github.com/gorilla/mux"
	httpUtil "mxe.ericsson/depmanager/utils/http"
)

const (
	basePath string = "/v1"
	//PackagePath endpoint package is wrapper for ArgocCD applications
	PackagePath     string = basePath + "/package"
	PackageSyncPath string = PackagePath + "/sync"
)

func getAuthToken(ctx context.Context) string {
	authInfo := ctx.Value(kithttp.ContextKeyRequestAuthorization).(string)
	authToken := strings.TrimPrefix(authInfo, "Bearer ")
	return authToken
}

//Endpoints struct for endpoint.Endpoint
type Endpoints struct {
	PostPackageEndpoint       endpoint.Endpoint
	PostPackageSyncEndpoint   endpoint.Endpoint
	PatchPackageEndpoint      endpoint.Endpoint
	GetPackagesEndpoint       endpoint.Endpoint
	GetSystemPackagesEndpoint endpoint.Endpoint
	DeletePackageEndpoint     endpoint.Endpoint
}

// MakeServerEndpoints from Server
func MakeServerEndpoints(s Service) Endpoints {
	return Endpoints{
		PostPackageEndpoint:     MakePostPackageEndpoint(s),
		PostPackageSyncEndpoint: MakePostPackageSyncEndpoint(s),
		PatchPackageEndpoint:    MakePatchPackageEndpoint(s),
		GetPackagesEndpoint:     MakeGetPackagesEndpoint(s),
		DeletePackageEndpoint:   MakeDeletePackageEndpoint(s),
	}
}

// MakePostPackageEndpoint returns an endpoint via the passed service.
// Primarily useful in a server.
func MakePostPackageEndpoint(s Service) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (response interface{}, err error) {
		req := request.(PostPackageRequest)
		appln, e := s.PostPackage(ctx, req.PackageRequestMeta, req.Archive)
		return PostPackageResponse{Application: appln, Err: e}, nil
	}
}

// MakePostPackageSyncEndpoint returns an endpoint via the passed service.
// Primarily useful in a server.
func MakePostPackageSyncEndpoint(s Service) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (response interface{}, err error) {
		req := request.(PostPackageSyncRequest)
		appln, e := s.SyncPackage(ctx, req.SyncReq)
		return PostPackageSyncResponse{Application: appln, Err: e}, nil
	}
}

// MakePatchPackageEndpoint returns an endpoint via the passed service.
// Primarily useful in a server.
func MakePatchPackageEndpoint(s Service) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (response interface{}, err error) {
		req := request.(PatchPackageRequest)
		appln, e := s.PatchPackage(ctx, req.ApplicationQuery, req.Archive)
		return PatchPackageResponse{Application: appln, Err: e}, nil
	}
}

//MakeGetPackagesEndpoint returns endpoint via the passed service
//Primarily useful in server
func MakeGetPackagesEndpoint(s Service) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (response interface{}, err error) {
		req := request.(GetApplicationsRequest)
		applns, e := s.GetPackages(ctx, req.AppQuery)
		return GetApplicationsResponse{ApplicationsList: applns, Err: e}, nil
	}
}

// MakeDeletePackageEndpoint returns an endpoint via the passed service.
// Primarily useful in a server.
func MakeDeletePackageEndpoint(s Service) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (response interface{}, err error) {
		req := request.(DeleteApplicationRequest)
		appln := s.DeletePackage(ctx, req.ApplicationName, req.PropagationPolicy)
		return appln, nil
	}
}

// RegisterHTTPHandlers map the handler functions
func RegisterHTTPHandlers(r *mux.Router, e Endpoints, options ...httptransport.ServerOption) {
	r.Methods("POST").Path(PackagePath).Handler(httptransport.NewServer(
		e.PostPackageEndpoint,
		decodePostPackageRequest,
		httpUtil.EncodeJSONResponse,
		options...,
	))

	r.Methods("POST").Path(PackageSyncPath).Handler(httptransport.NewServer(
		e.PostPackageSyncEndpoint,
		decodePostPackageSyncRequest,
		httpUtil.EncodeJSONResponse,
		options...,
	))

	r.Methods("PATCH").Path(PackagePath).Handler(httptransport.NewServer(
		e.PatchPackageEndpoint,
		decodePatchPackageRequest,
		httpUtil.EncodeJSONResponse,
		options...,
	))

	r.Methods("GET").Path(PackagePath).Handler(httptransport.NewServer(
		e.GetPackagesEndpoint,
		decodeGetPackagesRequest,
		httpUtil.EncodeJSONResponse,
		options...,
	))

	r.Methods("DELETE").Path(PackagePath).Handler(httptransport.NewServer(
		e.DeletePackageEndpoint,
		decodeDeletePackageRequest,
		httpUtil.EncodeJSONResponse,
		options...,
	))
}
