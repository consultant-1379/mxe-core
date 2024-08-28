#!/usr/bin/env bash
set -ex

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
REPOROOT=$(dirname $(dirname $SCRIPTPATH))

download_vuln_db(){
    if [[ -z $GRYPE_DB_VERSION ]]
    then
        echo "Fatal: Env GRYPE_DB_VERSION is not set, No grype db version specified, exit.."
        exit 1 
    fi 
    echo "Downloading vulnerability database for $GRYPE_DB_VERSION" 
    wget "https://toolbox-data.anchore.io/grype/databases/vulnerability-db_${GRYPE_DB_VERSION}.tar.gz"
}

download_vuln_db