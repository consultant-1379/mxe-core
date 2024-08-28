# syntax = docker/dockerfile:1.3-labs

ARG BASE_IMAGE=ubuntu:latest
FROM ${BASE_IMAGE}

ARG DEBIAN_FRONTEND=noninteractive 

ARG VCLUSTER_VERSION=v0.10.2
ENV VCLUSTER_VERSION=${VCLUSTER_VERSION}
ARG VCLUSTER_DOWNLOAD_URL=https://github.com/loft-sh/vcluster/releases/download/${VCLUSTER_VERSION}/vcluster-linux-amd64

ARG KUBECTL_VERSION=v1.24.13
ENV KUBECTL_VERSION=${KUBECTL_VERSION}
ARG KUBECTL_DOWNLOAD_URL=https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/amd64/kubectl

ARG HELM_VERSION=v3.11.3
ENV HELM_VERSION=${HELM_VERSION}
ARG HELM_DOWNLOAD_URL=https://get.helm.sh/helm-${HELM_VERSION}-linux-amd64.tar.gz

ARG HELMFILE_VERSION=0.153.1
ENV HELMFILE_VERSION=${HELMFILE_VERSION}
ENV HELMFILE_DOWNLOAD_URL=https://github.com/helmfile/helmfile/releases/download/v${HELMFILE_VERSION}/helmfile_${HELMFILE_VERSION}_linux_amd64.tar.gz

ARG JQ_VERSION=1.6
ENV JQ_VERSION=${JQ_VERSION}
ENV JQ_DOWNLOAD_URL=https://github.com/stedolan/jq/releases/download/jq-${JQ_VERSION}/jq-linux64

ARG YQ_VERSION=v4.26.1
ENV YQ_VERSION=${YQ_VERSION}
ENV YQ_DOWNLOAD_URL=https://github.com/mikefarah/yq/releases/download/${YQ_VERSION}/yq_linux_amd64

ARG CRANE_VERSION=v0.11.0
ENV CRANE_VERSION=${CRANE_VERSION}
ENV CRANE_DOWNLOAD_URL=https://github.com/google/go-containerregistry/releases/download/${CRANE_VERSION}/go-containerregistry_Linux_x86_64.tar.gz

RUN <<eot
    #!/usr/bin/env bash 
    set -ex
    ###########################
    # Install wget and unzip
    # and python3
    ###########################
    apt-get update
    apt-get install -yq wget unzip git
    ###########################
    wget http://pki.ericsson.se/CertData/EGADIssuingCA3.crt -P /usr/local/share/ca-certificates
    wget http://pki.ericsson.se/CertData/EGADRootCA.crt -P /usr/local/share/ca-certificates
    update-ca-certificates
    ###########################
    # Install vcluster
    ###########################
    wget ${VCLUSTER_DOWNLOAD_URL} -O /usr/local/bin/vcluster 
    chmod +x /usr/local/bin/vcluster
    ###########################
    # Install kubectl
    ###########################
    wget ${KUBECTL_DOWNLOAD_URL} -O /usr/local/bin/kubectl
    chmod +x /usr/local/bin/kubectl
    ###########################
    # Install helm 
    ###########################
    wget ${HELM_DOWNLOAD_URL} -O helm.tar.gz 
    tar -xvf helm.tar.gz 
    mv linux-amd64/helm /usr/local/bin/helm
    chmod +x /usr/local/bin/helm
    rm -rf linux-amd64 helm.tar.gz
    ###########################
    # Install helmfile
    ###########################
    wget ${HELMFILE_DOWNLOAD_URL} -O helmfile.tar.gz
    tar xvzf helmfile.tar.gz
    mv helmfile /usr/local/bin/helmfile
    chmod +x /usr/local/bin/helmfile
    rm helmfile.tar.gz
    ###########################
    # Install helmdiff
    ###########################
    helm plugin install https://github.com/databus23/helm-diff
    ###########################
    # Install jq
    ###########################
    wget ${JQ_DOWNLOAD_URL} -O /usr/local/bin/jq
    chmod +x /usr/local/bin/jq
    ###########################
    # Install yq
    ###########################
    wget ${YQ_DOWNLOAD_URL} -O /usr/local/bin/yq
    chmod +x /usr/local/bin/yq
    ###########################
    # Install krew 
    ###########################
    (
        set -x; cd "$(mktemp -d)" &&
        OS="$(uname | tr '[:upper:]' '[:lower:]')" &&
        ARCH="$(uname -m | sed -e 's/x86_64/amd64/' -e 's/\(arm\)\(64\)\?.*/\1\2/' -e 's/aarch64$/arm64/')" &&
        KREW="krew-${OS}_${ARCH}" &&
        wget "https://github.com/kubernetes-sigs/krew/releases/latest/download/${KREW}.tar.gz" &&
        tar zxvf "${KREW}.tar.gz" &&
        ./"${KREW}" install krew &&
        cp $HOME/.krew/bin/kubectl-krew /usr/local/bin/
    )
    ##################################
    # Install images plugin from krew
    #################################
    kubectl krew install images
    ##################################
    # Install crane
    #################################
    wget ${CRANE_DOWNLOAD_URL} -O crane.tar.gz 
    tar xvzf crane.tar.gz 
    mv crane /usr/local/bin/crane
    chmod +x /usr/local/bin/crane
    rm -f crane.tar.gz
eot

ENV PATH="${PATH}:/root/.krew/bin"