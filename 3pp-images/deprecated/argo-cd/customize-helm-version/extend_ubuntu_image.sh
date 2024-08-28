#!/bin/bash

set -e

case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in. /home/user/bin
BASEDIR=$(dirname $SCRIPT)

MODULEROOT=$(dirname $(dirname $(dirname $BASEDIR)))
source "${MODULEROOT}/base_image.sh"
echo "Base image is  ${BASE_IMAGE}"

REPO_URL=https://github.com/argoproj/argo-cd.git
RELEASE_TAG=v2.0.3
BASE_ARGOCD_IMAGE=armdocker.rnd.ericsson.se/proj-mxe/argoproj/argocd:v2.0.3-ubuntu-${LATEST_DATE_TAG}
IMAGE=armdocker.rnd.ericsson.se/proj-mxe/argoproj/argocd:v2.0.3-02-ubuntu-${LATEST_DATE_TAG}
HELM_VERSION=3.6.2 

DOCKER_BUILDKIT=1 docker build --no-cache --build-arg HELM_VERSION=${HELM_VERSION} --build-arg BASE_ARGOCD_IMAGE=${BASE_ARGOCD_IMAGE} -t ${IMAGE} -f ${BASEDIR}/Dockerfile ${BASEDIR} --progress=plain
buildStatus=$? 


if [[ $buildStatus == 0 ]]; then
    docker push ${IMAGE}
    pushed=$?
    if [[ $pushed == 0 ]]; then
        echo "image is pushed successfully, helm version is modified"
    fi
else
    echo "Docker push failed"
fi
