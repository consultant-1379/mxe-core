package session

import (
	"context"
	"encoding/json"
	"net/http"

	httpUtil "mxe.ericsson/depmanager/utils/http"
)

/*
POST CreateSession section
*/

func decodeSessionCreateRequest(ctx context.Context, r *http.Request) (request interface{}, err error) {

	var createSessionReq *SessionCreateRequest

	if err := json.NewDecoder(r.Body).Decode(&createSessionReq); err != nil {
		return nil, err
	}
	return createSessionReq, nil
}

func encodeSessionCreateRequest(ctx context.Context, req *http.Request, request interface{}) error {
	// r.Methods("POST").Path("/profiles/")
	req.URL.Path = SessionPath
	return httpUtil.EncodeRequest(ctx, req, request)
}

func decodeSessionCreateResponse(ctx context.Context, resp *http.Response) (interface{}, error) {
	var response SessionCreateResponse
	err := httpUtil.DecodeJSONResponse(resp, &response)
	return response, err
}

// CreateSession implements Service. Primarily useful in a client.
func (e Endpoints) CreateSession(ctx context.Context, sessionCreateReq SessionCreateRequest) (string, string, error) {
	response, err := e.CreateSessionEndpoint(ctx, sessionCreateReq)
	if err != nil {
		return "", "", err
	}
	resp := response.(SessionCreateResponse)
	return resp.Token, resp.RefreshToken, resp.Err
}
