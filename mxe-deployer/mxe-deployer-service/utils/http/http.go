package http

import (
	"bytes"
	"context"
	"crypto/subtle"
	"encoding/json"
	"errors"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"path"
	"strings"

	kitlog "github.com/go-kit/kit/log"
	httptransport "github.com/go-kit/kit/transport/http"
	"github.com/gorilla/mux"
)

const (
	ContentTypeJSON = "application/json"
	ContentTypeHTML = "text/html"
	ContentTypeText = "text/plain"
)

func NewRouter(logger kitlog.Logger) (*mux.Router, []httptransport.ServerOption) {
	r := mux.NewRouter()
	options := []httptransport.ServerOption{
		httptransport.ServerErrorEncoder(ErrorEncoder),
		httptransport.ServerErrorLogger(logger),
		httptransport.ServerBefore(httptransport.PopulateRequestContext),
	}
	return r, options
}

func NewClientWithToken(bearerToken string, httpClient *http.Client) []httptransport.ClientOption {
	options := []httptransport.ClientOption{
		httptransport.ClientBefore(httptransport.SetRequestHeader("Authorization", fmt.Sprintf("Bearer %s", bearerToken))),
		httptransport.SetClient(httpClient),
	}
	return options
}

func NewClient(headers map[string]string, httpClient *http.Client) []httptransport.ClientOption {
	var requestFuncs []httptransport.RequestFunc

	for key, value := range headers {
		requestFuncs = append(requestFuncs, httptransport.SetRequestHeader(key, value))
	}

	options := []httptransport.ClientOption{
		httptransport.ClientBefore(requestFuncs...),
		httptransport.SetClient(httpClient),
	}
	return options
}

func EncodeJSONResponse(ctx context.Context, w http.ResponseWriter, response interface{}) error {
	if f, ok := response.(failer); ok && f.Error() != nil {
		ErrorEncoder(ctx, f.Error(), w)
		return nil
	}
	if headerer, ok := response.(httptransport.Headerer); ok {
		for k, values := range headerer.Headers() {
			for _, v := range values {
				w.Header().Add(k, v)
			}
		}
	}
	w.Header().Set("Content-Type", "application/json; charset=utf-8")

	code := http.StatusOK
	if sc, ok := response.(httptransport.StatusCoder); ok {
		code = sc.StatusCode()
	}
	w.WriteHeader(code)

	enc := json.NewEncoder(w)
	enc.SetIndent("", "  ")
	return enc.Encode(response)
}

func ErrorEncoder(_ context.Context, err error, w http.ResponseWriter) {
	errMap := map[string]interface{}{"error": err.Error()}
	enc := json.NewEncoder(w)
	enc.SetIndent("", "  ")

	if headerer, ok := err.(httptransport.Headerer); ok {
		for k, values := range headerer.Headers() {
			for _, v := range values {
				w.Header().Add(k, v)
			}
		}
	}
	w.Header().Set("Content-Type", "application/json; charset=utf-8")

	code := http.StatusInternalServerError
	if sc, ok := err.(httptransport.StatusCoder); ok {
		code = sc.StatusCode()
	}
	w.WriteHeader(code)

	enc.Encode(errMap)
}

// failer is an interface that should be implemented by response types.
// Response encoders can check if responses are Failer, and if so if they've
// failed, and if so encode them using a separate write path based on the error.
type failer interface {
	Error() error
}

type errorWrapper struct {
	Error string `json:"error"`
}

func ErrorDecoder(r *http.Response) error {
	contentType := r.Header.Get("Content-Type")
	if strings.Contains(contentType, ContentTypeJSON) {
		return JSONErrorDecoder(r)
	}
	return TextErrorDecoder(r, contentType)
}

func JSONErrorDecoder(r *http.Response) error {
	var w errorWrapper
	if err := json.NewDecoder(r.Body).Decode(&w); err != nil {
		return err
	}
	return errors.New(w.Error)
}

func TextErrorDecoder(r *http.Response, contentType string) error {
	responseBytes, _ := ioutil.ReadAll(r.Body)
	errorMsg := fmt.Sprintf("expected JSON formatted error response, but detected error with Content-Type %s. Error is: %s", contentType, string(responseBytes))
	return errors.New(errorMsg)
}

// EncodeRequest likewise JSON-encodes the request to the HTTP request body.
// Don't use it directly as a transport/http.Client EncodeRequestFunc:
// profilesvc endpoints require mutating the HTTP method and request path.
func EncodeRequest(_ context.Context, req *http.Request, request interface{}) error {
	var buf bytes.Buffer
	err := json.NewEncoder(&buf).Encode(request)
	if err != nil {
		return err
	}
	req.Body = ioutil.NopCloser(&buf)
	return nil
}

func CopyURL(base *url.URL, appendPath string) *url.URL {
	next := *base
	next.Path = path.Join(base.Path, appendPath)
	return &next
}

func DecodeJSONRequest(r *http.Request, into interface{}) error {
	defer r.Body.Close()
	err := json.NewDecoder(r.Body).Decode(into)
	return err
}

func DecodeJSONResponse(r *http.Response, into interface{}) error {
	defer r.Body.Close()

	if r.StatusCode != http.StatusOK {
		return ErrorDecoder(r)
	}

	err := json.NewDecoder(r.Body).Decode(into)
	return err
}

func RequireBasicAuth(h http.HandlerFunc, username, password, realm string) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		u, p, ok := r.BasicAuth()
		if !ok || subtle.ConstantTimeCompare([]byte(u), []byte(username)) != 1 || subtle.ConstantTimeCompare([]byte(p), []byte(password)) != 1 {
			w.Header().Set("WWW-Authenticate", `Basic realm="`+realm+`"`)
			w.WriteHeader(401)
			w.Write([]byte("Authorization Required\n"))
			return
		}
		h(w, r)
	}
}
