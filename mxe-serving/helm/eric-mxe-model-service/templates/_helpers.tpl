{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-mxe-model-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*

*/}}
{{- define "eric-mxe-model-service.seldon-metrics-svc-name" -}}
{{- $name := include "eric-mxe-model-service.name" . -}}
{{- printf "%s-%s" $name "pm-podmonitor" | trunc 63 -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-mxe-model-service.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create default backend name.
*/}}
{{- define "eric-mxe-model-service.defaultbackend" -}}
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
{{- define "eric-mxe-model-service.ingress-annotation-prefix" -}}
{{- if .Values.ingress.annotationPrefix -}}
{{- .Values.ingress.annotationPrefix -}}
{{- else -}}
mxe.nginx.ingress.kubernetes.io
{{- end -}}
{{- end -}}

{{- define "eric-mxe-model-service.ingress-annotations" -}}
{{ include "eric-mxe-model-service.ingress-annotation-prefix" . }}/default-backend: {{ include "eric-mxe-model-service.defaultbackend" . | quote }}
{{- if .Values.ingress.owasp.enabled }}
{{ include "eric-mxe-model-service.ingress-annotation-prefix" . }}/enable-modsecurity: "true"
{{ include "eric-mxe-model-service.ingress-annotation-prefix" . }}/modsecurity-snippet: |
  SecRule REQUEST_URI "@beginsWith /v1/model-services/" "id:10003,phase:1,chain"
    SecRule REQUEST_METHOD "DELETE" "nolog,ctl:ruleRemoveById=911100"
  SecRule REQUEST_URI "@beginsWith /v1/model-services/" "id:10006,phase:1,chain"
    SecRule REQUEST_METHOD "PATCH" "nolog,ctl:ruleRemoveById=911100"
  SecRule REQUEST_URI "@beginsWith /v2/model-services/" "id:10203,phase:1,chain"
    SecRule REQUEST_METHOD "DELETE" "nolog,ctl:ruleRemoveById=911100"
  SecRule REQUEST_URI "@beginsWith /v2/model-services/" "id:10206,phase:1,chain"
    SecRule REQUEST_METHOD "PATCH" "nolog,ctl:ruleRemoveById=911100"
  Include /etc/nginx/owasp-modsecurity-crs/nginx-modsecurity.conf
  SecRuleEngine On
{{- end }}
{{ include "eric-mxe-model-service.ingress-annotation-prefix" . }}/service-upstream: "true"
{{ include "eric-mxe-model-service.ingress-annotation-prefix" . }}/upstream-vhost: {{ include "eric-mxe-model-service.name" . | quote }}
{{- end -}}

{{/*
Expand the service account of the chart.
*/}}
{{- define "eric-mxe-model-service.serviceaccount" -}}
{{- $name := include "eric-mxe-model-service.name" . -}}
{{- printf "%s-%s" $name "serviceaccount" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the role binding.
*/}}
{{- define "eric-mxe-model-service.rolebinding" -}}
{{- $name := include "eric-mxe-model-service.name" . -}}
{{- printf "%s-%s" $name "rolebinding" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the role.
*/}}
{{- define "eric-mxe-model-service.role" -}}
{{- $name := include "eric-mxe-model-service.name" . -}}
{{- printf "%s-%s" $name "role" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Image pull secrets
*/}}
{{- define "eric-mxe-model-service.image-pull-secrets" -}}
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
Add External CA - Args command
*/}}
{{- define "eric-mxe-model-service.add-externalca-command" -}}
{{- if .Values.isExtCA -}}
mkdir -p /cacerts/pem;
cp -r /etc/ssl/certs/*.* /cacerts/pem/;
touch /cacerts/ca-bundle.pem;
for pem in `ls /cacerts/pem/*.pem`; do cat $pem >> /cacerts/ca-bundle.pem; done;
cp /usr/share/pki/trust/anchors/extca.crt /cacerts/pem/rootca.pem;
cat /usr/share/pki/trust/anchors/extca.crt >>  /cacerts/ca-bundle.pem;
cd /cacerts/pem; hashkey=`openssl x509 -in rootca.pem -noout -hash`;
if [ ! -L $hashkey.0 ]; then ln -s rootca.pem $hashkey.0; fi;
{{- end -}}
{{- end -}}

{{/*
Add External CA - volume mounts
*/}}
{{- define "eric-mxe-model-service.add-externalca-volume-mounts" -}}
{{- if .Values.isExtCA -}}
- mountPath: /usr/share/pki/trust/anchors/extca.crt
  name: iam-ca-cert
  subPath: ca.crt
{{- end -}}   
{{- end -}}

