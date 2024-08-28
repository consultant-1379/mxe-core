{{- define "eric-mxe-jupyterhub.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-mxe-jupyterhub.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- /*
  eric-mxe-jupyterhub.userTolerations
    Lists the tolerations for node taints that the user pods should have
*/}}
{{- define "eric-mxe-jupyterhub.userTolerations" -}}
- key: hub.jupyter.org_dedicated
  operator: Equal
  value: user
  effect: NoSchedule
- key: hub.jupyter.org/dedicated
  operator: Equal
  value: user
  effect: NoSchedule
{{- if .Values.singleuser.extraTolerations }}
{{- .Values.singleuser.extraTolerations | toYaml | trimSuffix "\n" | nindent 0 }}
{{- end }}
{{- end }}

{{- define "eric-mxe-jupyterhub.userNodeAffinityRequired" -}}
{{- if .Values.singleuser.extraNodeAffinity.required }}
{{- .Values.singleuser.extraNodeAffinity.required | toYaml | trimSuffix "\n" | nindent 0 }}
{{- end }}
{{- end }}

{{- define "eric-mxe-jupyterhub.coreAffinity" -}}
affinity:
  nodeAffinity:
    preferredDuringSchedulingIgnoredDuringExecution:
      - weight: 100
        preference:
          matchExpressions:
            - key: hub.jupyter.org/node-purpose
              operator: In
              values: [core]
{{- end }}

{{/* vim: set filetype=mustache: */}}
{{/*
Create default backend name.
*/}}
{{- define "eric-mxe-jupyterhub.defaultbackend" -}}
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
{{- define "eric-mxe-jupyterhub.ingress-class" -}}
{{- if .Values.ingress.class -}}
{{- .Values.ingress.class -}}
{{- else -}}
eric-mxe-ingress-controller-class
{{- end -}}
{{- end -}}

{{/*
Create default ingress class name.
*/}}
{{- define "eric-mxe-jupyterhub.ingress-annotation-prefix" -}}
{{- if .Values.ingress.annotationPrefix -}}
{{- .Values.ingress.annotationPrefix -}}
{{- else -}}
mxe.nginx.ingress.kubernetes.io
{{- end -}}
{{- end -}}

{{/*
Image pull secrets
*/}}
{{- define "eric-mxe-jupyterhub.image-pull-secrets" -}}
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
Image registry
*/}}
{{- define "eric-mxe-jupyterhub.image-registry" -}}
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
{{- define "eric-mxe-jupyterhub.image-repository" -}}
{{- include "eric-mxe-jupyterhub.image-registry" . -}}
{{- printf "%s" .Values.imageCredentials.repoPath -}}
/
{{- end -}}

