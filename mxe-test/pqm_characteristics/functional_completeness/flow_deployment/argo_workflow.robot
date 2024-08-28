*** Settings ***
Documentation    Tests to verify the argo workflow basic use cases in MXE
Metadata    Version    0.1
Library  libraries/MxeKubernetesLibrary.py
Library    OperatingSystem
Library    Collections
Library    String
Library    yaml
Resource  keywords/argo_workflow_gui_utilities_higher_level_keywords.robot
Variables  variables/flow_deployment_inputs.py
Variables  variables/mxe_cluster_details.py

*** Variables ***

*** Test Cases ***

creating sample argo workflow
    [Documentation]  To create a sample argo workflow and check if it succeeds
    [Tags]   argo-workflow    test-dev    test-weekly    tc1
    ${generated_manifest}=    update manifest with dynamic namespace    workflow_manifest_file=${tc1_argo_workflow_manifest_file}    namespace=${mxe_namespace}
    Create File    updated_sample_manifest_file.yaml    ${generated_manifest}
    ${workflow_deployment_name}=    create_custom_resource_deployment_in_namespace    custom_resource_group_name=${argo_custom_resource_group}    custom_resource_version=${argo_custom_resource_version}    custom_resource_namespace=${mxe_namespace}    custom_resource_plural_name=${argo_custom_resource_plural_name}    custom_resource_manifest_file=updated_sample_manifest_file.yaml
    check_argo_workflow_status    custom_resource_group_name=${argo_custom_resource_group}    custom_resource_version=${argo_custom_resource_version}    custom_resource_namespace=${mxe_namespace}    custom_resource_plural_name=${argo_custom_resource_plural_name}    custom_resource_deployment_name=${workflow_deployment_name}

creating sample spark operator (scala) based argo workflow
    [Documentation]  To create a sample spark operator based argo workflow and check if it succeeds
    [Tags]   argo-workflow    test-dev    test-weekly    tc2
    ${generated_manifest}=    update manifest with dynamic namespace    workflow_manifest_file=${tc2_argo_workflow_manifest_file}    namespace=${mxe_namespace}
    Create File    updated_sample_manifest_file.yaml    ${generated_manifest}
    ${workflow_deployment_name}=    create_custom_resource_deployment_in_namespace    custom_resource_group_name=${argo_custom_resource_group}    custom_resource_version=${argo_custom_resource_version}    custom_resource_namespace=${mxe_namespace}    custom_resource_plural_name=${argo_custom_resource_plural_name}    custom_resource_manifest_file=updated_sample_manifest_file.yaml
    check_argo_workflow_status    custom_resource_group_name=${argo_custom_resource_group}    custom_resource_version=${argo_custom_resource_version}    custom_resource_namespace=${mxe_namespace}    custom_resource_plural_name=${argo_custom_resource_plural_name}    custom_resource_deployment_name=${workflow_deployment_name}

check argo workflow gui
    [Documentation]    To login to argo workflow gui and check the workflow tab
    [Tags]   argo-workflow    test-dev    test-weekly    tc3
    login to argo workflow gui    url=${mxe_host}    username=${mxe_username}    password=${mxe_password}
    click workflows tab

creating sample spark application (python) based argo workflow
    [Documentation]  To create a sample spark operator based argo workflow and check if it succeeds
    [Tags]   argo-workflow    test-dev    test-weekly    tc4
    ${generated_manifest}=    update manifest with dynamic namespace    workflow_manifest_file=${tc3_argo_workflow_manifest_file}    namespace=${mxe_namespace}
    Create File    updated_sample_manifest_file.yaml    ${generated_manifest}
    ${workflow_deployment_name}=    create_custom_resource_deployment_in_namespace    custom_resource_group_name=${argo_custom_resource_group}    custom_resource_version=${argo_custom_resource_version}    custom_resource_namespace=${mxe_namespace}    custom_resource_plural_name=${argo_custom_resource_plural_name}    custom_resource_manifest_file=updated_sample_manifest_file.yaml
    check_argo_workflow_status    custom_resource_group_name=${argo_custom_resource_group}    custom_resource_version=${argo_custom_resource_version}    custom_resource_namespace=${mxe_namespace}    custom_resource_plural_name=${argo_custom_resource_plural_name}    custom_resource_deployment_name=${workflow_deployment_name}

*** Keywords ***

update manifest with dynamic namespace
    [Documentation]    To update the namespace in the workflow manifest file
    [Arguments]    ${workflow_manifest_file}    ${namespace}    
    ${manifest_file}=  Get File  ${workflow_manifest_file}    # file should be in valid YAML format
    ${load_manifest}=  yaml.Safe Load  ${manifest_file}
    ${mxe_end_point}=    Remove String    ${mxe_host}    https://
    Set To Dictionary  ${load_manifest}[metadata]  namespace=${namespace}
    ${updated_manifest}=  yaml.Dump  ${load_manifest}
    log    ${updated_manifest}
    [Return]    ${updated_manifest}
    