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

FROM $CBOS_IMAGE:$CBOS_VERSION
ARG CBOS_VERSION
ARG IMAGE_PRODUCT_TITLE='MXE Exploration Jupyterlab IMAGE'
ARG IMAGE_PRODUCT_NUMBER='CXU 101 0076'

ENV CBO_REPO_URL=https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/$CBOS_VERSION
ENV USER_ID=219438
ENV USER_NAME=eric-mxe-jupyterlab
ENV HOME=/home/$USER_NAME
ENV PYPI_SERVER=https://arm.sero.gic.ericsson.se/artifactory/api/pypi/pypi-remote/simple

COPY requirements.txt /tmp/

RUN echo "$USER_ID:x:$USER_ID:$USER_ID::/nonexistent:/bin/false" >> /etc/passwd      \
    && echo "$USER_ID:!::0:::::" >> /etc/shadow                                      \
    && echo "$USER_ID:x:$USER_ID:" >> /etc/group                                     \
    && mkdir -p $HOME                                                                \
    && chown -R $USER_ID:0 $HOME                                                     \
    && zypper ar -C -G -f $CBO_REPO_URL?ssl_verify=no COMMON_BASE_OS_SLES_REPO       \
    && zypper install -l -y python311 python311-pip                                  \
    && zypper install -l -y bind-utils iputils ca-certificates git-core nodejs npm   \
    # Set Python 3.11 as default
    && update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.11 1  \
    # Java as Spark Dependency
    && zypper install -l -y java-openjdk-headless                                    \
    # Install Depedencies
    && pip3 install -i $PYPI_SERVER -r /tmp/requirements.txt                         \
    # Cleanup
    && zypper rm -y npm18                                                            \
    && zypper clean --all                                                            \
    && zypper rr COMMON_BASE_OS_SLES_REPO

WORKDIR $HOME

USER $USER_ID

EXPOSE 8888

ENTRYPOINT ["catatonit", "--"]

CMD ["jupyterhub-singleuser"]

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
