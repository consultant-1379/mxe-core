ARG CBO_URL="armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles"
ARG COMMON_BASE_OS_VERSION

FROM ${CBO_URL}:${COMMON_BASE_OS_VERSION} as cbo-base

# MXE CLI Linux Image
LABEL com.ericsson.product-number="CXC 174 2518"

# MXE CLI Windows Image
LABEL com.ericsson.product-number="CXC 174 2519"

# MXE CLI MacOS Image
LABEL com.ericsson.product-number="CXC 174 3212"

ARG COMMON_BASE_OS_VERSION
ARG ARM_URL="https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local"
ARG DEVEL_REPO="adp-dev/adp-build-env"
ARG DEVEL_VERSION
ARG RUNTIME_REPO="common_base_os/sles"
ARG GOVERSION 

ARG CLI_VERSION 
ARG JQ_VERSION=1.6
ENV JQ_VERSION=${JQ_VERSION}

ENV COMMON_BASE_OS_VERSION $COMMON_BASE_OS_VERSION
ENV PATH "$PATH:/usr/local/go/bin"

RUN set -ex                                                            \
  && echo "mxe:x:1000:1000::/home/mxe:/bin/bash" >> /etc/passwd        \
  && echo "mxe:x:1000:" >> /etc/group                                  \
  && mkdir -p /home/mxe /home/mxe/.mxe/                               \
  && chown -R mxe:mxe /home/mxe

RUN set -ex                                                                                                                             \                             					                                                                                    
  && update-ca-certificates                                                                                                             \
  && zypper addrepo --gpgcheck-strict --refresh "${ARM_URL}/${RUNTIME_REPO}/${COMMON_BASE_OS_VERSION}" COMMON_BASE_OS_SLES_REPO         \
  && zypper addrepo --gpgcheck-strict --refresh "${ARM_URL}/${DEVEL_REPO}/${DEVEL_VERSION}" CBO_DEVEL                                   \
  && zypper addrepo --gpgcheck-strict --refresh "${ARM_URL}/${RUNTIME_REPO}/${COMMON_BASE_OS_VERSION}_devel" CBO_HEADERS                \
  && zypper --gpg-auto-import-keys refresh                                                                                              \                                                                                                                                  					                                                                                     
  && zypper update --auto-agree-with-licenses --no-confirm                                                                              \
  && zypper install --auto-agree-with-licenses --no-confirm git-core wget curl jq unzip                                          \
  && wget http://pki.ericsson.se/CertData/EGADIssuingCA3.crt -P /usr/share/pki/trust/anchors/                                           \
  && wget http://pki.ericsson.se/CertData/EGADRootCA.crt -P /usr/share/pki/trust/anchors/                                               \ 
  && update-ca-certificates                                                                                                             \
  && zypper clean --all

ADD bin/linux/mxe-cli-linux-${CLI_VERSION}.tgz /usr/local/bin/

RUN chmod +x /usr/local/bin/mxe-*

WORKDIR /home/mxe

USER mxe

ARG BUILD_DATE
ARG RELEASE_VERSION
ARG RELEASE_REVISION
LABEL org.opencontainers.image.title="MXE CLI"
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.revision=${RELEASE_REVISION}
LABEL org.opencontainers.image.vendor="Ericsson"
LABEL org.opencontainers.image.version=${RELEASE_VERSION}
