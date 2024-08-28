ARG COMMON_BASE_OS_VERSION
ARG COMMON_BASE_OS_DOCKER_URL=armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles

FROM $COMMON_BASE_OS_DOCKER_URL:$COMMON_BASE_OS_VERSION

LABEL com.ericsson.product-number="CXU 101 0050"

ARG NODE_VERSION
ARG NPM_VERSION
ARG UID=229276
ARG NPM_REGISTRY=https://arm.seli.gic.ericsson.se/artifactory/api/npm/npm-remote/
ARG BASE_OS_REPO=arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/
ARG COMMON_BASE_OS_VERSION

RUN zypper ar -C -G -f https://${BASE_OS_REPO}${COMMON_BASE_OS_VERSION} COMMON_BASE_OS_SLES_REPO \
    && zypper install -l -y nodejs npm \
    && zypper clean --all \
    && zypper rr COMMON_BASE_OS_SLES_REPO

COPY server/* target/ /var/www/

WORKDIR /var/www

RUN npm18 install --registry ${NPM_REGISTRY} \
    && chown -R $UID:0 /var/www \
    && chmod -R g+w /var/www \
    && zypper rm -y npm18

EXPOSE 8080

USER 229276

ENTRYPOINT ["node","server.js"]

ARG BUILD_DATE
ARG RELEASE_VERSION
ARG RELEASE_REVISION
LABEL org.opencontainers.image.title="MXE GUI"
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.revision=${RELEASE_REVISION}
LABEL org.opencontainers.image.vendor="Ericsson"
LABEL org.opencontainers.image.version=${RELEASE_VERSION}
