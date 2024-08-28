#!/bin/bash
set -x

init_path="$(dirname $0)/../../"
xray_scan_config=$init_path/scan-config/xray_report.config
_3pp_images=$(cat $init_path/.bob/var.docker-images-3pp-with-tag|sed 's/,\s*$//')
mxe_images=$(cat $init_path/.bob/var.docker-images-mxe-with-tag|sed 's/,\s*$//')
image_list="$_3pp_images,$mxe_images"
repo_path=$1
IFS=,
for image in $image_list
do
        path="ARM_SELI/$repo_path/$(echo $image|cut -d"/" -f3-)"
        image_path=$path yq -i '.xray.paths |= . + env(image_path)' $xray_scan_config
done