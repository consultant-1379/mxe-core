{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the secret.
Should not be changed as subcharts reference this by value.
*/}}
{{- define "mxe-commons.pg-secret-name" -}}
{{- default "mxe-db-password" .Values.secret.database.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "mxe-commons.docker-registry-secret-name" -}}
{{- default "mxe-docker-registry-secret" .Values.secret.registry.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the chart.
*/}}
{{- define "mxe-commons.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "mxe-commons.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}


{{/*
Product information of Ericsson products (DR-D1121-064, DR-D1121-067)
*/}}
{{- define "mxe-commons.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}

{{/*
Define the name of the configmap for storing logcontrol.json file
*/}}
{{- define "mxe-commons.log-control-configmap" -}}
{{- .Values.global.mxeLogControlConfigMap -}}
{{- end -}}

{{/*
Annotations to add to auth ingress
*/}}
{{- define "mxe-commons.ingress.auth.annotations" }}
{{- if .Values.keycloak.ingress.auth.annotations }}
{{ toYaml .Values.keycloak.ingress.auth.annotations | }}
{{- end }}
{{- end }}

{{/*
Keycloak service name
*/}}
{{- define "mxe-commons.access-mgmt-service-name" -}}
{{- default "eric-sec-access-mgmt" (index .Values "eric-sec-access-mgmt" "name") -}}
{{- end -}}

{{/*
Expand the name of the chart.
*/}}
{{- define "mxe-commons.post-install-hook-istio-name" -}}
{{- default "post-install-hook-istio" .Values.mxePostInstallHookIstio.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the chart. and create name for imagepull secret merge job's resources
*/}}
{{- define "mxe-commons.post-install-hook-pull-secrets-name" -}}
{{- default "post-install-pull-secrets" .Values.mxePostInstallPullSecret.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Image pull secrets
*/}}
{{- define "mxe-commons.post-install-hook-istio.image-pull-secrets" -}}
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
Image pull secrets
*/}}
{{- define "mxe-commons.post-install-hook-pull-secrets-merge-pull-secrets" -}}
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
Image pull secret name
*/}}
{{- define "mxe-commons.post-install-hook-pull-secrets-merge-pull-secrets-name" -}}
{{- if .Values.imageCredentials.registry.url -}}
{{- if .Values.imageCredentials.registry.pullSecret -}}
{{ .Values.imageCredentials.registry.pullSecret | quote -}}
{{- end -}}
{{- else -}}
{{- if .Values.global.registry.pullSecret -}}
{{ .Values.global.registry.pullSecret | quote -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Image repository
*/}}
{{- define "mxe-commons.post-install-hook-istio.image-repository" -}}
{{- if .Values.imageCredentials.registry.url -}}
{{- .Values.imageCredentials.registry.url -}}
{{- else -}}
{{- .Values.global.registry.url -}}
{{- end -}}
{{- printf "/%s/" .Values.mxePostInstallHookIstio.image.repoPath -}}
{{- end -}}


{{/*
Image repository
*/}}
{{- define "mxe-commons.post-install-hook-pull-secrets.image-repository" -}}
{{- if .Values.imageCredentials.registry.url -}}
{{- .Values.imageCredentials.registry.url -}}
{{- else -}}
{{- .Values.global.registry.url -}}
{{- end -}}
{{- printf "/%s/" .Values.mxePostInstallPullSecret.image.repoPath -}}
{{- end -}}


{{/*
Istio resources - Request Authentication
*/}}
{{- define "mxe-commons.post-install-hook-istio-req-authn-name" -}}
{{- default "post-install-hook-istio-req-authn" .Values.mxePostInstallHookIstio.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Istio resources - Keycloak service name based url for issuer
*/}}
{{- define "mxe-commons.keycloak-service-path" -}}
{{- if .Values.global.serviceMesh.enabled -}}
http://eric-sec-access-mgmt-http:8443
{{- else -}}
http://eric-sec-access-mgmt-http:8080
{{- end -}}
{{- end -}}

{{/*
Istio resources - Keycloak host name based url for issuer
*/}}
{{- define "mxe-commons.api-host-path" -}}
{{- if eq (.Values.global.mxeApiport | int64) 443 -}}
{{- printf "https://%s" .Values.global.mxeApiHostname -}}
{{- else -}}
{{- printf "https://%s:%v" .Values.global.mxeApiHostname .Values.global.mxeApiport -}}
{{- end -}}
{{- end -}}

