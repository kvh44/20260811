{{/* Return the chart name. */}}
{{- define "users-api.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/* Return a stable resource name. */}}
{{- define "users-api.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name (include "users-api.name" .) | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{/* Common Kubernetes labels. */}}
{{- define "users-api.labels" -}}
helm.sh/chart: {{ printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" }}
{{ include "users-api.selectorLabels" . }}
app.kubernetes.io/name: {{ include "users-api.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/* Keep the existing Deployment selector immutable during migration. */}}
{{- define "users-api.selectorLabels" -}}
app: {{ include "users-api.fullname" . | quote }}
{{- end }}
