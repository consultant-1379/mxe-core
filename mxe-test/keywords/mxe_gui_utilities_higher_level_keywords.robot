*** Settings ***

Library    SeleniumLibrary
Library    XvfbRobot
Variables  variables/mxe_cluster_details.py
Resource  mxe_gui_dashboard_utilities_lower_level_keywords.robot
Resource  mxe_gui_model_packages_utilities_lower_level_keywords.robot
Resource  mxe_gui_model_services_utilities_lower_level_keywords.robot
Resource  mxe_gui_training_packages_utilities_lower_level_keywords.robot
Resource  mxe_gui_training_jobs_utilities_lower_level_keywords.robot

*** Keywords ***

login to mxe gui
    [Documentation]  To open mxe gui and login with valid credentials
    Open Browser    ${mxe_host}    headlessfirefox
    # Start Virtual Display    1920    1080
    # Open Browser    ${mxe_host}
    # Set Window Size    1920    1080
    Maximize Browser Window
    Set Browser Implicit Wait    5
    Input Text    id=username    ${mxe_username}
    Input Text    id=password    ${mxe_password}
    Click Button    //input[@value='Sign In']
    Sleep    5    wait until the elements are visible

logout of mxe gui
    [Documentation]  To sign-out from mxe gui
    Execute Javascript    ${user_name_item}.click();
    Sleep    2    wait until the elements are visible
    Execute Javascript    ${sign_out_button}.click();
    Close Browser

onboard model from sourcecode via gui
    [Documentation]  To onboard a model via gui
    [Arguments]    ${source_code_file}
    click dashboard menu
    click model packages menu
    click onboard model button
    upload model source code file    ${source_code_file}
    wait until the model source code file is uploaded

check model available status via gui
    [Documentation]  To check the onboarded model becomes available via gui
    [Arguments]    ${model_id}    ${model_version}
    click dashboard menu
    click model packages menu
    verify the onboarded model becomes available    ${model_id}    ${model_version}

delete model via gui
    [Documentation]  To delete the model via gui
    [Arguments]    ${model_id}    ${model_version}
    click dashboard menu
    click model packages menu
    select the model to be deleted    ${model_id}    ${model_version}
    delete the model

check if model is deleted via gui
    [Documentation]  To check the onboarded model is deleted via gui
    [Arguments]    ${model_id}    ${model_version}
    click dashboard menu
    click model packages menu
    verify the model is deleted    ${model_id}    ${model_version}

create model service using manifest via gui
    [Documentation]  To create a single model service via gui
    [Arguments]    ${manifest_file}
    click dashboard menu
    click model services menu
    click create model service button
    click upload manifest tab
    upload model deployment manifest    ${manifest_file}

create single manual scale model service using parameters via gui
    [Documentation]  To create a single model service with manual scaling via gui
    [Arguments]    ${model_id}    ${model_version}    ${service_name}    ${replicas}=1
    click dashboard menu
    click model services menu
    click create model service button
    provide manual scaling inputs to create a single model service    ${model_id}    ${model_version}    ${service_name}    ${replicas}
    click create button

create ab manual scale model service using parameters via gui
    [Documentation]  To create an ab model service with manual scaling via gui
    [Arguments]    ${modelA_id}    ${modelA_version}    ${modelB_id}    ${modelB_version}    ${service_name}    ${replicas}=1
    click dashboard menu
    click model services menu
    click create model service button
    provide manual scaling inputs to create an ab model service    ${modelA_id}    ${modelA_version}    ${modelB_id}    ${modelB_version}    ${service_name}    ${replicas}
    click create button

create single auto scale model service using parameters via gui
    [Documentation]  To create a single model service with auto scaling via gui
    [Arguments]    ${model_id}    ${model_version}    ${service_name}    ${metric_type}='cpu'    ${average_metric_value}=100    ${min_replicas}=1    ${max_replicas}=2
    click dashboard menu
    click model services menu
    click create model service button
    provide auto scaling inputs to create a single model service    ${metric_type}    ${model_id}    ${model_version}    ${service_name}    ${average_metric_value}    ${min_replicas}    ${max_replicas}
    click create button

