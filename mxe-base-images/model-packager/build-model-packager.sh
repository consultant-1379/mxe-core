#!/bin/bash
set -x 
set -e 

echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo " This script is deprecated. Use bob/bob image:build-packager instead"
echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
exit 1

REPO_URL=https://github.com/GoogleContainerTools/kaniko.git
KANIKO_VERSION=${KANIKO_VERSION:-v1.7.0}
CRANE_VERSION=${CRANE_VERSION:-v0.8.0}
RELEASE_VERSION=${RELEASE_VERSION:-1.7.0-20220314}
COMMON_BASE_OS_VERSION=${COMMON_BASE_OS_VERSION:-3.47.0-10}
DEVEL_VERSION=${DEVEL_VERSION:-1.27-latest}
IMAGE=armdocker.rnd.ericsson.se/proj-mxe/kaniko/model-packager:${RELEASE_VERSION}
BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in. /home/user/bin
BASEDIR=$(dirname $SCRIPT)

MODULEROOT=$(dirname $BASEDIR)

if [[ -d "${BASEDIR}/kaniko" ]]; then
  echo "Removing ${BASEDIR}/kaniko directory as it already exists"
  rm -rf "${BASEDIR}/kaniko"
fi

echo "Cloning kaniko repository"
git clone "${REPO_URL}" -b "${KANIKO_VERSION}" "${BASEDIR}/kaniko"
DOCKER_BUILDKIT=1 docker build -t ${IMAGE} -f ${BASEDIR}/Dockerfile --build-arg COMMON_BASE_OS_VERSION=${COMMON_BASE_OS_VERSION} --build-arg BUILD_DATE=${BUILD_DATE} --build-arg=DEVEL_VERSION=${DEVEL_VERSION} --build-arg KANIKO_VERSION=${KANIKO_VERSION} --build-arg CRANE_VERSION=${CRANE_VERSION} --build-arg RELEASE_VERSION=${RELEASE_VERSION} ${BASEDIR}
docker push ${IMAGE}
pushed=$?

if [[ $pushed == 0 ]] && [[ -d "${BASEDIR}/kaniko" ]]; then
    rm -rf "${BASEDIR}/kaniko"
fi
