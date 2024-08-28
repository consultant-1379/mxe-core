#!/bin/bash

# Find Absolute path to this script. /home/user/bin/foo.sh using readlink
if [ "$(uname)" == "Darwin" ]; then
    SCRIPT=$(greadlink -f $0)
    LOG_DIR=/tmp/$(gdate +"%Y-%m-%dT%H:%M:%S.%N")
else
    SCRIPT=$(readlink -f $0)
    LOG_DIR=/tmp/$(date +"%Y-%m-%dT%H:%M:%S.%N")
fi
# Absolute path this script is in. /home/user/bin
SCRIPTPATH=`dirname $SCRIPT`


LOCAL_BASE_DIR="$(cd $SCRIPTPATH && cd ../ && pwd)/"
REMOTE_DIR=/mnt/disk0/enxxram/workspace/mee/depmanager/
REMOTE_USER=enxxram
REMOTE_SERVER=ce10018


mkdir -p ${LOG_DIR}
echo "Log dir is ${LOG_DIR}"

sync(){
    rsync -cavu --delete "${LOCAL_BASE_DIR}" "${REMOTE_USER}@${REMOTE_SERVER}:${REMOTE_DIR}" > "${LOG_DIR}/sync.log" 2>&1
    rsyncStatus=$?
    if [ ${rsyncStatus} -ne 0 ]
    then
        echo "make failed with $rsyncStatus"
        exit $rsyncStatus
    fi 
}

makeRemote(){

ssh "${REMOTE_USER}@${REMOTE_SERVER}" ARG1=${REMOTE_DIR} 'bash -s' <<'ENDSSH'
        cd $ARG1
        ls 
        export DOCKER_BUILDKIT=1
        sudo make PLATFORM=darwin/amd64 all 
        status=$?

        if [ $status -ne 0 ]
        then
            echo "make failed with $status"
        fi 

        exit $status
ENDSSH

    makeStatus=$?
    if [ $makeStatus -ne 0 ]
    then
            echo "make failed with ${rsyncStatus}"
            exit $rsyncStatus
    fi

}

copyBin(){
    rm $LOCAL_BASE_DIR/bin/*
    rmdir $LOCAL_BASE_DIR/bin
    scp -r "${REMOTE_USER}@${REMOTE_SERVER}:${REMOTE_DIR}bin/" "${LOCAL_BASE_DIR}bin/"
    copyStatus=$?   
    echo "Copy completed with statuscode $copyStatus"
    if [ ${copyStatus} -ne 0 ]
    then
        echo "make failed with $copyStatus"
        exit ${copyStatus}
    fi 
}


sync
makeRemote
copyBin