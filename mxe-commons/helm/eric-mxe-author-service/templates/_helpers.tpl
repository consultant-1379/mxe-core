{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-mxe-author-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-mxe-author-service.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Config name to be used for certificate-sidecar.
*/}}
{{- define "eric-mxe-author-service.certificate-sidecar-configmap-name" -}}
{{- $name := include "eric-mxe-author-service.name" . -}}
{{- printf "%s-%s" $name "certificate-sidecar-configmap" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create default backend name.
*/}}
{{- define "eric-mxe-author-service.defaultbackend" -}}
{{- if .Values.ingress.defaultBackendFullNameOverride -}}
{{- .Values.ingress.defaultBackendFullNameOverride -}}
{{- else -}}
{{- $name := default "eric-mxe-default-backend" .Values.ingress.defaultBackendNameOverride -}}
{{- printf "%s" $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
Create default ingress class name.
*/}}
{{- define "eric-mxe-author-service.ingress-class" -}}
{{- if .Values.ingress.ingressClass -}}
{{- .Values.ingress.ingressClass -}}
{{- else -}}
eric-mxe-ingress-controller-class
{{- end -}}
{{- end -}}

{{/*
Create default ingress class name.
*/}}
{{- define "eric-mxe-author-service.ingress-annotation-prefix" -}}
{{- if .Values.ingress.annotationPrefix -}}
{{- .Values.ingress.annotationPrefix -}}
{{- else -}}
mxe.nginx.ingress.kubernetes.io
{{- end -}}
{{- end -}}

{{/*
Expand the service account of the chart.
*/}}
{{- define "eric-mxe-author-service.serviceaccount" -}}
{{- $name := include "eric-mxe-author-service.name" . -}}
{{- printf "%s-%s" $name "serviceaccount" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the role binding.
*/}}
{{- define "eric-mxe-author-service.rolebinding" -}}
{{- $name := include "eric-mxe-author-service.name" . -}}
{{- printf "%s-%s" $name "rolebinding" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the role.
*/}}
{{- define "eric-mxe-author-service.role" -}}
{{- $name := include "eric-mxe-author-service.name" . -}}
{{- printf "%s-%s" $name "role" | trunc 63 | trimSuffix "-" -}}
{{- end -}}


{{/*
Image pull secrets DR-D1123-115
*/}}
{{- define "eric-mxe-author-service.image-pull-secrets" -}}
{{- if .Values.global.pullSecret -}}
imagePullSecrets:
  - name: {{ .Values.global.pullSecret | quote -}}
{{- else if .Values.global.registry.pullSecret -}}
imagePullSecrets:
  - name: {{ .Values.global.registry.pullSecret | quote -}}
{{- else if .Values.imageCredentials.pullSecret -}}
imagePullSecrets:
  - name: {{ .Values.imageCredentials.pullSecret | quote -}}
{{- else if .Values.imageCredentials.registry.url -}}
{{- if .Values.imageCredentials.registry.pullSecret -}}
imagePullSecrets:
  - name: {{ .Values.imageCredentials.registry.pullSecret | quote -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Image repository
*/}}
{{- define "eric-mxe-author-service.image-registry" -}}
{{- if .Values.imageCredentials.registry.url -}}
{{- .Values.imageCredentials.registry.url -}}
{{- else -}}
{{- .Values.global.registry.url -}}
{{- end -}}
{{- end -}}

{{/*
Image repository
*/}}
{{- define "eric-mxe-author-service.image-repository" -}}
{{- include "eric-mxe-author-service.image-registry" . -}}
{{- printf "/%s/" .Values.imageCredentials.repoPath -}}
{{- end -}}

