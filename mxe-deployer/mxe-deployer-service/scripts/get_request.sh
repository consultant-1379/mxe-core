#!/bin/bash
set -x

case "$(uname -s)" in
    Darwin*) SCRIPT=$(greadlink -f $0) ;;
    *)       SCRIPT=$(readlink -f $0)
esac

SCRIPTPATH=$(dirname $SCRIPT)
DATAPATH=$(cd $SCRIPTPATH && cd .. && pwd)/data

#SERVER_URL=localhost:7543
SERVER_URL=https://mxe-deployer.olah024.rnd.gic.ericsson.se

TOKEN=$(${SCRIPTPATH}/postSession.sh | jq -r .Token)
#echo "List Cluster EP:"
#curl -XGET localhost:7543/v1/listclusters -H"Authorization: Bearer $TOKEN"

#echo "List Packages EP:"
#curl -XGET ${SERVER_URL}/v1/package -H"Authorization: Bearer $TOKEN"

#sleep 1

#echo "List Packages EP:"
curl --request POST  -vvv --header "Authorization: Bearer $TOKEN" --url "${SERVER_URL}/v1/package/sync" -d '{ "applicationSyncReq": { "name" : "mxe-training", "prune": true } }'
sleep 1

echo "Delete Packages EP:"
##curl -XDELETE ${SERVER_URL}/v1/package?name=mee-argo-wf -H"Authorization: Bearer $TOKEN"

#sleep 1

#echo "List Packages EP:"
#curl -XGET localhost:7543/v1/package?packageType=system -H"Authorization: Bearer $TOKEN"