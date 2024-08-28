#!/usr/bin/env bash

MXE_COMMONS_NAMESPACE=${MXE_DEPLOYER_NAMESPACE}

POD_UID=$(kubectl get pods -n ${MXE_DEPLOYER_NAMESPACE} ${POD_NAME} -o=jsonpath='{.metadata.uid}')
CLIENT_SECRET=${POD_UID}

while [ -z ${KEYCLOAK_STATEFULSET_NAME} ]; do
    KEYCLOAK_STATEFULSET_NAME=$(kubectl get -n "${MXE_COMMONS_NAMESPACE}" statefulset -l "app.kubernetes.io/name=eric-sec-access-mgmt" -o=jsonpath='{.items[0].metadata.name}') || true
done
    
kubectl rollout status statefulset "${KEYCLOAK_STATEFULSET_NAME}" -n ${MXE_COMMONS_NAMESPACE} --watch

if [ "${SERVICE_MESH_MTLS_ENABLED}" = true ] ; then
    kubectl exec -n ${MXE_COMMONS_NAMESPACE} -c "${KEYCLOAK_CONTAINER_NAME}" "${KEYCLOAK_STATEFULSET_NAME}-0" -- /bin/bash -c "
    rm -rf /opt/jboss/rundir-safe; mkdir /opt/jboss/rundir-safe; keytool --import -alias sip-tls -storetype JKS -keystore /opt/jboss/rundir-safe/kcadm.truststore -storepass password -noprompt -file /run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt; \${JBOSS_HOME}/bin/kcadm.sh config truststore --trustpass password /opt/jboss/rundir-safe/kcadm.truststore --config /opt/jboss/rundir-safe/kcadm.config"

    kubectl exec -n ${MXE_COMMONS_NAMESPACE} -c "${KEYCLOAK_CONTAINER_NAME}" "${KEYCLOAK_STATEFULSET_NAME}-0" -- /bin/bash -c "
    \${JBOSS_HOME}/bin/kcadm.sh config credentials --config /opt/jboss/rundir-safe/kcadm.config --server https://eric-sec-access-mgmt-http:8443/auth --realm master --user ${KEYCLOAK_ADMIN_USERNAME} --client ${KEYCLOAK_ADMIN_CLIENT} --password ${KEYCLOAK_ADMIN_PASSWORD}"
else
    kubectl exec -n ${MXE_COMMONS_NAMESPACE} -c "${KEYCLOAK_CONTAINER_NAME}" "${KEYCLOAK_STATEFULSET_NAME}-0" -- /bin/bash -c "
    \${JBOSS_HOME}/bin/kcadm.sh config credentials --config \/opt/jboss/rundir-safe/kcadm.config --server http://localhost:8080/auth --realm master --user ${KEYCLOAK_ADMIN_USERNAME} --client ${KEYCLOAK_ADMIN_CLIENT} --password ${KEYCLOAK_ADMIN_PASSWORD}"
fi

keycloak_login_status=$?
if [[ $keycloak_login_status -ne 0 ]]; then
    echo "keycloak login failed"
    exit $keycloak_login_status
fi

clients=$(kubectl exec -n ${MXE_COMMONS_NAMESPACE} -c "${KEYCLOAK_CONTAINER_NAME}" "${KEYCLOAK_STATEFULSET_NAME}-0" -- /bin/bash -c "
    \${JBOSS_HOME}/bin/kcadm.sh get clients -r ${MXE_REALM} --fields id,clientId --config \/opt/jboss/rundir-safe/kcadm.config")

echo $clients | grep ${CLIENT_ID}
if [[ $? -eq 0 ]]; then
    echo "client already exists"
    clientId=$(echo $clients | jq -c '.[] | select( .clientId=="gitea" ) | .id')
    
    kubectl exec -n ${MXE_COMMONS_NAMESPACE} -c "${KEYCLOAK_CONTAINER_NAME}" "${KEYCLOAK_STATEFULSET_NAME}-0" -- /bin/bash -c "
    \${JBOSS_HOME}/bin/kcadm.sh delete clients/${clientId} -r ${MXE_REALM} --config \/opt/jboss/rundir-safe/kcadm.config"
    
    client_deletion_status=$?
    if [[ $client_deletion_status -ne 0 ]]; then
    echo "existing client deletion failed"
    exit $client_deletion_status
    fi
    
    echo "existing client deleted"
fi

client_creation_result=$(kubectl exec -n ${MXE_COMMONS_NAMESPACE} -c "${KEYCLOAK_CONTAINER_NAME}" "${KEYCLOAK_STATEFULSET_NAME}-0" -- /bin/bash -c "
    \${JBOSS_HOME}/bin/kcadm.sh create clients -r ${MXE_REALM} -s clientId=${CLIENT_ID} -s name=${CLIENT_NAME} -s enabled=true -s clientAuthenticatorType=client-secret -s secret=${CLIENT_SECRET} -s 'redirectUris=[\"${CLIENT_REDIRECT_URI}*\"]' --config /opt/jboss/rundir-safe/kcadm.config" 2>&1)

client_creation_status=$?
if [[ $client_creation_status -ne 0 ]]; then
    echo "client creation failed"
    exit $client_creation_status
fi

echo "client created successfully"

kubectl delete secret ${GITEA_AUTH_SOURCE_SECRET} -n ${MXE_DEPLOYER_NAMESPACE}
kubectl create secret generic ${GITEA_AUTH_SOURCE_SECRET} -n ${MXE_DEPLOYER_NAMESPACE} \
--from-literal=authName=${GITEA_AUTH_NAME} --from-literal=clientId=${CLIENT_ID} \
--from-literal=clientSecret=${CLIENT_SECRET} --from-literal=autoDiscoveryUrl=${AUTO_DISCOVERY_URL} \
--from-literal=authProvider=${GITEA_AUTH_PROVIDER}