{{/*
Create annotation for the product information (DR-D1121-064, DR-D1121-067)
*/}}
{{- define "eric-mxe-author-service.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}

{{/*
Update strategy for deployments
*/}}
{{- define "eric-mxe-author-service.update-strategy" -}}
strategy:
  type: {{ .Values.updateStrategy.type | quote }}
{{- if eq .Values.updateStrategy.type "RollingUpdate" }}
  rollingUpdate:
    maxUnavailable: {{ .Values.updateStrategy.rollingUpdate.maxUnavailable }}
    maxSurge: {{ .Values.updateStrategy.rollingUpdate.maxSurge -}}
{{- end -}}
{{- end -}}

{{/*
TLS for ingress
*/}}
{{- define "eric-mxe-author-service.ingress-tls" -}}
{{- if and .Values.global.mxeDisableDefaultIngressControllerUse (or .Values.ingress.secretName .Values.global.mxeApiTlsSecretName) -}}
tls:
  - secretName: {{ default .Values.global.mxeApiTlsSecretName .Values.ingress.secretName }}
    hosts: {{ if or .Values.global.mxeApiHostname .Values.ingress.hostname }}
      - {{ default .Values.global.mxeApiHostname .Values.ingress.hostname -}}
{{- end }}
{{ else if and (not .Values.global.mxeDisableDefaultIngressControllerUse) .Values.ingress.secretName -}}
tls:
  - secretName: {{ .Values.ingress.secretName }}
    hosts: {{ if .Values.ingress.hostname }}
      - {{ .Values.ingress.hostname -}}
{{- end }}
{{ end -}}
{{- end -}}

{{/*
The name of the custom db user's secret
*/}}
{{- define "eric-mxe-author-service.db-custom-user-secret-name" -}}
{{- if .Values.database.customUserSecretFullNameOverride -}}
{{- .Values.database.customUserSecretFullNameOverride -}}
{{- else -}}
{{- $name := include "eric-mxe-author-service.name" . -}}
{{- printf "%s-%s" $name "db-user" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
The name of the db
*/}}
{{- define "eric-mxe-author-service.db-name" -}}
{{- if .Values.database.nameOverride -}}
{{- $name := .Values.database.nameOverride | quote -}}
{{- regexReplaceAll "[^a-zA-Z0-9_]" $name "" | trunc 63 -}}
{{- else -}}
{{- $name := include "eric-mxe-author-service.name" . | quote -}}
{{- regexReplaceAll "[^a-zA-Z0-9_]" $name "" | trunc 63 -}}
{{- include "eric-mxe-author-service.name" . | regexReplaceAll "[^a-zA-Z0-9_]" "" | trunc 63 -}}
{{- end -}}
{{- end -}}

{{/*
The user of the db
*/}}
{{- define "eric-mxe-author-service.db-user-name" -}}
{{- $name := include "eric-mxe-author-service.name" . | quote -}}
{{- regexReplaceAll "[^a-zA-Z0-9_]" $name "" | trunc 63 -}}
{{- end -}}

{{/*
The host of the docker registry
*/}}
{{- define "eric-mxe-author-service.docker-registry-hostname" -}}
{{- if .Values.dockerRegistry.nameOverride -}}
{{- .Values.dockerRegistry.nameOverride -}}
{{- else -}}
eric-lcm-container-registry-registry
{{- end -}}
.{{- .Release.Namespace -}}.svc.cluster.local
{{- if .Values.dockerRegistry.ingressEnabled -}}
:5000
{{- end -}}
{{- end -}}

{{/*
The host of the docker
*/}}
{{- define "eric-mxe-author-service.docker-host" -}}
{{- if .Values.docker.hostOverride -}}
{{- .Values.docker.hostOverride -}}
{{- end -}}
{{- end -}}

{{/*
Expand the name of the configmap of waiting for keycloak.
*/}}
{{- define "eric-mxe-author-service.wait-for-keycloak-configmap" -}}
{{- $name := include "eric-mxe-author-service.name" . -}}
{{- printf "%s-%s" $name "wait-for-keycloak-configmap" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Istio sidecar annotations/labels
*/}}
{{- define "eric-mxe-author-service.istio-sidecar-annotations" -}}
sidecar.istio.io/rewriteAppHTTPProbers: {{ .Values.global.serviceMesh.enabled | quote }}
{{ include "eric-mxe-author-service.istio-sidecar-annotations-egress" . }}
{{- end -}}

{{- define "eric-mxe-author-service.istio-sidecar-labels" -}}
sidecar.istio.io/inject: {{ .Values.global.serviceMesh.enabled | quote }}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts
*/}}
{{- define "eric-mxe-author-service.istio-sidecar-annotations-egress" -}}
{{- if .Values.global.serviceMesh.enabled -}}
{{ include "eric-mxe-author-service.istio-sidecar-annotations-egress-mounts" . }}
proxy.istio.io/config: '{ "holdApplicationUntilProxyStarts": true }'
traffic.sidecar.istio.io/excludeOutboundPorts: "5432"
{{- end -}}
{{- end -}}

