#!/usr/bin/env bash 

########################################################################################################
# Script
#   a) requires eric-aiml-pipeline drop image tag
#   b) Pulls workflow controller, executor and cli images 
#   c) Retags them and pushes to armdocker proj-mxe location
#######################################################################################################
set -ex

case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in.
BASEDIR=$(dirname $SCRIPT)

MODULEROOT=$(dirname $BASEDIR)

# dynamic params
ML_PIPELINE_DROP_TAG="1.1.0-hb7ebb42"

# find these from docker image labels
# sample command:
# IMAGE="armdocker.rnd.ericsson.se/proj-mlops-ci-internal/components/eric-aiml-pipeline-argoexec:${ML_PIPELINE_DROP_TAG}"
# docker pull ${IMAGE}; docker inspect ${IMAGE} | jq '.[].Config.Labels' 

ARGO_WORKFLOW_VERSION="v3.4.9"  # Obtained by inspecting source docker image label com.ericsson.product-3pp-version
CBO_VERSION="6.14.0-10"   # Obtained by inspecting source docker image label com.ericsson.base-image.product-version

# derived params
IMAGES=("argoexec" "argocli" "workflow-controller")
SOURCE_PREFIX="eric-aiml-pipeline"
SOURCE_IMAGE_REPO="armdocker.rnd.ericsson.se/proj-mlops-ci-internal"

TARGET_IMAGE_PREFIX="armdocker.rnd.ericsson.se/proj-mxe/quay.io/argoproj"

for image in "${IMAGES[@]}"
do
    SOURCE_IMAGE="${SOURCE_IMAGE_REPO}/${SOURCE_PREFIX}-${image}:${ML_PIPELINE_DROP_TAG}"
    TARGET_IMAGE="${TARGET_IMAGE_PREFIX}/${image}:${ARGO_WORKFLOW_VERSION}-cbos-${CBO_VERSION}"
    echo "Pulling ${SOURCE_IMAGE}"
    docker pull ${SOURCE_IMAGE}
    docker tag ${SOURCE_IMAGE} ${TARGET_IMAGE}
    echo "Pushing ${TARGET_IMAGE}"
    docker push ${TARGET_IMAGE}
done
