#!/bin/bash
########################################################################################################
# Script
#   a)clones argo-cd release tag,
#   b)sets helm version to 3.2.4 and
#   c) builds docker image for argo-cd using ubuntu:20.10 base image
#   d) pushes image to armdocker
#######################################################################################################
set -e

case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in. /home/user/bin
BASEDIR=$(dirname $SCRIPT)

MODULEROOT=$(dirname $(dirname $BASEDIR))
source "${MODULEROOT}/base_image.sh"
echo "Base image is  ${BASE_IMAGE}"

REPO_URL=https://github.com/argoproj/applicationset.git
RELEASE_TAG=v0.4.1
IMAGE=armdocker.rnd.ericsson.se/proj-mxe/argoproj/argocd-applicationset:v0.4.1-ubuntu-${LATEST_DATE_TAG}


if [[ -d "${BASEDIR}/applicationset" ]]; then
    rm -rf "${BASEDIR}/applicationset"
fi

git clone -b "${RELEASE_TAG}" "${REPO_URL}" "${BASEDIR}/applicationset" &&
sed -i -e "s#docker.io/library/ubuntu:21.10#$BASE_IMAGE#g" "${BASEDIR}/applicationset/Dockerfile"

DOCKER_BUILDKIT=1 docker build --no-cache -t ${IMAGE} ${BASEDIR}/applicationset --progress plain
buildStatus=$?

if [[ $buildStatus == 0 ]]; then
    docker push ${IMAGE}
    pushed=$?
    if [[ $pushed == 0 ]] && [[ -d "${BASEDIR}/applicationset" ]]; then
        rm -rf "${BASEDIR}/applicationset"
    fi
else
    echo "Docker build failed"
fi
