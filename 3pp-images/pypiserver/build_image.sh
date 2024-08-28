#! /usr/bin/env bash

set -x
set -e

CWD=$(pwd)

case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in. /home/user/bin
BASEDIR=$(dirname $SCRIPT)
MODULEROOT=$(dirname $BASEDIR)
source "${MODULEROOT}/base_image.sh"

cleanup() {
if [[ -d "${BASEDIR}/pypiserver" ]]; then
    rm -rf "${BASEDIR}/pypiserver"
fi
}

ALPINE_BASE_IMAGE="python:${PYTHON_ALPINE_TAG}"
ALPINE_BASE_IMAGE_ARMPROXY=$ARMDOCKER_PROXY/$ALPINE_BASE_IMAGE
ALPINE_OS_PATCH_VERSION="${LATEST_ALPINE_TAG}"
REPO_URL=https://github.com/pypiserver/pypiserver.git
VERSION=2.0.1
BUILD_NO=
IMAGE="armdocker.rnd.ericsson.se/proj-mxe/pypiserver/pypiserver:${VERSION}-alpine${ALPINE_OS_PATCH_VERSION}"
if [[ -n "${BUILD_NO}" ]]; then
    IMAGE="${IMAGE}-${BUILD_NO}"
fi
SRC_ALPINE_BASE_IMAGE="python:3.8-alpine3.12"

cleanup

git clone -b "v${VERSION}" "${REPO_URL}" "${BASEDIR}/pypiserver"

sed -i "s#${SRC_ALPINE_BASE_IMAGE}#${ALPINE_BASE_IMAGE_ARMPROXY}#g" "${BASEDIR}/pypiserver/Dockerfile"

# pypi server image already contains 9898 user, update docker image so that process will run as 9898 user
sed -i '$a\'"USER 9898"'' "${BASEDIR}/pypiserver/Dockerfile"

sed -i "s#21.12.0#23.9.0#g" "${BASEDIR}/pypiserver/docker/docker-requirements.txt"

DOCKER_BUILDKIT=1 docker build -t ${IMAGE} "${BASEDIR}/pypiserver"
buildStatus=$?

if [[ $buildStatus == 0 ]]; then
    docker push ${IMAGE}
    pushed=$?
    if [[ $pushed == 0 ]]; then
        cleanup
    fi
else
    echo "Docker build failed"
fi
