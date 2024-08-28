##########################################
##          BASE CBO IMAGE              ##
##########################################
ARG CBO_IMAGE_URL
ARG CBO_VERSION
ARG BUILD_DATE
ARG SPARK_OPERATOR_IMAGE_TAG

FROM ${CBO_IMAGE_URL}:${CBO_VERSION} as base

ARG CBO_VERSION
ARG ARM_URL="https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local"
ARG DEVEL_REPO="adp-dev/adp-build-env"
ARG DEVEL_VERSION
ARG RUNTIME_REPO="common_base_os/sles"

ARG TINI_VERSION

ENV LC_CTYPE en_US.UTF-8
ENV LANG en_US.UTF-8

LABEL maintainer "MXE CI Group <PDLMMECIMM@pdl.internal.ericsson.com>"
LABEL title "MXE CBO"
LABEL description "CBO base image for Spark Operator"
LABEL os.name "CBO"
LABEL os.version "$CBO_VERSION"
LABEL devel.version "$DEVEL_VERSION"

ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /sbin/tini
RUN chmod +x /sbin/tini

RUN set -ex;                                                      \
    echo "mxe:x:1000:1000::/home/mxe:/bin/bash" >> /etc/passwd;   \
    echo "mxe:x:1000:" >> /etc/group;                             \
    mkdir -p /home/mxe;                                           \
    chown mxe:mxe /home/mxe

RUN set -ex;                                                                                                \
    update-ca-certificates;                                                                                 \
    zypper addrepo --gpgcheck-strict --refresh ${ARM_URL}/${DEVEL_REPO}/${DEVEL_VERSION} CBO_DEVEL;         \
    zypper addrepo --gpgcheck-strict --refresh ${ARM_URL}/${RUNTIME_REPO}/${CBO_VERSION} CBO_RUNTIME;       \
    zypper addrepo --gpgcheck-strict --refresh ${ARM_URL}/${RUNTIME_REPO}/${CBO_VERSION}_devel CBO_HEADERS; \
    zypper --gpg-auto-import-keys refresh;                                                                  \
    zypper update --auto-agree-with-licenses --no-confirm;                                                  \
    zypper install --auto-agree-with-licenses --no-confirm wget curl;                                       \
    curl -L0 -k --output /usr/share/pki/trust/anchors/EGADIssuingCA3.crt  http://pki.ericsson.se/CertData/EGADIssuingCA3.crt;  \
    curl -L0 -k --output /usr/share/pki/trust/anchors/EGADRootCA.crt http://pki.ericsson.se/CertData/EGADRootCA.crt;           \ 
    zypper clean --all

USER mxe

ENTRYPOINT [ "/sbin/tini", "-v", "--" ]
CMD ["/bin/sh"]

##########################################
##   SPARK-OPERATOR & SPARK BUILDER     ##
##########################################
FROM armdocker.rnd.ericsson.se/dockerhub-ericsson-remote/golang:1.19.2-alpine as builder
WORKDIR /workspace

# Copy the Go Modules manifests
COPY go.mod go.mod
COPY go.sum go.sum
# Cache deps before building and copying source so that we don't need to re-download as much
# and so that source changes don't invalidate our downloaded layer
RUN go mod download \
    && mkdir -p /spark/apache-log4j \
    && wget -q https://archive.apache.org/dist/spark/spark-3.4.1/spark-3.4.1-bin-hadoop3.tgz \
    && tar -xzf spark-3.4.1-bin-hadoop3.tgz \
    && mv spark-3.4.1-bin-hadoop3/* /spark   \
    && rm spark-3.4.1-bin-hadoop3.tgz

# Copy the go source code
COPY main.go main.go
COPY pkg/ pkg/
RUN go mod tidy

# Build
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 GO111MODULE=on go build -a -o /usr/bin/spark-operator main.go

#################################
#####   BUILD SPARK IMAGE   #####
#################################

FROM base as spark-operator

ARG CBO_JAVA_VERSION=1_8_0
ARG BUILD_DATE
ARG SPARK_OPERATOR_IMAGE_TAG

LABEL org.opencontainers.image.title "Spark Operator image based on ADP CBO"
LABEL description "A Spark container image usable in a k8s scheduled job based on OpenJDK JRE ${CBO_JAVA_VERSION}"
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.revision=1
LABEL org.opencontainers.image.version=${SPARK_OPERATOR_IMAGE_TAG}
LABEL org.opencontainers.image.vendor="Ericsson"

ENV JAVA_HOME=/usr/lib64/jvm/jre
ENV SPARK_HOME=/opt/spark
ENV PATH=$SPARK_HOME/bin:$PATH:${JAVA_HOME}/bin

USER root
RUN zypper --gpg-auto-import-keys refresh                                                                       \
    && zypper update --auto-agree-with-licenses --no-confirm                                                    \
    && zypper install --auto-agree-with-licenses --no-confirm java-${CBO_JAVA_VERSION}-openjdk-headless openssl \
    && zypper clean --all

COPY --from=builder /spark/jars/spark* \
    /spark/jars/scala-library* \
    /spark/jars/hadoop* \
    /spark/jars/commons* \
    /spark/jars/kubernetes* \
    /spark/jars/stax* \
    /spark/jars/woodstox* \
    /spark/jars/okhttp* \
    /spark/jars/okio* \
    /spark/jars/logging* \
    /spark/jars/json4s* \
    /spark/jars/jackson* \
    /spark/jars/log4j* \
    /spark/jars/guava* \
    /spark/jars/ivy* \
    /spark/jars/snakeyaml* \
    /spark/jars/slf4j* \
    /spark/jars/jcl-over-slf4j-2.0.6.jar \
    /spark/jars/jul-to-slf4j-2.0.6.jar \
    ${SPARK_HOME}/jars/

RUN chown mxe:mxe ${SPARK_HOME}/jars/*jar

COPY --from=builder /spark/bin ${SPARK_HOME}/bin/
COPY --from=builder /spark/sbin ${SPARK_HOME}/sbin/
COPY --from=builder /spark/kubernetes/tests ${SPARK_HOME}/tests/
COPY --from=builder /spark/examples ${SPARK_HOME}/examples/
COPY --from=builder /usr/bin/spark-operator /usr/bin/
COPY hack/gencerts.sh /usr/bin/
COPY entrypoint.sh /usr/bin/

RUN sed -i s@/usr/bin/tini@/sbin/tini@g /usr/bin/entrypoint.sh

USER mxe

ENTRYPOINT [ "/usr/bin/entrypoint.sh" ]
