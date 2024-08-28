{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-mxe-commons-pre-upgrade-job.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-mxe-commons-pre-upgrade-job.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the role binding.
*/}}
{{- define "eric-mxe-commons-pre-upgrade-job.rolebinding" -}}
{{- $name := include "eric-mxe-commons-pre-upgrade-job.name" . -}}
{{- printf "%s-%s" $name "rolebinding" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the role.
*/}}
{{- define "eric-mxe-commons-pre-upgrade-job.role" -}}
{{- $name := include "eric-mxe-commons-pre-upgrade-job.name" . -}}
{{- printf "%s-%s" $name "role" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the service account of the chart.
*/}}
{{- define "eric-mxe-commons-pre-upgrade-job.serviceaccount" -}}
{{- $name := include "eric-mxe-commons-pre-upgrade-job.name" . -}}
{{- printf "%s-%s" $name "serviceaccount" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Image pull secret name
*/}}
{{- define "eric-mxe-commons-pre-upgrade-job.image-pull-secret-name" -}}
{{- if .Values.global.pullSecret -}}
{{- .Values.global.pullSecret -}}
{{- else if .Values.global.registry.pullSecret -}}
{{- .Values.global.registry.pullSecret -}}
{{- else if .Values.imageCredentials.pullSecret -}}
{{- .Values.imageCredentials.pullSecret -}}
{{- else if .Values.imageCredentials.registry.url -}}
{{- if .Values.imageCredentials.registry.pullSecret -}}
{{- .Values.imageCredentials.registry.pullSecret -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Image pull secrets
*/}}
{{- define "eric-mxe-commons-pre-upgrade-job.image-pull-secrets" -}}
{{- $name := include "eric-mxe-commons-pre-upgrade-job.image-pull-secret-name" . -}}
{{- if $name -}}
imagePullSecrets:
  - name: {{ $name | quote -}}
{{- end -}}
{{- end -}}


{{/*
Image repository
*/}}
{{- define "eric-mxe-commons-pre-upgrade-job.image-repository" -}}
{{- if .Values.imageCredentials.registry.url -}}
{{- .Values.imageCredentials.registry.url -}}
{{- else -}}
{{- .Values.global.registry.url -}}
{{- end -}}
{{- printf "/%s/" .Values.imageCredentials.repoPath -}}
{{- end -}}


{{/*
Product information of Ericsson products
*/}}
{{- define "eric-mxe-commons-pre-upgrade-job.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}


{{/*
Job Selector
*/}}
{{- define "eric-mxe-commons-pre-upgrade-job.jobSelector" -}}
{{- if .Values.includeReleaseNameSelector -}}
{{- print "app.kubernetes.io/instance=" .Release.Name  "," -}}
{{- end -}}
{{- .Values.jobSelector -}}
{{- end -}}

{{/*
   Kubernetes Availability and Resiliency Design Rule DR-D1120-060-AD
*/}}
{{- define "eric-mxe-commons-pre-upgrade-job.tolerations" -}}
  {{- if gt (len .Values.tolerations) 0 -}}
  tolerations:
  {{- range .Values.tolerations }}
      {{- if and (eq .operator "Exists") (ne .value "") }}
          {{- fail "Value must be empty when 'operator' is 'Exists'"}}
      {{- else if and (ne .operator "Exists") (eq .key "") }}
          {{- fail "Operator must be 'Exists' when 'key' is empty." }}
      {{- else if and (ne .effect "NoExecute") (.tolerationSeconds)}}
          {{- fail "Effect must be 'NoExecute' when 'tolerationSeconds' is set." }}
      {{- else }}
      - key: {{ .key | quote }}
        value: {{ .value | quote }}
        operator: {{ .operator | quote }}
        effect: {{ .effect | quote }}
        tolerationSeconds: 300
      {{- end }}
    {{- end }}
  {{- else -}}
  tolerations: []
  {{- end -}}
{{- end -}}

{{/*
Standard labels of Helm and Kubernetes
*/}}
{{- define "eric-mxe-commons-pre-upgrade-job.standard-labels" -}}
app.kubernetes.io/part-of: "mxe"
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/name: {{ include "eric-mxe-commons-pre-upgrade-job.name" . | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
helm.sh/chart: {{ include "eric-mxe-commons-pre-upgrade-job.chart" . | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
{{- end -}}

{{/*
Create a user defined label - [DR-D1121-068] [DR-D1121-060]
*/}}
{{ define "eric-mxe-commons-pre-upgrade-job.config-labels" }}
  {{- $global := (.Values.global).labels -}}
  {{- $service := .Values.labels -}}
  {{- include "eric-mxe-commons-pre-upgrade-job.mergeLabels" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged labels for Default, which includes Standard and Config - Used in all files
*/}}
{{- define "eric-mxe-commons-pre-upgrade-job.labels" -}}
  {{- $standard := include "eric-mxe-commons-pre-upgrade-job.standard-labels" . | fromYaml -}}
  {{- $config := include "eric-mxe-commons-pre-upgrade-job.config-labels" . | fromYaml -}}
  {{- include "eric-mxe-commons-pre-upgrade-job.mergeLabels" (dict "location" .Template.Name "sources" (list $standard $config)) | trim }}
{{- end -}}

{{/*
Create a user defined annotation - [DR-D1121-060]
*/}}
{{ define "eric-mxe-commons-pre-upgrade-job.config-annotations" }}
  {{- $global := (.Values.global).annotations -}}
  {{- $service := .Values.annotations -}}
  {{- include "eric-mxe-commons-pre-upgrade-job.mergeAnnotations" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged annotations for Default, which includes productInfo and config - Used in all files
*/}}
{{- define "eric-mxe-commons-pre-upgrade-job.annotations" -}}
  {{- $productInfo := include "eric-mxe-commons-pre-upgrade-job.product-info" . | fromYaml -}}
  {{- $config := include "eric-mxe-commons-pre-upgrade-job.config-annotations" . | fromYaml -}}
  {{- include "eric-mxe-commons-pre-upgrade-job.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $config)) | trim }}
{{- end -}}

{{- define "eric-mxe-commons-pre-upgrade-job.hook-weight" }}
{{- $val := . }}
helm.sh/hook-weight: {{ quote $val }}
{{- end -}}

{{- define "eric-mxe-commons-pre-upgrade-job.istio-annotations" -}}
sidecar.istio.io/rewriteAppHTTPProbers: "false"
{{- end -}}

{{- define "eric-mxe-commons-pre-upgrade-job.istio-labels" }}
sidecar.istio.io/inject: "false"
{{- end -}}
