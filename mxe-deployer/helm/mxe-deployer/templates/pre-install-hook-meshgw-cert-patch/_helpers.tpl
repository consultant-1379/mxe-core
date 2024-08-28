

{{/*
Expand the name of the chart. and create name for imagepull secret merge job's resources
*/}}
{{- define "mxe-deployer.pre-install-hook-meshgw-cert-patch-name" -}}
{{- default "pre-install-meshgw-cert-patch" .Values.mxePreInstallMeshgwCertPatch.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "mxe-deployer.security-annotations-pre-install-hook-meshgw-cert-patch" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" "internal-certificate-patcher" }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{/*
Image pull secrets
*/}}
{{- define "mxe-deployer.pre-install-hook-meshgw-cert-patch-pull-secrets" -}}
{{- if .Values.global.registry.pullSecret -}}
imagePullSecrets:
  - name: {{ .Values.global.registry.pullSecret | quote -}}
{{- end -}}
{{- end -}}

{{/*
Image pull secret name
*/}}
{{- define "mxe-deployer.pre-install-hook-meshgw-cert-patch-pull-secrets-name" -}}
{{- if .Values.global.registry.pullSecret -}}
{{ .Values.global.registry.pullSecret | quote -}}
{{- end -}}
{{- end -}}

{{/*
Image repository
*/}}
{{- define "mxe-deployer.pre-install-hook-meshgw-cert-patch.image-repository" -}}
{{- .Values.global.registry.url -}}
{{- printf "/%s/" .Values.mxePreInstallMeshgwCertPatch.image.repoPath -}}
{{- end -}}

{{/*
Security Context 
*/}}
{{- define "mxe-deployer.pre-install-hook-meshgw-security-context" -}}
allowPrivilegeEscalation: false
capabilities:
  drop:
    - ALL
privileged: false
readOnlyRootFilesystem: true
runAsNonRoot: true
seccompProfile:
  type:
    {{- if .Values.seccompProfile.type -}}
      {{ $seccompProfile := ternary (printf "%s/%s" .Values.seccompProfile.type .Values.seccompProfile.localhostProfile) .Values.seccompProfile.type (eq .Values.seccompProfile.type "localhost") -}}
      {{ printf " "}}{{ $seccompProfile }}
    {{- end -}}
{{- end -}}

{{- define "mxe-deployer.pre-install-hook-meshgw-cert-patch.registryImagePullPolicy" -}}
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