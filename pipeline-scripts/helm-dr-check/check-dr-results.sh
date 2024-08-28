#!/usr/bin/env bash
set -x

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
SCRIPTROOT=$(dirname $SCRIPTPATH)
REPOROOT=$(dirname $SCRIPTROOT)

use_config=${1:-true}
CHARTSTATE="${REPOROOT}/dr-check-results/chartDRState.log"

if [[ "$use_config" == "true" ]]
then
    if [ -f ${CHARTSTATE} ]; then
        cat ${CHARTSTATE}
        exit 1
    else
        exit 0
    fi
else
    exit 0
fi
