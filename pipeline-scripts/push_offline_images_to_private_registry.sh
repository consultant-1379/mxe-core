#!/usr/bin/env bash 
set -ex 

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
REPOROOT=$(dirname $SCRIPTPATH)

ONLINE_REGISTRY_URL=$1
ONLINE_SERO_REGISTRY_URL=$2
PRIVATE_REGISTRY_URL=$3
OFFLINE_TAR_BALL=$4
IMAGES_LIST_FILE=${OFFLINE_TAR_BALL}.txt

if [ -z "${PRIVATE_REGISTRY_USER}" ]; then
    echo "PRIVATE_REGISTRY_USER is not set"
    exit 1
fi

if [ -z "${PRIVATE_REGISTRY_PASSWORD}" ]; then
    echo "PRIVATE_REGISTRY_PASSWORD is not set"
    exit 1
fi

docker login ${PRIVATE_REGISTRY_URL} --username ${PRIVATE_REGISTRY_USER} --password ${PRIVATE_REGISTRY_PASSWORD} 

docker load -i ${OFFLINE_TAR_BALL}

while IFS= read -r image
do 
    destination_image=$(echo $image | sed "s#${ONLINE_REGISTRY_URL}#${PRIVATE_REGISTRY_URL}#g" | sed "s#${ONLINE_SERO_REGISTRY_URL}#${PRIVATE_REGISTRY_URL}#g")
    echo "Source Image: $image Target Image: $destination_image"
    docker tag $image $destination_image
    docker push $destination_image
    docker rmi $image -f
done < ${IMAGES_LIST_FILE}