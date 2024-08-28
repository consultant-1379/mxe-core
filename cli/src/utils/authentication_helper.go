package utils

import (
	"bufio"
	"encoding/json"
	"errors"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"os"
	"strconv"
	"strings"

	"golang.org/x/term"
)

const clientId = "mxe-rest-client"
const mxeTokenFile = "mxeTokenFile"
const keycloakPath = "/auth/realms/"
const keycloakRealm = "mxe"
const keycloakTokenEndPoint = "/protocol/openid-connect/token"
const environmentMxeUser = "MXE_USER"
const environmentMxePassword = "MXE_PASSWORD"
const legalWarningMessagePath = "/legal/message.txt"

func GetOfflineToken() (*CreateTokenResponse, error) {
	mxeDir := MxeDir()
	if !directoryExists(mxeDir) {
		err := os.MkdirAll(mxeDir, 0700)
		if err != nil {
			LogDebug("Failed to create " + mxeDir)
			return nil, err
		}
	}

	fileName := fmt.Sprintf("%s%c%s.%s", mxeDir, os.PathSeparator, mxeTokenFile, GetSelectedCluster())

	if !fileExists(fileName) {
		return generateTokenFromParameters(fileName)
	}

	return readTokenFromFile(fileName)
}

func RefreshOfflineToken() (*CreateTokenResponse, error) {
	mxeDir := MxeDir()
	fileName := fmt.Sprintf("%s%c%s.%s", mxeDir, os.PathSeparator, mxeTokenFile, GetSelectedCluster())

	if !fileExists(fileName) {
		errorMessage := "Token file does not exist, client is not authenticated yet, token refresh failed"
		LogDebug(errorMessage)
		return nil, errors.New(errorMessage)
	}

	return generateTokenWithRefresh(fileName)
}

func directoryExists(dirName string) bool {
	return exist(dirName, true)
}

func fileExists(fileName string) bool {
	return exist(fileName, false)
}

func exist(fileName string, directory bool) bool {
	info, err := os.Stat(fileName)
	if os.IsNotExist(err) {
		return false
	}
	if directory {
		return info.IsDir()
	} else {
		return !info.IsDir()
	}
}

func generateTokenFromParameters(fileName string) (*CreateTokenResponse, error) {
	userPassword := getUserPassword()
	userName := userPassword.userName
	password := userPassword.password

	resp, genTokenError := createToken(createTokenRequest(userName, password))

	if genTokenError != nil {
		return nil, genTokenError
	}

	createTokenResp, err := getCreateTokenResponse(resp, fileName)

	if err == nil {
		writeLegalWarning()
		writeLastLoginMessage()
	}

	return createTokenResp, err

}

func generateTokenWithRefresh(fileName string) (*CreateTokenResponse, error) {
	tokenResponse, err := readTokenFromFile(fileName)
	if err != nil {
		return nil, err
	}

	resp, genTokenError := createToken(refreshTokenRequest(tokenResponse.RefreshToken, clientId))
	if genTokenError != nil {
		return nil, genTokenError
	}

	if resp.StatusCode == 400 {
		err := os.RemoveAll(fileName)
		if err != nil {
			LogDebug("Failed to delete " + fileName)
			return nil, err
		}
		return generateTokenFromParameters(fileName)
	}

	return getCreateTokenResponse(resp, fileName)
}

func getCreateTokenResponse(resp *http.Response, fileName string) (*CreateTokenResponse, error) {
	if resp.StatusCode != 200 {
		return nil, getAuthenticationError(resp)
	}

	var record CreateTokenResponse
	if err := json.NewDecoder(resp.Body).Decode(&record); err != nil {
		errorMessage := "Failed to decode response from server!"
		LogDebug(errorMessage)
		return nil, errors.New(errorMessage)
	}

	if err := writeTokenToFile(&record, fileName); err != nil {
		LogDebug(err.Error())
		return nil, err
	}

	return &record, nil
}

func writeTokenToFile(response *CreateTokenResponse, fileName string) error {
	prettyJson, err := json.MarshalIndent(response, "", "    ")
	if err != nil {
		return err
	}
	jsonFile, err := os.Create(fileName)
	if err != nil {
		return err
	}
	err = os.Chmod(fileName, 0600)
	if err != nil {
		return err
	}
	jsonFile.WriteString(string(prettyJson))
	defer jsonFile.Close()
	return nil
}

func getUserPassword() UserPassword {
	var userPassword UserPassword
	userNameFromEnvironment := os.Getenv(environmentMxeUser)
	passwordFromEnvironment := os.Getenv(environmentMxePassword)
	if userNameFromEnvironment != "" && passwordFromEnvironment != "" {
		LogDebug(environmentMxeUser + " and " + environmentMxePassword + " system variable is set and will be used during authentication")
		userPassword.userName = userNameFromEnvironment
		userPassword.password = passwordFromEnvironment
	} else {
		fmt.Print("Enter Username: ")
		reader := bufio.NewReader(os.Stdin)
		userName, _ := reader.ReadString('\n')
		userPassword.userName = strings.TrimSpace(userName)
		fmt.Print("Enter Password: ")
		bytePassword, _ := term.ReadPassword(int(os.Stdin.Fd()))
		userPassword.password = string(bytePassword)
	}
	return userPassword

}

func createTokenRequest(username string, password string) url.Values {
	data := url.Values{}
	data.Set("username", username)
	data.Set("password", password)
	data.Set("grant_type", "password")
	data.Set("client_id", clientId)
	data.Set("scope", "offline_access")
	return data
}

func refreshTokenRequest(refreshToken string, clientId string) url.Values {
	data := url.Values{}
	data.Set("grant_type", "refresh_token")
	data.Set("client_id", clientId)
	data.Set("scope", "offline_access")
	data.Set("refresh_token", refreshToken)
	return data
}

func createToken(data url.Values) (*http.Response, error) {
	urlStr := API_URL() + keycloakPath + keycloakRealm + keycloakTokenEndPoint
	client := &http.Client{}
	r, _ := http.NewRequest("POST", urlStr, strings.NewReader(data.Encode()))
	r.Header.Add("Content-Type", "application/x-www-form-urlencoded")
	r.Header.Add("Content-Length", strconv.Itoa(len(data.Encode())))

	resp, err := client.Do(r)
	if err != nil {
		return nil, err
	}
	return resp, nil
}

func getAuthenticationError(resp *http.Response) error {
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

	LogDebug(fmt.Sprintf("%s %s", resp.Status, errorMessage))
	return errors.New(errorMessage)
}

func readTokenFromFile(fileName string) (*CreateTokenResponse, error) {
	jsonFile, err := os.Open(fileName)
	if err != nil {
		return nil, err
	}
	defer jsonFile.Close()
	byteValue, _ := ioutil.ReadAll(jsonFile)
	var response *CreateTokenResponse
	json.Unmarshal(byteValue, &response)

	return response, nil
}

type UserPassword struct {
	userName string
	password string
}
