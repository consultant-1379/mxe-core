package errors

import "context"

type errNotFound struct{ error }

func (errNotFound) NotFound() {}

func (e errNotFound) Cause() error {
	return e.error
}

func (e errNotFound) StatusCode() int {
	return GetHTTPErrorStatusCode(e)
}

// NotFound is a helper to create an error of the class with the same name from any error type
func NotFound(err error) error {
	if err == nil || IsNotFound(err) {
		return err
	}
	return errNotFound{err}
}

type errInvalidParameter struct{ error }

func (errInvalidParameter) InvalidParameter() {}

func (e errInvalidParameter) Cause() error {
	return e.error
}

func (e errInvalidParameter) StatusCode() int {
	return GetHTTPErrorStatusCode(e)
}

// InvalidParameter is a helper to create an error of the class with the same name from any error type
func InvalidParameter(err error) error {
	if err == nil || IsInvalidParameter(err) {
		return err
	}
	return errInvalidParameter{err}
}

type errConflict struct{ error }

func (errConflict) Conflict() {}

func (e errConflict) Cause() error {
	return e.error
}

func (e errConflict) StatusCode() int {
	return GetHTTPErrorStatusCode(e)
}

// Conflict is a helper to create an error of the class with the same name from any error type
func Conflict(err error) error {
	if err == nil || IsConflict(err) {
		return err
	}
	return errConflict{err}
}

type errUnauthorized struct{ error }

func (errUnauthorized) Unauthorized() {}

func (e errUnauthorized) Cause() error {
	return e.error
}

func (e errUnauthorized) StatusCode() int {
	return GetHTTPErrorStatusCode(e)
}

// Unauthorized is a helper to create an error of the class with the same name from any error type
func Unauthorized(err error) error {
	if err == nil || IsUnauthorized(err) {
		return err
	}
	return errUnauthorized{err}
}

type errUnavailable struct{ error }

func (errUnavailable) Unavailable() {}

func (e errUnavailable) Cause() error {
	return e.error
}

func (e errUnavailable) StatusCode() int {
	return GetHTTPErrorStatusCode(e)
}

// Unavailable is a helper to create an error of the class with the same name from any error type
func Unavailable(err error) error {
	if err == nil || IsUnavailable(err) {
		return err
	}
	return errUnavailable{err}
}

type errForbidden struct{ error }

func (errForbidden) Forbidden() {}

func (e errForbidden) Cause() error {
	return e.error
}

func (e errForbidden) StatusCode() int {
	return GetHTTPErrorStatusCode(e)
}

// Forbidden is a helper to create an error of the class with the same name from any error type
func Forbidden(err error) error {
	if err == nil || IsForbidden(err) {
		return err
	}
	return errForbidden{err}
}

type errSystem struct{ error }

func (errSystem) System() {}

func (e errSystem) Cause() error {
	return e.error
}

func (e errSystem) StatusCode() int {
	return GetHTTPErrorStatusCode(e)
}

// System is a helper to create an error of the class with the same name from any error type
func System(err error) error {
	if err == nil || IsSystem(err) {
		return err
	}
	return errSystem{err}
}

type errNotModified struct{ error }

func (errNotModified) NotModified() {}

func (e errNotModified) Cause() error {
	return e.error
}

func (e errNotModified) StatusCode() int {
	return GetHTTPErrorStatusCode(e)
}

// NotModified is a helper to create an error of the class with the same name from any error type
func NotModified(err error) error {
	if err == nil || IsNotModified(err) {
		return err
	}
	return errNotModified{err}
}

type errNotImplemented struct{ error }

func (errNotImplemented) NotImplemented() {}

func (e errNotImplemented) Cause() error {
	return e.error
}

func (e errNotImplemented) StatusCode() int {
	return GetHTTPErrorStatusCode(e)
}

// NotImplemented is a helper to create an error of the class with the same name from any error type
func NotImplemented(err error) error {
	if err == nil || IsNotImplemented(err) {
		return err
	}
	return errNotImplemented{err}
}

type errUnknown struct{ error }

func (errUnknown) Unknown() {}

func (e errUnknown) Cause() error {
	return e.error
}

func (e errUnknown) StatusCode() int {
	return GetHTTPErrorStatusCode(e)
}

// Unknown is a helper to create an error of the class with the same name from any error type
func Unknown(err error) error {
	if err == nil || IsUnknown(err) {
		return err
	}
	return errUnknown{err}
}

type errCancelled struct{ error }

func (errCancelled) Cancelled() {}

func (e errCancelled) Cause() error {
	return e.error
}
func (e errCancelled) StatusCode() int {
	return GetHTTPErrorStatusCode(e)
}

// Cancelled is a helper to create an error of the class with the same name from any error type
func Cancelled(err error) error {
	if err == nil || IsCancelled(err) {
		return err
	}
	return errCancelled{err}
}

type errDeadline struct{ error }

func (errDeadline) DeadlineExceeded() {}

func (e errDeadline) Cause() error {
	return e.error
}

func (e errDeadline) StatusCode() int {
	return GetHTTPErrorStatusCode(e)
}

// Deadline is a helper to create an error of the class with the same name from any error type
func Deadline(err error) error {
	if err == nil || IsDeadline(err) {
		return err
	}
	return errDeadline{err}
}

type errDataLoss struct{ error }

func (errDataLoss) DataLoss() {}

func (e errDataLoss) Cause() error {
	return e.error
}

func (e errDataLoss) StatusCode() int {
	return GetHTTPErrorStatusCode(e)
}

// DataLoss is a helper to create an error of the class with the same name from any error type
func DataLoss(err error) error {
	if err == nil || IsDataLoss(err) {
		return err
	}
	return errDataLoss{err}
}

// FromContext returns the error class from the passed in context
func FromContext(ctx context.Context) error {
	e := ctx.Err()
	if e == nil {
		return nil
	}

	if e == context.Canceled {
		return Cancelled(e)
	}
	if e == context.DeadlineExceeded {
		return Deadline(e)
	}
	return Unknown(e)
}