{{/*
Product information of Ericsson products
*/}}
{{- define "eric-mxe-jupyterhub.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}

{{/*
   Kubernetes Availability and Resiliency Design Rule DR-D1120-060-AD
*/}}
{{- define "eric-mxe-jupyterhub.tolerations" -}}
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
Update strategy for deployments
*/}}
{{- define "eric-mxe-jupyterhub.update-strategy" -}}
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
{{- define "eric-mxe-jupyterhub.ingress-tls" -}}
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
Expand the name of the Jupyterhub ingress.
*/}}
{{- define "eric-mxe-jupyterhub.ingress-name" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "ingress" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Jupyterhub hub.
*/}}
{{- define "eric-mxe-jupyterhub.hub-name" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "hub" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Jupyterhub configmap.
*/}}
{{- define "eric-mxe-jupyterhub.hub-config-name" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "hub-config" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Jupyterhub pvc.
*/}}
{{- define "eric-mxe-jupyterhub.hub-pvc-name" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "hub-db-dir" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Jupyterhub secret.
*/}}
{{- define "eric-mxe-jupyterhub.hub-secret-name" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "hub-secret" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Jupyter Single user secret.
*/}}
{{- define "eric-mxe-jupyterhub.singleuser-extrafile-secret-name" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "singleuser-extrafile-secret" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Jupyterhub's hub's pod distribution budget.
*/}}
{{- define "eric-mxe-jupyterhub.hub-pod-distribution-budget-name" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "hub-pdb" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Jupyterhub's hub's service account.
*/}}
{{- define "eric-mxe-jupyterhub.hub-service-account-name" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "hub-service-account" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Jupyterhub's hub's role.
*/}}
{{- define "eric-mxe-jupyterhub.hub-role-name" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "hub-role" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Jupyterhub's hub's role binding.
*/}}
{{- define "eric-mxe-jupyterhub.hub-role-binding-name" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "hub-role-binding" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Jupyterhub's hub's service
*/}}
{{- define "eric-mxe-jupyterhub.hub-service-name" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "hub-service" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Jupyterhub proxy.
*/}}
{{- define "eric-mxe-jupyterhub.proxy-name" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "proxy" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Jupyterhub proxy-api.
*/}}
{{- define "eric-mxe-jupyterhub.proxy-service-api-name" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "proxy-api" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Jupyterhub proxy-public.
*/}}
{{- define "eric-mxe-jupyterhub.proxy-service-public-name" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "proxy-public" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Jupyterhub proxy-public.
*/}}
{{- define "eric-mxe-jupyterhub.proxy-pod-distribution-budget-name" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "proxy-pdb" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Jupyterhub image puller.
*/}}
{{- define "eric-mxe-jupyterhub.image-puller" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "image-puller" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Jupyterhub hook image awaiter.
*/}}
{{- define "eric-mxe-jupyterhub.hook-image-awaiter" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s-%s" $name "hook-image-awaiter" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the Environment variable service host.
*/}}
{{- define "eric-mxe-jupyterhub.env-hub-service-host" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s_%s" $name "HUB_SERVICE_SERVICE_HOST" | trunc 63 | trimSuffix "-" | upper | replace "-" "_" -}}
{{- end -}}

{{/*
Expand the name of the Environment variable service port.
*/}}
{{- define "eric-mxe-jupyterhub.env-hub-service-port" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s_%s" $name "HUB_SERVICE_SERVICE_PORT" | trunc 63 | trimSuffix "-" | upper | replace "-" "_" -}}
{{- end -}}

{{/*
Expand the name of the Environment variable proxy api service host.
*/}}
{{- define "eric-mxe-jupyterhub.env-hub-proxy-api-service-host" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s_%s" $name "PROXY_API_SERVICE_HOST" | trunc 63 | trimSuffix "-" | upper | replace "-" "_" -}}
{{- end -}}

{{/*
Expand the name of the Environment variable proxy api service port.
*/}}
{{- define "eric-mxe-jupyterhub.env-hub-proxy-api-service-port" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s_%s" $name "PROXY_API_SERVICE_PORT" | trunc 63 | trimSuffix "-" | upper | replace "-" "_" -}}
{{- end -}}

{{/*
Expand the name of the Environment variable proxy public service host.
*/}}
{{- define "eric-mxe-jupyterhub.env-hub-proxy-public-service-host" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s_%s" $name "PROXY_PUBLIC_SERVICE_HOST" | trunc 63 | trimSuffix "-" | upper | replace "-" "_" -}}
{{- end -}}

{{/*
Expand the name of the Environment variable proxy public service port.
*/}}
{{- define "eric-mxe-jupyterhub.env-hub-proxy-public-service-port" -}}
{{- $name := include "eric-mxe-jupyterhub.name" . -}}
{{- printf "%s_%s" $name "PROXY_PUBLIC_SERVICE_PORT" | trunc 63 | trimSuffix "-" | upper | replace "-" "_" -}}
{{- end -}}

{{/*
pypi configuration trusted host
*/}}
{{- define "eric-mxe-jupyterhub.trusted-hosts" -}}
  {{- $trustedHosts := list -}}
  {{- if .Values.pypiServer.internal -}}
    {{- $_ := urlParse .Values.pypiServer.internal -}}
    {{ if eq (get $_ "scheme") "http"  -}}
      {{- $trustedHosts =  get $_ "host" | append $trustedHosts -}}
    {{- end -}}
  {{- end -}}
  {{- if .Values.pypiServer.external -}}
    {{- $_ := urlParse .Values.pypiServer.external -}}
    {{ if eq (get $_ "scheme") "http"  -}}
       {{- $trustedHosts =  get $_ "host" | append $trustedHosts -}}
    {{- end -}}
  {{- end -}}
  {{- if $trustedHosts -}}
    {{- print "trusted-host = " (join " "  $trustedHosts) -}}
  {{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations
*/}}
{{- define "eric-mxe-jupyterhub.istio-sidecar-annotations" -}}
sidecar.istio.io/rewriteAppHTTPProbers: {{ .Values.global.serviceMesh.enabled | quote }}
{{ include "eric-mxe-jupyterhub.istio-sidecar-annotations-egress" . }}
{{- end -}}

{{- define "eric-mxe-jupyterhub.istio-sidecar-labels" -}}
sidecar.istio.io/inject: {{ .Values.global.serviceMesh.enabled | quote }}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts
*/}}
{{- define "eric-mxe-jupyterhub.istio-sidecar-annotations-egress" -}}
{{- if .Values.global.serviceMesh.enabled -}}
{{ include "eric-mxe-jupyterhub.istio-sidecar-annotations-egress-mounts" . }}
proxy.istio.io/config: '{ "holdApplicationUntilProxyStarts": true }'
{{- end -}}
{{- end -}}

{{- define "eric-mxe-jupyterhub.hub.podPriority" -}}
{{- if .Values.podPriority -}}
  {{- if .Values.podPriority.mxeJupyterhubHub -}}
    {{- .Values.podPriority.mxeJupyterhubHub.priorityClassName | toString -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{- define "eric-mxe-jupyterhub.proxy.podPriority" -}}
{{- if .Values.podPriority -}}
  {{- if .Values.podPriority.mxeJupyterhubProxy -}}
    {{- .Values.podPriority.mxeJupyterhubProxy.priorityClassName | toString -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{/*
Added below to handle DR-D1123-123 Coordinated Values for fsGroup Shall be Used
*/}}
{{- define "eric-mxe-jupyterhub.fsGroup.coordinated" -}}
    {{- if .Values.global -}}
        {{- if .Values.global.fsGroup -}}
            {{- if .Values.global.fsGroup.manual -}}
                {{ .Values.global.fsGroup.manual }}
            {{- else -}}
                {{- if .Values.global.fsGroup.namespace -}}
                  {{- if eq .Values.global.fsGroup.namespace true -}}
                  {{/* The 'default' defined in the Security Policy will be used. */}}
                  {{- else -}}
                      10000
                  {{- end -}}
                {{- else -}}
                    10000
                {{- end -}}
            {{- end -}}
        {{- else -}}
            10000
        {{- end -}}
    {{- else -}}
        10000
    {{- end -}}
{{- end -}}

{{/*
adding security context
*/}}
{{- define "eric-mxe-jupyterhub.usercontainer-security-context" -}}
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
Istio sidecar annotations - egress volume mounts
*/}}
{{- define "eric-mxe-jupyterhub.istio-sidecar-annotations-egress-mounts" -}}
{{- if .Values.global.serviceMesh.enabled -}}
sidecar.istio.io/userVolume: {{ include "eric-mxe-jupyterhub.istio-sidecar-annotations-user-volume" . | squote }}
sidecar.istio.io/userVolumeMount: {{ include "eric-mxe-jupyterhub.istio-sidecar-annotations-volume-mounts" . | squote }}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - user volume
*/}}
{{- define "eric-mxe-jupyterhub.istio-sidecar-annotations-user-volume" -}}
{{- printf "{" -}}
{{- include "eric-mxe-jupyterhub.istio-sidecar-annotations-user-volume-egress" . -}},
{{- include "eric-mxe-jupyterhub.istio-sidecar-annotations-user-volume-egress-iam" . -}},
{{- include "eric-mxe-jupyterhub.istio-sidecar-annotations-user-volume-egress-lcm" . -}},
{{- include "eric-mxe-jupyterhub.istio-sidecar-annotations-user-volume-egress-pm" . }}
{{- printf "}" -}}
{{- end }}


{{/*
Istio sidecar annotations - volume mounts - egress
*/}}
{{- define "eric-mxe-jupyterhub.istio-sidecar-annotations-user-volume-egress" -}}
{{- with .Values.global.serviceMesh.egress.ca }}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\"}}" .secretName .genSecretName -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress iam
*/}}
{{- define "eric-mxe-jupyterhub.istio-sidecar-annotations-user-volume-egress-iam" -}}
{{- with .Values.global.serviceMesh.egress.iam }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress pm
*/}}
{{- define "eric-mxe-jupyterhub.istio-sidecar-annotations-user-volume-egress-pm" -}}
{{- with .Values.global.serviceMesh.egress.pmServer }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress lcm
*/}}
{{- define "eric-mxe-jupyterhub.istio-sidecar-annotations-user-volume-egress-lcm" -}}
{{- with .Values.global.serviceMesh.egress.lcmRegistry }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - user volume mounts
*/}}
{{- define "eric-mxe-jupyterhub.istio-sidecar-annotations-volume-mounts" }}
{{- printf "{" -}}
{{- include "eric-mxe-jupyterhub.istio-sidecar-annotations-volume-mounts-egress" . -}},
{{- include "eric-mxe-jupyterhub.istio-sidecar-annotations-volume-mounts-egress-iam" . -}},
{{- include "eric-mxe-jupyterhub.istio-sidecar-annotations-volume-mounts-egress-lcm" . -}},
{{- include "eric-mxe-jupyterhub.istio-sidecar-annotations-volume-mounts-egress-pm" . -}}
{{- printf "}" -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress
*/}}
{{- define "eric-mxe-jupyterhub.istio-sidecar-annotations-volume-mounts-egress" -}}
{{- with .Values.global.serviceMesh.egress.ca }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .caCertsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress iam
*/}}
{{- define "eric-mxe-jupyterhub.istio-sidecar-annotations-volume-mounts-egress-iam" -}}
{{- with .Values.global.serviceMesh.egress.iam }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress lcm
*/}}
{{- define "eric-mxe-jupyterhub.istio-sidecar-annotations-volume-mounts-egress-lcm" -}}
{{- with .Values.global.serviceMesh.egress.lcmRegistry }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress pm server
*/}}
{{- define "eric-mxe-jupyterhub.istio-sidecar-annotations-volume-mounts-egress-pm" -}}
{{- with .Values.global.serviceMesh.egress.pmServer }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Standard labels
*/}}
{{- define "eric-mxe-jupyterhub.standard-labels" -}}
app.kubernetes.io/part-of: "mxe"
app.kubernetes.io/instance: {{ .Release.Name | quote }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
helm.sh/chart: {{ include "eric-mxe-jupyterhub.chart" . }}
{{- end -}}

{{/*
Create a user defined label
*/}}
{{ define "eric-mxe-jupyterhub.config-labels" }}
  {{- $global := (.Values.global).labels -}}
  {{- $service := .Values.labels -}}
  {{- $additional := include "eric-mxe-jupyterhub.additionalLabels" . | fromYaml -}}
  {{- include "eric-mxe-jupyterhub.mergeLabels" (dict "location" .Template.Name "sources" (list $global $service $additional)) }}
{{- end }}

{{/*
Merged labels for Default, which includes Standard and Config
*/}}
{{- define "eric-mxe-jupyterhub.labels" -}}
  {{- $standard := include "eric-mxe-jupyterhub.standard-labels" . | fromYaml -}}
  {{- $config := include "eric-mxe-jupyterhub.config-labels" . | fromYaml -}}
  {{- include "eric-mxe-jupyterhub.mergeLabels" (dict "location" .Template.Name "sources" (list $standard $config)) | trim }}
{{- end -}}

{{/*
Create a user defined annotation
*/}}
{{ define "eric-mxe-jupyterhub.config-annotations" }}
  {{- $global := (.Values.global).annotations -}}
  {{- $service := .Values.annotations -}}
  {{- include "eric-mxe-jupyterhub.mergeAnnotations" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged annotations for Default, which includes productInfo and config
*/}}
{{- define "eric-mxe-jupyterhub.annotations" -}}
  {{- $productInfo := include "eric-mxe-jupyterhub.product-info" . | fromYaml -}}
  {{- $config := include "eric-mxe-jupyterhub.config-annotations" . | fromYaml -}}
  {{- include "eric-mxe-jupyterhub.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $config)) | trim }}
{{- end -}}

{{/*
Create a map from ".Values.global" with defaults if missing in values file.
This hides defaults from values file.
*/}}
{{ define "eric-mxe-jupyterhub.global" }}
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
{{ define "eric-mxe-jupyterhub.hub-nodeSelector" }}
{{- $g := fromJson (include "eric-mxe-jupyterhub.global" .) -}}
{{- $global := $g.nodeSelector -}}
{{- $serviceHub := .Values.hub.nodeSelector -}}
{{- $service := .Values.nodeSelector -}}
{{- include "eric-mxe-jupyterhub.aggregatedMerge" (dict "context" "nodeSelector" "location" .Template.Name "sources" (list $global $service $serviceHub)) -}}
{{ end }}

{{ define "eric-mxe-jupyterhub.proxy-nodeSelector" }}
{{- $g := fromJson (include "eric-mxe-jupyterhub.global" .) -}}
{{- $global := $g.nodeSelector -}}
{{- $proxyChpService := .Values.proxy.chp.nodeSelector -}}
{{- $service := .Values.nodeSelector -}}
{{- include "eric-mxe-jupyterhub.aggregatedMerge" (dict "context" "nodeSelector" "location" .Template.Name "sources" (list $global $service $proxyChpService)) -}}
{{ end }}

{{ define "eric-mxe-jupyterhub.singleuser-nodeSelector" }}
{{- $g := fromJson (include "eric-mxe-jupyterhub.global" .) -}}
{{- $global := $g.nodeSelector -}}
{{- $service := .Values.singleuser.nodeSelector -}}
{{- include "eric-mxe-jupyterhub.aggregatedMerge" (dict "context" "nodeSelector" "location" .Template.Name "sources" (list $global $service)) -}}
{{ end }}

{{/*
Added below to handle DR-D1121-102 for globalRegistryPullPolicy Shall be Used
*/}}
{{- define "eric-mxe-jupyterhub.registryImagePullPolicy" -}}
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

{{- define "eric-mxe-jupyterhub.proxy-deployment-annotations" -}}
{{- /* 
# We want to restart proxy only if the auth token changes
# Other changes to the hub config should not restart.
# We truncate to 4 chars to avoid leaking auth token info,
# since someone could brute force the hash to obtain the token
#
# Note that if auth_token has to be generated at random, it will be
# generated at random here separately from being generated at random in
# the k8s Secret template. This will cause this annotation to change to
# match the k8s Secret during the first upgrade following an auth_token
# was generated.
*/}}
checksum/auth-token: {{ include "eric-mxe-jupyterhub.hub.config.ConfigurableHTTPProxy.auth_token" . | sha256sum | trunc 4 | quote }}
{{- if .Values.appArmorProfile.type }}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s" "container.apparmor.security.beta.kubernetes.io/chp" }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{- define "eric-mxe-jupyterhub.proxy-deployment-labels" -}}
app.kubernetes.io/component: "jupyterhub-proxy"
app.kubernetes.io/name: {{ include "eric-mxe-jupyterhub.proxy-name" . | quote }}
hub.jupyter.org/network-access-hub: "true"
hub.jupyter.org/network-access-singleuser: "true"
{{- end -}}

{{- define "eric-mxe-jupyterhub.hub-deployment-annotations" -}}
{{- /* This lets us autorestart when the secret changes! */}}
checksum/config-map: {{ include (print .Template.BasePath "/hub/configmap.yaml") . | sha256sum }}
checksum/secret: {{ include (print .Template.BasePath "/hub/secret.yaml") . | sha256sum }}
{{- if .Values.global.serviceMesh.enabled }}
traffic.sidecar.istio.io/excludeOutboundPorts: "8888"
{{- end }}
{{- if .Values.appArmorProfile.type }}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s" "container.apparmor.security.beta.kubernetes.io/hub" }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{- define "eric-mxe-jupyterhub.hub-deployment-labels" -}}
app.kubernetes.io/component: "jupyterhub-hub"
app.kubernetes.io/name: {{ include "eric-mxe-jupyterhub.hub-name" . | quote }}
hub.jupyter.org/network-access-proxy-api: "true"
hub.jupyter.org/network-access-proxy-http: "true"
hub.jupyter.org/network-access-singleuser: "true"
{{- end -}}

{{- define "eric-mxe-jupyterhub.ingress-annotations" -}}
{{ include "eric-mxe-jupyterhub.ingress-annotation-prefix" . }}/default-backend: {{ include "eric-mxe-jupyterhub.defaultbackend" . | quote }}
{{ include "eric-mxe-jupyterhub.ingress-annotation-prefix" . }}/service-upstream: "true"
{{ include "eric-mxe-jupyterhub.ingress-annotation-prefix" . }}/upstream-vhost: {{ include "eric-mxe-jupyterhub.proxy-service-public-name" . | quote }}
{{ include "eric-mxe-jupyterhub.ingress-annotation-prefix" . }}/proxy-body-size: "0"
{{- end -}}

{{- define "eric-mxe-jupyterhub.ericProdInfoRepoPath" }}
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

{{- define "eric-mxe-jupyterhub.ericProdInfoRegistry" }}
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
eric-mxe-jupyterhub image name path
*/}}
{{- define "eric-mxe-jupyterhub.setImageRepo" }}
  {{- $top := index . 0 }}
  {{- $serviceLevelImageRegistry := index . 1 }}
  {{- $serviceLevelImageRepoPath := index . 2 }}
  {{- $imageName := index . 3 }}

  {{- /*
  Registry and Repopath for the specified image container is fetched from eric-product-info.yaml
  */}}
  {{- $productInfo := fromYaml ($top.Files.Get "eric-product-info.yaml") -}}
  {{- $registryUrl := include "eric-mxe-jupyterhub.ericProdInfoRegistry" (list $productInfo $imageName) -}}
  {{- $imageRepoPath := include "eric-mxe-jupyterhub.ericProdInfoRepoPath" (list $productInfo $imageName) -}}

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
{{- define "eric-mxe-jupyterhub.prometheus-annotations" -}}
prometheus.io/path: {{ .Values.global.mxePrometheusPath | quote }}
prometheus.io/port: {{ .Values.global.mxePrometheusPort | quote }}
prometheus.io/scrape: {{ .Values.global.mxePrometheusScrape | quote }}
prometheus.io/scrape-role: {{ .Values.global.mxePrometheusScrapeRole | quote }}
prometheus.io/scrape-interval: {{ .Values.global.mxePrometheusScrapeInterval | quote }}
{{- end -}}
{{/*
Merge global tolerations with service tolerations (DR-D1120-061-AD).
*/}}
{{- define "eric-mxe-jupyterhub.merge-tolerations" -}}
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
        {{- $globalItemId := include "eric-mxe-jupyterhub.merge-tolerations.get-identifier" $globalItem -}}
        {{- range $serviceItem := $serviceTolerations -}}
          {{- $serviceItemId := include "eric-mxe-jupyterhub.merge-tolerations.get-identifier" $serviceItem -}}
          {{- if eq $serviceItemId $globalItemId -}}
            {{- $matchingItems = append $matchingItems $serviceItem -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
      {{- range $globalItem := $globalTolerations -}}
        {{- $globalItemId := include "eric-mxe-jupyterhub.merge-tolerations.get-identifier" $globalItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-jupyterhub.merge-tolerations.get-identifier" $matchItem -}}
          {{- if eq $matchItemId $globalItemId -}}
            {{- $matchCount = add1 $matchCount -}}
          {{- end -}}
        {{- end -}}
        {{- if eq $matchCount 0 -}}
          {{- $nonMatchingItems = append $nonMatchingItems $globalItem -}}
        {{- end -}}
      {{- end -}}
      {{- range $serviceItem := $serviceTolerations -}}
        {{- $serviceItemId := include "eric-mxe-jupyterhub.merge-tolerations.get-identifier" $serviceItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-jupyterhub.merge-tolerations.get-identifier" $matchItem -}}
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
{{ define "eric-mxe-jupyterhub.merge-tolerations.get-identifier" }}
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
{{- define "eric-mxe-jupyterhub.pg-client-cert" -}}
{{ .Values.global.serviceMesh.egress.postgres.clientCertificate }}
{{- end -}}

{{/*
postgres certificate private key
*/}}
{{- define "eric-mxe-jupyterhub.pg-private-key" -}}
{{ .Values.global.serviceMesh.egress.postgres.privateKey }}
{{- end -}}

{{/*
postgres certificate ca
*/}}
{{- define "eric-mxe-jupyterhub.pg-ca-issuer" -}}
{{ .Values.global.serviceMesh.egress.postgres.caIssuer }}
{{- end -}}

{{/*
pg-cert - volume mounts
*/}}
{{- define "eric-mxe-jupyterhub.pg-cert-volume-mounts" -}}
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
proxy pg-cert -  volumes
*/}}
{{- define "eric-mxe-jupyterhub-proxy.pg-cert-volumes" -}}
{{- if .Values.global.security.tls.enabled }}
- name: trusted-ca
  secret:
    secretName: eric-sec-sip-tls-trusted-root-cert
- name: pg-cert
  secret:
    secretName: {{ include "eric-mxe-jupyterhub.proxy-name" . }}-pg-cert
{{- end -}}
{{- end -}}

{{/*
hub pg-cert -  volumes
*/}}
{{- define "eric-mxe-jupyterhub-hub.pg-cert-volumes" -}}
{{- if .Values.global.security.tls.enabled }}
- name: trusted-ca
  secret:
    secretName: eric-sec-sip-tls-trusted-root-cert
- name: pg-cert
  secret:
    secretName: {{ include "eric-mxe-jupyterhub.hub-name" . }}-pg-cert
{{- end -}}
{{- end -}}
