ARG COMMON_BASE_OS_VERSION
FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles:$COMMON_BASE_OS_VERSION

LABEL com.ericsson.product-number="CXU 101 0202"

ARG COMMON_BASE_OS_VERSION
ARG PYTHON3_PKG_VERSION
ENV COMMON_BASE_OS_VERSION $COMMON_BASE_OS_VERSION
RUN zypper addrepo -C -G -f "https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/$COMMON_BASE_OS_VERSION?ssl_verify=no" COMMON_BASE_OS_SLES_REPO; \
    zypper install -l -y python311 python311-pip; \
    zypper clean --all; \
    ln -s /usr/bin/python3.11 /usr/bin/python3; \
    set -ex;                                                     \
    python3 -m pip install --upgrade --no-cache-dir pip;          \
    python3 -m pip install --upgrade --no-cache-dir wheel;         \
    python3 -m pip install --upgrade --no-cache-dir setuptools;     \
    echo "mxe:x:1001:1001::/home/mxe:/bin/bash" >> /etc/passwd; \
    echo "mxe:x:1001:" >> /etc/group;                            \
    mkdir -p /home/mxe;                                          \
    chown mxe:mxe /home/mxe

ENV PATH=${PATH}:/home/mxe

WORKDIR /home/mxe

COPY requirements.txt .

RUN python3 -m pip install --no-cache-dir -r requirements.txt

COPY *.py ./

RUN chmod +x ./*.py

USER mxe

CMD ["consumer.py"]

ARG BUILD_DATE
ARG RELEASE_VERSION
ARG RELEASE_REVISION
LABEL org.opencontainers.image.title="MXE Kafka Pyconsumer"
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.revision=${RELEASE_REVISION}
LABEL org.opencontainers.image.vendor="Ericsson"
LABEL org.opencontainers.image.version=${RELEASE_VERSION}
