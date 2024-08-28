package utils

import (
	"bytes"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"strings"
	"text/tabwriter"

	"github.com/mitchellh/go-homedir"
	"github.com/nogoegst/wslpath"
)

var Exit = os.Exit
var Verbose bool

var mxeDir string
var mxeDirLinux string

func init() {
	setMxeDirs()
}

func setMxeDirs() {
	var home, _ = homedir.Dir()
	mxeDir = fmt.Sprintf("%s%c%s", home, os.PathSeparator, MXE_FOLDER)
	mxeDirLinux = fmt.Sprintf("%s/%s", GetBashEnv("HOME"), MXE_FOLDER)
}

func MxeDir() string {
	if mxeDir == "" {
		setMxeDirs()
	}
	return mxeDir
}

func MxeDirLinux() string {
	if mxeDirLinux == "" {
		setMxeDirs()
	}
	return mxeDirLinux
}

func GetBashEnv(env string) string {

	if !strings.HasPrefix(env, "$") {
		env = "$" + env
	}

	cmdStr := fmt.Sprintf("echo -n %s", env)
	cmd := exec.Command("bash", "-c", cmdStr)
	var outb bytes.Buffer
	cmd.Stdout = &outb
	cmd.Run()

	return outb.String()

	LogInfo("BASHENVHOME =" + outb.String() + "=")

	env, _ = Run(CmdParams{Cmd: fmt.Sprintf("echo -n %s", env)})
	env = strings.TrimSpace(env)
	LogInfo("ORIG BASHENVHOME =" + env + "=")

	return env
}

/**
Get username from command line
*/
func GetUsername() string {
	user, _ := RunBashCommand("echo $USER")
	// LogInfo(fmt.Sprintf("Logged in as %s", user))
	return user
}

func printTableRowAsseble(row []string) string {
	var result = ""
	for _, element := range row {
		result = result + element + string('\t')
	}
	// Removing the last padding
	return strings.TrimSuffix(result, string('\t'))
}

func PrintTable(header []string, content [][]string) {
	var padchar byte = ' '
	w := new(tabwriter.Writer)
	w.Init(os.Stdout, 0, 0, 2, padchar, 0)
	fmt.Fprintln(w, printTableRowAsseble(header))
	for _, row := range content {
		fmt.Fprintln(w, printTableRowAsseble(row))
	}
	fmt.Fprintln(w)
	w.Flush()
}

func RunBashCommand(command string) (string, string) {
	// return runCommand("bash", append([]string{"-ci"}, strings.Split(command, " ")...)...)
	//return RunCommand("bash", []string{"-c", command}...)
	return Run(CmdParams{Cmd: command})
}

func RunBashCommandFromDir(command string, dir string) (string, string) {
	//return RunCommand("bash", []string{"-c", "cd " + dir + "; " + command}...)
	return Run(CmdParams{Cmd: command, Dir: dir})
}

// Alternative Run function. Example usage:
//   utils.Run(utils.CmdParams{Dir:"/work/dir", Win:true, Fail:true,
//     Cmd:"echo this is the command to run"})
// Default bool value is false, default string is "" (empty-string)

type CmdParams struct {
	Cmd     string            // the actual command
	EnvVars map[string]string // separate field for env vals
	Dir     string            // workdir, set empty string ("") if workdir does not matter
	Pwd     string            // sudo password for bash commands, set empty string ("") if not sudo command
	Win     bool              // if command should be run in windows (else in bash) (only if running on windows)
	NoFail  bool              // if error should be ignored (if false, panic() is called when command returns error)
	ErrMsg  string            // error message to output if error happens
	OutErr  bool              // if command stdout/stderr should be output as error message (even if NoFail is true)
	Verbose bool              // if command stdout/stderr should be output as debug message
}

func Run(p CmdParams) (string, string) {

	// constructing command object
	var cmdObj *exec.Cmd
	cmdPrefix := ""
	// TODO: maybe sanity-check Dir, Pwd, etc. so it does not contain commands
	if p.Dir != "" {
		cmdPrefix += "cd \"" + p.Dir + "\"; "
	}
	if p.Win && runtime.GOOS == "windows" {
		for k, v := range p.EnvVars {
			os.Setenv(k, v)
		}
		cmdObj = exec.Command("powershell", []string{cmdPrefix + p.Cmd}...)
	} else {
		if p.Pwd != "" {
			cmdPrefix += "echo \"" + p.Pwd + "\" | sudo -S "
		}
		exports := ""

		if runtime.GOOS != "windows" {
			exports += fmt.Sprintf("export PATH=%s:$PATH && ", MxeDirLinux())
		}
		if runtime.GOOS == "windows" {
			exports += fmt.Sprintf("export PATH=\"%s:$PATH\" && ", MxeDirLinux())
		}

		for k, v := range p.EnvVars {
			exports += fmt.Sprintf("export %s=%s && ", k, v)
		}
		cmdPrefix += exports
		// NOTE: bash -ci loads bashrc on wls, but on linux bash commands will be stopped and sent to background
		cmdObj = exec.Command("bash", []string{"-c", cmdPrefix + p.Cmd}...) //"\"" + p.Cmd + "\""
	}

	if Verbose {
		if p.Pwd != "" {
			cmdPrefix = strings.Replace(cmdPrefix, p.Pwd, "REDACTED", -1)
			p.Pwd = "REDACTED" // don't print pwd
		}

		LogDebug(fmt.Sprintf("Command prefix: %s\n", cmdPrefix))
		LogDebug(fmt.Sprintf("Command: %+v\n", p))

	}

	//var cmdErr error
	var outb, errb bytes.Buffer
	cmdObj.Stdout, cmdObj.Stderr = &outb, &errb
	cmdErr := cmdObj.Run()
	// remove null characters (windows command "bash" had output in which every 2nd char was null)
	errs := strings.TrimSpace(strings.Replace(errb.String(), "\x00", "", -1))
	outs := strings.TrimSpace(strings.Replace(outb.String(), "\x00", "", -1))

	if Verbose && len(outs) > 0 {
		LogDebug(outs)
	}

	if cmdErr != nil {
		cmdErrs := strings.TrimSpace(strings.Replace(cmdErr.Error(), "\x00", "", -1))
		if p.NoFail {
			if Verbose {
				LogDebug("Command failed. Continuing...")
			}
			if Verbose && len(errs) > 0 && !p.OutErr {
				LogDebug(errs)
			}
			if Verbose && len(cmdErrs) > 0 {
				LogDebug(cmdErrs)
			}
			if p.OutErr && len(outs) > 0 {
				LogWarn(outs)
			}
			if p.OutErr && len(errs) > 0 {
				LogWarn(errs)
			}
			if p.ErrMsg != "" {
				LogWarn(p.ErrMsg)
			}
		} else {
			LogError("Command failed, stopping execution...")
			if p.OutErr && len(outs) > 0 {
				LogError(outs)
			}
			if len(errs) > 0 {
				LogError(errs)
			}
			if p.ErrMsg != "" {
				LogError(p.ErrMsg)
			}
			panic(cmdErrs)
		}
	}

	return outs, errs
}

func WindowsPathToLinux(windowsPath string) string {
	if runtime.GOOS != "windows" {
		return windowsPath
	}
	path, _ := wslpath.FromWindows(windowsPath)
	return strings.TrimSpace(path)
}

func CheckBash() {
	Run(CmdParams{Cmd: "test 1", OutErr: true, ErrMsg: "Shell error."})
}

func GetPwd() string {
	ex, err := os.Executable()
	if err != nil {
		panic(err)
	}
	return filepath.Dir(ex)
}
