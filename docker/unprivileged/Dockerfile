ARG COMMON_BASE_OS_VERSION
FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles:$COMMON_BASE_OS_VERSION

# default-backend Image
LABEL com.ericsson.product-number="CXU 101 0053"

# keycloak-init Image
LABEL com.ericsson.product-number="CXU 101 0061"

ARG COMMON_BASE_OS_VERSION
ARG USER_ID
ENV COMMON_BASE_OS_VERSION $COMMON_BASE_OS_VERSION
RUN zypper addrepo -C -G -f "https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/$COMMON_BASE_OS_VERSION?ssl_verify=no" COMMON_BASE_OS_SLES_REPO

RUN zypper install -l -y \
      java-17-openjdk \
    && zypper clean --all

RUN echo "$USER_ID:x:$USER_ID:$USER_ID:An identity for user:/nonexistent:/bin/false" >> /etc/passwd;\
    echo "$USER_ID:!::0:::::" >> /etc/shadow

WORKDIR /
ARG PKG_NAME
ADD $PKG_NAME /pkg.jar 
RUN chown $USER_ID:0 /pkg.jar

EXPOSE 8080
ENTRYPOINT ["/usr/bin/java", "-jar", "/pkg.jar"]

USER "$USER_ID"

ARG BUILD_DATE
ARG RELEASE_VERSION
ARG RELEASE_REVISION
LABEL org.opencontainers.image.title="MXE Default Backend/Keycloak Init"
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.revision=${RELEASE_REVISION}
LABEL org.opencontainers.image.vendor="Ericsson"
LABEL org.opencontainers.image.version=${RELEASE_VERSION}