{{/*
Add External CA - volumes
*/}}
{{- define "eric-mxe-model-service.add-externalca-volumes" -}}
{{- if .Values.isExtCA -}}
- name: iam-ca-cert
  secret:
    defaultMode: 420
    secretName: iam-ca-cert
{{- end -}}
{{- end -}}

{{/*
Image repository
*/}}
{{- define "eric-mxe-model-service.image-repository" -}}
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
{{- define "eric-mxe-model-service.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}

{{/*
Update strategy for deployments
*/}}
{{- define "eric-mxe-model-service.update-strategy" -}}
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
{{- define "eric-mxe-model-service.ingress-tls" -}}
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
The service name of model catalogue service
*/}}
{{- define "eric-mxe-model-service.model-catalogue-service-name" -}}
{{- if .Values.modelCatalogueService.serviceNameOverride -}}
{{- .Values.modelCatalogueService.serviceNameOverride -}}
{{- else -}}
{{- $name := "eric-mxe-model-catalogue-service" -}}
{{- printf "%s" $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
The name of the Seldon Engine's service account
*/}}
{{- define "eric-mxe-model-service.seldon-service-account-name" -}}
{{- if .Values.seldon.engine.serviceAccount.nameOverride -}}
{{- .Values.seldon.engine.serviceAccount.nameOverride -}}
{{- else -}}
eric-mxe-seldon-service-account-engine
{{- end -}}
{{- end -}}

{{/*
The secret name of the docker registry
*/}}
{{- define "eric-mxe-model-service.docker-registry-secret-name" -}}
{{- if .Values.dockerRegistry.secretNameOverride -}}
{{- .Values.dockerRegistry.secretNameOverride -}}
{{- else -}}
{{ include "eric-mxe-model-service.image-pull-secrets" . }}
{{- end -}}
{{- end -}}

{{/*
The full host of the docker registry
*/}}
{{- define "eric-mxe-model-service.docker-registry-full-host" -}}
{{- .Values.dockerRegistry.externalHostname -}}
{{- end -}}


{{/*
The full host of the docker registry
*/}}
{{- define "eric-mxe-model-service.seldon-crd-name" -}}
{{- if .Values.seldon.crd.nameOverride -}}
{{- .Values.seldon.crd.nameOverride -}}
{{- else -}}
SeldonDeployment
{{- end -}}
{{- end -}}


{{/*
Expand the name of the configmap of waiting for keycloak.
*/}}
{{- define "eric-mxe-model-service.wait-for-keycloak-configmap" -}}
{{- $name := include "eric-mxe-model-service.name" . -}}
{{- printf "%s-%s" $name "wait-for-keycloak-configmap" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Istio sidecar annotations, labels
*/}}
{{- define "eric-mxe-model-service.istio-sidecar-annotations" -}}
sidecar.istio.io/rewriteAppHTTPProbers: {{ .Values.global.serviceMesh.enabled | quote }}
{{- end -}}

{{- define "eric-mxe-model-service.istio-sidecar-labels" -}}
sidecar.istio.io/inject: {{ .Values.global.serviceMesh.enabled | quote }}
{{- end -}}

{{/*
Prometheus annotations
*/}}
{{- define "eric-mxe-model-service.prometheus-annotations" -}}
prometheus.io/path: {{ .Values.global.mxePrometheusPath | quote }}
prometheus.io/port: {{ .Values.global.mxePrometheusPort | quote }}
prometheus.io/scrape: {{ .Values.global.mxePrometheusScrape | quote }}
prometheus.io/scrape-role: {{ .Values.global.mxePrometheusScrapeRole | quote }}
prometheus.io/scrape-interval: {{ .Values.global.mxePrometheusScrapeInterval | quote }}
{{- end -}}

{{/*
Deployer Service - Oauth Mxe Api host path url
*/}}
{{- define "eric-mxe-model-service.oauth-api-host-path-url" -}}
{{- if eq (.Values.global.mxeApiport | int64) 443 -}}
{{- printf "https://%s" .Values.global.mxeOauthApiHostname -}}
{{- else -}}
{{- printf "https://%s:%v" .Values.global.mxeOauthApiHostname .Values.global.mxeApiport -}}
{{- end -}}
{{- end -}}

