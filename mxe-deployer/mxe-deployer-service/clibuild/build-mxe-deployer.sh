#!/usr/bin/env sh
set -ex
TARGETOS=$1
TARGETARCH=$2
VERSION=$3
OUTPUT_DIR=$4

go version
go env | sort
mkdir -p ${OUTPUT_DIR}

if [ ${TARGETOS} = "linux" ]; then
    GO111MODULE="on" CGO_ENABLED=0 GOOS=${TARGETOS} GOARCH=${TARGETARCH} go build -o ${OUTPUT_DIR}/mxe-deploy -a -tags netgo -modcacherw -ldflags "-linkmode external -extldflags -static -s -w -X mxe.ericsson/depmanager/dmserver/client/config.MXE_CLI_VERSION=${VERSION}" ./dmcli
elif [ ${TARGETOS} = "windows" ]; then
    GO111MODULE="on" CGO_ENABLED=0 GOOS=${TARGETOS} GOARCH=${TARGETARCH} go build -o ${OUTPUT_DIR}/mxe-deploy.exe -a -tags netgo -modcacherw -ldflags "-X mxe.ericsson/depmanager/dmserver/client/config.MXE_CLI_VERSION=${VERSION}" ./dmcli
elif [ ${TARGETOS} = "darwin" ]; then
    GO111MODULE="on" CGO_ENABLED=0 GOOS=${TARGETOS} GOARCH=${TARGETARCH} go build -o ${OUTPUT_DIR}/mxe-deploy -a -tags netgo -modcacherw -ldflags "-X mxe.ericsson/depmanager/dmserver/client/config.MXE_CLI_VERSION=${VERSION}" ./dmcli
else
    echo "${TARGETOS} not supported"
fi
