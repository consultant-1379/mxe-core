#!/usr/bin/env bash
set -ex

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
REPOROOT=$(dirname $(dirname $SCRIPTPATH))

log_msg(){
    set +x
    echo -e "\n\n $1"
    set -x
}

update_vuln_db(){
    log_msg "Attempting to update vuln db"
    grype db update || true 
}

log_version(){
    log_msg "Grype version:" 
    grype version
    log_msg "Grype DB status:"
    grype db status
    log_msg "Syft version:"
    syft --version
    log_msg
}

IMAGES_LIST=$1
REPORT_PATH=$2
GENERATE_SBOM=${3:-true}
GRYPE_PARAMETERS="--scope Squashed"

if [[ $GENERATE_SBOM == "true" ]] ; then
    SBOM_OPTS="--generate-sbom"
else
    SBOM_OPTS=""
fi

BATCH_SIZE=10


if [ ! -d ${REPORT_PATH} ]
then 
    mkdir -p ${REPORT_PATH} 
fi 

import_vuln_db(){
    if [[ -z $GRYPE_DB_VERSION ]]
    then
        log_msg "Fatal: Env GRYPE_DB_VERSION is not set, No grype db version specified, exit.."
        exit 1 
    fi 
    if [[ ! -f "vulnerability-db_${GRYPE_DB_VERSION}.tar.gz" ]]
    then
        log_msg "vulnerability-db_${GRYPE_DB_VERSION}.tar.gz is not found"
        exit 1 
    fi 
    grype db import "vulnerability-db_${GRYPE_DB_VERSION}.tar.gz"
}


# commented because db is now pinned
# update_vuln_db
# Commented because grype db import fails due to permission issue
# import_vuln_db
log_version

declare -a images
while read -r -d, image 
do
    images+=("$image")
done <<< "${IMAGES_LIST}"

noOfImages="${#images[@]}"

for(( i=0; i<${noOfImages};i+=$BATCH_SIZE ))
do 
    start=$i 
    log_msg "Start Index: $start"
    imagesChunk=("${images[@]:$start:$BATCH_SIZE}")
    log_msg "Scanning ${#imagesChunk[@]} images: ${imagesChunk[@]}"
    grype_scan ${imagesChunk[@]/#/--image } --report-dir ${REPORT_PATH} --grype-parameters "${GRYPE_PARAMETERS}" ${SBOM_OPTS}
    status=$? 
    if [[ $status -ne 0 ]]
    then 
        log_msg "Anchore scan failed.."
        exit $status 
    fi
done 