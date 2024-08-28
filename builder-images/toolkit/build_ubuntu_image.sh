#!/bin/bash
#############################################################################################################################
# Script
#  Builds a container consisting of stable versions of kubectl,helm, helmfile, yq, jq, vcluster, vcerts...
#  This image can be used in ruleset as docker-image to run common shell processes
#  This is useful because sometimes we want to use newer versions of these tools than the one supplied by ADP
#  For example, ADP gives yq version 2.x but we want to use 4.x which has lot of improvements
#############################################################################################################################
#  WARNING:  It is not advised to use this image to create deliverables. For those purposes use the official ADP images
#############################################################################################################################

set -e

case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in. /home/user/bin
BASEDIR=$(dirname $SCRIPT)
MODULEROOT=$(dirname $BASEDIR)
REPOROOT=$(dirname $MODULEROOT)

source "${REPOROOT}/3pp-images/base_image.sh"
echo "Base image is  ${BASE_IMAGE}"

RELEASE_TAG=$(date +%Y%m%d)

IMAGE=armdocker.rnd.ericsson.se/proj-mxe-ci-internal/mxe-toolkit:${RELEASE_TAG}-ubuntu-${LATEST_DATE_TAG}

DOCKER_BUILDKIT=1 docker build --no-cache \
    --build-arg BASE_IMAGE=${BASE_IMAGE} -t ${IMAGE} \
    -f ${BASEDIR}/Dockerfile ${BASEDIR} --progress plain
buildStatus=$?

if [[ $buildStatus == 0 ]]; then
    docker push ${IMAGE}
    pushed=$?
else
    echo "Docker build failed"
fi
