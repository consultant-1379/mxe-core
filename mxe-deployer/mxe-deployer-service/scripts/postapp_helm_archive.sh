#!/bin/bash

TOKEN=$(argocd account generate-token)

case "$(uname -s)" in
    Darwin*) SCRIPT=$(greadlink -f $0) ;;
    *)       SCRIPT=$(readlink -f $0)
esac

SCRIPTPATH=$(dirname $SCRIPT)
DATAPATH=$(cd $SCRIPTPATH && cd .. && pwd)/data

curl -XPOST localhost:7543/v1/package -H"Authorization: Bearer $TOKEN" --form archive=@${DATAPATH}/ambassador.tar.gz --form 'options={
    "appName": "aks-ambassador",
    "packageSource": {
        "repoURL": "https://gitlab.internal.ericsson.com/enxxram/depmanager_test.git",
        "path": "ambassador",
        "targetRevision": "master"
    },
    "packageDestination": {
        "namespace": "ambassador",
        "server": "https://kubernetes.default.svc"
    },
    "info": [
        {
            "name": "issuedBy",
            "value": "enxxram"
        }
    ],
    "syncPolicy":{
        "automated":{
            "prune": true,
            "selfHeal": true,
            "allowEmpty" : false,
        },
        "syncOptions":["CreateNamespace=True"],

    }
}' -vvv