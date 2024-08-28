package utils

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"strings"
	"time"
)

const userInfoEndPoint = "/protocol/openid-connect/userinfo"
const lineWidth = 80

func writeLastLoginMessage() {
	var login = getLastLogin()
	if login != 0 {
		fmt.Printf("\nLast login: %s\n", time.Unix(login, 0))
	}
	fmt.Print("\n")
}

func getLastLogin() int64 {

	urlStr := API_URL() + keycloakPath + keycloakRealm + userInfoEndPoint
	resp, err := SendHttpRequest("GET", urlStr)
	if err != nil {
		LogError(err.Error())
		return 0
	}
	defer resp.Body.Close()

	if !HandleErrorResponse(resp, selectedCluster) {
		return 0
	}

	var userInfo UserInfoResponse
	if err := json.NewDecoder(resp.Body).Decode(&userInfo); err != nil {
		LogDebug("Failed to decode response from server!")
		return 0
	}

	return userInfo.PrevAuthTime / 1000
}

func writeLegalWarning() {
	var legal = getLegalWarning()
	if legal != "" {
		prettyPrintLegal(legal)
	}
}

func getLegalWarning() string {
	resp, err := SendHttpRequest("GET", API_URL() + legalWarningMessagePath)
	if err != nil {
		LogError(err.Error())
		return ""
	}

	defer resp.Body.Close()

	if IsStatusCodeSuccess(resp.StatusCode) {
		if body, err := ioutil.ReadAll(resp.Body); err == nil {
			return string(body)
		}
	}
	return ""
}

func prettyPrintLegal(legal string) {
	rows := strings.Split(legal, "\n")

	for _, row := range rows {
		if len(row) > 0 {
			prettyPrintLegalRow(row, )
		}
	}
	fmt.Print("\n")
}

func prettyPrintLegalRow(row string) {
	words := strings.Fields(row)
	SpaceLeft := lineWidth - len(words[0])
	fmt.Print("\n", words[0])

	for _, word := range words[1:] {
		if len(word) + 1 > SpaceLeft {
			SpaceLeft = lineWidth - len(word)
			fmt.Print("\n", word)
		} else {
			SpaceLeft = SpaceLeft - (len(word) + 1)
			fmt.Print(" ", word)
		}
	}
}
