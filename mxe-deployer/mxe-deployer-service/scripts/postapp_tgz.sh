#!/bin/bash
set -x

case "$(uname -s)" in
    Darwin*) SCRIPT=$(greadlink -f $0) ;;
    *)       SCRIPT=$(readlink -f $0)
esac

SCRIPTPATH=$(dirname $SCRIPT)
DATAPATH=$(cd $SCRIPTPATH && cd .. && pwd)/data

#SERVER_URL=https://deployerservice.mee-argocd.cram010.rnd.gic.ericsson.se
SERVER_URL=localhost:7543

TOKEN=$(${SCRIPTPATH}/postSession.sh | jq -r .Token)

curl -XPOST ${SERVER_URL}/v1/package -H"Authorization: Bearer $TOKEN" --form archive=@${DATAPATH}/argowfDir.tar.gz --form 'options={
    "appName": "mee-argo-wf",
    "project": "default",
    "packageSource": {
        "repoURL": "https://gitlab.internal.ericsson.com/enxxram/depmanager_test.git",
        "path": "argowf",
        "targetRevision": "master"
    },
    "packageDestination": {
        "namespace": "argocd",
        "server": "https://kubernetes.default.svc"
    },
    "labels": [ "issuedBy=enxxram"],
    "syncPolicy":{},
}' 
#-vvv --trace-ascii -