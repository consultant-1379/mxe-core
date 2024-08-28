#!/bin/bash
set -x

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
REPOROOT=$(dirname $SCRIPTPATH)

MXE_DEPLOY_CLI="${REPOROOT}/cli/mxe-deploy"

TESTDEPLOYERENDPOINT=$1
TESTDEPLOYERAUTHENDPOINT=$2
ARGOCD_KEYCLOAKADMIN_USER=$3
ARGOCD_KEYCLOAKADMIN_PASSWORD=$4

noOfRetries=5
loggedIn=0
i=0

env

while [[ $i -lt $noOfRetries && $loggedIn -eq 0 ]]; do
    ${MXE_DEPLOY_CLI} login ${TESTDEPLOYERENDPOINT} --ssoMode --ssoHost ${TESTDEPLOYERAUTHENDPOINT} \
        --username ${ARGOCD_KEYCLOAKADMIN_USER} --password ${ARGOCD_KEYCLOAKADMIN_PASSWORD}
    loginStatus=$?
    if [[ $loginStatus -eq 0 ]]; then
        loggedIn=1
    fi
    let i=i+1
done

if [[ $loggedIn -eq 0 ]]; then
    printf "Could not login even after 5 retries"
    exit 1
fi
