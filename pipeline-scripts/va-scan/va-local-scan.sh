#!/usr/bin/env bash
set -ex

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
REPOROOT=$(dirname $(dirname $SCRIPTPATH))

PIPELINE_SCRIPTS_DIR="$REPOROOT/pipeline-scripts"

export MXE_VERSION_TO_SCAN='2.3.0-176'
export GRYPE_IMAGE_TAG='1.2.6'
export GRYPE_DB_VERSION='2022-05-01T08:16:52Z'
export TRIVY_IMAGE_TAG='20220502'
export RULE_SET="ruleset2.0-va.yaml"
export USER="mxecifunc"

if [ -z $DOCKER_API_TOKEN ]
then 
    echo "DOCKER_API_TOKEN for $USER is not set, exit.."
    exit 1 
fi 
export API_TOKEN=$DOCKER_API_TOKEN

trivy_scan(){
    $PIPELINE_SCRIPTS_DIR/run_rule.sh trivy:scan-internal-images &
    local pid_internal=$!
    $PIPELINE_SCRIPTS_DIR/run_rule.sh trivy:scan-2pp-images &
    local pid_2pp=$!
    $PIPELINE_SCRIPTS_DIR/run_rule.sh trivy:scan-3pp-images &
    local pid_3pp=$!

    wait $pid_internal
    local scan_status_internal=$?
    wait $pid_2pp
    local scan_status_2pp=$?
    wait $pid_3pp
    local scan_status_3pp=$?

    echo "trivy_internal_scan_status: ${scan_status_internal}"
    echo "trivy_2pp_scan_status: ${scan_status_2pp}"
    echo "trivy_3pp_scan_status: ${scan_status_3pp}"

    $PIPELINE_SCRIPTS_DIR/run_rule.sh analyze:trivy-report
}

grype_scan(){
    pipeline-scripts/run_rule.sh anchore:scan-internal-images &
    local pid_internal=$!
    pipeline-scripts/run_rule.sh anchore:scan-2pp-images &
    local pid_2pp=$!
    pipeline-scripts/run_rule.sh anchore:scan-3pp-images &
    local pid_3pp=$!

    wait $pid_internal
    local scan_status_internal=$?
    wait $pid_2pp
    local scan_status_2pp=$?
    wait $pid_3pp
    local scan_status_3pp=$?

    echo "grype_internal_scan_status: ${scan_status_internal}"
    echo "grype_2pp_scan_status: ${scan_status_2pp}"
    echo "grype_3pp_scan_status: ${scan_status_3pp}"

    pipeline-scripts/run_rule.sh analyze:anchore-report
    pipeline-scripts/run_rule.sh analyze:os-report
}


$PIPELINE_SCRIPTS_DIR/va-scan/set_image_versions.sh
$PIPELINE_SCRIPTS_DIR/run_rule.sh clean
$PIPELINE_SCRIPTS_DIR/run_rule.sh get-mxe-images ruleset2.0.yaml
$PIPELINE_SCRIPTS_DIR/run_rule.sh init
$PIPELINE_SCRIPTS_DIR/run_rule.sh image-list

trivy_scan 
grype_scan 