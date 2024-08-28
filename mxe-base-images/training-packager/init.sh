#!/usr/bin/env bash

# Exit script on error
set -eE

# # Log file
LOG="/log$$"
touch "${LOG}"

# record own PID
export PID=$$

# Saving package id and version
declare -A metainf=()
metainf[Id]="${DUMMY_MODEL_ENTRY_NAME}"
metainf[Version]="unknown"
declare -p metainf > /metainf
author_verify_name="null"
author_verify_public_key=""

NAMESPACE=$(cat /var/run/secrets/kubernetes.io/serviceaccount/namespace)

echo "Service Mesh MTLS enabled       : ${SERVICE_MESH_MTLS_ENABLED}"
if [ "${SERVICE_MESH_MTLS_ENABLED}" = true ] ; then
  until curl -fsI http://localhost:15021/healthz/ready
  do 
    echo "Waiting for Sidecar..."
    sleep 3 
  done
fi

echo "Packaging..."
echo "Dummy model entry       : ${DUMMY_MODEL_ENTRY_NAME}"
echo "Package name            : ${PACKAGE_NAME}"
echo "Package type            : ${PACKAGE_TYPE}"
echo "Minio url               : ${MINIO_URL}"
echo "Minio bucket            : ${MINIO_BUCKET}"
echo "MinioaccessKey          : ${MINIO_ACCESS_KEY}"
echo "Docker registry         : ${DOCKER_REGISTRY_HOSTNAME}"
echo "Docker host             : ${DOCKER_IN_DOCKER_HOST}"
echo "Model catalogue service : ${MODEL_CATALOG_HOST}"
echo "Author service          : ${AUTHOR_SERVICE_HOST}:${AUTHOR_SERVICE_PORT}"
echo "Namespace               : ${NAMESPACE}"

temp_location=$(mktemp -d mxe-packager-XXXXXX -p /tmp)
export DOCKER_CONFIG=/mnt/.docker/

if [ "${PACKAGE_TYPE}" == "training_package_source" ]; then
  ENDPOINT="training-packages"
  source_location="/training/package"
else
  set_error_message "Unknown package type: \"${PACKAGE_TYPE}\""
  error_pre_creation 0
fi

MXE_MODEL_CATALOG_API="http://${MODEL_CATALOG_HOST}.${NAMESPACE}.svc.cluster.local:8080/v1/${ENDPOINT}"

mkdir -p "${source_location}"

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
  set_model_status_error
  pkill -P ${PID}
  echo "Exiting from packaging with exit code 1"
  exit 1
}

# Trap on error pre creation
error_pre_creation(){
  set_model_status_error

  if [ -z "${1}" ]; then
    exitCode=1
  else
    exitCode=${1}
  fi
  echo "Exiting from pre creation with exit code ${exitCode}"
  exit "${exitCode}"
}

# Trap on error post creation
error_post_creation(){
  set_model_status_error
  echo "Exiting from post creation with exit code 1"
  exit 1
}

set_model_status_error() {
  echo "ERROR: ${error_message}" 1>&2

  local errorLog
  errorLog=$(jq --raw-input --slurp < ${LOG})

  . /metainf

  curl -s -X PATCH -H "Content-Type: application/json" -d @- "${MXE_MODEL_CATALOG_API}/${metainf[Id]}/${metainf[Version]}" <<CURL_DATA
{
  "status": "error",
  "message": "${error_message//\"/\\\"}",
  "errorLog": ${errorLog}
}
CURL_DATA
}

set_model_status() {
  set_error_message "Modifying the model's status to ${1} has failed!"

  . /metainf

  curl -s -X PATCH \
    -d "{
        \"status\": \"${1}\"
      }" -H "Content-Type: application/json" "${MXE_MODEL_CATALOG_API}/${metainf[Id]}/${metainf[Version]}" &> "${LOG}"

  clear_error_message
}

