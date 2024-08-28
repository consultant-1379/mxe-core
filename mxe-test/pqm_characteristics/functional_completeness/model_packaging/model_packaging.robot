*** Settings ***
Documentation    Tests to verify the model packaging use cases of MXE via CLI and GUI interfaces
Metadata    Version    0.1
Library  libraries/MxeModelCliLibrary.py
Library  libraries/MxeModelServiceCliLibrary.py
Variables  variables/model_onboarding_inputs.py
Resource  keywords/mxe_gui_utilities_higher_level_keywords.robot

*** Variables ***

*** Test Cases ***

model packaging via cli
    [Documentation]  To onboard a model from source code via cli in order to verify model packaging
    [Tags]   cli  model-packaging    test-dev    tc1    test-weekly
    onboard model from sourcecode via cli   source_code_path=${model1_source_code_filepath}
    check model available status via cli    model_id=${model1_id_int}    model_version=${model1_version_int}
    delete model via cli    model_id=${model1_id_int}    model_version=${model1_version_int}
    check if model is deleted via cli     model_id=${model1_id_int}    model_version=${model1_version_int}

model packaging via gui
    [Documentation]  To onboard a model from source code via gui in order to verify model packaging
    [Tags]   gui  model-packaging    tc2
    [Setup]     login to mxe gui
    [Teardown]    logout of mxe gui
    onboard model from sourcecode via gui    source_code_file=${model2_source_code_archive_file}
    check model available status via gui    model_id=${model2_id_int}    model_version=${model2_version_int}
    delete model via gui    model_id=${model2_id_int}    model_version=${model2_version_int}
    check if model is deleted via gui   model_id=${model2_id_int}    model_version=${model2_version_int}

verify the help options in model cli command
    [Documentation]    To verify mxe-model command options as given below:
    ...    mxe-model help
    ...    mxe-model version
    ...    mxe-model list --help
    ...    mxe-model list -h
    ...    mxe-model list --verbose
    ...    mxe-model list -v
    [Tags]    cli    test-dev    test    tc3    test-weekly
    check model command options

model packaging from a path that does not exist via cli
    [Documentation]    To verify valid error message while onboarding a model from the path that does not exist
    [Tags]    cli    model-packaging    negative    test-dev    tc4    test-weekly
    ${msg}=    Run Keyword And Expect Error    *    onboard model from sourcecode via cli    source_code_path=/tmp/
    Should Contain    ${msg}    Error: open /tmp/MXE-META-INF/INFO: no such file or directory

model onboard with the same image of another onboarded model via cli
    [Documentation]    To verify valid error message while onboarding a model with the same image of another onboarded model
    [Tags]    cli    model-packaging    negative    test-dev    tc5    test-weekly
    [Teardown]    Run Keywords    delete model via cli    model_id=${negative_model_id_cli_ext}    model_version=${negative_model_version_cli_ext}
    ...           AND    check if model is deleted via cli     model_id=${negative_model_id_cli_ext}    model_version=${negative_model_version_cli_ext}
    onboard model from external registry via cli    model_id=${negative_model_id_cli_ext}    model_author=${negative_model_author_cli_ext}    model_title=${negative_model_title_cli_ext}    model_description=${negative_model_description_cli_ext}    model_version=${negative_model_version_cli_ext}    model_registry_path=${negative_model_registry_path_cli_ext}
    check model available status via cli    model_id=${negative_model_id_cli_ext}    model_version=${negative_model_version_cli_ext}
    ${msg}=    Run Keyword And Expect Error    *    onboard model from external registry via cli    model_id=${negative_model_id_cli_ext}    model_author=${negative_model_author_cli_ext}    model_title=${negative_model_title_cli_ext}    model_description=${negative_model_description_cli_ext}    model_version=${negative_model_version_cli_ext}    model_registry_path=${negative_model_registry_path_cli_ext}
    Should Contain    ${msg}    Error: Model with image "${negative_model_registry_path_cli_ext}" has already been onboarded

model onboard with the same model id and version of another onboarded model via cli
    [Documentation]    To verify valid error message while onboarding a model with the same model id and version of another onboarded model
    [Tags]    cli    model-packaging    negative    test-dev    tc6    test-weekly
    [Teardown]    Run Keywords    delete model via cli    model_id=${negative_model_id_cli_ext}    model_version=${negative_model_version_cli_ext}
    ...           AND    check if model is deleted via cli     model_id=${negative_model_id_cli_ext}    model_version=${negative_model_version_cli_ext}
    onboard model from external registry via cli    model_id=${negative_model_id_cli_ext}    model_author=${negative_model_author_cli_ext}    model_title=${negative_model_title_cli_ext}    model_description=${negative_model_description_cli_ext}    model_version=${negative_model_version_cli_ext}    model_registry_path=${negative_model_registry_path_cli_ext}
    check model available status via cli    model_id=${negative_model_id_cli_ext}    model_version=${negative_model_version_cli_ext}
    ${msg}=    Run Keyword And Expect Error    *    onboard model from external registry via cli    model_id=${negative_model_id_cli_ext}    model_author=${negative_model_author_cli_ext}    model_title=${negative_model_title_cli_ext}    model_description=${negative_model_description_cli_ext}    model_version=${negative_model_version_cli_ext}    model_registry_path=${negative_random_model_registry_path_cli_ext}
    Should Contain    ${msg}    Error: Model with ID "${negative_model_id_cli_ext}" and version "${negative_model_version_cli_ext}" has already been onboarded

delete a model that does not exist via cli
    [Documentation]    To verify valid error message while deleting a model that does not exist
    [Tags]    cli    model-packaging    negative    test-dev    tc7    test-weekly
    ${msg}=    Run Keyword And Expect Error    *    delete model via cli    model_id=unavailable.model    model_version=0.0.1
    Should Contain    ${msg}    Error: Model "unavailable.model" version "0.0.1" not found!

delete a model that is associated with a model service via cli
    [Documentation]    To verify valid error message while deleting a model that is associated with a running model service
    [Tags]    cli    model-packaging    negative    test-dev    tc8    test-weekly
    [Teardown]    Run Keywords    delete model service via cli    service_name=${negative_test_service_name}
    ...           AND    check if model service is deleted via cli     service_name=${negative_test_service_name}
    ...           AND    delete model via cli    model_id=${negative_model_id_cli_ext}    model_version=${negative_model_version_cli_ext}
    ...           AND    check if model is deleted via cli     model_id=${negative_model_id_cli_ext}    model_version=${negative_model_version_cli_ext}
    onboard model from external registry via cli    model_id=${negative_model_id_cli_ext}    model_author=${negative_model_author_cli_ext}    model_title=${negative_model_title_cli_ext}    model_description=${negative_model_description_cli_ext}    model_version=${negative_model_version_cli_ext}    model_registry_path=${negative_model_registry_path_cli_ext}
    check model available status via cli    model_id=${negative_model_id_cli_ext}    model_version=${negative_model_version_cli_ext}
    create model service via cli    manifest_file=${negative_test_manifest}
    check if model service is running via cli    service_name=${negative_test_service_name}
    ${msg}=    Run Keyword And Expect Error    *    delete model via cli    model_id=${negative_model_id_cli_ext}   model_version=${negative_model_version_cli_ext}
    should contain    ${msg}    Error: Model "${negative_model_id_cli_ext}" version "${negative_model_version_cli_ext}" has an associated running model service. Please delete all the associated model services with the command mxe-service delete, and delete the model afterwards!        
