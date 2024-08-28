
#!/usr/bin/env bash
set -ex

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
MXE_CODEBASE_DIR=$(dirname $SCRIPTPATH)

PRE_REQ_HELMFILE="$MXE_CODEBASE_DIR/resources/helmfile/pre-requisites/helmfile.yaml"
RENDERED_PRE_REQ_HELMFILE="$MXE_CODEBASE_DIR/.bob/rendered_pre_req_helmfile.yaml"

if [ $RELEASE == "true" ]
then 
    PRE_REQ_HELMFILE="$MXE_CODEBASE_DIR/resources/helmfile/pre-requisites/pra_helmfile.yaml"
fi

# Any state values file will do, used to replace properties in helmfile.yaml & get a valid yaml to make it parseable using yq 
REFERENCE_STATE_VALUES_FILE="$MXE_CODEBASE_DIR/resources/testenv/hahn081/values/pre-requisites-config.yaml"

## build helmfile.yaml with values from reference state values file to get a valid yaml
helmfile --file $PRE_REQ_HELMFILE --state-values-file $REFERENCE_STATE_VALUES_FILE build > $RENDERED_PRE_REQ_HELMFILE

mxe_pre_req_offline_images_archive=${1:-$MXE_CODEBASE_DIR/.bob/mxe-pre-req-offline-installer-images.tar}

declare -A HELM_REPOS=()

for repository in $(yq e -o=j -I=0 '.repositories[]' $RENDERED_PRE_REQ_HELMFILE)
do 
    repo_name=$(echo $repository | jq -r '.name')
    repo_url=$(echo $repository | jq -r '.url')
    HELM_REPOS[$repo_name]=$repo_url
done

for elem in "${!HELM_REPOS[@]}"
do
 echo "key : ${elem}" -- "value: ${HELM_REPOS[${elem}]}"
done

# load the pre-req charts info from the yaml file into an array with each element being a json object
readarray PRE_REQ_CHARTS < <(yq e -o=j -I=0 '.releases[]' $RENDERED_PRE_REQ_HELMFILE)

PRODUCT_INFO_DIR="$MXE_CODEBASE_DIR/.bob/pre_req_product_info/"
PRE_REQ_IMAGES_FILE="$MXE_CODEBASE_DIR/.bob/var.pre-req-images"
ERIC_PRODUCT_INFO_FILE_NAME="eric-product-info.yaml"

rm -rf "$PRODUCT_INFO_DIR"
mkdir -p "$PRODUCT_INFO_DIR"

# iterate the json objects
for PRE_REQ_CHART_JSON in "${PRE_REQ_CHARTS[@]}"
do
    echo "PRE_REQ_CHART_JSON: $PRE_REQ_CHART_JSON"
    chartName=$(echo $PRE_REQ_CHART_JSON | jq -r '.chart')
    repoId=$(echo $chartName | cut -d/ -f1)
    name=$(echo $chartName | cut -d/ -f2)
    repository=${HELM_REPOS[$repoId]}
    if [ $RELEASE == "false" ]
    then
        # get latest version of the chart
        version=$(helm show chart $name --repo $repository | yq .version ) 
    else
        # get the version of the chart from the helmfile 
        version=$(echo $PRE_REQ_CHART_JSON | jq -r '.version')
    fi 
    echo "Chartname:$name Chartrepo:$repository Chartversion:$version"
    chartURL="$repository/$name/$name-${version}.tgz"
    wget -qO- ${chartURL} | tar xvz -C ${PRODUCT_INFO_DIR} "$name/${ERIC_PRODUCT_INFO_FILE_NAME}"
done 

chmod 777 -R $PRODUCT_INFO_DIR

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
printf "%s, " "${UNIQ_IMAGES[@]}" > $PRE_REQ_IMAGES_FILE

# Make offline archive
crane pull "${UNIQ_IMAGES[@]}" ${mxe_pre_req_offline_images_archive}