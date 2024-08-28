package utils

import (
	"time"

	"github.com/briandowns/spinner"
)

func StartSpinner(prefix string) *spinner.Spinner {
	s := spinner.New(spinner.CharSets[9], 200*time.Millisecond)
	s.Prefix = prefix + " "
	s.FinalMSG = prefix
	s.Start()

	return s
}

func StopSpinner(s *spinner.Spinner) {
	s.Stop()
	LogInfo("")
}

func HandleSpinnerErrorMessage(errorMsg string, s *spinner.Spinner) {
	StopSpinner(s)
	LogError(errorMsg)
}

func HandleSpinnerError(err error, s *spinner.Spinner) {
	HandleSpinnerErrorMessage(err.Error(), s)
}
