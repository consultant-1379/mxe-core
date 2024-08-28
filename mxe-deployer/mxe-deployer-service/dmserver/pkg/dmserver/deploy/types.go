package deploy

import (
	"github.com/argoproj/argo-cd/v2/pkg/apiclient/application"
	v1alpha1 "github.com/argoproj/argo-cd/v2/pkg/apis/application/v1alpha1"
	fileUtils "mxe.ericsson/depmanager/utils/file"
)

// PackageRequestMeta contains metadata about the package request
type PackageRequestMeta struct {
	ApplicationName string                          `json:"appName,omitempty"`
	Source          v1alpha1.ApplicationSource      `json:"packageSource,omitempty"`
	Destination     v1alpha1.ApplicationDestination `json:"packageDestination"`
	Project         string                          `json:"project,omitempty"`
	Labels          []string                        `json:"labels,omitempty"`
	Annotations     []string                        `json:"annotations,omitempty"`
	SyncPolicy      *v1alpha1.SyncPolicy            `json:"syncPolicy,omitempty"`
	InitSync        bool                            `json:"initSync,omitempty"`
}

//PostPackageRequest contains PackageMeta and manifests
type PostPackageRequest struct {
	*PackageRequestMeta
	Archive *fileUtils.InputFile //optional
}

//PatchPackageRequest contains PackageMeta and manifests
type PatchPackageRequest struct {
	ApplicationQuery *application.ApplicationQuery
	Archive          *fileUtils.InputFile //optional
}

//PostPackageResponse represents the error response if any from the Deployer service for bootstrapping
type PostPackageResponse struct {
	Application *v1alpha1.Application `json:"application"`
	Err         error                 `json:"err"`
}

func (r PostPackageResponse) Error() error { return r.Err }

//PostPackageSyncRequest contains PackageMeta and manifests
type PostPackageSyncRequest struct {
	SyncReq *application.ApplicationSyncRequest `json:"applicationSyncReq"`
}

//PostPackageSyncResponse represents the error response if any from the Deployer service for bootstrapping
type PostPackageSyncResponse struct {
	Application *v1alpha1.Application `json:"application"`
	Err         error                 `json:"err,omitempty"`
}

func (r PostPackageSyncResponse) Error() error { return r.Err }

//PatchPackageResponse represents the error response if any from the Deployer service for bootstrapping
type PatchPackageResponse struct {
	Application *v1alpha1.Application `json:"application"`
	Err         error                 `json:"err,omitempty"`
}

func (r PatchPackageResponse) Error() error { return r.Err }

//GetApplicationsRequest contains authtoken
type GetApplicationsRequest struct {
	AppQuery *application.ApplicationQuery
}

//GetApplicationsResponse list of applications matching application query
type GetApplicationsResponse struct {
	ApplicationsList *v1alpha1.ApplicationList `json:"packages"`
	Err              error                     `json:"err,omitempty"`
}

func (r GetApplicationsResponse) Error() error { return r.Err }

// DeleteApplicationRequest name of the Application to be deleted
type DeleteApplicationRequest struct {
	ApplicationName   *string
	PropagationPolicy *string
}

// DeleteApplicationResponse name of the Application to be deleted
type DeleteApplicationResponse struct {
	Status bool  `json:"status"`
	Err    error `json:"err,omitempty"`
}

func (r DeleteApplicationResponse) Error() error { return r.Err }
