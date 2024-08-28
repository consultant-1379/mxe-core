{{/*
Expand the name of the secret.
Should not be changed as subcharts reference this by value.
*/}}
{{- define "mxe-gitea.pg-secret-name" -}}
{{- default "gitea-db-password" .Values.mxeGitea.secret.database.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "mxe.docker-registry-secret-name" -}}
{{- default "mxe-docker-registry-secret" .Values.secret.registry.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the chart.
*/}}
{{- define "mxe-gitea.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "mxe-gitea.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Product information of Ericsson products
*/}}
{{- define "mxe-gitea.product-info" -}}
ericsson.com/product-name: "MXE"
ericsson.com/product-number: "CXD 101 0813"
ericsson.com/product-revision: {{ .Values.productInfo.rstate | default "-" | quote }}
{{- end -}}

{{/*
Image pull secrets
*/}}
{{- define "mxe-gitea.gitea-keycloak-init.image-pull-secrets" -}}
{{- if .Values.mxeGitea.imageCredentials.registry.url -}}
{{- if .Values.mxeGitea.imageCredentials.registry.pullSecret -}}
imagePullSecrets:
  - name: {{ .Values.mxeGitea.imageCredentials.registry.pullSecret | quote -}}
{{- end -}}
{{- else -}}
{{- if .Values.global.registry.pullSecret -}}
imagePullSecrets:
  - name: {{ .Values.global.registry.pullSecret | quote -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Image repository
*/}}
{{- define "mxe-gitea.gitea-keycloak-init.image-repository" -}}
{{- if .Values.mxeGitea.imageCredentials.registry.url -}}
{{- .Values.mxeGitea.imageCredentials.registry.url -}}
{{- else -}}
{{- .Values.global.registry.url -}}
{{- end -}}
{{- printf "/%s/" .Values.mxeGitea.imageCredentials.repoPath -}}
{{- end -}}

{{/*
Expand the name of the job.
*/}}
{{- define "mxe-gitea.gitea-keycloak-init" -}}
{{- default "gitea-keycloak-init" .Values.mxeGitea.giteaPostInstallInit.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "mxe-gitea.fullname" -}}
{{- if .Values.mxeGitea.fullnameOverride -}}
{{- .Values.mxeGitea.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.mxeGitea.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "mxe-gitea.ingress-default.name" -}}
"{{ include "mxe-gitea.fullname" .}}-gitea-default"
{{- end -}}

{{/*
Expand the host of ingress controller.
*/}}
{{- define "mxe-gitea.ingress-controller-host" -}}
{{- $name := default "eric-mxe-ingress-controller" .Values.mxeGitea.ingressController.hostOverride -}}
{{- printf "%s" $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the port of ingress controller.
*/}}
{{- define "mxe-gitea.ingress-controller-port" -}}
{{- .Values.mxeGitea.ingressController.service.httpPort -}}
{{- end -}}


{{/*
Istio resources - Authorization Policy to allow gitea
*/}}
{{- define "mxe-gitea.pre-install-hook-authz-allow-gitea-name" -}}
{{- default "pre-install-hook-authz-allow-gitea" .Values.mxeGitea.mxePostInstallHookIstioAuthzAllowGitea.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Istio resources - Authorization Policy rules to allow Gitea
*/}}
{{- define "mxe-gitea.istio-authz-policy-rules-allow-gitea" -}}
action: ALLOW
rules:
- to:
  - operation:
      hosts: ["{{ include "mxe-gitea.gitea-api-host" .}}"]
      paths: ["/", "/*"]
{{- end -}}

{{/*
Istio resources - Gitea host name
*/}}
{{- define "mxe-gitea.gitea-api-host" -}}
{{- $a := split "/" .Values.mxeGitea.giteaKeycloakInit.clientRedirectUri -}} 
{{- printf "%s" $a._2 -}}
{{- end -}}

{{/*
Expand the mxe realm user secret of the chart.
*/}}
{{- define "mxe-gitea.secret-realm-mxe" -}}
{{- if .Values.mxeGitea.mxeUserSecretName -}}
{{- .Values.mxeGitea.mxeUserSecretName -}}
{{- else -}}
eric-mxe-gatekeeper-sec-access-creds-realm-mxe
{{- end -}}
{{- end -}}

{{/*
Expand the name of the job.
*/}}
{{- define "mxe-gitea.gitea-create-user" -}}
{{- default "post-install-gitea-create-user" .Values.mxeGitea.giteaPostInstallCreateUser.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Image pull secrets
*/}}
{{- define "mxe-gitea.gitea-create-user.image-pull-secrets" -}}
{{- include "mxe-gitea.gitea-keycloak-init.image-pull-secrets" . }}
{{- end -}}

{{/*
Image repository
*/}}
{{- define "mxe-gitea.gitea-create-user.image-repository" -}}
{{ include "mxe-gitea.gitea-keycloak-init.image-repository" . }}
{{- end -}}

{{/*
Image pull secrets
*/}}
{{- define "mxe-gitea.wait-for-gitea.image-pull-secrets" -}}
{{- include "mxe-gitea.gitea-keycloak-init.image-pull-secrets" . }}
{{- end -}}

{{/*
Image repository
*/}}
{{- define "mxe-gitea.wait-for-gitea.image-repository" -}}
{{ include "mxe-gitea.gitea-keycloak-init.image-repository" . }}
{{- end -}}

