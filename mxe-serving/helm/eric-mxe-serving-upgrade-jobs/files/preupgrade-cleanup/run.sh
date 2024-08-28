#!/usr/bin/env bash

set -e
set -x

. init.sh

fetch_api "/apis/admissionregistration.k8s.io/v1beta1/validatingwebhookconfigurations/${SELDON_VALIDATING_WEBHOOK_NAME}" "DELETE" || true


sleep 30s
