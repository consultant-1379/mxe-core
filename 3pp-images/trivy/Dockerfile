##############################################################################################################################
# armdocker.rnd.ericsson.se/proj-adp-cicd-drop/trivy-inline-scan:latest provides latest trivy release version is 0.16.0 
# but this version does not support scanning ubuntu:20.10 
# Support for ubuntu:20.10 is available in master but not released yet
# As a workaround, this Dockerfile builds trivy from source 
# replaces the binary in armdocker.rnd.ericsson.se/proj-adp-cicd-drop/trivy-inline-scan:latest  
# and builds armdocker.rnd.ericsson.se/proj-mxe/trivy-inline-scan:latest
###############################################################################################################################
FROM golang:1.16

RUN mkdir -p $GOPATH/src/github.com/aquasecurity; \
    cd $GOPATH/src/github.com/aquasecurity; \
    git clone --depth 1 https://github.com/aquasecurity/trivy; \
    cd trivy/cmd/trivy/ ; \
    export GO111MODULE=on; \
    go install; 

FROM armdocker.rnd.ericsson.se/proj-adp-cicd-drop/trivy-inline-scan:latest  

COPY --from=0  /go/bin/trivy /usr/bin/trivy
