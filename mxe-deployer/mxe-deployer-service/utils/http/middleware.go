package http

import (
	"bytes"
	"fmt"
	"io"
	"net/http"
	"net/http/httptest"
	"net/http/httputil"
)

// Middleware is a chainable decorator for HTTP Handlers.
type Middleware func(http.Handler) http.Handler

// DebugMiddleware is a Middleware which prints the HTTP request and response to out.
// Use os.Stdout to print to standard out.
// If printBody is false, only the HTTP headers are printed.
// The Middleware requires a logger in case the request fails.
//
// Example: handler = HTTPDebugMiddleware(debugOut, true, nopLogger)(handler)
func DebugMiddleware(out io.Writer, printBody bool, logger func(...interface{}) error) Middleware {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			dump, err := httputil.DumpRequest(r, printBody)
			if err != nil {
				logger("err", err)
			}
			fmt.Fprintf(out, "---BEGIN Request---\n%s\n---END Request---\n", string(dump))
			recorder := httptest.NewRecorder()

			next.ServeHTTP(recorder, r)

			for key, values := range recorder.Header() {
				w.Header().Del(key)
				for _, value := range values {
					w.Header().Set(key, value)
				}
			}

			buf := new(bytes.Buffer)
			recorder.Body.WriteTo(io.MultiWriter(w, buf))
			recorder.Body = buf

			respDump, err := httputil.DumpResponse(recorder.Result(), printBody)
			if err != nil {
				logger("err", err)
			}

			fmt.Fprintf(out, "---BEGIN Response---\n%s\n---END Response---\n", string(respDump))
		})
	}
}
