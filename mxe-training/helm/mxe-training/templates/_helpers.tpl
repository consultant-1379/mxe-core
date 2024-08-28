{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the secret.
Should not be changed as subcharts reference this by value.
*/}}
{{- define "mxe-training.pg-secret-name" -}}
{{- default "mxe-db-password" .Values.secret.database.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "mxe-training.docker-registry-secret-name" -}}
{{- default "mxe-docker-registry-secret" .Values.secret.registry.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "mxe-training.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}


{{/*
Product information of Ericsson products (DR-D1121-064, DR-D1121-067)
*/}}
{{- define "mxe-training.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}

{{/*
Expand the name of the chart.
*/}}
{{- define "mxe-training.post-install-hook-istio-name" -}}
{{- default "post-install-hook-istio" .Values.mxePostInstallHookIstio.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Image pull secrets
*/}}
{{- define "mxe-training.post-install-hook-istio.image-pull-secrets" -}}
{{- if .Values.imageCredentials.registry.url -}}
{{- if .Values.imageCredentials.registry.pullSecret -}}
imagePullSecrets:
  - name: {{ .Values.imageCredentials.registry.pullSecret | quote -}}
{{- end -}}
{{- else -}}
{{- if .Values.global.registry.pullSecret -}}
imagePullSecrets:
  - name: {{ .Values.global.registry.pullSecret | quote -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Image repository
*/}}
{{- define "mxe-training.post-install-hook-istio.image-repository" -}}
{{- if .Values.imageCredentials.registry.url -}}
{{- .Values.imageCredentials.registry.url -}}
{{- else -}}
{{- .Values.global.registry.url -}}
{{- end -}}
{{- printf "/%s/" .Values.imageCredentials.repoPath -}}
{{- end -}}


{{/*
Istio resources - Request Authentication
*/}}
{{- define "mxe-training.post-install-hook-istio-req-authn-name" -}}
{{- default "post-install-hook-istio-req-authn" .Values.mxePostInstallHookIstio.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Istio resources - Keycloak service name based url for issuer
*/}}
{{- define "mxe-training.keycloak-service-path" -}}
http://eric-sec-access-mgmt-http:8080
{{- end -}}

{{/*
Istio resources - Keycloak host name based url for issuer
*/}}
{{- define "mxe-training.api-host-path" -}}
{{- printf "https://%s" .Values.global.mxeApiHostname -}}
{{- end -}}

{{/*
Istio resources - Request Authentication rules
*/}}
{{- define "mxe-training.istio-req-authn-jwt-rules" -}}
jwtRules:
- issuer: "{{ include "mxe-training.keycloak-service-path" .}}/auth/realms/mxe"
  jwksUri: "{{ include "mxe-training.api-host-path" .}}/auth/realms/mxe/protocol/openid-connect/certs"
  forwardOriginalToken: true
- issuer: "{{ include "mxe-training.keycloak-service-path" .}}/auth/realms/mxe"
  jwksUri: "{{ include "mxe-training.api-host-path" .}}/auth/realms/master/protocol/openid-connect/certs"
  forwardOriginalToken: true
- issuer: "{{ include "mxe-training.api-host-path" .}}/auth/realms/mxe"
  jwksUri: "{{ include "mxe-training.api-host-path" .}}/auth/realms/mxe/protocol/openid-connect/certs"
  forwardOriginalToken: true
- issuer: "{{ include "mxe-training.api-host-path" .}}/auth/realms/master"
  jwksUri: "{{ include "mxe-training.api-host-path" .}}/auth/realms/master/protocol/openid-connect/certs"
  forwardOriginalToken: true
{{- end -}}

{{/*
Istio resources - Authorization Policy
*/}}
{{- define "mxe-training.post-install-hook-istio-authz-policy-name" -}}
{{- default "post-install-hook-istio-authz-policy" .Values.mxePostInstallHookIstio.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Istio resources - Authorization Policy rules
*/}}
{{- define "mxe-training.istio-authz-policy-rules" -}}
action: ALLOW
rules:
# 1. Whitelist urls - 
#    i. Allow anyone to access keycloak 
#    ii. Any realm user to access MXE UI
- to:
  # The below rule doesn't have `from` allowing any access (without JWT) to access whitelisted urls
  # https://github.com/istio/istio/issues/27432
  - operation:
      paths: 
      - "/"
      - "/auth*"
      - "/oauth/*"
      - "/mxe"
      - "/favicon.ico"
      - "/config/*"
      - "/locale/*"
      - "/components/*" 
      - "/panels/*"
      - "/apps/*"
      - "/assets/*"
      - "/libs/*"
      - "/plugins/*"
      - "/v1/prometheus/*"
# 2. Allow "mxe-user" (role: mxe_default_role) to "READ_WRITE" access all path "/*"
- to:
  - operation:
      paths: ["/*"]
  when:
  - key: request.auth.claims[iss]
    values: ["{{ include "mxe-training.keycloak-service-path" .}}/auth/realms/mxe"]
  - key: request.auth.claims[roles]
    values: ["mxe_default_role"]
# 3. Allow existing Access control policy users to access models/model-services endpoints
#     Role: mxe_model_serving_role 
#     Access: READ_WRITE
#     Paths "/v1/models, /v1/models/*, /v1/model-services, /v1/model-services/*, "
- to:
  - operation:
      paths: ["/v1/models", "/v1/models/*", "/v1/model-services", "/v1/model-services/*"]
  when:
  - key: request.auth.claims[iss]
    values: ["{{ include "mxe-training.keycloak-service-path" .}}/auth/realms/mxe"]
  - key: request.auth.claims[roles]
    values: ["mxe_model_serving_role"]
# 4. Allow Kubernetes service (dynamic seldon model) to pull docker image - 
# (TODO: Temp workaround, TO_BE_REMOVED)
- to:
  - operation:
      paths: ["/*"]
      methods: ["GET"]
  when:
  - key: request.auth.claims["iss"]
    values: ["kubernetes/serviceaccount"]
  - key: request.auth.claims["sub"]
    values: ["system:serviceaccount:{{.Release.Namespace}}:default"]  
{{- end -}}