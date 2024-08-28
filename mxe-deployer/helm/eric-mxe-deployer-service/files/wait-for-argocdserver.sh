#!/usr/bin/env bash

set -ex
set -x

while [ -z ${ARGOCD_SERVER_DEPLOYMENT_NAME} ]; do
  ARGOCD_SERVER_DEPLOYMENT_NAME=$(kubectl get -n "${ARGOCD_SERVER_DEPLOYMENT_NAMESPACE}" deploy -l "app.kubernetes.io/component=server" -l "app.kubernetes.io/name=argocd-server" -o=jsonpath='{.items[0].metadata.name}') || true
done

while [ -z ${ARGOCD_REPOSERVER_DEPLOYMENT_NAME} ]; do
  ARGOCD_REPOSERVER_DEPLOYMENT_NAME=$(kubectl get -n "${ARGOCD_SERVER_DEPLOYMENT_NAMESPACE}" deploy -l "app.kubernetes.io/component=repo-server" -l "app.kubernetes.io/name=argocd-repo-server" -o=jsonpath='{.items[0].metadata.name}') || true
done

while [ -z ${ARGOCD_CONTROLLER_STATEFULSET_NAME} ]; do
  ARGOCD_CONTROLLER_STATEFULSET_NAME=$(kubectl get -n "${ARGOCD_SERVER_DEPLOYMENT_NAMESPACE}" statefulset -l "app.kubernetes.io/component=application-controller" -l "app.kubernetes.io/name=argocd-application-controller" -o=jsonpath='{.items[0].metadata.name}') || true
done

kubectl rollout status deployment "${ARGOCD_SERVER_DEPLOYMENT_NAME}" -n "${ARGOCD_SERVER_DEPLOYMENT_NAMESPACE}" --watch
kubectl rollout status deployment "${ARGOCD_REPOSERVER_DEPLOYMENT_NAME}" -n "${ARGOCD_SERVER_DEPLOYMENT_NAMESPACE}" --watch
kubectl rollout status statefulset "${ARGOCD_CONTROLLER_STATEFULSET_NAME}" -n "${ARGOCD_SERVER_DEPLOYMENT_NAMESPACE}" --watch

if [[ ${ARGOCD_REDIS_HA_ENABLED} = "true" ]] && [[ ${ARGOCD_REDIS_ENABLED} = "false" ]]
then 
  while [ -z ${ARGOCD_REDIS_HA_STATEFULSET_NAME} ]; do
    ARGOCD_REDIS_HA_STATEFULSET_NAME=$(kubectl get -n "${ARGOCD_SERVER_DEPLOYMENT_NAMESPACE}" statefulset -l "app=redis-ha" -o=jsonpath='{.items[0].metadata.name}') || true
  done
  kubectl rollout status statefulset "${ARGOCD_REDIS_HA_STATEFULSET_NAME}" -n "${ARGOCD_SERVER_DEPLOYMENT_NAMESPACE}" --watch
elif [[ ${ARGOCD_REDIS_HA_ENABLED} = "false" ]] && [[ ${ARGOCD_REDIS_ENABLED} = "true" ]]
then
  while [ -z ${ARGOCD_REDIS_DEPLOYMENT_NAME} ]; do
    ARGOCD_REDIS_DEPLOYMENT_NAME=$(kubectl get -n "${ARGOCD_SERVER_DEPLOYMENT_NAMESPACE}" deployment -l "app.kubernetes.io/name=argocd-redis" -o=jsonpath='{.items[0].metadata.name}') || true
  done
  kubectl rollout status deployment "${ARGOCD_REDIS_DEPLOYMENT_NAME}" -n "${ARGOCD_SERVER_DEPLOYMENT_NAMESPACE}" --watch
else 
  echo "Received Invalid configuration.. redisHAEnabled:${ARGOCD_REDIS_HA_ENABLED}, redisEnabled:${ARGOCD_REDIS_ENABLED}"
  echo "Only one of redisHAEnabled or redisEnabled can be enabled/disabled at a time"
  echo "Aborting, cannot startup"
  exit 1
fi 
echo "\n\n argocd is up"

if [ "${INTERNAL_GITOPS_REPO_ENABLED}" = "true" ]
then
  while [ -z ${GITEA_DEPLOYMENT_NAME} ]; do
    GITEA_DEPLOYMENT_NAME=$(kubectl get -n "${ARGOCD_SERVER_DEPLOYMENT_NAMESPACE}" deployment -l "app.kubernetes.io/name=gitea" -o=jsonpath='{.items[0].metadata.name}') || true
  done

  kubectl rollout status deployment "${GITEA_DEPLOYMENT_NAME}" -n "${ARGOCD_SERVER_DEPLOYMENT_NAMESPACE}" --watch

  echo "\n\n gitea is up"
fi