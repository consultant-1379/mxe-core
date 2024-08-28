#! /usr/bin/env bash 

set -x
set -e 

CWD=$(pwd)

case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in. /home/user/bin
BASEDIR=$(dirname $SCRIPT)
MODULEROOT=$(dirname $BASEDIR)
source "${MODULEROOT}/base_image.sh"
echo "Latest Alpine tag is ${LATEST_ALPINE_TAG}"

cleanup() {
if [[ -d "${BASEDIR}/ingress-nginx" ]]; then
    if [[ -d "${BASEDIR}/ingress-nginx/.modcache/" ]]; then 
        chmod -R 777  "${BASEDIR}/ingress-nginx/.modcache/"
    fi 
    rm -rf "${BASEDIR}/ingress-nginx"
fi
}

ALPINE_OS_VERSION=${LATEST_ALPINE_TAG}
ALPINE_BASE_IMAGE="alpine:${ALPINE_OS_VERSION}"
ALPINE_BASE_IMAGE_ARMPROXY=$ARMDOCKER_PROXY/$ALPINE_BASE_IMAGE
REPO_URL=https://github.com/kubernetes/ingress-nginx.git
CONTROLLER_VERSION=controller-v1.8.1
REGISTRY="armdocker.rnd.ericsson.se/proj-mxe/k8s.gcr.io"
REGISTRY_INTERNAL="armdocker.rnd.ericsson.se/proj-mxe-ci-internal/k8s.gcr.io"
NGINX_REGISTRY="${REGISTRY}/ingress-nginx"
ALPINE_VERSION_IN_SRC=3.18.2
SRC_ALPINE_BASE_IMAGE="alpine:${ALPINE_VERSION_IN_SRC}"
BUILD_ID=mxe2.8

cleanup

git clone -b "${CONTROLLER_VERSION}" "${REPO_URL}" "${BASEDIR}/ingress-nginx"

sed -i "s#${SRC_ALPINE_BASE_IMAGE}#${ALPINE_BASE_IMAGE_ARMPROXY}#g" "${BASEDIR}/ingress-nginx/images/nginx/rootfs/Dockerfile"
sed -i "s/writeDirs=/set -x;writeDirs=/g" "${BASEDIR}/ingress-nginx/images/nginx/rootfs/Dockerfile"

cd "${BASEDIR}/ingress-nginx/images/nginx"
make PLATFORMS=linux/amd64 OUTPUT=--load TAG=${CONTROLLER_VERSION} REGISTRY=${REGISTRY_INTERNAL} build 
buildStatus=$?

if [[ $buildStatus -ne 0 ]]; then
    echo "Docker build failed for nginx"
    exit 1
fi

docker push  "${REGISTRY_INTERNAL}/nginx:${CONTROLLER_VERSION}"

cd "${BASEDIR}/ingress-nginx/"
make SHELL='sh -x' build 
make SHELL='sh -x' BASE_IMAGE="${REGISTRY_INTERNAL}/nginx:${CONTROLLER_VERSION}" REGISTRY=${NGINX_REGISTRY} VERSION=${CONTROLLER_VERSION} BUILD_ID=${BUILD_ID} image 
buildStatus=$?

if [[ $buildStatus -ne 0 ]]; then
    echo "Docker build failed for nginx controller"
    exit 1
fi

CONTROLLER_IMAGE=$(echo $CONTROLLER_VERSION | sed 's/-/:/g')
docker tag "${NGINX_REGISTRY}/${CONTROLLER_IMAGE}" "${NGINX_REGISTRY}/${CONTROLLER_IMAGE}-${ALPINE_OS_VERSION}"

docker push "${NGINX_REGISTRY}/${CONTROLLER_IMAGE}-${ALPINE_OS_VERSION}"

pushed=$?
if [[ $pushed == 0 ]]; then
    cleanup
fi
