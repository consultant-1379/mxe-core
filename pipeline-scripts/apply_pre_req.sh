#!/usr/bin/env bash
#
# COPYRIGHT Ericsson 2023
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

set -x
initialize(){
    release_version=$PREV_RELEASE_VERSION
    drop_version=$UPGRADE_VERSION
    pre_req_script_path="$(dirname $0)"
    init_path="$(dirname $0)/../"
}

check_exit_code(){
    if [ $? -eq 0 ]
    then
        INFO "$1"
    else
        ERROR "$2"
        exit 1
    fi
}

INFO(){
    echo "$(date --utc +%FT%TZ) INFO: $1"
}

ERROR(){
    echo "$(date --utc +%FT%TZ) ERROR: $1"
}

get_script_list(){
    if [ -z "$drop_version" -o -z "$release_version" ]
    then
        ERROR "Drop or Release version is empty"
        exit 1
    fi
    if [ $mode == "upgrade" ]
    then
        from_version=$release_version
        to_version=$drop_version
    elif [ $mode == "rollback" ]
    then
        from_version=$drop_version
        to_version=$release_version
    fi
    list_of_avail_versions=$( ls $pre_req_script_path/$delta_dir)
    for ver in $list_of_avail_versions
    do
        vercmp $ver 
        return_code=$?

        if [ $return_code -eq 0 ]
        then
            applicable_scripts=$(echo "$applicable_scripts $ver")
        fi
    done
    echo "$applicable_scripts" > $init_path/applicable_versions
}

vercmp() {
    to_check=$1
    if [ $mode == "upgrade" ]
    then
        if [ "$(printf '%s\n' "$from_version" "$to_check" | sort -V | head -n1)" == "$from_version" -a "$from_version" != "$to_check" ]
        then
            if [ "$(printf '%s\n' "$to_version" "$to_check" | sort -rV | head -n1)" = "$to_version" ]
            then
                # Greater than from version and less than or equal to to_version.
                return 0
            else   
                # Greater than both from and to version.
                return 2
            fi
        else
            #Less than from version
            return 1
        fi
    elif [ $mode == "rollback" ] 
    then
        if [ "$(printf '%s\n' "$to_version" "$to_check" | sort -V | head -n1)" == "$to_version" -a "$to_version" != "$to_check" ]
        then
            if [ "$(printf '%s\n' "$from_version" "$to_check" | sort -V | head -n1)" == "$to_check" ]
            then
                # Less than from version and Greater than to_version.
                return 0
            else   
                # Greater than both from and to version.
                return 2
            fi
        else
            #Less than to version
            return 1
        fi
    fi
}

execute(){	
    chmod +x $pre_req_script_path/$delta_dir/$version/delta
    $pre_req_script_path/$delta_dir/$version/delta
    check_exit_code "Successfully executed $pre_req_script_path/$delta_dir/$version/delta" "Error while executing $pre_req_script_path/$delta_dir/$version/delta"
}

apply_pre_req(){
    INFO "Get Applicable version scripts"
    echo "$mode" > $init_path/.bob/var.mode
    get_script_list
    app_versions=$(cat $init_path/applicable_versions)
    if [ -z $app_versions ]
    then
        INFO "no application versions found"
    fi
    for ver in $app_versions
    do
        echo "$ver" > $init_path/.bob/var.script_version
        INFO "Executing pre script for $ver"
        if [ -f "$init_path/pipeline-scripts/$delta_dir/$ver/pre" ]
        then
            $init_path/pipeline-scripts/$delta_dir/$ver/pre
            check_exit_code "$ver pre script executed successfully" "$ver pre script failed"
        else
            INFO "Pre script not available for $ver. Hence Skipping"
        fi

        INFO "Executing delta script for $ver"
        if [ -f "$init_path/pipeline-scripts/$delta_dir/$ver/delta" ]
        then
            $init_path/bob/bob pre-req
            check_exit_code "$ver delta script executed successfully" "$ver delta script failed"
        else
            INFO "Delta script not available for $ver. Hence Skipping"
        fi

        INFO "Executing post script for $ver"
        if [ -f $init_path/pipeline-scripts/$delta_dir/$ver/post ]
        then
            $init_path/pipeline-scripts/$delta_dir/$ver/post
            check_exit_code "$ver post script executed successfully" "$ver post script failed"
        else
            INFO "Post script not available for $ver. Hence Skipping"
        fi
        

    done

}

usage(){
    echo "$0 -m <mode> -e <version> -> <to execute the delta script>"
    echo "$0 -E <to apply the pre-req>"
}

main(){
    initialize
    if [ $# -eq 0 ]
    then  
	    echo "Wrong Argument" 
        usage
        exit 1
    fi
    while getopts "v:e:m:hE" opt
    do
        case $opt in
            h)
                usage
                exit 0
                ;;
            e) 
                version=$OPTARG
                execute
                ;;
            m) 
                mode=$OPTARG  
                if [ $mode == "upgrade" ]
                then
                    delta_dir="upgrade_pre_req"
                elif [ $mode == "rollback" ]
                then
                    delta_dir="rollback_pre_req"
                fi  
                ;;
            E)
                #Apply Pre Req
                apply_pre_req
                ;;

            esac
    done
}

main $@
