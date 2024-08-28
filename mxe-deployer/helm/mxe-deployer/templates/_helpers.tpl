
{{/*
Product information of Ericsson products (DR-D1121-064, DR-D1121-067)
*/}}
{{- define "mxe-deployer.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}

{{/*
Expand the name of the chart.
*/}}
{{- define "mxe-deployer.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "mxe-deployer.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
   Kubernetes Availability and Resiliency Design Rule DR-D1120-060-AD
*/}}
{{- define "mxe-deployer.tolerations" -}}
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
Expand the name of the chart. and create name for imagepull secret merge job's resources
*/}}
{{- define "mxe-deployer.post-install-hook-certificate-sidecar-name" -}}
{{- default "post-install-certificate-sidecar" .Values.mxePostInstallCertificateSideCar.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Image pull secrets
*/}}
{{- define "mxe-deployer.post-install-hook-certificate-sidecar-merge-pull-secrets" -}}
{{- if .Values.global.registry.pullSecret -}}
imagePullSecrets:
  - name: {{ .Values.global.registry.pullSecret | quote -}}
{{- end -}}
{{- end -}}

{{/*
Image pull secrets
*/}}
{{- define "mxe-deployer.post-install-hook-certificate-externalcertauth-pull-secrets" -}}
{{- if .Values.global.registry.pullSecret -}}
imagePullSecrets:
  - name: {{ .Values.global.registry.pullSecret | quote -}}
{{- end -}}
{{- end -}}

{{/*
Image pull secret name
*/}}
{{- define "mxe-deployer.post-install-hook-certificate-sidecar-merge-pull-secrets-name" -}}
{{- if .Values.global.registry.pullSecret -}}
{{ .Values.global.registry.pullSecret | quote -}}
{{- end -}}
{{- end -}}

{{/*
Image registry
*/}}
{{- define "mxe-deployer.post-install-hook-certificate-sidecar.image-registry" -}}
{{- .Values.global.registry.url -}}
{{- end -}}

{{/*
Image repository
*/}}
{{- define "mxe-deployer.post-install-hook-certificate-sidecar.image-repository" -}}
{{- .Values.global.registry.url -}}
{{- printf "/%s/" .Values.mxePostInstallCertificateSideCar.image.repoPath -}}
{{- end -}}

{{/*
Image repository
*/}}
{{- define "mxe-deployer.post-install-hook-certificate-externalcertauth-repository" -}}
{{- .Values.global.registry.url -}}
{{- printf "/%s/" .Values.mxePostInstallExtCertificate.image.repoPath -}}
{{- end -}}

{{/*
Config name to be used for certificate-sidecar script.
*/}}
{{- define "mxe-deployer.post-install-hook-certificate-sidecar-script-name" -}}
{{- $name := include "mxe-deployer.post-install-hook-certificate-sidecar-name" . -}}
{{- printf "%s-%s" $name "script" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Config name to be used for certificate-sidecar script.
*/}}
{{- define "mxe-deployer.post-install-hook-certificate-sidecar-patch-template-name" -}}
{{- $name := include "mxe-deployer.post-install-hook-certificate-sidecar-name" . -}}
{{- printf "%s-%s" $name "patch-template" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Gitea service name
*/}}
{{- define "mxe-deployer.gitea-service-name" -}}
{{- default "gitea" (index .Values "gitea" "fullname") -}}
{{- end -}}

{{/*
Define the name of the dockerconfig secret for internal container registry
*/}}
{{- define "mxe-deployer.internal-container-registry-dockerconfig-secret-name" -}}
{{- printf "%s" "mxe-internal-registry-dockerconfig-secret" -}}
{{- end -}}

{{/*
 MXE Ingress Gateway - name
*/}}
{{- define "mxe-deployer.mesh-ingress-gw-name" -}}
mxe-commons-ingress-gw
{{- end -}}

{{- define "mxe-deployer.security-annotations-certificate-sidecar" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" "post-install-hook-certificate-sidecar" }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{/*
Security Context 
*/}}
{{- define "mxe-deployer.post-install-hook-certificate-sidecar-security-context" -}}
allowPrivilegeEscalation: false
capabilities:
  drop:
    - ALL
privileged: false
runAsNonRoot: true
seccompProfile:
  type:
    {{- if .Values.seccompProfile.type -}}
      {{ $seccompProfile := ternary (printf "%s/%s" .Values.seccompProfile.type .Values.seccompProfile.localhostProfile) .Values.seccompProfile.type (eq .Values.seccompProfile.type "localhost") -}}
      {{ printf " "}}{{ $seccompProfile }}
    {{- end -}}
{{- end -}}

{{- define "mxe-deployer.post-install-hook.registryImagePullPolicy" -}}
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