{{/*
Database init - env
*/}}
{{- define "eric-mxe-author-service.db-init-env" -}}
{{- if .Values.global.serviceMesh.enabled -}}
- name: PGSSLROOTCERT
  value: {{ " /home/dbinit/.postgresql/ca.crt" }}
- name: PGSSLCERT
  value: {{ " /home/dbinit/.postgresql/" }}{{ include "eric-mxe-author-service.pg-client-cert" . }}
- name: PGSSLKEY
  value: {{ " /home/dbinit/.postgresql/" }}{{ include "eric-mxe-author-service.pg-private-key" . }}
- name: PGSSLMODE
  value: {{ "require" }}
{{- end -}}
{{- end -}}

{{/*
Database init - args command
*/}}
{{- define "eric-mxe-author-service.db-init-args-command" -}}
{{- if .Values.global.serviceMesh.enabled -}}
command: ["sh", "-c", "mkdir -p /home/dbinit/.postgresql && cp /run/secrets/certificates/client/postgres/* /home/dbinit/.postgresql && cp /run/secrets/certificates/trusted/* /home/dbinit/.postgresql && chmod 400 /home/dbinit/.postgresql/* && /home/dbinit/db-init.sh"]
{{- end -}}
{{- end -}}

{{/*
Database init - volume mounts
*/}}
{{- define "eric-mxe-author-service.db-init-volume-mounts" -}}
volumeMounts:
- mountPath: /home/dbinit/.postgresql
  name: dbinit-postgres-sql-home
{{- if .Values.global.serviceMesh.enabled }}
- name: postgres-cert
  mountPath: /run/secrets/certificates/client/postgres
  readOnly: true
- name: trusted-ca
  mountPath: /run/secrets/certificates/trusted
  readOnly: true
{{- end -}}
{{- end -}}

{{/*
Oauth Mxe Api host path url
*/}}
{{- define "eric-mxe-author-service.oauth-api-host-path-url" -}}
{{- if eq (.Values.global.mxeApiport | int64) 443 -}}
{{- printf "https://%s" .Values.global.mxeOauthApiHostname -}}
{{- else -}}
{{- printf "https://%s:%v" .Values.global.mxeOauthApiHostname .Values.global.mxeApiport -}}
{{- end -}}
{{- end -}}

{{/*
author-service - secret containing root ca for oauth hostname 
*/}}
{{- define "eric-mxe-author-service.iam-ca-cert-name" -}}
{{- .Values.global.mxeIamCaSecretName -}}
{{- end -}}


{{/*
Prometheus annotations
*/}}
{{- define "eric-mxe-author-service.prometheus-annotations" -}}
prometheus.io/path: {{ .Values.global.mxePrometheusPath | quote }}
prometheus.io/port: {{ .Values.global.mxePrometheusPort | quote }}
prometheus.io/scrape: {{ .Values.global.mxePrometheusScrape | quote }}
prometheus.io/scrape-role: {{ .Values.global.mxePrometheusScrapeRole | quote }}
prometheus.io/scrape-interval: {{ .Values.global.mxePrometheusScrapeInterval | quote }}
{{- end -}}

{{/*
Define the name of the configmap for storing logcontrol.json file
*/}}
{{- define "eric-mxe-author-service.log-control-configmap" -}}
{{- .Values.global.mxeLogControlConfigMap -}}
{{- end -}}

{{/*
author-service - mtls enabled - init certs
*/}}
{{- define "eric-mxe-author-service.init-certs-mtls" -}}
{{- if .Values.global.serviceMesh.enabled -}}
mkdir /tmp/siptls
openssl x509 -outform der -in /run/secrets/certificates/client/postgres/clicert.pem -out "/tmp/siptls/postgresql.crt"
openssl pkcs8 -topk8 -inform PEM -in /run/secrets/certificates/client/postgres/cliprivkey.pem -outform DER -out "/tmp/siptls/postgresql.pk8" -v1 PBE-MD5-DES -nocrypt
{{- end -}}
{{- end -}}

{{/*
author-service - mtls enabled - init certs - volume mounts
*/}}
{{- define "eric-mxe-author-service.init-certs-mtls-volume-mounts" -}}
{{- if .Values.global.serviceMesh.enabled -}}
- name: mxeauth-cert
  mountPath: /run/secrets/certificates/client/postgres
{{- end -}}
{{- end -}}

