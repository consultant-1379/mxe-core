#GOPATH := $(CURDIR)/../.bob/gopath
VERSION := $(shell cat ../VERSION_PREFIX)

all: mxe-model mxe-service mxe-training utils

clean:
	GOPATH=$(GOPATH) go clean -x -modcache

mxe-model mxe-service mxe-training utils:
	cd src/ && CGO_ENABLED=0 GOPATH=$(GOPATH) GOARCH=amd64 GOOS=linux \
		go install -a -tags netgo -v -modcacherw -ldflags "-linkmode external -extldflags -static -s -w -X mxe.ericsson/utils.MXE_CLI_VERSION=$(VERSION)" ./$@

linux:
	cd src/ && CGO_ENABLED=0 GOPATH=$(GOPATH) GOARCH=amd64 GOOS=$@ \
		go install -a -tags netgo -v -modcacherw -ldflags "-linkmode external -extldflags -static -s -w -X mxe.ericsson/utils.MXE_CLI_VERSION=$(VERSION)" ./...

windows:
	cd src/ && CGO_ENABLED=0 GOPATH=$(GOPATH) GOARCH=amd64 GOOS=$@ \
		go install -a -tags netgo -v -modcacherw -ldflags "-X mxe.ericsson/utils.MXE_CLI_VERSION=$(VERSION)" ./...

macos:
	cd src/ && CGO_ENABLED=0 GOPATH=$(GOPATH) GOARCH=amd64 GOOS=darwin \
		go install -a -tags netgo -v -modcacherw -ldflags "-X mxe.ericsson/utils.MXE_CLI_VERSION=$(VERSION)" ./...

test:
	cd src/ && go test -count=1 -v -modcacherw ./utils ./mxe-model/cmd ./mxe-training/cmd ./mxe-service/cmd

testsum:
	go get gotest.tools/gotestsum
	cd src/ && gotestsum --format short-verbose ./...

.PHONY: all mxe-model mxe-service mxe-training utils test
