#!/usr/bin/env bash

set -ex
set -x

while [ -z ${GITEA_DEPLOYMENT_NAME} ]; do
  GITEA_DEPLOYMENT_NAME=$(kubectl get -n "${MXE_DEPLOYER_NAMESPACE}" deployment -l "app.kubernetes.io/name=gitea" -o=jsonpath='{.items[0].metadata.name}') || true
done

kubectl rollout status deployment "${GITEA_DEPLOYMENT_NAME}" -n "${MXE_DEPLOYER_NAMESPACE}" --watch

echo "\n\n gitea is up"
