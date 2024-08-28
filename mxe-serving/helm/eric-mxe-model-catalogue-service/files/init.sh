#!/bin/sh
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

createBucket() {
  local BUCKET
  BUCKET=$1

  if ! checkBucketExists "$BUCKET"; then
    echo "Creating bucket '$BUCKET'"
    mc mb "myminio/$BUCKET"
  else
    echo "Bucket '$BUCKET' already exists."
  fi

  mc policy set none "myminio/$BUCKET"
}

checkUserExists() {
  local USER
  USER=$1

  mc admin user info myminio "$USER" > /dev/null 2>&1
  return $?
}

createUser() {
  local ACCESS_KEY
  ACCESS_KEY=$1

  local SECRET_KEY
  SECRET_KEY=$2

  if ! checkUserExists "$ACCESS_KEY"; then
    echo "Creating user '$ACCESS_KEY'"
    mc admin user add myminio "$ACCESS_KEY" "$SECRET_KEY"
  else
    echo "User '$ACCESS_KEY' already exists."
  fi
}

checkPolicyExists() {
  local POLICY
  POLICY=$1

  mc admin policy info myminio "$POLICY" > /dev/null 2>&1
  return $?
}

checkPolicyNotAttachedToUser() {
  local POLICY
  POLICY=$1

  local USER
  USER=$2

  local policyNotAttached=""
  local result=$(mc admin policy entities myminio/ --user $USER |grep -o $POLICY|wc -l)

  if [ "$result" -le 1 ]
  then
   policyNotAttached="true"
  else
   policyNotAttached="false"
  fi
  echo "$policyNotAttached"
}

createPolicy() {
  local POLICY
  POLICY=$1

  local BUCKET
  BUCKET=$2

  local ACTIONS
  ACTIONS=$3

  local ADDITIONAL_STATEMENTS
  ADDITIONAL_STATEMENTS=$4

  if [ -n "$ADDITIONAL_STATEMENTS" ]; then
    ADDITIONAL_STATEMENTS=",
    $ADDITIONAL_STATEMENTS"
  fi

  BUCKET_POLICY=$HOME/.mc/bucket-policy.json
  echo $BUCKET_POLICY
  cat - > $BUCKET_POLICY <<END
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": $ACTIONS,
      "Resource":["arn:aws:s3:::$BUCKET/*"]
    }$ADDITIONAL_STATEMENTS
  ]
}
END

  if ! checkPolicyExists "$POLICY"; then
    echo "Creating policy '$POLICY'"
    mc admin policy create myminio "$POLICY" $BUCKET_POLICY
  else
    echo "Policy '$POLICY' already exists."
  fi

  rm $BUCKET_POLICY
}

addPolicyToUser() {
  local POLICY
  POLICY=$1

  local USER
  USER=$2

  local checkPolicyNotAttached
  checkPolicyNotAttached=$(checkPolicyNotAttachedToUser "$POLICY" "$USER")


  if [ "$checkPolicyNotAttached" = "true" ]; then
   echo "Attaching Policy for user '$USER'"
   mc admin policy attach myminio "$POLICY" --user $USER
  else
   echo "Policy already attached to User '$USER'"
  fi
}

if [ "$MINIO_CREATE" = "true" ]; then
  connectToMinio

  createBucket "$MINIO_BUCKET"

  createUser "$MINIO_USER_ACCESS_KEY" "$MINIO_USER_SECRET_KEY"

  createPolicy "$MINIO_USER_ACCESS_KEY" "$MINIO_BUCKET" "$MINIO_USER_ACTIONS" "$MINIO_USER_STATEMENTS"

  addPolicyToUser "$MINIO_USER_ACCESS_KEY" "$MINIO_USER_ACCESS_KEY"
fi