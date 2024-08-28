package client

import (
	"crypto/tls"
	"encoding/base64"
	"fmt"
	"io/ioutil"
	"net"
	"net/http"
	"os"
	"strings"
	"sync"
	"time"

	log "github.com/sirupsen/logrus"

	argocdclient "github.com/argoproj/argo-cd/v2/pkg/apiclient"
	tls_util "github.com/argoproj/argo-cd/v2/util/tls"
	kitlog "github.com/go-kit/kit/log"
	"github.com/golang-jwt/jwt/v4"

	httptransport "github.com/go-kit/kit/transport/http"
	"github.com/pkg/errors"
	"mxe.ericsson/depmanager/dmserver/client/config"
	"mxe.ericsson/depmanager/dmserver/pkg/dmserver/deploy"
	"mxe.ericsson/depmanager/dmserver/pkg/dmserver/session"
	"mxe.ericsson/depmanager/utils/common"
	httputil "mxe.ericsson/depmanager/utils/http"
	"mxe.ericsson/depmanager/utils/oidc"
)

const (
	MetaDataTokenKey = "token"
	// EnvArgoCDServer is the environment variable to look for an Argo CD server address
	EnvArgoCDServer = "ARGOCD_SERVER"
	// EnvArgoCDAuthToken is the environment variable to look for an Argo CD auth token
	EnvArgoCDAuthToken = "ARGOCD_AUTH_TOKEN"
	// EnvArgoCDgRPCMaxSizeMB is the environment variable to look for a max gRPC message size
	EnvArgoCDgRPCMaxSizeMB = "ARGOCD_GRPC_MAX_SIZE_MB"
)

type Client interface {
	GetProperty(string) (string, error)
	ClientOptions() argocdclient.ClientOptions
	HTTPClient(int) (*http.Client, error)
	SessionClient() (*session.Endpoints, error)
	PackageClient() (*deploy.Endpoints, error)
	// TODO: Ask aswin about client cluster ep
	// ClusterClient() (*cluster.Endpoints, error)
}

type client struct {
	ServerAddr      string
	PlainText       bool
	Insecure        bool
	CertPEMData     []byte
	ClientCert      *tls.Certificate
	AuthToken       string
	RefreshToken    string
	UserAgent       string
	GRPCWeb         bool
	GRPCWebRootPath string
	Headers         []string
	Log             kitlog.Logger

	proxyMutex *sync.Mutex
	MxeHost    string
}

// NewLoginClientOrDie creates a new minimal API client from a set of login options, or fails fatally if the new client creation fails.
func NewLoginClientOrDie(opts *argocdclient.ClientOptions, ssoHost string) Client {
	client, err := NewLoginClient(opts, ssoHost)
	if err != nil {
		log.Fatal(err)
	}
	return client
}

// NewLoginClient provides a minimal client for logging into DMServer without using localconfig file
func NewLoginClient(opts *argocdclient.ClientOptions, ssoHost string) (Client, error) {
	var c client
	c.proxyMutex = &sync.Mutex{}
	// Override server address if specified in env or CLI flag
	if serverFromEnv := os.Getenv(EnvArgoCDServer); serverFromEnv != "" {
		c.ServerAddr = serverFromEnv
	}
	if opts.ServerAddr != "" {
		c.ServerAddr = opts.ServerAddr
	}
	// Make sure we got the server address and auth token from somewhere
	if c.ServerAddr == "" {
		return nil, errors.New("Deployer service address unspecified")
	}
	if parts := strings.Split(c.ServerAddr, ":"); len(parts) == 1 {
		// If port is unspecified, assume the most likely port
		c.ServerAddr += ":443"
	}
	if strings.TrimSpace(ssoHost) != "" {
		c.MxeHost = ssoHost
	}
	return &c, nil
}

// NewClientOrDie creates a new API client from a set of config options, or fails fatally if the new client creation fails.
func NewClientOrDie(opts *argocdclient.ClientOptions) Client {
	client, err := NewClient(opts)
	if err != nil {
		log.Fatal(err)
	}
	return client
}

