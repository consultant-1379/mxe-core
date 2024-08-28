#!/bin/bash
#######################################################################################################################################
# Wrapper Script that scans all customised 3pp images used in MXE-Deployer
#######################################################################################################################################

case "$(uname -s)" in
Darwin*) SCRIPT=$(greadlink -f $0) ;;
*) SCRIPT=$(readlink -f $0) ;;
esac
echo
CWD=$(pwd)
BASEDIR=$(dirname $SCRIPT)
MODULEROOT=$(dirname $BASEDIR)
source "${MODULEROOT}/base_image.sh"
WORKSPACE_DIR=$(cd $BASEDIR && cd ../ && pwd)
REPORTS_DIR="${BASEDIR}/logs_and_reports/"
TRIVY_LOGS="${REPORTS_DIR}/trivy-reports/"
CUSTOM_IMAGES_DIR="${WORKSPACE_DIR}/3pp-images"
mkdir -p ${TRIVY_LOGS}
cd ${CWD}

images=(
    armdocker.rnd.ericsson.se/proj-mxe/argoproj/argocd:v2.4.12-ubuntu-${LATEST_DATE_TAG}
    armdocker.rnd.ericsson.se/proj-mxe/haproxy:2.0.4-ubuntu-${LATEST_DATE_TAG}
    armdocker.rnd.ericsson.se/proj-mxe/redis:6.0.7-ubuntu-${LATEST_DATE_TAG}
    armdocker.rnd.ericsson.se/proj-mxe/gitea/mxe-gitea:v1.20.2-ubuntu-${LATEST_DATE_TAG}
    armdocker.rnd.ericsson.se/proj-mxe/seldonio/seldon-core-s2i-python37:1.12.0-ubuntu-${LATEST_DATE_TAG}
    armdocker.rnd.ericsson.se/proj-mxe/argoproj/argoexec:v3.1.8-ubuntu-${LATEST_DATE_TAG}
)

docker rmi -f armdocker.rnd.ericsson.se/proj-adp-cicd-drop/trivy-inline-scan:latest || true
# docker build -t armdocker.rnd.ericsson.se/proj-mxe/trivy-inline-scan:latest -f ${CUSTOM_IMAGES_DIR}/trivy/Dockerfile ${CUSTOM_IMAGES_DIR}/trivy
# trivy_image_build_status=$?

# if [[ $trivy_image_build_status -ne 0 ]]; then
#     echo "trivy image could not be built"
#     exit $status
# fi

for image in "${images[@]}"; do
    image_name=$(echo $image | sed 's#armdocker.rnd.ericsson.se/proj-mxe/##g')
    image_name=$(echo $image_name | sed 's#/#_#g' | sed 's#:#_#g')
    docker pull ${image}
    docker run -e TRIVY_USERNAME -e TRIVY_PASSWORD --user $(id -u):$(id -g) \
        $(for x in $(id -G); do printf " --group-add %s" "$x"; done) \
        -v /var/run/docker.sock:/var/run/docker.sock \
        -v $PWD:/mnt --rm armdocker.rnd.ericsson.se/proj-adp-cicd-drop/trivy-inline-scan:latest \
        ${image} 2>&1 | tee "${TRIVY_LOGS}/${image_name}.txt"
done

#cd ${REPORTS_DIR}
#docker run --init --rm --user $(id -u):$(id -g) \
#    -w ${PWD} -v ${PWD}:${PWD} -v /var/run/docker.sock:/var/run/docker.sock \
#    $(for x in $(id -G); do printf " --group-add %s" "$x"; done) \
#    armdocker.rnd.ericsson.se/proj-adp-cicd-drop/anchore-inline-scan:latest \
#    scan "${images[@]}"
