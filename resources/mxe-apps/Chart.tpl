apiVersion: v2
appVersion: {{ mxe_version }}
dependencies:
    - name: mxe-serving
      repository: {{ mxe_helm_repo_url }}
      version: {{ mxe_version }}
      condition: mxe-serving.enabled
    - name: mxe-exploration
      repository: {{ mxe_helm_repo_url }}
      version: {{ mxe_version }}
      condition: mxe-exploration.enabled
    - name: mxe-workflow
      repository: {{ mxe_helm_repo_url }}
      version: {{ mxe_version }}
      condition: mxe-workflow.enabled
    - name: mxe-training
      repository: {{ mxe_helm_repo_url }}
      version: {{ mxe_version }}
      condition: mxe-training.enabled
description: helm chart for mxe-apps
keywords:
    - serving 
    - exploration
    - workflow
    - training
maintainers:
    - email: PDLMEESUPP@pdl.internal.ericsson.com
      name: MXE
name: mxe-apps
version: {{ mxe_version }}
