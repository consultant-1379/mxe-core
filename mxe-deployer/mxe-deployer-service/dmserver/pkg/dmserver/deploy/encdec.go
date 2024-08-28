package deploy

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"io/ioutil"
	"mime/multipart"
	"net/http"
	"strings"

	log "github.com/sirupsen/logrus"

	"github.com/argoproj/argo-cd/v2/pkg/apiclient/application"
	"github.com/argoproj/argo-cd/v2/pkg/apis/application/v1alpha1"
	"github.com/pkg/errors"
	fileUtils "mxe.ericsson/depmanager/utils/file"
	httpUtil "mxe.ericsson/depmanager/utils/http"
	"sigs.k8s.io/yaml"
)

/*
POST PackageRequest section
*/

func decodePackageRequestMeta(packageOptions string) (*PackageRequestMeta, error) {
	var requestMeta *PackageRequestMeta

	err := yaml.Unmarshal([]byte(packageOptions), &requestMeta)
	if err != nil {
		fmt.Printf("\n %#v", packageOptions)
		return nil, errors.Wrapf(err, "Failed to read the request data")
	}

	if (requestMeta.Destination == v1alpha1.ApplicationDestination{} ||
		requestMeta.Source == v1alpha1.ApplicationSource{}) {
		return nil, errors.New("Source/Destination is not set correctly. ")
	}

	return requestMeta, nil
}

func validatePostPackageRequest(archiveInputFile *fileUtils.InputFile, packageOptions *PackageRequestMeta) error {
	if archiveInputFile == nil {
		log.Info("Application archive is missing, assuming that manifests are already available in git")
	}

	if packageOptions == nil {
		return errors.New("No options supplied for creating application. formValue options is missing")
	}

	return nil
}

func decodePostPackageRequest(ctx context.Context, r *http.Request) (request interface{}, err error) {

	defer r.Body.Close()

	var archiveInputFile *fileUtils.InputFile = nil

	archiveFile, archiveFileHeader, err := r.FormFile("archive")
	if err == nil {
		archiveInputFile = &fileUtils.InputFile{
			FileName: archiveFileHeader.Filename,
			Reader:   archiveFile,
		}
	} else if err != http.ErrMissingFile {
		return nil, errors.Wrap(err, "Error thrown while fetching the input archive file")
	}

	packageOptions := r.FormValue("options")

	appPackageMeta, err := decodePackageRequestMeta(packageOptions)
	if err != nil {
		return nil, errors.Wrap(err, "Failed to read package options")
	}

	err = validatePostPackageRequest(archiveInputFile, appPackageMeta)
	if err != nil {
		return nil, err
	}

	return PostPackageRequest{
		PackageRequestMeta: appPackageMeta,
		Archive:            archiveInputFile,
	}, nil
}

func encodePostPackageRequest(ctx context.Context, r *http.Request, request interface{}) error {
	req := request.(PostPackageRequest)
	r.URL.Path = PackagePath

	body := new(bytes.Buffer)
	writer := multipart.NewWriter(body)

	if req.Archive != nil {
		archiveFile, err := writer.CreateFormFile("archive", req.Archive.FileName)
		if err != nil {
			return err
		}
		_, err = io.Copy(archiveFile, req.Archive.Reader)
		if err != nil {
			return errors.Wrap(err, "copying archive file to multipart writer")
		}

		writer.WriteField("archive", req.Archive.FileName)
	} else {
		log.Info("Application archive is missing, assuming that manifests are already available in git")
	}

	jsonReq, err := json.Marshal(req.PackageRequestMeta)
	if err != nil {
		return err
	}
	fieldWriter, err := writer.CreateFormField("options")
	if err != nil {
		return err
	}
	io.Copy(fieldWriter, bytes.NewReader(jsonReq))

	if err := writer.Close(); err != nil {
		return errors.Wrap(err, "closing multipart writer")
	}

	r.Header.Set("Content-Type", writer.FormDataContentType())
	r.Body = ioutil.NopCloser(body)

	return nil
}

func decodePostPackageResponse(ctx context.Context, resp *http.Response) (interface{}, error) {
	var response PostPackageResponse
	err := httpUtil.DecodeJSONResponse(resp, &response)

	/*body, err := ioutil.ReadAll(resp.Body)
	fmt.Printf("\n body:%s", string(body))

	if err != nil {
		log.Printf("Error reading body: %v", err)
	}
	resp.Body = ioutil.NopCloser(bytes.NewBuffer(body))
	*/
	return response, err
}

