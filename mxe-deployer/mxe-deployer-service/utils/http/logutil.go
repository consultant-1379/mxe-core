package http

import (
	"context"
	"net"
	"net/http"

	"github.com/go-kit/kit/log"
	kithttp "github.com/go-kit/kit/transport/http"
)

// NewHTTPLogger returns a Logger from a Go-Kit Logger.
func NewHTTPLogger(logger log.Logger) *HTTPLogger {
	return &HTTPLogger{logger: logger}
}

// HTTPLogger wraps the Go-Kit Logger to return a logger which implements a
// ServerFinalizerFunc.
type HTTPLogger struct {
	logger log.Logger
}

type interceptingWriter struct {
	http.ResponseWriter
	code    int
	written int64
}

func (iw *interceptingWriter) WriteHeader(code int) {
	iw.code = code
	iw.ResponseWriter.WriteHeader(code)
}

// Middleware calls the ServerFinalizerFunc at the end of an HTTP Request.
func HTTPLogMiddleware(finalizer kithttp.ServerFinalizerFunc, next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		ctx := r.Context()

		iw := &interceptingWriter{w, http.StatusOK, 0}
		defer func() {
			ctx = context.WithValue(ctx, kithttp.ContextKeyResponseHeaders, iw.Header())
			ctx = context.WithValue(ctx, kithttp.ContextKeyResponseSize, iw.written)
			finalizer(ctx, iw.code, r)
		}()
		w = iw

		next.ServeHTTP(w, r)
	})
}

// Middleware returns creates an HTTP logging middleware using LoggingFinalizer.
func (l *HTTPLogger) Middleware(next http.Handler) http.Handler {
	return HTTPLogMiddleware(l.LoggingFinalizer, next)
}

// LoggingFinalizer is a ServerFinalizerFunc which logs information about a completed
// HTTP Request.
func (l *HTTPLogger) LoggingFinalizer(ctx context.Context, code int, r *http.Request) {
	host, _, err := net.SplitHostPort(r.RemoteAddr)
	if err != nil {
		host = r.RemoteAddr
	}

	url := *r.URL
	uri := r.RequestURI

	// Requests using the CONNECT method over HTTP/2.0 must use
	// the authority field (aka r.Host) to identify the target.
	// Refer: https://httpwg.github.io/specs/rfc7540.html#CONNECT
	if r.ProtoMajor == 2 && r.Method == "CONNECT" {
		uri = r.Host
	}

	if uri == "" {
		uri = url.RequestURI()
	}

	keyvals := []interface{}{
		"method", r.Method,
		"status", code,
		"proto", r.Proto,
		"host", host,
		"user_agent", r.UserAgent(),
		"path", uri,
	}

	if referer := r.Referer(); referer != "" {
		keyvals = append(keyvals, "referer", referer)
	}

	// check both the finalizer context key and the go-kit one.
	if size, ok := ResponseSize(ctx); ok {
		keyvals = append(keyvals, "response_size", size)
	}

	l.logger.Log(keyvals...)
}

// ResponseSize returns the written response size from a ServerFinalizerFunc context.
func ResponseSize(ctx context.Context) (int, bool) {
	size, ok := ctx.Value(kithttp.ContextKeyResponseSize).(int)
	return size, ok
}
