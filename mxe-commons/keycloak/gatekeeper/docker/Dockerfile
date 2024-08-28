ARG COMMON_BASE_OS_VERSION

FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles:$COMMON_BASE_OS_VERSION

LABEL com.ericsson.product-number="CXU 101 0059"

ARG COMMON_BASE_OS_VERSION
ARG USER_ID

# Add MXE Root CA - for dns based discovery url for keycloak
ENV COMMON_BASE_OS_VERSION $COMMON_BASE_OS_VERSION
RUN zypper addrepo -C -G -f "https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/$COMMON_BASE_OS_VERSION?ssl_verify=no" COMMON_BASE_OS_SLES_REPO \
    && zypper install -l -y curl \
    && curl -L0 -k --output /usr/share/pki/trust/anchors/EGADIssuingCA3.crt  http://pki.ericsson.se/CertData/EGADIssuingCA3.crt  \
    && curl -L0 -k --output /usr/share/pki/trust/anchors/EGADRootCA.crt http://pki.ericsson.se/CertData/EGADRootCA.crt      \ 
    && update-ca-certificates   \
    && zypper clean --all

RUN echo "$USER_ID:x:$USER_ID:$USER_ID:An identity for user:/nonexistent:/bin/false" >> /etc/passwd;\
    echo "$USER_ID:!::0:::::" >> /etc/shadow;\
    mkdir /home/gatekeeper;\
    chown $USER_ID:0 -R /home/gatekeeper

ADD ./target/keycloak-gatekeeper /
ENTRYPOINT ["/keycloak-gatekeeper"]

USER "$USER_ID"

ARG BUILD_DATE
ARG RELEASE_VERSION
ARG RELEASE_REVISION
LABEL org.opencontainers.image.title="MXE Gatekeeper"
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.revision=${RELEASE_REVISION}
LABEL org.opencontainers.image.vendor="Ericsson"
LABEL org.opencontainers.image.version=${RELEASE_VERSION}
