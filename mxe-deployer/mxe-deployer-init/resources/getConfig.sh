#!/usr/bin/env bash
set -x 

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
NAMESPACE=${1:-mxe} 

kubectl get configmap -n ${NAMESPACE}  eric-mxe-deployer-service-init-configmap -ojsonpath='{.data.realmConfig\.yaml}' > ${SCRIPTPATH}/argocdRealmConfig.yaml
kubectl get configmap -n ${NAMESPACE}  eric-mxe-deployer-service-init-configmap -ojsonpath='{.data.oidcConfig\.yaml}' > ${SCRIPTPATH}/argocdOIDCConfig.yaml
kubectl get configmap -n ${NAMESPACE}  eric-mxe-deployer-service-init-configmap -ojsonpath='{.data.rbacConfig\.yaml}' > ${SCRIPTPATH}/argocdRBACConfig.yaml
kubectl get configmap -n ${NAMESPACE}  eric-mxe-deployer-service-init-configmap -ojsonpath='{.data.repositoriesConfig\.yaml}' > ${SCRIPTPATH}/argocdRepositoriesConfig.yaml
kubectl get configmap -n ${NAMESPACE}  eric-mxe-deployer-service-init-configmap -ojsonpath='{.data.oidcTokenConfig\.yaml}' > ${SCRIPTPATH}/keycloakTokenConfig.yaml
