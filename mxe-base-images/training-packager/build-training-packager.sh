#!/bin/bash
set -x 
set -e 

echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo " This script is deprecated. Use bob/bob image:build-packager instead"
echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
exit 1

RELEASE_VERSION=${RELEASE_VERSION:-1.7.0-20220314}
BASE_IMAGE_TAG=${BASE_IMAGE_TAG:-1.7.0-20220314}
IMAGE=armdocker.rnd.ericsson.se/proj-mxe/kaniko/model-training-packager:${RELEASE_VERSION}

BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in. /home/user/bin
BASEDIR=$(dirname $SCRIPT)

MODULEROOT=$(dirname $BASEDIR)

DOCKER_BUILDKIT=1 docker build -t ${IMAGE} -f ${BASEDIR}/Dockerfile --build-arg BASE_IMAGE_TAG=${BASE_IMAGE_TAG} --build-arg BUILD_DATE=${BUILD_DATE} ${BASEDIR}
docker push ${IMAGE}
pushed=$?

if [[ $pushed == 0 ]]; then
    echo "Successfully pushed ${IMAGE}"
fi
