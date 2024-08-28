{{/*
Expand the name of the chart.
*/}}
{{- define "eric-mxe-deployer-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-mxe-deployer-service.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Product information of Ericsson products
*/}}
{{- define "eric-mxe-deployer-service.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "eric-mxe-deployer-service.common-labels" -}}
helm.sh/chart: {{ include "eric-mxe-deployer-service.chart" . | quote }}
{{ include "eric-mxe-deployer-service.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "eric-mxe-deployer-service.selectorLabels" -}}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
app.kubernetes.io/part-of: "mxe-deployer"
{{- end }}

{{/*
Get labels from values
*/}}
{{ define "eric-mxe-deployer-service.config-labels" }}
  {{- $global := (.Values.global).labels -}}
  {{- $service := .Values.labels -}}
  {{- include "eric-mxe-deployer-service.mergeLabels" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merge all labels - [DR-D1121-060]
*/}}
{{- define "eric-mxe-deployer-service.labels" -}}
  {{- $standard := include "eric-mxe-deployer-service.common-labels" . | fromYaml -}}
  {{- $config := include "eric-mxe-deployer-service.config-labels" . | fromYaml -}}
  {{- include "eric-mxe-deployer-service.mergeLabels" (dict "location" .Template.Name "sources" (list $standard $config)) | trim }}
{{- end -}}

{{/*
Create a user defined annotation - [DR-D1121-060]
*/}}
{{ define "eric-mxe-deployer-service.config-annotations" }}
  {{- $global := (.Values.global).annotations -}}
  {{- $service := .Values.annotations -}}
  {{- include "eric-mxe-deployer-service.mergeAnnotations" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged annotations for Default, which includes productInfo and config - Used in all files
*/}}
{{- define "eric-mxe-deployer-service.annotations" -}}
  {{- $productInfo := include "eric-mxe-deployer-service.product-info" . | fromYaml -}}
  {{- $config := include "eric-mxe-deployer-service.config-annotations" . | fromYaml -}}
  {{- include "eric-mxe-deployer-service.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $config)) | trim }}
{{- end -}}

{{/*
Image pull secrets
*/}}
{{- define "eric-mxe-deployer-service.image-pull-secrets" -}}
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
{{- define "eric-mxe-deployer-service.image-repository" -}}
{{- if .Values.imageCredentials.registry.url -}}
{{- .Values.imageCredentials.registry.url -}}
{{- else -}}
{{- .Values.global.registry.url -}}
{{- end -}}
{{- printf "/%s/" .Values.imageCredentials.repoPath -}}
{{- end -}}


{{/*
Expand the name of the configmap of waiting for argocd server.
*/}}
{{- define "eric-mxe-deployer-service.init-configmap" -}}
{{- $name := include "eric-mxe-deployer-service.name" . -}}
{{- printf "%s-%s" $name "init-configmap" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the dm-configmap note: this was hard named as dm-config in MXE 2.3 and earlier versions.
*/}}
{{- define "eric-mxe-deployer-service.dm-configmap" -}}
{{- $name := include "eric-mxe-deployer-service.name" . -}}
{{- printf "%s-%s" $name "dm-configmap" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the authors-configmap note: this was hard named as authors-configmap in MXE 2.3 and earlier versions.
*/}}
{{- define "eric-mxe-deployer-service.authors-configmap" -}}
{{- $name := include "eric-mxe-deployer-service.name" . -}}
{{- printf "%s-%s" $name "authors-configmap" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the role binding.
*/}}
{{- define "eric-mxe-deployer-service.rolebinding" -}}
{{- $name := include "eric-mxe-deployer-service.name" . -}}
{{- printf "%s-%s" $name "rolebinding" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the role.
*/}}
{{- define "eric-mxe-deployer-service.role" -}}
{{- $name := include "eric-mxe-deployer-service.name" . -}}
{{- printf "%s-%s" $name "role" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the service account of the chart.
*/}}
{{- define "eric-mxe-deployer-service.serviceaccount" -}}
{{- $name := include "eric-mxe-deployer-service.name" . -}}
{{- printf "%s-%s" $name "serviceaccount" | trunc 63 | trimSuffix "-" -}}
{{- end -}}


{{/*
Expand the argocd server to connect to
*/}}
{{- define "eric-mxe-deployer-service.argocdserver" -}}
{{- printf "%s:%v" .Values.config.argocd.server.serviceName .Values.config.argocd.server.servicePort -}}
{{- end -}}

{{/*
Expand the argocd server to connect to
*/}}
{{- define "eric-mxe-deployer-service.usePlaintext" -}}
{{- true -}}
{{- end -}}


{{/*
Expand the gitOps repo to use
*/}}
{{- define "eric-mxe-deployer-service.gitOpsRepoURL" -}}
{{- if .Values.config.git.internal.enabled -}}
{{- $giteaRootURL:= (index .Values "config" "git" "internal" "rootUrl") -}}
{{- $org:= (index .Values "config" "git" "internal" "org") -}}
{{- $repo:= (index .Values "config" "git" "internal" "repo") -}}
{{- print $giteaRootURL "/" $org "/" $repo ".git"  -}}
{{- else -}}
{{- .Values.config.git.url -}}
{{- end -}}
{{- end -}}

{{/*
Is Gitops repo URL HTTP/SSH ?
*/}}
{{- define "eric-mxe-deployer-service.gitOpsRepoConnProtocol" -}}
{{- $repoURL := include "eric-mxe-deployer-service.gitOpsRepoURL" . -}}
{{- if hasPrefix "http" $repoURL -}}
{{- "http"  -}}
{{- else -}}
{{- "ssh" -}}
{{- end -}}
{{- end -}}

{{/*
Git commit domain to use
*/}}
{{- define "eric-mxe-deployer-service.gitopsRepoDomain" -}}
{{- if .Values.config.git.internal.enabled -}}
{{- .Values.config.git.internal.domain -}}
{{- else -}}
{{- .Values.config.git.author.domain -}}
{{- end -}}
{{- end -}}

{{/*
Create default ingress annotation prefix.
*/}}
{{- define "eric-mxe-deployer-service.ingress-annotation-prefix" -}}
{{- if .Values.ingress.annotationPrefix -}}
{{- .Values.ingress.annotationPrefix -}}
{{- else -}}
mxe.nginx.ingress.kubernetes.io
{{- end -}}
{{- end -}}

{{/*
Expand the keycloak namespace
*/}}
{{- define "eric-mxe-deployer-service.oauth-service-namespace" -}}
{{- if .Values.config.sso.mxe.namespace -}}
{{- .Values.config.sso.mxe.namespace -}}
{{- else -}}
{{- .Release.Namespace -}}
{{- end -}}
{{- end -}}

{{/*
Expand the mxeHost
*/}}
{{- define "eric-mxe-deployer-service.mxe-host" -}}
{{- if eq (.Values.config.sso.mxe.ingress.apiPort | int64) 443 -}}
{{- printf "%s://%s" .Values.config.sso.mxe.ingress.protocol .Values.config.sso.mxe.ingress.apiHostName -}}
{{- else -}}
{{- printf "%s://%s:%v" .Values.config.sso.mxe.ingress.protocol .Values.config.sso.mxe.ingress.apiHostName .Values.config.sso.mxe.ingress.apiPort -}}
{{- end -}}
{{- end -}}


{{/*
Expand the keycloak realm path
*/}}
{{- define "eric-mxe-deployer-service.oauth-realm-path" -}}
{{- printf "/auth/realms/%s" .Values.config.sso.argocdRealmName -}}
{{- end -}}

{{/*
Expand the keycloak realm issuer using mxeApiHostName
*/}}
{{- define "eric-mxe-deployer-service.actual-oauth-issuer" -}}
{{- $mxeHost := include "eric-mxe-deployer-service.mxe-host" . -}}
{{- $realmPath := include "eric-mxe-deployer-service.oauth-realm-path" . -}}
{{- printf "%s%s" $mxeHost $realmPath -}}
{{- end -}}

{{/*
Keycloak oauth host name based url for issuer
Based on structure followed in mxe-commons
*/}}
{{- define "eric-mxe-deployer-service.oauth-host" -}}
{{- if eq (.Values.config.sso.mxe.ingress.apiPort | int64) 443 -}}
{{- printf "https://%s" .Values.config.sso.mxe.ingress.oauthApiHostName -}}
{{- else -}}
{{- printf "https://%s:%v" .Values.config.sso.mxe.ingress.oauthApiHostName .Values.config.sso.mxe.ingress.apiPort -}}
{{- end -}}
{{- end -}}

{{/*
Expand the keycloak realm issuer using the oauth endpoint
*/}}
{{- define "eric-mxe-deployer-service.oauth-issuer" -}}
{{- $oauthHost := include "eric-mxe-deployer-service.oauth-host" . -}}
{{- $realmPath := include "eric-mxe-deployer-service.oauth-realm-path" . -}}
{{- printf "%s%s" $oauthHost $realmPath -}}
{{- end -}}

{{/*
Expand the argocd ui url
*/}}
{{- define "eric-mxe-deployer-service.argocdui-url" -}}
{{- $namespace := include "eric-mxe-deployer-service.oauth-service-namespace" . -}}
{{- if .Values.config.argocd.ui.ingress.enabled -}}
{{- printf "https://%s" .Values.config.argocd.ui.ingress.host -}}
{{- else -}}
{{- printf "%s.%s.svc.cluster.local:%s" .Values.config.argocd.server.serviceName $namespace  .Values.config.argocd.ui.servicePort -}}
{{- end -}}
{{- end -}}


{{/*
Expand the keycloak realm path
*/}}
{{- define "eric-mxe-deployer-service.post-install-hook-deployer-istio-authz-policy-name" -}}
{{- $name := include "eric-mxe-deployer-service.name" . -}}
{{- $suffixname := default "post-install-hook-istio-authz-policy" .Values.postInstallHookIstio.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- printf "%s-%s" $name $suffixname | trunc 63 | trimSuffix "-" -}}
{{- end -}}


{{/*
Istio resources - Authorization Policy rules to allow Deployer endpoints
*/}}
{{- define "eric-mxe-deployer-service.istio-authz-policy-rules-allow-deployer" -}}
{{- $argoCDUIIngressEnabled := default false .Values.config.argocd.ui.ingress.enabled  }}
{{- $mxeDeployerIngressEnabled := default false .Values.ingress.enabled  }}
action: ALLOW
rules:
- to:
  - operation:
      hosts:
      {{- if $argoCDUIIngressEnabled }}
      - {{ include "eric-mxe-deployer-service.mxe-argocd.argocd-ui-ingress-host" . | quote}}
      {{- end }}
      {{- if $mxeDeployerIngressEnabled }}
      - {{ .Values.ingress.hostname | quote }}
      {{- end }}
      paths: ["/", "/*"]
{{- end -}}


{{/*
Istio resources - Authorization Policy to allow deployer endpoints
*/}}
{{- define "eric-mxe-deployer-service.pre-install-hook-authz-name" -}}
{{- $name := include "eric-mxe-deployer-service.name" . -}}
{{- $suffixname := default "pre-install-hook-authz-allow" .Values.deployerPreInstallHookAuthzAllow.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- printf "%s-%s" $name $suffixname | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Istio resources - Request Authentication
*/}}
{{- define "eric-mxe-deployer-service.post-install-hook-istio-req-authn-name" -}}
{{- $name := include "eric-mxe-deployer-service.name" . -}}
{{- $suffixname := default "post-install-hook-istio-req-authn" .Values.deployerPreInstallHookReqAuthn.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- printf "%s-%s" $name $suffixname | trunc 63 | trimSuffix "-" -}}
{{- end -}}


{{/*
Istio resources - Request Authentication rules
*/}}
{{- define "eric-mxe-deployer-service.istio-req-authn-jwt-rules" -}}
jwtRules:
- issuer: "{{ include "eric-mxe-deployer-service.oauth-issuer" .}}"
  jwksUri: "{{ include "eric-mxe-deployer-service.oauth-issuer" .}}/protocol/openid-connect/certs"
  forwardOriginalToken: true
{{- end -}}

{{/*
Expand the argocd http default ingress name
*/}}
{{- define "eric-mxe-deployer-service.mxe-argocd.server.httpingress-default.name" -}}
{{- $name := include "eric-mxe-deployer-service.name" . -}}
{{- $suffixname := default "argocd-server" .Values.config.argocd.server.fullNameOverride | trunc 63 | trimSuffix "-" -}}
{{- printf "%s-%s-default" $name $suffixname | trunc 63 | trimSuffix "-" -}}
{{- end -}}



{{/*
Expand the host of ingress controller.
*/}}
{{- define "eric-mxe-deployer-service.mxe-argocd.ingress-controller-host" -}}
{{- $name := default "eric-mxe-ingress-controller" (index .Values "config" "sso" "mxe" "ingressController" "nameOverride") -}}
{{- printf "%s" $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the port of ingress controller.
*/}}
{{- define "eric-mxe-deployer-service.mxe-argocd.ingress-controller-port" -}}
{{- (index .Values "config" "sso" "mxe" "ingressController" "service" "httpPort") -}}
{{- end -}}

{{/*
Argo CD ui ingress host
*/}}
{{- define "eric-mxe-deployer-service.mxe-argocd.argocd-ui-ingress-host" -}}
{{- (index .Values "config" "argocd" "ui" "ingress" "host") -}}
{{- end -}}

{{/*
Argo CD ui ingress host tls secret
*/}}
{{- define "eric-mxe-deployer-service.mxe-argocd.argocd-ui-tls" -}}
{{- (index .Values "config" "argocd" "ui" "ingress" "tlsSecretName") -}}
{{- end -}}

{{/*
Add External CA - Args command
*/}}
{{- define "eric-mxe-deployer-service.add-externalca-command" -}}
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
{{- define "eric-mxe-deployer-service.add-externalca-volume-mounts" -}}
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
{{- define "eric-mxe-deployer-service.add-externalca-volume-mounts-maincontainer" -}}
{{- if .Values.isExtCA -}}
- mountPath: /var/lib/ca-certificates
  name: cacerts
{{- end -}}
{{- end -}}

{{/*
Add External CA - volumes
*/}}
{{- define "eric-mxe-deployer-service.add-externalca-volumes" -}}
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
adding security context
*/}}
{{- define "eric-mxe-deployer-service.initcontainer-security-context" -}}
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
{{- define "eric-mxe-deployer-service.container-security-context" -}}
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

{{- define "eric-mxe-deployer-service.podPriority" -}}
{{- if .Values.podPriority -}}
  {{- if .Values.podPriority.deployerservice -}}
    {{- .Values.podPriority.deployerservice.priorityClassName | toString -}}
  {{- end -}}
{{- end -}}
{{- end -}}


{{/*
 MXE Ingress Gateway - name
*/}}
{{- define "eric-mxe-deployer-service.mesh-ingress-gw-svc-name" -}}
eric-mesh-ingressgateway
{{- end -}}

{{/*
 MXE Ingress Gateway - name
*/}}
{{- define "eric-mxe-deployer-service.mesh-ingress-gw-port" -}}
{{- if .Values.global.serviceMesh.enabled -}}
443
{{- else -}}
80
{{- end -}}
{{- end -}}

{{/*
 MXE Ingress Gateway - name
*/}}
{{- define "eric-mxe-deployer-service.mesh-ingress-gw-protocol" -}}
{{- if .Values.global.serviceMesh.enabled -}}
HTTPS
{{- else -}}
HTTP
{{- end -}}
{{- end -}}

{{/*
Security annotations
*/}}
{{- define "eric-mxe-deployer-service.security-annotations" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name }}: {{ $appArmorProfile }}
{{ printf "%s/%s-%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name "argocd-repos-init" }}: {{ $appArmorProfile }}
{{ printf "%s/%s-%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name "keycloak-init" }}: {{ $appArmorProfile }}
{{ printf "%s/%s-%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name "kubectl" }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{/*
Create a map from ".Values.global" with defaults if missing in values file.
This hides defaults from values file.
*/}}
{{ define "eric-mxe-deployer-service.global" }}
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
{{ define "eric-mxe-deployer-service.nodeSelector" }}
{{- $g := fromJson (include "eric-mxe-deployer-service.global" .) -}}
{{- $global := $g.nodeSelector -}}
{{- $service := .Values.nodeSelector -}}
{{- include "eric-mxe-deployer-service.aggregatedMerge" (dict "context" "nodeSelector" "location" .Template.Name "sources" (list $global $service)) -}}
{{ end }}

{{/*
adding TopologySpreadConstraints
*/}}
{{- define "eric-mxe-deployer-service.topologySpreadConstraints" }}
{{- if .Values.topologySpreadConstraints }}
{{- range $config, $values := .Values.topologySpreadConstraints }}
- topologyKey: {{ $values.topologyKey }}
  maxSkew: {{ $values.maxSkew | default 1 }}
  whenUnsatisfiable: {{ $values.whenUnsatisfiable | default "ScheduleAnyway" }}
{{- end }}
{{- end }}
{{- end }}

{{- define "eric-mxe-deployer-service.registryImagePullPolicy" -}}
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

{{- define "eric-mxe-deployer-service.ericProdInfoRepoPath" }}
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

{{- define "eric-mxe-deployer-service.ericProdInfoRegistry" }}
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
eric-mxe-deployer-service image name path
*/}}
{{- define "eric-mxe-deployer-service.setImageRepo" }}
  {{- $top := index . 0 }}
  {{- $serviceLevelImageRegistry := index . 1 }}
  {{- $serviceLevelImageRepoPath := index . 2 }}
  {{- $imageName := index . 3 }}

  {{- /*
  Registry and Repopath for the specified image container is fetched from eric-product-info.yaml
  */}}
  {{- $productInfo := fromYaml ($top.Files.Get "eric-product-info.yaml") -}}
  {{- $registryUrl := include "eric-mxe-deployer-service.ericProdInfoRegistry" (list $productInfo $imageName) -}}
  {{- $imageRepoPath := include "eric-mxe-deployer-service.ericProdInfoRepoPath" (list $productInfo $imageName) -}}

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

{{- define "eric-mxe-deployer-service.hook-weight" }}
{{- $val := . }}
helm.sh/hook-weight: {{ quote $val }}
{{- end -}}

{{- define "eric-mxe-deployer-service.ingress-annotations" }}
nginx.ingress.kubernetes.io/session-cookie-expires: "36000"
nginx.ingress.kubernetes.io/session-cookie-max-age: "36000"
nginx.ingress.kubernetes.io/proxy-body-size: 20G
nginx.ingress.kubernetes.io/proxy-request-buffering: "off"
nginx.ingress.kubernetes.io/proxy-connect-timeout: "120"
nginx.ingress.kubernetes.io/proxy-send-timeout: "120"
nginx.ingress.kubernetes.io/proxy-read-timeout: "120"
nginx.ingress.kubernetes.io/backend-protocol: {{ include "eric-mxe-deployer-service.mesh-ingress-gw-protocol" . | quote }}
{{- end -}}

{{/*
DR-D1123-135 podSecurityContext supplementalGroups
*/}}
{{- define "eric-mxe-deployer-service.podSecurityContext.supplementalGroups" -}}
  {{- /*
  Fetch the global.podSecurityContext.supplementalGroups
  */}}
  {{- $listGlobal :=  (list) -}}
  {{- if .Values.global -}}
    {{- if .Values.global.podSecurityContext -}}
      {{- if .Values.global.podSecurityContext.supplementalGroups -}}
        {{- $listGlobal = .Values.global.podSecurityContext.supplementalGroups -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}

  {{- /*
  Fetch the service level podSecurityContext.supplementalGroups
  */}}
  {{- $listLocal := (list) -}}
  {{- if .Values.podSecurityContext -}}
    {{- if .Values.podSecurityContext.supplementalGroups -}}
      {{- $listLocal = .Values.podSecurityContext.supplementalGroups -}}
    {{- end -}}
  {{- end -}}

  {{- /*
  Merge both the lists
  */}}  
  {{- $listMerged := (list) -}}
  {{- if $listGlobal -}}
    {{- $listMerged = $listGlobal -}}
  {{- end -}}
  {{- if $listLocal -}}
    {{- $listMerged = concat $listMerged $listLocal | uniq -}}
  {{- end -}}
  {{- if $listMerged -}}
    {{- toYaml $listMerged | nindent 8 -}}
  {{- end -}}
{{- end -}}

{{/*
Add Istio Label
*/}}
{{- define "eric-mxe-deployer-service.istio-sidecar-labels" -}}
sidecar.istio.io/inject: {{ .Values.global.serviceMesh.enabled | quote }}
{{- end -}}

{{/*
Prometheus annotations
*/}}
{{- define "eric-mxe-deployer-service.prometheus-annotations" -}}
prometheus.io/path: {{ .Values.global.mxePrometheusPath | quote }}
prometheus.io/port: {{ .Values.global.mxePrometheusPort | quote }}
prometheus.io/scrape: {{ .Values.global.mxePrometheusScrape | quote }}
prometheus.io/scrape-role: {{ .Values.global.mxePrometheusScrapeRole | quote }}
prometheus.io/scrape-interval: {{ .Values.global.mxePrometheusScrapeInterval | quote }}
{{- end -}}
{{/*
Merge global tolerations with service tolerations (DR-D1120-061-AD).
*/}}
{{- define "eric-mxe-deployer-service.merge-tolerations" -}}
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
        {{- $globalItemId := include "eric-mxe-deployer-service.merge-tolerations.get-identifier" $globalItem -}}
        {{- range $serviceItem := $serviceTolerations -}}
          {{- $serviceItemId := include "eric-mxe-deployer-service.merge-tolerations.get-identifier" $serviceItem -}}
          {{- if eq $serviceItemId $globalItemId -}}
            {{- $matchingItems = append $matchingItems $serviceItem -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
      {{- range $globalItem := $globalTolerations -}}
        {{- $globalItemId := include "eric-mxe-deployer-service.merge-tolerations.get-identifier" $globalItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-deployer-service.merge-tolerations.get-identifier" $matchItem -}}
          {{- if eq $matchItemId $globalItemId -}}
            {{- $matchCount = add1 $matchCount -}}
          {{- end -}}
        {{- end -}}
        {{- if eq $matchCount 0 -}}
          {{- $nonMatchingItems = append $nonMatchingItems $globalItem -}}
        {{- end -}}
      {{- end -}}
      {{- range $serviceItem := $serviceTolerations -}}
        {{- $serviceItemId := include "eric-mxe-deployer-service.merge-tolerations.get-identifier" $serviceItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-deployer-service.merge-tolerations.get-identifier" $matchItem -}}
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
{{ define "eric-mxe-deployer-service.merge-tolerations.get-identifier" }}
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
{{- define "eric-mxe-deployer-service.pg-client-cert" -}}
{{ .Values.global.serviceMesh.egress.postgres.clientCertificate }}
{{- end -}}

{{/*
postgres certificate private key
*/}}
{{- define "eric-mxe-deployer-service.pg-private-key" -}}
{{ .Values.global.serviceMesh.egress.postgres.privateKey }}
{{- end -}}

{{/*
postgres certificate ca
*/}}
{{- define "eric-mxe-deployer-service.pg-ca-issuer" -}}
{{ .Values.global.serviceMesh.egress.postgres.caIssuer }}
{{- end -}}

{{/*
pg-cert - volume mounts
*/}}
{{- define "eric-mxe-deployer-service.pg-cert-volume-mounts" -}}
{{- if .Values.global.security.tls.enabled }}
- name: pg-cert
  mountPath: /run/secrets/certificates/client/pg-cert
- name: trusted-ca
  mountPath: /run/secrets/certificates/trusted
  readOnly: true
{{- end -}}
{{- end -}}

{{/*
pg-cert -  volumes
*/}}
{{- define "eric-mxe-deployer-service.pg-cert-volumes" -}}
{{- if .Values.global.security.tls.enabled }}
- name: trusted-ca
  secret:
    secretName: eric-sec-sip-tls-trusted-root-cert
- name: pg-cert
  secret:
    secretName: {{ include "eric-mxe-deployer-service.name" . }}-pg-cert
{{- end -}}
{{- end -}}
