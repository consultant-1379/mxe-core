{{/*
Expand the name of the chart.
*/}}
{{- define "eric-mxe-prometheus.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-mxe-prometheus.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Kube State Metrics' deployment.
*/}}
{{- define "eric-mxe-prometheus.kube-state-metrics-deployment-name" -}}
{{- $name := include "eric-mxe-prometheus.name" . -}}
{{- printf "%s-%s" $name "kube-state-metrics" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Kube State Metrics' service.
*/}}
{{- define "eric-mxe-prometheus.kube-state-metrics-service-name" -}}
{{- $name := include "eric-mxe-prometheus.name" . -}}
{{- printf "%s-%s" $name "kube-state-metrics-service" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Kube State Metrics' service account.
*/}}
{{- define "eric-mxe-prometheus.kube-state-metrics-service-account-name" -}}
{{- $name := include "eric-mxe-prometheus.name" . -}}
{{- printf "%s-%s" $name "kube-state-metrics-service-account" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Kube State Metrics' role.
*/}}
{{- define "eric-mxe-prometheus.kube-state-metrics-role-name" -}}
{{- $name := include "eric-mxe-prometheus.name" . -}}
{{- printf "%s-%s" $name "kube-state-metrics-role" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "eric-mxe-prometheus.service-account-name" -}}
{{- $name := include "eric-mxe-prometheus.name" . -}}
{{- printf "%s-%s" $name "service-account" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Kube State Metrics' role binding.
*/}}
{{- define "eric-mxe-prometheus.kube-state-metrics-role-binding-name" -}}
{{- $name := include "eric-mxe-prometheus.name" . -}}
{{- printf "%s-%s" $name "kube-state-metrics-role-binding" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Image pull secrets
*/}}
{{- define "eric-mxe-prometheus.image-pull-secrets" -}}
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
{{- define "eric-mxe-prometheus.image-repository" -}}
{{- if .Values.imageCredentials.registry.url -}}
{{- .Values.imageCredentials.registry.url -}}
{{- else -}}
{{- .Values.global.registry.url -}}
{{- end -}}
{{- printf "/%s/" .Values.imageCredentials.repoPath -}}
{{- end -}}

