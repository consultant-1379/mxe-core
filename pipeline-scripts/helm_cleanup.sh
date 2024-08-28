#!/usr/bin/env bash
set -x

############ WARNING: This script is not used ####################
########## It is referenced in Nightly pipeline which is currently outdated and unused #############

namespace="${1}"
username="${2}"
deployernamespace="${namespace}"

helm delete mxe-apps-${username} -n ${namespace}
helm delete mxe-deployer-${username} -n ${deployernamespace}
helm delete mxe-commons-${username} -n ${namespace}
helm delete eric-ctrl-bro-${username} -n ${namespace}
helm delete eric-mesh-controller-crd-${username} -n ${namespace}

kubectl delete validatingwebhookconfigurations -l app.kubernetes.io/instance=mxe-apps
kubectl delete CustomResourceDefinition seldondeployments.machinelearning.seldon.io --ignore-not-found=true

kubectl delete deployment,statefulset,ingress,jobs,svc,configmap,secret,sa,role,rolebinding,clusterrole,clusterrolebinding,pdb,hpa -n ${namespace} -l app.kubernetes.io/instance=mxe-apps-${username}
kubectl delete deployment,statefulset,ingress,jobs,svc,configmap,secret,sa,role,rolebinding,clusterrole,clusterrolebinding,pdb,hpa -n ${deployernamespace} -l app.kubernetes.io/instance=mxe-deployer-${username}
kubectl delete deployment,statefulset,ingress,jobs,svc,configmap,secret,sa,role,rolebinding,clusterrole,clusterrolebinding,pdb,hpa -n ${namespace} -l app.kubernetes.io/instance=mxe-commons-${username}
kubectl delete deployment,statefulset,ingress,jobs,svc,configmap,secret,sa,role,rolebinding,clusterrole,clusterrolebinding,pdb,hpa -n ${namespace} -l app.kubernetes.io/instance=eric-mesh-controller-crd-${username}
kubectl -n ${namespace} get deploy,sts,ingress,svc,secret,configmap -l app.kubernetes.io/part-of=mxe -o name | xargs kubectl -n ${namespace}  delete --ignore-not-found
kubectl delete CustomResourceDefinition applications.argoproj.io --ignore-not-found=true
kubectl delete CustomResourceDefinition appprojects.argoproj.io --ignore-not-found=true
kubectl delete authorizationpolicy -n ${namespace} -l app.kubernetes.io/part-of=mxe
kubectl delete requestauthentication -n ${namespace} -l app.kubernetes.io/part-of=mxe
kubectl delete CustomResourceDefinition -l app.kubernetes.io/name=eric-mesh-controller-crd
kubectl delete mutatingwebhookconfigurations -l app.kubernetes.io/instance=mxe-commons
kubectl delete validatingwebhookconfigurations -l app.kubernetes.io/instance=mxe-commons
kubectl delete CustomResourceDefinition applications.argoproj.io --ignore-not-found=true
kubectl delete CustomResourceDefinition appprojects.argoproj.io --ignore-not-found=true
kubectl label namespace ${namespace} istio-injection-
kubectl label namespace ${namespace} eric-inject-ns-
kubectl delete pvc -n ${namespace} --all
