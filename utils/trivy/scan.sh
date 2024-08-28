#!/bin/bash
set -x

case "$(uname -s)" in
    Darwin*) SCRIPT=$(greadlink -f $0) ;;
    *)       SCRIPT=$(readlink -f $0)
esac

# Absolute path this script is in. /home/user/bin
SCRIPTPATH=$(dirname $SCRIPT)

VULNERABILITIES_FILE="${SCRIPTPATH}/vulnerabilities.txt"

if [[ -f "${VULNERABILITIES_FILE}" ]]
then 
    rm "${VULNERABILITIES_FILE}"
fi

image_list=('armdocker.rnd.ericsson.se/proj-mxe/dexidp/dex:v2.25.0'
            'armdocker.rnd.ericsson.se/proj-mxe/argoproj/argocd:v1.8.1'
            'armdocker.rnd.ericsson.se/proj-mxe/redis:5.0.10-alpine'
            'armdocker.rnd.ericsson.se/proj-mxe/haproxy:2.0.4'
            'armdocker.rnd.ericsson.se/proj-mxe/busybox:1.31.1'
            'armdocker.rnd.ericsson.se/proj-mxe/oliver006/redis_exporter:v1.3.2'
            'armdocker.rnd.ericsson.se/proj-mxe-ci-internal/mxe/mxe-deployer-service:2.0.0-h45cb85fb')

for image in "${image_list[@]}"
do 
    GITHUB_TOKEN=${GITHUB_TOKEN} trivy image --severity HIGH,CRITICAL,MEDIUM $image 2>&1 >> "${VULNERABILITIES_FILE}" 
    printf "\n\n\n" >> "${VULNERABILITIES_FILE}"
done 