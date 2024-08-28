#!/bin/bash


case "$(uname -s)" in
    Darwin*) SCRIPT=$(greadlink -f $0) ;;
    *)       SCRIPT=$(readlink -f $0)
esac

SCRIPTPATH=$(dirname $SCRIPT)
DATAPATH=$(cd $SCRIPTPATH && cd .. && pwd)/data

SERVER_URL=localhost:7543

TOKEN=$(${SCRIPTPATH}/postSession.sh | jq -r .Token)

curl -XPATCH ${SERVER_URL}/v1/package -H"Authorization: Bearer $TOKEN" --form archive=@${DATAPATH}/argowfDir.zip  --form 'appSelector={
    "name": "mee-argo-wf",
}' -vvv