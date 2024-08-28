#!/bin/bash
set -x
set -e
set -o pipefail

#######################################################################################################################################
# Wrapper Script that builds all customised 3pp images used in MXE
#######################################################################################################################################

case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac

BASEDIR=$(dirname $SCRIPT)
LOG_DIR=${BASEDIR}/logs_and_reports/build_logs

# build_argo_wf(){
#     docker buildx create --name argo_wf_builder --use 
#     ${BASEDIR}/argo-workflows/build_alpine_image.sh 2>&1 | tee ${LOG_DIR}/build_argoexec.log
#     docker buildx rm argo_wf_builder
#     docker buildx uninstall
# }

build_argo_cd(){
    ${BASEDIR}/mxe-deployer/haproxy/build_haproxy_image.sh  2>&1 | tee ${LOG_DIR}/build_ha_proxy.log
    ${BASEDIR}/mxe-deployer/argo-cd/build_ubuntu_image.sh 2>&1 | tee ${LOG_DIR}/build_argo_cd.log
    ${BASEDIR}/mxe-deployer/redis/build_redis_image.sh  2>&1 | tee ${LOG_DIR}/build_redis.log
    # deprecated: # ${BASEDIR}/mxe-deployer/applicationset/build_ubuntu_image.sh 2>&1 | tee ${LOG_DIR}/build_argocd_applicationset.log
    #${BASEDIR}/mxe-deployer/dex/build_dex_image.sh
}

build_gitea(){
    ${BASEDIR}/gitea/build_gitea_image.sh 2>&1 | tee ${LOG_DIR}/build_gitea.log
}

build_seldon_core(){
    ## Seldon core operator and s2i image
    ${BASEDIR}/seldon/images/build.sh 2>&1 | tee ${LOG_DIR}/seldon_images_build.log
}

build_ubuntu_images(){
    ### mxe-deployer ###
    ## gitea
    build_gitea

    ## argocd
    build_argo_cd

    ### seldon ###
    build_seldon_core

    ## minio mc
    ${BASEDIR}/mc/build_image.sh 2>&1 | tee ${LOG_DIR}/build_mc.log

    # build inotify image
    ${BASEDIR}/certificate-sidecar/build_ubuntu_image.sh 2>&1 | tee ${LOG_DIR}/build_certificate_sidecar.log
}


build_alpine_images(){
    
    ## pypiserver
    #${BASEDIR}/pypiserver/build_image.sh 2>&1 | tee ${LOG_DIR}/build_pypiserver.log

    ## nginx
    ${BASEDIR}/nginx-controller/build_image.sh 2>&1 | tee ${LOG_DIR}/build_nginx_controller.log
}


build_cbo_images(){
    ${BASEDIR}/spark-operator/build_sparkoperator_image.sh 2>&1 | tee ${LOG_DIR}/build_spark-operator.log

    ${BASEDIR}/argo-workflows/copy_service_image.sh 2>&1 | tee ${LOG_DIR}/copy_service_image.log
}

# invoke functions by uncommenting 
# or to generate individual image keep comments intact and 
#        just copy the image's command here and execute the script 

#build_ubuntu_images
#build_alpine_images
#build_cbo_images 
#build_argo_wf

#build_argo_cd
#build_gitea
#build_seldon_core
 