{{/*
Expand the name of the job.
*/}}
{{- define "mxe-gitea.gitea-create-repo" -}}
{{- default "post-install-gitea-create-repo" .Values.mxeGitea.giteaPostInstallCreateRepo.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Image pull secrets
*/}}
{{- define "mxe-gitea.gitea-create-repo.image-pull-secrets" -}}
{{- include "mxe-gitea.gitea-keycloak-init.image-pull-secrets" . }}
{{- end -}}

{{/*
Image repository
*/}}
{{- define "mxe-gitea.gitea-create-repo.image-repository" -}}
{{ include "mxe-gitea.gitea-keycloak-init.image-repository" . }}
{{- end -}}

{{/*
Expand the gitOps repo to use
*/}}
{{- define "mxe-gitea.gitOpsRepoURL" -}}
{{- if .Values.internalGitopsRepoEnabled -}}
{{- $giteaRootURL:= (index .Values "mxeGitea" "giteaRootUrl") -}}
{{- $org:= (index .Values "mxeGitea" "keepAliveRepo" "org") -}}
{{- $repo:= (index .Values "mxeGitea" "keepAliveRepo" "repo") -}}
{{- print $giteaRootURL "/" $org "/" $repo ".git"  -}}
{{- else -}}
{{- .Values.mxeGitea.giteaRootUrl -}}
{{- end -}}
{{- end -}}

{{/*
Expand the name of the job.
*/}}
{{- define "mxe-gitea.gitea-create-auth-source" -}}
{{- default "post-install-gitea-create-auth-source" .Values.mxeGitea.giteaPostInstallCreateAuthSource.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Image pull secrets
*/}}
{{- define "mxe-gitea.gitea-create-auth-source.image-pull-secrets" -}}
{{- include "mxe-gitea.gitea-keycloak-init.image-pull-secrets" . }}
{{- end -}}

{{/*
Image repository
*/}}
{{- define "mxe-gitea.gitea-create-auth-source.image-repository" -}}
{{- printf "%s" .Values.gitea.image.repository -}}
{{- end -}}

{{/*
Expand the name of the job.
*/}}
{{- define "mxe-gitea.wait-for-gitea.name" -}}
{{- default "post-install-wait-for-gitea" .Values.mxeGitea.giteaPostInstallWaitForGitea.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
 MXE Ingress Gateway - name
*/}}
{{- define "mxe-gitea.mesh-ingress-gw-svc-name" -}}
eric-mesh-ingressgateway
{{- end -}}

{{/*
 MXE Ingress Gateway - port
*/}}
{{- define "mxe-gitea.mesh-ingress-gw-port" -}}
{{- if .Values.global.serviceMesh.enabled -}}
443
{{- else -}}
80
{{- end -}}
{{- end -}}

{{/*
 MXE Ingress Gateway - protocol
*/}}
{{- define "mxe-gitea.mesh-ingress-gw-protocol" -}}
{{- if .Values.global.serviceMesh.enabled -}}
HTTPS
{{- else -}}
HTTP
{{- end -}}
{{- end -}}

{{/*
 MXE Ingress Gateway - name
*/}}
{{- define "mxe-gitea.mesh-ingress-gw-name" -}}
mxe-commons-ingress-gw
{{- end -}}

{{/*
Add External CA - Args command
*/}}
{{- define "mxe-gitea.add-externalca-command" -}}
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
{{- define "mxe-gitea.add-externalca-volume-mounts" -}}
{{- if .Values.isExtCA -}}
- mountPath: /usr/share/pki/trust/anchors/extca.crt
  name: iam-ca-cert
  subPath: ca.crt
- mountPath: /cacerts
  name: cacerts
{{- end -}}
{{- end -}}

{{/*
Add External CA - volume mounts for main container
*/}}
{{- define "mxe-gitea.add-externalca-volume-mounts-maincontainer" -}}
{{- if .Values.isExtCA -}}
- mountPath: /var/lib/ca-certificates
  name: cacerts
{{- end -}}
{{- end -}}

{{/*
Add External CA - volumes
*/}}
{{- define "mxe-gitea.add-externalca-volumes" -}}
{{- if .Values.isExtCA -}}
- name: iam-ca-cert
  secret:
    defaultMode: 420
    secretName: iam-ca-cert
- emptyDir: {}
  name: cacerts
{{- end -}}
{{- end -}}

{{/*
Add External CA - security context
*/}}
{{- define "mxe-gitea.add-externalca-security-context" -}}
allowPrivilegeEscalation: false
capabilities:
  drop:
  - all
privileged: false
runAsNonRoot: true
{{- end -}}

{{/*
Security annotations
*/}}
{{- define "mxe-gitea.security-annotations-gitea-user" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" "gitea-create-user" }}: {{ $appArmorProfile }}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" "wait-for-gitea" }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{- define "mxe-gitea.security-annotations-gitea-repo" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" "gitea-create-repo" }}: {{ $appArmorProfile }}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" "wait-for-gitea" }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{- define "mxe-gitea.security-annotations-gitea-auth" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" "gitea-create-auth-source" }}: {{ $appArmorProfile }}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" "wait-for-gitea" }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{- define "mxe-gitea.security-annotations-gitea-restart" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" "rollout-restart-gitea" }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{- define "mxe-gitea.security-annotations-gitea-keycloak" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" "gitea-keycloak-init" }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{/*
Security Context 
*/}}
{{- define "mxe-gitea.jobs-security-context" -}}
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

{{- define "mxe-deployer-gitea.registryImagePullPolicy" -}}
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