{{/*
Istio resources - Keycloak oauth host name based url for issuer
*/}}
{{- define "mxe-commons.oauth-api-host-path" -}}
{{- if eq (.Values.global.mxeApiport | int64) 443 -}}
{{- printf "https://%s" .Values.global.mxeOauthApiHostname -}}
{{- else -}}
{{- printf "https://%s:%v" .Values.global.mxeOauthApiHostname .Values.global.mxeApiport -}}
{{- end -}}
{{- end -}}

{{/*
Istio resources - Keycloak oauth host name
*/}}
{{- define "mxe-commons.oauth-api-host" -}}
{{- if .Values.global.mxeOauthApiHostname -}}
{{- printf "%s" .Values.global.mxeOauthApiHostname -}}
{{- end -}}
{{- end -}}

{{/*
Istio resources - Keycloak auth host name
*/}}
{{- define "mxe-commons.api-host" -}}
{{- if .Values.global.mxeApiHostname -}}
{{- printf "%s" .Values.global.mxeApiHostname -}}
{{- end -}}
{{- end -}}

{{/*
Istio resources - Request Authentication rules
*/}}
{{- define "mxe-commons.istio-req-authn-jwt-rules" -}}
jwtRules:
- issuer: "{{ include "mxe-commons.keycloak-service-path" .}}/auth/realms/mxe"
  jwksUri: "{{ include "mxe-commons.api-host-path" .}}/auth/realms/mxe/protocol/openid-connect/certs"
  forwardOriginalToken: true
- issuer: "{{ include "mxe-commons.keycloak-service-path" .}}/auth/realms/mxe"
  jwksUri: "{{ include "mxe-commons.api-host-path" .}}/auth/realms/master/protocol/openid-connect/certs"
  forwardOriginalToken: true
- issuer: "{{ include "mxe-commons.api-host-path" .}}/auth/realms/mxe"
  jwksUri: "{{ include "mxe-commons.api-host-path" .}}/auth/realms/mxe/protocol/openid-connect/certs"
  forwardOriginalToken: true
- issuer: "{{ include "mxe-commons.api-host-path" .}}/auth/realms/master"
  jwksUri: "{{ include "mxe-commons.api-host-path" .}}/auth/realms/master/protocol/openid-connect/certs"
  forwardOriginalToken: true
- issuer: "{{ include "mxe-commons.oauth-api-host-path" .}}/auth/realms/mxe"
  jwksUri: "{{ include "mxe-commons.oauth-api-host-path" .}}/auth/realms/mxe/protocol/openid-connect/certs"
  forwardOriginalToken: true
{{- end -}}

{{/*
Istio resources - Authorization Policy
*/}}
{{- define "mxe-commons.post-install-hook-istio-authz-policy-name" -}}
{{- default "post-install-hook-istio-authz-policy" .Values.mxePostInstallHookIstio.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Istio resources - Authorization Policy rules
*/}}
{{- define "mxe-commons.istio-authz-policy-rules" -}}
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
      - "/v2/*"
      - "/jupyter/*"
# 2. Allow "mxe-user" (role: mxe_default_role) to "READ_WRITE" access all path "/*"
- to:
  - operation:
      paths: ["/*"]
  when:
  - key: request.auth.claims[iss]
    values: ["{{ include "mxe-commons.oauth-api-host-path" .}}/auth/realms/mxe"]
  - key: request.auth.claims[roles]
    values: ["mxe_default_role"]
# 3. Allow existing Access control policy users to access models/model-services endpoints
#     Role: mxe_model_serving_role 
#     Access: READ_WRITE
#     Paths "/v1/models, /v1/models/*, /v1/model-services, /v1/model-services/*, "
- to:
  - operation:
      paths: ["/v1/models", "/v1/models/*", "/v1/model-services", "/v1/model-services/*","/v2/model-services", "/v2/model-services/*"]
  when:
  - key: request.auth.claims[iss]
    values: ["{{ include "mxe-commons.oauth-api-host-path" .}}/auth/realms/mxe"]
  - key: request.auth.claims[roles]
    values: ["mxe_model_serving_role"]
# 4. Allow Kubernetes service (dynamic seldon model) to pull docker image - 
# (TODO: Temp workaround, TO_BE_REMOVED)
#- to:
#  - operation:
#      paths: ["/*"]
#      methods: ["GET"]
#  when:
#  - key: request.auth.claims["iss"]
#    values: ["kubernetes/serviceaccount"]
#  - key: request.auth.claims["sub"]
#    values: ["system:serviceaccount:{{.Release.Namespace}}:default"]
{{- end -}}

