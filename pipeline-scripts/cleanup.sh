#!/usr/bin/env bash

set -x
set -o functrace

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
REPOROOT=$(dirname $SCRIPTPATH)

NAMESPACE=$1
DEPLOYER_NAMESPACE=${2:-$NAMESPACE}
MXE_WORKFLOW_RELEASE_NAME="mxe-workflow"
MXE_TRAINING_RELEASE_NAME="mxe-training"
MXE_EXPLORATION_RELEASE_NAME="mxe-exploration"
MXE_SERVING_RELEASE_NAME="mxe-serving"
MXE_DEPLOYER_RELEASE_NAME="mxe-deployer"
MXE_COMMONS_RELEASE_NAME="mxe-commons"

helm_uninstall(){
    CHART_NAME=$1
    helm -n ${NAMESPACE} list | grep ${CHART_NAME}
    if [ $? -eq 0 ]; then
        helm uninstall ${CHART_NAME} -n ${NAMESPACE} --debug --wait
        if [ $? -ne 0 ]; then
            FAILED_COMMAND="Helm uninstall failed for ${CHART_NAME}"
            FAILED_COMMANDS+=("$FAILED_COMMAND")
        fi
    fi
}

delete_crd_resource(){
    CRD_NAME=$1
    CRD_RESOURCE_NAME=$2
    kubectl get crd | grep ${CRD_NAME}
    if [ $? -eq 0 ]; then
        kubectl delete ${CRD_RESOURCE_NAME} -n ${NAMESPACE} --all
    fi
}

delete_mxe_workflow(){
    helm_uninstall ${MXE_WORKFLOW_RELEASE_NAME}
    kubectl delete deployment,statefulset,jobs,pods,svc,ingress,configmap,secret,sa,role,rolebinding,clusterrole,clusterrolebinding,pdb,hpa -n ${NAMESPACE} -l "app.kubernetes.io/instance=${MXE_WORKFLOW_RELEASE_NAME}"

    kubectl delete mutatingwebhookconfigurations mxe-workflow-spark-operator-webhook-config --ignore-not-found=true

    delete_crd_resource workflows.argoproj.io workflow
    delete_crd_resource sparkapplications.sparkoperator.k8s.io sparkapplications

    kubectl delete secret -n ${NAMESPACE}  mxe-workflow-spark-operator-webhook-certs --ignore-not-found=true

    local crds=(
        clusterworkflowtemplates.argoproj.io
        cronworkflows.argoproj.io
        workfloweventbindings.argoproj.io
        workflows.argoproj.io
        workflowtemplates.argoproj.io
        workflowtasksets.argoproj.io
        workflowtaskresults.argoproj.io
        workflowartifactgctasks.argoproj.io
        scheduledsparkapplications.sparkoperator.k8s.io
        sparkapplications.sparkoperator.k8s.io
    )

    for crd in "${crds[@]}"; do
        kubectl delete CustomResourceDefinition "${crd}" --ignore-not-found=true 
    done
}

delete_mxe_training(){
    helm_uninstall ${MXE_TRAINING_RELEASE_NAME}
    kubectl delete deployment,statefulset,jobs,pods,svc,ingress,configmap,secret,sa,role,rolebinding,clusterrole,clusterrolebinding,pdb,hpa -n ${NAMESPACE} -l "app.kubernetes.io/instance=${MXE_TRAINING_RELEASE_NAME}"

}

delete_mxe_exploration(){
    helm_uninstall ${MXE_EXPLORATION_RELEASE_NAME}
    kubectl delete deployment,statefulset,jobs,pods,svc,ingress,configmap,secret,sa,role,rolebinding,clusterrole,clusterrolebinding,pdb,hpa -n ${NAMESPACE} -l "app.kubernetes.io/instance=${MXE_EXPLORATION_RELEASE_NAME}"
}

delete_mxe_serving() {
    helm_uninstall ${MXE_SERVING_RELEASE_NAME}
    kubectl delete validatingwebhookconfigurations -l "app.kubernetes.io/instance=${MXE_SERVING_RELEASE_NAME}"
    kubectl delete deployment,statefulset,jobs,pods,svc,ingress,configmap,secret,sa,role,rolebinding,clusterrole,clusterrolebinding,pdb,hpa -n ${NAMESPACE} -l "app.kubernetes.io/instance=${MXE_SERVING_RELEASE_NAME}"
    kubectl delete CustomResourceDefinition seldondeployments.machinelearning.seldon.io --ignore-not-found=true
}

delete_deployer_helm_release(){
    helm_uninstall ${MXE_DEPLOYER_RELEASE_NAME}
    delete_crd_resource applications.argoproj.io applications
    kubectl delete deployment,statefulset,jobs,pods,svc,ingress,configmap,secret,sa,role,rolebinding,clusterrole,clusterrolebinding,pdb,hpa -n ${NAMESPACE} -l "app.kubernetes.io/instance=${MXE_DEPLOYER_RELEASE_NAME}"

    local crds=(
        applications.argoproj.io
        appprojects.argoproj.io
        argocdextensions.argoproj.io
        applicationsets.argoproj.io
    )
    for crd in "${crds[@]}"; do
        kubectl delete CustomResourceDefinition "${crd}" --ignore-not-found=true 
    done
}

delete_commons_helm_release() { 
    helm_uninstall ${MXE_COMMONS_RELEASE_NAME}
    kubectl delete deployment,statefulset,jobs,pods,svc,ingress,configmap,secret,sa,role,rolebinding,clusterrole,clusterrolebinding,pdb,hpa -n ${NAMESPACE} -l "app.kubernetes.io/instance=${MXE_COMMONS_RELEASE_NAME}"
    kubectl delete deployment,statefulset,jobs,pods,svc,ingress,configmap,secret,sa,role,rolebinding,clusterrole,clusterrolebinding,pdb,hpa -n ${NAMESPACE} -l "app.kubernetes.io/part-of=mxe"
    kubectl delete CustomResourceDefinition -l  app.kubernetes.io/name=ambassador
    kubectl delete authorizationpolicy -n ${NAMESPACE} -l app.kubernetes.io/part-of=mxe
    kubectl delete requestauthentication -n ${NAMESPACE} -l app.kubernetes.io/part-of=mxe
    kubectl delete mutatingwebhookconfigurations -l "app.kubernetes.io/instance=${MXE_COMMONS_RELEASE_NAME}"
    kubectl delete validatingwebhookconfigurations -l "app.kubernetes.io/instance=${MXE_COMMONS_RELEASE_NAME}"
    kubectl delete ingressclass eric-mxe-ingress-controller-class --ignore-not-found=true
    kubectl label namespace ${NAMESPACE} istio-injection-
    kubectl label namespace ${NAMESPACE} eric-inject-ns-
}

main(){
    FAILED_COMMANDS=() 
    delete_mxe_training 
    delete_mxe_workflow
    delete_mxe_exploration 
    delete_mxe_serving
    delete_deployer_helm_release
    delete_commons_helm_release

    if [ ${#FAILED_COMMANDS[@]} -gt 0 ]; then
        echo "Following commands failed.. Inspect log for more details & cleanup leftover resources manually before re-running the pipeline"
        printf '%s\n' "${FAILED_COMMANDS[@]}"
        exit 1
    fi
    exit 0
}
main