// NewClient creates a new API client from a set of config options.
func NewClient(opts *argocdclient.ClientOptions) (Client, error) {
	var c client
	localCfg, err := config.ReadLocalConfig(opts.ConfigPath)
	if err != nil {
		return nil, err
	}
	c.proxyMutex = &sync.Mutex{}
	var ctxName string
	if localCfg != nil {
		configCtx, err := localCfg.ResolveContext(opts.Context)
		if err != nil {
			return nil, err
		}
		if configCtx != nil {
			c.ServerAddr = configCtx.Server.Server
			if configCtx.Server.CACertificateAuthorityData != "" {
				c.CertPEMData, err = base64.StdEncoding.DecodeString(configCtx.Server.CACertificateAuthorityData)
				if err != nil {
					return nil, err
				}
			}
			if configCtx.Server.ClientCertificateData != "" && configCtx.Server.ClientCertificateKeyData != "" {
				clientCertData, err := base64.StdEncoding.DecodeString(configCtx.Server.ClientCertificateData)
				if err != nil {
					return nil, err
				}
				clientCertKeyData, err := base64.StdEncoding.DecodeString(configCtx.Server.ClientCertificateKeyData)
				if err != nil {
					return nil, err
				}
				clientCert, err := tls.X509KeyPair(clientCertData, clientCertKeyData)
				if err != nil {
					return nil, err
				}
				c.ClientCert = &clientCert
			} else if configCtx.Server.ClientCertificateData != "" || configCtx.Server.ClientCertificateKeyData != "" {
				return nil, errors.New("ClientCertificateData and ClientCertificateKeyData must always be specified together")
			}
			c.PlainText = configCtx.Server.PlainText
			c.Insecure = configCtx.Server.Insecure
			c.GRPCWeb = configCtx.Server.GRPCWeb
			c.GRPCWebRootPath = configCtx.Server.GRPCWebRootPath
			c.AuthToken = configCtx.User.AuthToken
			c.RefreshToken = configCtx.User.RefreshToken
			c.MxeHost = configCtx.MxeHost
			ctxName = configCtx.Name
		}
	}
	if opts.UserAgent != "" {
		c.UserAgent = opts.UserAgent
	} else {
		c.UserAgent = fmt.Sprintf("%s/%s", common.ArgoCDUserAgentName, common.GetVersion().Version)
	}
	// Override server address if specified in env or CLI flag
	if serverFromEnv := os.Getenv(EnvArgoCDServer); serverFromEnv != "" {
		c.ServerAddr = serverFromEnv
	}
	if opts.ServerAddr != "" {
		c.ServerAddr = opts.ServerAddr
	}
	// Make sure we got the server address and auth token from somewhere
	if c.ServerAddr == "" {
		return nil, errors.New("Deployer service address unspecified")
	}
	if parts := strings.Split(c.ServerAddr, ":"); len(parts) == 1 {
		// If port is unspecified, assume the most likely port
		c.ServerAddr += ":443"
	}
	// Override auth-token if specified in env variable or CLI flag
	if authFromEnv := os.Getenv(EnvArgoCDAuthToken); authFromEnv != "" {
		c.AuthToken = authFromEnv
	}
	if opts.AuthToken != "" {
		c.AuthToken = opts.AuthToken
	}
	// Override certificate data if specified from CLI flag
	if opts.CertFile != "" {
		b, err := ioutil.ReadFile(opts.CertFile)
		if err != nil {
			return nil, err
		}
		c.CertPEMData = b
	}
	// Override client certificate data if specified from CLI flag
	if opts.ClientCertFile != "" && opts.ClientCertKeyFile != "" {
		clientCert, err := tls.LoadX509KeyPair(opts.ClientCertFile, opts.ClientCertKeyFile)
		if err != nil {
			return nil, err
		}
		c.ClientCert = &clientCert
	} else if opts.ClientCertFile != "" || opts.ClientCertKeyFile != "" {
		return nil, errors.New("--client-crt and --client-crt-key must always be specified together")
	}
	// Override insecure/plaintext options if specified from CLI
	if opts.PlainText {
		c.PlainText = true
	}
	if opts.Insecure {
		c.Insecure = true
	}
	if opts.GRPCWeb {
		c.GRPCWeb = true
	}
	if opts.GRPCWebRootPath != "" {
		c.GRPCWebRootPath = opts.GRPCWebRootPath
	}
	if localCfg != nil {
		err = c.refreshAuthToken(localCfg, ctxName, opts.ConfigPath)
		if err != nil {
			return nil, err
		}
	}
	c.Headers = opts.Headers

	return &c, nil
}

// HTTPClient returns a HTTPClient appropriate for performing OAuth, based on TLS settings
func (c *client) HTTPClient(timeout int) (*http.Client, error) {
	tlsConfig, err := c.tlsConfig()
	if err != nil {
		return nil, err
	}
	return &http.Client{
		Transport: &http.Transport{
			TLSClientConfig: tlsConfig,
			Proxy:           http.ProxyFromEnvironment,
			DialContext: (&net.Dialer{
				Timeout:   time.Duration(timeout) * time.Second,
				KeepAlive: 30 * time.Second,
			}).DialContext,
			TLSHandshakeTimeout:   10 * time.Second,
			ExpectContinueTimeout: 1 * time.Second,
		},
	}, nil
}

