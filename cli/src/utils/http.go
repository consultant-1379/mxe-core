package utils

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
	"strings"
)

func API_VERSION() string {
	return "v1"
}

func API_VERSION_v2() string {
	return "v2"
}

func CreateApiUrl(apiRoute string) string {
	return fmt.Sprintf("%s/%s/%s", API_URL(), API_VERSION(), apiRoute)
}

func CreateApiUrlv2(apiRoute string) string {
	return fmt.Sprintf("%s/%s/%s", API_URL(), API_VERSION_v2(), apiRoute)
}

func IsStatusCodeSuccess(statusCode int) bool {
	return statusCode >= 200 && statusCode <= 299
}

func IsStatusCodeServerError(statusCode int) bool {
	return statusCode >= 500 && statusCode <= 599
}

func HandleResponse(resp *http.Response, cluster string) bool {
	return handleResponse(resp, cluster, true)
}

func HandleErrorResponse(resp *http.Response, cluster string) bool {
	return handleResponse(resp, cluster, false)
}

func handleResponse(resp *http.Response, cluster string, logSuccess bool) bool {
	if IsStatusCodeSuccess(resp.StatusCode) && !logSuccess {
		return true
	}
	if body, err := ioutil.ReadAll(resp.Body); err != nil {
		LogError(fmt.Sprintf("Body reader error: %s.", err.Error()))
		return false
	} else {
		if string(body) == "" {
			LogError(fmt.Sprintf("Server responded with status code: %d.", resp.StatusCode))
			return false
		} else {
			type CommandResult struct {
				Message        string `json:"message"`
				AdditionalInfo string `json:"additionalInfo"`
			}

			var result CommandResult
			err = json.Unmarshal(body, &result)

			if IsStatusCodeSuccess(resp.StatusCode) {
				if err != nil {
					LogSuccess(addClusterToMessage(fmt.Sprintf("%s", body), cluster))
				} else {
					LogSuccess(addClusterToMessage(fmt.Sprintf("%s", result.Message), cluster))
					if result.AdditionalInfo != "" {
						LogInfo(result.AdditionalInfo)
					}
				}
				return true
			} else {
				if err != nil {
					if IsStatusCodeServerError(resp.StatusCode) {
						fmt.Println("MXE Service Error: " + resp.Status)
					} else {
						LogError("Unknown Error Occurred.")
						LogDebug(resp.Status)
					}
					LogDebug(addClusterToMessage(fmt.Sprintf("%s", body), cluster))
				} else {
					LogError(addClusterToMessage(fmt.Sprintf("%s", result.Message), cluster))
				}
				return false
			}
		}
	}
}

func ProbeTokenValidity() error {
	_, err := SendHttpRequest("HEAD", fmt.Sprintf("%s/", API_URL()))
	return err
}

func SendHttpRequest(method string, url string) (*http.Response, error) {
	return SendHttpRequestWithBody(method, url, "", nil)
}

func SendHttpRequestWithBody(method string, url string, contentType string, body io.Reader) (*http.Response, error) {
	var bodyBytes []byte
	if body != nil {
		bodyBytes, _ = ioutil.ReadAll(body)
	}

	resp, err := sendHttpRequestWithBody(method, url, contentType, bodyBytes, false)
	if err != nil {
		return nil, err
	}

	/*
		Issue	: mxe-cli is getting error after auth token expiration (after upgrade to louketo-proxy 1.0.0)
		Reason	: The Response code for authorization failure is HTTP 303 ( previouly it was HTTP 307)
		Fix		: Adding the '303' error code for token refresh
	*/
	if resp.StatusCode == 307 || resp.StatusCode == 303 {
		LogDebug("Authentication token is invalid, try to refresh token")
		return sendHttpRequestWithBody(method, url, contentType, bodyBytes, true)
	}

	return resp, nil
}

// SendLargeHttpRequest is used to send large file to MXE without using memory buffers or doing retries
// SendHttpRequestWithBody runs out of memory when sending large files, hence this alternative
func SendLargeHttpRequest(method string, url string, contentType string, body io.Reader) (*http.Response, error) {
	req, err := createHttpRequest(method, url, contentType, body)
	if err != nil {
		LogDebug(err.Error())
		return nil, err
	}

	err = addTokenToHttpHeader(req, false)
	if err != nil {
		LogDebug(err.Error())
		return nil, err
	}

	client := &http.Client{
		CheckRedirect: func(req *http.Request, via []*http.Request) error {
			return http.ErrUseLastResponse
		},
	}

	resp, err := client.Do(req)
	if err != nil {
		LogDebug(err.Error())
		return nil, errors.New("No connection to MXE cluster")
	}

	return resp, nil
}

func sendHttpRequestWithBody(method string, url string, contentType string, bodyBytes []byte, refreshToken bool) (*http.Response, error) {
	req, err := createHttpRequest(method, url, contentType, bytes.NewBuffer(bodyBytes))
	if err != nil {
		LogDebug(err.Error())
		return nil, err
	}

	err = addTokenToHttpHeader(req, refreshToken)
	if err != nil {
		LogDebug(err.Error())
		return nil, err
	}

	client := &http.Client{
		CheckRedirect: func(req *http.Request, via []*http.Request) error {
			return http.ErrUseLastResponse
		},
	}

	resp, err := client.Do(req)
	if err != nil {
		LogDebug(err.Error())
		return nil, errors.New("No connection to MXE cluster.")
	}

	return resp, nil
}

func createHttpRequest(method string, url string, contentType string, body io.Reader) (*http.Request, error) {
	req, err := http.NewRequest(method, url, body)

	if err != nil {
		LogDebug(err.Error())
		return nil, errors.New("Failed to create a new HTTP request.")

	}
	if body != nil && len(contentType) > 0 {
		req.Header.Set("Content-Type", contentType)
	}

	return req, nil
}

func addTokenToHttpHeader(req *http.Request, refreshToken bool) error {
	var tokenResponse *CreateTokenResponse
	var err error
	if refreshToken {
		tokenResponse, err = RefreshOfflineToken()
	} else {
		tokenResponse, err = GetOfflineToken()
	}
	if err != nil {
		return err
	} else if len(tokenResponse.AccessToken) > 0 {
		LogDebug("Token found in file, will be used in header")
		req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", tokenResponse.AccessToken))
	}
	return nil
}

func addClusterToMessage(message string, cluster string) string {
	return strings.Replace(message, " cluster", " cluster \""+cluster+"\"", -1)
}
