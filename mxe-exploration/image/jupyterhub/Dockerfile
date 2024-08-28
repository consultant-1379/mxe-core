#
# COPYRIGHT Ericsson 2023
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

ARG CBOS_VERSION=5.19.0-8
ARG CBOS_IMAGE=armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles
ARG CBO_BASE_REPO=https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local
ARG PYPI_SERVER=https://arm.sero.gic.ericsson.se/artifactory/api/pypi/pypi-remote/simple

###################################################################################################
# Python Base Image

FROM $CBOS_IMAGE:$CBOS_VERSION as python_base
ARG CBO_BASE_REPO
ARG CBOS_VERSION
ARG USER_ID=297324
ARG USER_NAME=eric-mxe-jupyterhub
ARG HOME=/home/${USER_NAME}
ARG CBOS_REPO_URL=${CBO_BASE_REPO}/common_base_os/sles/${CBOS_VERSION}

RUN echo "${USER_ID}:x:${USER_ID}:${USER_ID}::/nonexistent:/bin/false" >> /etc/passwd   \
    && echo "${USER_ID}:!::0:::::" >> /etc/shadow                                       \
    && echo "${USER_ID}:x:${USER_ID}:" >> /etc/group                                    \
    && mkdir -p ${HOME}                                                                 \
    && chown -R ${USER_ID}:0 ${HOME}                                                    \
    && zypper ar -C -G -f ${CBOS_REPO_URL}?ssl_verify=no CBOS_REPO                      \
    && zypper install -l -y bind-utils iputils git-core python311 python311-pip         \
                            # requirement for pycurl                                    \
                            libcurl4                                                    \
                            #  requirement for using a local sqlite database            \
                            sqlite3                                                     \
    && ln -s /usr/bin/python3.11 /usr/bin/python                                        \
    && zypper clean --all                                                               \
    && zypper rr CBOS_REPO

###################################################################################################
# Jupyterhub library wheel

FROM python_base as jupyterhub_base
ARG CBO_BASE_REPO
ARG CBOS_VERSION
ARG PYPI_SERVER
ARG CBO_REPO_DEV_URL=${CBO_BASE_REPO}/common_base_os/sles/${CBOS_VERSION}_devel
ARG ADP_BUILD_REPO_URL=${CBO_BASE_REPO}/adp-dev/adp-build-env/${CBOS_VERSION}

RUN zypper ar -C -G -f ${CBO_REPO_DEV_URL}?ssl_verify=no COMMON_BASE_OS_SLES_DEV_REPO    \
    && zypper ar -C -G -f ${ADP_BUILD_REPO_URL}?ssl_verify=no CBO_DEVEL_REPO             \
    #  requirement for pycurl                                                            \
    && zypper install -l -y  gcc openssl openssl-devel libcurl-devel python311-devel     \
    && zypper clean --all                                                                \
    && zypper rr COMMON_BASE_OS_SLES_DEV_REPO CBO_DEVEL_REPO

# Build wheels
COPY requirements.txt /tmp/requirements.txt

RUN --mount=type=cache,target=/tmp/cache                                                 \
      pip3 install -i ${PYPI_SERVER} build wheel                                         \
      && pip3 wheel -i ${PYPI_SERVER} --wheel-dir=/tmp/wheels -r /tmp/requirements.txt   

###################################################################################################
# The final stage

FROM python_base
ARG PYPI_SERVER
ARG IMAGE_PRODUCT_TITLE='MXE Exploration Jupyterhub IMAGE'
ARG IMAGE_PRODUCT_NUMBER='CXU 101 0075'

# install wheels built in the build stage
COPY requirements.txt build/mxeauth-2.6-py3-none-any.whl /tmp/
COPY requirements.txt /tmp/requirements.txt

RUN --mount=type=cache,target=/tmp/cache                                                 \
    --mount=type=cache,from=jupyterhub_base,source=/tmp/wheels,target=/tmp/wheels        \
    pip3 install --no-index --find-links=/tmp/wheels -r /tmp/requirements.txt            \
    && pip3 install -i ${PYPI_SERVER} /tmp/mxeauth-2.6-py3-none-any.whl

WORKDIR ${HOME}
USER ${USER_ID}

EXPOSE 8081

ENTRYPOINT ["catatonit", "--"]

CMD ["jupyterhub", "--config", "/run/secrets/usr/local/etc/jupyterhub/jupyterhub_config.py"]

ARG COMMIT
ARG BUILD_DATE
ARG APP_VERSION
ARG RSTATE
ARG IMAGE_PRODUCT_NUMBER
ARG IMAGE_PRODUCT_TITLE
LABEL \
    org.opencontainers.image.title=$IMAGE_PRODUCT_TITLE \
    org.opencontainers.image.created=$BUILD_DATE \
    org.opencontainers.image.revision=$COMMIT \
    org.opencontainers.image.vendor=Ericsson \
    org.opencontainers.image.version=$APP_VERSION \
    com.ericsson.product-revision=$RSTATE \
    com.ericsson.product-number=$IMAGE_PRODUCT_NUMBER
