#!/usr/bin/env bash 

set -eux

SCRIPT=$(readlink -f $0)
NMAP_DIR=$(dirname $SCRIPT)
PIPELINE_SCRIPTS_DIR=$(dirname $NMAP_DIR)
REPOROOT=$(dirname $PIPELINE_SCRIPTS_DIR)

if [[ -z $ADP_API_TOKEN ]]
then 
    echo "ADP_API_TOKEN env is not set. This is required to download nmap chart from ADP. Please export it before running"
    exit 1
fi

export KUBECONFIG="/home/enxxram/.kube/configs/olah025_config"
export PWD=$(pwd -P)
export HELM_TOKEN=$ADP_API_TOKEN
export NAMESPACE=mxe 
export RULE_SET="ruleset2.0-scan.yaml"

$PIPELINE_SCRIPTS_DIR/run_rule.sh "vulnerability-check:nmap-port-scan-for-mxe-commons"
$PIPELINE_SCRIPTS_DIR/run_rule.sh "vulnerability-check:nmap-port-scan-for-mxe-deployer"
$PIPELINE_SCRIPTS_DIR/run_rule.sh "vulnerability-check:nmap-port-scan-for-mxe-components"
$PIPELINE_SCRIPTS_DIR/run_rule.sh "nmap-xml-to-html-converter"