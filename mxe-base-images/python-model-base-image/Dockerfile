ARG COMMON_BASE_OS_VERSION
ARG DEVEL_VERSION
ARG RELEASE_VERSION
ARG RELEASE_REVISION
ARG BUILD_DATE
ARG ARM_URL="https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local"
ARG DEVEL_REPO="adp-dev/adp-build-env"
ARG RUNTIME_REPO="common_base_os/sles"
ARG MC_VERSION=RELEASE.2023-08-29T22-55-06Z

FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles:${COMMON_BASE_OS_VERSION} as base
ARG COMMON_BASE_OS_VERSION
ARG ARM_URL
ARG DEVEL_REPO
ARG DEVEL_VERSION
ARG RUNTIME_REPO

RUN set -ex                                                                                                                 \
    && zypper addrepo --gpgcheck-strict --refresh ${ARM_URL}/${DEVEL_REPO}/${DEVEL_VERSION} CBO_DEVEL                       \
    && zypper addrepo --gpgcheck-strict --refresh ${ARM_URL}/${RUNTIME_REPO}/${COMMON_BASE_OS_VERSION} CBO_RUNTIME          \
    && zypper addrepo --gpgcheck-strict --refresh ${ARM_URL}/${RUNTIME_REPO}/${COMMON_BASE_OS_VERSION}_devel CBO_HEADERS    \
    && zypper --gpg-auto-import-keys refresh                                                                                \
    && zypper update --auto-agree-with-licenses --no-confirm                                                                \
    && zypper install --no-confirm curl wget xz jq zip python311 python311 python311-pip                                    \
    && ln -s /usr/bin/python3.11 /usr/bin/python3                                                                           \
    && python3 -m pip install --no-cache-dir --upgrade pip                                                                  \
    && python3 -m pip install --no-cache-dir --upgrade pipenv setuptools wheel                                              \
    && update-ca-certificates                                                                                               \                                                                                             \
    && curl -L -o /usr/local/bin/mc https://dl.min.io/client/mc/release/linux-amd64/archive/mc.RELEASE.2024-04-18T16-45-29Z \
    && chmod +x /usr/local/bin/mc                                                                                           \
    && zypper clean --all

# BEGIN_RUNTIME
FROM base as python-runtime
ARG COMMON_BASE_OS_VERSION
ARG BUILD_DATE
ARG PYTHON_VERSION
ARG RELEASE_VERSION
ARG RELEASE_REVISION

ENV PYTHONUTF8 1    
RUN set -ex;                                                      \
    echo "mxe:x:1000:1000::/home/mxe:/bin/bash" >> /etc/passwd;   \
    echo "mxe:x:1000:" >> /etc/group;                             \
    mkdir -p /home/mxe;                                           \
    chown mxe:mxe /home/mxe

USER mxe
WORKDIR /home/mxe
ENV PATH /home/mxe/bin:/home/mxe/.local/bin:$PATH
CMD [ "/usr/local/bin/python3" ]
# END_RUNTIME

# MODEL RUNTIME
FROM python-runtime as model-runtime
ARG COMMON_BASE_OS_VERSION
ARG BUILD_DATE
ARG PYTHON_VERSION
ARG RELEASE_VERSION
ARG RELEASE_REVISION
ARG SELDON_CORE_VERSION 
LABEL os.name="CBO"
LABEL os.version=${COMMON_BASE_OS_VERSION}
LABEL python.version=${PYTHON_VERSION}
LABEL seldon.core.version=${SELDON_CORE_VERSION}
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.description="Base Model Image for Python using Common Base OS"
LABEL org.opencontainers.image.title="Base Model image for Python"
LABEL org.opencontainers.image.revision=${RELEASE_REVISION}
LABEL org.opencontainers.image.version=${RELEASE_VERSION}
LABEL org.opencontainers.image.vendor="Ericsson"
# mxe-serving-docker-images/model-python-base
LABEL com.ericsson.product-number="CXU 101 1452"

COPY constraints.txt /tmp/constraints.txt

USER root 
RUN set -e                                                                                               \
    && python3 -m pip install --no-cache-dir seldon-core==${SELDON_CORE_VERSION} -c /tmp/constraints.txt --no-build-isolation \
    && rm /tmp/constraints.txt                                                                           \
    && find /usr/local -type d -name "__pycache__" -prune -exec rm -rf '{}' +
USER mxe 

# BEGIN_RUNTIME
FROM python-runtime as training-runtime
ARG COMMON_BASE_OS_VERSION
ARG BUILD_DATE
ARG PYTHON_VERSION
ARG RELEASE_VERSION

ENV PYTHONUTF8 1
LABEL os.name="CBO"
LABEL os.version=${COMMON_BASE_OS_VERSION}
LABEL python.version=${PYTHON_VERSION}
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.description="Base Training Image for Python using Common Base OS"
LABEL org.opencontainers.image.title="Base Training image for Python"
LABEL org.opencontainers.image.revision=${RELEASE_REVISION}
LABEL org.opencontainers.image.version=${RELEASE_VERSION}
LABEL org.opencontainers.image.vendor="Ericsson"

# mxe-training-docker-images/training-python-base
LABEL com.ericsson.product-number="CXU 101 1453"

# END_RUNTIME
