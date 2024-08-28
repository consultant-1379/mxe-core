{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-mxe-ingress-controller.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the namespace of the chart.
*/}}
{{- define "eric-mxe-ingress-controller.namespace" -}}
{{- $name := include "eric-mxe-ingress-controller.name" . -}}
{{- default $name .Values.namespaceOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of nginx-configmap
*/}}
{{- define "eric-mxe-ingress-controller.nginx-configmap" -}}
{{- $fullname := include "eric-mxe-ingress-controller.name" . -}}
{{- printf "%s-%s" $fullname "nginx-configuration" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of tcp-services-configmap
*/}}
{{- define "eric-mxe-ingress-controller.tcp-services-configmap" -}}
{{- $fullname := include "eric-mxe-ingress-controller.name" . -}}
{{- printf "%s-%s" $fullname "tcp-services" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of udp-services-configmap
*/}}
{{- define "eric-mxe-ingress-controller.udp-services-configmap" -}}
{{- $fullname := include "eric-mxe-ingress-controller.name" . -}}
{{- printf "%s-%s" $fullname "udp-services" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Security Context for Container Image
*/}}
{{- define "eric-mxe-ingress-controller.container-security-context" -}}
securityContext:
  allowPrivilegeEscalation: false
  capabilities:
    drop:
      - ALL
    add:
      - NET_BIND_SERVICE
  privileged: false
  runAsNonRoot: true
  # www-data -> 101
  runAsUser: 101
  {{ if .Values.seccompProfile.type -}}
  seccompProfile:
    type: {{ .Values.seccompProfile.type }}
    {{ if eq .Values.seccompProfile.type "Localhost" -}}
    localhostProfile: {{ .Values.seccompProfile.localhostProfile }}
    {{- end -}}
  {{- end -}}
{{- end -}}

{{/*
Expand the service account of the chart.
*/}}
{{- define "eric-mxe-ingress-controller.serviceaccount" -}}
{{- $fullname := include "eric-mxe-ingress-controller.name" . -}}
{{- $defaultname := printf "%s-%s" $fullname "serviceaccount" -}}
{{- default $defaultname .Values.serviceAccountNameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of clusterrole
*/}}
{{- define "eric-mxe-ingress-controller.clusterrole" -}}
{{- $fullname := include "eric-mxe-ingress-controller.name" . -}}
{{- printf "%s-%s" $fullname "clusterrole" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of role
*/}}
{{- define "eric-mxe-ingress-controller.role" -}}
{{- $fullname := include "eric-mxe-ingress-controller.name" . -}}
{{- printf "%s-%s" $fullname "role" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the election id
*/}}
{{- define "eric-mxe-ingress-controller.election-id" -}}
{{- $fullname := include "eric-mxe-ingress-controller.name" . -}}
{{- $defaultname := printf "%s-%s" $fullname "leader" -}}
{{- default $defaultname .Values.electionIdOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the class
*/}}
{{- define "eric-mxe-ingress-controller.class" -}}
{{- $name := default "eric-mxe-ingress-controller-class" .Values.nameOverride -}}
{{- printf "%s" $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the Controller Class
*/}}
{{- define "eric-mxe-ingress-controller.controllerclass" -}}
{{- $class := include "eric-mxe-ingress-controller.class" . -}}
{{- printf "%s/%s" "k8s.io" $class | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of clusterrole binding
*/}}
{{- define "eric-mxe-ingress-controller.clusterrolebinding" -}}
{{- $fullname := include "eric-mxe-ingress-controller.name" . -}}
{{- printf "%s-%s" $fullname "clusterrolebinding" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of role binding
*/}}
{{- define "eric-mxe-ingress-controller.rolebinding" -}}
{{- $fullname := include "eric-mxe-ingress-controller.name" . -}}
{{- printf "%s-%s" $fullname "rolebinding" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-mxe-ingress-controller.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create annotation prefix for Ingress.
*/}}
{{- define "eric-mxe-ingress-controller.ingress-annotation-prefix" -}}
mxe.nginx.ingress.kubernetes.io
{{- end -}}

