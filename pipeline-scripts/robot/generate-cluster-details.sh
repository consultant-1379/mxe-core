#! /usr/bin/env bash
set -x 

FILE_NAME=$1
MXE_HOST=$2
MXE_USER=$3
MXE_PASSWORD=$4
KEYCLOAK_USERNAME=$5 
KEYCLOAK_PASSWORD=$6
NAMESPACE=$7
OAUTH_API_HOST=$8

cat >"${FILE_NAME}" << EOF
mxe_host = "${MXE_HOST}"
mxe_username = "${MXE_USER}"
mxe_password = "${MXE_PASSWORD}"
invalid_mxe_password = 'invalid-password'
keycloak_url = "${MXE_HOST}/auth/"
keycloak_username = "${KEYCLOAK_USERNAME}"
keycloak_password = "${KEYCLOAK_PASSWORD}"
mxe_namespace = "${NAMESPACE}"
oauth_api_host = "${OAUTH_API_HOST}"
EOF
