#!/usr/bin/env bash
set -e

connectToMinio() {
  local ACCESS
  ACCESS=$(cat /run/secrets/config/accesskey)

  local SECRET
  SECRET=$(cat /run/secrets/config/secretkey)

  echo "Service Mesh enabled: $SERVICE_MESH_MTLS_ENABLED"
  if [ "${SERVICE_MESH_MTLS_ENABLED}" = true ] ; then
    MK_CMD="mkdir -p /home/mxe/.mc/certs/CAs"
    $MK_CMD
    CP_CMD="cp /run/secrets/certificates/trusted/* /home/mxe/.mc/certs/CAs/."
    $CP_CMD
  fi

  set +e ; # The connections to minio are allowed to fail.
  echo "Connecting to Minio server: $MINIO_URL"

  local MC_COMMAND
  MC_COMMAND="mc config host add myminio $MINIO_URL $ACCESS $SECRET"

  local STATUS

  $MC_COMMAND
  STATUS=$?

  until [ $STATUS = 0 ]; do
    echo \"Failed attempt!\"

    sleep 5

    $MC_COMMAND
    STATUS=$?
  done

  set -e

  return 0
}

checkBucketExists() {
  local BUCKET
  BUCKET=$1

  mc ls "myminio/$BUCKET" > /dev/null 2>&1
  return $?
}

deleteBucket() {
  local BUCKET
  BUCKET=$1

  if checkBucketExists "$BUCKET"; then
    echo "Deleting bucket '$BUCKET'"
    mc rb --force "myminio/$BUCKET"
  else
    echo "Bucket '$BUCKET' does not exist."
  fi
}

checkUserExists() {
  local USER
  USER=$1

  mc admin user info myminio "$USER" > /dev/null 2>&1
  return $?
}

deleteUser(){
  local ACCESS_KEY
  ACCESS_KEY=$1
  
  if checkUserExists "$ACCESS_KEY"; then
    echo "Deleting user '$ACCESS_KEY'"
    mc admin user remove myminio "$ACCESS_KEY"
  else
    echo "User '$ACCESS_KEY' does not exist."
  fi
}

deletePolicy() {
  local POLICY
  POLICY=$1
  echo "Attempting to remove policy '$POLICY'"
  mc admin policy remove myminio "$POLICY" > /dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "Policy '$POLICY' removed."
  else
    echo "Policy '$POLICY' does not exist."
  fi
}

connectToMinio 

if [ -z "$BUCKETS_TO_DELETE" ]
then 
    echo "$BUCKETS_TO_DELETE is empty"
    exit 1 
fi 

if [ -z "$USERS_TO_DELETE" ]
then 
    echo "$USERS_TO_DELETE is empty"
    exit 1 
fi 


MINIO_BUCKETS=(${BUCKETS_TO_DELETE//,/ })
MINIO_USERS=(${USERS_TO_DELETE//,/ })

for MINIO_USER in "${MINIO_USERS[@]}"; do
     deleteUser "$MINIO_USER"
     deletePolicy "$MINIO_USER"
done

for MINIO_BUCKET in "${MINIO_BUCKETS[@]}"; do
     deleteBucket "$MINIO_BUCKET"
done
