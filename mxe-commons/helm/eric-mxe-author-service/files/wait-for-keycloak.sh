#!/usr/bin/env bash

set -e
set -x

kubectl rollout status statefulset "${KEYCLOAK_STATEFULSET_NAME}" -n "${KEYCLOAK_STATEFULSET_NAMESPACE}" --watch
