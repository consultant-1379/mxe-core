package oidc

import (
	"encoding/json"
	"errors"

	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"strconv"
	"strings"

	log "github.com/sirupsen/logrus"
)

const (
	KEYCLOAK_CLIENT_ID      = "argocd-rest-client"
	KEYCLOAK_PATH           = "/auth/realms/"
	KEYCLOAK_REALM          = "argocd"
	KEYCLOAK_TOKEN_ENDPOINT = "/protocol/openid-connect/token"
)

type MxeOIDCProvider struct {
	MxeHost       string
	Username      string
	Password      string
	ClientID      string
	TokenEndpoint string
	Token         *AuthToken
}

func NewMxeOIDCProvider(mxeHost string) (*MxeOIDCProvider, error) {
	_, err := url.Parse(mxeHost)
	if err != nil {
		return nil, err
	}
	p := MxeOIDCProvider{MxeHost: mxeHost}
	p.initialise()
	return &p, nil
}
func (p *MxeOIDCProvider) SetCredentials(username, password string) {
	p.Username = username
	p.Password = password
}

func (p *MxeOIDCProvider) initialise() {
	p.ClientID = KEYCLOAK_CLIENT_ID
	p.TokenEndpoint = p.GetTokenIssuerEp()
}

func (p *MxeOIDCProvider) GetTokenIssuerEp() string {
	if p.MxeHost == "" {
		return ""
	}
	return fmt.Sprintf("%s%s%s%s", p.MxeHost, KEYCLOAK_PATH, KEYCLOAK_REALM, KEYCLOAK_TOKEN_ENDPOINT)
}

type AuthToken struct {
	AccessToken      string `json:"access_token"`
	ExpiresIn        int    `json:"expires_in"`
	RefreshExpiresIn int    `json:"refresh_expires_in"`
	RefreshToken     string `json:"refresh_token"`
	TokenType        string `json:"token_type"`
	NotBeforePolicy  int    `json:"not-before-policy"`
	SessionState     string `json:"session_state"`
	Scope            string `json:"scope"`
}

func (p *MxeOIDCProvider) createTokenRequest() url.Values {
	urlValues := url.Values{}
	urlValues.Set("username", p.Username)
	urlValues.Set("password", p.Password)
	urlValues.Set("grant_type", "password")
	urlValues.Set("client_id", p.ClientID)
	urlValues.Set("scope", "offline_access")
	return urlValues
}

func (p *MxeOIDCProvider) createToken(data url.Values) (*http.Response, error) {
	client := &http.Client{}
	r, _ := http.NewRequest("POST", p.TokenEndpoint, strings.NewReader(data.Encode()))
	r.Header.Add("Content-Type", "application/x-www-form-urlencoded")
	r.Header.Add("Content-Length", strconv.Itoa(len(data.Encode())))

	resp, err := client.Do(r)
	if err != nil {
		return nil, err
	}
	return resp, nil
}

func (p *MxeOIDCProvider) getAuthenticationError(resp *http.Response) error {
	type CommandResult struct {
		Error            string `json:"error"`
		ErrorDescription string `json:"error_description"`
	}

	var result CommandResult
	errorMessage := "Authentication failure: "
	body, _ := ioutil.ReadAll(resp.Body)
	err := json.Unmarshal(body, &result)
	if err != nil {
		errorMessage = errorMessage + resp.Status
	} else {
		errorMessage = errorMessage + result.ErrorDescription
	}

	log.Debug(fmt.Sprintf("%s %s", resp.Status, errorMessage))
	return errors.New(errorMessage)
}

func (p *MxeOIDCProvider) responseToToken(response *http.Response) {
	var authToken AuthToken
	if err := json.NewDecoder(response.Body).Decode(&authToken); err != nil {
		errorMessage := "failed to decode response from server"
		log.Fatal(errorMessage)
	}
	p.Token = &authToken
}

func (p *MxeOIDCProvider) GetToken() error {
	urlValues := p.createTokenRequest()
	response, err := p.createToken(urlValues)
	if err != nil {
		log.Error("Unable to retrieve token from %s due to error: %#v", p.TokenEndpoint, err)
		return err
	}
	if response.StatusCode != http.StatusOK {
		return p.getAuthenticationError(response)
	}
	p.responseToToken(response)
	return nil
}

func (p *MxeOIDCProvider) refreshTokenRequest(refreshToken string) url.Values {
	data := url.Values{}
	data.Set("grant_type", "refresh_token")
	data.Set("client_id", p.ClientID)
	data.Set("scope", "offline_access")
	data.Set("refresh_token", refreshToken)
	return data
}

func (p *MxeOIDCProvider) RefreshToken(refreshToken string) {
	urlValues := p.refreshTokenRequest(refreshToken)
	response, err := p.createToken(urlValues)
	if err != nil {
		log.Fatalf("Unable to retrieve refresh token from %s due to error: %#v", p.TokenEndpoint, err)
	}
	if response.StatusCode != http.StatusOK {
		log.Fatal(p.getAuthenticationError(response))
	}
	p.responseToToken(response)
}
