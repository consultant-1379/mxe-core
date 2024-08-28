#!/usr/bin/env bash
# Run any task/rule from any RULESET.  Export RULE_SET as env variable with value set to ruleset.yaml name and invoke this script
# Ex:
# export RULE_SET="ruleset2.0-va.yaml"

set -x 
set -e 

# RuleName or RuleName:TaskName to run
RULE_NAME="${1}"
# Rule set file to use. Check for 2nd positional parameter, if not available set to the value of env variable $RULE_SET
RULESET="${2:-${RULE_SET}}"

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
REPOROOT=$(dirname $SCRIPTPATH)
LOG_DIR="${REPOROOT}/logs"

if [ -z "${RULESET}" ]
then 
    RULESET="ruleset2.0.yaml"
fi 

BOB_BINARY="${REPOROOT}/bob/bob"
RULESET_PATH="${REPOROOT}/${RULESET}"

mkdir -p "${LOG_DIR}"

LOG_FILE="${LOG_DIR}/$(echo $RULE_NAME | sed 's/:/_/g').log"

"${BOB_BINARY}" -r "${RULESET_PATH}" "${RULE_NAME}" 2>&1 | tee "${LOG_FILE}"

bobExitCode=${PIPESTATUS[0]}

exit "${bobExitCode}"