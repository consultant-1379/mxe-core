# syntax = docker/dockerfile:1.3-labs
ARG BASE_IMAGE 

FROM ${BASE_IMAGE} as builder  

ARG MC_DOWNLOAD_URL

WORKDIR /workspace
RUN <<EOF bash
    set -ex
    apt-get update
    apt-get install -yq wget
    wget -O mc ${MC_DOWNLOAD_URL}
    chmod +x mc 
EOF

## Sets up Non root user, and copies mc from builder. Runs mc as non root user
FROM ${BASE_IMAGE} as runtime 

ARG MC_VERSION 

RUN <<EOF bash
    set -ex
    echo "mxe:x:1000:1000::/home/mxe:/bin/bash" >> /etc/passwd
    echo "mxe:x:1000:" >> /etc/group
    mkdir -p /home/mxe
    chown -R mxe:mxe /home/mxe
EOF

COPY --from=builder "/workspace/mc" /usr/local/bin/mc

LABEL 'MC_VERSION' $MC_VERSION 

USER mxe 
WORKDIR /home/mxe

ENTRYPOINT [ "mc" ]