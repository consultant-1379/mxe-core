#!/bin/bash
########################################################################################################
# Script
#   a)clones argo-cd release tag,
#   b)sets helm version to 3.2.4 and
#   c) builds docker image for argo-cd using ubuntu:20.10 base image
#   d) pushes image to armdocker
#######################################################################################################
set -ex

case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in. /home/user/bin
BASEDIR=$(dirname $SCRIPT)

MODULEROOT=$(dirname $(dirname $BASEDIR))
source "${MODULEROOT}/base_image.sh"
echo "Base image is  ${BASE_IMAGE}"

REPO_URL=https://github.com/argoproj/argo-cd.git
RELEASE_TAG=v2.11.0
BUILD_NO=""
IMAGE=armdocker.rnd.ericsson.se/proj-mxe/argoproj/argocd:${RELEASE_TAG}-ubuntu-${LATEST_DATE_TAG}
## Source version is from https://github.com/argoproj/argo-cd/blob/master/hack/tool-versions.sh#L14
SOURCE_HELM_VERSION="3.14.3"
# Target version is the version we want to set as per ADP images like py3helmkubebuilder
# Set to same as value of HELM_VERSION env in ruleset2.0.yaml 
TARGET_HELM_VERSION="3.13.0"

# if Build no is set, append it to the image name else leave as is
# Build no is just a unique identifier in case we need to rebuild the image using same source code multiple times
if [[ -n "$BUILD_NO" ]]; then 
    IMAGE="${IMAGE}-${BUILD_NO}"
fi
echo "Image name is ${IMAGE}"

if [[ -d "${BASEDIR}/argo-cd" ]]; then
    rm -rf "${BASEDIR}/argo-cd"
fi

git clone -b "${RELEASE_TAG}" "${REPO_URL}" "${BASEDIR}/argo-cd"

sed -i "s#helm3_version=${SOURCE_HELM_VERSION}#helm3_version=${TARGET_HELM_VERSION}#g" "${BASEDIR}/argo-cd/hack/tool-versions.sh"

wget -O "${BASEDIR}/argo-cd/hack/installers/checksums/helm-v${TARGET_HELM_VERSION}-linux-amd64.tar.gz.sha256" https://get.helm.sh/helm-v${TARGET_HELM_VERSION}-linux-amd64.tar.gz.sha256sum 

DOCKER_BUILDKIT=1 docker build --no-cache \
    --build-arg BASE_IMAGE=${BASE_IMAGE} \
    --build-arg GIT_TREE_STATE=clean \
    -t ${IMAGE} -f ${BASEDIR}/Dockerfile ${BASEDIR}/argo-cd --progress plain
buildStatus=$?

if [[ $buildStatus == 0 ]]; then
    docker push ${IMAGE}
    pushed=$?
    if [[ $pushed == 0 ]] && [[ -d "${BASEDIR}/argo-cd" ]]; then
        rm -rf "${BASEDIR}/argo-cd"
    fi
else
    echo "Docker build failed"
fi
