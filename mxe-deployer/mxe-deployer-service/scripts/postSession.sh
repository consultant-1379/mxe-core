#!/bin/bash
set -x
case "$(uname -s)" in
    Darwin*) SCRIPT=$(greadlink -f $0) ;;
    *)       SCRIPT=$(readlink -f $0)
esac

SCRIPTPATH=$(dirname $SCRIPT)
DATAPATH=$(cd $SCRIPTPATH && cd .. && pwd)/data
SERVER_URL=https://mxe-deployer.olah024.rnd.gic.ericsson.se
OAUTH_HOST=https://oauth.mxe.olah024.rnd.gic.ericsson.se
USERNAME=argocd-admin
PASSWORD=admin-pass
curl -XPOST "${SERVER_URL}/v1/session" -H"Content-Type:application/json" --data "{ \"ssoMode\":true, \"ssoHost\": \"${OAUTH_HOST}\", \"username\": \"${USERNAME}\", \"password\": \"${PASSWORD}\"}"
#curl -XPOST "${SERVER_URL}/v1/session" -H"Content-Type:application/json" --data '{"username": "admin", "password":"mxe_argocd"}'