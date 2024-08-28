package utils

import (
	"os"
	"strings"

	"mxe.ericsson/mxe-deploy-init/pkg/errors"

	log "github.com/sirupsen/logrus"
)

const (
	UrlSeparator string = "/"
)

// SetLogFormat sets a logrus log format
func SetLogFormat(logFormat string) {
	switch strings.ToLower(logFormat) {
	case "json":
		log.SetFormatter(&log.JSONFormatter{})
	case "text":
		if os.Getenv("FORCE_LOG_COLORS") == "1" {
			log.SetFormatter(&log.TextFormatter{ForceColors: true})
		}
	default:
		log.Fatalf("Unknown log format '%s'", logFormat)
	}
}

// SetLogLevel parses and sets a logrus log level
func SetLogLevel(logLevel string) {
	level, err := log.ParseLevel(logLevel)
	errors.CheckError(err)
	log.SetLevel(level)
}

func SetReportCaller() {
	log.SetReportCaller(true)
}
