package utils

import (
	"fmt"
)

//func Prefix() string {
//	return "[" + time.Now().Format("2006-01-02 15:04:05") + "] "
//}

func LogDebug(msg string) {
	if Verbose {
		fmt.Println("Verbose: " + msg)
	}
}

func LogError(msg string) {
	fmt.Println("Error: " + msg)
}

func LogSuccess(msg string) {
	fmt.Println("Success: " + msg)
}

func LogInfo(msg string) {
	fmt.Println(msg)
}

func LogNotice(msg string) {
	fmt.Println(msg)
}

func LogWarn(msg string) {
	fmt.Println("Warning: " + msg)
}