{{/*
Deployer Service - hostname
*/}}
{{- define "eric-mxe-model-service.dm-host-name" -}}
eric-mxe-deployer-service.{{- .Values.deployerService.namespace -}}.svc:80
{{- end -}}

{{/*
Deployer Service - protocol
*/}}
{{- define "eric-mxe-model-service.dm-protocol" -}}
http
{{- end -}}


{{/*
model-service - secret containing root ca for oauth hostname 
*/}}
{{- define "eric-mxe-model-service.iam-ca-cert-name" -}}
{{- .Values.global.mxeIamCaSecretName -}}
{{- end -}}

{{/*
Define the name of the configmap for storing logcontrol.json file
*/}}
{{- define "eric-mxe-model-service.log-control-configmap" -}}
{{- .Values.global.mxeLogControlConfigMap -}}
{{- end -}}

{{- define "eric-mxe-model-service.podPriority" -}}
{{- if .Values.podPriority -}}
  {{- if .Values.podPriority.modelService -}}
    {{- .Values.podPriority.modelService.priorityClassName | toString -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{/*
adding security context
*/}}
{{- define "eric-mxe-model-service.initcontainer-security-context" -}}
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

{{/*
adding security context
*/}}
{{- define "eric-mxe-model-service.usercontainer-security-context" -}}
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

{{/*
Standard labels of Helm and Kubernetes
*/}}
{{- define "eric-mxe-model-service.standard-labels" -}}
app.kubernetes.io/part-of: "mxe"
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
helm.sh/chart: {{ include "eric-mxe-model-service.chart" . | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
{{- end -}}

{{/*
Create a user defined label - [DR-D1121-068] [DR-D1121-060]
*/}}
{{ define "eric-mxe-model-service.config-labels" }}
  {{- $global := (.Values.global).labels -}}
  {{- $service := .Values.labels -}}
  {{- include "eric-mxe-model-service.mergeLabels" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged labels for Default, which includes Standard and Config - Used in all files
*/}}
{{- define "eric-mxe-model-service.labels" -}}
  {{- $standard := include "eric-mxe-model-service.standard-labels" . | fromYaml -}}
  {{- $config := include "eric-mxe-model-service.config-labels" . | fromYaml -}}
  {{- include "eric-mxe-model-service.mergeLabels" (dict "location" .Template.Name "sources" (list $standard $config)) | trim }}
{{- end -}}

{{/*
Create a user defined annotation - [DR-D1121-060]
*/}}
{{ define "eric-mxe-model-service.config-annotations" }}
  {{- $global := (.Values.global).annotations -}}
  {{- $service := .Values.annotations -}}
  {{- include "eric-mxe-model-service.mergeAnnotations" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged annotations for Default, which includes productInfo and config - Used in all files
*/}}
{{- define "eric-mxe-model-service.annotations" -}}
  {{- $productInfo := include "eric-mxe-model-service.product-info" . | fromYaml -}}
  {{- $config := include "eric-mxe-model-service.config-annotations" . | fromYaml -}}
  {{- include "eric-mxe-model-service.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $config)) | trim }}
{{- end -}}

{{/*
Security annotations
*/}}
{{- define "eric-mxe-model-service.security-annotations" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name | trimSuffix "-" }}: {{ $appArmorProfile }}
{{ printf "%s/%s-%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name "kubectl" }}: {{ $appArmorProfile }}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" "init-cacerts" }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{/*
Create a map from ".Values.global" with defaults if missing in values file.
This hides defaults from values file.
*/}}
{{ define "eric-mxe-model-service.global" }}
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
{{ define "eric-mxe-model-service.nodeSelector" }}
{{- $g := fromJson (include "eric-mxe-model-service.global" .) -}}
{{- $global := $g.nodeSelector -}}
{{- $service := .Values.nodeSelector -}}
{{- include "eric-mxe-model-service.aggregatedMerge" (dict "context" "nodeSelector" "location" .Template.Name "sources" (list $global $service)) -}}
{{ end }}

{{/*
Added below to handle DR-D1121-102 for globalRegistryPullPolicy Shall be Used
*/}}
{{- define "eric-mxe-model-service.registryImagePullPolicy" -}}
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
{{- define "eric-mxe-model-service.topologySpreadConstraints" }}
{{- if .Values.topologySpreadConstraints }}
{{- range $config, $values := .Values.topologySpreadConstraints }}
- topologyKey: {{ $values.topologyKey }}
  maxSkew: {{ $values.maxSkew | default 1 }}
  whenUnsatisfiable: {{ $values.whenUnsatisfiable | default "ScheduleAnyway" }}
{{- end }}
{{- end }}
{{- end }}

{{- define "eric-mxe-model-service.ericProdInfoRepoPath" }}
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

{{- define "eric-mxe-model-service.ericProdInfoRegistry" }}
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
eric-mxe-model-service image name path
*/}}
{{- define "eric-mxe-model-service.setImageRepo" }}
  {{- $top := index . 0 }}
  {{- $serviceLevelImageRegistry := index . 1 }}
  {{- $serviceLevelImageRepoPath := index . 2 }}
  {{- $imageName := index . 3 }}

  {{- /*
  Registry and Repopath for the specified image container is fetched from eric-product-info.yaml
  */}}
  {{- $productInfo := fromYaml ($top.Files.Get "eric-product-info.yaml") -}}
  {{- $registryUrl := include "eric-mxe-model-service.ericProdInfoRegistry" (list $productInfo $imageName) -}}
  {{- $imageRepoPath := include "eric-mxe-model-service.ericProdInfoRepoPath" (list $productInfo $imageName) -}}

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
{{- define "eric-mxe-model-service.merge-tolerations" -}}
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
        {{- $globalItemId := include "eric-mxe-model-service.merge-tolerations.get-identifier" $globalItem -}}
        {{- range $serviceItem := $serviceTolerations -}}
          {{- $serviceItemId := include "eric-mxe-model-service.merge-tolerations.get-identifier" $serviceItem -}}
          {{- if eq $serviceItemId $globalItemId -}}
            {{- $matchingItems = append $matchingItems $serviceItem -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
      {{- range $globalItem := $globalTolerations -}}
        {{- $globalItemId := include "eric-mxe-model-service.merge-tolerations.get-identifier" $globalItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-model-service.merge-tolerations.get-identifier" $matchItem -}}
          {{- if eq $matchItemId $globalItemId -}}
            {{- $matchCount = add1 $matchCount -}}
          {{- end -}}
        {{- end -}}
        {{- if eq $matchCount 0 -}}
          {{- $nonMatchingItems = append $nonMatchingItems $globalItem -}}
        {{- end -}}
      {{- end -}}
      {{- range $serviceItem := $serviceTolerations -}}
        {{- $serviceItemId := include "eric-mxe-model-service.merge-tolerations.get-identifier" $serviceItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-model-service.merge-tolerations.get-identifier" $matchItem -}}
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
{{ define "eric-mxe-model-service.merge-tolerations.get-identifier" }}
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
model-service - postgres certificate name, for handling DR-D1123-113
*/}}
{{- define "eric-mxe-model-service.pg-client-cert" -}}
{{ .Values.global.serviceMesh.egress.postgres.clientCertificate }}
{{- end -}}

{{/*
model-service - postgres certificate private key, for handling DR-D1123-113
*/}}
{{- define "eric-mxe-model-service.pg-private-key" -}}
{{ .Values.global.serviceMesh.egress.postgres.privateKey }}
{{- end -}}

{{/*
model-service - postgres certificate ca, for handling DR-D1123-113
*/}}
{{- define "eric-mxe-model-service.pg-ca-issuer" -}}
{{ .Values.global.serviceMesh.egress.postgres.caIssuer }}
{{- end -}}

{{/*
pg-cert - volume mounts, for handling DR-D1123-113
*/}}
{{- define "eric-mxe-model-service.pg-cert-volume-mounts" -}}
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
mxe-model-service -  volumes, for handling DR-D1123-113
*/}}
{{- define "eric-mxe-model-service.pg-cert-volumes" -}}
{{- if .Values.global.security.tls.enabled }}
- name: trusted-ca
  secret:
    secretName: eric-sec-sip-tls-trusted-root-cert
- name: pg-cert
  secret:
    secretName: {{ include "eric-mxe-model-service.name" . }}-pg-cert
{{- end -}}
{{- end -}}
