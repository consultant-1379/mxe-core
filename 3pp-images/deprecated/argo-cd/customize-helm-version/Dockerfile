# syntax = docker/dockerfile:1.3-labs
ARG BASE_ARGOCD_IMAGE
FROM ${BASE_ARGOCD_IMAGE} as BASE 
ARG HELM_VERSION 

USER root 
RUN <<EOF bash
        set -ex
        wget https://get.helm.sh/helm-v${HELM_VERSION}-linux-amd64.tar.gz 
        tar -zxvf helm-v${HELM_VERSION}-linux-amd64.tar.gz
        mv linux-amd64/helm /usr/local/bin/helm
        rm helm-v${HELM_VERSION}-linux-amd64.tar.gz 
        rm -rf linux-amd64/
EOF

USER argocd