{{/*
Istio resources - Authorization Policy to deny oauth admin
*/}}
{{- define "mxe-commons.post-install-hook-authz-deny-oauth-admin-name" -}}
{{- default "post-install-hook-authz-deny-oauth-admin" .Values.mxePostInstallHookIstioAuthzDenyOuthAdmin.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Istio resources - Authorization Policy rules to deny oauth admin
*/}}
{{- define "mxe-commons.istio-authz-policy-rules-deny-oauth-admin" -}}
action: DENY
rules:
- to:
  - operation:
      hosts: ["{{ include "mxe-commons.oauth-api-host" .}}"]
      paths: ["/auth", "/auth/", "/auth/admin", "/auth/admin/","/auth/admin/*"]
{{- end -}}

{{/*
Keycloak oauth ingress name
*/}}
{{- define "mxe-commons.keycloak-oauth-ingress.name" -}}
{{- default "eric-sec-access-mgmt-oauth" .Values.keycloak.ingress.oauth.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Keycloak oauth default ingress name
*/}}
{{- define "mxe-commons.keycloak-oauth-ingress-default.name" -}}
{{- default "eric-sec-access-mgmt-oauth-default" .Values.keycloak.ingress.oauth.default.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Keycloak auth ingress name
*/}}
{{- define "mxe-commons.keycloak-auth-ingress.name" -}}
{{- default "eric-sec-access-mgmt-ingress" .Values.keycloak.ingress.auth.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
LCM ingress name
*/}}
{{- define "mxe-commons.internal-container-registry.name" -}}
{{- default "eric-lcm-container-registry" (index .Values "internal-container-registry" "nameOverride") | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
LCM ingress annotations
*/}}
{{- define "mxe-commons.internal-container-registry.ingress.annotations" -}}
{{- if index .Values "internal-container-registry" "ingress" "annotations" -}}
{{ toYaml (index .Values "internal-container-registry" "ingress" "annotations") | }}
{{- end -}}
{{- end -}}

{{/*
LCM service name
*/}}
{{- define "mxe-commons.internal-container-registry.service.name" -}}
{{- $name := default "eric-lcm-container-registry" (index .Values "internal-container-registry" "ingress" "service" "name") -}}
{{- printf "%s-registry" $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Metrics service name
*/}}
{{- define "mxe-commons.internal-container-registry.metrics.service.name" -}}
{{- $name := default "eric-lcm-container-registry" (index .Values "internal-container-registry" "ingress" "metrics" "service" "name") -}}
{{- printf "%s-registry" $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the host of ingress controller.
*/}}
{{- define "mxe-commons.ingress-controller-host" -}}
{{- $name := default "eric-mxe-ingress-controller" .Values.ingressController.hostOverride -}}
{{- printf "%s" $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the port of ingress controller.
*/}}
{{- define "mxe-commons.ingress-controller-port" -}}
{{- .Values.ingressController.service.httpPort -}}
{{- end -}}

{{/*
Keycloak ingress configuration - re-route to discoveryurl
*/}}
{{- define "mxe-commons.post-install-hook-ingress-config-snippet" -}}
{{- printf "set $http_x_forwarded_proto https; set $http_x_forwarded_host %s;proxy_redirect https://%s/ https://$http_host/;" .Values.global.mxeOauthApiHostname .Values.global.mxeOauthApiHostname -}}
{{- end -}}

{{/*
Define the name of the credentials secret for internal container registry
*/}}
{{- define "mxe-commons.internal-container-registry-secret-name" -}}
{{- printf "%s" "mxe-internal-registry-creds" -}}
{{- end -}}

{{/*
Define the name of the dockerconfig secret for internal container registry
*/}}
{{- define "mxe-commons.internal-container-registry-dockerconfig-secret-name" -}}
{{- printf "%s" "mxe-internal-registry-dockerconfig-secret" -}}
{{- end -}}

