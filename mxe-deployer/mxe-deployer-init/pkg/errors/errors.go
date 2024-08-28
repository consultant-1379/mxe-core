package errors

import (
	"os"

	log "github.com/sirupsen/logrus"
)

const (

	// ErrorGeneric is returned for generic error
	ErrorGeneric = 20
)

// Fatal is a wrapper for logrus.Fatal() to exit with custom code
func Fatal(exitcode int, args ...interface{}) {
	exitfunc := func() {
		os.Exit(exitcode)
	}
	log.RegisterExitHandler(exitfunc)
	log.Fatal(args...)
}

func CheckError(err error) {
	if err != nil {
		Fatal(ErrorGeneric, err)
	}
}
