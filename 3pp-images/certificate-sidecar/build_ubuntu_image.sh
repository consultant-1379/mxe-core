#! /usr/bin/env bash

set -ex

CWD=$(pwd)

case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in. /home/user/bin
BASEDIR=$(dirname $SCRIPT)
MODULEROOT=$(dirname $BASEDIR)
source "${MODULEROOT}/base_image.sh"

INOTIFY_VERSION=3.22.1.0-2

IMAGE=armdocker.rnd.ericsson.se/proj-mxe/certificate-sidecar:ubuntu-${LATEST_DATE_TAG}

DOCKER_BUILDKIT=1 docker build --build-arg BASE_IMAGE=$BASE_IMAGE --build-arg INOTIFY_VERSION=$INOTIFY_VERSION \
    -t ${IMAGE} "${BASEDIR}"

buildStatus=$?

if [[ $buildStatus == 0 ]]; then
    docker push ${IMAGE}
else
    echo "Docker build failed"
fi