{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-mxe-model-catalogue-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-mxe-model-catalogue-service.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create default backend name.
*/}}
{{- define "eric-mxe-model-catalogue-service.defaultbackend" -}}
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
{{- define "eric-mxe-model-catalogue-service.ingress-annotation-prefix" -}}
{{- if .Values.ingress.annotationPrefix -}}
{{- .Values.ingress.annotationPrefix -}}
{{- else -}}
mxe.nginx.ingress.kubernetes.io
{{- end -}}
{{- end -}}

{{- define "eric-mxe-model-catalogue-service.ingress-secured-annotations" -}}
{{ include "eric-mxe-model-catalogue-service.ingress-annotation-prefix" . }}/default-backend: {{ include "eric-mxe-model-catalogue-service.defaultbackend" . | quote }}
{{- if .Values.ingress.owasp.enabled }}
{{ include "eric-mxe-model-catalogue-service.ingress-annotation-prefix" . }}/enable-modsecurity: "true"
{{ include "eric-mxe-model-catalogue-service.ingress-annotation-prefix" . }}/modsecurity-snippet: |
  SecRule REQUEST_URI "@beginsWith /v1/models/" "id:10002,phase:1,pass,chain"
    SecRule REQUEST_METHOD "DELETE" "nolog,ctl:ruleRemoveById=911100"
  Include /etc/nginx/owasp-modsecurity-crs/nginx-modsecurity.conf
  SecRuleEngine On
{{- end }}
{{ include "eric-mxe-model-catalogue-service.ingress-annotation-prefix" . }}/service-upstream: "true"
{{ include "eric-mxe-model-catalogue-service.ingress-annotation-prefix" . }}/upstream-vhost: {{ include "eric-mxe-model-catalogue-service.name" . | quote }}
{{- end -}}

{{- define "eric-mxe-model-catalogue-service.ingress-annotations" -}}
{{ include "eric-mxe-model-catalogue-service.ingress-annotation-prefix" . }}/default-backend: {{ include "eric-mxe-model-catalogue-service.defaultbackend" . | quote }}
{{ include "eric-mxe-model-catalogue-service.ingress-annotation-prefix" . }}/proxy-body-size: "20G"
{{ include "eric-mxe-model-catalogue-service.ingress-annotation-prefix" . }}/proxy-request-buffering: "off"
{{ include "eric-mxe-model-catalogue-service.ingress-annotation-prefix" . }}/service-upstream: "true"
{{ include "eric-mxe-model-catalogue-service.ingress-annotation-prefix" . }}/upstream-vhost: {{ include "eric-mxe-model-catalogue-service.name" . | quote }}
{{- end -}}

{{/*
Expand the service account of the chart.
*/}}
{{- define "eric-mxe-model-catalogue-service.serviceaccount" -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . -}}
{{- printf "%s-%s" $name "serviceaccount" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the role binding.
*/}}
{{- define "eric-mxe-model-catalogue-service.rolebinding" -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . -}}
{{- printf "%s-%s" $name "rolebinding" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the role.
*/}}
{{- define "eric-mxe-model-catalogue-service.role" -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . -}}
{{- printf "%s-%s" $name "role" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the host of model service.
*/}}
{{- define "eric-mxe-model-catalogue-service.model-service-host" -}}
{{- if .Values.modelService.hostOverride -}}
{{- .Values.modelService.hostOverride -}}
{{- else -}}
{{- printf "%s" "eric-mxe-model-service" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
Expand the host of author service.
*/}}
{{- define "eric-mxe-model-catalogue-service.author-service-host" -}}
{{- if .Values.authorService.hostOverride -}}
{{- .Values.authorService.hostOverride -}}
{{- else -}}
{{- printf "%s" "eric-mxe-author-service" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
Image pull secret name
*/}}
{{- define "eric-mxe-model-catalogue-service.image-pull-secret-name" -}}
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
Image pull secrets
*/}}
{{- define "eric-mxe-model-catalogue-service.image-pull-secrets" -}}
{{- $name := include "eric-mxe-model-catalogue-service.image-pull-secret-name" . -}}
{{- if $name -}}
imagePullSecrets:
  - name: {{ $name | quote -}}
{{- end -}}
{{- end -}}

{{/*
The secret name of the docker registry
*/}}
{{- define "eric-mxe-model-catalogue-service.docker-registry-secret-name" -}}
{{- if .Values.dockerRegistry.secretNameOverride -}}
{{- .Values.dockerRegistry.secretNameOverride -}}
{{- else -}}
{{ include "eric-mxe-model-catalogue-service.image-pull-secret-name" . }}
{{- end -}}
{{- end -}}

