{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-mxe-gatekeeper.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-mxe-gatekeeper.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Image repository
*/}}
{{- define "eric-mxe-gatekeeper.image-repository" -}}
{{- if .Values.imageCredentials.registry.url -}}
{{- .Values.imageCredentials.registry.url -}}
{{- else -}}
{{- .Values.global.registry.url -}}
{{- end -}}
{{- printf "/%s/" .Values.imageCredentials.repoPath -}}
{{- end -}}

{{/*
The name of the db
*/}}
{{- define "eric-mxe-gatekeeper.db-name" -}}
{{- if .Values.database.nameOverride -}}
{{- $name := .Values.database.nameOverride | quote -}}
{{- regexReplaceAll "[^a-zA-Z0-9_]" $name "" | trunc 63 -}}
{{- else -}}
{{ .Values.database.name | quote }}
{{- end -}}
{{- end -}}

{{/*
Product information of Ericsson products (DR-D1121-064, DR-D1121-067)
*/}}
{{- define "eric-mxe-gatekeeper.product-info" -}}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end -}}

{{/*
   Kubernetes Availability and Resiliency Design Rule DR-D1120-060-AD
*/}}
{{- define "eric-mxe-gatekeeper.tolerations" -}}
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
Image pull secrets DR-D1123-115
*/}}
{{- define "eric-mxe-gatekeeper.image-pull-secrets" -}}
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
Expand the template of the gatekeeper login page
*/}}
{{- define "eric-mxe-gatekeeper.template" -}}
{{- $name := include "eric-mxe-gatekeeper.name" . -}}
{{- printf "%s-%s" $name "template" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the admin ingress of the chart.
*/}}
{{- define "eric-mxe-gatekeeper.ingress-admin" -}}
{{- $name := include "eric-mxe-gatekeeper.name" . -}}
{{- printf "%s-%s" $name "ingress-admin" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*changed to eric-mxe-gatekeeper-keycloak-access-creds-hooked from old name eric-sec-access-mgmt-creds-hooked*/}}
{{/*old name in mxe 2.3 & prior eric-sec-access-mgmt-creds-hooked*/}}
{{- define "eric-mxe-gatekeeper.secret-eric-sec-access-mgmt-creds" -}}
{{- $name := include "eric-mxe-gatekeeper.name" . -}}
{{- printf "%s-%s" $name "keycloak-access-creds-hooked" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the master realm auth secret of the chart.
*/}}
{{- define "eric-mxe-gatekeeper.secret-realm-master" -}}
{{- if .Values.keycloak.realm.master.secret.secretName -}}
{{- .Values.keycloak.realm.master.secret.secretName -}}
{{- else -}}
{{ include "eric-mxe-gatekeeper.secret-eric-sec-access-mgmt-creds" . }}
{{- end -}}
{{- end -}}

{{/*
Expand the mxe realm user secret of the chart.
new name eric-mxe-gatekeeper-sec-access-creds-realm-mxe from old eric-sec-access-mgmt-creds-realm-mxe
*/}}
{{- define "eric-mxe-gatekeeper.secret-realm-mxe" -}}
{{- if .Values.keycloak.realm.mxe.secret.secretName -}}
{{- .Values.keycloak.realm.mxe.secret.secretName -}}
{{- else -}}
{{- $name := include "eric-mxe-gatekeeper.name" . -}}
{{- printf "%s-%s" $name "sec-access-creds-realm-mxe" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
Expand the role of the chart.
*/}}
{{- define "eric-mxe-gatekeeper.role" -}}
{{- $name := include "eric-mxe-gatekeeper.name" . -}}
{{- printf "%s-%s" $name "role" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the role binding of the chart.
*/}}
{{- define "eric-mxe-gatekeeper.rolebinding" -}}
{{- $name := include "eric-mxe-gatekeeper.name" . -}}
{{- printf "%s-%s" $name "role-binding" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the config secret of the chart.
*/}}
{{- define "eric-mxe-gatekeeper.secret-config" -}}
{{- $name := include "eric-mxe-gatekeeper.name" . -}}
{{- printf "%s-%s" $name "secret-config" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the service account of the chart.
*/}}
{{- define "eric-mxe-gatekeeper.serviceaccount" -}}
{{- $name := include "eric-mxe-gatekeeper.name" . -}}
{{- printf "%s-%s" $name "service-account" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the configmap of keycloak updater.
*/}}
{{- define "eric-mxe-gatekeeper.keycloak-updater-configmap" -}}
{{- $name := include "eric-mxe-gatekeeper.name" . -}}
{{- printf "%s-%s" $name "keycloak-updater-configmap" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
The http path of keycloak.
*/}}
{{- define "eric-mxe-gatekeeper.keycloak-http-path" -}}
{{- .Values.keycloak.service.http.protocol -}}://{{- .Values.keycloak.service.http.name -}}
{{- if .Values.keycloak.service.http.port -}}
:{{- .Values.keycloak.service.http.port -}}
{{- end -}}
/{{- .Values.keycloak.service.path -}}
{{- end -}}

