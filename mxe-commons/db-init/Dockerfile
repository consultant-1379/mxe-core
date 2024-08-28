ARG COMMON_BASE_OS_VERSION
FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles:$COMMON_BASE_OS_VERSION

LABEL com.ericsson.product-number="CXU 101 0056"

ARG USER_ID=197776
ARG COMMON_BASE_OS_VERSION
ENV COMMON_BASE_OS_VERSION $COMMON_BASE_OS_VERSION
RUN zypper addrepo -C -G -f "https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/$COMMON_BASE_OS_VERSION?ssl_verify=no" COMMON_BASE_OS_SLES_REPO

RUN zypper install -l -y -d \
      shadow \
      postgresql \
      && rpm -i $(ls /var/cache/zypp/packages/COMMON_BASE_OS_SLES_REPO/*/* | xargs echo) \
    && zypper clean --all

ENV POSTGRESQL_SERVICE=eric-data-document-database-pg
ENV POSTGRESQL_PORT_NAME=postgresql
ENV POSTGRESQL_SUPERUSER_USER=postgres
ENV POSTGRESQL_SUPERUSER_PWD=Postgres1#
ENV POSTGRESQL_CREATEDUSER_USER=changeme
ENV POSTGRESQL_CREATEDUSER_PWD=changeme
ENV POSTGRESQL_DB=changeme

RUN echo "$USER_ID:x:$USER_ID:$USER_ID:An identity for dbinit:/nonexistent:/bin/false" >> /etc/passwd;\
    echo "$USER_ID:!::0:::::" >> /etc/shadow;\
    mkdir -p /home/dbinit;\
    chown $USER_ID:0 /home/dbinit

COPY --chown=$USER_ID:0 ./db-init.sh /home/dbinit/

RUN  chmod +x /home/dbinit/db-init.sh

USER 197776

ENTRYPOINT ["/home/dbinit/db-init.sh"]

ARG BUILD_DATE
ARG RELEASE_VERSION
ARG RELEASE_REVISION
LABEL org.opencontainers.image.title="MXE DB Init"
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.revision=${RELEASE_REVISION}
LABEL org.opencontainers.image.vendor="Ericsson"
LABEL org.opencontainers.image.version=${RELEASE_VERSION}
