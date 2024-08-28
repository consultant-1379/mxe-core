#!/usr/bin/env bash

############ WARNING: This script is not used ####################
########## It is referenced in Nightly pipeline which is currently outdated and unused #############

helm_install() {
    ${HELM_BINARY} install "${RELEASE_NAME}" "${CHART}" $([ -n "${VERSION}" ] && echo "--version=${VERSION}") \
        --debug --wait --timeout="${TIMEOUT}" --namespace "${NAMESPACE}" $([ -n "${VALUES_FILE}" ] && echo "--values ${VALUES_FILE}")  $([ -n "${EXTRA_VALUES_FILE}" ] && echo "--values ${EXTRA_VALUES_FILE}")
    helmStatus=$?
    if [ $helmStatus -ne 0 ]; then
        echo "helm install failed with status code $helmStatus"
        exit $helmStatus
    fi
}

helm_upgrade() {
    ${HELM_BINARY} upgrade  "${RELEASE_NAME}" "${CHART}"  $([ -n "${VERSION}" ] && echo "--version=${VERSION}") \
        --debug --wait --timeout="${TIMEOUT}" --namespace "${NAMESPACE}" $([ -n "${VALUES_FILE}" ] && echo "--values ${VALUES_FILE}") $([ -n "${EXTRA_VALUES_FILE}" ] && echo "--values ${EXTRA_VALUES_FILE}")
    helmStatus=$?
    if [ $helmStatus -ne 0 ]; then
        echo "helm upgrade failed with status code $helmStatus"
        exit $helmStatus
    fi
}

helm_upgrade_install(){
    ${HELM_BINARY} upgrade --install "${RELEASE_NAME}" "${CHART}"  $([ -n "${VERSION}" ] && echo "--version=${VERSION}") \
        --debug --wait --timeout="${TIMEOUT}" --namespace "${NAMESPACE}" $([ -n "${VALUES_FILE}" ] && echo "--values ${VALUES_FILE}") $([ -n "${EXTRA_VALUES_FILE}" ] && echo "--values ${EXTRA_VALUES_FILE}")
    helmStatus=$?
    if [ $helmStatus -ne 0 ]; then
        echo "helm upgrade failed with status code $helmStatus"
        exit $helmStatus
    fi
}

helm_dependency_update(){
    ${HELM_BINARY} dependency update "${CHART_DIR}"
    helmStatus=$?
    if [ $helmStatus -ne 0 ]; then
        echo "helm dependency update failed with status code $helmStatus"
        exit $helmStatus
    fi
}

helm_repo_add(){
    ${HELM_BINARY} repo add ${REPO_NAME} ${HELM_REPO_URL} \
    && ${HELM_BINARY} repo update
}

make_mxe_app_chart_dir(){
    mkdir -p "${CHART_DIR}"
    cat "${CHART_TMPL}"  | sed "s/{{ mxe_version }}/${VERSION}/g" \
        | sed "s#{{ mxe_helm_repo_url }}#${HELM_REPO_URL}#g" > "${CHART_DIR}/Chart.yaml"
}


usage()
{
    echo -e "${PWD}/helm_base.sh    
            --helmBinary <path to helm>
            --repoURL <helm repo url>
            --repoName <helm reponame>
            --releaseName <helm releasename> 
            --chart <chartname or url or path> 
            --timeout <command timeout> 
            --namespace <installation namespace> 
            --valuesFile <chart values file> 
            --version <chart version> 
            --chartTemplate <chart.yaml template> 
            --chartDir <folder where chart should be created using template> 
            --extraValuesFile <additional values.yaml file>
            --operation <one of install|upgrade|upgrade_install|dependency_update|repo_add|make_app_chart"
}

printParsedParams(){
    paramKeys=(HELM_BINARY
            HELM_REPO_URL
            REPO_NAME
            RELEASE_NAME
            CHART
            TIMEOUT
            NAMESPACE
            VALUES_FILE
            VERSION
            CHART_TMPL
            CHART_DIR
            EXTRA_VALUES_FILE
            OPERATION
    )

    for param in "${paramKeys[@]}"
    do 
        if [ -n "${!param}" ]
        then 
            echo -e "KEY: $param, VALUE: ${!param}"
        fi 
    done
}

while [ "$1" != "" ]; do
    PARAM=`echo $1 | awk -F= '{print $1}'`
    VALUE=`echo $1 | awk -F= '{OFS="=";$1=""; printf substr($0,2)}'`
    case $PARAM in
        -h | --help)
            usage
            exit
            ;;
        --helmBinary)
            HELM_BINARY="${VALUE}"
            ;;
        --repoURL)
            HELM_REPO_URL="${VALUE}"
            ;;
        --repoName)
            REPO_NAME="${VALUE}"
            ;;
        --releaseName)
            RELEASE_NAME="${VALUE}" 
            ;;
        --chart)
            CHART="${VALUE}" 
            ;;    
        --timeout)
            TIMEOUT="${VALUE}"
            ;;
        --namespace)
            NAMESPACE="${VALUE}"
            ;;
        --valuesFile)
            VALUES_FILE="${VALUE}"
            ;;
        --version)
            VERSION="${VALUE}"
            ;;
        --chartTemplate)
            CHART_TMPL="${VALUE}"
            ;;
        --chartDir)
            CHART_DIR="${VALUE}"
            ;;
        --extraValuesFile)
            EXTRA_VALUES_FILE="${VALUE}"
            ;;
        --operation)
            OPERATION="${VALUE}"
            ;;
        *)
            echo "ERROR: unknown parameter \"$PARAM\""
            usage
            exit 1
            ;;
    esac
    shift
done

printParsedParams 

if [ -n "${OPERATION}" ]; then 
    case "${OPERATION}" in 
        install)
            helm_install
            exitCode=$?
        ;;
        upgrade)
            helm_upgrade
            exitCode=$?
        ;;
        upgrade_install)
            helm_upgrade_install
            exitCode=$?
        ;;
        dependency_update)
            helm_dependency_update
            exitCode=$?
        ;;
        repo_add)
            helm_repo_add
            exitCode=$?
        ;;
        make_app_chart)
            make_mxe_app_chart_dir
            exitCode=$?
        ;;
        *)
            echo "ERROR: unknown operation $OPERATION"
            usage
            exit 1
        ;;
    esac
    if [[ -n "${exitCode}" && ${exitCode} -ne 0 ]]; then 
        echo -e "Operation $OPERATION failed with exit code ${exitCode}"
        exit ${exitCode}
    else 
        echo "Operation ${OPERATION} is executed successfully"
    fi 
else 
    echo "--operation flag is not supplied"
fi