{{/*
The secure https path of keycloak.
*/}}
{{- define "eric-mxe-gatekeeper.keycloak-https-path" -}}
{{- .Values.keycloak.service.https.protocol -}}://{{- .Values.keycloak.service.https.name -}}.{{ .Release.Namespace }}.svc.cluster.local
{{- if .Values.keycloak.service.https.port -}}
:{{- .Values.keycloak.service.https.port -}}
{{- end -}}
/{{- .Values.keycloak.service.path -}}
{{- end -}}

{{/*
The path of keycloak.
*/}}
{{- define "eric-mxe-gatekeeper.keycloak-path" -}}
{{- if .Values.global.serviceMesh.enabled -}}
{{ include "eric-mxe-gatekeeper.keycloak-https-path" . }}
{{- else -}}
{{ include "eric-mxe-gatekeeper.keycloak-http-path" . }}
{{- end -}}
{{- end -}}

{{/*
The ingress port name
*/}}
{{- define "eric-mxe-gatekeeper.keycloak-ingress-port-name" -}}
{{- if .Values.global.serviceMesh.enabled -}}
https-tls
{{- else -}}
http
{{- end -}}
{{- end -}}

{{/*
Expand the host of ingress controller.
*/}}
{{- define "eric-mxe-gatekeeper.ingress-controller-host" -}}
{{- $name := default "eric-mxe-ingress-controller" .Values.ingressController.hostOverride -}}
{{- printf "%s" $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the temporal user creation setting of realm.
*/}}
{{- define "eric-mxe-gatekeeper.realm-mxe-temporal-user" -}}
{{- if .Values.keycloak.realm.mxe.secret.secretName -}}
false
{{- else -}}
{{- .Values.keycloak.realm.mxe.secret.temporalUser -}}
{{- end -}}
{{- end -}}

{{/*
Create name for the updater job of the keycloak realm
*/}}
{{- define "eric-mxe-gatekeeper-upgrade-jobs.realm-updater-name" -}}
{{- if (index .Values "realm-updater-upgrade-job" "fullNameOverride") -}}
{{- index .Values "realm-updater-upgrade-job" "fullNameOverride" -}}
{{- else -}}
{{- $name := include "eric-mxe-gatekeeper.name" . -}}
{{- printf "%s-%s" $name "post-upgrade-realm-updater" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
Post Upgrade job - Keycloak init container - args
*/}}
{{- define "eric-mxe-gatekeeper.post-upgrade-job-keycloak-init-args-command" -}}
{{- if .Values.global.serviceMesh.enabled -}}
command:
- sh
- -c
- |
  openssl pkcs12 -export -name "client-cert" -in "/run/secrets/iam/client/clicert.pem" -inkey "/run/secrets/iam/client/cliprivkey.pem"  -out /tmp/clicert.pks12 -passout pass:"password"; 
  keytool -importkeystore -deststorepass "password" -destkeypass "password" -destkeystore "/tmp/server.keystore" -srckeystore "/tmp/clicert.pks12" -srcstoretype PKCS12  -deststoretype PKCS12 -srcstorepass "password" -noprompt 2>/dev/null; 
  keytool -import -alias "sip-tls" -file "/run/secrets/trusted/ca/ca.crt" -storetype JKS -keystore "/tmp/castore" --noprompt --storepass "password"; 
  /usr/bin/java -jar /pkg.jar add-resource --resource-names=Role,Group,GroupMembership,ProtocolMapper;
{{- else -}}
args:
  - add-resource
  - --resource-names=Role,Group,GroupMembership,ProtocolMapper
{{- end -}}
{{- end -}}

{{/*
Create default ingress annotation prefix.
*/}}
{{- define "eric-mxe-gatekeeper.ingress-annotation-prefix" -}}
{{- if .Values.ingress.annotationPrefix -}}
{{- .Values.ingress.annotationPrefix -}}
{{- else -}}
mxe.nginx.ingress.kubernetes.io
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations, labels
*/}}
{{- define "eric-mxe-gatekeeper.istio-sidecar-annotations" -}}
sidecar.istio.io/rewriteAppHTTPProbers: {{ .Values.global.serviceMesh.enabled | quote }}
{{ include "eric-mxe-gatekeeper.istio-sidecar-annotations-egress" . }}
{{- end -}}

