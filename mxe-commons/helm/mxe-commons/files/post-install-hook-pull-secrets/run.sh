#! /usr/bin/env bash

set -ex 

if [[ -z $DOCKER_JSONS_DIR ]]
then 
    echo "Env DOCKER_JSONS_DIR is not set. Aborting.."
    exit 1
fi 

count_of_docker_config_jsons=$(ls -1 $DOCKER_JSONS_DIR/*.json | wc -l)

if [[ $count_of_docker_config_jsons == 0 ]]
then 
    echo "Did not find any docker config jsons in $DOCKER_JSON_DIR to merge. Aborting..."
    exit 1
fi 

output_dir=$(mktemp -d -p /work)

echo "Merging pull secrets present in $DOCKER_JSONS_DIR"
jq -rs 'reduce .[] as $item ({}; . * $item)' $DOCKER_JSONS_DIR/*.json > $output_dir/merged_docker_config.json 
echo "docker config jsons are merged and written to $output_dir/merged_docker_config.json"

kubectl create secret docker-registry $MERGED_DOCKER_CONFIG_SECRET_NAME \
    --from-file=.dockerconfigjson=$output_dir/merged_docker_config.json \
    --namespace "${MXE_NAMESPACE}" -o yaml --dry-run=client | kubectl patch -f - \
    -p '{"metadata":{"labels": {"app.kubernetes.io/part-of": "mxe"}}}' \
    --dry-run=client -o yaml | kubectl apply -f - 