{{/*
The host of the docker registry
*/}}
{{- define "mxe-commons.docker-registry-hostname" -}}
{{- if .Values.dockerRegistry.fullHostOverride -}}
{{- .Values.dockerRegistry.fullHostOverride -}}
{{- if .Values.dockerRegistry.portOverride -}}
:{{- .Values.dockerRegistry.portOverride -}}
{{- end -}}
{{- else -}}
{{- if .Values.dockerRegistry.nameOverride -}}
{{- .Values.dockerRegistry.nameOverride -}}
{{- else -}}
eric-lcm-container-registry-registry
{{- end -}}
.{{- .Release.Namespace -}}.svc.{{- .Values.global.mxeClusterDomain -}}
{{- if .Values.dockerRegistry.portOverride -}}
:{{- .Values.dockerRegistry.portOverride -}}
{{- else if .Values.dockerRegistry.ingressEnabled -}}
:5000
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
The ingress host of the docker registry
*/}}
{{- define "mxe-commons.docker-registry-ingress-hostname" -}}
{{- .Values.global.mxeApiHostname -}}
{{- end -}}

{{/*
The full host of the docker registry
*/}}
{{- define "mxe-commons.docker-registry-config" -}}
{{- $registrySvcName := (include "mxe-commons.docker-registry-hostname" .) -}}
{{- $registryHostName := (include "mxe-commons.docker-registry-ingress-hostname" .) -}}
{{- with .Values.global.internalRegistry -}}
{{- $authString := (printf "%s:%s" .username .password | b64enc) -}}
{{-  (printf "{\"auths\":{\"%s\":{\"username\":\"%s\",\"password\":\"%s\",\"auth\":\"%s\"},\"%s\":{\"username\":\"%s\",\"password\":\"%s\",\"auth\":\"%s\"}}}" $registrySvcName .username .password $authString $registryHostName .username .password $authString) | b64enc -}}
{{- end -}}
{{- end -}}



{{/*
Define name for Ratelimit EnvoyFilter
*/}}
{{- define "mxe-commons.ratelimit-envoyfilter-name" -}}
{{- printf "%s-%s" .Release.Name "ratelimit-filter" | trunc 63 | trimSuffix "-" -}}
{{- end -}}


{{/*
 Lcm Registry - Service Name
*/}}
{{- define "mxe-commons.egress-lcm-service-name" -}}
{{ .Values.global.serviceMesh.egress.lcmRegistry.serviceName }}
{{- end -}}

{{/*
 Lcm Registry - TLS Mode
*/}}
{{- define "mxe-commons.egress-lcm-tls-mode" -}}
{{ .Values.global.serviceMesh.egress.lcmRegistry.tlsMode }}
{{- end -}}

{{/*
 Lcm Registry - Egress client certificate
*/}}
{{- define "mxe-commons.egress-lcm-client-cert" -}}
{{- with .Values.global.serviceMesh.egress.lcmRegistry }}
{{- printf "%s%s" .certsPath .clientCertificate -}}
{{- end -}}
{{- end -}}

{{/*
  Egress CA certs
*/}}
{{- define "mxe-commons.egress-ca-cert" -}}
{{- with .Values.global.serviceMesh.egress.ca }}
{{- printf "%s%s" .caCertsPath .caCertificates -}}
{{- end -}}
{{- end -}}

{{/*
 Lcm Registry - Egress private key
*/}}
{{- define "mxe-commons.egress-lcm-private-key" -}}
{{- with .Values.global.serviceMesh.egress.lcmRegistry }}
{{- printf "%s%s" .certsPath .privateKey -}}
{{- end -}}
{{- end -}}


{{/*
 IAM - Service Name
*/}}
{{- define "mxe-commons.egress-iam-service-name" -}}
{{ .Values.global.serviceMesh.egress.iam.serviceName }}
{{- end -}}

{{/*
 IAM - Service Port
*/}}
{{- define "mxe-commons.egress-iam-service-port" -}}
{{- if .Values.global.serviceMesh.enabled -}}
8443
{{- else -}}
8080
{{- end -}}
{{- end -}}

{{/*
 IAM - ServiceEntry host name
*/}}
{{- define "mxe-commons.egress-iam-service-host" -}}
{{ include "mxe-commons.egress-iam-service-name" . }}.{{ .Release.Namespace }}.svc.cluster.local
{{- end -}}

{{/*
 IAM - TLS Mode
*/}}
{{- define "mxe-commons.egress-iam-tls-mode" -}}
{{ .Values.global.serviceMesh.egress.iam.tlsMode }}
{{- end -}}

{{/*
 IAM - Egress client certificate
*/}}
{{- define "mxe-commons.egress-iam-client-cert" -}}
{{ .Values.global.serviceMesh.egress.iam.clientCertificate }}
{{- end -}}

