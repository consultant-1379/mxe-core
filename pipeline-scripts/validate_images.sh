#! /usr/bin/env bash
set -ex 

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
REPOROOT=$(dirname $SCRIPTPATH)
IMAGES_DIR="${REPOROOT}/images"

rm -rf "${IMAGES_DIR}"
mkdir -p $IMAGES_DIR

chmod -R 777 $IMAGES_DIR

NAMESPACE=$1
IMAGE_REGISTRIES=(${2:-"armdocker.rnd.ericsson.se" "serodocker.sero.gic.ericsson.se"})

kubectl images -n $NAMESPACE -o json > $IMAGES_DIR/mxe-images.json

if [ -s $IMAGES_DIR/mxe-images.json ]
then

    IMAGE_REGISTRY=""
    count=${#IMAGE_REGISTRIES[@]}
    for ((i=0; i<$count; i++)); do
        if [[ $i -eq 0 ]]; then
            IMAGE_REGISTRY="${IMAGE_REGISTRIES[$i]}"
        else
            IMAGE_REGISTRY="$IMAGE_REGISTRY\|^${IMAGE_REGISTRIES[$i]}"
        fi
    done

    echo "Images currently running in $NAMESPACE are:"
    cat $IMAGES_DIR/mxe-images.json | grep -v '^\[Summary\]'| jq
    count_of_bad_images=$(cat $IMAGES_DIR/mxe-images.json | grep -v '^\[Summary\]' | jq -r ".[].image" | grep -v "^$IMAGE_REGISTRY" | wc -l)

    if [[ $count_of_bad_images -ne 0 ]]
    then 
        echo "There are $count_of_bad_images images that are not from $IMAGE_REGISTRY"
        echo "They are: \n$(cat $IMAGES_DIR/mxe-images.json | grep -v '^\[Summary\]' | jq -r ".[].image" | grep -v "^$IMAGE_REGISTRY")"
        exit 1
    fi
else
    echo "$IMAGES_DIR/mxe-images.json is empty"
    exit 1
fi
 


