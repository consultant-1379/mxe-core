#!/usr/bin/env bash
set -ex


IMAGES_LIST=$1
TRIVY_REPORTS_DIR=$2

log_msg(){
    set -x
    echo -e "\n\n $1"
    set +x
}

download_vuln_db(){
    log_msg "Attempting to update vuln db...."
    trivy image --download-db-only || true
    log_msg
}

log_version(){
    log_msg "Trivy version:" 
    trivy  --version
    log_msg
}

trivyScan(){
    local image=$1
    log_msg "Scanning $image";
    imageName=$(basename "$image" | cut -f1 -d":");
    /entrypoint.sh -f json -o  "${TRIVY_REPORTS_DIR}/$imageName".json $image;
}

## commented for repeatable scans, use only the vuln db provided by ADP
#download_vuln_db
log_version


while read -r -d, image 
do
    trivyScan "${image}"
done <<< "${IMAGES_LIST}"