#! /usr/bin/env bash

set -ex

GITEA_DEPLOYMENT_NAME=$(kubectl get deployment -n ${MXE_NAMESPACE} -l app.kubernetes.io/name=${GITEA_SERVICE_NAME} -o name)
ARGOCD_SERVER_DEPLOYMENT_NAME=$(kubectl get deployment -n ${MXE_NAMESPACE} -l app.kubernetes.io/name=argocd-server -o name)
ARGOCD_REPOSERVER_DEPLOYMENT_NAME=$(kubectl get deployment -n ${MXE_NAMESPACE} -l app.kubernetes.io/name=argocd-repo-server -o name)
sed -e "s|###ARGO_CONTAINER_NAME###|"server"|g" /etc/patch-template/argocd-deployment-patch.yaml-template > /tmp/argocd-server-deployment-patch.yaml
sed -e "s|###ARGO_CONTAINER_NAME###|"repo-server"|g" /etc/patch-template/argocd-deployment-patch.yaml-template > /tmp/argocd-reposerver-deployment-patch.yaml

GITEA_AVAILABLE_REPLICAS=$(kubectl get ${GITEA_DEPLOYMENT_NAME} -n ${MXE_NAMESPACE} -o=jsonpath='{.status.availableReplicas}')
kubectl scale ${GITEA_DEPLOYMENT_NAME} -n ${MXE_NAMESPACE} --replicas=0
kubectl rollout status "${GITEA_DEPLOYMENT_NAME}" -n ${MXE_NAMESPACE} --watch
kubectl patch ${GITEA_DEPLOYMENT_NAME} --patch-file /etc/patch-template/gitea-deployment-patch.yaml-template -n ${MXE_NAMESPACE}
kubectl patch ${ARGOCD_SERVER_DEPLOYMENT_NAME} --patch-file /tmp/argocd-server-deployment-patch.yaml -n ${MXE_NAMESPACE}
kubectl patch ${ARGOCD_REPOSERVER_DEPLOYMENT_NAME} --patch-file /tmp/argocd-reposerver-deployment-patch.yaml -n ${MXE_NAMESPACE}
kubectl scale ${GITEA_DEPLOYMENT_NAME} -n ${MXE_NAMESPACE} --replicas=${GITEA_AVAILABLE_REPLICAS}
kubectl rollout status "${GITEA_DEPLOYMENT_NAME}" -n ${MXE_NAMESPACE} --watch
kubectl rollout status "${ARGOCD_REPOSERVER_DEPLOYMENT_NAME}" -n ${MXE_NAMESPACE} --watch
kubectl rollout status "${ARGOCD_SERVER_DEPLOYMENT_NAME}" -n ${MXE_NAMESPACE} --watch
