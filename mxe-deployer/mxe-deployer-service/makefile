all: server cli
runserver: dmserver run-server
server: dmserver
cli: dmcli-linux dmcli-windows dmcli-mac
#test: lint unit-test

PLATFORM=local
VERSION=v1
DEVEL_VERSION=1.25.0
COMMON_BASE_OS_VERSION=3.31.0-9
GOVERSION=1.15
HOMEDIR=$${HOME}
DM_BASE=$(shell pwd)
MAKE_VERBOSE=1
MXE_CLI_VERSION=0.0.1

.PHONY: test 
test:
	@echo "\n\n ${HOMEDIR}"
	@echo "\n\n ${DM_BASE}"

.PHONY: dmserver
dmserver:
	@docker build 													\
	--build-arg DEVEL_VERSION=${DEVEL_VERSION} 						\
	--build-arg COMMON_BASE_OS_VERSION=${COMMON_BASE_OS_VERSION} 	\
	--build-arg GOVERSION=${GOVERSION}								\
	--target dm-server 												\
	--tag depmanager:latest . 										\
	--tag depmanager:${VERSION}


.PHONY: run-server
run-server:
	@echo "docker run -v ${HOMEDIR}/.kube/config:/home/mxe/.kube/config:ro -v ${DM_BASE}/resources/:/dm/config --env DM_CONFIG_FILE=/dm/config/config.yaml -p 7543:7543 -it depmanager" 

	@./scripts/get_argocd_config.sh

	@docker run -v "${HOMEDIR}/.kube/config:/home/mxe/.kube/config":ro      \
	-v "${DM_BASE}/resources/:/dm/config" 						\
	--env DM_CONFIG_FILE=/dm/config/config.yaml 					    	\
	-p 7543:7543  													    	\
	-it depmanager
