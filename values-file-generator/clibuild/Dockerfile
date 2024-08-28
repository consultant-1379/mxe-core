#syntax=docker/dockerfile:1.3
FROM --platform=${BUILDPLATFORM} armdockerhub.rnd.ericsson.se/golang:1.18-alpine AS base
RUN apk --no-cache add build-base make git gcc libtool musl-dev ca-certificates dumb-init 

FROM base  AS modbase 
WORKDIR /src
ENV CGO_ENABLED=0
COPY go.* .
RUN --mount=type=cache,target=/go/pkg/mod/ \
    go mod download

FROM modbase AS build
ARG TARGETOS
ARG TARGETARCH
ARG VERSION
ARG OUTPUT_DIR=/out/mxe-generate
RUN --mount=target=. \
    --mount=type=cache,target=/go/pkg/mod/ \
    --mount=type=cache,target=/root/.cache/go-build/ \
    ./clibuild/build-mxe-generator.sh ${TARGETOS} ${TARGETARCH} ${VERSION} ${OUTPUT_DIR}

FROM modbase AS unit-test
RUN --mount=target=. \
    --mount=type=cache,target=/go/pkg/mod/ \
    --mount=type=cache,target=/root/.cache/go-build/ \
  go test -v ./...

FROM base AS lint
RUN --mount=target=. \
    --mount=from=lint-base,src=/usr/bin/golangci-lint,target=/usr/bin/golangci-lint \
    --mount=type=cache,target=/go/pkg/mod \
    --mount=type=cache,target=/root/.cache/go-build \
    --mount=type=cache,target=/root/.cache/golangci-lint \
  golangci-lint run --timeout 10m0s ./...

FROM scratch AS bin-linux
ARG OUTPUT_DIR=/out/mxe-generate
COPY --from=build ${OUTPUT_DIR} /linux

FROM scratch AS bin-darwin
ARG OUTPUT_DIR=/out/mxe-generate
COPY --from=build ${OUTPUT_DIR} /darwin

FROM scratch AS bin-windows
ARG OUTPUT_DIR=/out/mxe-generate
COPY --from=build ${OUTPUT_DIR} /windows

FROM bin-${TARGETOS} AS bin
