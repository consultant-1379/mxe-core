#!/usr/bin/env bash

namespace="$(cat /var/run/secrets/kubernetes.io/serviceaccount/namespace)"

kubecurl() {
  curl --cacert /var/run/secrets/kubernetes.io/serviceaccount/ca.crt --header "Authorization: Bearer $(cat /var/run/secrets/kubernetes.io/serviceaccount/token)" "$@"
}

fetch_api() {
  local method="GET"
  local url="https://kubernetes.default.svc.cluster.local$1"
  if [ -n "$2" ]; then
    method="$2"
  fi
  local content_type="application/json-patch+json"
  if [ -n "$4" ]; then
    content_type="$4"
  fi
  if [ -n "$3" ]; then
    echo "$3" | kubecurl --request "${method}" --header "Content-Type: $content_type; charset=utf-8" --data @- "${url}"
  else
    kubecurl --request "${method}" "${url}"
  fi
}

create_backup_from_non_hook_secret() {
  mapfile -t secrets < <(fetch_api "/api/v1/namespaces/${namespace}/secrets?labelSelector=app.kubernetes.io%2Fpart-of=mxe,app.kubernetes.io%2Fcomponent=$1" | jq -c -r '.items | .[]')

  for secret in "${secrets[@]}"; do
    if ! echo "${secret}" | jq -e '.code' > /dev/null; then
      local secret_is_hook=$(echo "${secret}" | jq -cr '.metadata.annotations."helm.sh/hook"')

      if [ "$secret_is_hook" = "null" ]; then
        local backup_secret=$(echo "${secret}" | jq -cr --arg newName "$2" '{
          "apiVersion": "v1",
          "data": .data,
          "stringData": .stringData,
          "kind": "Secret",
          "metadata": {
              "annotations": .metadata.annotations,
              "labels": (.metadata.labels + {
                  "mxe.ericsson.se/original-name": .metadata.name,
                  "mxe.ericsson.se/remove-after-install": "true"
              }),
              "name": $newName,
              "namespace": .metadata.namespace
          }
        }')

        fetch_api "/api/v1/namespaces/${namespace}/secrets/" "POST" "${backup_secret}" "application/json"
      fi
    fi
  done
}