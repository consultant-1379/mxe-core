#!/usr/bin/env bash
set -e 
set -x 

############ WARNING: This script is not used ####################
########## It is referenced in Nightly pipeline which is currently outdated and unused #############

HELM_BINARY="${1}"
NAMESPACE="${2}"
TIMEOUT="${3}"
APPCHART_TMPL="${4}"
APPCHART_DIR="${5}"
MXE_VALUES="${6}"
MXE_DEPLOYER_VALUES="${7}"
MXE_APP_EXTRA_VALUES="${8}"
ERIC_SEC_SIP_TLS_CRD_CHART="${9}"
ERIC_DATA_DISTRIBUTED_COORDINATOR_ED_CHART="${10}"
ERIC_SEC_KEY_MANAGEMENT_CHART="${11}"
ERIC_SEC_SIP_TLS_CHART="${12}"
SM_CONTROLLER_CRD_CHART="${13}"
ERIC_MESH_CONTROLLER_CHART="${14}"
ERIC_CTRL_BRO_CHART="${15}"
MXE_HELM_REPO_URL="${16}"
VERSION="${17}"
USERNAME="${18}"

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
REPOROOT=$(dirname $SCRIPTPATH)
HELM_BASE="${SCRIPTPATH}/helm_base.sh"

${HELM_BASE} --helmBinary="${HELM_BINARY}" --repoURL="${MXE_HELM_REPO_URL}" \
        --repoName="mxe" --operation="repo_add"

${HELM_BASE} --helmBinary="${HELM_BINARY}" --releaseName="eric-sec-sip-tls-crd-${USERNAME}" \
        --chart="${ERIC_SEC_SIP_TLS_CRD_CHART}" --timeout="${TIMEOUT}" --namespace="${NAMESPACE}" \
        --atomic --operation="upgrade_install"

${HELM_BASE} --helmBinary="${HELM_BINARY}" --releaseName="eric-data-distributed-coordinator-ed-${USERNAME}" \
        --chart="${ERIC_DATA_DISTRIBUTED_COORDINATOR_ED_CHART}" --timeout="${TIMEOUT}" --namespace="${NAMESPACE}" \
        --operation="install"

${HELM_BASE} --helmBinary="${HELM_BINARY}" --releaseName="eric-sec-key-management-${USERNAME}" \
        --chart="${ERIC_SEC_KEY_MANAGEMENT_CHART}" --timeout="${TIMEOUT}" --namespace="${NAMESPACE}" \
        --operation="install"

${HELM_BASE} --helmBinary="${HELM_BINARY}" --releaseName="eric-sec-sip-tls-${USERNAME}" \
        --chart="${ERIC_SEC_SIP_TLS_CHART}" --timeout="${TIMEOUT}" --namespace="${NAMESPACE}" \
        --operation="install"

${HELM_BASE} --helmBinary="${HELM_BINARY}" --releaseName="eric-mesh-controller-crd-${USERNAME}" \
        --chart="${SM_CONTROLLER_CRD_CHART}" --timeout="${TIMEOUT}" --namespace="${NAMESPACE}" \
        --valuesFile="${MXE_VALUES}" --operation="upgrade_install"

${HELM_BASE} --helmBinary="${HELM_BINARY}" --releaseName="eric-mesh-controller-${USERNAME}" \
        --chart="${ERIC_MESH_CONTROLLER_CHART}" --timeout="${TIMEOUT}" --namespace="${NAMESPACE}" \
        --valuesFile="${MXE_VALUES}" --set telemetry.enabled=true --set telemetry.v2.enabled=true \
        --operation="install"

${HELM_BASE} --helmBinary="${HELM_BINARY}" --releaseName="eric-ctrl-bro-${USERNAME}" \
        --chart="${ERIC_CTRL_BRO_CHART}" --timeout="${TIMEOUT}" --namespace="${NAMESPACE}" \
        --valuesFile="${MXE_VALUES}" --operation="upgrade_install"

${HELM_BASE} --helmBinary="${HELM_BINARY}" --releaseName="mxe-commons-${USERNAME}" \
        --chart="mxe/mxe-commons" --version="${VERSION}" --timeout="${TIMEOUT}" \
        --namespace="${NAMESPACE}" --valuesFile="${MXE_VALUES}" --operation="install" 

${HELM_BASE} --helmBinary="${HELM_BINARY}" --releaseName="mxe-deployer-${USERNAME}" \
        --chart="mxe/mxe-deployer" --version="${VERSION}" --timeout="${TIMEOUT}" \
        --namespace="${NAMESPACE}" --valuesFile="${MXE_DEPLOYER_VALUES}" --operation="install"  

${HELM_BASE} --chartTemplate="${APPCHART_TMPL}" --chartDir="${APPCHART_DIR}" \
        --version=${VERSION} --repoURL="${MXE_HELM_REPO_URL}" --operation="make_app_chart"

${HELM_BASE}  --helmBinary="${HELM_BINARY}" --chartDir="${APPCHART_DIR}" \
        --operation="dependency_update"

${HELM_BASE}  --helmBinary="${HELM_BINARY}" --releaseName="mxe-apps-${USERNAME}" \
        --chart="${APPCHART_DIR}" --version="${VERSION}" --timeout="${TIMEOUT}" \
        --namespace="${NAMESPACE}" --valuesFile="${MXE_VALUES}" \
        --extraValuesFile="${MXE_APP_EXTRA_VALUES}" --operation="install"