{{/*
author-service - jdbc params
*/}}
{{- define "eric-mxe-author-service.jdbc-params" -}}
{{- if .Values.global.serviceMesh.enabled -}}
{{- printf "?ssl=true&sslmode=verify-full&sslcert=/tmp/siptls/postgresql.crt&sslkey=/tmp/siptls/postgresql.pk8&sslrootcert=/run/secrets/certificates/trusted/ca.crt" -}}
{{ else }}
{{ "" }}
{{- end -}}
{{- end -}}

{{/*
author-service - service mesh volume mounts
*/}}
{{- define "eric-mxe-author-service.service-mesh-volume-mounts" -}}
{{- if .Values.global.serviceMesh.enabled -}}
- name: mxeauth-cert
  mountPath: /run/secrets/certificates/client/postgres
  readOnly: true
- name: trusted-ca
  mountPath: /run/secrets/certificates/trusted
  readOnly: true
{{- end -}}
{{- end -}}

{{- define "eric-mxe-author-service.certificate-sidecar-run-command" -}}
{{- printf "/usr/bin/openssl x509 -outform der -in ${SOURCE}/clicert.pem -out ${DESTINATION}/postgresql.crt;/usr/bin/openssl pkcs8 -topk8 -inform PEM -in ${SOURCE}/cliprivkey.pem -outform DER -out ${DESTINATION}/postgresql.pk8 -v1 PBE-MD5-DES -nocrypt;/bin/chmod 600 ${DESTINATION}/*" -}}
{{- end -}}

{{- define "eric-mxe-author-service.certificate-sidecar-source" -}}
{{- printf "/run/secrets/certificates/client/postgres/" -}}
{{- end -}}

{{- define "eric-mxe-author-service.certificate-sidecar-destination" -}}
{{- printf "/tmp/siptls" -}}
{{- end -}}

{{/*
author-service - service mesh volumes
*/}}
{{- define "eric-mxe-author-service.service-mesh-volumes" -}}
- name: dbinit-postgres-sql-home
  emptyDir: {}
{{- if .Values.global.serviceMesh.enabled }}
- name: postgres-cert
  secret:
    secretName: eric-data-document-database-pg-postgres-cert
- name: trusted-ca
  secret:
    secretName: {{ (((((.Values).global).security).tls).trustedInternalRootCa).secret | default "eric-sec-sip-tls-trusted-root-cert" | quote }}
- name: mxeauth-cert
  secret:
    secretName: {{ include "eric-mxe-author-service.name" . }}-pg-cert
- name: kubernetes-run-script
  configMap:
    name: {{ include "eric-mxe-author-service.name" . }}-certificate-sidecar-configmap
{{- end -}}
{{- end -}}


{{/*
author-service - postgres certificate name
*/}}
{{- define "eric-mxe-author-service.pg-client-cert" -}}
{{ .Values.global.serviceMesh.egress.postgres.clientCertificate }}
{{- end -}}

{{/*
author-service - postgres certificate private key
*/}}
{{- define "eric-mxe-author-service.pg-private-key" -}}
{{ .Values.global.serviceMesh.egress.postgres.privateKey }}
{{- end -}}

{{/*
author-service - postgres certificate ca
*/}}
{{- define "eric-mxe-author-service.pg-ca-issuer" -}}
{{ .Values.global.serviceMesh.egress.postgres.caIssuer }}
{{- end -}}

{{/*
Istio sidecar annotations - egress volume mounts
*/}}
{{- define "eric-mxe-author-service.istio-sidecar-annotations-egress-mounts" -}}
{{- if .Values.global.serviceMesh.enabled -}}
sidecar.istio.io/userVolume: {{ include "eric-mxe-author-service.istio-sidecar-annotations-user-volume" . | squote }}
sidecar.istio.io/userVolumeMount: {{ include "eric-mxe-author-service.istio-sidecar-annotations-volume-mounts" . | squote }}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - user volume
*/}}
{{- define "eric-mxe-author-service.istio-sidecar-annotations-user-volume" -}}
{{- printf "{" -}}
{{- include "eric-mxe-author-service.istio-sidecar-annotations-user-volume-egress" . -}},
{{- include "eric-mxe-author-service.istio-sidecar-annotations-user-volume-egress-iam" . -}},
{{- include "eric-mxe-author-service.istio-sidecar-annotations-user-volume-egress-lcm" . -}},
{{- include "eric-mxe-author-service.istio-sidecar-annotations-user-volume-egress-pm" . }}
{{- printf "}" -}}
{{- end }}


