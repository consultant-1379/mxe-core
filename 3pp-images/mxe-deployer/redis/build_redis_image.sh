#!/bin/bash
#######################################################################################################################################
# Script
#   a) clones redis docker repo, resets repo to the commit hash corresponding to the image version we are interested in
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

REPO_URL=https://github.com/docker-library/redis.git
COMMIT_HASH=d77143afb3dc8d0b05225ab23b001cf6c41e1b62
REDIS_VERSION=7.0
REDIS_PATCH_VERSION=9
IMAGE=armdocker.rnd.ericsson.se/proj-mxe/redis:${REDIS_VERSION}.${REDIS_PATCH_VERSION}-ubuntu-${LATEST_DATE_TAG}


if [[ -d "${BASEDIR}/redis" ]]; then
    rm -rf "${BASEDIR}/redis"
fi

# clone the git repository into the current directory
git clone ${REPO_URL} "${BASEDIR}/redis"

cd "${BASEDIR}/redis"

# checkout a commit using its hash
git checkout ${COMMIT_HASH}

# hard reset repository
git reset --hard

cd ${CWD}
DOCKER_BUILDKIT=1 docker build --no-cache --build-arg BASE_IMAGE=${BASE_IMAGE} -t ${IMAGE} -f "${BASEDIR}/Dockerfile" "${BASEDIR}/redis/${REDIS_VERSION}/" --progress plain
buildStatus=$?

if [[ $buildStatus == 0 ]]; then
    docker push ${IMAGE}
    pushed=$?
    if [[ $pushed == 0 ]] && [[ -d "${BASEDIR}/redis" ]]; then
        rm -rf "${BASEDIR}/redis"
    fi
else
    echo "Docker build failed"
fi
