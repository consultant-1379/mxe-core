#!/usr/bin/env bash

set -e
set -x

. init.sh

mapfile -t ingressservices < <(fetch_api "/api/v1/namespaces/${namespace}/services?labelSelector=app.kubernetes.io%2Fcomponent=ingress-service,app.kubernetes.io%2Fpart-of=mxe" | jq -c -r '.items | .[]')

for mxeingressservice in "${ingressservices[@]}"; do
  name=$(echo "${mxeingressservice}" | jq -r '.metadata.name')

  echo "${name}"

  fetch_api "/api/v1/namespaces/${namespace}/services/${name}" "DELETE"
done

fetch_api "/apis/security.istio.io/v1beta1/namespaces/${namespace}/authorizationpolicies?labelSelector=app.kubernetes.io%2Fpart-of=mxe" DELETE
fetch_api "/apis/security.istio.io/v1beta1/namespaces/${namespace}/requestauthentications?labelSelector=app.kubernetes.io%2Fpart-of=mxe" DELETE

sleep 30s