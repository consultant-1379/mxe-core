#!/usr/bin/env bash
set -x

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
REPOROOT=$(dirname $SCRIPTPATH)

PRE_REQ_CONFIG_FILE=$1
CONFIG_FILE=$2
TLS_FLAG=$3

flag="$TLS_FLAG" yq -i e ".encryption.enable_in_transit = env(flag)" $PRE_REQ_CONFIG_FILE
flag="$TLS_FLAG" yq -i e ".mxe_commons.encryption.enable_in_transit = env(flag)" $CONFIG_FILE
flag="$TLS_FLAG" yq -i e ".mxe_commons.encryption.enable_at_rest = env(flag)" $CONFIG_FILE