{{/*
Image registry
*/}}
{{- define "eric-mxe-model-catalogue-service.image-registry" -}}
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
{{- define "eric-mxe-model-catalogue-service.image-repository" -}}
{{- include "eric-mxe-model-catalogue-service.image-registry" . -}}
{{- printf "%s" .Values.imageCredentials.repoPath -}}
/
{{- end -}}

{{/*
Product information of Ericsson products (DR-D1121-064, DR-D1121-067)
*/}}
{{- define "eric-mxe-model-catalogue-service.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}

{{/*
Update strategy for deployments
*/}}
{{- define "eric-mxe-model-catalogue-service.update-strategy" -}}
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
{{- define "eric-mxe-model-catalogue-service.ingress-tls" -}}
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
Modified below to handle DR-D1121-120
*/}}
{{/*
The name of the custom db user's secret
*/}}
{{- define "eric-mxe-model-catalogue-service.db-custom-user-secret-name" -}}
{{- if .Values.database.customUserSecretFullNameOverride -}}
{{- .Values.database.customUserSecretFullNameOverride -}}
{{- else -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . -}}
{{- printf "%s-%s" $name "db-hooked" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
The name of the db
*/}}
{{- define "eric-mxe-model-catalogue-service.db-name" -}}
{{- if .Values.database.nameOverride -}}
{{- $name := .Values.database.nameOverride | quote -}}
{{- regexReplaceAll "[^a-zA-Z0-9_]" $name "" | trunc 63 -}}
{{- else -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . | quote -}}
{{- regexReplaceAll "[^a-zA-Z0-9_]" $name "" | trunc 63 -}}
{{- include "eric-mxe-model-catalogue-service.name" . | regexReplaceAll "[^a-zA-Z0-9_]" "" | trunc 63 -}}
{{- end -}}
{{- end -}}

{{/*
The user of the db
*/}}
{{- define "eric-mxe-model-catalogue-service.db-user-name" -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . | quote -}}
{{- regexReplaceAll "[^a-zA-Z0-9_]" $name "" | trunc 63 -}}
{{- end -}}

{{/*
The host of the docker registry
*/}}
{{- define "eric-mxe-model-catalogue-service.docker-registry-hostname" -}}
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

{{/*
The ENV variable of external registry's CA secret
*/}}
{{- define "eric-mxe-model-catalogue-service.installer-docker-registry-casecretname-env" -}}
{{- if .Values.installerDockerRegistry.caSecretName -}}
- name: INSTALLER_DOCKER_REGISTRY_CA_SECRET_NAME
  value: {{ .Values.installerDockerRegistry.caSecretName -}}
{{- end -}}
{{- end -}}


{{/*
Expand the name of the files' ingress.
*/}}
{{- define "eric-mxe-model-catalogue-service.ingress-file" -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . -}}
{{- printf "%s-%s" $name "ingress-file" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the secured ingress.
*/}}
{{- define "eric-mxe-model-catalogue-service.ingress-secured" -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . -}}
{{- printf "%s-%s" $name "ingress-secured" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the configmap of waiting for keycloak.
*/}}
{{- define "eric-mxe-model-catalogue-service.wait-for-keycloak-configmap" -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . -}}
{{- printf "%s-%s" $name "wait-for-keycloak-configmap" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the post install hook's configmap of the chart.
*/}}
{{- define "eric-mxe-model-catalogue-service.minio-configmap" -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . -}}
{{- printf "%s-%s" $name "post-install-hook-configmap" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the service's minio bucket of the chart.
*/}}
{{- define "eric-mxe-model-catalogue-service.model-source-minio-bucket" -}}
{{ printf "mxe-models-%s" "bucket" | lower }}
{{- end -}}

{{/*
Expand the secret of service's minio user name of the chart.
*/}}
{{- define "eric-mxe-model-catalogue-service.model-source-service-minio-user-secret-name" -}}
{{- if .Values.minio.userModelSourceServiceSecretName -}}
{{ .Values.minio.userModelSourceServiceSecretName }}
{{- else -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . -}}
{{- printf "%s-%s" $name "ms-service-minio-secret" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
Expand the service's minio user name of the chart.
*/}}
{{- define "eric-mxe-model-catalogue-service.model-source-service-minio-user-name" -}}
{{ printf "modelc-service-%s" "user" | lower }}
{{- end -}}

{{/*
Expand the secret of service's minio user name of the chart.
*/}}
{{- define "eric-mxe-model-catalogue-service.model-source-instance-minio-user-secret-name" -}}
{{- if .Values.minio.userModelSourceInstanceSecretName -}}
{{ .Values.minio.userModelSourceInstanceSecretName }}
{{- else -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . -}}
{{- printf "%s-%s" $name "ms-instance-minio-secret" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
Expand the service's minio user name of the chart.
*/}}
{{- define "eric-mxe-model-catalogue-service.model-source-instance-minio-user-name" -}}
{{ printf "modelc-instance-%s" "user" | lower }}
{{- end -}}

