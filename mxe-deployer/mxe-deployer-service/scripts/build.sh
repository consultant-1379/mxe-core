#!/bin/bash

# Absolute path to this script. /home/user/bin/foo.sh
if [ "$(uname)" == "Darwin" ]; then
    SCRIPT=$(greadlink -f $0)
else
    SCRIPT=$(readlink -f $0)
fi
# Absolute path this script is in. /home/user/bin
SCRIPTPATH=$(dirname $SCRIPT)
SERVICEROOT=$(dirname $SCRIPTPATH)
CWD=$(pwd)

cd $SERVICEROOT

go mod download 
GOOS=linux GOARCH=amd64 go build -o ./bin/depmanager ./dmserver/cmd/dmserver/main.go
status=$?
cd $CWD 

if [ $status -ne 0 ];
then 
    exit 1
fi
