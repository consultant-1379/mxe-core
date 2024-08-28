#!/bin/bash
set -x 
TOKEN=$(argocd account generate-token)

case "$(uname -s)" in
    Darwin*) SCRIPT=$(greadlink -f $0) ;;
    *)       SCRIPT=$(readlink -f $0)
esac

SCRIPTPATH=$(dirname $SCRIPT)
DATAPATH=$(cd $SCRIPTPATH && cd .. && pwd)/data

curl -XPOST localhost:7543/v1/package -H"Authorization: Bearer $TOKEN" --form archive=@${DATAPATH}/argowfDir.zip --form 'options={
    "appName": "mee-argo-wf",
    "packageSource": {
        "repoURL": "https://gitlab.internal.ericsson.com/enxxram/depmanager_test.git",
        "path": "argowf",
        "targetRevision": "master"
    },
    "packageDestination": {
        "namespace": "argocd",
        "server": "https://kubernetes.default.svc"
    },
    "info": [
        {
            "name": "issuedBy",
            "value": "enxxram"
        }
    ]
}' -vvv --trace-ascii -