{{/*
The internal pypi server (DR-1121-061)
*/}}
{{- define "eric-mxe-model-catalogue-service.pypi-internal-server" -}}
{{- if .Values.pypiServer.internal -}}
{{ if contains "http://" .Values.pypiServer.internal }}
{{- printf "%s" .Values.pypiServer.internal -}}
{{ else }}
{{- printf "http://%s" .Values.pypiServer.internal -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
The external pypi server
*/}}
{{- define "eric-mxe-model-catalogue-service.pypi-external-server" -}}
{{- if .Values.pypiServer.external -}}
{{ if or (contains "https://" .Values.pypiServer.external) (contains "http://" .Values.pypiServer.external) }}
{{- printf "%s" .Values.pypiServer.external -}}
{{ else }}
{{- printf "https://%s" .Values.pypiServer.external -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations, labels
*/}}
{{- define "eric-mxe-model-catalogue-service.istio-sidecar-annotations" -}}
sidecar.istio.io/rewriteAppHTTPProbers: {{ .Values.global.serviceMesh.enabled | quote }}
{{ include "eric-mxe-model-catalogue-service.istio-sidecar-annotations-egress-mounts" . }}
{{- end -}}

{{- define "eric-mxe-model-catalogue-service.istio-sidecar-labels" -}}
sidecar.istio.io/inject: {{ .Values.global.serviceMesh.enabled | quote }}
{{- end -}}

{{/*
Prometheus annotations
*/}}
{{- define "eric-mxe-model-catalogue-service.prometheus-annotations" -}}
prometheus.io/path: {{ .Values.global.mxePrometheusPath | quote }}
prometheus.io/port: {{ .Values.global.mxePrometheusPort | quote }}
prometheus.io/scrape: {{ .Values.global.mxePrometheusScrape | quote }}
prometheus.io/scrape-role: {{ .Values.global.mxePrometheusScrapeRole | quote }}
prometheus.io/scrape-interval: {{ .Values.global.mxePrometheusScrapeInterval | quote }}
{{- end -}}

{{/*
 Oauth Mxe Api host path url
*/}}
{{- define "eric-mxe-model-catalogue-service.oauth-api-host-path-url" -}}
{{- if eq (.Values.global.mxeApiport | int64) 443 -}}
{{- printf "https://%s" .Values.global.mxeOauthApiHostname -}}
{{- else -}}
{{- printf "https://%s:%v" .Values.global.mxeOauthApiHostname .Values.global.mxeApiport -}}
{{- end -}}
{{- end -}}

{{/*
model-catalogue-service - secret containing root ca for oauth hostname 
*/}}
{{- define "eric-mxe-model-catalogue-service.iam-ca-cert-name" -}}
{{- .Values.global.mxeIamCaSecretName -}}
{{- end -}}

{{/*
mxe-model-catalogue-service - jdbc params
*/}}
{{- define "eric-mxe-model-catalogue-service.jdbc-params" -}}
{{- if .Values.global.serviceMesh.enabled -}}
{{- printf "?ssl=true&sslmode=verify-full&sslcert=/tmp/siptls/postgresql.crt&sslkey=/tmp/siptls/postgresql.pk8&sslrootcert=/run/secrets/certificates/trusted/ca.crt" -}}
{{ else }}
{{ "" }}
{{- end -}}
{{- end -}}

{{/*
mxe-model-catalogue-service - service mesh volume mounts
*/}}
{{- define "eric-mxe-model-catalogue-service.service-mesh-volume-mounts" -}}
- mountPath: /home/mxe/.mc/
  name: mxe-mc-home
{{- if .Values.global.serviceMesh.enabled }}
- name: mxemodelcatalogueservice-pg-cert
  mountPath: /run/secrets/certificates/client/postgres
  readOnly: true
- name: trusted-ca
  mountPath: /run/secrets/certificates/trusted
  readOnly: true
{{- end -}}
{{- end -}}

{{/*
mxe-model-catalogue-service - service mesh volumes
*/}}
{{- define "eric-mxe-model-catalogue-service.service-mesh-volumes" -}}
- name: dbinit-postgres-sql-home
  emptyDir: {}
- name: mxe-mc-home
  emptyDir: {}
{{- if .Values.global.serviceMesh.enabled }}
- name: postgres-cert
  secret:
    secretName: eric-data-document-database-pg-postgres-cert
- name: trusted-ca
  secret:
    secretName: eric-sec-sip-tls-trusted-root-cert
