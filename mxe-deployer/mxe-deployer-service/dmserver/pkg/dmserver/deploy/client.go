package deploy

import (
	"net/url"

	"github.com/go-kit/kit/log"
	httptransport "github.com/go-kit/kit/transport/http"
)

// MakeClientEndpoints returns an Endpoints struct where each endpoint invokes
// the corresponding method on the remote instance, via a transport/http.Client.
// Useful in a profilesvc client.
// instance is the address of the endpint which has the functionality exposed in the serverendpoint
func MakeClientEndpoints(instance string, logger log.Logger, opts ...httptransport.ClientOption) (*Endpoints, error) {
	tgt, err := url.Parse(instance)
	if err != nil {
		return &Endpoints{}, err
	}
	tgt.Path = ""

	// Note that the request encoders need to modify the request URL, changing
	// the path. That's fine: we simply need to provide specific encoders for
	// each endpoint.

	return &Endpoints{
		PostPackageEndpoint:     httptransport.NewClient("POST", tgt, encodePostPackageRequest, decodePostPackageResponse, opts...).Endpoint(),
		PatchPackageEndpoint:    httptransport.NewClient("PATCH", tgt, encodePatchPackageRequest, decodePatchPackageResponse, opts...).Endpoint(),
		PostPackageSyncEndpoint: httptransport.NewClient("POST", tgt, encodePostPackageSyncRequest, decodePostPackageSyncResponse, opts...).Endpoint(),
		GetPackagesEndpoint:     httptransport.NewClient("GET", tgt, encodeGetPackagesRequest, decodeGetPackagesResponse, opts...).Endpoint(),
		DeletePackageEndpoint:   httptransport.NewClient("DELETE", tgt, encodeDeletePackageRequest, decodeDeletePackageResponse, opts...).Endpoint(),
	}, nil
}
