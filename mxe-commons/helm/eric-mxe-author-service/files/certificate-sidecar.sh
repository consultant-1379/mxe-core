#!/usr/bin/env bash

set -x

if [ -z "${SOURCE}" ] || [ -z "${DESTINATION}" ] || [ -z "${RUN_COMMAND}" ]; then
  echo "Mandatory variables SOURCE/DESTINATION/RUN_COMMAND not set"
  exit 1
fi

if [ ! -d ${DESTINATION} ]
then
    mkdir -p ${DESTINATION}
fi

echo "Source: " ${SOURCE}
echo "Destination: " ${DESTINATION}
echo "Run Command: " ${RUN_COMMAND}

inotifywait -q -m -r --excludei ..$(date +"%Y") ${SOURCE} | while read DIRECTORY EVENT FILE; do
    case $EVENT in
        MOVED_TO*)
            TIMESTAMP=`date`
            echo "[${TIMESTAMP}]: The file ${DIRECTORY}${FILE} was modified"
            eval "$RUN_COMMAND"
            ;;
    esac
done