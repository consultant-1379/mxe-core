#!/bin/bash
########################################################################################################
# Script
#   a)clones argo-workflow release tag,
#   b) runs make targets 
#   c) pushes image to armdocker
#######################################################################################################
set -ex

# This script is deprecated.
# added exit 1 to avoid accidental execution
# images built in ml-pipeline microservice have to be reused in MXE
# see 3pp-images/argo-workflows/copy_service_image.sh


case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in. /home/user/bin
BASEDIR=$(dirname $SCRIPT)

MODULEROOT=$(dirname $BASEDIR)

MODULEROOT=$(dirname $BASEDIR)
source  "${MODULEROOT}/base_image.sh"
echo "Latest alpine image tag is  ${LATEST_ALPINE_TAG}"

## Before running this script, make sure that the source image does not need to be updated.
REPO_URL=https://github.com/argoproj/argo-workflows.git
RELEASE_TAG=v3.3.8
IMAGE=armdocker.rnd.ericsson.se/proj-mxe/quay.io/argoproj/argoexec:${RELEASE_TAG}-alpine-${LATEST_ALPINE_TAG}
MAKEFILE_EXEC_TARGET=argoexec-image
ALPINE_SOURCE_VERSION="3"
ALPINE_TARGET_VERSION="$LATEST_ALPINE_TAG"


cleanup() {
if [[ -d "${BASEDIR}/argo-workflows" ]]; then
    rm -rf "${BASEDIR}/argo-workflows"
fi
}

cleanup
git clone -b "${RELEASE_TAG}" "${REPO_URL}" "${BASEDIR}/argo-workflows" 

cd ${BASEDIR}/argo-workflows
echo $PWD 
sed -i "s#docker buildx build#docker buildx build --load#g" Makefile
sed -i "s#alpine:$ALPINE_SOURCE_VERSION#armdockerhub.rnd.ericsson.se/alpine:$ALPINE_TARGET_VERSION#g" Dockerfile
sed -i "s#golang:1.17#armdockerhub.rnd.ericsson.se/golang:1.19.7#g" Dockerfile
mkdir dist/
export DOCKER_BUILDKIT=1
make ${MAKEFILE_EXEC_TARGET} 2>&1
docker tag quay.io/argoproj/argoexec:${RELEASE_TAG} ${IMAGE}

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

cd ${OLDPWD}