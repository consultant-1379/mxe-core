#!/usr/bin/env bash

# Exit script on error
set -eE
# set +x

# Log file
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
echo "Minio URL               : ${MINIO_URL}"
echo "Minio Bucket            : ${MINIO_BUCKET}"
echo "Minio AccessKey         : ${MINIO_ACCESS_KEY}"
echo "Docker Registry         : ${DOCKER_REGISTRY_HOSTNAME}"
echo "Model Catalogue Service : ${MODEL_CATALOG_HOST}"
echo "Author Service          : ${AUTHOR_SERVICE_HOST}:${AUTHOR_SERVICE_PORT}"
echo "Pypi Internal Server    : ${PYPISERVICE_INTERNAL_SERVER}"
echo "Pypi External Server    : ${PYPISERVICE_EXTERNAL_SERVER}"
echo "Namespace               : ${NAMESPACE}"

temp_location=$(mktemp -d mxe-packager-XXXXXX -p /tmp)

if [ "${PACKAGE_TYPE}" == "source" ] || [ "${PACKAGE_TYPE}" == "archive" ]; then
  ENDPOINT="models"
  source_location="${temp_location}/source"
else
  set_error_message "Unknown package type: \"${PACKAGE_TYPE}\""
  error_pre_creation 0
fi

export DOCKER_CONFIG=/mnt/.docker

MXE_MODEL_CATALOG_API="http://${MODEL_CATALOG_HOST}.${NAMESPACE}.svc.cluster.local:8080/v1/${ENDPOINT}"
echo "Mxe model catalog api endpoint : ${MXE_MODEL_CATALOG_API}"

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
  delete_minio_package
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
  delete_minio_package
  exit "${exitCode}"
}

