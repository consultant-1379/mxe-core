package main

import (
	"mxe.ericsson/mxe-generate/cmd"
	"mxe.ericsson/mxe-generate/utils/errors"
)

func main() {
	err := cmd.NewCommand().Execute()
	errors.CheckError(err)
}
