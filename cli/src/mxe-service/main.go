package main

import (
	"fmt"
	"os"

	"mxe.ericsson/mxe-service/cmd"
)

func main() {
	defer func() {
		if r := recover(); r != nil {
			fmt.Printf("Error: %s\n", r)
			os.Exit(1)
		}
	}()

	cmd.Execute()
}
