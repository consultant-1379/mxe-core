#!/bin/bash
########################################################################################################
# Script
#   pre-requisite: docker login armdocker.rnd.ericsson.se
#   a) clones gitea release tag,
#   b) builds docker image for gitea using ubuntu as base image
#   c) pushes image to armdocker
#######################################################################################################
set -x 
set -e 

case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in. /home/user/bin
BASEDIR=$(dirname $SCRIPT)

MODULEROOT=$(dirname $BASEDIR)
source  "${MODULEROOT}/base_image.sh"
echo "Base image is  ${BASE_IMAGE}"

REPO_URL=https://github.com/go-gitea/gitea.git
RELEASE_TAG=${RELEASE_TAG:-v1.20.2}
IMAGE=armdocker.rnd.ericsson.se/proj-mxe/gitea/mxe-gitea:${RELEASE_TAG}-ubuntu-${LATEST_DATE_TAG}-rootless


if [[ -d "${BASEDIR}/gitea" ]]; then
    rm -rf "${BASEDIR}/gitea"
fi

git clone "${REPO_URL}" "${BASEDIR}/gitea"
DOCKER_BUILDKIT=1 docker build --no-cache --build-arg BASE_IMAGE=${BASE_IMAGE} --build-arg GITEA_VERSION=${RELEASE_TAG} --build-arg TAGS="bindata sqlite sqlite_unlock_notify" -t ${IMAGE} -f ${BASEDIR}/Dockerfile.rootless ${BASEDIR}/gitea
docker push ${IMAGE}
pushed=$?

if [[ $pushed == 0 ]] && [[ -d "${BASEDIR}/gitea" ]]; then
    rm -rf "${BASEDIR}/gitea"
fi