func (c *client) GetProperty(propertyName string) (string, error) {
	switch propertyName {
	case "serverAddr":
		return c.ServerAddr, nil
	case "mxeHost":
		return c.MxeHost, nil
	default:

		return "", fmt.Errorf("%s cannot be retrieved from deployer client", propertyName)
	}
}

func (c *client) checkTokenValidity(claims jwt.StandardClaims) bool {
	claimMinValidity := time.Now().Add(time.Second * time.Duration(config.TOKEN_ADDITIONAL_VALIDITY_SEC))
	expiresAt := time.Unix(claims.ExpiresAt, 0)
	return expiresAt.Sub(claimMinValidity) >= 0
}

// refreshAuthToken refreshes a JWT auth token if it is invalid (e.g. expired)
func (c *client) refreshAuthToken(localCfg *config.LocalConfig, ctxName, configPath string) error {
	if c.RefreshToken == "" {
		// If we have no refresh token, there's no point in doing anything
		return nil
	}
	configCtx, err := localCfg.ResolveContext(ctxName)
	if err != nil {
		return err
	}

	parser := jwt.NewParser(jwt.WithoutClaimsValidation())
	var claims jwt.StandardClaims
	_, _, err = parser.ParseUnverified(configCtx.User.AuthToken, &claims)
	if err != nil {
		return err
	}
	if claims.Valid() == nil && c.checkTokenValidity(claims) {
		// token is still valid
		return nil
	}
	log.Debug("Auth token no longer valid. Refreshing")
	if c.MxeHost == "" {
		log.Fatal("MXE host is not set. Cannot refresh token")
	}
	mxeOIDCProvider, err := oidc.NewMxeOIDCProvider(c.MxeHost)
	if err != nil {
		log.Fatal("Failed to parse url ", c.MxeHost)
	}
	mxeOIDCProvider.RefreshToken(c.RefreshToken)

	c.AuthToken = mxeOIDCProvider.Token.AccessToken
	c.RefreshToken = mxeOIDCProvider.Token.RefreshToken
	localCfg.UpsertUser(config.User{
		Name:         ctxName,
		AuthToken:    c.AuthToken,
		RefreshToken: c.RefreshToken,
	})
	err = config.WriteLocalConfig(*localCfg, configPath)
	if err != nil {
		return err
	}
	return nil
}

func (c *client) tlsConfig() (*tls.Config, error) {
	var tlsConfig tls.Config
	if len(c.CertPEMData) > 0 {
		cp := tls_util.BestEffortSystemCertPool()
		if !cp.AppendCertsFromPEM(c.CertPEMData) {
			return nil, fmt.Errorf("credentials: failed to append certificates")
		}
		tlsConfig.RootCAs = cp
	}
	if c.ClientCert != nil {
		tlsConfig.Certificates = append(tlsConfig.Certificates, *c.ClientCert)
	}
	if c.Insecure {
		tlsConfig.InsecureSkipVerify = true
	}
	return &tlsConfig, nil
}

func (c *client) ClientOptions() argocdclient.ClientOptions {
	return argocdclient.ClientOptions{
		ServerAddr: c.ServerAddr,
		PlainText:  c.PlainText,
		Insecure:   c.Insecure,
		AuthToken:  c.AuthToken,
	}
}

func (c *client) getDefaultHeaders() map[string]string {
	return map[string]string{
		"Authorization": fmt.Sprintf("Bearer %s", c.AuthToken),
	}
}

func (c *client) PackageClient() (*deploy.Endpoints, error) {
	httpClient, err := c.HTTPClient(60)
	if err != nil {
		return nil, err
	}
	return deploy.MakeClientEndpoints(c.ServerAddr, c.Log, httputil.NewClient(c.getDefaultHeaders(), httpClient)...)
}

func (c *client) SessionClient() (*session.Endpoints, error) {
	httpClient, err := c.HTTPClient(30)
	if err != nil {
		return nil, err
	}
	return session.MakeClientEndpoints(c.ServerAddr, c.Log, httptransport.SetClient(httpClient))
}

// TODO: CHeck with aswin
/*
func (c *client) ClusterClient() (*cluster.Endpoints, error) {
	httpClient, err := c.HTTPClient()
	if err != nil {
		return nil, err
	}
	return cluster.MakeClientEndpoints(c.ServerAddr, c.Log, httputil.NewClient(c.AuthToken, httpClient)...)
}
*/