{{/*
Image pull secrets DR-D1123-115
*/}}
{{- define "eric-mxe-ingress-controller.image-pull-secrets" -}}
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
{{- define "eric-mxe-ingress-controller.image-repository" -}}
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
{{- define "eric-mxe-ingress-controller.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}

{{/*
Update strategy for deployments
*/}}
{{- define "eric-mxe-ingress-controller.update-strategy" -}}
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
{{- define "eric-mxe-ingress-controller.istio-sidecar-annotations" -}}
sidecar.istio.io/rewriteAppHTTPProbers: "true"
proxy.istio.io/config: |-
  proxyStatsMatcher:
    inclusionRegexps:
      - .*http_local_rate_limit.*
{{ include "eric-mxe-ingress-controller.istio-sidecar-annotations-egress" . }}
{{- end -}}

{{- define "eric-mxe-ingress-controller.istio-sidecar-labels" -}}
sidecar.istio.io/inject: "true"
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts
*/}}
{{- define "eric-mxe-ingress-controller.istio-sidecar-annotations-egress" -}}
{{- if .Values.global.serviceMesh.enabled -}}
{{ include "eric-mxe-ingress-controller.istio-sidecar-annotations-egress-mounts" . }}
{{- end -}}
{{- end -}}

{{/*
Expand the template of the gatekeeper login page
*/}}
{{- define "eric-mxe-ingress-controller.pdb" -}}
{{- $name := include "eric-mxe-ingress-controller.name" . -}}
{{- printf "%s-%s" $name "pdb" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
ReplicaCount of the chart.
*/}}
{{- define "eric-mxe-ingress-controller.replicaCount" -}}
{{- if .Values.replicaCount -}}
{{ .Values.replicaCount }}
{{- else -}}
1
{{- end -}}
{{- end -}}

{{/*
podDisruptionBudget.minAvailable of the chart.
*/}}
{{- define "eric-mxe-ingress-controller.podDisruptionBudget.minAvailable" -}}
{{ .Values.podDisruptionBudget.minAvailable }}
{{- end -}}

{{/*
   Kubernetes Availability and Resiliency Design Rule DR-D1120-060-AD
*/}}
{{- define "eric-mxe-ingress-controller.tolerations" -}}
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
adding TopologySpreadConstraints
*/}}
{{- define "eric-mxe-ingress-controller.topologySpreadConstraints" }}
{{- if .Values.topologySpreadConstraints }}
{{- range $config, $values := .Values.topologySpreadConstraints }}
- topologyKey: {{ $values.topologyKey }}
  maxSkew: {{ $values.maxSkew | default 1 }}
  whenUnsatisfiable: {{ $values.whenUnsatisfiable | default "ScheduleAnyway" }}
{{- end }}
{{- end }}
{{- end }}

{{- define "eric-mxe-ingress-controller.podPriority" -}}
{{- if .Values.podPriority -}}
  {{- if .Values.podPriority.mxeIngressController -}}
    {{- .Values.podPriority.mxeIngressController.priorityClassName | toString -}}
  {{- end -}}
{{- end -}}
{{- end -}}
{{/*
Istio sidecar annotations - egress volume mounts
*/}}
{{- define "eric-mxe-ingress-controller.istio-sidecar-annotations-egress-mounts" -}}
{{- if .Values.global.serviceMesh.enabled -}}
sidecar.istio.io/userVolume: {{ include "eric-mxe-ingress-controller.istio-sidecar-annotations-user-volume" . | squote}}
sidecar.istio.io/userVolumeMount: {{ include "eric-mxe-ingress-controller.istio-sidecar-annotations-volume-mounts" . | squote}}
{{- end -}}
{{- end -}}


{{/*
Istio sidecar annotations - user volume
*/}}
{{- define "eric-mxe-ingress-controller.istio-sidecar-annotations-user-volume" -}}
{{- printf "{" -}}
{{- include "eric-mxe-ingress-controller.istio-sidecar-annotations-user-volume-egress" . -}},
{{- include "eric-mxe-ingress-controller.istio-sidecar-annotations-user-volume-egress-iam" . -}},
{{- include "eric-mxe-ingress-controller.istio-sidecar-annotations-user-volume-egress-lcm" . -}},
{{- include "eric-mxe-ingress-controller.istio-sidecar-annotations-user-volume-egress-pm" . }}
{{- printf "}" -}}
{{- end }}