// PostPackage implements Service. Primarily useful in a client.
func (e Endpoints) PostPackage(ctx context.Context, packageOptions *PackageRequestMeta, archiveFile *fileUtils.InputFile) (*PostPackageResponse, error) {
	request := PostPackageRequest{
		PackageRequestMeta: packageOptions,
		Archive:            archiveFile,
	}
	response, err := e.PostPackageEndpoint(ctx, request)
	if err != nil {
		return nil, err
	}
	resp := response.(PostPackageResponse)
	return &resp, nil
}

/*
PATCH PackageRequest section
*/

func decodePatchPackageRequestMeta(applicationSelector string) (*application.ApplicationQuery, error) {
	var appQuery *application.ApplicationQuery

	err := yaml.Unmarshal([]byte(applicationSelector), &appQuery)
	if err != nil {
		return nil, errors.Wrapf(err, "Failed to read the request data")
	}

	return appQuery, nil
}

func validatePatchPackageRequest(archiveInputFile *fileUtils.InputFile, appQuery *application.ApplicationQuery) error {
	if archiveInputFile == nil {
		return errors.New("Archive file is not supplied, it is a mandatory input")
	}

	if appQuery == nil {
		return errors.New("Cannot patch, appName is not specified")
	}

	return nil
}

func decodePatchPackageRequest(ctx context.Context, r *http.Request) (request interface{}, err error) {

	defer r.Body.Close()

	var archiveInputFile *fileUtils.InputFile = nil

	archiveFile, archiveFileHeader, err := r.FormFile("archive")
	if err != nil {
		return nil, errors.Wrap(err, "Error thrown while fetching the input archive file")
	}
	archiveInputFile = &fileUtils.InputFile{
		Reader:   archiveFile,
		FileName: archiveFileHeader.Filename,
	}

	applicationSelector := r.FormValue("appSelector")

	appQuery, err := decodePatchPackageRequestMeta(applicationSelector)
	if err != nil {
		return nil, errors.Wrap(err, "Failed to read package options")
	}

	err = validatePatchPackageRequest(archiveInputFile, appQuery)
	if err != nil {
		return nil, err
	}

	return PatchPackageRequest{
		ApplicationQuery: appQuery,
		Archive:          archiveInputFile,
	}, nil

}

func encodePatchPackageRequest(ctx context.Context, r *http.Request, request interface{}) error {
	req := request.(PatchPackageRequest)
	r.URL.Path = PackagePath

	body := new(bytes.Buffer)
	writer := multipart.NewWriter(body)

	if req.Archive == nil {
		return errors.New("Archive file is not supplied, it is a mandatory input")
	} else {
		archiveFile, err := writer.CreateFormFile("archive", req.Archive.FileName)
		if err != nil {
			return err
		}

		_, err = io.Copy(archiveFile, req.Archive.Reader)
		if err != nil {
			return errors.Wrap(err, "Error while copying archive file to multipart writer")
		}
		writer.WriteField("archive", req.Archive.FileName)

	}

	jsonReq, err := json.Marshal(req.ApplicationQuery)
	if err != nil {
		return err
	}
	fieldWriter, err := writer.CreateFormField("appSelector")
	if err != nil {
		return err
	}
	io.Copy(fieldWriter, bytes.NewReader(jsonReq))

	if err := writer.Close(); err != nil {
		return errors.Wrap(err, "closing multipart writer")
	}

	r.Header.Set("Content-Type", writer.FormDataContentType())
	r.Body = ioutil.NopCloser(body)

	return nil
}

func decodePatchPackageResponse(ctx context.Context, resp *http.Response) (interface{}, error) {
	var response PatchPackageResponse
	err := httpUtil.DecodeJSONResponse(resp, &response)
	return response, err
}

// PatchPackage implements Service. Primarily useful in a client.
func (e Endpoints) PatchPackage(ctx context.Context, appQuery *application.ApplicationQuery, manifestArchive *fileUtils.InputFile) (*PatchPackageResponse, error) {
	request := PatchPackageRequest{
		ApplicationQuery: appQuery,
		Archive:          manifestArchive,
	}
	response, err := e.PatchPackageEndpoint(ctx, request)
	if err != nil {
		return nil, err
	}
	resp := response.(PatchPackageResponse)
	return &resp, nil
}

func decodePostPackageSyncRequest(ctx context.Context, r *http.Request) (interface{}, error) {
	var request PostPackageSyncRequest
	err := httpUtil.DecodeJSONRequest(r, &request)
	return request, err
}

func decodePostPackageSyncResponse(ctx context.Context, resp *http.Response) (interface{}, error) {
	var response PostPackageSyncResponse
	err := httpUtil.DecodeJSONResponse(resp, &response)
	return response, err
}

func encodePostPackageSyncRequest(ctx context.Context, req *http.Request, request interface{}) error {
	req.URL.Path = PackageSyncPath
	syncReq := request.(PostPackageSyncRequest)
	return httpUtil.EncodeRequest(ctx, req, syncReq)

}

