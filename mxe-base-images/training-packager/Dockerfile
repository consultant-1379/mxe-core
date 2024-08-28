ARG BASE_IMAGE_TAG
ARG BASE_IMAGE_NAME=armdocker.rnd.ericsson.se/proj-mxe/kaniko/model-packager

FROM ${BASE_IMAGE_NAME}:${BASE_IMAGE_TAG}
ARG BUILD_DATE
ARG RELEASE_VERSION
ARG RELEASE_REVISION

LABEL com.ericsson.product-number="CXU 101 0836"
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.description="Model training packager image for MXE, uses kaniko"
LABEL org.opencontainers.image.title="Model training packager - Kaniko"
LABEL org.opencontainers.image.revision=${RELEASE_REVISION}
LABEL org.opencontainers.image.vendor="Ericsson"
LABEL org.opencontainers.image.version=${RELEASE_VERSION}


RUN mkdir -p /training

COPY ./init.sh /
COPY ./training-init.sh /training/init-train.sh
RUN chmod +x /init.sh && chmod +x /training/init-train.sh

WORKDIR /tmp

ENTRYPOINT ["/init.sh"]
