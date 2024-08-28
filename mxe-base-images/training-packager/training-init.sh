#!/usr/bin/env bash

# Exit script on error
set -E

# Working directory
PACKAGE_SOURCE_LOCATION="/tmp/package"
PACKAGE_DEST_LOCATION="${HOME}/package"

# Log files
TRAINING_LOG_FILE="trainingLog"
TRAINING_LOG="${PACKAGE_DEST_LOCATION}/${TRAINING_LOG_FILE}"

#Training output directory
TRAINING_OUTPUT_DIR="output"
TRAINING_OUTPUT="${PACKAGE_DEST_LOCATION}/${TRAINING_OUTPUT_DIR}"

# record own PID
export PID=$$

NAMESPACE=$(cat /var/run/secrets/kubernetes.io/serviceaccount/namespace)

echo "Service Mesh MTLS enabled       : ${SERVICE_MESH_MTLS_ENABLED}"
if [ "${SERVICE_MESH_MTLS_ENABLED}" = true ] ; then
  until curl -fsI http://localhost:15021/healthz/ready
  do 
    echo "Waiting for Sidecar..."
    sleep 3 
  done
fi

echo "Model training..."
echo "Training id                             : ${TRAINING_JOB_ID}"
echo "Training job result repository url      : ${TRAINING_REPOSITORY_HOST}"
echo "Training job result repository bucket   : ${TRAINING_REPOSITORY_PORT}"
echo "Training job result repository accessKey: ${TRAINING_REPOSITORY_USER}"
echo "Model training service                  : ${MODEL_TRAINING_HOST}"
echo "Namespace                               : ${NAMESPACE}"
echo ""

MXE_TRAINING_JOBS_API="http://${MODEL_TRAINING_HOST}.${NAMESPACE}.svc.cluster.local:8080/v1/training-jobs"

exit_code=0

mkdir -p "${TRAINING_OUTPUT}"
cp -r ${PACKAGE_SOURCE_LOCATION}/* ${PACKAGE_DEST_LOCATION}/
chmod +x ${PACKAGE_DEST_LOCATION}/train.sh
touch "${TRAINING_LOG}"

set_error_message() {
  error_message="$1"
}

clear_error_message() {
  set_error_message "Error: Something went wrong"
}

clear_error_message

# Trap in SIGTERM or SIGINT
handle_sigterm() {
  set_error_message "Sigterm or sigint during training execution"
  pkill -P ${PID}
  handle_error
}

# Trap on error post creation
handle_error(){
  echo "Error handling..."
  echo "ERROR: ${error_message}" 1>&2
  errorLog=$(tail -200 < "${TRAINING_LOG}" | jq --raw-input --slurp '.')
  echo "ErrorLog: ${errorLog}"

  curl -X PATCH -H "Content-Type: application/json" -d @- "${MXE_TRAINING_JOBS_API}/${TRAINING_JOB_ID}" <<CURL_DATA
{
  "status": "failed"
}
CURL_DATA

  curl -X PATCH -H "Content-Type: application/json" -d @- "${MXE_TRAINING_JOBS_API}/${TRAINING_JOB_ID}" <<CURL_DATA
{
  "message": "${error_message//\"/\\\"}",
  "errorLog": ${errorLog}
}
CURL_DATA

  exit_code=1
}

#Running trainer shell script which starts the trainer python script
run_trainer() {
  echo "Run train.sh script from trainer package..."

  set_error_message "Training of \"${TRAINING_JOB_ID}\" failed"

  pushd "${PACKAGE_DEST_LOCATION}"
  bash -e ${PACKAGE_DEST_LOCATION}/train.sh  &>> "${TRAINING_LOG}"

  clear_error_message
}

save_result() {
  echo "Save training reults to training repository..."

  set_error_message "Failed to create zip archive with training results"

  pushd "${PACKAGE_DEST_LOCATION}"
  zip -Ar "${TRAINING_JOB_ID}" "${TRAINING_LOG_FILE}" "${TRAINING_OUTPUT_DIR}" &>> "${TRAINING_LOG}"

  clear_error_message
  set_error_message "Failed to save training results"

  local MC_SCHEME
  MC_SCHEME=$(sed -r 's;^([a-z]+)://.+$;\1;' <<< "${MINIO_URL}")

  local MC_HOST
  MC_HOST=${MINIO_URL#"$MC_SCHEME://"}

  export MC_HOST_trainingjobresultrepository="${MC_SCHEME}://${MINIO_ACCESS_KEY}:${MINIO_SECRET_KEY}@${MC_HOST}"

  mc cp "${TRAINING_JOB_ID}" "trainingjobresultrepository/${MINIO_BUCKET}/${TRAINING_JOB_ID}"

  unset MC_HOST_trainingjobresultrepository

  clear_error_message
}

set_success_status() {
  echo "Setting training status..."

  set_error_message "Modifying the training job's status has been failed!"

  curl -X PATCH \
    -d "{
        \"status\": \"completed\"
      }" -H "Content-Type: application/json" "${MXE_TRAINING_JOBS_API}/${TRAINING_JOB_ID}" &>> "${TRAINING_LOG}"

  clear_error_message
}

trap handle_error ERR
trap handle_sigterm SIGTERM SIGINT

run_trainer &
childProcess=$! 
wait "$childProcess"

save_result &
childProcess=$! 
wait "$childProcess"

if [ "$exit_code" -eq "0" ]; then
  set_success_status &
  childProcess=$!
  wait "$childProcess"
fi

if [ "${SERVICE_MESH_MTLS_ENABLED}" = true ] ; then
  # Job - running under istio needs to be killed
  echo "Kill the istio sidecar if running with istio and no error ?? "
  KILL_POST_RESPONSE=$(curl -X POST http://localhost:15000/quitquitquit)
  #KILL_POST_RETURN_CODE=${KILL_POST_RESPONSE: -3}
  #KILL_POST_RETURN_MESSAGE=${KILL_POST_RESPONSE:: -3}
  #echo "code: ${KILL_POST_RETURN_CODE}"
  #echo "message: ${KILL_POST_RETURN_MESSAGE}"
fi