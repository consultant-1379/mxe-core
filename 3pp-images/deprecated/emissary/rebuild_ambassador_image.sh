#!/usr/bin/env bash

set -ex 

case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in. /home/user/bin
BASEDIR=$(dirname $SCRIPT)

MODULEROOT=$(dirname $BASEDIR)

cleanup() {
if [[ -d "${BASEDIR}/docker-alpine-glibc" ]]; then
    rm -rf "${BASEDIR}/docker-alpine-glibc"
fi

if [[ -d "${BASEDIR}/emissary" ]]; then
    rm -rf "${BASEDIR}/emissary"
fi

}

ALPINE_SOURCE_REPO_URL=git@github.com:Docker-Hub-frolvlad/docker-alpine-glibc.git
ALPINE_REPO_TAG=alpine-3.15_glibc-2.34
ORIGINAL_SOURCE_IMAGE=alpine:3.12 
LATEST_PATCH_IMAGE=alpine:3.12.8
NEW_IMAGE_SUFFIX=$(echo $LATEST_PATCH_IMAGE | sed "s/:/-/g")

AMBASSADOR_REPO_URL=git@github.com:emissary-ingress/emissary.git
AMBASSADOR_RELEASE_VERSION=1.13.10
AMBASSADOR_IMAGE=armdocker.rnd.ericsson.se/proj-mxe/datawire/ambassador:${AMBASSADOR_RELEASE_VERSION}-${NEW_IMAGE_SUFFIX}
AMBASSADOR_SOURCE_BASE_IMAGE=docker.io/library/debian:10.7-slim

cleanup

## Rebuild base image used in ambassador with latest alpine patch ${LATEST_PATCH_IMAGE}

git clone -b "${ALPINE_REPO_TAG}" "${ALPINE_SOURCE_REPO_URL}" "${BASEDIR}/docker-alpine-glibc"
sed -i "s/${ORIGINAL_SOURCE_IMAGE}/${LATEST_PATCH_IMAGE}/g" "${BASEDIR}/docker-alpine-glibc/Dockerfile"

docker build -t "docker.io/frolvlad/alpine-glibc:modified-${ALPINE_REPO_TAG}" "${BASEDIR}/docker-alpine-glibc" 

## Rebuild ambassador with above image

git clone -b "v${AMBASSADOR_RELEASE_VERSION}" "${AMBASSADOR_REPO_URL}" "${BASEDIR}/emissary"

cd "${BASEDIR}/emissary"

for file in $(grep -Rl  frolvlad/alpine-glibc:alpine-3.12_glibc-2.32 *); 
do 
    sed -i "s#frolvlad/alpine-glibc:${ALPINE_REPO_TAG}#frolvlad/alpine-glibc:modified-${ALPINE_REPO_TAG}#g" $file 
done

make images 

buildStatus=$?
if [[ $buildStatus == 0 ]]; then
    docker tag ambassador.local/ambassador-ea:latest "${AMBASSADOR_IMAGE}"
    docker push ${AMBASSADOR_IMAGE}
    pushed=$?
    if [[ $pushed == 0 ]]; then
        cleanup
    fi
else
    echo "Docker build failed"
fi

cd ${OLDPWD}
