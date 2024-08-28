{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-mxe-serving-pre-upgrade-job.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-mxe-serving-pre-upgrade-job.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the role binding.
*/}}
{{- define "eric-mxe-serving-pre-upgrade-job.rolebinding" -}}
{{- $name := include "eric-mxe-serving-pre-upgrade-job.name" . -}}
{{- printf "%s-%s" $name "rolebinding" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the role.
*/}}
{{- define "eric-mxe-serving-pre-upgrade-job.role" -}}
{{- $name := include "eric-mxe-serving-pre-upgrade-job.name" . -}}
{{- printf "%s-%s" $name "role" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the service account of the chart.
*/}}
{{- define "eric-mxe-serving-pre-upgrade-job.serviceaccount" -}}
{{- $name := include "eric-mxe-serving-pre-upgrade-job.name" . -}}
{{- printf "%s-%s" $name "serviceaccount" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Image pull secret name
*/}}
{{- define "eric-mxe-serving-pre-upgrade-job.image-pull-secret-name" -}}
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
{{- define "eric-mxe-serving-pre-upgrade-job.image-pull-secrets" -}}
{{- $name := include "eric-mxe-serving-pre-upgrade-job.image-pull-secret-name" . -}}
{{- if $name -}}
imagePullSecrets:
  - name: {{ $name | quote -}}
{{- end -}}
{{- end -}}


{{/*
Image repository
*/}}
{{- define "eric-mxe-serving-pre-upgrade-job.image-repository" -}}
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
{{- define "eric-mxe-serving-pre-upgrade-job.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}


{{/*
Job Selector
*/}}
{{- define "eric-mxe-serving-pre-upgrade-job.jobSelector" -}}
{{- if .Values.includeReleaseNameSelector -}}
{{- print "app.kubernetes.io/instance=" .Release.Name  "," -}}
{{- end -}}
{{- .Values.jobSelector -}}
{{- end -}}

{{/*
Merge global tolerations with service tolerations (DR-D1120-061-AD).
*/}}
{{- define "eric-mxe-serving-pre-upgrade-job.merge-tolerations" -}}
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
        {{- $globalItemId := include "eric-mxe-serving-pre-upgrade-job.merge-tolerations.get-identifier" $globalItem -}}
        {{- range $serviceItem := $serviceTolerations -}}
          {{- $serviceItemId := include "eric-mxe-serving-pre-upgrade-job.merge-tolerations.get-identifier" $serviceItem -}}
          {{- if eq $serviceItemId $globalItemId -}}
            {{- $matchingItems = append $matchingItems $serviceItem -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
      {{- range $globalItem := $globalTolerations -}}
        {{- $globalItemId := include "eric-mxe-serving-pre-upgrade-job.merge-tolerations.get-identifier" $globalItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-serving-pre-upgrade-job.merge-tolerations.get-identifier" $matchItem -}}
          {{- if eq $matchItemId $globalItemId -}}
            {{- $matchCount = add1 $matchCount -}}
          {{- end -}}
        {{- end -}}
        {{- if eq $matchCount 0 -}}
          {{- $nonMatchingItems = append $nonMatchingItems $globalItem -}}
        {{- end -}}
      {{- end -}}
      {{- range $serviceItem := $serviceTolerations -}}
        {{- $serviceItemId := include "eric-mxe-serving-pre-upgrade-job.merge-tolerations.get-identifier" $serviceItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-serving-pre-upgrade-job.merge-tolerations.get-identifier" $matchItem -}}
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
{{ define "eric-mxe-serving-pre-upgrade-job.merge-tolerations.get-identifier" }}
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

{{/*
Standard labels of Helm and Kubernetes
*/}}
{{- define "eric-mxe-serving-pre-upgrade-job.standard-labels" -}}
app.kubernetes.io/part-of: "mxe"
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
helm.sh/chart: {{ include "eric-mxe-serving-pre-upgrade-job.chart" . | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
app.kubernetes.io/name: {{ include "eric-mxe-serving-pre-upgrade-job.name" . | quote }}
{{- end -}}

{{/*
Create a user defined label - [DR-D1121-068] [DR-D1121-060]
*/}}
{{ define "eric-mxe-serving-pre-upgrade-job.config-labels" }}
  {{- $global := (.Values.global).labels -}}
  {{- $service := .Values.labels -}}
  {{- include "eric-mxe-serving-pre-upgrade-job.mergeLabels" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged labels for Default, which includes Standard and Config - Used in all files
*/}}
{{- define "eric-mxe-serving-pre-upgrade-job.labels" -}}
  {{- $standard := include "eric-mxe-serving-pre-upgrade-job.standard-labels" . | fromYaml -}}
  {{- $config := include "eric-mxe-serving-pre-upgrade-job.config-labels" . | fromYaml -}}
  {{- include "eric-mxe-serving-pre-upgrade-job.mergeLabels" (dict "location" .Template.Name "sources" (list $standard $config)) | trim }}
{{- end -}}

{{/*
Create a user defined annotation - [DR-D1121-060]
*/}}
{{ define "eric-mxe-serving-pre-upgrade-job.config-annotations" }}
  {{- $global := (.Values.global).annotations -}}
  {{- $service := .Values.annotations -}}
  {{- include "eric-mxe-serving-pre-upgrade-job.mergeAnnotations" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged annotations for Default, which includes productInfo and config - Used in all files
*/}}
{{- define "eric-mxe-serving-pre-upgrade-job.annotations" -}}
  {{- $productInfo := include "eric-mxe-serving-pre-upgrade-job.product-info" . | fromYaml -}}
  {{- $config := include "eric-mxe-serving-pre-upgrade-job.config-annotations" . | fromYaml -}}
  {{- include "eric-mxe-serving-pre-upgrade-job.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $config)) | trim }}
{{- end -}}

{{- define "eric-mxe-serving-pre-upgrade-job.hook-weight" }}
{{- $val := . }}
helm.sh/hook-weight: {{ quote $val }}
{{- end -}}

{{- define "eric-mxe-serving-pre-upgrade-job.istio-annotations" -}}

sidecar.istio.io/rewriteAppHTTPProbers: "false"
{{- end -}}

{{- define "eric-mxe-serving-pre-upgrade-job.istio-labels" }}
sidecar.istio.io/inject: "false"
{{- end -}}