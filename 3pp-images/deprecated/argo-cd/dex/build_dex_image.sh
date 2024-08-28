#!/bin/bash
########################################################################################################
# Script
#   a)clones dex release tag,
#   b) builds docker image for dex using ubuntu:20.10 base image
#   c) pushes image to armdocker
#######################################################################################################


case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in. /home/user/bin
BASEDIR=$(dirname $SCRIPT)

MODULEROOT=$(dirname $(dirname $BASEDIR))
source "${MODULEROOT}/base_image.sh"
echo "Base image is  ${BASE_IMAGE}"

REPO_URL=git@github.com:dexidp/dex.git
RELEASE_TAG=v2.27.0
IMAGE=armdocker.rnd.ericsson.se/proj-mxe/dexidp/dex:v2.27.0-ubuntu-${LATEST_DATE_TAG}

if [[ -d "${BASEDIR}/dex" ]]; then
    rm -rf "${BASEDIR}/dex"
fi

git clone -b "${RELEASE_TAG}" "${REPO_URL}" "${BASEDIR}/dex"

DOCKER_BUILDKIT=1 docker build --no-cache --build-arg BASE_IMAGE=${BASE_IMAGE} -t ${IMAGE} -f ${BASEDIR}/Dockerfile ${BASEDIR}/dex --progress plain
buildStatus=$?

if [[ $buildStatus == 0 ]]; then
    docker push ${IMAGE}
    pushed=$?
    if [[ $pushed == 0 ]] && [[ -d "${BASEDIR}/dex" ]]; then
        rm -rf "${BASEDIR}/dex"
    fi
else
    echo "Docker build failed"
fi
