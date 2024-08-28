#!/usr/bin/env bash
echo '==== BEGIN GITEA AUTH SOURCE CONFIGURATION ===='

while [ -z ${GITEA_POD_NAME} ]; do
    GITEA_POD_NAME=$(kubectl get -n "${MXE_DEPLOYER_NAMESPACE}" pods -l "app.kubernetes.io/name=gitea" -o=jsonpath='{.items[0].metadata.name}') || true
done

kubectl exec -n ${MXE_DEPLOYER_NAMESPACE} "${GITEA_POD_NAME}" -c gitea -- /bin/bash -c " \
    gitea admin auth add-oauth \
    --name ${AUTH_NAME} \
    --provider ${AUTH_PROVIDER} \
    --key ${CLIENT_ID} \
    --secret ${CLIENT_SECRET} \
    --auto-discover-url ${AUTO_DISCOVERY_URL} \
    || \
    ( \
    gitea admin auth update-oauth --id 1 \
    --name ${AUTH_NAME} \
    --provider ${AUTH_PROVIDER} \
    --key ${CLIENT_ID} \
    --secret ${CLIENT_SECRET} \
    --auto-discover-url ${AUTO_DISCOVERY_URL} \
    )"
echo '==== END GITEA AUTH SOURCE CONFIGURATION ===='
