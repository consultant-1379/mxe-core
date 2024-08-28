package deploy

import (
	"context"

	argocdclient "github.com/argoproj/argo-cd/v2/pkg/apiclient"
	"github.com/argoproj/argo-cd/v2/pkg/apiclient/application"
	"github.com/argoproj/argo-cd/v2/pkg/apis/application/v1alpha1"
	argoprojio "github.com/argoproj/argo-cd/v2/util/io"
	"github.com/pkg/errors"
)

func (s *deployService) SyncPackage(ctx context.Context, syncReq *application.ApplicationSyncRequest) (*v1alpha1.Application, error) {
	authToken := getAuthToken(ctx)
	clientOpts, err := s.getClientOptions(authToken)
	if err != nil {
		return nil, errors.Wrap(err, "Failed to get argocd clientoptions")
	}

	acdClient, err := argocdclient.NewClient(clientOpts)
	if err != nil {
		return nil, errors.Wrap(err, "Failed to instantiate argocd grpc client using provided client options")
	}

	applnConn, applnClient, err := acdClient.NewApplicationClient()
	if err != nil {
		return nil, errors.Wrap(err, "Failed to instantiate argocd application client")
	}

	defer argoprojio.Close(applnConn)

	return applnClient.Sync(ctx, syncReq)
}