{{/*
Istio sidecar annotations - volume mounts - egress
*/}}
{{- define "eric-mxe-ingress-controller.istio-sidecar-annotations-user-volume-egress" -}}
{{- with .Values.global.serviceMesh.egress.ca }}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\"}}" .secretName .genSecretName -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress iam
*/}}
{{- define "eric-mxe-ingress-controller.istio-sidecar-annotations-user-volume-egress-iam" -}}
{{- with .Values.global.serviceMesh.egress.iam }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress pm
*/}}
{{- define "eric-mxe-ingress-controller.istio-sidecar-annotations-user-volume-egress-pm" -}}
{{- with .Values.global.serviceMesh.egress.pmServer }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress lcm
*/}}
{{- define "eric-mxe-ingress-controller.istio-sidecar-annotations-user-volume-egress-lcm" -}}
{{- with .Values.global.serviceMesh.egress.lcmRegistry }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - user volume mounts
*/}}
{{- define "eric-mxe-ingress-controller.istio-sidecar-annotations-volume-mounts" }}
{{- printf "{" -}}
{{- include "eric-mxe-ingress-controller.istio-sidecar-annotations-volume-mounts-egress" . -}},
{{- include "eric-mxe-ingress-controller.istio-sidecar-annotations-volume-mounts-egress-iam" . -}},
{{- include "eric-mxe-ingress-controller.istio-sidecar-annotations-volume-mounts-egress-lcm" . -}},
{{- include "eric-mxe-ingress-controller.istio-sidecar-annotations-volume-mounts-egress-pm" . -}}
{{- printf "}" -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress
*/}}
{{- define "eric-mxe-ingress-controller.istio-sidecar-annotations-volume-mounts-egress" -}}
{{- with .Values.global.serviceMesh.egress.ca }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .caCertsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress iam
*/}}
{{- define "eric-mxe-ingress-controller.istio-sidecar-annotations-volume-mounts-egress-iam" -}}
{{- with .Values.global.serviceMesh.egress.iam }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress lcm
*/}}
{{- define "eric-mxe-ingress-controller.istio-sidecar-annotations-volume-mounts-egress-lcm" -}}
{{- with .Values.global.serviceMesh.egress.lcmRegistry }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress pm server
*/}}
{{- define "eric-mxe-ingress-controller.istio-sidecar-annotations-volume-mounts-egress-pm" -}}
{{- with .Values.global.serviceMesh.egress.pmServer }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
POD AntiAffinity type (soft/hard)
*/}}
{{- define "eric-mxe-ingress-controller.podAntiAffinityType" -}}
{{- $podantiaffinity := "soft" }}
{{- if hasKey .Values "affinity" }}
  {{- $podantiaffinity = .Values.affinity.podAntiAffinity }}
{{- end }}
{{- if eq $podantiaffinity "hard" }}
  requiredDuringSchedulingIgnoredDuringExecution:
  - labelSelector:
      matchExpressions:
      - key: "app.kubernetes.io/part-of"
        operator: "In"
        values:
          - "mxe"
      - key: "app.kubernetes.io/component"
        operator: "In"
        values:
          - "ingress-controller"
      - key: "app.kubernetes.io/instance"
        operator: "In"
        values:
          - "{{ .Release.Name }}"
    topologyKey: kubernetes.io/hostname
{{- else if eq $podantiaffinity  "soft" }}
  preferredDuringSchedulingIgnoredDuringExecution:
    - weight: 50
      podAffinityTerm:
        topologyKey: "kubernetes.io/hostname"
        labelSelector:
          matchExpressions:
            - key: "app.kubernetes.io/part-of"
              operator: "In"
              values:
                - "mxe"
            - key: "app.kubernetes.io/component"
              operator: "In"
              values:
                - "ingress-controller"
            - key: "app.kubernetes.io/instance"
              operator: "In"
              values:
                - "{{ .Release.Name }}"
    - weight: 25
      podAffinityTerm:
        topologyKey: "kubernetes.io/hostname"
        labelSelector:
          matchExpressions:
            - key: "app.kubernetes.io/part-of"
              operator: "In"
              values:
                - "mxe"
            - key: "app.kubernetes.io/component"
              operator: "In"
              values:
                - "ingress-controller"
            - key: "app.kubernetes.io/instance"
              operator: "NotIn"
              values:
                - "{{ .Release.Name }}"
{{- end -}}
{{- end -}}

