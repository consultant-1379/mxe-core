{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the secret.
Should not be changed as subcharts reference this by value.
*/}}
{{- define "mxe.pg-secret-name" -}}
{{- default "mxe-db-password" .Values.secret.database.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}
*/}}

{{- define "mxe.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "mxe.argoexec-build-job" -}}
{{- default "argoexec-build-job" .Values.argoexecBuild.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
The secret name of the docker registry
*/}}
{{- define "mxe.docker-registry-secret-name" -}}
{{- if .Values.dockerRegistry.secretNameOverride -}}
{{- .Values.dockerRegistry.secretNameOverride -}}
{{- else -}}
{{ include "mxe.name" . }}
{{- end -}}
{{- end -}}

{{/*
The full host of the docker registry
*/}}
{{- define "mxe.docker-registry-full-host" -}}
{{- .Values.dockerRegistry.externalHostname -}}:{{- .Values.dockerRegistry.externalPort -}}
{{- end -}}

{{/*
The full host of the docker registry
*/}}
{{- define "mxe.docker-registry-config" -}}
{{- $registryName := (include "mxe.docker-registry-full-host" .) -}}
{{- with .Values.global.internalRegistry }}
{{- printf "{\"auths\":{\"%s\":{\"username\":\"%s\",\"password\":\"%s\",\"auth\":\"%s\"}}}" $registryName .username .password (printf "%s:%s" .username .password | b64enc) | b64enc }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "mxe.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}


{{/*
Product information of Ericsson products (DR-D1121-064, DR-D1121-067)
*/}}
{{- define "mxe.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}

{{/*
Image pull secrets
*/}}
{{- define "mxe.argoexec-build.image-pull-secrets" -}}
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
Image registry
*/}}
{{- define "mxe.image-registry" -}}
{{- if .Values.imageCredentials.registry.url -}}
{{- .Values.imageCredentials.registry.url -}}
{{- else -}}
{{- .Values.global.registry.url -}}
{{- end -}}
/
{{- end -}}

{{/*
Image repository
*/}}
{{- define "mxe.image-repository" -}}
{{- include "mxe.image-registry" . -}}
{{- printf "%s" .Values.imageCredentials.argoexecBuild.repoPath -}}
/
{{- end -}}

{{/*
The host of the docker registry
*/}}
{{- define "mxe.docker-registry-hostname" -}}
{{- if .Values.dockerRegistry.fullHostOverride -}}
{{- .Values.dockerRegistry.fullHostOverride -}}
{{- if .Values.dockerRegistry.portOverride -}}
:{{- .Values.dockerRegistry.portOverride -}}
{{- end -}}
{{- else -}}
{{- if .Values.dockerRegistry.nameOverride -}}
{{- .Values.dockerRegistry.nameOverride -}}
{{- else -}}
eric-lcm-container-registry-registry
{{- end -}}
.{{- .Release.Namespace -}}.svc.{{- .Values.global.mxeClusterDomain -}}
{{- if .Values.dockerRegistry.portOverride -}}
:{{- .Values.dockerRegistry.portOverride -}}
{{- else if .Values.dockerRegistry.ingressEnabled -}}
:5000
{{- end -}}
{{- end -}}
{{- end -}}

{/*
Image pull secret name
*/}}
{{- define "mxe.image-pull-secret-name" -}}
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
Define the name of the credentials secret for internal container registry
*/}}
{{- define "mxe.internal-container-registry-secret-name" -}}
{{- printf "%s" "mxe-internal-registry-creds" -}}
{{- end -}}

{{/*
Istio resources - Keycloak service name based url for issuer
*/}}
{{- define "mxe.keycloak-service-path" -}}
{{- if .Values.global.serviceMesh.enabled -}}
http://eric-sec-access-mgmt-http:8443
{{- else -}}
http://eric-sec-access-mgmt-http:8080
{{- end -}}
{{- end -}}

{{/*
Istio resources - Keycloak host name based url for issuer
*/}}
{{- define "mxe.api-host-path" -}}
{{- printf "https://%s" .Values.apiHostname -}}
{{- end -}}

{{/*
Istio resources - Request Authentication rules
*/}}
{{- define "mxe.istio-req-authn-jwt-rules" -}}
jwtRules:
- issuer: "{{ include "mxe.keycloak-service-path" .}}/auth/realms/mxe"
  jwksUri: "{{ include "mxe.api-host-path" .}}/auth/realms/mxe/protocol/openid-connect/certs"
  forwardOriginalToken: true
- issuer: "{{ include "mxe.keycloak-service-path" .}}/auth/realms/mxe"
  jwksUri: "{{ include "mxe.api-host-path" .}}/auth/realms/master/protocol/openid-connect/certs"
  forwardOriginalToken: true
- issuer: "{{ include "mxe.api-host-path" .}}/auth/realms/mxe"
  jwksUri: "{{ include "mxe.api-host-path" .}}/auth/realms/mxe/protocol/openid-connect/certs"
  forwardOriginalToken: true
- issuer: "{{ include "mxe.api-host-path" .}}/auth/realms/master"
  jwksUri: "{{ include "mxe.api-host-path" .}}/auth/realms/master/protocol/openid-connect/certs"
  forwardOriginalToken: true
{{- end -}}

{{/*
Define the name of the dockerfile for the model packaging job
*/}}
{{- define "mxe.docker-configmap" -}}
{{- printf "%s" "mxe-workflow-argoexec-docker-configmap" | trunc 63 | trimSuffix "-" -}}
{{- end -}}


{{/*
Istio sidecar annotations
*/}}
{{- define "mxe.istio-sidecar-annotations" -}}
sidecar.istio.io/rewriteAppHTTPProbers: {{ .Values.global.serviceMesh.enabled | quote }}
{{ include "mxe.istio-sidecar-annotations-egress" . }}
{{- end -}}