{{/*
Istio sidecar annotations - volume mounts - egress
*/}}
{{- define "eric-mxe-author-service.istio-sidecar-annotations-user-volume-egress" -}}
{{- with .Values.global.serviceMesh.egress.ca }}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\"}}" .secretName .genSecretName -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress iam
*/}}
{{- define "eric-mxe-author-service.istio-sidecar-annotations-user-volume-egress-iam" -}}
{{- with .Values.global.serviceMesh.egress.iam }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress pm
*/}}
{{- define "eric-mxe-author-service.istio-sidecar-annotations-user-volume-egress-pm" -}}
{{- with .Values.global.serviceMesh.egress.pmServer }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress lcm
*/}}
{{- define "eric-mxe-author-service.istio-sidecar-annotations-user-volume-egress-lcm" -}}
{{- with .Values.global.serviceMesh.egress.lcmRegistry }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - user volume mounts
*/}}
{{- define "eric-mxe-author-service.istio-sidecar-annotations-volume-mounts" }}
{{- printf "{" -}}
{{- include "eric-mxe-author-service.istio-sidecar-annotations-volume-mounts-egress" . -}},
{{- include "eric-mxe-author-service.istio-sidecar-annotations-volume-mounts-egress-iam" . -}},
{{- include "eric-mxe-author-service.istio-sidecar-annotations-volume-mounts-egress-lcm" . -}},
{{- include "eric-mxe-author-service.istio-sidecar-annotations-volume-mounts-egress-pm" . -}}
{{- printf "}" -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress
*/}}
{{- define "eric-mxe-author-service.istio-sidecar-annotations-volume-mounts-egress" -}}
{{- with .Values.global.serviceMesh.egress.ca }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .caCertsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress iam
*/}}
{{- define "eric-mxe-author-service.istio-sidecar-annotations-volume-mounts-egress-iam" -}}
{{- with .Values.global.serviceMesh.egress.iam }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress lcm
*/}}
{{- define "eric-mxe-author-service.istio-sidecar-annotations-volume-mounts-egress-lcm" -}}
{{- with .Values.global.serviceMesh.egress.lcmRegistry }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress pm server
*/}}
{{- define "eric-mxe-author-service.istio-sidecar-annotations-volume-mounts-egress-pm" -}}
{{- with .Values.global.serviceMesh.egress.pmServer }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
adding security context
*/}}
{{- define "eric-mxe-author-service.initdb-security-context" -}}
securityContext:
  readOnlyRootFilesystem: true
  capabilities:
    drop:
      - all
  privileged: false
  runAsNonRoot: true
  allowPrivilegeEscalation: false
  {{ if .Values.seccompProfile.type -}}
  seccompProfile:
    type: {{ .Values.seccompProfile.type }}
    {{ if eq .Values.seccompProfile.type "Localhost" -}}
    localhostProfile: {{ .Values.seccompProfile.localhostProfile }}
    {{- end -}}
  {{- end -}}
{{- end -}}

{{- define "eric-mxe-author-service.kubectl-security-context" -}}
securityContext:
  capabilities:
    drop:
      - all
  privileged: false
  runAsNonRoot: true
  allowPrivilegeEscalation: false
  readOnlyRootFilesystem: true
  {{ if .Values.seccompProfile.type -}}
  seccompProfile:
    type: {{ .Values.seccompProfile.type }}
    {{ if eq .Values.seccompProfile.type "Localhost" -}}
    localhostProfile: {{ .Values.seccompProfile.localhostProfile }}
    {{- end -}}
  {{- end -}}
{{- end -}}

{{/*
adding security context
*/}}
{{- define "eric-mxe-author-service.usercontainer-security-context" -}}
securityContext:
  capabilities:
    drop:
      - all
  privileged: false
  runAsUser: 10101
  runAsNonRoot: true
  readOnlyRootFilesystem: true
  allowPrivilegeEscalation: false
  {{ if .Values.seccompProfile.type -}}
  seccompProfile:
    type: {{ .Values.seccompProfile.type }}
    {{ if eq .Values.seccompProfile.type "Localhost" -}}
    localhostProfile: {{ .Values.seccompProfile.localhostProfile }}
    {{- end -}}
  {{- end -}}
{{- end -}}

