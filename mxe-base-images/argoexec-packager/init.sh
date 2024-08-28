#!/usr/bin/env bash

# Exit script on error
set -eE
# set +x

# Log file
LOG="/log$$"
touch "${LOG}"

# record own PID
export PID=$$

NAMESPACE=$(cat /var/run/secrets/kubernetes.io/serviceaccount/namespace)

if [ ! -z "${ADD_EXTERNAL_INSTALLER_DOCKER_REGISTRY_CA}" ]; then
  cp /mnt/trustedregistry/ca.crt /usr/share/pki/trust/anchors/installerregistryca.crt;
  cp /mnt/trusted/ca/ca.crt /usr/share/pki/trust/anchors/siptlsca.crt;
  /usr/sbin/update-ca-certificates;
fi

until curl -fsI http://localhost:15021/healthz/ready
do 
  echo "Waiting for Sidecar..."
  sleep 3 
done


echo "Creating argoexec image..."
echo "Docker Registry         : ${DOCKER_REGISTRY_HOSTNAME}"
echo "Namespace               : ${NAMESPACE}"

mkdir -p /mnt/.docker
cp /mnt/dockersecret/.dockerconfigjson /mnt/.docker/config.json
export DOCKER_CONFIG=/mnt/.docker/

set_error_message() {
  # echo "${LOG}"
  error_message="$1"
  true > "${LOG}"
}

clear_error_message() {
  set_error_message "Something went wrong"
}

clear_error_message

# Trap on sigterm
sigterm_handling(){
  set_error_message "Sigterm or sigint during packaging"
  pkill -P ${PID}
  echo "Exiting from packaging with exit code 1"
  exit 1
}

# Build the argoexec using Dockerfile
build_image() {

  pushd ${WORK_DIR}

  echo "$(executor version)"
  cp /mnt/trusted/ca/ca.crt ${PWD}/ca.crt
  cp /mnt/dockerfile/Dockerfile  ${PWD}/Dockerfile

  echo -e "\n\nDockerfile content is:"
  cat ${PWD}/Dockerfile

  set_error_message "Something went wrong with kaniko building with the archive \"${DUMMY_MODEL_ENTRY_NAME}\""
  echo "executor --dockerfile  ${PWD}/Dockerfile --context ${PWD} --destination \"${DOCKER_REGISTRY_HOSTNAME}/argoexec:1.0.0\""
  executor --dockerfile ${PWD}/Dockerfile --context ${PWD} --destination "${DOCKER_REGISTRY_HOSTNAME}/argoexec:1.0.0" 2>&1
  
  pushed=$?
  if [[ $pushed == 0 ]]; then
    echo "Image pushed successfully"
  else
    echo "Error in pushing the image"
    exit 1
  fi
  clear_error_message
  popd
}

#Handler for SIGTERM and SIGINT
trap sigterm_handling SIGTERM SIGINT

build_image &
childProcess=$!
wait "$childProcess"

echo "Kill the istio sidecar if running with istio and no error ?? "
KILL_POST_RESPONSE=$(curl -X POST http://localhost:15000/quitquitquit)
echo "message: ${KILL_POST_RETURN_MESSAGE}"