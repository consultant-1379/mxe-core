ARG COMMON_BASE_OS_VERSION
FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles:$COMMON_BASE_OS_VERSION

# author-service Image
LABEL com.ericsson.product-number="CXU 101 0058"

# model-catalogue-service Image
LABEL com.ericsson.product-number="CXU 101 0055"

# model-service Image
LABEL com.ericsson.product-number="CXU 101 0054"

# model-training-service Image
LABEL com.ericsson.product-number="CXU 101 0836"

ARG COMMON_BASE_OS_VERSION
ENV COMMON_BASE_OS_VERSION $COMMON_BASE_OS_VERSION
RUN zypper addrepo -C -G -f "https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/$COMMON_BASE_OS_VERSION?ssl_verify=no" COMMON_BASE_OS_SLES_REPO

RUN zypper install -l -y \
      java-17-openjdk \
    && zypper clean --all

WORKDIR /
ARG PKG_NAME
ADD $PKG_NAME /pkg.jar
EXPOSE 8080
ENTRYPOINT ["/usr/bin/java", "-jar", "/pkg.jar"]

ARG BUILD_DATE
ARG RELEASE_VERSION
ARG RELEASE_REVISION
LABEL org.opencontainers.image.title="MXE Author/Model/Model Catalogue/Model Training Service"
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.revision=${RELEASE_REVISION}
LABEL org.opencontainers.image.vendor="Ericsson"
LABEL org.opencontainers.image.version=${RELEASE_VERSION}
