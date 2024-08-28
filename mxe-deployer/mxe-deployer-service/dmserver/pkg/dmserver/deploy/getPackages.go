package deploy

import (
	"context"

	argocdclient "github.com/argoproj/argo-cd/v2/pkg/apiclient"
	"github.com/argoproj/argo-cd/v2/pkg/apiclient/application"
	"github.com/argoproj/argo-cd/v2/pkg/apis/application/v1alpha1"
	argoprojio "github.com/argoproj/argo-cd/v2/util/io"
	"github.com/pkg/errors"
)

func (s *deployService) GetPackages(ctx context.Context, applnQuery *application.ApplicationQuery) (*v1alpha1.ApplicationList, error) {
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

	if applnQuery.Name != nil {
		return s.getPackage(ctx, applnQuery, applnClient)
	} else {
		return s.listPackage(ctx, applnQuery, applnClient)
	}

}

func (s *deployService) listPackage(ctx context.Context,
	applnQuery *application.ApplicationQuery, applnClient application.ApplicationServiceClient) (*v1alpha1.ApplicationList, error) {

	alpha1ApplnList, err := applnClient.List(ctx, applnQuery)
	if err != nil {
		return nil, errors.Wrap(err, "Failed to list applications")
	}

	return alpha1ApplnList, err
}

func (s *deployService) getPackage(ctx context.Context,
	applnQuery *application.ApplicationQuery, applnClient application.ApplicationServiceClient) (*v1alpha1.ApplicationList, error) {

	appln, err := applnClient.Get(ctx, applnQuery)
	if err != nil {
		return nil, errors.Wrapf(err, "Failed to get application %s", *applnQuery.Name)
	}

	return &v1alpha1.ApplicationList{Items: []v1alpha1.Application{*appln}}, err
}
