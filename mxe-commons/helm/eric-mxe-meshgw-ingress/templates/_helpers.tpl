{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-mxe-meshgw-ingress.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-mxe-meshgw-ingress.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create default backend name.
*/}}
{{- define "eric-mxe-meshgw-ingress.defaultbackend" -}}
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
{{- define "eric-mxe-meshgw-ingress.ingress-class" -}}
{{- if .Values.ingress.ingressClass -}}
{{- .Values.ingress.ingressClass -}}
{{- else -}}
eric-mxe-ingress-controller-class
{{- end -}}
{{- end -}}

{{/*
Create default ingress annotation prefix.
*/}}
{{- define "eric-mxe-meshgw-ingress.ingress-annotation-prefix" -}}
{{- if .Values.ingress.annotationPrefix -}}
{{- .Values.ingress.annotationPrefix -}}
{{- else -}}
mxe.nginx.ingress.kubernetes.io
{{- end -}}
{{- end -}}


{{/*
Create ingress annotations.
*/}}
{{- define "eric-mxe-meshgw-ingress.ingress-annotations" -}}
{{ include "eric-mxe-meshgw-ingress.ingress-annotation-prefix" . }}/default-backend: {{ include "eric-mxe-meshgw-ingress.defaultbackend" . | quote }}
{{ include "eric-mxe-meshgw-ingress.ingress-annotation-prefix" . }}/proxy-body-size: "20G"
{{ include "eric-mxe-meshgw-ingress.ingress-annotation-prefix" . }}/rewrite-target: /seldon/{{- .Release.Namespace -}}/$2/api/v0.1/predictions
{{- if and .Values.ingress.owasp.enabled .Values.ingress.modelServiceOwasp.enabled }}
{{ include "eric-mxe-meshgw-ingress.ingress-annotation-prefix" . }}/enable-modsecurity: "true"
{{ include "eric-mxe-meshgw-ingress.ingress-annotation-prefix" . }}/modsecurity-snippet: |
  Include /etc/nginx/owasp-modsecurity-crs/nginx-modsecurity.conf
  SecRuleEngine On
{{- end }}
{{ include "eric-mxe-meshgw-ingress.ingress-annotation-prefix" . }}/service-upstream: "true"
{{ include "eric-mxe-meshgw-ingress.ingress-annotation-prefix" . }}/upstream-vhost: {{ include "eric-mxe-meshgw-ingress.service-name" . | quote }}

{{- end -}}


{{/*
Create annotation for the product information (DR-D1121-064, DR-D1121-067)
*/}}
{{- define "eric-mxe-meshgw-ingress.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}

{{/*
TLS for ingress
*/}}
{{- define "eric-mxe-meshgw-ingress.ingress-tls" -}}
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

{{- define "eric-mxe-meshgw-ingress.service-name" -}}
{{- if .Values.service.fullnameOverride -}}
{{- .Values.service.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default "eric-mesh-ingressgateway" .Values.service.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Standard labels of Helm and Kubernetes
*/}}
{{- define "eric-mxe-meshgw-ingress.standard-labels" -}}
app.kubernetes.io/component: "eric-mesh-ingressgateway-ingress"
app.kubernetes.io/part-of: "mxe"
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/name: {{ include "eric-mxe-meshgw-ingress.name" . | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
helm.sh/chart: {{ include "eric-mxe-meshgw-ingress.chart" . | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
{{- end -}}

{{/*
Create a user defined label - [DR-D1121-068] [DR-D1121-060]
*/}}
{{ define "eric-mxe-meshgw-ingress.config-labels" }}
  {{- $global := (.Values.global).labels -}}
  {{- $service := .Values.labels -}}
  {{- include "eric-mxe-meshgw-ingress.mergeLabels" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged labels for Default, which includes Standard and Config - Used in all files
*/}}
{{- define "eric-mxe-meshgw-ingress.labels" -}}
  {{- $standard := include "eric-mxe-meshgw-ingress.standard-labels" . | fromYaml -}}
  {{- $config := include "eric-mxe-meshgw-ingress.config-labels" . | fromYaml -}}
  {{- include "eric-mxe-meshgw-ingress.mergeLabels" (dict "location" .Template.Name "sources" (list $standard $config)) | trim }}
{{- end -}}

{{/*
Create a user defined annotation - [DR-D1121-060]
*/}}
{{ define "eric-mxe-meshgw-ingress.config-annotations" }}
  {{- $global := (.Values.global).annotations -}}
  {{- $service := .Values.annotations -}}
  {{- include "eric-mxe-meshgw-ingress.mergeAnnotations" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged annotations for Default, which includes productInfo and config - Used in all files
*/}}
{{- define "eric-mxe-meshgw-ingress.annotations" -}}
  {{- $productInfo := include "eric-mxe-meshgw-ingress.product-info" . | fromYaml -}}
  {{- $config := include "eric-mxe-meshgw-ingress.config-annotations" . | fromYaml -}}
  {{- include "eric-mxe-meshgw-ingress.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $config)) | trim }}
{{- end -}}
{{/*
Merge global tolerations with service tolerations (DR-D1120-061-AD).
*/}}
{{- define "eric-mxe-meshgw-ingress.merge-tolerations" -}}
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
        {{- $globalItemId := include "eric-mxe-meshgw-ingress.merge-tolerations.get-identifier" $globalItem -}}
        {{- range $serviceItem := $serviceTolerations -}}
          {{- $serviceItemId := include "eric-mxe-meshgw-ingress.merge-tolerations.get-identifier" $serviceItem -}}
          {{- if eq $serviceItemId $globalItemId -}}
            {{- $matchingItems = append $matchingItems $serviceItem -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
      {{- range $globalItem := $globalTolerations -}}
        {{- $globalItemId := include "eric-mxe-meshgw-ingress.merge-tolerations.get-identifier" $globalItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-meshgw-ingress.merge-tolerations.get-identifier" $matchItem -}}
          {{- if eq $matchItemId $globalItemId -}}
            {{- $matchCount = add1 $matchCount -}}
          {{- end -}}
        {{- end -}}
        {{- if eq $matchCount 0 -}}
          {{- $nonMatchingItems = append $nonMatchingItems $globalItem -}}
        {{- end -}}
      {{- end -}}
      {{- range $serviceItem := $serviceTolerations -}}
        {{- $serviceItemId := include "eric-mxe-meshgw-ingress.merge-tolerations.get-identifier" $serviceItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-meshgw-ingress.merge-tolerations.get-identifier" $matchItem -}}
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
{{ define "eric-mxe-meshgw-ingress.merge-tolerations.get-identifier" }}
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