# Downloading the package file from the model repository
load_package_from_repo() {
  set_error_message "Cannot connect to Minio!"

  local MC_SCHEME
  MC_SCHEME=$(sed -r 's;^([a-z]+)://.+$;\1;' <<< "${MINIO_URL}")

  local MC_HOST
  MC_HOST=${MINIO_URL#"$MC_SCHEME://"}

  export MC_HOST_repository="${MC_SCHEME}://${MINIO_ACCESS_KEY}:${MINIO_SECRET_KEY}@${MC_HOST}"

  mc cp "repository/${MINIO_BUCKET}/${PACKAGE_NAME}" "${temp_location}/${PACKAGE_NAME}" 

  unset MC_HOST_repository

  if [ ! -f "${temp_location}/${PACKAGE_NAME}" ]; then
    set_error_message "Archive \"${DUMMY_MODEL_ENTRY_NAME}\" cannot be found in our model repository !"
    error_pre_creation 0
  fi

  clear_error_message
}

# Parsing the meta inf file into the metainf array
load_meta_inf() {
  if [ ! -f "$1/INFO" ]; then
    set_error_message "\"MXE-META-INF/INFO\" file cannot be found in the archive \"${DUMMY_MODEL_ENTRY_NAME}\""
    error_pre_creation 0
  fi

  local description
  while read -r line || [ -n "$line" ]; do
    if [ -n "${description}" ]; then
      printf -v description "%s\n%s" "${description}" "$line"
    else
      IFS=':' read -r key value <<< "$line"
      if [ -n "${key}" ]; then
        if [[ "${key}" == "Description" ]]; then
          description=$(sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//' <<< "${value}")
        else
          metainf["${key}"]=$(sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//' <<< "${value}")
        fi
      fi
    fi
  done < "$1/INFO" &> "${LOG}"

  # Strip empty lines from the end of description
  description=$(sed -e :a -e '/^\n*$/{$d;N;};/\n$/ba' <<< "${description}")
  description=$(jq --raw-input --slurp <<< "${description}")
  metainf[Description]="${description}"

  for required_key in Title Id Version Author Description; do
    if [ -z "${metainf["$required_key"]}" ]; then
      set_error_message "\"MXE-META-INF/INFO\" doesn't contain required parameter ${required_key} in the archive \"${DUMMY_MODEL_ENTRY_NAME}\""
      error_pre_creation 0
    fi
  done

  echo "Package type:"
  echo ${metainf["Type"]}

  for ext in jpg jpeg gif png ; do
    local icon_file="$1/icon.${ext}"
    if [ -f "${icon_file}" ]; then
      local icon_mime icon_location
      icon_mime="$(file --brief --mime-type "${icon_file}")"
      icon_location="${icon_file}"
      break
    fi
  done
  if [ -n "${icon_mime}" ] && [ -n "${icon_location}" ]; then
    local icon_data
    icon_data=$(base64 "${icon_location}" 2> "${LOG}")
    metainf[Icon]="data:${icon_mime};charset=utf-8;base64,${icon_data}"
  fi

  clear_error_message
}

# Build the image of the model from its source
build_training_image() {
  pushd "/training"
  echo "Kaniko version: $(executor version)"

  cp /mnt/dockerfile/Dockerfile .

  local pypi_registry="https://pypi.org/simple"
  echo "Checking the availability of ${pypi_registry} ..."
  local pypi_response=$(curl -sIL -w '%{http_code}' -o /dev/null -m 5 ${pypi_registry} || true)
  local pypiservice_host_full="http://${PYPISERVICE_HOST}:8080/simple/"
  local offline

  if [ "${pypi_response}" -ne "200" ]; then offline=1; else offline=0; fi

  set_error_message "Pushing to the internal Docker registry has failed!"
  echo "executor --dockerfile ${PWD}/Dockerfile --context ${PWD} --destination ${MODEL_IMAGE_INTERNAL} --build-arg TRAINING_SCRIPT_DIR=package --build-arg OFFLINE=${offline} --build-arg PYPISERVICE_HOST=${PYPISERVICE_HOST} --build-arg PYPISERVICE_URL=${pypiservice_host_full}"
  executor --dockerfile ${PWD}/Dockerfile --context ${PWD} --destination ${MODEL_IMAGE_INTERNAL} --build-arg TRAINING_SCRIPT_DIR=package --build-arg OFFLINE=${offline} --build-arg PYPISERVICE_HOST=${PYPISERVICE_HOST} --build-arg PYPISERVICE_URL=${pypiservice_host_full} 2>&1
  
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

packagingSteps() {
  trap error_pre_creation ERR

  if [ "${PACKAGE_TYPE}" == "training_package_source" ]; then
    load_package_from_repo
  fi

  # Extracting the downloaded package file
  if [ "${PACKAGE_TYPE}" == "training_package_source" ]; then
    set_error_message "Something went wrong with unzipping the archive \"${DUMMY_MODEL_ENTRY_NAME}\""
    echo "unzip ${temp_location}/${PACKAGE_NAME} -d ${source_location}"
    unzip "${temp_location}/${PACKAGE_NAME}" -d "${source_location}"
    clear_error_message
  fi

  if [ "${PACKAGE_TYPE}" == "training_package_source" ]; then
    load_meta_inf "${source_location}/MXE-META-INF"
  fi

  # Setting model image name based on the meta info
  MODEL_IMAGE_INTERNAL="${DOCKER_REGISTRY_HOSTNAME}/${metainf[Id]}:${metainf[Version]}"
  MODEL_IMAGE_EXTERNAL="${metainf[Id]}:${metainf[Version]}"

  set_error_message "Replacing the dummy model has failed!"

  POST_RESPONSE=$(curl -s -w "%{http_code}" -X POST -H "Content-Type: application/json" -d @- "${MXE_MODEL_CATALOG_API}/${DUMMY_MODEL_ENTRY_NAME}/unknown" <<CURL_DATA
  {
    "id": "${metainf[Id]}",
    "description": ${metainf[Description]},
    "version": "${metainf[Version]}",
    "title": "${metainf[Title]}",
    "author": "${metainf[Author]}",
    "image": "${MODEL_IMAGE_EXTERNAL}",
    "icon": "${metainf[Icon]}",
    "signedByName": ${author_verify_name},
    "signedByPublicKey": "${author_verify_public_key}"
  }
CURL_DATA
    )

  POST_RETURN_CODE=${POST_RESPONSE: -3}
  POST_RETURN_MESSAGE=${POST_RESPONSE:: -3}

  echo "Response of replacing dummy model"
  echo "code: ${POST_RETURN_CODE}"
  echo "message: ${POST_RETURN_MESSAGE}"

  if [ "${POST_RETURN_CODE}" == "409" ]; then
    set_error_message "A model already exists with id \"${metainf[Id]}\" and version \"${metainf[Version]}\""
    error_pre_creation 0
  fi

  if [ "${POST_RETURN_CODE}" == "400" ]; then
    set_error_message "An internal error happened while updating model data (hint: check INFO file encoding, it must be UTF-8)"
    error_pre_creation 0
  fi

  declare -p metainf > /metainf

  if [ "${POST_RETURN_CODE}" == "403" ]; then
    set_error_message "There is no permission to onboard model ${metainf[Id]}"
    error_pre_creation 0
  fi

  clear_error_message

  # Set a new trap for new record
  trap - ERR
  trap error_post_creation ERR

  set_model_status "packaging"

  build_training_image "${MODEL_IMAGE_INTERNAL}"

  set_model_status "available"

}

#Handler for SIGTERM and SIGINT
trap sigterm_handling SIGTERM SIGINT

packagingSteps &
childProcess=$!
wait "$childProcess"

if [ "${SERVICE_MESH_MTLS_ENABLED}" = true ] ; then
  # Job - running under istio needs to be killed
  echo "Kill the istio sidecar if running with istio and no error ?? "
  KILL_POST_RESPONSE=$(curl -X POST http://localhost:15000/quitquitquit)
  #KILL_POST_RETURN_CODE=${KILL_POST_RESPONSE: -3}
  #KILL_POST_RETURN_MESSAGE=${KILL_POST_RESPONSE:: -3}
  #echo "code: ${KILL_POST_RETURN_CODE}"
  #echo "message: ${KILL_POST_RETURN_MESSAGE}"
fi