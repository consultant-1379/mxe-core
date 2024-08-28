#!/usr/bin/env bash
set -x

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
REPOROOT=$(dirname $SCRIPTPATH)

NAMESPACE=$1 
DEPLOYER_NAMESPACE=$2

MXE_DEPLOY_CLI="${REPOROOT}/cli/mxe-deploy"
LOGS_DIR="${REPOROOT}/logs"
TRAINING_PACKAGER_LOGS_DIR="${LOGS_DIR}/training-packager-logs"
MODEL_PACKAGER_LOGS_DIR="${LOGS_DIR}/model-packager-logs"

export_log(){
selector=$1
outputFile=$2
namespace=${3:-${NAMESPACE}}
kubectl logs -n ${namespace}  --prefix=true -l "$selector" --all-containers=true  --tail=20000 --max-log-requests 10 > ${outputFile} || true
}

mkdir -p ${LOGS_DIR} ${TRAINING_PACKAGER_LOGS_DIR} ${MODEL_PACKAGER_LOGS_DIR}

for job in $(kubectl get jobs -n ${NAMESPACE} -l app.kubernetes.io/component=packager,app.kubernetes.io/instance=mxe-training --output=jsonpath={.items..metadata.name}) 
do 
    export_log "job-name=$job"  "${TRAINING_PACKAGER_LOGS_DIR}/${job}.log"
done

for job in $(kubectl get jobs -n ${NAMESPACE} -l app.kubernetes.io/component=packager,app.kubernetes.io/instance=mxe-serving --output=jsonpath={.items..metadata.name}) 
do 
    export_log "job-name=$job"  "${MODEL_PACKAGER_LOGS_DIR}/${job}.log" 
done
        
export_log 'app=nginx-ingress' "${LOGS_DIR}/default-ingress-controller.log" "kube-system"
export_log 'app.kubernetes.io/name=ambassador'  "${LOGS_DIR}//ambassador.log"
export_log 'app.kubernetes.io/name=eric-mxe-gatekeeper' "${LOGS_DIR}/eric-mxe-gatekeeper.log"
export_log 'app.kubernetes.io/name=eric-sec-access-mgmt' "${LOGS_DIR}/eric-sec-access-mgmt.log"
export_log 'app.kubernetes.io/name=eric-mxe-model-service' "${LOGS_DIR}/mxe-model-service.log"
export_log 'app.kubernetes.io/name=eric-mxe-model-catalogue-service' "${LOGS_DIR}/mxe-model-catalogue-service.log"
export_log 'app.kubernetes.io/name=eric-mxe-model-training-service' "${LOGS_DIR}/eric-mxe-model-training-service.log"
export_log 'app.kubernetes.io/name=eric-mxe-ingress-controller' "${LOGS_DIR}/eric-mxe-ingress-controller.log"
export_log 'app.kubernetes.io/name=eric-mxe-deployer-service' "${LOGS_DIR}/mxe-deployer-service.log"  ${DEPLOYER_NAMESPACE}
export_log 'app.kubernetes.io/name=argocd-application-controller' "${LOGS_DIR}/argocd-application-controller.log" ${DEPLOYER_NAMESPACE}
export_log 'app.kubernetes.io/name=argocd-repo-server' "${LOGS_DIR}/argocd-repo-server.log" ${DEPLOYER_NAMESPACE}
export_log 'app.kubernetes.io/name=argocd-server' "${LOGS_DIR}/argocd-server.log" ${DEPLOYER_NAMESPACE}
export_log 'app.kubernetes.io/name=gitea' "${LOGS_DIR}/mxe-deployer-gitea.log" ${DEPLOYER_NAMESPACE}
export_log 'app.kubernetes.io/name=memcached' "${LOGS_DIR}/mxe-deployer-gitea-memcached.log" ${DEPLOYER_NAMESPACE}
export_log 'app.kubernetes.io/name=spark-operator' "${LOGS_DIR}/mxe-workflow-spark-operator.log"
export_log 'spark-role=driver' "${LOGS_DIR}/mxe-workflow-spark-driver.log"
export_log 'spark-role=executor' "${LOGS_DIR}/mxe-workflow-spark-executor.log"


kubectl logs -n ${DEPLOYER_NAMESPACE}  jobs/post-install-gitea-create-user > "${LOGS_DIR}/post-install-gitea-create-user.log" || true
kubectl get events --all-namespaces --sort-by='{.metadata.creationTimestamp}' --output=json > "${LOGS_DIR}/events.json" || true
${MXE_DEPLOY_CLI} package list -o json  > "${LOGS_DIR}/argocd-apps-list.json" || true