- name: mxemodelcatalogueservice-pg-cert
  secret:
    secretName: {{ include "eric-mxe-model-catalogue-service.name" . }}-pg-cert
- name: kubernetes-run-script
  configMap:
    name: {{ include "eric-mxe-model-catalogue-service.name" . }}-certificate-sidecar-configmap
{{- end -}}
{{- end -}}

{{/*
Define the name of the credentials secret for internal container registry
*/}}
{{- define "eric-mxe-model-catalogue-service.internal-container-registry-secret-name" -}}
{{- printf "%s" "mxe-internal-registry-creds" -}}
{{- end -}}

{{/*
Define the name of the dockerfile for the model packaging job
*/}}
{{- define "eric-mxe-model-catalogue-service.model-source-docker-37-configmap" -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . -}}
{{- printf "%s-%s" $name "model-dockerfile-python37" | trunc 63 | trimSuffix "-" -}}
{{/*- printf "%s" "model-dockerfile-python37" | trunc 63 | trimSuffix "-" -*/}}
{{- end -}}

{{/*
Define the name of the configmap for storing logcontrol.json file
*/}}
{{- define "eric-mxe-model-catalogue-service.log-control-configmap" -}}
{{- .Values.global.mxeLogControlConfigMap -}}
{{- end -}}


{{/*
Added below to handle DR-D1123-123 Coordinated Values for fsGroup Shall be Used
*/}}

{{- define "eric-mxe-model-catalogue-service.fsGroup.coordinated" -}}
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
model catalog-service - postgres certificate name
*/}}
{{- define "eric-mxe-model-catalogue-service.pg-client-cert" -}}
{{ .Values.global.serviceMesh.egress.postgres.clientCertificate }}
{{- end -}}

{{/*
model catalog-service - postgres certificate private key
*/}}
{{- define "eric-mxe-model-catalogue-service.pg-private-key" -}}
{{ .Values.global.serviceMesh.egress.postgres.privateKey }}
{{- end -}}

{{/*
model catalog-service - postgres certificate ca
*/}}
{{- define "eric-mxe-model-catalogue-service.pg-ca-issuer" -}}
{{ .Values.global.serviceMesh.egress.postgres.caIssuer }}
{{- end -}}

{{/*
Istio sidecar annotations - egress volume mounts
*/}}
{{- define "eric-mxe-model-catalogue-service.istio-sidecar-annotations-egress-mounts" -}}
{{- if .Values.global.serviceMesh.enabled -}}
sidecar.istio.io/userVolume: {{ include "eric-mxe-model-catalogue-service.istio-sidecar-annotations-user-volume" . | squote }}
sidecar.istio.io/userVolumeMount: {{ include "eric-mxe-model-catalogue-service.istio-sidecar-annotations-volume-mounts" . | squote }}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - user volume
*/}}
{{- define "eric-mxe-model-catalogue-service.istio-sidecar-annotations-user-volume" -}}
{{- printf "{" -}}
{{- include "eric-mxe-model-catalogue-service.istio-sidecar-annotations-user-volume-egress" . -}},
{{- include "eric-mxe-model-catalogue-service.istio-sidecar-annotations-user-volume-egress-iam" . -}},
{{- include "eric-mxe-model-catalogue-service.istio-sidecar-annotations-user-volume-egress-lcm" . -}},
{{- include "eric-mxe-model-catalogue-service.istio-sidecar-annotations-user-volume-egress-pm" . }}
{{- printf "}" -}}
{{- end }}


