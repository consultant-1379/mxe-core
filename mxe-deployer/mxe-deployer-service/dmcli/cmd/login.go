package cmd

import (
	"context"
	"errors"
	"fmt"

	log "github.com/sirupsen/logrus"

	argocdclient "github.com/argoproj/argo-cd/v2/pkg/apiclient"
	argocdCliUtils "github.com/argoproj/argo-cd/v2/util/cli"
	argocderrors "github.com/argoproj/argo-cd/v2/util/errors"
	"github.com/spf13/cobra"
	dmclientpkg "mxe.ericsson/depmanager/dmserver/client"
	dmclientconfig "mxe.ericsson/depmanager/dmserver/client/config"
	"mxe.ericsson/depmanager/dmserver/pkg/dmserver/session"
	oidcutil "mxe.ericsson/depmanager/utils/oidc"
)

func userLogin(username, password, ssoHost string, ssoMode bool, globalClientOpts *argocdclient.ClientOptions) (string, string, error) {
	if username == "" {
		log.Fatalln("username is not provided for login")
	}
	if password == "" {
		log.Fatalln("password is not provided for login")
	}

	dmClient := dmclientpkg.NewLoginClientOrDie(globalClientOpts, ssoHost)

	sessionEps, err := dmClient.SessionClient()
	if err != nil {
		log.Fatalf("Unable to create session endpoints due to error: %#v", err)
	}

	sessionCreateReq := session.SessionCreateRequest{
		Username: username,
		Password: password,
	}

	if ssoHost != "" {
		if ssoMode {
			mxeOIDCProvider, err := oidcutil.NewMxeOIDCProvider(ssoHost)
			if err != nil {
				log.Fatalf("Unable to create oidc provider using host %s due to error: %#v", ssoHost, err)
			}
			sessionCreateReq.SsoHost = mxeOIDCProvider.MxeHost
			sessionCreateReq.SsoMode = true
		} else {
			return "", "", errors.New("received value for ssoHost but ssoMode flag is not enabled")
		}

	} else {
		log.Warnln("Received argocd local user login request. It is recommended to configure Keycloak SSO to login.")
	}

	return sessionEps.CreateSession(context.Background(), sessionCreateReq)
}

// NewLoginCommand returns a new instance of `argocd login` command
func NewLoginCommand(globalClientOpts *argocdclient.ClientOptions) *cobra.Command {
	var (
		ctxName  string
		username string
		password string
		ssoHost  string
		ssoMode  bool
	)
	var command = &cobra.Command{
		Use:   "login SERVER",
		Short: "Log in to eric-mxe-deployer-service",
		Long:  "Log in to eric-mxe-deployer-service",
		Example: fmt.Sprintf(`
		
		# Non-interactive Login using SSO - with server url, mxeApiHost URL, user credentials from keycloak
		%s login http(s)://eric-mxe-deployer-service-url --ssoMode --ssoHost http(s)://mxe-host-url --username $USERNAME --password $PASSWORD

		# Interactive Login using SSO - with server url, mxeApiHost URL
		%s login http(s)://eric-mxe-deployer-service-url --ssoMode --ssoHost http(s)://mxe-host-url

		# Non-interactive Admin user Login with server url and credentials when SSO mode is disabled - DEPRECATED
		%s login http(s)://eric-mxe-deployer-service-url --username $USERNAME --password $PASSWORD

		# Interactive Admin user login with server url when SSO mode is disabled - DEPRECATED
		%s login http(s)://eric-mxe-deployer-service-url

		`, cliName, cliName, cliName, cliName),
		Run: func(c *cobra.Command, args []string) {
			var tokenString, refreshToken string
			var err error

			if len(args) == 1 {
				globalClientOpts.ServerAddr = args[0]
			} else {
				log.Fatal("server address is not supplied")
			}

			username, password = argocdCliUtils.PromptCredentials(username, password)
			tokenString, refreshToken, err = userLogin(username, password, ssoHost, ssoMode, globalClientOpts)
			if err != nil {
				log.Fatalf("Login failed with error : %s", err.Error())
			}

			// login successful. Persist the config
			localCfg, err := dmclientconfig.ReadLocalConfig(globalClientOpts.ConfigPath)
			argocderrors.CheckError(err)
			if localCfg == nil {
				localCfg = &dmclientconfig.LocalConfig{}
			}
			if ctxName == "" {
				ctxName = globalClientOpts.ServerAddr
			}
			localCfg.UpsertServer(dmclientconfig.Server{
				Server:          globalClientOpts.ServerAddr,
				PlainText:       globalClientOpts.PlainText,
				Insecure:        globalClientOpts.Insecure,
				GRPCWeb:         globalClientOpts.GRPCWeb,
				GRPCWebRootPath: globalClientOpts.GRPCWebRootPath,
			})
			localCfg.UpsertUser(dmclientconfig.User{
				Name:         ctxName,
				AuthToken:    tokenString,
				RefreshToken: refreshToken,
			})
			localCfg.CurrentContext = ctxName
			localCfg.UpsertContext(dmclientconfig.ContextRef{
				Name:    ctxName,
				User:    ctxName,
				Server:  globalClientOpts.ServerAddr,
				MxeHost: ssoHost,
			})
			err = dmclientconfig.WriteLocalConfig(*localCfg, globalClientOpts.ConfigPath)
			argocderrors.CheckError(err)
			fmt.Printf("Authentication successful\n")
		},
	}
	//DefaultSSOLocalPort := 8805
	command.Flags().StringVar(&ctxName, "name", "", "name to use for the context")
	command.Flags().StringVar(&username, "username", "", "the username of an account to authenticate")
	command.Flags().StringVar(&password, "password", "", "the password of an account to authenticate")
	command.Flags().BoolVar(&ssoMode, "ssoMode", false, "If ssoMode is enabled set MXE Api Host URL for SSO login")
	command.Flags().StringVar(&ssoHost, "ssoHost", "", "If ssoMode is enabled set MXE Api Host URL for SSO login")
	return command
}
