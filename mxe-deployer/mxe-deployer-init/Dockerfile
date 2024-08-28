# syntax = docker/dockerfile:1-experimental

ARG CBO_URL="armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles"
ARG COMMON_BASE_OS_VERSION
ARG GOVERSION
FROM armdockerhub.rnd.ericsson.se/golang:${GOVERSION}-alpine AS go-base
WORKDIR /src
ENV CGO_ENABLED=0
COPY ["go.mod", "go.sum", "/src/"]
RUN --mount=type=cache,target=/go/pkg/mod/ \
    go mod download
COPY . .

FROM go-base AS build-server
ARG TARGETOS
ARG TARGETARCH
RUN --mount=target=. \
    --mount=type=cache,target=/go/pkg/mod/ \
    --mount=type=cache,target=/root/.cache/go-build/ \
  GOOS=linux GOARCH=amd64 go build -ldflags="-s -w" -o /out/mxe-deployer-init main.go

FROM ${CBO_URL}:${COMMON_BASE_OS_VERSION} as cbo-base

ARG COMMON_BASE_OS_VERSION
ARG ARM_URL="https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local"
ARG DEVEL_REPO="adp-dev/adp-build-env"
ARG DEVEL_VERSION
ARG RUNTIME_REPO="common_base_os/sles"
ARG GOVERSION 

ENV COMMON_BASE_OS_VERSION $COMMON_BASE_OS_VERSION
ENV PATH "$PATH:/usr/local/go/bin"

RUN set -ex                                                            \
  && echo "mxe:x:1000:1000::/home/mxe:/bin/bash" >> /etc/passwd        \
  && echo "mxe:x:1000:" >> /etc/group                                  \
  && mkdir -p /home/mxe /home/mxe/.kube/                               \
  && chown -R mxe:mxe /home/mxe

VOLUME ["/home/mxe/.kube"]

RUN set -ex                                                                                                                             \                             					                                                                                    
  && update-ca-certificates                                                                                                             \
  && zypper addrepo --gpgcheck-strict --refresh "${ARM_URL}/${RUNTIME_REPO}/${COMMON_BASE_OS_VERSION}" COMMON_BASE_OS_SLES_REPO         \
  && zypper addrepo --gpgcheck-strict --refresh "${ARM_URL}/${DEVEL_REPO}/${DEVEL_VERSION}" CBO_DEVEL                                   \
  && zypper addrepo --gpgcheck-strict --refresh "${ARM_URL}/${RUNTIME_REPO}/${COMMON_BASE_OS_VERSION}_devel" CBO_HEADERS                \
  && zypper --gpg-auto-import-keys refresh                                                                                              \
  && zypper update --auto-agree-with-licenses --no-confirm                                                                              \
  && zypper install --auto-agree-with-licenses --no-confirm wget                                                \
  && wget http://pki.ericsson.se/CertData/EGADIssuingCA3.crt -P /usr/share/pki/trust/anchors/                                           \
  && wget http://pki.ericsson.se/CertData/EGADRootCA.crt -P /usr/share/pki/trust/anchors/                                               \ 
  && update-ca-certificates                                                                                                             \
  && zypper clean --all


FROM cbo-base as dm-server-init

LABEL com.ericsson.product-number="CXU 101 0944"

WORKDIR /init
COPY --chown=mxe --from=build-server /out/mxe-deployer-init /init/

RUN mkdir -p /init/config; \
  chown -R mxe:mxe /init/config

USER 1000

CMD ["bash"]

ARG BUILD_DATE
ARG RELEASE_VERSION
ARG RELEASE_REVISION
LABEL org.opencontainers.image.title="MXE Deployer Init"
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.revision=${RELEASE_REVISION}
LABEL org.opencontainers.image.vendor="Ericsson"
LABEL org.opencontainers.image.version=${RELEASE_VERSION}