{{/*
Istio sidecar annotations - volume mounts - egress
*/}}
{{- define "eric-mxe-model-catalogue-service.istio-sidecar-annotations-user-volume-egress" -}}
{{- with .Values.global.serviceMesh.egress.ca }}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\"}}" .secretName .genSecretName -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress iam
*/}}
{{- define "eric-mxe-model-catalogue-service.istio-sidecar-annotations-user-volume-egress-iam" -}}
{{- with .Values.global.serviceMesh.egress.iam }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress pm
*/}}
{{- define "eric-mxe-model-catalogue-service.istio-sidecar-annotations-user-volume-egress-pm" -}}
{{- with .Values.global.serviceMesh.egress.pmServer }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress lcm
*/}}
{{- define "eric-mxe-model-catalogue-service.istio-sidecar-annotations-user-volume-egress-lcm" -}}
{{- with .Values.global.serviceMesh.egress.lcmRegistry }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - user volume mounts
*/}}
{{- define "eric-mxe-model-catalogue-service.istio-sidecar-annotations-volume-mounts" }}
{{- printf "{" -}}
{{- include "eric-mxe-model-catalogue-service.istio-sidecar-annotations-volume-mounts-egress" . -}},
{{- include "eric-mxe-model-catalogue-service.istio-sidecar-annotations-volume-mounts-egress-iam" . -}},
{{- include "eric-mxe-model-catalogue-service.istio-sidecar-annotations-volume-mounts-egress-lcm" . -}},
{{- include "eric-mxe-model-catalogue-service.istio-sidecar-annotations-volume-mounts-egress-pm" . -}}
{{- printf "}" -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress
*/}}
{{- define "eric-mxe-model-catalogue-service.istio-sidecar-annotations-volume-mounts-egress" -}}
{{- with .Values.global.serviceMesh.egress.ca }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .caCertsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress iam
*/}}
{{- define "eric-mxe-model-catalogue-service.istio-sidecar-annotations-volume-mounts-egress-iam" -}}
{{- with .Values.global.serviceMesh.egress.iam }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress lcm
*/}}
{{- define "eric-mxe-model-catalogue-service.istio-sidecar-annotations-volume-mounts-egress-lcm" -}}
{{- with .Values.global.serviceMesh.egress.lcmRegistry }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress pm server
*/}}
{{- define "eric-mxe-model-catalogue-service.istio-sidecar-annotations-volume-mounts-egress-pm" -}}
{{- with .Values.global.serviceMesh.egress.pmServer }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Database init - env
*/}}
{{- define "eric-mxe-model-catalogue-service.db-init-env" -}}
{{- if .Values.global.serviceMesh.enabled -}}
- name: PGSSLROOTCERT
  value: {{ " /home/dbinit/.postgresql/ca.crt" }}
- name: PGSSLCERT
  value: {{ " /home/dbinit/.postgresql/" }}{{ include "eric-mxe-model-catalogue-service.pg-client-cert" . }}
- name: PGSSLKEY
  value: {{ " /home/dbinit/.postgresql/" }}{{ include "eric-mxe-model-catalogue-service.pg-private-key" . }}
- name: PGSSLMODE
  value: {{ "require" }}
{{- end -}}
{{- end -}}

{{/*
Database init - volume mounts
*/}}
{{- define "eric-mxe-model-catalogue-service.db-init-volume-mounts" -}}
volumeMounts:
- mountPath: /home/dbinit/.postgresql
  name: dbinit-postgres-sql-home
{{- if .Values.global.serviceMesh.enabled }}
- name: postgres-cert
  mountPath: /run/secrets/certificates/client/postgres
  readOnly: true
- name: trusted-ca
  mountPath: /run/secrets/certificates/trusted
  readOnly: true
{{- end -}}
{{- end -}}

{{/*
Database init - args command
*/}}
{{- define "eric-mxe-model-catalogue-service.db-init-args-command" -}}
{{- if .Values.global.serviceMesh.enabled -}}
command: ["sh", "-c", "mkdir -p /home/dbinit/.postgresql && cp /run/secrets/certificates/client/postgres/* /home/dbinit/.postgresql && cp /run/secrets/certificates/trusted/* /home/dbinit/.postgresql && chmod 400 /home/dbinit/.postgresql/* && /home/dbinit/db-init.sh"]
{{- end -}}
{{- end -}}

{{/*
Minio init - volume mounts
*/}}
{{- define "eric-mxe-model-catalogue-service.minio-init-volume-mounts" -}}
- mountPath: /home/mxe/.mc/
  name: mxe-mc-home
{{- if .Values.global.serviceMesh.enabled }}
- name: trusted-ca
  mountPath: /run/secrets/certificates/trusted
  readOnly: true
{{- end -}}
{{- end -}}

{{/*
Minio init - minio url
*/}}
{{- define "eric-mxe-model-catalogue-service.minio-url" -}}
{{- if .Values.global.serviceMesh.enabled -}}
{{ .Values.minio.https.url }}
{{ else }}
{{ .Values.minio.http.url }}
{{- end -}}
{{- end -}}


{{/*
 Model catalog service - Egress CA
*/}}
{{- define "eric-mxe-model-catalogue-service.egress-ca-cert" -}}
{{ .Values.global.serviceMesh.egress.ca.caCertsPath}}{{ .Values.global.serviceMesh.egress.ca.caCertificates}}
{{- end -}}

