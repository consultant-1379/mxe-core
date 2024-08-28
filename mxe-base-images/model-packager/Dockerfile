ARG COMMON_BASE_OS_VERSION

FROM armdockerhub.rnd.ericsson.se/golang:1.20
WORKDIR /src
ARG TARGETARCH=amd64

ENV GOARCH=$TARGETARCH
ENV CGO_ENABLED=0
ENV GOBIN=/usr/local/bin
# Get GCR credential helper
RUN go install github.com/GoogleCloudPlatform/docker-credential-gcr@4cdd60d0f2d8a69bc70933f4d7718f9c4e956ff8

# Get Amazon ECR credential helper
RUN go install github.com/awslabs/amazon-ecr-credential-helper/ecr-login/cli/docker-credential-ecr-login@69c85dc22db6511932bbf119e1a0cc5c90c69a7f # v0.6.0

# Get ACR docker env credential helper
RUN go install github.com/chrismellard/docker-credential-acr-env@09e2b5a8ac86c3ec347b2473e42b34367d8fa419

RUN echo $GOARCH > /goarch

# Add .docker config dir
RUN mkdir -p /kaniko/.docker

COPY kaniko/ .
RUN \
    --mount=type=cache,target=/root/.cache/go-build \
    --mount=type=cache,target=/go/pkg \
    make GOARCH=$TARGETARCH

FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles:${COMMON_BASE_OS_VERSION}
ARG COMMON_BASE_OS_VERSION
ARG KANIKO_VERSION
ARG CRANE_VERSION
ARG RELEASE_VERSION
ARG ARM_URL="https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local"
ARG DEVEL_REPO="adp-dev/adp-build-env"
ARG DEVEL_VERSION
ARG RUNTIME_REPO="common_base_os/sles"

ARG BUILD_DATE
ARG RELEASE_REVISION

LABEL com.ericsson.product-number="CXU 101 0057"
LABEL kaniko.version=${KANIKO_VERSION}
LABEL crane.version=${CRANE_VERSION}
LABEL os.name="CBO"
LABEL os.version=${COMMON_BASE_OS_VERSION}
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.description="Model packager image for MXE, uses kaniko and crane"
LABEL org.opencontainers.image.title="Model packager - Kaniko"
LABEL org.opencontainers.image.revision=${RELEASE_REVISION}
LABEL org.opencontainers.image.version=${RELEASE_VERSION}
LABEL org.opencontainers.image.vendor="Ericsson"

WORKDIR /tmp

COPY ./init.sh /

RUN set -ex \ 
    && zypper addrepo --gpgcheck-strict --refresh ${ARM_URL}/${DEVEL_REPO}/${DEVEL_VERSION} CBO_DEVEL         \
    && zypper addrepo --gpgcheck-strict --refresh ${ARM_URL}/${RUNTIME_REPO}/${COMMON_BASE_OS_VERSION} CBO_RUNTIME      \
    && zypper addrepo --gpgcheck-strict --refresh ${ARM_URL}/${RUNTIME_REPO}/${COMMON_BASE_OS_VERSION}_devel CBO_HEADERS \
    && zypper --gpg-auto-import-keys refresh                                                                  \
    && zypper update --auto-agree-with-licenses --no-confirm                                                  \
    && zypper install --no-confirm curl file jq openssl unzip \
    && curl -L0 -k --output /usr/share/pki/trust/anchors/EGADIssuingCA3.crt  http://pki.ericsson.se/CertData/EGADIssuingCA3.crt  \
    && curl -L0 -k --output /usr/share/pki/trust/anchors/EGADRootCA.crt http://pki.ericsson.se/CertData/EGADRootCA.crt      \ 
    && curl -s -L https://github.com/google/go-containerregistry/releases/download/${CRANE_VERSION}/go-containerregistry_Linux_x86_64.tar.gz\
      | tar -C /usr/local/bin -xzf - crane \
    && chmod +x /usr/local/bin/crane \
    && curl -L -o /usr/local/bin/mc https://dl.min.io/client/mc/release/linux-amd64/archive/mc.RELEASE.2024-04-18T16-45-29Z \
    && chmod +x /usr/local/bin/mc \
    && chmod +x /init.sh \
    && update-ca-certificates   \
    && zypper clean --all

COPY --from=0 /src/out/executor /kaniko/executor
#######################################################################################################
## Following lines are present in Kaniko dockerfile in https://github.com/GoogleContainerTools/kaniko/blob/v1.8.1/deploy/Dockerfile
## to install credential helpers for each HCP
## Might be needed in future if configure MXE to use 
## HCP's docker registry inplace of ADP LCM Container registry
## Commented out these for now to exclude CVEs from these additional credential helpers
##TODO: Check with @Padmanaban Mathulu if these are needed
#######################################################################################################
#COPY --from=0 /usr/local/bin/docker-credential-gcr /kaniko/docker-credential-gcr
#COPY --from=0 /usr/local/bin/docker-credential-ecr-login /kaniko/docker-credential-ecr-login
#COPY --from=0 /usr/local/bin/docker-credential-acr-env /kaniko/docker-credential-acr-env
COPY kaniko/files/nsswitch.conf /etc/nsswitch.conf
ENV HOME /root
ENV USER root
ENV PATH /usr/bin/:/usr/local/bin:/kaniko

CMD ["/init.sh"]
