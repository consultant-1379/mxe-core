#!/bin/bash  
## Script restarts the internal ingress controller
## Verifies if istio proxy is injected

set -x

verify_proxy_sidecar_exists() {
    local label=$1
    local resourceType=$2

    ## kubectl rollout status exits for deployments before previously running pods finish terminatation, this messes up the side car existence check
    ## to make sure we only check *newly* created pods, make sure no of running pods == no of replicas
    noOfExpectedReplicas=$(kubectl get ${resourceType} -l "${label}" -n ${MXE_NAMESPACE} -o jsonpath='{.items[0].spec.replicas}')
    noOfActualReplicas=0
    i=0
     while [[ $i -le 9 ]]; do       
        noOfActualReplicas=$(kubectl get pods -l "${label}" -n ${MXE_NAMESPACE} -o name | wc -l)
        echo "\n Expected $noOfExpectedReplicas replicas, Actually $noOfActualReplicas replicas are running "
        if [[ $noOfActualReplicas -ne $noOfExpectedReplicas ]]
        then 
          let "i=i+1"
          echo "\n Waiting to ensure all old replicas to finish terminating... "
          sleep 6s
        else
          break
        fi
    done

    if [[ $noOfActualReplicas -ne $noOfExpectedReplicas ]]; then
        echo "\n Expected $noOfExpectedReplicas replicas, but see $noOfActualReplicas replicas running "
        return 1
    fi

    invalid_pods=0
    for podName in $(kubectl get pods -l "${label}" --field-selector=status.phase==Running -n ${MXE_NAMESPACE} -o name); do
        echo "\n Label:$label  PodName:${podName}"
        proxyContainerCount=$(kubectl get ${podName} -n ${MXE_NAMESPACE} -o jsonpath='{.spec.containers[*].name}' | grep -c "istio-proxy")
        echo "\n no of proxy containers in ${podName} is ${proxyContainerCount}"
        if [ $proxyContainerCount -ne 1 ]; then
            echo "\n no of proxy containers in ${podName} should be 1 but is ${proxyContainerCount}"
            let "invalid_pods=invalid_pods+1"
        fi
    done
    return $invalid_pods
}

function verify_sidecar_exists_in_ingress_ctrlr() {
    # Get the name of Ingress controller deployment
    INGRESS_CTRL_DEPLOY=$(kubectl get deployment -n ${MXE_NAMESPACE} -l app.kubernetes.io/name=eric-mxe-ingress-controller -o name)
    verify_proxy_sidecar_exists "app.kubernetes.io/name=eric-mxe-ingress-controller" "deployment"
    proxy_injection_status=$?
    if [[ $proxy_injection_status -ne 0 ]]; then
        echo "\n Proxy sidecar does not exist in ${INGRESS_CTRL_DEPLOY}"
        exit 1
    fi
}

function ingress_add_annotation {
    echo "MXE_NAMESPACE: ${MXE_NAMESPACE}  replacing the ingress configuration snippet to gatekeepers discovery url"
    # Get the name of Ingress
    INGRESS_ERIC_SEC="ingress.networking.k8s.io/eric-sec-access-mgmt-ingress"
    kubectl patch ${INGRESS_ERIC_SEC} -n ${MXE_NAMESPACE} -p '{"metadata":{"annotations":{"mxe.nginx.ingress.kubernetes.io/configuration-snippet":"'"${INGRESS_CONFIG_SNIPPET}"'"}}}'
    kubectl patch ${INGRESS_ERIC_SEC} -n ${MXE_NAMESPACE} -p '{"spec":{"ingressClassName":"eric-mxe-ingress-controller-class"}}'
    echo "MXE_NAMESPACE: ${MXE_NAMESPACE} added configuration annotation for kc ingress"
}

# 1. Update the ingress annotation to handle - oauth based discovery url
ingress_add_annotation

# 2. Get the value of label: "istio-injection" for the namespace in which mxe is installed
ISTIO_INJECTION=$(kubectl get namespace ${MXE_NAMESPACE} -o=jsonpath='{.metadata.labels.istio-injection}')

# If the namespace doesn't contains the label, then istio sidecar injection is considered to be disabled
[ -z "$ISTIO_INJECTION" ] && ISTIO_INJECTION="disabled"

echo "ISTIO_INJECTION: $ISTIO_INJECTION"
if [ "$ISTIO_INJECTION" == "enabled" ]
then
    echo "MXE_NAMESPACE: ${MXE_NAMESPACE} is enabled for istio sidecar injection"
    verify_sidecar_exists_in_ingress_ctrlr
else
    echo "Invalid configuration.. MXE_NAMESPACE: ${MXE_NAMESPACE} is not enabled for istio sidecar injection, so exiting"
    exit 1
fi

