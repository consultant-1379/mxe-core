#!/usr/bin/env bash
set -ex

if [ -z $API_TOKEN ]
then 
    echo "API_TOKEN is not set. Please export it before running this script."
    exit 1
fi

echo "Running as user $USER"

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
MXE_CODEBASE_DIR=$(dirname $SCRIPTPATH)

DEFAULT_PRODUCT_INFO_DIR=".bob/product_info/"
DEFAULT_CRD_CHARTS_FILE=".bob/var.crd-chart-urls"
DEFAULT_IMAGES_2PP_FILE=".bob/var.images-2pp"

CRD_CHARTS_FILE="$MXE_CODEBASE_DIR/${1:-$DEFAULT_CRD_CHARTS_FILE}"
PRODUCT_INFO_DIR="$MXE_CODEBASE_DIR/${2:-$DEFAULT_PRODUCT_INFO_DIR}"
IMAGES_2PP_FILE="$MXE_CODEBASE_DIR/${3:-$DEFAULT_IMAGES_2PP_FILE}"
ADP_REQUIREMENTS_FILE_NAME="requirements.adp.release.yaml"
ERIC_PRODUCT_INFO_FILE_NAME="eric-product-info.yaml"

rm -rf "$PRODUCT_INFO_DIR"
mkdir -p "$PRODUCT_INFO_DIR"

## Get all requirements.adp.release.yaml from the MXE Code base and save into array ADP_REQUIREMENTS_FILES
readarray ADP_REQUIREMENTS_FILES < <(find $MXE_CODEBASE_DIR -name $ADP_REQUIREMENTS_FILE_NAME)

# Loop through array, read .dependencies[] from each ADP_REQUIREMENTS_FILE as a json with indent set to 0
# This creates a flat json array with one json record in each row
# Now this json array is iterated and using jq individual fields can be accessed to build the chart download URL
# Using the Chart Url only ChartName/eric_product_info.yaml is extracted and saved to the PRODUCT_INFO_DIR
for ADP_REQUIREMENTS_FILE in "${ADP_REQUIREMENTS_FILES[@]}"
do
    readarray REQUIREMENTS < <(yq e -o=j -I=0 '.dependencies[]' $ADP_REQUIREMENTS_FILE)
    for REQUIREMENT_JSON in "${REQUIREMENTS[@]}"
    do
        name=$(echo $REQUIREMENT_JSON | jq -r '.name')
        repository=$(echo $REQUIREMENT_JSON | jq -r '.repository')
        version=$(echo $REQUIREMENT_JSON | jq -r '.version')
        if [[ "$repository" == *"proj-mxe-deps-helm" ]]
        then 
            URL="${repository}-local/$name-$version.tgz"
            wget -qO- ${URL} --user $USER --password $API_TOKEN | tar xvz -C ${PRODUCT_INFO_DIR} "$name/${ERIC_PRODUCT_INFO_FILE_NAME}"
        else 
            URL="$repository/$name/$name-$version.tgz"
            wget -qO- ${URL} | tar xvz -C ${PRODUCT_INFO_DIR} "$name/${ERIC_PRODUCT_INFO_FILE_NAME}"
        fi 
        
    done
done

# Loop through the CRD_CHARTS_FILE and read line by line
# From URL, get the <name>-<version>.tgz of the Chart's tar ball using basename
# Read until the Chart Version to get the Chart name
#   To achieve this, find position of last hyphen in the Chart name and get Substring from 0 to that position
# Once the chart name is known then the URL can be used to download the Chart  and extract ChartName/eric_product_info.yaml
# Eg:
#  CRD_CHART=https://arm.sero.gic.ericsson.se/artifactory/proj-adp-gs-all-helm/eric-data-document-database-pg/eric-data-document-database-pg-7.0.0+49.tgz
#  name_with_version=eric-data-document-database-pg-7.0.0+49.tgz
#  position_of_last_hyphen=30
#  name=eric-data-document-database-pg

while IFS= read -r CRD_CHART
do
    if [ -n ${CRD_CHART} ]
    then
        name_with_version=$(basename $CRD_CHART)
        position_of_last_hyphen=$(echo $name_with_version | awk -F"-" '{print length($0)-length($NF)-1}')
        name=${name_with_version:0:$position_of_last_hyphen}
        wget -qO- ${CRD_CHART} | tar xvz -C ${PRODUCT_INFO_DIR} "$name/${ERIC_PRODUCT_INFO_FILE_NAME}"
    fi
done <  "$CRD_CHARTS_FILE"

# Get all the eric-product-info.yaml files from PRODUCT_INFO_DIR and save into array PRODUCT_INFO_FILES
readarray ERIC_PRODUCT_INFO_FILE_NAMES < <(find $PRODUCT_INFO_DIR -name $ERIC_PRODUCT_INFO_FILE_NAME)

# Loop through array, read .images[] from each eric-product-info.yaml  as a json with indent set to 0
# This creates a flat json array with one json record in each row
# Now this json array is iterated and using jq individual fields like repoPath, Name and tag can be accessed to build the IMAGE URI
# 
IMAGES=()
for ERIC_PRODUCT_INFO_FILE in "${ERIC_PRODUCT_INFO_FILE_NAMES[@]}"
do
    readarray IMAGES_JSON < <(yq e -o=j -I=0 '.images[]' $ERIC_PRODUCT_INFO_FILE)
    for IMAGE_JSON in "${IMAGES_JSON[@]}"
    do 
        IMAGE=""
	registry=$(echo $IMAGE_JSON | jq -r '.registry')
        repoPath=$(echo $IMAGE_JSON | jq -r '.repoPath')
        imageName=$(echo $IMAGE_JSON | jq -r '.name')
        tag=$(echo $IMAGE_JSON | jq -r '.tag')
        IMAGE="${registry}/${repoPath}/${imageName}:${tag}"
        if [[ -n ${IMAGE} ]]
        then
            IMAGES+=( "${IMAGE}" )
        fi
    done 
done

# Using sort -u, we can get distinct list of images which is then saved into IMAGES_2PP_FILE
UNIQ_IMAGES=($(printf "%s\n" "${IMAGES[@]}" | sort -u))
printf "%s, " "${UNIQ_IMAGES[@]}" > $IMAGES_2PP_FILE