// SyncPackage implements Service. Primarily useful in a client.
func (e Endpoints) SyncPackage(ctx context.Context, appSyncReq *application.ApplicationSyncRequest) (*PostPackageSyncResponse, error) {
	request := PostPackageSyncRequest{
		SyncReq: appSyncReq,
	}
	response, err := e.PostPackageSyncEndpoint(ctx, request)
	if err != nil {
		return nil, err
	}
	resp := response.(PostPackageSyncResponse)
	return &resp, nil
}

func decodeGetPackagesRequest(ctx context.Context, r *http.Request) (request interface{}, err error) {

	if applnNames, ok := r.URL.Query()["name"]; ok {
		appQuery := application.ApplicationQuery{Name: &applnNames[0]}
		return GetApplicationsRequest{
			AppQuery: &appQuery,
		}, nil
	} else {
		selector := r.URL.RawQuery
		log.Println("Selector is:", selector)

		filtersAppQuery := application.ApplicationQuery{Selector: &selector}
		return GetApplicationsRequest{
			AppQuery: &filtersAppQuery,
		}, nil
	}

}

func encodeGetPackagesRequest(ctx context.Context, req *http.Request, request interface{}) error {
	req.URL.Path = PackagePath
	listPackageRequest := request.(GetApplicationsRequest)
	if listPackageRequest.AppQuery != nil {
		vals := req.URL.Query()
		if listPackageRequest.AppQuery.Selector != nil && len(*listPackageRequest.AppQuery.Selector) > 0 {
			filterComponents := strings.Split(*listPackageRequest.AppQuery.Selector, "=")
			vals.Set(filterComponents[0], filterComponents[1])
			req.URL.RawQuery = vals.Encode()
		} else if listPackageRequest.AppQuery.Name != nil {
			vals.Set("name", *listPackageRequest.AppQuery.Name)
			req.URL.RawQuery = vals.Encode()
		}
	}
	return httpUtil.EncodeRequest(ctx, req, request)
}

func decodeGetPackagesResponse(ctx context.Context, resp *http.Response) (interface{}, error) {
	var response GetApplicationsResponse
	err := httpUtil.DecodeJSONResponse(resp, &response)
	return response, err
}

// GetPackages implements Service. Primarily useful in a client.
func (e Endpoints) GetPackages(ctx context.Context, applnQuery *application.ApplicationQuery) (*GetApplicationsResponse, error) {
	request := GetApplicationsRequest{
		AppQuery: applnQuery,
	}
	response, err := e.GetPackagesEndpoint(ctx, request)
	if err != nil {
		return nil, err
	}
	resp := response.(GetApplicationsResponse)
	return &resp, nil
}

// decodeDeletePackageRequest
func decodeDeletePackageRequest(ctx context.Context, r *http.Request) (request interface{}, err error) {
	var applicationDelReq DeleteApplicationRequest

	if applnNames, ok := r.URL.Query()["name"]; ok {
		log.Printf("Got AppParam urlParams: %v", applnNames)
		applicationDelReq = DeleteApplicationRequest{
			ApplicationName: &applnNames[0],
		}
	} else {
		return nil, errors.New("ApplicationName is missing in input")
	}

	if propagationPolicy, ok := r.URL.Query()["propagationPolicy"]; ok {
		log.Printf("Got propagationPolicy from urlParams: %v", propagationPolicy)
		applicationDelReq.PropagationPolicy = &propagationPolicy[0]
	}

	return applicationDelReq, nil
}

// DeletePackage implements Service. Primarily useful in a client.
func (e Endpoints) DeletePackage(ctx context.Context, appName *string, propagationPolicy *string) (*DeleteApplicationResponse, error) {

	request := DeleteApplicationRequest{
		ApplicationName:   appName,
		PropagationPolicy: propagationPolicy,
	}
	response, err := e.DeletePackageEndpoint(ctx, request)
	if err != nil {
		return nil, err
	}
	resp := response.(DeleteApplicationResponse)
	return &resp, nil
}

func encodeDeletePackageRequest(ctx context.Context, req *http.Request, request interface{}) error {
	req.URL.Path = PackagePath
	vals := req.URL.Query()
	reqStruct := request.(DeleteApplicationRequest)
	vals.Set("name", *reqStruct.ApplicationName)
	if reqStruct.PropagationPolicy != nil {
		vals.Set("propagationPolicy", *reqStruct.PropagationPolicy)
	}
	req.URL.RawQuery = vals.Encode()
	return httpUtil.EncodeRequest(ctx, req, request)
}
func decodeDeletePackageResponse(ctx context.Context, resp *http.Response) (interface{}, error) {
	var response DeleteApplicationResponse
	err := httpUtil.DecodeJSONResponse(resp, &response)
	return response, err
}
