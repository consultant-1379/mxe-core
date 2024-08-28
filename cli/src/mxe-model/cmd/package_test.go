package cmd

import (
	"testing"

	"mxe.ericsson/utils"
)

func Test_package_AllMissing(t *testing.T) {
	utils.TestLogRegex(t, "required flag\\(s\\) \"privatekey\", \"publickey\", \"source\" not set", func() {
		CmdSetArgs([]string{"package"})
		Execute()
	})
}

func Test_package_PrivateKeyPublicKeyMissing(t *testing.T) {
	utils.TestLogRegex(t, "required flag\\(s\\) \"privatekey\", \"publickey\" not set", func() {
		CmdSetArgs([]string{"package", "--source=imagerecognition"})
		Execute()
	})
}

func Test_package_SourceEmpty(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --source", func() {
		source = ""
		CmdSetArgs([]string{"package",  "--privatekey=privkey", "--publickey=pubkey", "--source"})
		Execute()
	})
}

func Test_package_PrivateKeyEmpty(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --privatekey", func() {
		privateKeyPath = ""
		CmdSetArgs([]string{"package", "--source=imagerecognition", "--publickey=pubkey", "--privatekey"})
		Execute()
	})
}

func Test_package_PublicKeyEmpty(t *testing.T) {
	utils.TestLogRegex(t, "Error: flag needs an argument: --publickey", func() {
		publicKeyPath = ""
		CmdSetArgs([]string{"package", "--source=imagerecognition", "--privatekey=privkey", "--publickey"})
		Execute()
	})
}
