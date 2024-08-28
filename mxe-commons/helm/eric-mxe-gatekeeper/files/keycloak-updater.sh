#!/usr/bin/env bash

set -e
set -x

kubectl rollout status statefulset "${KEYCLOAK_STATEFULSET_NAME}" -n ${KEYCLOAK_STATEFULSET_NAMESPACE} --watch

if [ "${SERVICE_MESH_MTLS_ENABLED}" = false ] ; then
  kubectl exec -n ${KEYCLOAK_STATEFULSET_NAMESPACE} -c "${KEYCLOAK_STATEFULSET_CONTAINER_NAME}" "${KEYCLOAK_STATEFULSET_NAME}-0" -- /bin/bash -c "
    \${JBOSS_HOME}/bin/kcadm.sh config credentials --config \/opt/jboss/rundir-safe/kcadm.config --server http://localhost:8080/auth --realm master --user ${KEYCLOAK_SERVICE_USERNAME} --client admin-cli --password ${KEYCLOAK_SERVICE_PASSWORD} &&
    \${JBOSS_HOME}/bin/kcadm.sh update realms/master --config \/opt/jboss/rundir-safe/kcadm.config --set sslRequired=none"
fi