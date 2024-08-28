#!/bin/bash
#######################################################################################################################################
# Script
#   a) clones ha-proxy docker repo, resets repo to the commit hash corresponding to the image version we are interested in
#   b) builds docker image for ha-proxy using ubuntu:20.10 base image
#   c) pushes image to armdocker
#######################################################################################################################################
set -x 
set -e 

CWD=$(pwd)

case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in. /home/user/bin
BASEDIR=$(dirname $SCRIPT)

MODULEROOT=$(dirname $(dirname $BASEDIR))
source "${MODULEROOT}/base_image.sh"
echo "Base image is  ${BASE_IMAGE}"

REPO_URL=https://github.com/docker-library/haproxy.git
COMMIT_HASH=e678a5620de6784d8dbe42d1c81f5db47f9e8cbc
HA_PROXY_VERSION=2.6
HA_PROXY_PATCH_VERSION=9
IMAGE=armdocker.rnd.ericsson.se/proj-mxe/haproxy:${HA_PROXY_VERSION}.${HA_PROXY_PATCH_VERSION}-ubuntu-${LATEST_DATE_TAG}

if [[ -d "${BASEDIR}/haproxy" ]]; then
    rm -rf "${BASEDIR}/haproxy"
fi

# clone the git repository into the current directory
git clone ${REPO_URL} "${BASEDIR}/haproxy"

cd "${BASEDIR}/haproxy"

# checkout a commit using its hash
git checkout ${COMMIT_HASH}

# hard reset repository
git reset --hard

cd ${CWD}

DOCKER_BUILDKIT=1 docker build --no-cache --build-arg BASE_IMAGE=${BASE_IMAGE} -t ${IMAGE} -f "${BASEDIR}/Dockerfile" "${BASEDIR}/haproxy/${HA_PROXY_VERSION}/" --progress plain
buildStatus=$?

if [[ $buildStatus == 0 ]]; then
    docker push ${IMAGE}
    pushed=$?
    if [[ $pushed == 0 ]] && [[ -d "${BASEDIR}/haproxy" ]]; then
        rm -rf "${BASEDIR}/haproxy"
    fi
else
    echo "Docker build failed"
fi
