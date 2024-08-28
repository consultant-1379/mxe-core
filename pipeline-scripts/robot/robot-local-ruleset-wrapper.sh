#!/usr/bin/env bash 

set -ex

SCRIPT=$(readlink -f $0)
ROBOTSCRIPTPATH=$(dirname $SCRIPT)
REPOROOT=$(dirname $(dirname $ROBOTSCRIPTPATH))
TEST_DIR="${REPOROOT}/mxe-test"
ARM_REPO="https://arm.seli.gic.ericsson.se/artifactory"

download_cli(){
if [[ -z $DOCKER_API_TOKEN ]]
then 
    echo "DOCKER_API_TOKEN env is not set. Please export it before running"
    exit 1
fi

if [[ "$MXE_VERSION" = *"+"* ]]; then
	CLI_URL="${ARM_REPO}/proj-mxe-release-generic/${MXE_VERSION}/mxe-cli-linux-${MXE_VERSION}.tgz"
else
	CLI_URL="${ARM_REPO}/proj-mxe-dev-generic/mxe-cli-linux-${MXE_VERSION}.tgz"
fi

curl -k --header "X-JFrog-Art-Api:${DOCKER_API_TOKEN}" --fail ${CLI_URL} | tar xvz -C "${REPOROOT}/cli"

}



cd $REPOROOT

#
# STEP1: Update steps in robot-test-local rule in ruleset2.0.yaml, refer robot-test-dev for what runs in pipeline
# STEP2: Update env properties below
# STEP3: Run this script
#
export MXE_VERSION=2.2.0-170
export TESTMXEENDPOINT=https://mxe.cech030.rnd.gic.ericsson.se
export MXE_USER=mxe-user
export MXE_PASSWORD=mxe-password
export KEYCLOAK_USERNAME=admin
export KEYCLOAK_PASSWORD=My-super-secret-pw123
export PWD=$(pwd -P)
export KUBECONFIG="${HOME}/.kube/config"
export NAMESPACE="mxe"
export TESTDEPLOYERAUTHENDPOINT=https://oauth.cech030.rnd.gic.ericsson.se

download_cli 
bob/bob robot-test-local 2>&1 | tee ${REPOROOT}/test.log 

cd $OLDPWD