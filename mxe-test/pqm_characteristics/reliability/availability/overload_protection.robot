*** Settings ***

Library    yaml
Library    OperatingSystem
Library    Collections
Library    Process
Library    libraries/MxeKubernetesLibrary.py
Resource    keywords/mxe_api_utilities_keywords.robot
Resource    keywords/mxe_cli_utilities_keywords.robot
Variables    variables/reliability/availability/overload_protection_inputs.py
Variables    variables/mxe_cluster_details.py


*** Variables ***

*** Test Cases ***

to verify overload protection for a specific model service
    [Documentation]    to test overload protection with a rate limit envoy filter applied 
     ...                to a specific model service
    [tags]    tc1    overload-protection    test-dev    test-staging    test-weekly
    [Setup]    onboard model from external registry    model_id=${op_model_id}    model_author=${op_model_author}    model_title=${op_model_title}    model_description=${op_model_description}    model_version=${op_model_version}    model_registry_path=${op_model_registry_path}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${op_sample_service_name}
    ...           AND    delete model    model_id=${op_model_id}    model_version=${op_model_version}
    ...           AND    delete_custom_resource_deployment_in_namespace    custom_resource_group_name=${envoy_filter_custom_resource_group}    custom_resource_version=${envoy_filter_custom_resource_version}    custom_resource_namespace=${mxe_namespace}    custom_resource_plural_name=${envoy_filter_custom_resource_plural_name}    custom_resource_deployment_name=${envoy_filter_cr_name}
    # creating rate limit configuration using envoy filter crd
    ${generated_manifest}=    update manifest with dynamic namespace    envoy_filter_manifest_file=${op_envoy_filter_manifest_file}    namespace=${mxe_namespace}
    Create File    updated_op_envoy_filter_manifest_file.yaml    ${generated_manifest}
    ${envoy_filter_cr_name}=    create_custom_resource_deployment_in_namespace    custom_resource_group_name=${envoy_filter_custom_resource_group}    custom_resource_version=${envoy_filter_custom_resource_version}    custom_resource_namespace=${mxe_namespace}    custom_resource_plural_name=${envoy_filter_custom_resource_plural_name}    custom_resource_manifest_file=updated_op_envoy_filter_manifest_file.yaml
    # creating a model service
    create model service via cli    manifest_file=${op_sample_model_service}
    check if model service is running via cli    service_name=${op_sample_service_name}
    invoke the model service via api    service_name=${op_sample_service_name}    input=${model_inception_input_file}
    # running a short performance test using load generator tool https://github.com/rakyll/hey
    ${access_token}=    Get Access Token
    ${result}=    Run Process    hey -c 10 -q 10 -z 1s -o csv -m POST -H "Content-Type: application/json" -H "Authorization: Bearer ${access_token}" -D ${model_inception_input_file} ${mxe_host}/model-endpoints/${op_sample_service_name} | cut -d "," -f 7    shell=True  
    Log    ${result.stdout}    
    Should Contain    ${result.stdout}    429       
    
*** keywords ***

update manifest with dynamic namespace
    [Documentation]    To update the namespace in the workflow manifest file
    [Arguments]    ${envoy_filter_manifest_file}    ${namespace}    
    ${manifest_file}=  Get File  ${envoy_filter_manifest_file}    # file should be in valid YAML format
    ${load_manifest}=  yaml.Safe Load  ${manifest_file}
    Set To Dictionary  ${load_manifest}[metadata]  namespace=${namespace}
    ${updated_manifest}=  yaml.Dump  ${load_manifest}
    log    ${updated_manifest}
    [Return]    ${updated_manifest}
