#!/usr/bin/env bash
# This script uploads the 3pp chart to the ARM registry.

set -ex 

if [[ -z $API_TOKEN ]]
then 
    echo "API_TOKEN is not set. Please export API TOKEN for arm.seli.gic.ericsson.se/artifactory "
    exit 1
fi 

# Source helm repository.

## argo-workflows
# SOURCE_HELM_REPO_URL="https://argoproj.github.io/argo-helm"
# SOURCE_HELM_REPO_NAME="argo"
# CHART_NAME="argo-workflows"
# CHART_VERSION="0.32.2"

## seldon-core
# SOURCE_HELM_REPO_URL="https://storage.googleapis.com/seldon-charts"
# SOURCE_HELM_REPO_NAME="seldon-core"
# CHART_NAME="seldon-core-operator"
# CHART_VERSION="1.17.1"

## argo-cd
SOURCE_HELM_REPO_URL="https://argoproj.github.io/argo-helm"
SOURCE_HELM_REPO_NAME="argo"
CHART_NAME="argo-cd"
CHART_VERSION="6.7.18"

## gitea
#SOURCE_HELM_REPO_URL="https://dl.gitea.io/charts/"
#SOURCE_HELM_REPO_NAME="gitea"
#CHART_NAME="gitea"
#CHART_VERSION=5.0.9

CHART_ARCHIVE_FILENAME="${CHART_NAME}-${CHART_VERSION}.tgz"

# Target helm repository.
TARGET_ARM_HELM_REPO_URL="https://arm.seli.gic.ericsson.se/artifactory/proj-mxe-deps-helm-local/"

echo " Adding $SOURCE_HELM_REPO_NAME helm repo"
helm repo add $SOURCE_HELM_REPO_NAME $SOURCE_HELM_REPO_URL --force-update
helm repo update $SOURCE_HELM_REPO_NAME

echo "Fetching chart for version $CHART_VERSION from $SOURCE_HELM_REPO_URL/$CHART_NAME"
helm pull $SOURCE_HELM_REPO_NAME/$CHART_NAME --version $CHART_VERSION

if [[ ! -f $CHART_ARCHIVE_FILENAME ]]
then
    echo "Chart archive file $CHART_ARCHIVE_FILENAME did not get downloaded. Exiting."
    exit 1
fi

docker run --init --rm  --volume $(pwd -P):/charts --workdir /charts  \
    armdocker.rnd.ericsson.se/proj-adp-cicd-drop/bob-adp-release-auto:latest \
    upload_file.sh --filename=${CHART_ARCHIVE_FILENAME}  \
    --repository="${TARGET_ARM_HELM_REPO_URL}" --api-token=${API_TOKEN}
status=$? 

if [[ $status -ne 0 ]] 
then
    echo "Upload to arm failed. Exiting."
    exit 1
else 
    echo "Chart ${CHART_ARCHIVE_FILENAME} uploaded to ${TARGET_ARM_HELM_REPO_URL}"
    rm ${CHART_ARCHIVE_FILENAME}
fi
