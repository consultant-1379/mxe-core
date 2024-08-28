#!/usr/bin/env bash
set -x 
set -e  

## As part of CVE fixes, all unused and obsolete images need to be removed from arm and also
## from mxe app helm charts

mxeRegistry="armdocker.rnd.ericsson.se"
srcLocation="proj-mxe"
destLocation="proj-mxe-ci-internal/3pp-bkp"

images=("dexidp/dex:v2.25.0"
        "busybox:1.31.1"
        "oliver006/redis_exporter:v1.15.1"
        "gcr.io/kfserving/storage-initializer:v0.4.0"
        "seldonio/engine:1.9.0")

if [[ -z $DOCKER_API_TOKEN ]]
then 
    echo "DOCKER_API_TOKEN env is not set. Please export it before running"
    exit 1
fi 

deleteImage(){
    local image=$1 
    imageName=$(echo $image | cut -d":" -f1)
    imageTag=$(echo $image | cut -d":" -f2)
    imageLocation="${mxeRegistry}/artifactory/proj-mxe-release-docker-global/${srcLocation}/${imageName}/${imageTag}" 
    echo -e "Deleting ${imageName} Tag:${imageTag}"
    echo -e "From location ${imageLocation}"
    curl --header "X-JFrog-Art-Api:${DOCKER_API_TOKEN}" \
            --request DELETE --location "${imageLocation}"
    statusCode=$? 
    echo "Image: ${image} deletion status=${statusCode}"
}

takeBackup(){
    local image=$1
    docker pull "${mxeRegistry}/${srcLocation}/${image}"
    docker tag "${mxeRegistry}/${srcLocation}/${image}" "${mxeRegistry}/${destLocation}/${image}"
    docker push "${mxeRegistry}/${destLocation}/${image}"
}

for image in "${images[@]}"
do 
    takeBackup ${image}
    deleteImage ${image}
done 