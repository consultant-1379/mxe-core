package session

import (
	"context"
	"errors"

	argocdclient "github.com/argoproj/argo-cd/v2/pkg/apiclient"
	sessionpkg "github.com/argoproj/argo-cd/v2/pkg/apiclient/session"
	"github.com/argoproj/argo-cd/v2/util/io"
	oidcutil "mxe.ericsson/depmanager/utils/oidc"
)

func argocdlogin(username string, password string, acdClient argocdclient.Client) (string, string, error) {
	sessConn, sessionIf, err := acdClient.NewSessionClient()
	if err != nil {
		return "", "", err
	}
	defer io.Close(sessConn)
	sessionRequest := sessionpkg.SessionCreateRequest{
		Username: username,
		Password: password,
	}
	createdSession, err := sessionIf.Create(context.Background(), &sessionRequest)
	if err != nil {
		return "", "", err
	}
	return createdSession.Token, "", nil
}

func argocdSSOLogin(username string, password string, ssoHost string) (string, string, error) {
	if ssoHost == "" {
		return "", "", errors.New("SSOHost value is not set")
	}
	oidcProvider, err := oidcutil.NewMxeOIDCProvider(ssoHost)
	if err != nil {
		return "", "", err
	}
	oidcProvider.SetCredentials(username, password)
	err = oidcProvider.GetToken()
	if err != nil {
		return "", "", err
	}
	return oidcProvider.Token.AccessToken, oidcProvider.Token.RefreshToken, nil
}

func (s *sessionService) CreateSession(ctx context.Context, sessionCreateReq *SessionCreateRequest) (string, string, error) {

	if sessionCreateReq.SsoMode {

		return argocdSSOLogin(sessionCreateReq.Username, sessionCreateReq.Password, sessionCreateReq.SsoHost)

	} else {
		acdClient, err := argocdclient.NewClient(s.Config.Store.ArgoCDClientOptions)
		if err != nil {
			return "", "", err
		}

		return argocdlogin(sessionCreateReq.Username, sessionCreateReq.Password, acdClient)
	}
}
