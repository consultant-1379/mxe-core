ARG COMMON_BASE_OS_VERSION
FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles:$COMMON_BASE_OS_VERSION

LABEL com.ericsson.product-number="CXU 101 0060"

ARG COMMON_BASE_OS_VERSION
ENV COMMON_BASE_OS_VERSION $COMMON_BASE_OS_VERSION
RUN zypper addrepo -C -G -f "https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/$COMMON_BASE_OS_VERSION?ssl_verify=no" COMMON_BASE_OS_SLES_REPO

ARG JQ_VERSION=1.6 
ARG KUBECTL_VERSION
ARG USER_ID

COPY ["init.sh", "git-ask-pass.sh", "/usr/local/bin/"]

RUN zypper install -l -y curl git-core libinotifytools0 inotify-tools \
    && curl -L0 -k --output /usr/share/pki/trust/anchors/EGADIssuingCA3.crt  http://pki.ericsson.se/CertData/EGADIssuingCA3.crt  \
    && curl -L0 -k --output /usr/share/pki/trust/anchors/EGADRootCA.crt http://pki.ericsson.se/CertData/EGADRootCA.crt      \ 
    && update-ca-certificates   \
    && zypper clean --all \
    && curl -L0 -k --output /usr/local/bin/jq https://github.com/stedolan/jq/releases/download/jq-${JQ_VERSION}/jq-linux64 \
	&& curl -L0 -k https://storage.googleapis.com/kubernetes-release/release/v${KUBECTL_VERSION}/bin/linux/amd64/kubectl --output /usr/local/bin/kubectl \
    && mkdir -p /etc/kubernetes-run-script \
    && chmod +x /usr/local/bin/jq  \
        /usr/local/bin/kubectl \
        "/usr/local/bin/git-ask-pass.sh" \
        "/usr/local/bin/init.sh" 

RUN echo "$USER_ID:x:$USER_ID:$USER_ID:An identity for user:/nonexistent:/bin/false" >> /etc/passwd;\
    echo "$USER_ID:!::0:::::" >> /etc/shadow

WORKDIR /tmp

ENTRYPOINT ["bash", "/etc/kubernetes-run-script/run.sh"]

USER "$USER_ID"

ARG BUILD_DATE
ARG RELEASE_VERSION
ARG RELEASE_REVISION
LABEL org.opencontainers.image.title="MXE Kubernetes Modifier"
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.revision=${RELEASE_REVISION}
LABEL org.opencontainers.image.vendor="Ericsson"
LABEL org.opencontainers.image.version=${RELEASE_VERSION}
