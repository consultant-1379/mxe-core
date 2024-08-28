#!/usr/bin/env bash 

set -ex 

SCRIPT=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPT)
REPOROOT=$(dirname $SCRIPTPATH)

TASK=$1
MXE_COMMONS_OFFLINE_IMAGES_ARCHIVE=$2
MXE_DEPLOYER_OFFLINE_IMAGES_ARCHIVE=$3 
MXE_SERVING_OFFLINE_IMAGES_ARCHIVE=$4
MXE_TRAINING_OFFLINE_IMAGES_ARCHIVE=$5
MXE_WORKFLOW_OFFLINE_IMAGES_ARCHIVE=$6
MXE_EXPLORATION_OFFLINE_IMAGES_ARCHIVE=$7
MXE_PRE_REQ_OFFLINE_IMAGES_ARCHIVE=$8


archives=($MXE_COMMONS_OFFLINE_IMAGES_ARCHIVE
          $MXE_DEPLOYER_OFFLINE_IMAGES_ARCHIVE 
          $MXE_SERVING_OFFLINE_IMAGES_ARCHIVE 
          $MXE_TRAINING_OFFLINE_IMAGES_ARCHIVE 
          $MXE_WORKFLOW_OFFLINE_IMAGES_ARCHIVE 
          $MXE_EXPLORATION_OFFLINE_IMAGES_ARCHIVE
          $MXE_PRE_REQ_OFFLINE_IMAGES_ARCHIVE)

generate-images-json-file(){
for file in "${archives[@]}"
do 
    echo "Listing images in .bob/$file"
    skopeo list-tags docker-archive:.bob/$file | tee .bob/$file.json 
done
}

generate-images-text-file(){
for file in "${archives[@]}"
do 
    jq -r '.Tags[]' .bob/$file.json > .bob/$file.txt
done
}



case $TASK in 
    "list-tags-in-offline-tar")
        generate-images-json-file
        ;;
    "generate-text")
        generate-images-text-file
        ;;
    *)
        echo "Invalid task"
        ;;
esac