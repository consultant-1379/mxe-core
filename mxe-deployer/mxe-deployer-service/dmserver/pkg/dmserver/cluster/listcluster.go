package cluster

import (
	"context"
	"fmt"
	"net/http"
	"strings"

	argocdclient "github.com/argoproj/argo-cd/v2/pkg/apiclient"
	"github.com/argoproj/argo-cd/v2/pkg/apiclient/cluster"
	"github.com/argoproj/argo-cd/v2/pkg/apis/application/v1alpha1"
	argoprojio "github.com/argoproj/argo-cd/v2/util/io"
	"github.com/jinzhu/copier"
	"github.com/pkg/errors"
)

func (s *clusterService) getClientOptions(authToken string) (*argocdclient.ClientOptions, error) {
	var clientOpts *argocdclient.ClientOptions = &argocdclient.ClientOptions{}

	err := copier.Copy(&clientOpts, &s.Config.Store.ArgoCDClientOptions)

	if err != nil {
		return nil, err
	}
	clientOpts.AuthToken = authToken

	return clientOpts, nil

}

// GetAuthTokenFromRequest get Authtoken from Bearer token
func GetAuthTokenFromRequest(r *http.Request) (string, bool) {
	var authToken string
	tokens, ok := r.Header["Authorization"]
	if ok && len(tokens) >= 1 {
		authToken = strings.TrimPrefix(tokens[0], "Bearer ")
	}
	return authToken, ok
}

// DecodeListClusterResponse decode cluste response
func decodeListClusterResponse(_ context.Context, r *http.Request) (request interface{}, err error) {
	defer r.Body.Close()
	authtoken, flag := GetAuthTokenFromRequest(r)

	if !flag {
		return nil, err
	}

	return ListClusterRequest{
		AuthToken: authtoken,
	}, nil

}

func (s *clusterService) ListCluster(ctx context.Context, authToken string) (*v1alpha1.ClusterList, error) {
	fmt.Println("called service with token", authToken)
	clientOpts, err := s.getClientOptions(authToken)
	if err != nil {
		return nil, errors.Wrap(err, "Failed to get argocd clientoptions")
	}
	fmt.Printf("\n\nClientOpts is %#v", clientOpts)

	acdClient, err := argocdclient.NewClient(clientOpts)
	if err != nil {
		return nil, errors.Wrap(err, "Failed to instantiate argocd grpc client using provided client options")
	}

	clusConn, clusterClient, err := acdClient.NewClusterClient()
	if err != nil {
		return nil, errors.Wrap(err, "Failed to instantiate argocd cluster client")
	}

	defer argoprojio.Close(clusConn)
	clusterQuery := cluster.ClusterQuery{}
	alpha1ClusterList, err := clusterClient.List(ctx, &clusterQuery)

	return alpha1ClusterList, err

}
