package main

import (
	"fmt"

	"mxe.ericsson/mxe-training/cmd"
)

func main() {
	defer func() {
		if r := recover(); r != nil {
			fmt.Printf("Exiting because of error: %s\n", r)
		}
	}()

	cmd.Execute()
}
