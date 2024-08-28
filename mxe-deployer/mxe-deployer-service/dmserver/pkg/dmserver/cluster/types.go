package cluster

//ListClusterRequest contains authtoken
type ListClusterRequest struct {
	AuthToken string
}

//ListClusterResponse list of kube clusters added in argocd
type ListClusterResponse struct {
	ClustersList []string `json:"clusters"`
	Err          error    `json:"err,omitempty"`
}