{{/*
Product information of Ericsson products (DR-D1121-064, DR-D1121-067)
*/}}
{{- define "eric-mxe-prometheus.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}

{{/*
Create seldon Core Analytics cluster role name.
*/}}
{{- define "eric-mxe-prometheus.role-name" -}}
{{- if .Values.rbac.roleFullNameOverride -}}
{{- printf "%s-role" .Values.rbac.roleFullNameOverride -}}
{{- else -}}
{{- $name := include "eric-mxe-prometheus.name" . -}}
{{- printf "%s-%s" $name "role" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
ConfigMap binding name.
*/}}
{{- define "eric-mxe-prometheus.config-map-name" -}}
{{- if .Values.rbac.roleFullNameOverride -}}
{{- printf "%s-config-map" .Values.rbac.roleFullNameOverride -}}
{{- else -}}
{{- $name := include "eric-mxe-prometheus.name" . -}}
{{- printf "%s-%s" $name "config-map" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}



{{/*
Create seldon Core Analytics role binding name.
*/}}
{{- define "eric-mxe-prometheus.role-binding-name" -}}
{{- if .Values.rbac.roleFullNameOverride -}}
{{- printf "%s-binding" .Values.rbac.roleFullNameOverride -}}
{{- else -}}
{{- $name := include "eric-mxe-prometheus.name" . -}}
{{- printf "%s-%s" $name "role-binding" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
Expand the name of the Prometheus' ingress.
*/}}
{{- define "eric-mxe-prometheus.ingress-name" -}}
{{- $name := include "eric-mxe-prometheus.name" . -}}
{{- printf "%s-%s" $name "ingress" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create default backend name.
*/}}
{{- define "eric-mxe-prometheus.defaultbackend" -}}
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
{{- define "eric-mxe-prometheus.ingress-class" -}}
{{- if .Values.ingress.ingressClass -}}
{{- .Values.ingress.ingressClass -}}
{{- else -}}
eric-mxe-ingress-controller-class
{{- end -}}
{{- end -}}

{{/*
Create default ingress annotation prefix.
*/}}
{{- define "eric-mxe-prometheus.ingress-annotation-prefix" -}}
{{- if .Values.ingress.annotationPrefix -}}
{{- .Values.ingress.annotationPrefix -}}
{{- else -}}
mxe.nginx.ingress.kubernetes.io
{{- end -}}
{{- end -}}

{{/*
TLS for ingress
*/}}
{{- define "eric-mxe-prometheus.ingress-tls" -}}
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
Update strategy for deployments
*/}}
{{- define "eric-mxe-prometheus.update-strategy" -}}
strategy:
  type: {{ .Values.updateStrategy.type | quote }}
{{- if eq .Values.updateStrategy.type "RollingUpdate" }}
  rollingUpdate:
    maxUnavailable: {{ .Values.updateStrategy.rollingUpdate.maxUnavailable }}
    maxSurge: {{ .Values.updateStrategy.rollingUpdate.maxSurge -}}
{{- end -}}
{{- end -}}

{{/*
The ingress port name
*/}}
{{- define "eric-mxe-prometheus.ingress-port-name" -}}
{{- if .Values.global.serviceMesh.enabled -}}
https-rproxy-pm
{{- else -}}
http-pm
{{- end -}}
{{- end -}}

{{/*
 PM Server - Egress client certificate
*/}}
{{- define "eric-mxe-prometheus.egress-client-cert" -}}
{{ .Values.global.serviceMesh.egress.pmServer.certsPath}}{{ .Values.global.serviceMesh.egress.pmServer.clientCertificate}}
{{- end }}

{{/*
 PM Server - Egress CA
*/}}
{{- define "eric-mxe-prometheus.egress-ca-cert" -}}
{{ .Values.global.serviceMesh.egress.ca.caCertsPath}}{{ .Values.global.serviceMesh.egress.ca.caCertificates}}
{{- end -}}

{{/*
 PM Server - Egress private key
*/}}
{{- define "eric-mxe-prometheus.egress-private-key" -}}
{{ .Values.global.serviceMesh.egress.pmServer.certsPath}}{{ .Values.global.serviceMesh.egress.pmServer.privateKey}}
{{- end -}}

{{/*
 PM Server - tls mode
*/}}
{{- define "eric-mxe-prometheus.tls-mode" -}}
{{ .Values.serviceMesh.tlsMode}}
{{- end }}

{{/*
 PM Server - Internal certificate common name
*/}}
{{- define "eric-mxe-prometheus.pm-service-name" -}}
{{ .Values.global.serviceMesh.egress.pmServer.serviceName }}
{{- end -}}

{{/*
 PM Server - ServiceEntry host name
*/}}
{{- define "eric-mxe-prometheus.pm-service-host" -}}
{{ include "eric-mxe-prometheus.pm-service-name" . }}.{{ .Release.Namespace }}.svc.cluster.local
{{- end -}}

{{/*
 PM Server - Internal certificate common name
*/}}
{{- define "eric-mxe-prometheus.pm-cert-name" -}}
{{ .Values.global.serviceMesh.egress.pmServer.clientCertificate }}
{{- end -}}

{{/*
 PM Server - Internal certificate common name
*/}}
{{- define "eric-mxe-prometheus.pm-private-key" -}}
{{ .Values.global.serviceMesh.egress.pmServer.privateKey}}
{{- end -}}

{{/*
 PM Server - Internal certificate common name
*/}}
{{- define "eric-mxe-prometheus.pm-ca-issuer" -}}
{{ .Values.global.serviceMesh.egress.pmServer.caIssuer}}
{{- end -}}

{{/*
 PM Server - Internal certificate common name
*/}}
{{- define "eric-mxe-prometheus.pm-gen-secret-name" -}}
{{ .Values.global.serviceMesh.egress.pmServer.genSecretName}}
{{- end -}}

{{/*
Standard labels of Helm and Kubernetes
*/}}
{{- define "eric-mxe-prometheus.standard-labels" -}}
app.kubernetes.io/part-of: "mxe"
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
helm.sh/chart: {{ include "eric-mxe-prometheus.chart" . | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
app.kubernetes.io/component: "prometheus-ingress"
{{- end -}}

{{/*
Create a user defined label - [DR-D1121-068] [DR-D1121-060]
*/}}
{{ define "eric-mxe-prometheus.config-labels" }}
  {{- $global := (.Values.global).labels -}}
  {{- $service := .Values.labels -}}
  {{- include "eric-mxe-prometheus.mergeLabels" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged labels for Default, which includes Standard and Config - Used in all files
*/}}
{{- define "eric-mxe-prometheus.labels" -}}
  {{- $standard := include "eric-mxe-prometheus.standard-labels" . | fromYaml -}}
  {{- $config := include "eric-mxe-prometheus.config-labels" . | fromYaml -}}
  {{- include "eric-mxe-prometheus.mergeLabels" (dict "location" .Template.Name "sources" (list $standard $config)) | trim }}
{{- end -}}

{{/*
Create a user defined annotation - [DR-D1121-060]
*/}}
{{ define "eric-mxe-prometheus.config-annotations" }}
  {{- $global := (.Values.global).annotations -}}
  {{- $service := .Values.annotations -}}
  {{- include "eric-mxe-prometheus.mergeAnnotations" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged annotations for Default, which includes productInfo and config - Used in all files
*/}}
{{- define "eric-mxe-prometheus.annotations" -}}
  {{- $productInfo := include "eric-mxe-prometheus.product-info" . | fromYaml -}}
  {{- $config := include "eric-mxe-prometheus.config-annotations" . | fromYaml -}}
  {{- include "eric-mxe-prometheus.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $config)) | trim }}
{{- end -}}

{{- define "eric-mxe-prometheus.ingress-annotations" -}}
{{ include "eric-mxe-prometheus.ingress-annotation-prefix" . }}/default-backend: {{ include "eric-mxe-prometheus.defaultbackend" . | quote }}
{{ include "eric-mxe-prometheus.ingress-annotation-prefix" . }}/rewrite-target: /$2
{{- if .Values.ingress.owasp.enabled }}
{{ include "eric-mxe-prometheus.ingress-annotation-prefix" . }}/enable-modsecurity: "true"
{{ include "eric-mxe-prometheus.ingress-annotation-prefix" . }}/modsecurity-snippet: |
  SecRule REQUEST_URI "@beginsWith /v1/prometheus/api/v1/query" "id:10001,phase:1,nolog,ctl:ruleRemoveById=942100;ARGS:query"
  Include /etc/nginx/owasp-modsecurity-crs/nginx-modsecurity.conf
  SecRuleEngine On
{{- end }}
{{ include "eric-mxe-prometheus.ingress-annotation-prefix" . }}/service-upstream: "true"
{{ include "eric-mxe-prometheus.ingress-annotation-prefix" . }}/upstream-vhost: eric-pm-server
{{- end -}}


