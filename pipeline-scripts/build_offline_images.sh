#!/usr/bin/env bash 
set -eux 
# Utility script to allow building offline images locally

export RELEASE_CANDIDATE_VERSION="2.3.0-176"
export RELEASE_VERSION="${RELEASE_CANDIDATE_VERSION/-/+}"
export NEXT_VERSION_PREFIX="2.4.0"
export RELEASE_ARTIFACTS_FOLDER="release-${RELEASE_VERSION}"
export API_TOKEN=$DOCKER_API_TOKEN
export USER=mxecifunc

./bob/bob clean
./bob/bob init-release
./bob/bob pra-release:init
./bob/bob package-offline-images