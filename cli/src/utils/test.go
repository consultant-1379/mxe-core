package utils

import (
	"fmt"
	"io/ioutil"
	"os"
	"strings"
	"testing"

	"github.com/stretchr/testify/assert"
)

var tmpExitCode int

type testable func()

func getPrintOutput(fn testable) string {
	rescueStdout := os.Stdout
	r, w, _ := os.Pipe()
	os.Stdout = w

	defer func() {
		os.Stdout = rescueStdout
	}()

	fn()

	w.Close()
	out, _ := ioutil.ReadAll(r)
	os.Stdout = rescueStdout

	return string(out)
}

func tmpExit(code int) {
	tmpExitCode = code
}
func HandleExit(fn testable) {
	origOsExit := Exit
	Exit = tmpExit
	// Don't forget to switch functions back!
	defer func() { Exit = origOsExit }()

	fn()

	if tmpExitCode != 1 {
		LogError(fmt.Sprintf("Function crashed with exit code other than 1: %d", tmpExitCode))
	}

	Exit = origOsExit
}

// Should be used when our utils.Log* functions are used to print
func TestLogRegex(t *testing.T, regexp string, fn testable) {
	HandleExit(func() {
		out := getPrintOutput(fn)

		LogInfo(out)

		assert.Regexp(t, regexp, out)
	})
}

// Should be used when the log is created by fmt.Print
func TestPrintRegex(t *testing.T, regexp string, fn testable) {
	HandleExit(func() {
		out := getPrintOutput(fn)

		LogInfo(out)

		// If the regex string doesn't contain line break, we should replace them, because multiline regex matches don't work here!
		if !strings.Contains(regexp, "\n") {
			out = strings.Replace(out, "\n", " ", -1)
		}

		assert.Regexp(t, regexp, out)
	})
}
