package main

import (
	"github.com/argoproj/argo-cd/v2/util/errors"
	"mxe.ericsson/depmanager/dmcli/cmd"

	// load the gcp plugin (required to authenticate against GKE clusters).
	_ "k8s.io/client-go/plugin/pkg/client/auth/gcp"
	// load the oidc plugin (required to authenticate with OpenID Connect).
	_ "k8s.io/client-go/plugin/pkg/client/auth/oidc"
	// load the azure plugin (required to authenticate with AKS clusters).
	_ "k8s.io/client-go/plugin/pkg/client/auth/azure"
)

func main() {
	err := cmd.NewCommand().Execute()
	errors.CheckError(err)
}
