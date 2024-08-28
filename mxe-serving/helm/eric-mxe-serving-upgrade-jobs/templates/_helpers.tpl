{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Product information of Ericsson products (DR-D1121-064, DR-D1121-067)
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}


{{/*
Add securityContext for the containers
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.main-security-context" -}}
securityContext:
  allowPrivilegeEscalation: false
  runAsNonRoot: true
  capabilities:
    drop:
      - ALL
  privileged: false
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
   Kubernetes Availability and Resiliency Design Rule DR-D1120-060-AD
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.tolerations" -}}
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
        tolerationSeconds: {{ .tolerationSeconds }}
      {{- end }}
    {{- end }}
  {{- else -}}
  tolerations: []
  {{- end -}}
{{- end -}}
{{/*
Merge global tolerations with service tolerations (DR-D1120-061-AD).
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.merge-tolerations" -}}
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
        {{- $globalItemId := include "eric-mxe-serving-upgrade-jobs.merge-tolerations.get-identifier" $globalItem -}}
        {{- range $serviceItem := $serviceTolerations -}}
          {{- $serviceItemId := include "eric-mxe-serving-upgrade-jobs.merge-tolerations.get-identifier" $serviceItem -}}
          {{- if eq $serviceItemId $globalItemId -}}
            {{- $matchingItems = append $matchingItems $serviceItem -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
      {{- range $globalItem := $globalTolerations -}}
        {{- $globalItemId := include "eric-mxe-serving-upgrade-jobs.merge-tolerations.get-identifier" $globalItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-serving-upgrade-jobs.merge-tolerations.get-identifier" $matchItem -}}
          {{- if eq $matchItemId $globalItemId -}}
            {{- $matchCount = add1 $matchCount -}}
          {{- end -}}
        {{- end -}}
        {{- if eq $matchCount 0 -}}
          {{- $nonMatchingItems = append $nonMatchingItems $globalItem -}}
        {{- end -}}
      {{- end -}}
      {{- range $serviceItem := $serviceTolerations -}}
        {{- $serviceItemId := include "eric-mxe-serving-upgrade-jobs.merge-tolerations.get-identifier" $serviceItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-serving-upgrade-jobs.merge-tolerations.get-identifier" $matchItem -}}
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
{{ define "eric-mxe-serving-upgrade-jobs.merge-tolerations.get-identifier" }}
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
Image pull secrets
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.image-pull-secrets" -}}
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
Create name for the updater job of seldondeployments
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.seldondeployment-updater-name" -}}
{{- if (index .Values "seldondeployment-updater" "fullNameOverride") -}}
{{- index .Values "seldondeployment-updater" "fullNameOverride" -}}
{{- else -}}
{{- $name := include "eric-mxe-serving-upgrade-jobs.name" . -}}
{{- printf "%s-%s" $name "sdep-updater" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
Expand the service account of the chart.
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.seldondeployment-updater-service-account" -}}
{{- $name := include "eric-mxe-serving-upgrade-jobs.name" . -}}
{{- printf "%s-%s" $name "sdep-updater-service-account" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the role binding.
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.seldondeployment-updater-role-binding" -}}
{{- $name := include "eric-mxe-serving-upgrade-jobs.name" . -}}
{{- printf "%s-%s" $name "sdep-updater-role-binding" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the role.
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.seldondeployment-updater-role" -}}
{{- $name := include "eric-mxe-serving-upgrade-jobs.name" . -}}
{{- printf "%s-%s" $name "sdep-updater-role" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the configmap.
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.seldondeployment-updater-configmap" -}}
{{- $name := include "eric-mxe-serving-upgrade-jobs.name" . -}}
{{- printf "%s-%s" $name "sdep-updater-configmap" | trunc 63 | trimSuffix "-" -}}
{{- end -}}


{{/*
Is Gitops repo using HTTP/SSH ?
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.seldondeployment-updater-gitOpsRepoConnProtocol" -}}
{{- $repoURL :=  ( index .Values "seldondeployment-updater" "gitOpsRepo" "url" ) -}}
{{- if hasPrefix "http" $repoURL -}}
{{- "http"  -}}
{{- else -}}
{{- "ssh" -}}
{{- end -}}
{{- end -}}

{{/*
Create name for the updater job of seldondeployments
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.preupgrade-cleanup-name" -}}
{{- if (index .Values "preupgrade-cleanup" "fullNameOverride") -}}
{{- index .Values "preupgrade-cleanup" "fullNameOverride" -}}
{{- else -}}
{{- $name := include "eric-mxe-serving-upgrade-jobs.name" . -}}
{{- printf "%s-%s" $name "preupgrade-cleanup" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
Expand the service account of the chart.
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.preupgrade-cleanup-service-account" -}}
{{- $name := include "eric-mxe-serving-upgrade-jobs.name" . -}}
{{- printf "%s-%s" $name "preupgrade-cleanup-service-account" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the cluster role binding.
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.preupgrade-cleanup-cluster-role-binding" -}}
{{- $name := include "eric-mxe-serving-upgrade-jobs.name" . -}}
{{- printf "%s-%s" $name "preupgrade-cleanup-cluster-role-binding" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the cluster role.
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.preupgrade-cleanup-cluster-role" -}}
{{- $name := include "eric-mxe-serving-upgrade-jobs.name" . -}}
{{- printf "%s-%s" $name "preupgrade-cleanup-cluster-role" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the role binding.
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.preupgrade-cleanup-role-binding" -}}
{{- $name := include "eric-mxe-serving-upgrade-jobs.name" . -}}
{{- printf "%s-%s" $name "preupgrade-cleanup-role-binding" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the role.
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.preupgrade-cleanup-role" -}}
{{- $name := include "eric-mxe-serving-upgrade-jobs.name" . -}}
{{- printf "%s-%s" $name "preupgrade-cleanup-role" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the configmap.
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.preupgrade-cleanup-configmap" -}}
{{- $name := include "eric-mxe-serving-upgrade-jobs.name" . -}}
{{- printf "%s-%s" $name "preupgrade-cleanup-configmap" | trunc 63 | trimSuffix "-" -}}
{{- end -}}


{{/*
Expand the name of the Seldon's validating webhook.
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.preupgrade-cleanup-seldon-validating-webhook-name" -}}
{{- if (index .Values "preupgrade-cleanup" "seldonValidatingWebhookNameOverride") -}}
{{- index .Values "preupgrade-cleanup" "seldonValidatingWebhookNameOverride" -}}
{{- else -}}
seldon-validating-webhook-configuration-{{ .Release.Namespace }}
{{- end -}}
{{- end -}}

{{/*
Expand the name of the Seldon's mutating webhook.
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.preupgrade-cleanup-seldon-mutating-webhook-name" -}}
{{- if (index .Values "preupgrade-cleanup" "seldonMutatingWebhookNameOverride") -}}
{{- index .Values "preupgrade-cleanup" "seldonMutatingWebhookNameOverride" -}}
{{- else -}}
seldon-mutating-webhook-configuration-{{ .Release.Namespace }}
{{- end -}}
{{- end -}}

{{/*preupgrade-cleanup-ambassador-name
Expand the name of Ambassador.
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.preupgrade-cleanup-ambassador-name" -}}
{{- if (index .Values "preupgrade-cleanup" "ambassadorNameOverride") -}}
{{- index .Values "preupgrade-cleanup" "ambassadorNameOverride" -}}
{{- else -}}
{{- if contains "ambassador" .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name "ambassador" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Expand the name of the Model catalogue service's db secret.
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.preupgrade-cleanup-model-catalogue-service-db-secret-name" -}}
{{- if (index .Values "preupgrade-cleanup" "modelCatalogueServiceDbSecretNameOverride") -}}
{{- index .Values "preupgrade-cleanup" "modelCatalogueServiceDbSecretNameOverride" -}}
{{- else -}}
eric-mxe-model-catalogue-service-db-hooked
{{- end -}}
{{- end -}}


{{/*
Standard labels of Helm and Kubernetes
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.standard-labels" -}}
app.kubernetes.io/part-of: "mxe"
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
helm.sh/chart: {{ include "eric-mxe-serving-upgrade-jobs.chart" . | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
app.kubernetes.io/name: {{ include "eric-mxe-serving-upgrade-jobs.name" . | quote }}
{{- end -}}

{{/*
Create a user defined label - [DR-D1121-068] [DR-D1121-060]
*/}}
{{ define "eric-mxe-serving-upgrade-jobs.config-labels" }}
  {{- $global := (.Values.global).labels -}}
  {{- $service := .Values.labels -}}
  {{- include "eric-mxe-serving-upgrade-jobs.mergeLabels" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged labels for Default, which includes Standard and Config - Used in all files
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.labels" -}}
  {{- $standard := include "eric-mxe-serving-upgrade-jobs.standard-labels" . | fromYaml -}}
  {{- $config := include "eric-mxe-serving-upgrade-jobs.config-labels" . | fromYaml -}}
  {{- include "eric-mxe-serving-upgrade-jobs.mergeLabels" (dict "location" .Template.Name "sources" (list $standard $config)) | trim }}
{{- end -}}

{{/*
Create a user defined annotation - [DR-D1121-060]
*/}}
{{ define "eric-mxe-serving-upgrade-jobs.config-annotations" }}
  {{- $global := (.Values.global).annotations -}}
  {{- $service := .Values.annotations -}}
  {{- include "eric-mxe-serving-upgrade-jobs.mergeAnnotations" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged annotations for Default, which includes productInfo and config - Used in all files
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.annotations" -}}
  {{- $productInfo := include "eric-mxe-serving-upgrade-jobs.product-info" . | fromYaml -}}
  {{- $config := include "eric-mxe-serving-upgrade-jobs.config-annotations" . | fromYaml -}}
  {{- include "eric-mxe-serving-upgrade-jobs.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $config)) | trim }}
{{- end -}}

{{/*
Security annotations
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.security-annotations-seldondeployment" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
container.apparmor.security.beta.kubernetes.io/seldondeployment-updater-job: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{- define "eric-mxe-serving-upgrade-jobs.security-annotations-preupgrade-cleanup" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
container.apparmor.security.beta.kubernetes.io/preupgrade-cleanup-job: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{/*
Added below to handle DR-D1121-102 for globalRegistryPullPolicy Shall be Used
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.registryImagePullPolicy" -}}
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

{{- define "eric-mxe-serving-upgrade-jobs.hook-weight" }}
{{- $val := . }}
helm.sh/hook-weight: {{ quote $val }}
{{- end -}}

{{- define "eric-mxe-serving-upgrade-jobs.istio-annotations" }}
sidecar.istio.io/rewriteAppHTTPProbers: "false"
{{- end -}}

{{- define "eric-mxe-serving-upgrade-jobs.istio-labels" }}
sidecar.istio.io/inject: "false"
{{- end -}}

{{- define "eric-mxe-serving-upgrade-jobs.ericProdInfoRepoPath" }}
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

{{- define "eric-mxe-serving-upgrade-jobs.ericProdInfoRegistry" }}
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
eric-mxe-serving-upgrade-jobs image name path
*/}}
{{- define "eric-mxe-serving-upgrade-jobs.setImageRepo" }}
  {{- $top := index . 0 }}
  {{- $serviceLevelImageRegistry := index . 1 }}
  {{- $serviceLevelImageRepoPath := index . 2 }}
  {{- $imageName := index . 3 }}

  {{- /*
  Registry and Repopath for the specified image container is fetched from eric-product-info.yaml
  */}}
  {{- $productInfo := fromYaml ($top.Files.Get "eric-product-info.yaml") -}}
  {{- $registryUrl := include "eric-mxe-serving-upgrade-jobs.ericProdInfoRegistry" (list $productInfo $imageName) -}}
  {{- $imageRepoPath := include "eric-mxe-serving-upgrade-jobs.ericProdInfoRepoPath" (list $productInfo $imageName) -}}

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
