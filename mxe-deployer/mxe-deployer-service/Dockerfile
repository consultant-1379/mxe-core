# syntax = docker/dockerfile:1-experimental

ARG CBO_URL="armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles"
ARG COMMON_BASE_OS_VERSION
ARG GOVERSION

FROM armdockerhub.rnd.ericsson.se/golang:${GOVERSION}-alpine AS go-base
WORKDIR /src
ENV CGO_ENABLED=0
COPY go.* .
RUN --mount=type=cache,target=/go/pkg/mod/ \
    go mod download
COPY . .

FROM go-base AS build-server
ARG TARGETOS
ARG TARGETARCH
ARG VERSION
ARG OUTPUT_DIR=/out/mxe-deploy
RUN --mount=target=. \
    --mount=type=cache,target=/go/pkg/mod/ \
    --mount=type=cache,target=/root/.cache/go-build/ \
    GOOS=linux GOARCH=amd64 go build -ldflags="-s -w" -o /out/depmanager dmserver/cmd/dmserver/main.go

FROM ${CBO_URL}:${COMMON_BASE_OS_VERSION} as cbo-base
ARG COMMON_BASE_OS_VERSION
ARG ARM_URL="https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local"
ARG DEVEL_REPO="adp-dev/adp-build-env"
ARG DEVEL_VERSION
ARG RUNTIME_REPO="common_base_os/sles"

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
  && zypper install --auto-agree-with-licenses --no-confirm git-core wget                                                               \
  && wget http://pki.ericsson.se/CertData/EGADIssuingCA3.crt -P /usr/share/pki/trust/anchors/                                           \
  && wget http://pki.ericsson.se/CertData/EGADRootCA.crt -P /usr/share/pki/trust/anchors/                                               \ 
  && update-ca-certificates                                                                                                             \
  && zypper clean --all

FROM cbo-base as dm-server

LABEL com.ericsson.product-number="CXU 101 0835"

WORKDIR /dm
COPY --chown=mxe --from=build-server /out/depmanager /dm/
COPY --chown=mxe --from=go-base /src/hack/git-ask-pass.sh /usr/local/bin/git-ask-pass.sh
COPY --chown=mxe --from=go-base /src/hack/git-verify-wrapper.sh /usr/local/bin/git-verify-wrapper.sh

RUN mkdir -p /dm/app/config; \
  chown -R mxe:mxe /dm/app/config; \
  mkdir -p /dm/app/commitauthors; \
  chown -R mxe:mxe /dm/app/commitauthors 

# support for mounting configuration from a configmap
RUN mkdir -p /app/config/ssh \
    && touch /app/config/ssh/ssh_known_hosts \
    && mkdir -p /etc/ssh \
    && ln -s /app/config/ssh/ssh_known_hosts /etc/ssh/ssh_known_hosts \ 
    && mkdir -p /app/config/tls \
    && mkdir -p /app/config/gpg/source \
    && mkdir -p /app/config/gpg/keys \
    && chown mxe /app/config/gpg/keys \
    && chmod 0700 /app/config/gpg/keys

USER 1000

CMD ["./depmanager", "--debug", "true"]

ARG BUILD_DATE
ARG RELEASE_VERSION
ARG RELEASE_REVISION
LABEL org.opencontainers.image.title="MXE Deployer Service"
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.revision=${RELEASE_REVISION}
LABEL org.opencontainers.image.vendor="Ericsson"
LABEL org.opencontainers.image.version=${RELEASE_VERSION}