{{- define "eric-mxe-model-catalogue-service.podPriority" -}}
{{- if .Values.podPriority -}}
  {{- if .Values.podPriority.modelCatalogueService -}}
    {{- .Values.podPriority.modelCatalogueService.priorityClassName | toString -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{/*
eric-mxe-model-catalogue-service - mtls enabled - init certs
*/}}
{{- define "eric-mxe-model-catalogue-service.init-certs-mtls" -}}
{{- if .Values.global.serviceMesh.enabled -}}
mkdir /tmp/siptls
openssl x509 -outform der -in /run/secrets/certificates/client/postgres/clicert.pem -out "/tmp/siptls/postgresql.crt"
openssl pkcs8 -topk8 -inform PEM -in /run/secrets/certificates/client/postgres/cliprivkey.pem -outform DER -out "/tmp/siptls/postgresql.pk8" -v1 PBE-MD5-DES -nocrypt
{{- end -}}
{{- end -}}

{{/*
eric-mxe-model-catalogue-service - mtls enabled - init certs - volume mounts
*/}}
{{- define "eric-mxe-model-catalogue-service.init-certs-mtls-volume-mounts" -}}
{{- if .Values.global.serviceMesh.enabled -}}
- name: mxemodelcatalogueservice-pg-cert
  mountPath: /run/secrets/certificates/client/postgres
{{- end -}}
{{- end -}}

{{/*
Add External CA - Args command
*/}}
{{- define "eric-mxe-model-catalogue-service.add-externalca-command" -}}
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
{{- define "eric-mxe-model-catalogue-service.add-externalca-volume-mounts" -}}
{{- if .Values.isExtCA -}}
- mountPath: /usr/share/pki/trust/anchors/extca.crt
  name: iam-ca-cert
  subPath: ca.crt
{{- end -}}   
{{- end -}}

{{/*
Add External CA - volumes
*/}}
{{- define "eric-mxe-model-catalogue-service.add-externalca-volumes" -}}
{{- if .Values.isExtCA -}}
- name: iam-ca-cert
  secret:
    defaultMode: 420
    secretName: iam-ca-cert
{{- end -}}
{{- end -}}

{{- define "eric-mxe-model-catalogue-service.certificate-sidecar-run-command" -}}
{{- printf "/usr/bin/openssl x509 -outform der -in ${SOURCE}/clicert.pem -out ${DESTINATION}/postgresql.crt;/usr/bin/openssl pkcs8 -topk8 -inform PEM -in ${SOURCE}/cliprivkey.pem -outform DER -out ${DESTINATION}/postgresql.pk8 -v1 PBE-MD5-DES -nocrypt;/bin/chmod 600 ${DESTINATION}/*" -}}
{{- end -}}

{{- define "eric-mxe-model-catalogue-service.certificate-sidecar-source" -}}
{{- printf "/run/secrets/certificates/client/postgres" -}}
{{- end -}}

{{- define "eric-mxe-model-catalogue-service.certificate-sidecar-destination" -}}
{{- printf "/tmp/siptls" -}}
{{- end -}}

{{/*
Config name to be used for certificate-sidecar.
*/}}
{{- define "eric-mxe-model-catalogue-service.certificate-sidecar-configmap-name" -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . -}}
{{- printf "%s-%s" $name "certificate-sidecar-configmap" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
adding security context
*/}}
{{- define "eric-mxe-model-catalogue-service.usercontainer-security-context" -}}
securityContext:
  allowPrivilegeEscalation: true
  capabilities:
    drop:
      - ALL
  runAsUser: 1111
  readOnlyRootFilesystem: true
  {{ if .Values.seccompProfile.type -}}
  seccompProfile:
    type: {{ .Values.seccompProfile.type }}
    {{ if eq .Values.seccompProfile.type "Localhost" -}}
    localhostProfile: {{ .Values.seccompProfile.localhostProfile }}
    {{- end -}}
  {{- end -}}
{{- end -}}

{{- define "eric-mxe-model-catalogue-service.minio-security-context" -}}
securityContext:
  readOnlyRootFilesystem: true
  allowPrivilegeEscalation: false
  capabilities:
    drop:
      - ALL
  privileged: false
  runAsNonRoot: true
  runAsUser: 1000
  {{ if .Values.seccompProfile.type -}}
  seccompProfile:
    type: {{ .Values.seccompProfile.type }}
    {{ if eq .Values.seccompProfile.type "Localhost" -}}
    localhostProfile: {{ .Values.seccompProfile.localhostProfile }}
    {{- end -}}
  {{- end -}}
{{- end -}}

{{- define "eric-mxe-model-catalogue-service.kubectl-security-context" -}}
securityContext:
  allowPrivilegeEscalation: false
  capabilities:
    drop:
      - ALL
  privileged: false
  readOnlyRootFilesystem: true
  runAsNonRoot: true
  runAsUser: 1000
  {{ if .Values.seccompProfile.type -}}
  seccompProfile:
    type: {{ .Values.seccompProfile.type }}
    {{ if eq .Values.seccompProfile.type "Localhost" -}}
    localhostProfile: {{ .Values.seccompProfile.localhostProfile }}
    {{- end -}}
  {{- end -}}
{{- end -}}

{{- define "eric-mxe-model-catalogue-service.main-security-context" -}}
securityContext:
  allowPrivilegeEscalation: false
  capabilities:
    drop:
      - ALL
  privileged: false
  {{ if .Values.seccompProfile.type -}}
  seccompProfile:
    type: {{ .Values.seccompProfile.type }}
    {{ if eq .Values.seccompProfile.type "Localhost" -}}
    localhostProfile: {{ .Values.seccompProfile.localhostProfile }}
    {{- end -}}
  {{- end -}}
{{- end -}}

{{- define "eric-mxe-model-catalogue-service.dbinit-security-context" -}}
securityContext:
  allowPrivilegeEscalation: false
  capabilities:
    drop:
      - ALL
  privileged: false
  readOnlyRootFilesystem: true
  runAsUser: 197776
  {{ if .Values.seccompProfile.type -}}
  seccompProfile:
    type: {{ .Values.seccompProfile.type }}
    {{ if eq .Values.seccompProfile.type "Localhost" -}}
    localhostProfile: {{ .Values.seccompProfile.localhostProfile }}
    {{- end -}}
  {{- end -}}
{{- end -}}

{{- define "eric-mxe-model-catalogue-service.init-security-context" -}}
securityContext:
  allowPrivilegeEscalation: false
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
The name of the Istio Service Entry
*/}}
{{- define "eric-mxe-model-catalogue-service.serviceEntry" -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . -}}
{{- printf "%s-%s" $name "se" | trunc 63 | trimSuffix "-" -}}
{{- end -}}


