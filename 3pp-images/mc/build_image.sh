#!/bin/bash
########################################################################################################
# Script
#   pre-requisite: docker login armdocker.rnd.ericsson.se
#  downloads preconfigured mc version
#######################################################################################################
set -ex 

case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in. /home/user/bin
BASEDIR=$(dirname $SCRIPT)

MODULEROOT=$(dirname $BASEDIR)
source "${MODULEROOT}/base_image.sh"

MC_VERSION="RELEASE.2024-04-18T16-45-29Z"
MC_DOWNLOAD_URL="https://dl.min.io/client/mc/release/linux-amd64/archive/mc.${MC_VERSION}"
IMAGE=armdocker.rnd.ericsson.se/proj-mxe/minio/mc:${MC_VERSION}-ubuntu-${LATEST_DATE_TAG}

DOCKER_BUILDKIT=1 docker build --no-cache \
        --build-arg BASE_IMAGE=${BASE_IMAGE} \
        --build-arg MC_DOWNLOAD_URL=${MC_DOWNLOAD_URL} \
        --build-arg MC_VERSION=${MC_VERSION} -t ${IMAGE} \
        -f ${BASEDIR}/Dockerfile ${BASEDIR} --progress plain
buildStatus=$?

if [[ $buildStatus == 0 ]]; then
        docker push ${IMAGE}
        pushed=$?
        if [[ $pushed -ne 0 ]]; then
                exit $pushed
        fi
else
        echo "Docker build failed"
fi