{{- define "eric-mxe-gatekeeper.istio-sidecar-labels" -}}
sidecar.istio.io/inject: {{ .Values.global.serviceMesh.enabled | quote }}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts
*/}}
{{- define "eric-mxe-gatekeeper.istio-sidecar-annotations-egress" -}}
{{- if .Values.global.serviceMesh.enabled -}}
{{ include "eric-mxe-gatekeeper.istio-sidecar-annotations-egress-mounts" . }}
traffic.sidecar.istio.io/includeInboundPorts: ""
{{- end -}}
{{- end -}}

{{/*
Add External CA - Args command
*/}}
{{- define "eric-mxe-gatekeeper.add-externalca-command" -}}
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
{{- define "eric-mxe-gatekeeper.add-externalca-volume-mounts" -}}
volumeMounts:
  - mountPath: /usr/share/pki/trust/anchors/extca.crt
    name: iam-ca-cert
    subPath: ca.crt
  - mountPath: /cacerts
    name: cacerts
{{- end -}}

{{/*
Add External CA - volumes
*/}}
{{- define "eric-mxe-gatekeeper.add-externalca-volumes" -}}
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
Keycloak init container - args
*/}}
{{- define "eric-mxe-gatekeeper.keycloak-init-args-command" -}}
{{- if (and .Values.isExtCA .Values.global.serviceMesh.enabled) }}
command:
- sh
- -c
- |
  openssl pkcs12 -export -name "client-cert" -in "/run/secrets/iam/client/clicert.pem" -inkey "/run/secrets/iam/client/cliprivkey.pem"  -out /tmp/clicert.pks12 -passout pass:"password";
  keytool -importkeystore -deststorepass "password" -destkeypass "password" -destkeystore "/tmp/server.keystore" -srckeystore "/tmp/clicert.pks12" -srcstoretype PKCS12  -deststoretype PKCS12 -srcstorepass "password" -noprompt 2>/dev/null;
  keytool -import -alias "externalca" -file "/etc/secrets/externalca/ca.crt" -storetype JKS -keystore "/tmp/castore" --noprompt --storepass "password";
  keytool -import -alias "sip-tls" -file "/run/secrets/trusted/ca/ca.crt" -storetype JKS -keystore "/tmp/castore" --noprompt --storepass "password";
  /usr/bin/java -jar /pkg.jar init;
{{- else if .Values.global.serviceMesh.enabled -}}
command:
- sh
- -c
- |
  openssl pkcs12 -export -name "client-cert" -in "/run/secrets/iam/client/clicert.pem" -inkey "/run/secrets/iam/client/cliprivkey.pem"  -out /tmp/clicert.pks12 -passout pass:"password";
  keytool -importkeystore -deststorepass "password" -destkeypass "password" -destkeystore "/tmp/server.keystore" -srckeystore "/tmp/clicert.pks12" -srcstoretype PKCS12  -deststoretype PKCS12 -srcstorepass "password" -noprompt 2>/dev/null;
  keytool -import -alias "sip-tls" -file "/run/secrets/trusted/ca/ca.crt" -storetype JKS -keystore "/tmp/castore" --noprompt --storepass "password";
  /usr/bin/java -jar /pkg.jar init;
{{- else if .Values.isExtCA -}}
command:
- sh
- -c
- |
  keytool -importcert -alias "externalca" -file "/etc/secrets/externalca/ca.crt" -keystore "/tmp/castore" --noprompt --storepass "password";
  /usr/bin/java -jar /pkg.jar init;
{{- else -}}
args:
  - init
{{- end -}}
{{- end -}}

{{/*
Keycloak init container - volume mounts
*/}}
{{- define "eric-mxe-gatekeeper.keycloak-init-volume-mounts" -}}
{{- if and .Values.isExtCA .Values.global.serviceMesh.enabled }}
volumeMounts:
  - name: tmp
    mountPath: /tmp
  - name: trusted-ca
    mountPath: /run/secrets/trusted/ca
    readOnly: true
  - name: eric-sec-access-mgmt-iam-int-client-cert
    mountPath: /run/secrets/iam/client
    readOnly: true
  - mountPath: /etc/secrets/externalca
    name: iam-ca-cert
  - mountPath: /cacerts
    name: cacerts
{{- else if .Values.isExtCA -}}
volumeMounts:
  - name: tmp
    mountPath: /tmp
  - mountPath: /etc/secrets/externalca
    name: iam-ca-cert
  - mountPath: /cacerts
    name: cacerts
{{- else if .Values.global.serviceMesh.enabled -}}
volumeMounts:
  - name: tmp
    mountPath: /tmp
  - name: trusted-ca
    mountPath: /run/secrets/trusted/ca
    readOnly: true
  - name: eric-sec-access-mgmt-iam-int-client-cert
    mountPath: /run/secrets/iam/client
    readOnly: true
{{- end -}}
{{- end -}}

