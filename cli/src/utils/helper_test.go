package utils

import (
	"fmt"
	"runtime"
	"testing"
)

func TestRun(t *testing.T) {
	cases := []struct {
		command, expected string
	}{
		{"echo apple", "apple"},
		{"echo test", "test"},
		//{"", ""},
	}
	for _, c := range cases {
		out, err := Run(CmdParams{Cmd: c.command})
		if out != c.expected {
			t.Errorf("RunCommand(%q), output: %q, error: %q, expected %q", c.command, out, err, c.expected)
		}
	}
}

func TestGetUsername(t *testing.T) {
	if GetUsername() == "" {
		t.Errorf("$USER variable is empty.")
	}
}

func TestGetBashEnv(t *testing.T) {
	if GetBashEnv("USER") == "" {
		t.Errorf("$USER variable is empty.")
	}
}

func TestRunBashCommandFromDir(t *testing.T) {
	dir := []string{
		"/home",
		"/etc",
		"/var",
	}
	fmt.Println(dir)
	for _, d := range dir {
		out, err := RunBashCommandFromDir("pwd", d)
		if out != d {
			t.Errorf("RunBashCommandFromDir(\"pwd\", %q), output: %q, error: %q, expected %q", d, out, err, d)
		}
	}
}

func TestWindowsPathToLinux(t *testing.T) {
	var dir []struct {
		dir, expected string
	}
	if runtime.GOOS != "windows" {
		dir = []struct {
			dir, expected string
		}{
			{"/home/lukacsg", "/home/lukacsg"},
			{"/etc", "/etc"},
			{"/var", "/var"},
		}
	} else {
		dir = []struct {
			dir, expected string
		}{
			{"C:\\User", "/mnt/c/User"},
			{"C:\\Windows", "/mnt/c/Windows"},
		}
	}
	//fmt.Println(dir)
	for _, d := range dir {
		out := WindowsPathToLinux(d.dir)
		if out != d.expected {
			t.Errorf("WindowsPathToLinux(%q), output: %q, expected %q", d.dir, out, d.expected)
		}
	}
}

func ExampleRunBashCommand() {
	out, _ := RunBashCommand("echo run bash test")
	fmt.Println(out)
	// Output: run bash test
}
