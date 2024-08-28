#!/bin/bash
set -x
SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
REPOROOT=$(dirname $SCRIPTPATH)

MXE_DEPLOY_CLI="${REPOROOT}/cli/mxe-deploy"

appName=$1
packageName=$2
repoPath=$3
revision=$4
namespace=$5
destServer=$6
valuesFile=$7
gitOpsRepo=$8
sleepFor=$9
iterations=${10}
syncOptions=${11}

${MXE_DEPLOY_CLI} package create "${appName}" \
    --packageName "${packageName}" \
    --repo "${gitOpsRepo}" \
    --path "${repoPath}" \
    --revision "${revision}" \
    --dest-namespace "${namespace}" \
    --dest-server "${destServer}" \
    --values "${valuesFile}" \
    --label "issuedBy=jenkinsci" \
    --init-sync $([ -n "${syncOptions}" ] && echo "${syncOptions}")

deployStatus=$?

if [[ $deployStatus -ne 0 ]]; then
    printf "Installation of ${appName} failed"
    exit ${deployStatus}
fi

${MXE_DEPLOY_CLI} package wait "${appName}" --check-interval "${sleepFor}" --max-attempts "${iterations}"

waitStatus=$?

if [[ $waitStatus -ne 0 ]]; then
    printf "App is not synced/fully healthy.. Aborting.."
    exit $waitStatus
fi