{{/*
Gatekeeper - volume mounts
*/}}
{{- define "eric-mxe-gatekeeper.volume-mounts" -}}
volumeMounts:
  - name: tmp
    mountPath: /home/gatekeeper
  - name: config
    mountPath: /run/secrets/conf
  - name: cert
    mountPath: /run/secrets/cert
  - name: template
    mountPath: /etc/template
{{- if .Values.global.security.tls.enabled }}
  - name: pg-cert
    mountPath: /run/secrets/certificates/client/pg-cert
    readOnly: true
  - name: trusted-ca
    mountPath: /run/secrets/trusted/ca
    readOnly: true
{{- end -}}
{{- if .Values.isExtCA }}  
  - mountPath: /var/lib/ca-certificates
    name: cacerts
{{- end }}
{{- end -}}

{{/*
Gatekeeper - Post-Upgrade Job - Volume mount
*/}}
{{- define "eric-mxe-gatekeeper.post-upgrade-job-volume-mounts" -}}
{{- if .Values.global.serviceMesh.enabled -}}
volumeMounts:
  - name: tmp
    mountPath: /tmp
  - name: trusted-ca
    mountPath: /run/secrets/trusted/ca
    readOnly: true
  - name: eric-sec-access-mgmt-iam-int-client-cert
    mountPath: /run/secrets/iam/client
    readOnly: true
{{- end -}}
{{- end -}}

{{/*
Gatekeeper - volumes
*/}}
{{- define "eric-mxe-gatekeeper.volumes" -}}
volumes:
  - name: config
    secret:
      secretName: {{ include "eric-mxe-gatekeeper.secret-config" . | quote }}
  - name: cert
    secret:
      secretName: {{ .Values.global.mxeApiTlsSecretName }}
  - name: template
    configMap:
      name: {{ include "eric-mxe-gatekeeper.template" . | quote }}
  - name: keycloak-updater-script
    configMap:
      name: {{ include "eric-mxe-gatekeeper.keycloak-updater-configmap" . | quote }}
{{- if .Values.global.security.tls.enabled }}
  - name: pg-cert
    secret:
      secretName: {{ include "eric-mxe-gatekeeper.name" . }}-pg-cert
{{- end -}}
{{- include "eric-mxe-gatekeeper.add-externalca-volumes" . | nindent 2 }}
{{- if .Values.global.serviceMesh.enabled -}}
{{- include "eric-mxe-gatekeeper.service-mesh-volumes" . | nindent 2 }}
{{- else -}}
  - name: dbinit-postgres-sql-home
    emptyDir: {}
{{- include "eric-mxe-gatekeeper.tmp-volume" . | nindent 2 }}
{{- end -}}
{{- end -}}

{{/*
Gatekeeper - tmp-volume
*/}}
{{- define "eric-mxe-gatekeeper.tmp-volume" -}}
- name: tmp
  emptyDir: {}
{{- end -}}

{{/*
Gatekeeper - Service Mesh - volumes
*/}}
{{- define "eric-mxe-gatekeeper.service-mesh-volumes" -}}
{{- if .Values.global.serviceMesh.enabled -}}
{{- include "eric-mxe-gatekeeper.tmp-volume" .}}
- name: trusted-ca
  secret:
    secretName: {{ (((((.Values).global).security).tls).trustedInternalRootCa).secret | default "eric-sec-sip-tls-trusted-root-cert" | quote }}
- name: eric-sec-access-mgmt-iam-int-client-cert
  secret:
    secretName: eric-sec-access-mgmt-iam-int-client-cert
{{- end -}}
{{- end -}}

{{/*
Gatekeeper - Post-Upgrade Job - Volumes
*/}}
{{- define "eric-mxe-gatekeeper.post-upgrade-job-volumes" -}}
{{- if .Values.global.serviceMesh.enabled -}}
volumes:
{{- include "eric-mxe-gatekeeper.service-mesh-volumes" . | nindent 2 }}
{{- end -}}
{{- end -}}

