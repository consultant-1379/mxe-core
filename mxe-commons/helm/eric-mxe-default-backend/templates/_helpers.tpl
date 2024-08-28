{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-mxe-default-backend.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
Removed release Name override for DR-D1121-064 Complaince
*/}}
{{- define "eric-mxe-default-backend.fullname" -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- printf "%s" $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-mxe-default-backend.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Image pull secrets DR-D1123-115
*/}}
{{- define "eric-mxe-default-backend.image-pull-secrets" -}}
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
{{- define "eric-mxe-default-backend.image-repository" -}}
{{- if .Values.imageCredentials.registry.url -}}
{{- .Values.imageCredentials.registry.url -}}
{{- else -}}
{{- .Values.global.registry.url -}}
{{- end -}}
{{- printf "/%s/" .Values.imageCredentials.repoPath -}}
{{- end -}}

{{/*
Create annotation for the product information (DR-D1121-064, DR-D1121-067)
*/}}
{{- define "eric-mxe-default-backend.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}

{{/*
Update strategy for deployments
*/}}
{{- define "eric-mxe-default-backend.update-strategy" -}}
strategy:
  type: {{ .Values.updateStrategy.type | quote }}
{{- if eq .Values.updateStrategy.type "RollingUpdate" }}
  rollingUpdate:
    maxUnavailable: {{ .Values.updateStrategy.rollingUpdate.maxUnavailable }}
    maxSurge: {{ .Values.updateStrategy.rollingUpdate.maxSurge -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations, labels
*/}}
{{- define "eric-mxe-default-backend.istio-sidecar-annotations" -}}
sidecar.istio.io/rewriteAppHTTPProbers: {{ .Values.global.serviceMesh.enabled | quote }}
{{- end -}}

{{- define "eric-mxe-default-backend.istio-sidecar-labels" -}}
sidecar.istio.io/inject: {{ .Values.global.serviceMesh.enabled | quote }}
{{- end -}}

{{/*
Security Context for Container Image
*/}}
{{- define "eric-mxe-default-backend.container-security-context" -}}
securityContext:
  capabilities:
    drop:
      - all
  privileged: false
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

{{- define "eric-mxe-default-backend.podPriority" -}}
{{- if .Values.podPriority -}}
  {{- if .Values.podPriority.mxeDefaultBackend -}}
    {{- .Values.podPriority.mxeDefaultBackend.priorityClassName | toString -}}
  {{- end -}}
{{- end -}}
{{- end -}}


{{/*
Standard labels of Helm and Kubernetes
*/}}
{{- define "eric-mxe-default-backend.standard-labels" -}}
app.kubernetes.io/part-of: "mxe"
app.kubernetes.io/name: {{ include "eric-mxe-default-backend.name" . | quote }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
helm.sh/chart: {{ include "eric-mxe-default-backend.chart" . | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
app.kubernetes.io/component: "default-backend"
{{- end -}}

{{/*
Create a user defined label - [DR-D1121-068] [DR-D1121-060]
*/}}
{{ define "eric-mxe-default-backend.config-labels" }}
  {{- $global := (.Values.global).labels -}}
  {{- $service := .Values.labels -}}
  {{- include "eric-mxe-default-backend.mergeLabels" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged labels for Default, which includes Standard and Config - Used in all files
*/}}
{{- define "eric-mxe-default-backend.labels" -}}
  {{- $standard := include "eric-mxe-default-backend.standard-labels" . | fromYaml -}}
  {{- $config := include "eric-mxe-default-backend.config-labels" . | fromYaml -}}
  {{- include "eric-mxe-default-backend.mergeLabels" (dict "location" .Template.Name "sources" (list $standard $config)) | trim }}
{{- end -}}

{{/*
Create a user defined annotation - [DR-D1121-060]
*/}}
{{ define "eric-mxe-default-backend.config-annotations" }}
  {{- $global := (.Values.global).annotations -}}
  {{- $service := .Values.annotations -}}
  {{- include "eric-mxe-default-backend.mergeAnnotations" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged annotations for Default, which includes productInfo and config - Used in all files
*/}}
{{- define "eric-mxe-default-backend.annotations" -}}
  {{- $productInfo := include "eric-mxe-default-backend.product-info" . | fromYaml -}}
  {{- $config := include "eric-mxe-default-backend.config-annotations" . | fromYaml -}}
  {{- include "eric-mxe-default-backend.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $config)) | trim }}
{{- end -}}

{{/*
Security annotations
*/}}
{{- define "eric-mxe-default-backend.security-annotations" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{/*
Create a map from ".Values.global" with defaults if missing in values file.
This hides defaults from values file.
*/}}
{{ define "eric-mxe-default-backend.global" }}
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
{{ define "eric-mxe-default-backend.nodeSelector" }}
{{- $g := fromJson (include "eric-mxe-default-backend.global" .) -}}
{{- $global := $g.nodeSelector -}}
{{- $service := .Values.nodeSelector -}}
{{- include "eric-mxe-default-backend.aggregatedMerge" (dict "context" "nodeSelector" "location" .Template.Name "sources" (list $global $service)) -}}
{{ end }}

{{/*
Added below to handle DR-D1121-102 for globalRegistryPullPolicy Shall be Used
*/}}
{{- define "eric-mxe-default-backend.registryImagePullPolicy" -}}
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
{{- define "eric-mxe-default-backend.topologySpreadConstraints" }}
{{- if .Values.topologySpreadConstraints }}
{{- range $config, $values := .Values.topologySpreadConstraints }}
- topologyKey: {{ $values.topologyKey }}
  maxSkew: {{ $values.maxSkew | default 1 }}
  whenUnsatisfiable: {{ $values.whenUnsatisfiable | default "ScheduleAnyway" }}
{{- end }}
{{- end }}
{{- end }}

{{- define "eric-mxe-default-backend.ericProdInfoRepoPath" }}
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

{{- define "eric-mxe-default-backend.ericProdInfoRegistry" }}
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
eric-mxe-default-backend image name path
*/}}
{{- define "eric-mxe-default-backend.setImageRepo" }}
  {{- $top := index . 0 }}
  {{- $serviceLevelImageRegistry := index . 1 }}
  {{- $serviceLevelImageRepoPath := index . 2 }}
  {{- $imageName := index . 3 }}

  {{- /*
  Registry and Repopath for the specified image container is fetched from eric-product-info.yaml
  */}}
  {{- $productInfo := fromYaml ($top.Files.Get "eric-product-info.yaml") -}}
  {{- $registryUrl := include "eric-mxe-default-backend.ericProdInfoRegistry" (list $productInfo $imageName) -}}
  {{- $imageRepoPath := include "eric-mxe-default-backend.ericProdInfoRepoPath" (list $productInfo $imageName) -}}

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
Prometheus annotations
*/}}
{{- define "eric-mxe-default-backend.prometheus-annotations" -}}
prometheus.io/path: {{ .Values.global.mxePrometheusPath | quote }}
prometheus.io/port: {{ .Values.global.mxePrometheusPort | quote }}
prometheus.io/scrape: {{ .Values.global.mxePrometheusScrape | quote }}
prometheus.io/scrape-role: {{ .Values.global.mxePrometheusScrapeRole | quote }}
prometheus.io/scrape-interval: {{ .Values.global.mxePrometheusScrapeInterval | quote }}
{{- end -}}
{{/*
Merge global tolerations with service tolerations (DR-D1120-061-AD).
*/}}
{{- define "eric-mxe-default-backend.merge-tolerations" -}}
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
        {{- $globalItemId := include "eric-mxe-default-backend.merge-tolerations.get-identifier" $globalItem -}}
        {{- range $serviceItem := $serviceTolerations -}}
          {{- $serviceItemId := include "eric-mxe-default-backend.merge-tolerations.get-identifier" $serviceItem -}}
          {{- if eq $serviceItemId $globalItemId -}}
            {{- $matchingItems = append $matchingItems $serviceItem -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
      {{- range $globalItem := $globalTolerations -}}
        {{- $globalItemId := include "eric-mxe-default-backend.merge-tolerations.get-identifier" $globalItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-default-backend.merge-tolerations.get-identifier" $matchItem -}}
          {{- if eq $matchItemId $globalItemId -}}
            {{- $matchCount = add1 $matchCount -}}
          {{- end -}}
        {{- end -}}
        {{- if eq $matchCount 0 -}}
          {{- $nonMatchingItems = append $nonMatchingItems $globalItem -}}
        {{- end -}}
      {{- end -}}
      {{- range $serviceItem := $serviceTolerations -}}
        {{- $serviceItemId := include "eric-mxe-default-backend.merge-tolerations.get-identifier" $serviceItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-default-backend.merge-tolerations.get-identifier" $matchItem -}}
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
{{ define "eric-mxe-default-backend.merge-tolerations.get-identifier" }}
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
 postgres certificate name
*/}}
{{- define "eric-mxe-default-backend.pg-client-cert" -}}
{{ .Values.global.serviceMesh.egress.postgres.clientCertificate }}
{{- end -}}

{{/*
postgres certificate private key
*/}}
{{- define "eric-mxe-default-backend.pg-private-key" -}}
{{ .Values.global.serviceMesh.egress.postgres.privateKey }}
{{- end -}}

{{/*
postgres certificate ca
*/}}
{{- define "eric-mxe-default-backend.pg-ca-issuer" -}}
{{ .Values.global.serviceMesh.egress.postgres.caIssuer }}
{{- end -}}

{{/*
pg-cert - volume mounts
*/}}
{{- define "eric-mxe-default-backend.pg-cert-volume-mounts" -}}
{{- if .Values.global.security.tls.enabled }}
- name: pg-cert
  mountPath: /run/secrets/certificates/client/pg-cert
  readOnly: true
- name: trusted-ca
  mountPath: /run/secrets/certificates/trusted
  readOnly: true
{{- end -}}
{{- end -}}

{{/*
pg-cert -  volumes
*/}}
{{- define "eric-mxe-default-backend.pg-cert-volumes" -}}
{{- if .Values.global.security.tls.enabled }}
- name: trusted-ca
  secret:
    secretName: eric-sec-sip-tls-trusted-root-cert
- name: pg-cert
  secret:
    secretName: {{ include "eric-mxe-default-backend.name" . }}-pg-cert
{{- end -}}
{{- end -}}