{{/*
The name of the Istio Destination Rule
*/}}
{{- define "eric-mxe-model-catalogue-service.destinationRule" -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . -}}
{{- printf "%s-%s" $name "dr" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
The name of the Istio Destination Rule
*/}}
{{- define "eric-mxe-model-catalogue-service.minio-destinationRule" -}}
{{- $name := include "eric-mxe-model-catalogue-service.name" . -}}
{{- printf "%s-%s" $name "minio-dr" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Standard labels of Helm and Kubernetes
*/}}
{{- define "eric-mxe-model-catalogue-service.standard-labels" -}}
app.kubernetes.io/part-of: "mxe"
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
helm.sh/chart: {{ include "eric-mxe-model-catalogue-service.chart" . | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
{{- end -}}

{{/*
Create a user defined label - [DR-D1121-068] [DR-D1121-060]
*/}}
{{ define "eric-mxe-model-catalogue-service.config-labels" }}
  {{- $global := (.Values.global).labels -}}
  {{- $service := .Values.labels -}}
  {{- include "eric-mxe-model-catalogue-service.mergeLabels" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged labels for Default, which includes Standard and Config - Used in all files
*/}}
{{- define "eric-mxe-model-catalogue-service.labels" -}}
  {{- $standard := include "eric-mxe-model-catalogue-service.standard-labels" . | fromYaml -}}
  {{- $config := include "eric-mxe-model-catalogue-service.config-labels" . | fromYaml -}}
  {{- include "eric-mxe-model-catalogue-service.mergeLabels" (dict "location" .Template.Name "sources" (list $standard $config)) | trim }}
{{- end -}}

{{/*
Create a user defined annotation - [DR-D1121-060]
*/}}
{{ define "eric-mxe-model-catalogue-service.config-annotations" }}
  {{- $global := (.Values.global).annotations -}}
  {{- $service := .Values.annotations -}}
  {{- include "eric-mxe-model-catalogue-service.mergeAnnotations" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged annotations for Default, which includes productInfo and config - Used in all files
*/}}
{{- define "eric-mxe-model-catalogue-service.annotations" -}}
  {{- $productInfo := include "eric-mxe-model-catalogue-service.product-info" . | fromYaml -}}
  {{- $config := include "eric-mxe-model-catalogue-service.config-annotations" . | fromYaml -}}
  {{- include "eric-mxe-model-catalogue-service.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $config)) | trim }}
{{- end -}}

{{/*
Security annotations
*/}}
{{- define "eric-mxe-model-catalogue-service.security-annotations" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name | trimSuffix "-" }}: {{ $appArmorProfile }}
{{ printf "%s/%s-%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name "init-db" }}: {{ $appArmorProfile }}
{{ printf "%s/%s-%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name "kubectl" }}: {{ $appArmorProfile }}
container.apparmor.security.beta.kubernetes.io/init-cacerts: {{ $appArmorProfile }}
container.apparmor.security.beta.kubernetes.io/minio-model-source-instance-config: {{ $appArmorProfile }}
container.apparmor.security.beta.kubernetes.io/minio-model-source-service-config: {{ $appArmorProfile }}
{{- if .Values.global.serviceMesh.enabled }}
{{ printf "%s/%s-%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name "certificate-sidecar" }}: {{ $appArmorProfile }}
{{- end }}
{{- end }}
{{- end -}}

{{/*
Create a map from ".Values.global" with defaults if missing in values file.
This hides defaults from values file.
*/}}
{{ define "eric-mxe-model-catalogue-service.global" }}
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
{{ define "eric-mxe-model-catalogue-service.nodeSelector" }}
{{- $g := fromJson (include "eric-mxe-model-catalogue-service.global" .) -}}
{{- $global := $g.nodeSelector -}}
{{- $service := .Values.nodeSelector -}}
{{- include "eric-mxe-model-catalogue-service.aggregatedMerge" (dict "context" "nodeSelector" "location" .Template.Name "sources" (list $global $service)) -}}
{{ end }}

{{/*
Added below to handle DR-D1121-102 for globalRegistryPullPolicy Shall be Used
*/}}
{{- define "eric-mxe-model-catalogue-service.registryImagePullPolicy" -}}
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
{{- define "eric-mxe-model-catalogue-service.topologySpreadConstraints" }}
{{- if .Values.topologySpreadConstraints }}
{{- range $config, $values := .Values.topologySpreadConstraints }}
- topologyKey: {{ $values.topologyKey }}
  maxSkew: {{ $values.maxSkew | default 1 }}
  whenUnsatisfiable: {{ $values.whenUnsatisfiable | default "ScheduleAnyway" }}
{{- end }}
{{- end }}
{{- end }}

{{- define "eric-mxe-model-catalogue-service.ericProdInfoRepoPath" }}
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

{{- define "eric-mxe-model-catalogue-service.ericProdInfoRegistry" }}
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
eric-mxe-model-catalogue-service image name path
*/}}
{{- define "eric-mxe-model-catalogue-service.setImageRepo" }}
  {{- $top := index . 0 }}
  {{- $serviceLevelImageRegistry := index . 1 }}
  {{- $serviceLevelImageRepoPath := index . 2 }}
  {{- $imageName := index . 3 }}

  {{- /*
  Registry and Repopath for the specified image container is fetched from eric-product-info.yaml
  */}}
  {{- $productInfo := fromYaml ($top.Files.Get "eric-product-info.yaml") -}}
  {{- $registryUrl := include "eric-mxe-model-catalogue-service.ericProdInfoRegistry" (list $productInfo $imageName) -}}
  {{- $imageRepoPath := include "eric-mxe-model-catalogue-service.ericProdInfoRepoPath" (list $productInfo $imageName) -}}

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

{{- define "eric-mxe-model-catalogue-service.remove-after-install" }}
mxe.ericsson.se/remove-after-install: "true"
{{- end -}}

{{- define "eric-mxe-model-catalogue-service.hook-weight" }}
{{- $val := . }}
helm.sh/hook-weight: {{ quote $val }}
{{- end -}}

{{- define "eric-mxe-model-catalogue-service.istio-annotations" }}
sidecar.istio.io/rewriteAppHTTPProbers: "false"
{{- end -}}

{{- define "eric-mxe-model-catalogue-service.istio-labels" }}
sidecar.istio.io/inject: "false"
{{- end -}}
{{/*
Merge global tolerations with service tolerations (DR-D1120-061-AD).
*/}}
{{- define "eric-mxe-model-catalogue-service.merge-tolerations" -}}
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
        {{- $globalItemId := include "eric-mxe-model-catalogue-service.merge-tolerations.get-identifier" $globalItem -}}
        {{- range $serviceItem := $serviceTolerations -}}
          {{- $serviceItemId := include "eric-mxe-model-catalogue-service.merge-tolerations.get-identifier" $serviceItem -}}
          {{- if eq $serviceItemId $globalItemId -}}
            {{- $matchingItems = append $matchingItems $serviceItem -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
      {{- range $globalItem := $globalTolerations -}}
        {{- $globalItemId := include "eric-mxe-model-catalogue-service.merge-tolerations.get-identifier" $globalItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-model-catalogue-service.merge-tolerations.get-identifier" $matchItem -}}
          {{- if eq $matchItemId $globalItemId -}}
            {{- $matchCount = add1 $matchCount -}}
          {{- end -}}
        {{- end -}}
        {{- if eq $matchCount 0 -}}
          {{- $nonMatchingItems = append $nonMatchingItems $globalItem -}}
        {{- end -}}
      {{- end -}}
      {{- range $serviceItem := $serviceTolerations -}}
        {{- $serviceItemId := include "eric-mxe-model-catalogue-service.merge-tolerations.get-identifier" $serviceItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-model-catalogue-service.merge-tolerations.get-identifier" $matchItem -}}
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
{{ define "eric-mxe-model-catalogue-service.merge-tolerations.get-identifier" }}
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