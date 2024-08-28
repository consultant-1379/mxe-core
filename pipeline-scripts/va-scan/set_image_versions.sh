#!/usr/bin/env bash

set -eux

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
REPOROOT=$(dirname $(dirname $SCRIPTPATH))

VA_RULESET_FILE="$REPOROOT/ruleset2.0-va.yaml"

if [[ -z $TRIVY_IMAGE_TAG ]]; then
    echo "TRIVY_IMAGE_TAG is not set"
    exit 1
fi 

if [[ -z $GRYPE_IMAGE_TAG ]]; then 
    echo "GRYPE_IMAGE_TAG is not set"
    exit 1
fi 

sed -i "s#%%GRYPE_IMAGE_TAG%%#${GRYPE_IMAGE_TAG}#g" "$VA_RULESET_FILE"
sed -i "s#%%TRIVY_IMAGE_TAG%%#${TRIVY_IMAGE_TAG}#g" "$VA_RULESET_FILE"
