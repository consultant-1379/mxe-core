ARG BASE_IMAGE
FROM $BASE_IMAGE

ARG USER_ID=10001
ARG INOTIFY_VERSION

LABEL inotify_version=${INOTIFY_VERSION}

RUN apt-get update;\
    apt-get install -y openssl libinotifytools0=${INOTIFY_VERSION} inotify-tools=${INOTIFY_VERSION};\
    echo "$USER_ID:x:$USER_ID:$USER_ID:An identity for user:/nonexistent:/bin/false" >> /etc/passwd;\
    echo "$USER_ID:!::0:::::" >> /etc/shadow;\
    mkdir -p /etc/kubernetes-run-script 

WORKDIR /tmp

ENTRYPOINT ["bash", "/etc/kubernetes-run-script/run.sh"]

USER "$USER_ID"