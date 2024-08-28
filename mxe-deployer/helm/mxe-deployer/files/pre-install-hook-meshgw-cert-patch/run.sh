#!/usr/bin/env bash

set -ex

MESHGW_CERTIFICATE_LABEL="certificate-identifier=mxe-mesh-ingress-gw"

MESHGW_INTERNAL_CERTIFICATE_NAME=$(kubectl get internalcertificate --namespace $MXE_NAMESPACE -l $MESHGW_CERTIFICATE_LABEL -o name)

if [[ -z "$MESHGW_INTERNAL_CERTIFICATE_NAME" ]]; then
    echo "Internal certificate matching label $MESHGW_CERTIFICATE_LABEL not found"
    exit 1
fi

if [[ ! -f "$PATCH_FILE" ]]; then
    echo "File $PATCH_FILE does not found"
    exit 1
fi

kubectl patch $MESHGW_INTERNAL_CERTIFICATE_NAME -n $MXE_NAMESPACE --patch-file $PATCH_FILE --type=merge