#! /usr/bin/env bash

set -ex

GITEA_DEPLOYMENT_NAME=$(kubectl get deployment -n ${MXE_NAMESPACE} -l app.kubernetes.io/name=${GITEA_SERVICE_NAME} -o name)
sed -e "s|###CONFIG_MAP_NAME###|"$CONFIG_MAP_NAME"|g" -e "s|###IMAGE_NAME###|"$IMAGE_NAME"|g" /etc/patch-template/gitea-deployment-patch.yaml-template > /tmp/gitea-deployment-patch.yaml
GITEA_AVAILABLE_REPLICAS=$(kubectl get ${GITEA_DEPLOYMENT_NAME} -n ${MXE_NAMESPACE} -o=jsonpath='{.status.availableReplicas}')
kubectl scale ${GITEA_DEPLOYMENT_NAME} -n ${MXE_NAMESPACE} --replicas=0
kubectl rollout status "${GITEA_DEPLOYMENT_NAME}" -n ${MXE_NAMESPACE} --watch
kubectl patch ${GITEA_DEPLOYMENT_NAME} --patch-file /tmp/gitea-deployment-patch.yaml -n ${MXE_NAMESPACE}
kubectl scale ${GITEA_DEPLOYMENT_NAME} -n ${MXE_NAMESPACE} --replicas=${GITEA_AVAILABLE_REPLICAS}
kubectl rollout status "${GITEA_DEPLOYMENT_NAME}" -n ${MXE_NAMESPACE} --watch
