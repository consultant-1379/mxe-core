*** Settings ***

Library    yaml
Library    OperatingSystem
Library    Collections
Library    libraries/MxeKeycloakLibrary.py
Library    libraries/MxeKubernetesLibrary.py
Variables    variables/mxe_cluster_details.py
Resource    keywords/mxe_api_utilities_keywords.robot
Variables    variables/security/api_access_control_inputs.py
Variables    variables/flow_deployment_inputs.py


*** Variables ***

*** Test Cases ***

to check privilege to access a specific resource
    [Documentation]    to test a user with a role who must be able to perform ANY action 
     ...                on resource urls
    [tags]    tc1    api-access-control    test-dev    test-staging    test-weekly
    [Setup]    Run Keywords     create new user in keycloak     username=${test_user_username}    password=${test_user_password}
    ...    AND    create new role in keycloak    rolename=${test_role}
    ...    AND    assign role with user    username=${test_user_username}    rolename=${test_role}
    [Teardown]    Run Keywords    delete_user_in_keycloak    username=${test_user_username}
    ...    AND    delete_role_in_keycloak    rolename=${test_role}
    ...    AND    delete_custom_resource_deployment_in_namespace    custom_resource_group_name=${auth_policy_custom_resource_group}    custom_resource_version=${auth_policy_custom_resource_version}    custom_resource_namespace=${mxe_namespace}    custom_resource_plural_name=${auth_policy_custom_resource_plural_name}    custom_resource_deployment_name=${auth_policy_cr_name}           
    ${generated_manifest}=    update manifest with namespace and oauth host    auth_policy_manifest_file=${tc1_auth_policy_manifest_file}    namespace=${mxe_namespace}    host=${oauth_api_host}
    Create File    updated_sample_manifest_file.yaml    ${generated_manifest}
    ${auth_policy_cr_name}=    create_custom_resource_deployment_in_namespace    custom_resource_group_name=${auth_policy_custom_resource_group}    custom_resource_version=${auth_policy_custom_resource_version}    custom_resource_namespace=${mxe_namespace}    custom_resource_plural_name=${auth_policy_custom_resource_plural_name}    custom_resource_manifest_file=updated_sample_manifest_file.yaml
    list the onboarded training packages via api    username=${test_user_username}    password=${test_user_password}
    list the onboarded models via api    username=${test_user_username}    password=${test_user_password}
    
*** keywords ***

update manifest with namespace and oauth host
    [Documentation]    To update the namespace and host name in the auth policy manifest file
    [Arguments]    ${auth_policy_manifest_file}    ${namespace}    ${host}
    ${manifest_file}=  Get File  ${auth_policy_manifest_file}    # file should be in valid YAML format
    ${load_manifest}=  yaml.Safe Load  ${manifest_file}
    Set To Dictionary  ${load_manifest}[metadata]  namespace=${namespace}
    log    ${load_manifest} 
    @{new_values} =	Create List	${host}/auth/realms/mxe
    Set To Dictionary  ${load_manifest}[spec][rules][0][when][0]  values=${new_values}
    ${updated_manifest}=  yaml.Dump  ${load_manifest}
    log    ${updated_manifest}
    [Return]    ${updated_manifest}