{{/*
 IAM - Egress private key
*/}}
{{- define "mxe-commons.egress-iam-private-key" -}}
{{ .Values.global.serviceMesh.egress.iam.privateKey }}
{{- end -}}

{{/*
 IAM - Egress client certificate
*/}}
{{- define "mxe-commons.egress-iam-client-cert-path" -}}
{{- with .Values.global.serviceMesh.egress.iam }}
{{- printf "%s%s" .certsPath .clientCertificate -}}
{{- end -}}
{{- end -}}

{{/*
 IAM - Egress private key
*/}}
{{- define "mxe-commons.egress-iam-private-key-path" -}}
{{- with .Values.global.serviceMesh.egress.iam }}
{{- printf "%s%s" .certsPath .privateKey -}}
{{- end -}}
{{- end -}}

{{/*
 IAM - TLS Mode
*/}}
{{- define "mxe-commons.egress-iam-gen-secret-name" -}}
{{ .Values.global.serviceMesh.egress.iam.genSecretName }}
{{- end -}}


{{/*
 IAM - TLS Mode
*/}}
{{- define "mxe-commons.egress-iam-cert-cn" -}}
{{ .Values.global.serviceMesh.egress.iam.certificateCN }}
{{- end -}}

{{/*
 IAM - TLS Mode
*/}}
{{- define "mxe-commons.egress-iam-ca-issuer" -}}
{{ .Values.global.serviceMesh.egress.iam.caIssuer }}
{{- end -}}

{{/*
 IAM - Service Port
*/}}
{{- define "mxe-commons.iam-port" -}}
{{- if .Values.global.serviceMesh.enabled -}}
{{- .Values.keycloak.service.httpsPort -}}
{{- else -}}
{{- .Values.keycloak.service.httpPort -}}
{{- end -}}
{{- end -}}

{{/*
 MXE Ingress Gateway - name
*/}}
{{- define "mxe-commons.mesh-ingress-gw-name" -}}
{{ include "mxe-commons.name" . }}-ingress-gw
{{- end -}}

{{/*
 MXE Ingress Gateway - name
*/}}
{{- define "mxe-commons.mesh-ingress-gw-svc-name" -}}
eric-mesh-ingressgateway
{{- end -}}

{{/*
 MXE Ingress Gateway - certs
*/}}
{{- define "mxe-commons.mesh-ingress-gw-certs" -}}
istio-ingressgateway-certs
{{- end -}}


{{/*
 MXE Ingress Gateway - port
*/}}
{{- define "mxe-commons.mesh-ingress-gw-port" -}}
{{- if .Values.global.serviceMesh.enabled -}}
443
{{- else -}}
80
{{- end -}}
{{- end -}}

{{/*
 MXE Ingress Gateway - protocol
*/}}
{{- define "mxe-commons.mesh-ingress-gw-protocol" -}}
{{- if .Values.global.serviceMesh.enabled -}}
HTTPS
{{- else -}}
HTTP
{{- end -}}
{{- end -}}

{{/*
Seldon Api gateway - destination rule traffic policy
*/}}
{{- define "mxe-commons.destination-rule-traffic-policy" -}}
{{- if .Values.global.serviceMesh.enabled -}}
trafficPolicy:
  portLevelSettings:
  - port:
      number: 80
    tls:
      mode: ISTIO_MUTUAL
  - port:
      number: 443
    tls:
      caCertificates: /etc/istio/egress-ca-certs/ca.crt
      mode: SIMPLE
  - port:
      number: 15021
    tls:
      mode: DISABLE
{{- else -}}
trafficPolicy:
  portLevelSettings:
  - port:
      number: 80
    tls:
      mode: DISABLE
  - port:
      number: 15021
    tls:
      mode: DISABLE
{{- end -}}
{{- end -}}

{{/*
 MXE Ingress Gateway - tls configuration
*/}}
{{- define "mxe-commons.ingress-gw-server-ports" -}}
{{- if .Values.global.serviceMesh.enabled -}}
- hosts:
  - "*"
  port:
    name: https
    number: 443
    protocol: HTTPS
  tls:
    mode: SIMPLE
    serverCertificate: /etc/istio/ingressgateway-certs/tls.crt
    privateKey: /etc/istio/ingressgateway-certs/tls.key
