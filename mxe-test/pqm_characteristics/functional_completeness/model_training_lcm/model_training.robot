*** Settings ***
Documentation    Tests to verify the model training life cycle management use cases of mxe via cli and gui interfaces
Metadata    Version    0.1
Library  libraries/MxeTrainingCliLibrary.py
Variables  variables/training_package_inputs.py
Resource  keywords/mxe_gui_utilities_higher_level_keywords.robot

*** Variables ***

*** Test Cases ***

model training lcm via cli
    [Documentation]  onboard a model training package, start the training job, download the results and clean up via cli
    [Tags]  cli   model-training    test-dev    tc1    test-staging    test-weekly
    onboard training package from sourcecode via cli    training_source_code_path=${training_source_code_filepath}
    check training package available status via cli    training_package_id=${training_model2_package_id}    training_package_version=${training_model2_package_version}
    start training job via cli    training_package_id=${training_model2_package_id}    training_package_version=${training_model2_package_version}
    check if training job is completed via cli    training_package_id=${training_model2_package_id}    training_package_version=${training_model2_package_version}
    ${job_id}=    fetch the training job id via cli    training_package_id=${training_model2_package_id}    training_package_version=${training_model2_package_version}    
    download training results via cli    training_job_id=${job_id}    output_directory=${training_results_directory}
    delete training job via cli    training_package_id=${training_model2_package_id}    training_package_version=${training_model2_package_version}
    check if training job is deleted via cli    training_package_id=${training_model2_package_id}    training_package_version=${training_model2_package_version}
    delete training package via cli    training_package_id=${training_model2_package_id}    training_package_version=${training_model2_package_version}
    check if training package is deleted via cli    training_package_id=${training_model2_package_id}    training_package_version=${training_model2_package_version}

model training lcm via gui
    [Documentation]  onboard a model training package, start the training job, download the results and clean up via gui
    [Tags]  gui   model-training    tc2
    [Setup]     login to mxe gui
    [Teardown]    logout of mxe gui 
    onboard training package from sourcecode via gui    training_source_code_file=${training_source_code_archive_file}
    check training package available status via gui    training_package_id=${training_model1_package_id}    training_package_version=${training_model1_package_version}
    start training job via gui    training_package_id=${training_model1_package_id}    training_package_version=${training_model1_package_version}  
    check if training job is completed via gui    training_package_id=${training_model1_package_id}    training_package_version=${training_model1_package_version} 
    download training results via gui    training_package_id=${training_model1_package_id}    training_package_version=${training_model1_package_version}
    delete training job via gui    training_package_id=${training_model1_package_id}    training_package_version=${training_model1_package_version}
    check if training job is deleted via gui    training_package_id=${training_model1_package_id}    training_package_version=${training_model1_package_version}
    delete training package via gui    training_package_id=${training_model1_package_id}    training_package_version=${training_model1_package_version}
    check if training package is deleted via gui    training_package_id=${training_model1_package_id}    training_package_version=${training_model1_package_version}

verify the help options in training cli command
    [Documentation]    To verify mxe-training command options as given below:
    ...    mxe-training help
    ...    mxe-training version
    ...    mxe-training list packages --help
    ...    mxe-training list packages -h
    ...    mxe-training list packages --verbose
    ...    mxe-training list packages -v
    [Tags]    cli    test-dev    model-training    tc3    test-weekly
    check training command options

onboard a training packaging from a path that does not exist via cli
    [Documentation]    To verify valid error message while onboarding a training packaging from a path that does not exist
    [Tags]  cli   model-training    test-dev    tc4    negative    test-weekly
    ${msg}=    Run Keyword And Expect Error    *    onboard training package from sourcecode via cli    training_source_code_path=/tmp
    Should Contain    ${msg}    Error: open /tmp/MXE-META-INF/INFO: no such file or directory

start the already running training job via cli
    [Documentation]    To verify valid error message while starting a training job that is already running
    [Tags]  cli   model-training    test-dev    tc5    negative    test-weekly
    onboard training package from sourcecode via cli    training_source_code_path=${training_source_code_filepath}
    check training package available status via cli    training_package_id=${training_model2_package_id}    training_package_version=${training_model2_package_version}
    start training job via cli    training_package_id=${training_model2_package_id}    training_package_version=${training_model2_package_version}
    ${msg}=    Run Keyword And Expect Error    *    start training job via cli    training_package_id=${training_model2_package_id}    training_package_version=${training_model2_package_version}
    Should Contain    ${msg}    Error: Training is already running with packageId "${training_model2_package_id}" packageVersion "${training_model2_package_version}"
    check if training job is completed via cli    training_package_id=${training_model2_package_id}    training_package_version=${training_model2_package_version}
    delete training job via cli    training_package_id=${training_model2_package_id}    training_package_version=${training_model2_package_version}
    check if training job is deleted via cli    training_package_id=${training_model2_package_id}    training_package_version=${training_model2_package_version}
    delete training package via cli    training_package_id=${training_model2_package_id}    training_package_version=${training_model2_package_version}
    check if training package is deleted via cli    training_package_id=${training_model2_package_id}    training_package_version=${training_model2_package_version}

start a non-existing training package via cli
    [Documentation]    To verify valid error message while starting a non-existing training package
    [Tags]  cli   model-training    test-dev    tc6    negative    test-weekly
    ${msg}=    Run Keyword And Expect Error    *    start training job via cli    training_package_id=unavailable.training.package    training_package_version=0.0.1
    Should Contain    ${msg}    Error: Training package with id "unavailable.training.package" version "0.0.1" not found!

delete a non-existing training job via cli
    [Documentation]    To verify valid error message while deleting a non-existing training job
    [Tags]  cli   model-training    test-dev    tc7    negative    test-weekly
    ${msg}=    Run Keyword And Expect Error    *    delete training job via cli    training_package_id=unavailable.training.package    training_package_version=0.0.1
    Should Contain    ${msg}    Error: No training job found with packageId "unavailable.training.package" and packageVersion "0.0.1"

delete a non-existing training package via cli
    [Documentation]    To verify valid error message while deleting a non-existing training package
    [Tags]  cli   model-training    test-dev    tc8    negative    test-weekly
    ${msg}=    Run Keyword And Expect Error    *    delete training package via cli    training_package_id=unavailable.training.package    training_package_version=0.0.1
    Should Contain    ${msg}    Error: Training package "unavailable.training.package" version "0.0.1" not found!

download a training result with incorrect job id via cli
    [Documentation]    To verify valid error message while trying to download a training result with incorrect job id
    [Tags]  cli   model-training    test-dev    tc9    negative    test-weekly
    ${msg}=    Run Keyword And Expect Error    *    download training results via cli    training_job_id=unknown.id    output_directory=${training_results_directory}
    Should Contain    ${msg}    Error: Training job with id "unknown.id" not found
