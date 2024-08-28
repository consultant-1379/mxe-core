package cmd

import "os"

const (
	cliName string = "mxe-deploy-init"
)

func GetHome() string {
	home, err := os.UserHomeDir()
	if err != nil {
		panic("User home is not defined")
	}
	return home
}
