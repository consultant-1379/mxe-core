package session

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
		CreateSessionEndpoint: httptransport.NewClient("POST", tgt, encodeSessionCreateRequest, decodeSessionCreateResponse, opts...).Endpoint(),
	}, nil
}