{{/*
Istio sidecar labels
*/}}
{{- define "mxe.istio-sidecar-labels" -}}
sidecar.istio.io/inject: {{ .Values.global.serviceMesh.enabled | quote }}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts
*/}}
{{- define "mxe.istio-sidecar-annotations-egress" -}}
{{- if .Values.global.serviceMesh.enabled -}}
{{ include "mxe.istio-sidecar-annotations-egress-mounts" . }}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - egress volume mounts
*/}}
{{- define "mxe.istio-sidecar-annotations-egress-mounts" -}}
{{- if .Values.global.serviceMesh.enabled -}}
sidecar.istio.io/userVolume: {{ include "mxe.istio-sidecar-annotations-user-volume" . | squote }}
sidecar.istio.io/userVolumeMount: {{ include "mxe.istio-sidecar-annotations-volume-mounts" . | squote }}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - user volume
*/}}
{{- define "mxe.istio-sidecar-annotations-user-volume" -}}
{{- printf "{" }}
{{- include "mxe.istio-sidecar-annotations-user-volume-egress" . -}},
{{- include "mxe.istio-sidecar-annotations-user-volume-egress-iam" . -}},
{{- include "mxe.istio-sidecar-annotations-user-volume-egress-lcm" . -}},
{{- include "mxe.istio-sidecar-annotations-user-volume-egress-pm" . }}
{{- printf "}" -}}
{{- end }}


{{/*
Istio sidecar annotations - volume mounts - egress
*/}}
{{- define "mxe.istio-sidecar-annotations-user-volume-egress" -}}
{{- with .Values.global.serviceMesh.egress.ca }}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\"}}" .secretName .genSecretName -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress iam
*/}}
{{- define "mxe.istio-sidecar-annotations-user-volume-egress-iam" -}}
{{- with .Values.global.serviceMesh.egress.iam }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress pm
*/}}
{{- define "mxe.istio-sidecar-annotations-user-volume-egress-pm" -}}
{{- with .Values.global.serviceMesh.egress.pmServer }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress lcm
*/}}
{{- define "mxe.istio-sidecar-annotations-user-volume-egress-lcm" -}}
{{- with .Values.global.serviceMesh.egress.lcmRegistry }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - user volume mounts
*/}}
{{- define "mxe.istio-sidecar-annotations-volume-mounts" }}
{{- printf "{" -}}
{{- include "mxe.istio-sidecar-annotations-volume-mounts-egress" . -}},
{{- include "mxe.istio-sidecar-annotations-volume-mounts-egress-iam" . -}},
{{- include "mxe.istio-sidecar-annotations-volume-mounts-egress-lcm" . -}},
{{- include "mxe.istio-sidecar-annotations-volume-mounts-egress-pm" . -}}
{{- printf "}" -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress
*/}}
{{- define "mxe.istio-sidecar-annotations-volume-mounts-egress" -}}
{{- with .Values.global.serviceMesh.egress.ca }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .caCertsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress iam
*/}}
{{- define "mxe.istio-sidecar-annotations-volume-mounts-egress-iam" -}}
{{- with .Values.global.serviceMesh.egress.iam }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress lcm
*/}}
{{- define "mxe.istio-sidecar-annotations-volume-mounts-egress-lcm" -}}
{{- with .Values.global.serviceMesh.egress.lcmRegistry }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress pm server
*/}}
{{- define "mxe.istio-sidecar-annotations-volume-mounts-egress-pm" -}}
{{- with .Values.global.serviceMesh.egress.pmServer }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Add Installer Docker Registry CA - Env Variable
*/}}
{{- define "mxe.argoexec-build-job.addinstallerdockerregistryca-env" -}}
{{- if .Values.argoexecBuild.installerDockerRegistry.caSecretName -}}
- name: ADD_EXTERNAL_INSTALLER_DOCKER_REGISTRY_CA
  value: "true"
{{- end -}}
{{- end -}}

{{/*
Add Installer Docker Registry CA - volume mounts to argoexec Job
*/}}
{{- define "mxe.argoexec-build-job.addinstallerdockerregistryca-volume-mounts" -}}
{{- if .Values.argoexecBuild.installerDockerRegistry.caSecretName -}}
- mountPath: /mnt/trustedregistry
  name: installer-docker-ca-cert
{{- end -}}
{{- end -}}

{{/*
Add Installer Docker Registry CA - volumes
*/}}
{{- define "mxe.argoexec-build-job.addinstallerdockerregistryca-volumes" -}}
{{- if .Values.argoexecBuild.installerDockerRegistry.caSecretName -}}
- name: installer-docker-ca-cert
  secret:
    defaultMode: 420
    secretName: {{ .Values.argoexecBuild.installerDockerRegistry.caSecretName }}
{{- end -}}
{{- end -}}

{{/*
AppArmor annotations
*/}}
{{- define "mxe.security-annotations" -}}
{{- if .Values.appArmorProfile.type -}}
{{- $containerName := include  "mxe.argoexec-build-job" . -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" $containerName }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{/*
Security Context - argoexec-build-job
*/}}
{{- define "mxe.argoexec-build-job-security-context" -}}
allowPrivilegeEscalation: false
runAsUser: 0
capabilities:
  add:
    - chown
    - dac_override
    - fowner
    - setgid
    - setuid
    - net_bind_service
  drop:
    - all
{{ if .Values.seccompProfile.type -}}
seccompProfile:
  type: {{ .Values.seccompProfile.type }}
  {{ if eq .Values.seccompProfile.type "Localhost" -}}
  localhostProfile: {{ .Values.seccompProfile.localhostProfile }}
  {{- end -}}
{{- end -}}
{{- end -}}