{{/*
podDisruptionBudget.minAvailable of the chart.
*/}}
{{- define "eric-mxe-gatekeeper.podDisruptionBudget.minAvailable" -}}
{{- if .Values.highAvailability -}}
{{ .Values.podDisruptionBudget.minAvailable }}
{{- else -}}
1
{{- end -}}
{{- end -}}

{{/*
ReplicaCount of the chart.
*/}}
{{- define "eric-mxe-gatekeeper.replicaCount" -}}
{{- if .Values.highAvailability -}}
3
{{- else -}}
1
{{- end -}}
{{- end -}}

{{/*
Oauth Mxe Api host path url
*/}}
{{- define "eric-mxe-gatekeeper.oauth-api-host-path-url" -}}
{{- if eq (.Values.global.mxeApiport | int64) 443 -}}
{{- printf "https://%s" .Values.global.mxeOauthApiHostname -}}
{{- else -}}
{{- printf "https://%s:%v" .Values.global.mxeOauthApiHostname .Values.global.mxeApiport -}}
{{- end -}}
{{- end -}}

{{/*
Expand the template of the gatekeeper login page
*/}}
{{- define "eric-mxe-gatekeeper.pdb" -}}
{{- $name := include "eric-mxe-gatekeeper.name" . -}}
{{- printf "%s-%s" $name "pdb" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Standard labels
*/}}
{{- define "eric-mxe-gatekeeper.standard-labels" -}}
app.kubernetes.io/part-of: "mxe"
app.kubernetes.io/instance: {{ .Release.Name | quote }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
helm.sh/chart: {{ include "eric-mxe-gatekeeper.chart" . }}
{{- end -}}

{{/*
Create a user defined label
*/}}
{{ define "eric-mxe-gatekeeper.config-labels" }}
  {{- $global := (.Values.global).labels -}}
  {{- $service := .Values.labels -}}
  {{- include "eric-mxe-gatekeeper.mergeLabels" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged labels for Default, which includes Standard and Config
*/}}
{{- define "eric-mxe-gatekeeper.labels" -}}
  {{- $standard := include "eric-mxe-gatekeeper.standard-labels" . | fromYaml -}}
  {{- $config := include "eric-mxe-gatekeeper.config-labels" . | fromYaml -}}
  {{- include "eric-mxe-gatekeeper.mergeLabels" (dict "location" .Template.Name "sources" (list $standard $config)) | trim }}
{{- end -}}

{{/*
Create a user defined annotation
*/}}
{{ define "eric-mxe-gatekeeper.config-annotations" }}
  {{- $global := (.Values.global).annotations -}}
  {{- $service := .Values.annotations -}}
  {{- include "eric-mxe-gatekeeper.mergeAnnotations" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged annotations for Default, which includes productInfo and config
*/}}
{{- define "eric-mxe-gatekeeper.annotations" -}}
  {{- $productInfo := include "eric-mxe-gatekeeper.product-info" . | fromYaml -}}
  {{- $config := include "eric-mxe-gatekeeper.config-annotations" . | fromYaml -}}
  {{- include "eric-mxe-gatekeeper.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $config)) | trim }}
{{- end -}}

{{/*
adding TopologySpreadConstraints
*/}}
{{- define "eric-mxe-gatekeeper.topologySpreadConstraints" }}
{{- if .Values.topologySpreadConstraints }}
{{- range $config, $values := .Values.topologySpreadConstraints }}
- topologyKey: {{ $values.topologyKey }}
  maxSkew: {{ $values.maxSkew | default 1 }}
  whenUnsatisfiable: {{ $values.whenUnsatisfiable | default "ScheduleAnyway" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
adding security context
*/}}
{{- define "eric-mxe-gatekeeper.initcontainer-security-context" -}}
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

{{- define "eric-mxe-gatekeeper.init-db-security-context" -}}
securityContext:
  capabilities:
    drop:
      - all
  privileged: false
  runAsNonRoot: true
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
{{- define "eric-mxe-gatekeeper.container-security-context" -}}
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
{{- define "eric-mxe-gatekeeper.istio-sidecar-annotations-egress-mounts" -}}
{{- if .Values.global.serviceMesh.enabled -}}
sidecar.istio.io/userVolume: {{ include "eric-mxe-gatekeeper.istio-sidecar-annotations-user-volume" . | squote}}
sidecar.istio.io/userVolumeMount: {{ include "eric-mxe-gatekeeper.istio-sidecar-annotations-volume-mounts" . | squote}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - user volume
*/}}
{{- define "eric-mxe-gatekeeper.istio-sidecar-annotations-user-volume" -}}
{{- printf "{" -}}
{{- include "eric-mxe-gatekeeper.istio-sidecar-annotations-user-volume-egress" . -}},
{{- include "eric-mxe-gatekeeper.istio-sidecar-annotations-user-volume-egress-iam" . -}},
{{- include "eric-mxe-gatekeeper.istio-sidecar-annotations-user-volume-egress-lcm" . -}},
{{- include "eric-mxe-gatekeeper.istio-sidecar-annotations-user-volume-egress-pm" . }}
{{- printf "}" -}}
{{- end }}


{{/*
Istio sidecar annotations - volume mounts - egress
*/}}
{{- define "eric-mxe-gatekeeper.istio-sidecar-annotations-user-volume-egress" -}}
{{- with .Values.global.serviceMesh.egress.ca }}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\"}}" .secretName .genSecretName -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress iam
*/}}
{{- define "eric-mxe-gatekeeper.istio-sidecar-annotations-user-volume-egress-iam" -}}
{{- with .Values.global.serviceMesh.egress.iam }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress pm
*/}}
{{- define "eric-mxe-gatekeeper.istio-sidecar-annotations-user-volume-egress-pm" -}}
{{- with .Values.global.serviceMesh.egress.pmServer }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress lcm
*/}}
{{- define "eric-mxe-gatekeeper.istio-sidecar-annotations-user-volume-egress-lcm" -}}
{{- with .Values.global.serviceMesh.egress.lcmRegistry }}
{{- if .enabled -}}
{{- printf "\"%s\":{\"secret\":{\"secretName\":\"%s\",\"optional\":%t}}" .secretName .genSecretName .optional  -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - user volume mounts
*/}}
{{- define "eric-mxe-gatekeeper.istio-sidecar-annotations-volume-mounts" }}
{{- printf "{" -}}
{{- include "eric-mxe-gatekeeper.istio-sidecar-annotations-volume-mounts-egress" . -}},
{{- include "eric-mxe-gatekeeper.istio-sidecar-annotations-volume-mounts-egress-iam" . -}},
{{- include "eric-mxe-gatekeeper.istio-sidecar-annotations-volume-mounts-egress-lcm" . -}},
{{- include "eric-mxe-gatekeeper.istio-sidecar-annotations-volume-mounts-egress-pm" . -}}
{{- printf "}" -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress
*/}}
{{- define "eric-mxe-gatekeeper.istio-sidecar-annotations-volume-mounts-egress" -}}
{{- with .Values.global.serviceMesh.egress.ca }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .caCertsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress iam
*/}}
{{- define "eric-mxe-gatekeeper.istio-sidecar-annotations-volume-mounts-egress-iam" -}}
{{- with .Values.global.serviceMesh.egress.iam }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress lcm
*/}}
{{- define "eric-mxe-gatekeeper.istio-sidecar-annotations-volume-mounts-egress-lcm" -}}
{{- with .Values.global.serviceMesh.egress.lcmRegistry }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{/*
Istio sidecar annotations - volume mounts - egress pm server
*/}}
{{- define "eric-mxe-gatekeeper.istio-sidecar-annotations-volume-mounts-egress-pm" -}}
{{- with .Values.global.serviceMesh.egress.pmServer }}
{{- printf "\"%s\":{\"mountPath\":\"%s\",\"readonly\":%t}" .secretName .certsPath .readonly  -}}
{{- end -}}
{{- end -}}

