package cluster

import (
	"context"

	"github.com/go-kit/kit/endpoint"
	httptransport "github.com/go-kit/kit/transport/http"
	"github.com/gorilla/mux"
	httpUtil "mxe.ericsson/depmanager/utils/http"
)

const (
	basePath string = "/v1"
	//ListClusterPath list cluster rest path
	ListClusterPath string = basePath + "/listclusters"
)

// Endpoints struct
type Endpoints struct {
	ListClusterEndpoint endpoint.Endpoint
}

// MakeServerEndpoints from Server
func MakeServerEndpoints(s Service) Endpoints {
	return Endpoints{
		ListClusterEndpoint: MakeListClusterEndpoint(s),
	}
}

// MakeListClusterEndpoint returns an endpoint via the passed service.
// Primarily useful in a server.
func MakeListClusterEndpoint(s Service) endpoint.Endpoint {
	return func(ctx context.Context, request interface{}) (response interface{}, err error) {
		req := request.(ListClusterRequest)
		clusterList, err := s.ListCluster(ctx, req.AuthToken)
		var listOfClusterNames []string
		for _, e := range clusterList.Items {
			listOfClusterNames = append(listOfClusterNames, e.Name)
		}
		return ListClusterResponse{ClustersList: listOfClusterNames, Err: err}, nil
	}
}

// RegisterHTTPHandlers with handler functions
func RegisterHTTPHandlers(r *mux.Router, e Endpoints, options ...httptransport.ServerOption) {

	r.Methods("GET").Path(ListClusterPath).Handler(httptransport.NewServer(
		e.ListClusterEndpoint,
		decodeListClusterResponse,
		httpUtil.EncodeJSONResponse,
		options...,
	))
}