{{/*
Standard labels
*/}}
{{- define "eric-mxe-ingress-controller.standard-labels" -}}
app.kubernetes.io/part-of: "mxe"
app.kubernetes.io/instance: {{ .Release.Name | quote }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
helm.sh/chart: {{ include "eric-mxe-ingress-controller.chart" . }}
{{- end -}}

{{/*
Create a user defined label
*/}}
{{ define "eric-mxe-ingress-controller.config-labels" }}
  {{- $global := (.Values.global).labels -}}
  {{- $service := .Values.labels -}}
  {{- include "eric-mxe-ingress-controller.mergeLabels" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged labels for Default, which includes Standard and Config
*/}}
{{- define "eric-mxe-ingress-controller.labels" -}}
  {{- $standard := include "eric-mxe-ingress-controller.standard-labels" . | fromYaml -}}
  {{- $config := include "eric-mxe-ingress-controller.config-labels" . | fromYaml -}}
  {{- include "eric-mxe-ingress-controller.mergeLabels" (dict "location" .Template.Name "sources" (list $standard $config)) | trim }}
{{- end -}}

{{/*
Create a user defined annotation
*/}}
{{ define "eric-mxe-ingress-controller.config-annotations" }}
  {{- $global := (.Values.global).annotations -}}
  {{- $service := .Values.annotations -}}
  {{- include "eric-mxe-ingress-controller.mergeAnnotations" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged annotations for Default, which includes productInfo and config
*/}}
{{- define "eric-mxe-ingress-controller.annotations" -}}
  {{- $productInfo := include "eric-mxe-ingress-controller.product-info" . | fromYaml -}}
  {{- $config := include "eric-mxe-ingress-controller.config-annotations" . | fromYaml -}}
  {{- include "eric-mxe-ingress-controller.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $config)) | trim }}
{{- end -}}

{{/*
Security annotations
*/}}
{{- define "eric-mxe-ingress-controller.security-annotations" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{/*
Create a map from ".Values.global" with defaults if missing in values file.
This hides defaults from values file.
*/}}
{{ define "eric-mxe-ingress-controller.global" }}
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
{{ define "eric-mxe-ingress-controller.nodeSelector" }}
{{- $g := fromJson (include "eric-mxe-ingress-controller.global" .) -}}
{{- $global := $g.nodeSelector -}}
{{- $service := .Values.nodeSelector -}}
{{- include "eric-mxe-ingress-controller.aggregatedMerge" (dict "context" "nodeSelector" "location" .Template.Name "sources" (list $global $service)) -}}
{{ end }}

{{/*
Added below to handle DR-D1121-102 for globalRegistryPullPolicy Shall be Used
*/}}
{{- define "eric-mxe-ingress-controller.registryImagePullPolicy" -}}
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

{{- define "eric-mxe-ingress-controller.ericProdInfoRepoPath" }}
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

{{- define "eric-mxe-ingress-controller.ericProdInfoRegistry" }}
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
eric-mxe-ingress-controller image name path
*/}}
{{- define "eric-mxe-ingress-controller.setImageRepo" }}
  {{- $top := index . 0 }}
  {{- $serviceLevelImageRegistry := index . 1 }}
  {{- $serviceLevelImageRepoPath := index . 2 }}
  {{- $imageName := index . 3 }}

  {{- /*
  Registry and Repopath for the specified image container is fetched from eric-product-info.yaml
  */}}
  {{- $productInfo := fromYaml ($top.Files.Get "eric-product-info.yaml") -}}
  {{- $registryUrl := include "eric-mxe-ingress-controller.ericProdInfoRegistry" (list $productInfo $imageName) -}}
  {{- $imageRepoPath := include "eric-mxe-ingress-controller.ericProdInfoRepoPath" (list $productInfo $imageName) -}}

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

{{- define "eric-mxe-ingress-controller.hook-weight" }}
{{- $val := . }}
helm.sh/hook-weight: {{ quote $val }}
{{- end -}}

{{/*
Prometheus annotations
*/}}
{{- define "eric-mxe-ingress-controller.prometheus-annotations" -}}
prometheus.io/path: {{ .Values.global.mxePrometheusPath | quote }}
prometheus.io/port: {{ .Values.global.mxePrometheusPort | quote }}
prometheus.io/scrape: {{ .Values.global.mxePrometheusScrape | quote }}
prometheus.io/scrape-role: {{ .Values.global.mxePrometheusScrapeRole | quote }}
prometheus.io/scrape-interval: {{ .Values.global.mxePrometheusScrapeInterval | quote }}
{{- end -}}
{{/*
Merge global tolerations with service tolerations (DR-D1120-061-AD).
*/}}
{{- define "eric-mxe-ingress-controller.merge-tolerations" -}}
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
        {{- $globalItemId := include "eric-mxe-ingress-controller.merge-tolerations.get-identifier" $globalItem -}}
        {{- range $serviceItem := $serviceTolerations -}}
          {{- $serviceItemId := include "eric-mxe-ingress-controller.merge-tolerations.get-identifier" $serviceItem -}}
          {{- if eq $serviceItemId $globalItemId -}}
            {{- $matchingItems = append $matchingItems $serviceItem -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
      {{- range $globalItem := $globalTolerations -}}
        {{- $globalItemId := include "eric-mxe-ingress-controller.merge-tolerations.get-identifier" $globalItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-ingress-controller.merge-tolerations.get-identifier" $matchItem -}}
          {{- if eq $matchItemId $globalItemId -}}
            {{- $matchCount = add1 $matchCount -}}
          {{- end -}}
        {{- end -}}
        {{- if eq $matchCount 0 -}}
          {{- $nonMatchingItems = append $nonMatchingItems $globalItem -}}
        {{- end -}}
      {{- end -}}
      {{- range $serviceItem := $serviceTolerations -}}
        {{- $serviceItemId := include "eric-mxe-ingress-controller.merge-tolerations.get-identifier" $serviceItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-ingress-controller.merge-tolerations.get-identifier" $matchItem -}}
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
{{ define "eric-mxe-ingress-controller.merge-tolerations.get-identifier" }}
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
ingress-controller - postgres certificate name
*/}}
{{- define "eric-mxe-ingress-controller.pg-client-cert" -}}
{{ .Values.global.serviceMesh.egress.postgres.clientCertificate }}
{{- end -}}

{{/*
ingress-controller - postgres certificate private key
*/}}
{{- define "eric-mxe-ingress-controller.pg-private-key" -}}
{{ .Values.global.serviceMesh.egress.postgres.privateKey }}
{{- end -}}

{{/*
ingress-controller - postgres certificate ca
*/}}
{{- define "eric-mxe-ingress-controller.pg-ca-issuer" -}}
{{ .Values.global.serviceMesh.egress.postgres.caIssuer }}
{{- end -}}

{{/*
pg-cert - volume mounts
*/}}
{{- define "eric-mxe-ingress-controller.pg-cert-volume-mounts" -}}
{{- if .Values.global.security.tls.enabled }}
volumeMounts:
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
{{- define "eric-mxe-ingress-controller.pg-cert-volumes" -}}
{{- if .Values.global.security.tls.enabled }}
volumes:
- name: trusted-ca
  secret:
    secretName: eric-sec-sip-tls-trusted-root-cert
- name: pg-cert
  secret:
    secretName: {{ include "eric-mxe-ingress-controller.name" . }}-pg-cert
{{- end -}}
{{- end -}}