{{- define "eric-mxe-author-service.podPriority" -}}
{{- if .Values.podPriority -}}
  {{- if .Values.podPriority.mxeAuthorService -}}
    {{- .Values.podPriority.mxeAuthorService.priorityClassName | toString -}}
  {{- end -}}
{{- end -}}
{{- end -}}


{{/*
Standard labels of Helm and Kubernetes
*/}}
{{- define "eric-mxe-author-service.standard-labels" -}}
app.kubernetes.io/part-of: "mxe"
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
helm.sh/chart: {{ include "eric-mxe-author-service.chart" . | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
{{- end -}}

{{/*
Create a user defined label - [DR-D1121-068] [DR-D1121-060]
*/}}
{{ define "eric-mxe-author-service.config-labels" }}
  {{- $global := (.Values.global).labels -}}
  {{- $service := .Values.labels -}}
  {{- include "eric-mxe-author-service.mergeLabels" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged labels for Default, which includes Standard and Config - Used in all files
*/}}
{{- define "eric-mxe-author-service.labels" -}}
  {{- $standard := include "eric-mxe-author-service.standard-labels" . | fromYaml -}}
  {{- $config := include "eric-mxe-author-service.config-labels" . | fromYaml -}}
  {{- include "eric-mxe-author-service.mergeLabels" (dict "location" .Template.Name "sources" (list $standard $config)) | trim }}
{{- end -}}

{{/*
Create a user defined annotation - [DR-D1121-060]
*/}}
{{ define "eric-mxe-author-service.config-annotations" }}
  {{- $global := (.Values.global).annotations -}}
  {{- $service := .Values.annotations -}}
  {{- include "eric-mxe-author-service.mergeAnnotations" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged annotations for Default, which includes productInfo and config - Used in all files
*/}}
{{- define "eric-mxe-author-service.annotations" -}}
  {{- $productInfo := include "eric-mxe-author-service.product-info" . | fromYaml -}}
  {{- $config := include "eric-mxe-author-service.config-annotations" . | fromYaml -}}
  {{- include "eric-mxe-author-service.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $config)) | trim }}
{{- end -}}

{{/*
Security annotations
*/}}
{{- define "eric-mxe-author-service.security-annotations" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name }}: {{ $appArmorProfile }}
{{ printf "%s/%s-%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name "init-db" }}: {{ $appArmorProfile }}
{{ printf "%s/%s-%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name "kubectl" }}: {{ $appArmorProfile }}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" "init-cacerts" }}: {{ $appArmorProfile }}
{{- if .Values.global.serviceMesh.enabled }}
{{ printf "%s/%s-%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name "certificate-sidecar" }}: {{ $appArmorProfile }}
{{- end }}
{{- end }}
{{- end -}}

{{/*
Ingress annotations
*/}}
{{- define "eric-mxe-author-service.ingress-annotation" -}}
{{ include "eric-mxe-author-service.ingress-annotation-prefix" . }}/default-backend: {{ include "eric-mxe-author-service.defaultbackend" . | quote }}
{{ include "eric-mxe-author-service.ingress-annotation-prefix" . }}/proxy-body-size: "20G"
{{ include "eric-mxe-author-service.ingress-annotation-prefix" . }}/proxy-request-buffering: "off"
{{ include "eric-mxe-author-service.ingress-annotation-prefix" . }}/service-upstream: "true"
{{ include "eric-mxe-author-service.ingress-annotation-prefix" . }}/upstream-vhost: {{ include "eric-mxe-author-service.name" . | quote }}
{{- end -}}

{{/*
Create a map from ".Values.global" with defaults if missing in values file.
This hides defaults from values file.
*/}}
{{ define "eric-mxe-author-service.global" }}
  {{- $globalDefaults := dict "nodeSelector" (dict) -}}
  {{ if .Values.global }}
    {{- mergeOverwrite $globalDefaults .Values.global | toJson -}}
  {{ else }}
    {{- $globalDefaults | toJson -}}
  {{ end }}
{{ end }}

{{/*
Create a merged set of nodeSelectors from global and service level.
*/}}
{{ define "eric-mxe-author-service.nodeSelector" }}
{{- $g := fromJson (include "eric-mxe-author-service.global" .) -}}
{{- $global := $g.nodeSelector -}}
{{- $service := .Values.nodeSelector -}}
{{- include "eric-mxe-author-service.aggregatedMerge" (dict "context" "nodeSelector" "location" .Template.Name "sources" (list $global $service)) -}}
{{ end }}

{{/*
Added below to handle DR-D1121-102 for globalRegistryPullPolicy Shall be Used
*/}}
{{- define "eric-mxe-author-service.registryImagePullPolicy" -}}
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
adding TopologySpreadConstraints
*/}}
{{- define "eric-mxe-author-service.topologySpreadConstraints" }}
{{- if .Values.topologySpreadConstraints }}
{{- range $config, $values := .Values.topologySpreadConstraints }}
- topologyKey: {{ $values.topologyKey }}
  maxSkew: {{ $values.maxSkew | default 1 }}
  whenUnsatisfiable: {{ $values.whenUnsatisfiable | default "ScheduleAnyway" }}
{{- end }}
{{- end }}
{{- end }}

{{- define "eric-mxe-author-service.ericProdInfoRepoPath" }}
  {{- $top := index . 0 }}
  {{- $imageName := index . 1 }}
  {{- $repoPath := "" -}}

  {{ range $k, $v := $top.images }}
  {{- if eq $k $imageName -}}
  {{- range $kk, $vv := . -}}
    {{ if eq $kk "repoPath" }}
      {{- $repoPath = $vv -}}
    {{ end -}}
  {{ end -}}
  {{- end }}
  {{- end }}
  {{- printf "%s" $repoPath -}}
{{- end -}}

{{- define "eric-mxe-author-service.ericProdInfoRegistry" }}
  {{- $top := index . 0 }}
  {{- $imageName := index . 1 }}
  {{- $registryUrl := "" -}}

  {{ range $k, $v := $top.images }}
  {{- if eq $k $imageName -}}
  {{- range $kk, $vv := . -}}
    {{ if eq $kk "registry" }}
      {{- $registryUrl = $vv -}}
    {{ end -}}
  {{ end -}}
  {{- end }}
  {{- end }}
  {{- printf "%s" $registryUrl -}}
{{- end -}}

{{/*
eric-mxe-author-service image name path
*/}}
{{- define "eric-mxe-author-service.setImageRepo" }}
  {{- $top := index . 0 }}
  {{- $serviceLevelImageRegistry := index . 1 }}
  {{- $serviceLevelImageRepoPath := index . 2 }}
  {{- $imageName := index . 3 }}

  {{- /*
  Registry and Repopath for the specified image container is fetched from eric-product-info.yaml
  */}}
  {{- $productInfo := fromYaml ($top.Files.Get "eric-product-info.yaml") -}}
  {{- $registryUrl := include "eric-mxe-author-service.ericProdInfoRegistry" (list $productInfo $imageName) -}}
  {{- $imageRepoPath := include "eric-mxe-author-service.ericProdInfoRepoPath" (list $productInfo $imageName) -}}

  {{- /*
  Availablilty of the Registry and Repopath from global level of the chart is checked.
  If available, this will overwrite the product-info registry and repoPath data.
  */}}
  {{- if $top.Values.global -}}
    {{- if $top.Values.global.registry -}}
      {{- if $top.Values.global.registry.url -}}
        {{- $registryUrl = $top.Values.global.registry.url -}}
      {{- end -}}
      {{- if $top.Values.global.registry.repoPath -}}
        {{- $imageRepoPath = $top.Values.global.registry.repoPath -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}

  {{- /*
  Availablilty of the Registry and Repopath from service level of the chart is checked.
  If available, this will overwrite the previously set registry and repoPath data (product-info/global-level data).
  */}}
  {{- if $top.Values.imageCredentials -}}
    {{- if $top.Values.imageCredentials.registry.url -}}
      {{- $registryUrl = $top.Values.imageCredentials.registry.url -}}
    {{- end -}}
    {{- if not (kindIs "invalid" $top.Values.imageCredentials.repoPath) -}}
      {{- $imageRepoPath = $top.Values.imageCredentials.repoPath -}}
    {{- end -}}
  {{- end -}}

  {{- /*
  Container specific Registry and Repopath availablity is checked.
  If available, this will overwrite the previously set registry and repoPath data (product-info/global-level/service-level data).
  */}}
  {{- if $serviceLevelImageRegistry }}
  {{- $registryUrl = $serviceLevelImageRegistry -}}
  {{- end }}
  {{- if $serviceLevelImageRepoPath }}
  {{- $imageRepoPath = $serviceLevelImageRepoPath -}}
  {{- end }}  

  {{- printf "%s/%s/" $registryUrl  $imageRepoPath -}}
{{- end -}}
{{/*
Merge global tolerations with service tolerations (DR-D1120-061-AD).
*/}}
{{- define "eric-mxe-author-service.merge-tolerations" -}}
  {{- if (.root.Values.global).tolerations }}
      {{- $globalTolerations := .root.Values.global.tolerations -}}
      {{- $serviceTolerations := list -}}
      {{- if .root.Values.tolerations -}}
        {{- if eq (typeOf .root.Values.tolerations) ("[]interface {}") -}}
          {{- $serviceTolerations = .root.Values.tolerations -}}
        {{- else if eq (typeOf .root.Values.tolerations) ("map[string]interface {}") -}}
          {{- $serviceTolerations = index .root.Values.tolerations .podbasename -}}
        {{- end -}}
      {{- end -}}
      {{- $result := list -}}
      {{- $nonMatchingItems := list -}}
      {{- $matchingItems := list -}}
      {{- range $globalItem := $globalTolerations -}}
        {{- $globalItemId := include "eric-mxe-author-service.merge-tolerations.get-identifier" $globalItem -}}
        {{- range $serviceItem := $serviceTolerations -}}
          {{- $serviceItemId := include "eric-mxe-author-service.merge-tolerations.get-identifier" $serviceItem -}}
          {{- if eq $serviceItemId $globalItemId -}}
            {{- $matchingItems = append $matchingItems $serviceItem -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
      {{- range $globalItem := $globalTolerations -}}
        {{- $globalItemId := include "eric-mxe-author-service.merge-tolerations.get-identifier" $globalItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-author-service.merge-tolerations.get-identifier" $matchItem -}}
          {{- if eq $matchItemId $globalItemId -}}
            {{- $matchCount = add1 $matchCount -}}
          {{- end -}}
        {{- end -}}
        {{- if eq $matchCount 0 -}}
          {{- $nonMatchingItems = append $nonMatchingItems $globalItem -}}
        {{- end -}}
      {{- end -}}
      {{- range $serviceItem := $serviceTolerations -}}
        {{- $serviceItemId := include "eric-mxe-author-service.merge-tolerations.get-identifier" $serviceItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-author-service.merge-tolerations.get-identifier" $matchItem -}}
          {{- if eq $matchItemId $serviceItemId -}}
            {{- $matchCount = add1 $matchCount -}}
          {{- end -}}
        {{- end -}}
        {{- if eq $matchCount 0 -}}
          {{- $nonMatchingItems = append $nonMatchingItems $serviceItem -}}
        {{- end -}}
      {{- end -}}
      {{- toYaml (concat $result $matchingItems $nonMatchingItems) -}}
  {{- else -}}
      {{- if .root.Values.tolerations -}}
        {{- if eq (typeOf .root.Values.tolerations) ("[]interface {}") -}}
          {{- toYaml .root.Values.tolerations -}}
        {{- else if eq (typeOf .root.Values.tolerations) ("map[string]interface {}") -}}
          {{- toYaml (index .root.Values.tolerations .podbasename) -}}
        {{- end -}}
      {{- end -}}
  {{- end -}}
{{- end -}}
{{/*
Helper function to get the identifier of a tolerations array element.
Assumes all keys except tolerationSeconds are used to uniquely identify
a tolerations array element.
*/}}
{{ define "eric-mxe-author-service.merge-tolerations.get-identifier" }}
  {{- $keyValues := list -}}
  {{- range $key := (keys . | sortAlpha) -}}
    {{- if eq $key "effect" -}}
      {{- $keyValues = append $keyValues (printf "%s=%s" $key (index $ $key)) -}}
    {{- else if eq $key "key" -}}
      {{- $keyValues = append $keyValues (printf "%s=%s" $key (index $ $key)) -}}
    {{- else if eq $key "operator" -}}
      {{- $keyValues = append $keyValues (printf "%s=%s" $key (index $ $key)) -}}
    {{- else if eq $key "value" -}}
      {{- $keyValues = append $keyValues (printf "%s=%s" $key (index $ $key)) -}}
    {{- end -}}
  {{- end -}}
  {{- printf "%s" (join "," $keyValues) -}}
{{ end }}