- hosts:
  - "*"
  port:
    name: http
    number: 80
    protocol: HTTPS
  tls:
    mode: ISTIO_MUTUAL
{{- else -}}
- hosts:
  - "*"
  port:
    name: http
    number: 80
    protocol: HTTP
{{- end -}}
{{- end -}}


{{/*
Security annotations
*/}}
{{- define "mxe-commons.security-annotations-post-install-hook" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" "post-install-hook-istio" }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{/*
Security Context  for post-install-hook
*/}}
{{- define "mxe-commons.post-install-hook-security-context" -}}
allowPrivilegeEscalation: false
capabilities:
  drop:
    - ALL
privileged: false
readOnlyRootFilesystem: true
runAsNonRoot: true
seccompProfile:
  type:
      {{- if .Values.seccompProfile.type -}}
        {{ $seccompProfile := ternary (printf "%s/%s" .Values.seccompProfile.type .Values.seccompProfile.localhostProfile) .Values.seccompProfile.type (eq .Values.seccompProfile.type "localhost") -}}
        {{ printf " "}}{{ $seccompProfile }}
      {{- end -}}
{{- end -}}

{{/*
Security Context  for post-install-hook-pull-secrets
*/}}
{{- define "mxe-commons.post-install-hook-pull-secrets-security-context" -}}
allowPrivilegeEscalation: false
capabilities:
  drop:
    - ALL
privileged: false
runAsNonRoot: true
readOnlyRootFilesystem: true
seccompProfile:
  type:
      {{- if .Values.seccompProfile.type -}}
        {{ $seccompProfile := ternary (printf "%s/%s" .Values.seccompProfile.type .Values.seccompProfile.localhostProfile) .Values.seccompProfile.type (eq .Values.seccompProfile.type "localhost") -}}
        {{ printf " "}}{{ $seccompProfile }}
      {{- end -}}
{{- end -}}

{{- define "mxe-commons.registryImagePullPolicy" -}}
{{- $top := index . 0 }}
{{- $serviceLevelImagePullPolicy := index . 1 }}
{{- $globalRegistryPullPolicy := "IfNotPresent" -}}
{{- if $top.Values.global -}}
    {{- if $top.Values.global.registry -}}
        {{- if $top.Values.global.registry.imagePullPolicy -}}
            {{- $globalRegistryPullPolicy = $top.Values.global.registry.imagePullPolicy -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{ if $serviceLevelImagePullPolicy }}
{{- $globalRegistryPullPolicy = $serviceLevelImagePullPolicy -}}
{{ end }}
{{- printf "%s" $globalRegistryPullPolicy -}}
{{- end -}}

{{/*
Standard labels of Helm and Kubernetes
*/}}
{{- define "mxe-commons.standard-labels" -}}
app.kubernetes.io/part-of: "mxe"
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
helm.sh/chart: {{ include "mxe-commons.chart" . | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
{{- end -}}

{{/*
 MXE PG BRO Config - name
*/}}
{{- define "mxe-commons.pg-br-configmap-name" -}}
{{ include "mxe-commons.name" . }}-pg-br-configmap
{{- end -}}

{{- define "mxe-commons.pg-co-name" -}}
{{ include "mxe-commons.name" . }}-pg-co
{{- end -}}

{{- define "mxe-commons.pg-backup-type-name" -}}
{{- default "mxe-pg" (index .Values "documentDatabasePG" "customObject" "backupRestore" "backupType") | trunc 63 -}}
{{- end -}}

{{- define "mxe-commons.pg-co-replicas" -}}
{{- default 2 (index .Values "documentDatabasePG" "customObject" "replicaCount") -}}
{{- end -}}

{{- define "mxe-commons.pg-housekeeping-threshold" -}}
{{- default 100 (index .Values "documentDatabasePG" "customObject" "resources" "persistentStorage" "housekeepingThresholdPercentage") -}}
{{- end -}}

{{- define "mxe-commons.pg-co-log-level" -}}
{{- default "info" (index .Values "documentDatabasePG" "customObject" "logLevel") -}}
{{- end -}}

{{- define "mxe-commons.pg-auth-mode" -}}
{{- if index .Values "documentDatabasePG" "customObject" "databaseServerConfig" "authentication" "mode" -}}
{{- index .Values "documentDatabasePG" "customObject" "databaseServerConfig" "authentication" "mode" -}}
{{- else -}}
  {{- if index .Values "global" "security" "tls" "enabled" -}}
  mTLSonly
  {{- else -}}
  password
  {{- end -}}
{{- end -}}
{{- end -}}