{{- define "eric-mxe-gatekeeper.podPriority" -}}
{{- if .Values.podPriority -}}
  {{- if .Values.podPriority.gatekeeper -}}
    {{- .Values.podPriority.gatekeeper.priorityClassName | toString -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{- define "eric-mxe-gatekeeper.realmUpdaterJob.podPriority" -}}
{{- if .Values.podPriority -}}
  {{- if .Values.podPriority.realmUpdaterJob -}}
    {{- .Values.podPriority.realmUpdaterJob.priorityClassName | toString -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{/*
POD AntiAffinity type (soft/hard)
*/}}
{{- define "eric-mxe-gatekeeper.podAntiAffinityType" -}}
{{- $podantiaffinity := "soft" }}
{{- if hasKey .Values "affinity" }}
  {{- $podantiaffinity = .Values.affinity.podAntiAffinity }}
{{- end }}
{{- if eq $podantiaffinity "hard" }}
  requiredDuringSchedulingIgnoredDuringExecution:
  - labelSelector:
      matchExpressions:
      - key: app.kubernetes.io/part-of
        operator: In
        values:
        - mxe
      - key: app.kubernetes.io/component
        operator: In
        values:
        - gatekeeper
      - key: app.kubernetes.io/instance
        operator: In
        values:
        - "{{ .Release.Name }}"
    topologyKey: kubernetes.io/hostname
{{- else if eq $podantiaffinity  "soft" }}
  preferredDuringSchedulingIgnoredDuringExecution:
  - weight: 100
    podAffinityTerm:
      labelSelector:
        matchExpressions:
        - key: app.kubernetes.io/part-of
          operator: In
          values:
          - mxe
        - key: app.kubernetes.io/component
          operator: In
          values:
          - gatekeeper
        - key: app.kubernetes.io/instance
          operator: In
          values:
          - "{{ .Release.Name }}"
      topologyKey: kubernetes.io/hostname
{{- end -}}
{{- end -}}

{{/*
Security annotations
*/}}
{{- define "eric-mxe-gatekeeper.security-annotations" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name }}: {{ $appArmorProfile }}
{{- if not .Values.global.serviceMesh.enabled }}
{{ printf "%s/%s-%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name "init-db" }}: {{ $appArmorProfile }}
{{- end }}
{{ printf "%s/%s-%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name "keycloak-init" }}: {{ $appArmorProfile }}
{{ printf "%s/%s-%s" "container.apparmor.security.beta.kubernetes.io" .Chart.Name "kubectl" }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{- define "eric-mxe-gatekeeper.security-annotations-post-upgrade" -}}
{{- if .Values.appArmorProfile.type -}}
{{ $appArmorProfile := ternary (printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile) .Values.appArmorProfile.type (eq .Values.appArmorProfile.type "localhost") -}}
{{ printf "%s/%s" "container.apparmor.security.beta.kubernetes.io" "realm-updater-job" }}: {{ $appArmorProfile }}
{{- end }}
{{- end -}}

{{- define "eric-mxe-gatekeeper.remove-after-install" }}
mxe.ericsson.se/remove-after-install: "true"
{{- end -}}

{{- define "eric-mxe-gatekeeper.hook-weight" }}
{{- $val := . }}
helm.sh/hook-weight: {{ quote $val }}
{{- end -}}

{{- define "eric-mxe-gatekeeper.job-istio-annotations" }}
sidecar.istio.io/rewriteAppHTTPProbers: "false"
{{- end -}}

{{- define "eric-mxe-gatekeeper.job-istio-labels" }}
sidecar.istio.io/inject: "false"
{{- end -}}

{{- define "eric-mxe-gatekeeper.ingress" }}
nginx.ingress.kubernetes.io/affinity: "cookie"
nginx.ingress.kubernetes.io/session-cookie-expires: "36000"
nginx.ingress.kubernetes.io/session-cookie-max-age: "36000"
nginx.ingress.kubernetes.io/ssl-redirect: {{ if .Values.global.mxeApiTlsSecretName -}}"true"{{- else -}}"false"{{- end }}
nginx.ingress.kubernetes.io/proxy-body-size: 20G
nginx.ingress.kubernetes.io/proxy-request-buffering: "off"
nginx.ingress.kubernetes.io/backend-protocol: "HTTPS"
{{- end -}}

{{- define "eric-mxe-gatekeeper.keycloak-admin-ingress" }}
mxe.nginx.ingress.kubernetes.io/configuration-snippet: "set $http_x_forwarded_proto https; set $http_x_forwarded_host $http_host;"
{{ include "eric-mxe-gatekeeper.ingress-annotation-prefix" . }}/service-upstream: "true"
{{ include "eric-mxe-gatekeeper.ingress-annotation-prefix" . }}/upstream-vhost: {{ .Values.keycloak.service.ingressname }}
{{- end -}}

{{/*
Security Context  for post-upgrade-job
*/}}
{{- define "eric-mxe-gatekeeper-upgrade-jobs.securityContext" -}}
allowPrivilegeEscalation: false
capabilities:
  drop:
    - ALL
privileged: false
readOnlyRootFilesystem: true
runAsNonRoot: true
{{ if .Values.seccompProfile.type -}}
seccompProfile:
  type: {{ .Values.seccompProfile.type }}
  {{ if eq .Values.seccompProfile.type "Localhost" -}}
  localhostProfile: {{ .Values.seccompProfile.localhostProfile }}
  {{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create a map from ".Values.global" with defaults if missing in values file.
This hides defaults from values file.
*/}}
{{ define "eric-mxe-gatekeeper.global" }}
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
{{ define "eric-mxe-gatekeeper.nodeSelector" }}
{{- $g := fromJson (include "eric-mxe-gatekeeper.global" .) -}}
{{- $global := $g.nodeSelector -}}
{{- $service := .Values.nodeSelector -}}
{{- include "eric-mxe-gatekeeper.aggregatedMerge" (dict "context" "nodeSelector" "location" .Template.Name "sources" (list $global $service)) -}}
{{ end }}

{{/*
Added below to handle DR-D1121-102 for globalRegistryPullPolicy Shall be Used
*/}}
{{- define "eric-mxe-gatekeeper.registryImagePullPolicy" -}}
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

{{- define "eric-mxe-gatekeeper.ericProdInfoRepoPath" }}
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

{{- define "eric-mxe-gatekeeper.ericProdInfoRegistry" }}
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
eric-mxe-gatekeeper image name path
*/}}
{{- define "eric-mxe-gatekeeper.setImageRepo" }}
  {{- $top := index . 0 }}
  {{- $serviceLevelImageRegistry := index . 1 }}
  {{- $serviceLevelImageRepoPath := index . 2 }}
  {{- $imageName := index . 3 }}

  {{- /*
  Registry and Repopath for the specified image container is fetched from eric-product-info.yaml
  */}}
  {{- $productInfo := fromYaml ($top.Files.Get "eric-product-info.yaml") -}}
  {{- $registryUrl := include "eric-mxe-gatekeeper.ericProdInfoRegistry" (list $productInfo $imageName) -}}
  {{- $imageRepoPath := include "eric-mxe-gatekeeper.ericProdInfoRepoPath" (list $productInfo $imageName) -}}

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

{{- define "eric-mxe-gatekeeper.ipFamilies" }}
{{- if .Values.global }}
{{- if .Values.global.internalIPFamily }}
ipFamilies: [ {{ .Values.global.internalIPFamily | quote }} ]
{{- end }}
{{- end }}
{{- end -}}

{{/*
Prometheus annotations
*/}}
{{- define "eric-mxe-gatekeeper.prometheus-annotations" -}}
prometheus.io/path: {{ .Values.global.mxePrometheusPath | quote }}
prometheus.io/port: {{ .Values.global.mxePrometheusPort | quote }}
prometheus.io/scrape: {{ .Values.global.mxePrometheusScrape | quote }}
prometheus.io/scrape-role: {{ .Values.global.mxePrometheusScrapeRole | quote }}
prometheus.io/scrape-interval: {{ .Values.global.mxePrometheusScrapeInterval | quote }}
{{- end -}}
{{/*
Merge global tolerations with service tolerations (DR-D1120-061-AD).
*/}}
{{- define "eric-mxe-gatekeeper.merge-tolerations" -}}
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
        {{- $globalItemId := include "eric-mxe-gatekeeper.merge-tolerations.get-identifier" $globalItem -}}
        {{- range $serviceItem := $serviceTolerations -}}
          {{- $serviceItemId := include "eric-mxe-gatekeeper.merge-tolerations.get-identifier" $serviceItem -}}
          {{- if eq $serviceItemId $globalItemId -}}
            {{- $matchingItems = append $matchingItems $serviceItem -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
      {{- range $globalItem := $globalTolerations -}}
        {{- $globalItemId := include "eric-mxe-gatekeeper.merge-tolerations.get-identifier" $globalItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-gatekeeper.merge-tolerations.get-identifier" $matchItem -}}
          {{- if eq $matchItemId $globalItemId -}}
            {{- $matchCount = add1 $matchCount -}}
          {{- end -}}
        {{- end -}}
        {{- if eq $matchCount 0 -}}
          {{- $nonMatchingItems = append $nonMatchingItems $globalItem -}}
        {{- end -}}
      {{- end -}}
      {{- range $serviceItem := $serviceTolerations -}}
        {{- $serviceItemId := include "eric-mxe-gatekeeper.merge-tolerations.get-identifier" $serviceItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-mxe-gatekeeper.merge-tolerations.get-identifier" $matchItem -}}
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
{{ define "eric-mxe-gatekeeper.merge-tolerations.get-identifier" }}
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
{{- define "eric-mxe-gatekeeper.pg-client-cert" -}}
{{ .Values.global.serviceMesh.egress.postgres.clientCertificate }}
{{- end -}}

{{/*
postgres certificate private key
*/}}
{{- define "eric-mxe-gatekeeper.pg-private-key" -}}
{{ .Values.global.serviceMesh.egress.postgres.privateKey }}
{{- end -}}

{{/*
postgres certificate ca
*/}}
{{- define "eric-mxe-gatekeeper.pg-ca-issuer" -}}
{{ .Values.global.serviceMesh.egress.postgres.caIssuer }}
{{- end -}}
