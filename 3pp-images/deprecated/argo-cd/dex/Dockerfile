ARG BASE_IMAGE
FROM ${BASE_IMAGE}

ARG GO_VERSION=1.15.6
ENV PATH="${PATH}:/usr/local/go/bin"

RUN apt-get update; \ 
    apt-get install -yq build-essential curl wget git; \
    wget https://golang.org/dl/go${GO_VERSION}.linux-amd64.tar.gz; \
    tar -C /usr/local -xzf go${GO_VERSION}.linux-amd64.tar.gz

ARG TARGETOS
ARG TARGETARCH
ARG TARGETVARIANT=""

WORKDIR /go/src/github.com/dexidp/dex

ENV GOOS=${TARGETOS} \
    GOARCH=${TARGETARCH} \
    GOARM=${TARGETVARIANT}

ARG GOPROXY

COPY go.mod go.sum ./
COPY api/v2/go.mod api/v2/go.sum ./api/v2/
RUN go mod download

COPY . .

RUN make release-binary

FROM ${BASE_IMAGE}

WORKDIR /

# Dex connectors, such as GitHub and Google logins require root certificates.
# Proper installations should manage those certificates, but it's a bad user
# experience when this doesn't work out of the box.
#
# OpenSSL is required so wget can query HTTPS endpoints for health checking.
RUN apt-get update; \
    apt-get install -yq ca-certificates openssl

USER 1001:1001

COPY --from=0 /go/bin/dex /usr/local/bin/dex

# Copy module dependencies for CVE scanning / dependency analysis.
COPY go.mod go.sum                 /opt/dex/dependencies/
COPY api/v2/go.mod api/v2/go.sum   /opt/dex/dependencies/api/v2/

# Import frontend assets and set the correct CWD directory so the assets
# are in the default path.
COPY web web

ENTRYPOINT ["dex"]

CMD ["version"]