#!/usr/bin/env bash
set -x

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
SCRIPTROOT=$(dirname $SCRIPTPATH)
REPOROOT=$(dirname $SCRIPTROOT)

mxe_int_chart=$1
use_config=${2:-true}

printarr() { declare -n __p="$1"; for k in "${!__p[@]}"; do printf "%s=%s\n" "$k" "${__p[$k]}" ; done ;  }

check_for_newline_at_end_of_file() {
    local file=$1
    local newline_count=$(tail -c1 $file | wc -l)
    if [ $newline_count -ne 1 ]; then
        echo "ERROR: $file does not end with a newline. Please add a newline at the end of the file."
        exit 1
    fi
}


OUTPUT_DIR="${REPOROOT}/dr-check-results/$mxe_int_chart"

[[ -d ${OUTPUT_DIR} ]] && rm -rf "${OUTPUT_DIR}"

DR_CONFIG_FILE=${SCRIPTPATH}/helm-dr-check.conf
check_for_newline_at_end_of_file $DR_CONFIG_FILE

declare -A dr_args_by_chart
while IFS=':' read -r helmChartName drArgs
do
  echo $helmChartName
  echo "$drArgs"
  dr_args_by_chart[$helmChartName]="$drArgs"
done < $DR_CONFIG_FILE

DR_RULES_TO_BE_FIXED_CONFIG_FILE=${SCRIPTPATH}/helm-dr-check-to-be-fixed.conf
check_for_newline_at_end_of_file $DR_RULES_TO_BE_FIXED_CONFIG_FILE

declare -A disabled_drs_to_be_fixed_by_chart
while IFS=':' read -r helmChartName drArgs
do
  echo $helmChartName
  echo "$drArgs"
  disabled_drs_to_be_fixed_by_chart[$helmChartName]="$drArgs"
done < $DR_RULES_TO_BE_FIXED_CONFIG_FILE

echo "Array dr_args_by_chart:"
printarr dr_args_by_chart

echo "Array disabled_drs_to_be_fixed_by_chart:"
printarr disabled_drs_to_be_fixed_by_chart

integrationChartValues=${REPOROOT}/${mxe_int_chart}/helm/${mxe_int_chart}/values.yaml
run_dr_check(){
    local helmChartDir=$1
    local dr_check_status=0
    echo "Scanning chartDir ${helmChartDir} .."
    local helmChartName=$(basename $helmChartDir)
    dr_check_outputDir="${OUTPUT_DIR}/${helmChartName}"
    mkdir -p "${dr_check_outputDir}"
    dr_check_log_file="${dr_check_outputDir}/dr_check.log"
    dr_args=""
    if [ -v 'dr_args_by_chart[$helmChartName]' ] && [ "$use_config" == "true" ]; then
      dr_args="${dr_args_by_chart[$helmChartName]}"
    fi
    if [ -v 'disabled_drs_to_be_fixed_by_chart[$helmChartName]' ] && [ "$use_config" == "true" ]; then
      dr_args="${dr_args} ${disabled_drs_to_be_fixed_by_chart[$helmChartName]}"
    fi

    if [ -f ${integrationChartValues} ]; then
        echo "Running command helm-dr-check --helm-v3 --helm-chart ${helmChartDir} -l DEBUG -output "${dr_check_outputDir}" --values ${integrationChartValues} ${dr_args} 2>&1 > $dr_check_log_file" > $dr_check_log_file
        helm-dr-check --helm-v3 --helm-chart ${helmChartDir} -l DEBUG -output "${dr_check_outputDir}" --values ${integrationChartValues} ${dr_args} 2>&1 >> $dr_check_log_file
    else
        echo "Running commandhelm-dr-check --helm-v3 --helm-chart ${helmChartDir} -l DEBUG -output "${dr_check_outputDir}" ${dr_args} 2>&1 > $dr_check_log_file" > $dr_check_log_file
        helm-dr-check --helm-v3 --helm-chart ${helmChartDir} -l DEBUG -output "${dr_check_outputDir}" ${dr_args} 2>&1 > $dr_check_log_file
    fi
    dr_check_status=$?
    if [[ $dr_check_status -ge 1 ]]
    then
        dr_check_status=1
    fi
    return $dr_check_status
}

declare -A procs
 for helmChartDir in $(ls -1d ${REPOROOT}/${mxe_int_chart}/helm/*)
 do
        helmChartName=$(basename $helmChartDir)
        echo $helmChartName
        if [[ $helmChartName != $mxe_int_chart ]]; then
            procs["$helmChartName"]="run_dr_check ${helmChartDir}"
        fi
 done

# number of processes
num_procs=${#procs[@]}
echo "Number of sub-processes to be spawned: ${num_procs}"

# spawn sub-processes
declare -A pids
for helmChartName in "${!procs[@]}"; do
    echo "cmd = ${procs[$helmChartName]}"
    ${procs[$helmChartName]} &
    pids[$helmChartName]="$!"
    echo "    pid = ${pids[$helmChartName]}"
done


consolidated_status_code=0

echo "dr-check-results for $mxe_int_chart:"
echo -e "\t HELM_CHART_DIR \t DR_CHECK_STATUS"
for helmChartName in "${!pids[@]}"; do
    wait "${pids[$helmChartName]}"
    dr_check_status="$?"
    echo -e "\t ${helmChartName} \t ${dr_check_status}"
    consolidated_status_code=$(( ${consolidated_status_code} | ${dr_check_status} ))
done

CHARTSTATE="${REPOROOT}/dr-check-results/chartDRState.log"
if [[ $consolidated_status_code -eq 1 ]]
then
    echo -e "\n\nOne or more charts have failed DR checks"
    echo "$mxe_int_chart:$consolidated_status_code" >> $CHARTSTATE
fi

exit 0
