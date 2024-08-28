#!/bin/bash
########################################################################################################
# Script
#   pre-requisite: docker login armdocker.rnd.ericsson.se
#   a) clones spark-opeator release tag,
#   b) builds docker image for spark-operator using gaia-cbo as base image
#   c) pushes image to armdocker
#######################################################################################################
# Spark Operator git repo details
REPO_URL=https://github.com/GoogleCloudPlatform/spark-on-k8s-operator.git
SPARK_OPERATOR_VERSION=${SPARK_OPERATOR_VERSION:-spark-operator-chart-1.1.27}

# Base CBO image details
CBO_VERSION=${CBO_VERSION:-6.14.0-10}
CBO_IMAGE_URL=${CBO_IMAGE_URL:-armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles}
DEVEL_VERSION=${DEVEL_VERSION:-6.14.0-10}
TINI_VERSION=${TINI_VERSION:-v0.19.0}

# Build No 
# for first run with a cbo base image, spark operator version set BUILD_NO to ''
BUILD_NO="01"

# Spark Operator image details
if [[ -z $BUILD_NO ]]
then 
    IMAGE_TAG=${IMAGE_TAG:-${CBO_VERSION}-1.1.27}
else 
    ## add build no to image tag if its set
    IMAGE_TAG=${IMAGE_TAG:-${CBO_VERSION}-1.1.27-${BUILD_NO}}
fi 

IMAGE=armdocker.rnd.ericsson.se/proj-mxe/spark/spark-operator:${IMAGE_TAG}
BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')

case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

BASEDIR=$(dirname $SCRIPT)
echo "Base dir is ${BASEDIR}"

MODULEROOT=$(dirname $BASEDIR)
echo "Base image is  ${CBO_IMAGE_URL}:${CBO_VERSION}"

if [[ -d "${BASEDIR}/spark-operator" ]]; then
    rm -rf "${BASEDIR}/spark-operator"
fi

git clone -b "${SPARK_OPERATOR_VERSION}" "${REPO_URL}" "${BASEDIR}/spark-operator/"

#update dependency versions to handle vulnerabilities
sed -i '/^\tvolcano.sh.*/a \\tgithub.com/gogo/protobuf v1.3.2 // indirect\n \tgolang.org/x/crypto v0.0.0-20220427172511-eb4f295cb31f // indirect\n \tgithub.com/prometheus/client_golang v1.12.1 // indirect\n \tgoogle.golang.org/protobuf v1.28.0 //indirect' "${BASEDIR}/spark-operator/go.mod"
sed -i 's/k8s.io\/kubernetes v1.19.6/k8s.io\/kubernetes v1.19.15/' "${BASEDIR}/spark-operator/go.mod"

#remove a line with security bug thats prints TLS secrets
sed -i 's/cat ${TMP_DIR}\/output//g' "${BASEDIR}/spark-operator/hack/gencerts.sh"

# sed -i "s/set -e/set -ex/g" "${BASEDIR}/spark-operator/hack/gencerts.sh"

DOCKER_BUILDKIT=1 docker build --build-arg IMAGE_TAG=${IMAGE_TAG} --build-arg BUILD_DATE=${BUILD_DATE} --build-arg CBO_IMAGE_URL=${CBO_IMAGE_URL} --build-arg CBO_VERSION=${CBO_VERSION} --build-arg TINI_VERSION=${TINI_VERSION} --build-arg DEVEL_VERSION=${DEVEL_VERSION} -t ${IMAGE} -f ${BASEDIR}/Dockerfile ${BASEDIR}/spark-operator --progress=plain
built=$?

if [[ $built == 0 ]]; then
    docker push ${IMAGE}
    pushed=$?
else 
    echo "Image build failed"
    exit 1
fi

if [[ $pushed == 0 ]] && [[ -d "${BASEDIR}/spark-operator" ]]; then
    rm -rf "${BASEDIR}/spark-operator"
fi