# Trap on error post creation
error_post_creation(){
  set_model_status_error
  echo "Exiting from post creation with exit code 1"
  delete_minio_package
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
  echo "Loading package from repo"
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

load_image_from_archive() {
  echo "Load image from archive"
  local image_filename="mxe-image.tar"
  local publickey_filename="mxe-author.pub"
  local signature_filename="mxe-image-id.sig"

  set_error_message "The received package was corrupt: Cannot extract files from package archive!"
  tar -C "${temp_location}" -xvf "${temp_location}/${PACKAGE_NAME}" &> "${LOG}"

  if [ ! -f "${temp_location}/${image_filename}" ] || [ ! -f "${temp_location}/${publickey_filename}" ] || [ ! -f "${temp_location}/${signature_filename}" ]; then
    set_error_message "The received package was incomplete."
    error_pre_creation 0
  fi

  set_error_message "The received package was not valid image-archive!"
  valid_image_archive=$(crane validate --tarball "${temp_location}/${image_filename}" | awk -F':' '{print $1}' )
  if [ "${valid_image_archive}" != "PASS" ]; then
    set_error_message "The received package was not valid image-archive: \"${DUMMY_MODEL_ENTRY_NAME}\""
    error_pre_creation 0
  fi

  set_error_message "The author verification cannot be performed."
  author_verify_public_key=$(cat "${temp_location}/${publickey_filename}")
  local author_verify_url="${AUTHOR_SERVICE_HOST}:${AUTHOR_SERVICE_PORT}/v1/authors/verify"
  local author_verify_result
  author_verify_result="$(curl -X POST --header "Content-Type: application/json" -s "${author_verify_url}" --data-binary "@${temp_location}/${publickey_filename}")"

  local author_verify_status
  author_verify_status=$(jq '.result' <<< "${author_verify_result}")
  author_verify_name=$(jq '.name' <<< "${author_verify_result}")

  if [[ "${author_verify_status}" != "true" ]]; then
    set_error_message "The public key of the archive is not registered."
    error_pre_creation 0
  fi

  local image_archive_digest
  image_archive_digest="sha256:"$(tar tf "${temp_location}/${image_filename}" | grep -E '^[[:xdigit:]]*\.json$' | head -n 1 | cut -f1 -d.)

  set_error_message "Model image signature verification failed."
  openssl dgst -sha256 -verify "${temp_location}/${publickey_filename}" -signature "${temp_location}/${signature_filename}" <(echo -n "${image_archive_digest}") &> "${LOG}" \
    || error_pre_creation 0

  set_error_message "Cannot extract filesystem from image-archive \"${DUMMY_MODEL_ENTRY_NAME}\""
  local wd untar_location target_dir
  untar_location=$(mktemp -d image-XXXXXX -p /tmp)
  target_dir=$(mktemp -d filesystem-XXXXXX -p /tmp)
  layers=($(tar -Oxf "${temp_location}/${image_filename}" manifest.json | jq -r '.[].Layers[]'))
  tar xf "${temp_location}/${image_filename}" -C ${untar_location}
  for i in ${layers[@]}; do
      echo "Extracting ${untar_location}/$i"
      tar -xf ${untar_location}/$i -C $target_dir
  done

  set_error_message "Cannot fetch the MXE-META-INF/INFO file from the archive \"${DUMMY_MODEL_ENTRY_NAME}\""
  cp "${target_dir}/microservice/MXE-META-INF/INFO" "${source_location}/" 2> "${LOG}"

  cp "${target_dir}/microservice/MXE-META-INF/icon.jpg"  "${source_location}/" || true
  cp "${target_dir}/microservice/MXE-META-INF/icon.jpeg" "${source_location}/" || true
  cp "${target_dir}/microservice/MXE-META-INF/icon.png"  "${source_location}/" || true
  cp "${target_dir}/microservice/MXE-META-INF/icon.gif"  "${source_location}/" || true

  set_error_message "Cannot delete image archive contents \"${DUMMY_MODEL_ENTRY_NAME}\""
  rm -Rf "${untar_location}" 2> "${LOG}"
  rm -Rf "${target_dir}" 2> "${LOG}"

  clear_error_message
}

# Parsing the meta inf file into the metainf array
load_meta_inf() {
  echo "Load Meta info"
  
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

  echo "Package type: ${metainf["Type"]}"

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
build_image() {
  pushd "${source_location}"

  echo "$(executor version)"
  
  local ENVS="ENV PIP_USER=true\n"

  # External Pypi Server ENV may be empty. Check and proceed accordingly
  if [ -z "${PYPISERVICE_EXTERNAL_SERVER}" ]; then
    echo "ENV PYPISERVICE_EXTERNAL_SERVER is not set."
    ENVS+="ENV PIP_INDEX_URL=${PYPISERVICE_INTERNAL_SERVER}\n"
  else
    # Check Availability of external Pypi server
    local external_pypi_response=$(curl -sIL -w '%{http_code}' -o /dev/null -m 5 ${PYPISERVICE_EXTERNAL_SERVER} || true)

    # External Pypi Server is not available
    if [ "${external_pypi_response}" -ne "200" ]; then
      echo "${PYPISERVICE_EXTERNAL_SERVER} Unavailable: ${external_pypi_response}"
      ENVS+="ENV PIP_INDEX_URL=${PYPISERVICE_INTERNAL_SERVER}\n"
    # External Pypi Server is available, set it as PIP_INDEX_URL
    else
      echo "${PYPISERVICE_EXTERNAL_SERVER} available. Setting ${PYPISERVICE_EXTERNAL_SERVER} as pip index URL"
      ENVS+="ENV PIP_INDEX_URL=${PYPISERVICE_EXTERNAL_SERVER}\n"
    fi
  fi

  echo "Adding ${PYPISERVICE_INTERNAL_SERVER} as the pip extra index URL"
  ENVS+="ENV PIP_EXTRA_INDEX_URL=${PYPISERVICE_INTERNAL_SERVER}\n"

  local pypi_internal_host=$(echo ${PYPISERVICE_INTERNAL_SERVER} | awk -F[/:] '{print $4}')
  ENVS+="ENV PIP_TRUSTED_HOST=${pypi_internal_host}\n"

  echo "Envs are"
  echo "$ENVS"

  MODEL_ENVS=$(sed /^#/d config.env | awk -F= 'NF==2 {gsub(/^[ \t]+/,"",$2); gsub(/[ \t]+$/,"",$2); printf "ENV %s=%s\n", $1, $2 } ')
  echo "Model Envs are"
  echo "$MODEL_ENVS"

  awk -v r="$MODEL_ENVS" -v r1="$ENVS" '{sub(/%MODEL_ENVS%/,r);sub(/%ENVS%/,r1)}1' /mnt/dockerfile/Dockerfile > ${PWD}/Dockerfile

  echo -e "\n\nDockerfile content is:"
  cat ${PWD}/Dockerfile

  # Generating the seldon model image using kaniko
  set_error_message "Something went wrong with kaniko building with the archive \"${DUMMY_MODEL_ENTRY_NAME}\""
  echo "executor --use-new-run --compressed-caching=false --dockerfile  ${PWD}/Dockerfile --context ${PWD} --destination \"${DOCKER_REGISTRY_HOSTNAME}/${metainf[Id]}:${metainf[Version]}\""
  executor --use-new-run --compressed-caching=false --dockerfile ${PWD}/Dockerfile --context ${PWD} --destination "${DOCKER_REGISTRY_HOSTNAME}/${metainf[Id]}:${metainf[Version]}" 2>&1

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

delete_minio_package() {
    #REST API call to delete the package from minio post succesful completion of model onboarding
    set_error_message "Deleting the package from minio has failed!"
    curl -s -X DELETE "${MXE_MODEL_CATALOG_API}/package/${PACKAGE_NAME}"
    clear_error_message
}

packagingSteps() {
  trap error_pre_creation ERR

  if [ "${PACKAGE_TYPE}" == "source" ] || [ "${PACKAGE_TYPE}" == "archive" ]; then
    load_package_from_repo
  fi

  # Extracting the downloaded package file
  if [ "${PACKAGE_TYPE}" == "source" ]; then
    set_error_message "Something went wrong with unzipping the archive \"${DUMMY_MODEL_ENTRY_NAME}\""
    echo "unzip ${temp_location}/${PACKAGE_NAME} -d ${source_location}"
    unzip "${temp_location}/${PACKAGE_NAME}" -d "${source_location}"
    clear_error_message
  elif [ "${PACKAGE_TYPE}" == "archive" ]; then
    load_image_from_archive
  fi

  if [ "${PACKAGE_TYPE}" == "source" ]; then
    load_meta_inf "${source_location}/MXE-META-INF"
  elif [ "${PACKAGE_TYPE}" == "archive" ]; then
    load_meta_inf "${source_location}"
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

  if [ "${PACKAGE_TYPE}" == "source" ]; then
    set_error_message "Building model image from source code has failed!"
    echo "build_image ${MODEL_IMAGE_INTERNAL}"
    build_image "${MODEL_IMAGE_INTERNAL}"
    set_model_status "available"
    if [ $? -eq 0 ]; then
        delete_minio_package
    fi
  elif [ "${PACKAGE_TYPE}" == "archive" ]; then
    # Pushing the image archive to internal Docker registry
    set_error_message "Pushing to the internal Docker registry has failed!"
    echo "crane push ${temp_location}/mxe-image.tar ${MODEL_IMAGE_INTERNAL}"
    crane push "${temp_location}/mxe-image.tar" "${MODEL_IMAGE_INTERNAL}" &> "${LOG}"
    set_model_status "available"
    if [ $? -eq 0 ]; then
        delete_minio_package
    fi
  fi
}

#Handler for SIGTERM and SIGINT
trap sigterm_handling SIGTERM SIGINT

packagingSteps &
childProcess=$!
wait "$childProcess"

echo "Service Mesh enabled: $SERVICE_MESH_MTLS_ENABLED"
if [ "${SERVICE_MESH_MTLS_ENABLED}" = true ] ; then
  # Job - running under istio needs to be killed
  echo "Kill the istio sidecar if running with istio and no error ?? "
  KILL_POST_RESPONSE=$(curl -X POST http://localhost:15000/quitquitquit)
  # KILL_POST_RETURN_CODE=${KILL_POST_RESPONSE: -3}
  # KILL_POST_RETURN_MESSAGE=${KILL_POST_RESPONSE:: -3}
  # echo "code: ${KILL_POST_RETURN_CODE}"
  # echo "message: ${KILL_POST_RETURN_MESSAGE}"
fi