create ab auto scale model service using parameters via gui
    [Documentation]  To create an ab model service with auto scaling via gui
    [Arguments]    ${modelA_id}    ${modelA_version}    ${modelB_id}    ${modelB_version}    ${service_name}    ${metric_type}='cpu'    ${average_metric_value}=100    ${min_replicas}=1    ${max_replicas}=2
    click dashboard menu
    click model services menu
    click create model service button
    provide auto scaling inputs to create an ab model service    ${metric_type}    ${modelA_id}    ${modelA_version}    ${modelB_id}    ${modelB_version}    ${service_name}    ${average_metric_value}    ${min_replicas}    ${max_replicas}
    click create button

check if model service is running via gui
    [Documentation]  To check the created model service is in running status via gui
    [Arguments]    ${service_name}
    click dashboard menu
    click model services menu
    verify the created model service is running    ${service_name}

delete model service via gui
    [Documentation]  To delete the model service via gui
    [Arguments]    ${service_name}
    click dashboard menu
    click model services menu
    select the model service    ${service_name}
    delete the model service

check if model service is deleted via gui
    [Documentation]  To check the model service is delete via gui
    [Arguments]    ${service_name}
    click dashboard menu
    click model services menu
    verify the model service is deleted    ${service_name}

invoke the model service via gui
    [Documentation]  To invoke the model service via gui
    [Arguments]    ${service_name}    ${input}    ${search_element}
    click dashboard menu
    click model services menu
    select the model service    ${service_name}
    Sleep    30s    wait until the model service pods are ready for prediction
    provide input to invoke the model service    ${input}
    invoke the model service
    verify the result of invoke model service    ${search_element}

onboard training package from sourcecode via gui
    [Documentation]  To onboard a training package via gui
    [Arguments]    ${training_source_code_file}
    click dashboard menu
    click training packages menu
    click onboard package button
    upload training package source code file    ${training_source_code_file}
    wait until the training package source code file is uploaded

check training package available status via gui
    [Documentation]  To check the onboarded training package becomes available via gui
    [Arguments]    ${training_package_id}    ${training_package_version}
    click dashboard menu
    click training packages menu
    verify the onboarded training package becomes available    ${training_package_id}    ${training_package_version}

delete training package via gui
    [Documentation]  To delete the training package via gui
    [Arguments]    ${training_package_id}    ${training_package_version}
    click dashboard menu
    click training packages menu
    select the training package to be deleted    ${training_package_id}    ${training_package_version}
    delete the training package

check if training package is deleted via gui
    [Documentation]  To check the training package is deleted via gui
    [Arguments]    ${training_package_id}    ${training_package_version}
    click dashboard menu
    click training packages menu
    verify the training package is deleted    ${training_package_id}    ${training_package_version}

start training job via gui
    [Documentation]  To start the training job via gui
    [Arguments]    ${training_package_id}    ${training_package_version}
    click dashboard menu
    click training packages menu
    start the training job    ${training_package_id}    ${training_package_version}

check if training job is completed via gui
    [Documentation]    To check the training job is in completed staus via gui
    [Arguments]    ${training_package_id}    ${training_package_version}
    click dashboard menu
    click training jobs menu
    verify the training job is completed    ${training_package_id}    ${training_package_version}

download training results via gui
    [Documentation]    To download the results of the training job via gui
    [Arguments]    ${training_package_id}    ${training_package_version}
    click dashboard menu
    click training jobs menu
    download the training results    ${training_package_id}    ${training_package_version}

delete training job via gui
    [Documentation]    To delete the training job via gui
    [Arguments]    ${training_package_id}    ${training_package_version}
    click dashboard menu
    click training jobs menu
    delete the training job    ${training_package_id}    ${training_package_version}

check if training job is deleted via gui
    [Documentation]  To check the training job is deleted via gui
    [Arguments]    ${training_package_id}    ${training_package_version}
    click dashboard menu
    click training jobs menu
    verify the training job is deleted    ${training_package_id}    ${training_package_version}
