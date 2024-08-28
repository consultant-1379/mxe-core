#!/usr/bin/env bash

set -eux

case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

# Absolute path this script is in. /home/user/bin
BASEDIR=$(dirname $SCRIPT)

MODULEROOT=$(dirname $BASEDIR)

cleanup() {
    rm -rf "${BASEDIR}/emissary"
    rm -rf "${BASEDIR}/docker-alpine-glibc"

}

ALPINE_GLIBC_SOURCE_REPO_URL=git@github.com:Docker-Hub-frolvlad/docker-alpine-glibc.git
ALPINE_GLIBC_REPO_TAG=alpine-3.15_glibc-2.34
ALPINE_GLIBC_ORIGINAL_SOURCE_IMAGE=alpine:3.15
LATEST_ALPINE_PATCH_IMAGE=alpine:3.15.4
NEW_IMAGE_SUFFIX=$(echo $LATEST_ALPINE_PATCH_IMAGE | sed "s/:/-/g")
CUSTOM_ALPINE_GLIBC_IMAGE_TAG=${NEW_IMAGE_SUFFIX}_glibc-2.34

EMISSARY_REPO_URL=git@github.com:emissary-ingress/emissary.git
EMISSARY_RELEASE_VERSION=1.14.2
# emissary 1.x is still ambassador, change name to emissary while upgrading to 2.x
EMISSARY_IMAGE=armdocker.rnd.ericsson.se/proj-mxe/datawire/ambassador:${EMISSARY_RELEASE_VERSION}-${NEW_IMAGE_SUFFIX}
GLIBC_SOURCE_IN_AMBASSADOR_CODE="docker.io/frolvlad/alpine-glibc:alpine-3.12_glibc-2.32"

cleanup

## Rebuild base image used in ambassador with latest alpine patch ${LATEST_ALPINE_PATCH_IMAGE}

git clone -b "${ALPINE_GLIBC_REPO_TAG}" "${ALPINE_GLIBC_SOURCE_REPO_URL}" "${BASEDIR}/docker-alpine-glibc"
sed -i "s/${ALPINE_GLIBC_ORIGINAL_SOURCE_IMAGE}/${LATEST_ALPINE_PATCH_IMAGE}/g" "${BASEDIR}/docker-alpine-glibc/Dockerfile"

docker build -t "frolvlad/alpine-glibc:${CUSTOM_ALPINE_GLIBC_IMAGE_TAG}" "${BASEDIR}/docker-alpine-glibc" 

## Rebuild emissary with latest OS Image

git clone -b "v${EMISSARY_RELEASE_VERSION}" "${EMISSARY_REPO_URL}" "${BASEDIR}/emissary"

cd "${BASEDIR}/emissary"


for file in $(grep -Rl $GLIBC_SOURCE_IN_AMBASSADOR_CODE  *);
do
    sed -i "s#${GLIBC_SOURCE_IN_AMBASSADOR_CODE}#frolvlad/alpine-glibc:${CUSTOM_ALPINE_GLIBC_IMAGE_TAG}#g" $file
done

# Fix dependency due to OS Image Update.

sed -i "s#python3.8#python3.9#g" builder/Dockerfile

sed -i '/RUN echo '\''manylinux1_compatible/,/#/c\
RUN curl https://sh.rustup.rs -sSf | bash -s -- -y\
RUN echo '\''source $HOME/.cargo/env'\'' >> $HOME/.bashrc\
RUN apk add patchelf\
RUN source $HOME/.cargo/env && rustup install nightly && rustup default nightly && echo '\''manylinux1_compatible = True'\'' > /usr/lib/python3.9/_manylinux.py && pip3 install orjson==3.6.5 && rm -f /usr/lib/python3.9/_manylinux.py' builder/Dockerfile.base

sed -i "s#rsa==4.6#rsa==4.7#g" builder/requirements.txt
sed -i "s#jinja2==2.11.2#jinja2==2.11.3#g" builder/requirements.txt
sed -i "s#kubernetes==9.0.0#kubernetes==12.0.1#g" builder/requirements.txt
sed -i "s#py==1.9.0#py==1.11.0#g" builder/requirements.txt
sed -i "s#urllib3==1.26.3#urllib3==1.26.8#g" builder/requirements.txt
sed -i "s#werkzeug==1.0.1#werkzeug==2.0.2#g" builder/requirements.txt
sed -i "s#packaging==#\#packaging==#g" builder/requirements.txt
sed -i "s#six==#\#six==#g" builder/requirements.txt

sed -i "s#protobuf v1.3.1#protobuf v1.3.2#g" vendor/modules.txt
sed -i "s#text v0.3.4#text v0.3.7#g" vendor/modules.txt

make images

buildStatus=$?
if [[ $buildStatus == 0 ]]; then
    docker tag ambassador.local/ambassador-ea:latest "${EMISSARY_IMAGE}"
    docker push ${EMISSARY_IMAGE}
    pushed=$?
    if [[ $pushed == 0 ]]; then
        cleanup
    fi
else
    echo "Docker build failed"
fi

cd ${OLDPWD}
