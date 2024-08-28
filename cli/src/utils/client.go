package utils

/*
*
Common
*/
type ModelDetails struct {
	Id      string   `json:"id"`
	Version string   `json:"version"`
	Weight  *float64 `json:"weight"`
}

type AutoscalingData struct {
	MinReplicas int                 `json:"minReplicas"`
	MaxReplicas int                 `json:"maxReplicas"`
	Metrics     []AutoscalingMetric `json:"metrics"`
}

type AutoscalingMetric struct {
	AutoscalingMetricName string `json:"name"`
	TargetAverageValue    int    `json:"targetAverageValue"`
}

/*
*
Requests
*/
type DataRequest struct {
	Data interface{} `json:"data"`
}

type StartModelRequest struct {
	Name        string           `json:"name"`
	Type        string           `json:"type"`
	Models      []ModelDetails   `json:"models"`
	Replicas    *int             `json:"replicas"`
	AutoScaling *AutoscalingData `json:"autoScaling"`
	Domain      *string          `json:"domain"`
}

type PatchServiceRequest struct {
	Models      []ModelDetails   `json:"models"`
	Type        string           `json:"type"`
	Replicas    *int             `json:"replicas"`
	AutoScaling *AutoscalingData `json:"autoScaling"`
}

type OnboardModelRequest struct {
	Id                       string `json:"id"`
	Version                  string `json:"version"`
	Title                    string `json:"title"`
	Author                   string `json:"author"`
	Description              string `json:"description"`
	Image                    string `json:"image"`
	DockerRegistrySecretName string `json:"dockerRegistrySecretName"`
}

type DeleteModelRequest struct {
	Name string `json:"name"`
}

/*
*
Responses
*/
type ListStartedModelResponse struct {
	Models      []ModelDetails   `json:"models"`
	Created     string           `json:"created"`
	Replicas    int              `json:"replicas"`
	Name        string           `json:"name"`
	Type        string           `json:"type"`
	Status      string           `json:"status"`
	User        string           `json:"createdByUserName"`
	AutoScaling *AutoscalingData `json:"autoScaling"`
}

type ListOnboardedModelResponse struct {
	Id           string `json:"id"`
	Title        string `json:"title"`
	Author       string `json:"author"`
	SignedByName string `json:"signedByName"`
	Description  string `json:"description"`
	Version      string `json:"version"`
	Image        string `json:"image"`
	Created      string `json:"created"`
	Icon         string `json:"icon"`
	Status       string `json:"status"`
	Message      string `json:"message"`
	ErrorLog     string `json:"errorLog"`
	User         string `json:"createdByUserName"`
}

type CreateTokenResponse struct {
	AccessToken      string `json:"access_token"`
	ExpiresIn        int    `json:"expires_in"`
	RefreshExpiresIn int    `json:"refresh_expires_in"`
	RefreshToken     string `json:"refresh_token"`
	TokenType        string `json:"token_type"`
	NotBeforePolicy  int    `json:"not-before-policy"`
	SessionState     string `json:"session_state"`
	Scope            string `json:"scope"`
}

type UserInfoResponse struct {
	Sub               string `json:"sub"`
	EmailVerified     bool   `json:"email_verified"`
	PreferredUsername string `json:"preferred_username"`
	GivenName         string `json:"given_name"`
	FamilyName        string `json:"family_name"`
	PrevAuthTime      int64  `json:"prev_auth_time"`
}

type StartTrainingJobRequest struct {
	PackageId      string `json:"packageId"`
	PackageVersion string `json:"packageVersion"`
}

type ListTrainingPackagesResponse struct {
	Id          string `json:"id"`
	Version     string `json:"version"`
	Title       string `json:"title"`
	Author      string `json:"author"`
	Description string `json:"description"`
	Image       string `json:"image"`
	Created     string `json:"created"`
	Icon        string `json:"icon"`
	Status      string `json:"status"`
	Message     string `json:"message"`
	ErrorLog    string `json:"errorLog"`
	Internal    bool   `json:"internal"`
	User        string `json:"createdByUserName"`
}

type ListTrainingJobsResponse struct {
	Id             string `json:"id"`
	PackageId      string `json:"packageId"`
	PackageVersion string `json:"packageVersion"`
	Created        string `json:"created"`
	Status         string `json:"status"`
	Message        string `json:"message"`
	ErrorLog       string `json:"errorLog"`
	Completed      string `json:"completed"`
}

type StartTrainingJobsResponse struct {
	Id string `json:"id"`
}
