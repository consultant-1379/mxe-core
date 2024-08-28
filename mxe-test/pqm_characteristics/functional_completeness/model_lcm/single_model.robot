*** Settings ***
Documentation    Tests to verify the use cases of single model deployment via cli and gui interface
Metadata    Version    0.1
Library    OperatingSystem
Library    Collections
Library    String
Library    yaml
Library  libraries/MxeModelCliLibrary.py
Library  libraries/MxeModelServiceCliLibrary.py
Resource  keywords/mxe_api_utilities_keywords.robot
Resource  keywords/mxe_gui_utilities_higher_level_keywords.robot
Resource  keywords/mxe_cli_utilities_keywords.robot
Variables  variables/mxe_cluster_details.py
Variables  variables/model_onboarding_inputs.py
Variables  variables/model_service_inputs.py

*** Variables ***

*** Test Cases ***

single model deployment (manual-scaling) from internal registry via cli
    [Documentation]    To onboard a model from source code and perform single model lcm (deploy with manual scaling-invoke-delete) via cli
    ...                The manifest will have just the model id in the image name parameter.
    [Tags]    cli    single-model    manual-scaling    internal    tc1    test-weekly
    [Setup]    onboard model from sourcecode    source_code_path=${model3_source_code_filepath}    model_id=${model3_id_int}    model_version=${model3_version_int}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc1_cli_model_service_name}
    ...           AND    delete model    model_id=${model3_id_int}    model_version=${model3_version_int}
    create model service via cli    manifest_file=${tc1_cli_single_model_ms_int}
    check if model service is running via cli    service_name=${tc1_cli_model_service_name}    
    invoke the model service via api    service_name=${tc1_cli_model_service_name}    input=${model_iris_input_file}

single model deployment (manual-scaling) from internal registry (full repo) via cli
    [Documentation]    To onboard a model from source code and perform single model lcm (deploy with manual scaling-invoke-delete) via cli
    ...                The manifest will have full container registry path of model in the image name parameter.
    [Tags]    cli    single-model    manual-scaling    internal    tc2    test-dev    test-staging    test-weekly
    [Setup]    onboard model from sourcecode    source_code_path=${model3_source_code_filepath}    model_id=${model3_id_int}    model_version=${model3_version_int}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc2_cli_model_service_name}
    ...           AND    delete model    model_id=${model3_id_int}    model_version=${model3_version_int}
    ${generated_manifest}=    update manifest with full image path of internal registry    model_deployment_manifest_file=${tc2_cli_single_model_ms_int_fullpath}    model_id=${model3_id_int}    model_version=${model3_version_int}
    Create File  updated_model_deployment_manifest_file.yaml  ${generated_manifest}
    create model service via cli    manifest_file=updated_model_deployment_manifest_file.yaml
    check if model service is running via cli    service_name=${tc2_cli_model_service_name}
    invoke the model service via api    service_name=${tc2_cli_model_service_name}    input=${model_iris_input_file}

single model deployment (manual-scaling) from external registry via cli
    [Documentation]    To onboard a model from external registry and perform single model lcm (deploy with manual scaling-invoke-delete) via cli
    ...                The manifest will have just the model id in the image name parameter.
    ...                Post invoking, prometheus is queried to validate the metrics availability for model service.
    [Tags]    cli    single-model    manual-scaling    tc3    test-dev    test-weekly    prometheus
    [Setup]    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc3_cli_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    create model service via cli    manifest_file=${tc3_cli_single_model_ms}
    check if model service is running via cli    service_name=${tc3_cli_model_service_name}
    Repeat Keyword    5 times    invoke the model service via api    service_name=${tc3_cli_model_service_name}    input=${model_inception_input_file}
    sleep    60s    wait until prometheus scrape the metrics from seldon
    invoke prometheus with a query    service_name=${tc3_cli_model_service_name}

single model deployment (manual-scaling) from external registry (full path) via cli
    [Documentation]    To onboard a model from external registry and perform single model lcm (deploy with manual scaling-invoke-delete) via cli
    ...                The manifest will have full external registry path of model in the image name parameter.
    [Tags]    cli    single-model    manual-scaling    tc4    test-dev    test-weekly
    [Setup]    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc4_cli_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    create model service via cli    manifest_file=${tc4_cli_single_model_ms_fullpath}
    check if model service is running via cli    service_name=${tc4_cli_model_service_name}
    invoke the model service via api    service_name=${tc4_cli_model_service_name}    input=${model_inception_input_file}

single model deployment (manual-scaling) using manifest via gui
    [Documentation]    To onboard a model from external registry and perform single model lcm (deploy with manual scaling-invoke-delete) via gui using manifest file
    [Tags]    gui    single-model    manual-scaling    tc5
    [Setup]    Run Keywords    onboard model from external registry    model_id=${single_model1_id_gui_ext}    model_author=${single_model1_author_gui_ext}    model_title=${single_model1_title_gui_ext}    model_description=${single_model1_description_gui_ext}    model_version=${single_model1_version_gui_ext}    model_registry_path=${single_model1_registry_path_gui_ext}
    ...        AND    login to mxe gui
    [Teardown]    Run Keywords    delete model service    interface='gui'    service_name=${tc5_gui_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_gui_ext}    model_version=${single_model1_version_gui_ext}
    ...           AND    logout of mxe gui
    create model service using manifest via gui    manifest_file=${tc5_gui_single_model_ms}
    check if model service is running via gui    service_name=${tc5_gui_model_service_name}
    invoke the model service via gui    service_name=${tc5_gui_model_service_name}    input=${model_iris_input_data}    search_element=${model_iris_search_element}

single model deployment (manual-scaling) using parameters via gui
    [Documentation]    To onboard a model from external registry and perform single model lcm (deploy with manual scaling-invoke-delete) via gui using parameters
    [Tags]    gui    single-model    manual-scaling    tc6
    [Setup]    Run Keywords    onboard model from external registry    model_id=${single_model1_id_gui_ext}    model_author=${single_model1_author_gui_ext}    model_title=${single_model1_title_gui_ext}    model_description=${single_model1_description_gui_ext}    model_version=${single_model1_version_gui_ext}    model_registry_path=${single_model1_registry_path_gui_ext}
    ...        AND    login to mxe gui
    [Teardown]    Run Keywords    delete model service    interface='gui'    service_name=${tc6_gui_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_gui_ext}    model_version=${single_model1_version_gui_ext}
    ...           AND    logout of mxe gui
    create single manual scale model service using parameters via gui    model_id=${single_model1_id_gui_ext}    model_version=${single_model1_version_gui_ext}    service_name=${tc6_gui_model_service_name}    replicas=2
    check if model service is running via gui    service_name=${tc6_gui_model_service_name}
    invoke the model service via gui    service_name=${tc6_gui_model_service_name}    input=${model_iris_input_data}    search_element=${model_iris_search_element}

single model deployment (auto-scaling cpu) from external registry via cli
    [Documentation]    To onboard a model from external registry and perform single model lcm (deploy with auto scaling via cpu-invoke-delete) via cli
    [Tags]    cli    single-model    auto-scaling    cpu    tc7    test-dev    test-weekly
    [Setup]    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc7_cli_model_service_cpu_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    create model service via cli    manifest_file=${tc7_cli_single_model_as_cpu}
    check if model service is running via cli    service_name=${tc7_cli_model_service_cpu_name}
    invoke the model service via api    service_name=${tc7_cli_model_service_cpu_name}    input=${model_inception_input_file}

single model deployment (auto-scaling memory) from external registry via cli
    [Documentation]    To onboard a model from external registry and perform single model lcm (deploy with auto scaling via memory-invoke-delete) via cli
    [Tags]    cli    single-model    auto-scaling    memory    tc8    test-dev    test-weekly
    [Setup]    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc8_cli_model_service_mem_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    create model service via cli    manifest_file=${tc8_cli_single_model_as_mem}
    check if model service is running via cli    service_name=${tc8_cli_model_service_mem_name}
    invoke the model service via api    service_name=${tc8_cli_model_service_mem_name}    input=${model_inception_input_file}

single model deployment (auto-scaling cpu) using manifest via gui
    [Documentation]    To onboard a model from external registry and perform single model lcm (deploy with auto scaling via cpu-invoke-delete) via gui using manifest file
    [Tags]    gui    single-model    auto-scaling    cpu    tc9
    [Setup]    Run Keywords    onboard model from external registry    model_id=${single_model1_id_gui_ext}    model_author=${single_model1_author_gui_ext}    model_title=${single_model1_title_gui_ext}    model_description=${single_model1_description_gui_ext}    model_version=${single_model1_version_gui_ext}    model_registry_path=${single_model1_registry_path_gui_ext}
    ...        AND    login to mxe gui
    [Teardown]    Run Keywords    delete model service    interface='gui'    service_name=${tc9_gui_model_service_cpu_name}
    ...           AND    delete model    model_id=${single_model1_id_gui_ext}    model_version=${single_model1_version_gui_ext}
    ...           AND    logout of mxe gui
    create model service using manifest via gui    manifest_file=${tc9_gui_single_model_as_cpu}
    check if model service is running via gui    service_name=${tc9_gui_model_service_cpu_name}
    invoke the model service via gui    service_name=${tc9_gui_model_service_cpu_name}    input=${model_iris_input_data}    search_element=${model_iris_search_element}

single model deployment (auto-scaling cpu) using parameters via gui
    [Documentation]    To onboard a model from external registry and perform single model lcm (deploy with auto scaling via cpu-invoke-delete) via gui using parameters
    [Tags]    gui    single-model    auto-scaling    cpu    tc10
    [Setup]    Run Keywords    onboard model from external registry    model_id=${single_model1_id_gui_ext}    model_author=${single_model1_author_gui_ext}    model_title=${single_model1_title_gui_ext}    model_description=${single_model1_description_gui_ext}    model_version=${single_model1_version_gui_ext}    model_registry_path=${single_model1_registry_path_gui_ext}
    ...        AND    login to mxe gui
    [Teardown]    Run Keywords    delete model service    interface='gui'    service_name=${tc10_gui_model_service_cpu_name}
    ...           AND    delete model    model_id=${single_model1_id_gui_ext}    model_version=${single_model1_version_gui_ext}
    ...           AND    logout of mxe gui
    create single auto scale model service using parameters via gui    metric_type='cpu'    model_id=${single_model1_id_gui_ext}    model_version=${single_model1_version_gui_ext}    service_name=${tc10_gui_model_service_cpu_name}    average_metric_value=200    min_replicas=1    max_replicas=3
    check if model service is running via gui    service_name=${tc10_gui_model_service_cpu_name}
    invoke the model service via gui    service_name=${tc10_gui_model_service_cpu_name}    input=${model_iris_input_data}    search_element=${model_iris_search_element}

single model deployment (auto-scaling memory) using manifest via gui
    [Documentation]    To onboard a model from external registry and perform single model lcm (deploy with auto scaling via memory-invoke-delete) via gui using manifest file
    [Tags]    gui    single-model    auto-scaling    memory    tc11
    [Setup]    Run Keywords    onboard model from external registry    model_id=${single_model1_id_gui_ext}    model_author=${single_model1_author_gui_ext}    model_title=${single_model1_title_gui_ext}    model_description=${single_model1_description_gui_ext}    model_version=${single_model1_version_gui_ext}    model_registry_path=${single_model1_registry_path_gui_ext}
    ...        AND    login to mxe gui
    [Teardown]    Run Keywords    delete model service    interface='gui'    service_name=${tc11_gui_model_service_mem_name}
    ...           AND    delete model    model_id=${single_model1_id_gui_ext}    model_version=${single_model1_version_gui_ext}
    ...           AND    logout of mxe gui
    create model service using manifest via gui    manifest_file=${tc11_gui_single_model_as_mem}
    check if model service is running via gui    service_name=${tc11_gui_model_service_mem_name}
    invoke the model service via gui    service_name=${tc11_gui_model_service_mem_name}    input=${model_iris_input_data}    search_element=${model_iris_search_element}

single model deployment (auto-scaling memory) using parameters via gui
    [Documentation]    To onboard a model from external registry and perform single model lcm (deploy with auto scaling via memory-invoke-delete) via gui using parameters
    [Tags]    gui    single-model    auto-scaling    memory    tc12
    [Setup]    Run Keywords    onboard model from external registry    model_id=${single_model1_id_gui_ext}    model_author=${single_model1_author_gui_ext}    model_title=${single_model1_title_gui_ext}    model_description=${single_model1_description_gui_ext}    model_version=${single_model1_version_gui_ext}    model_registry_path=${single_model1_registry_path_gui_ext}
    ...        AND    login to mxe gui
    [Teardown]    Run Keywords    delete model service    interface='gui'    service_name=${tc12_gui_model_service_mem_name}
    ...           AND    delete model    model_id=${single_model1_id_gui_ext}    model_version=${single_model1_version_gui_ext}
    ...           AND    logout of mxe gui
    create single auto scale model service using parameters via gui    metric_type='memory'    model_id=${single_model1_id_gui_ext}    model_version=${single_model1_version_gui_ext}    service_name=${tc12_gui_model_service_mem_name}    average_metric_value=1500    min_replicas=1    max_replicas=3
    check if model service is running via gui    service_name=${tc12_gui_model_service_mem_name}
    invoke the model service via gui    service_name=${tc12_gui_model_service_mem_name}    input=${model_iris_input_data}    search_element=${model_iris_search_element}

modify instances of single model deployment (manual-scaling) via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify instances - invoke - delete the model service via cli
    ...                The manifest will have just the model id in the image name parameter.
    [Tags]    cli    single-model    modify-instance    manual-scaling    tc13    modify    test-weekly
    [Setup]    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc13_cli_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    create model service via cli    manifest_file=${tc13_cli_single_model_ms}
    check if model service is running via cli    service_name=${tc13_cli_model_service_name}    instances=1
    invoke the model service via api    service_name=${tc13_cli_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc13_cli_model_service_name}    manifest_file=${tc13_cli_modify_scale_up_single_model_ms}
    check if model service is running via cli    service_name=${tc13_cli_model_service_name}    instances=2
    invoke the model service via api    service_name=${tc13_cli_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc13_cli_model_service_name}    manifest_file=${tc13_cli_modify_scale_down_single_model_ms}
    check if model service is running via cli    service_name=${tc13_cli_model_service_name}    instances=1
    invoke the model service via api    service_name=${tc13_cli_model_service_name}    input=${model_inception_input_file}

modify model of single model deployment (manual-scaling) via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify model - invoke - delete the model service via cli
    ...                The manifest will have just the model id in the image name parameter.
    [Tags]    cli    single-model    modify-model    manual-scaling    tc14    modify    test-weekly
    [Setup]    Run Keywords    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${single_model2_id_cli_ext}    model_author=${single_model2_author_cli_ext}    model_title=${single_model2_title_cli_ext}    model_description=${single_model2_description_cli_ext}    model_version=${single_model2_version_cli_ext}    model_registry_path=${single_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc14_cli_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    ...           AND    delete model    model_id=${single_model2_id_cli_ext}    model_version=${single_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc14_cli_single_model_ms}
    check if model service is running via cli    service_name=${tc14_cli_model_service_name}    instances=1    model=${single_model1_id_cli_ext}:${single_model1_version_cli_ext}
    invoke the model service via api    service_name=${tc14_cli_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc14_cli_model_service_name}    manifest_file=${tc14_cli_modify_single_model_ms}
    check if model service is running via cli    service_name=${tc14_cli_model_service_name}    instances=1    model=${single_model2_id_cli_ext}:${single_model2_version_cli_ext}   
    invoke the model service via api    service_name=${tc14_cli_model_service_name}    input=${model_inception_input_file}

modify instances and model of single model deployment (manual-scaling) via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify instances & model - invoke - delete the model service via cli
    ...                The manifest will have just the model id in the image name parameter.
    [Tags]    cli    single-model    modify-model    modify-instance    manual-scaling    tc15    modify    test-dev    test-weekly
    [Setup]    Run Keywords    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${single_model2_id_cli_ext}    model_author=${single_model2_author_cli_ext}    model_title=${single_model2_title_cli_ext}    model_description=${single_model2_description_cli_ext}    model_version=${single_model2_version_cli_ext}    model_registry_path=${single_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc15_cli_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    ...           AND    delete model    model_id=${single_model2_id_cli_ext}    model_version=${single_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc15_cli_single_model_ms}
    check if model service is running via cli    service_name=${tc15_cli_model_service_name}    instances=1    model=${single_model1_id_cli_ext}:${single_model1_version_cli_ext}
    invoke the model service via api    service_name=${tc15_cli_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc15_cli_model_service_name}    manifest_file=${tc15_cli_modify_single_model_ms}
    check if model service is running via cli    service_name=${tc15_cli_model_service_name}    instances=2    model=${single_model2_id_cli_ext}:${single_model2_version_cli_ext}   
    invoke the model service via api    service_name=${tc15_cli_model_service_name}    input=${model_inception_input_file}

modify metric type of single model deployment (auto-scaling) from via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify metric type - invoke - delete the model service via cli
    [Tags]    cli    single-model    modify-metric    auto-scaling    tc16    modify    test-weekly-off
    [Setup]    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc16_cli_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    create model service via cli    manifest_file=${tc16_cli_single_model_as}
    check if model service is running via cli    service_name=${tc16_cli_model_service_name}    autoscaling=cpu:300m
    invoke the model service via api    service_name=${tc16_cli_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc16_cli_model_service_name}    manifest_file=${tc16_cli_modify_single_model_as}
    check if model service is running via cli    service_name=${tc16_cli_model_service_name}    autoscaling=memory:1500Mi   
    invoke the model service via api    service_name=${tc16_cli_model_service_name}    input=${model_inception_input_file}

modify instances of single model deployment (auto-scaling) from via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify instances - invoke - delete the model service via cli
    [Tags]    cli    single-model    modify-instance    auto-scaling    tc17    modify    test-weekly-off
    [Setup]    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc17_cli_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    create model service via cli    manifest_file=${tc17_cli_single_model_as}
    check if model service is running via cli    service_name=${tc17_cli_model_service_name}    autoscaling=cpu:300m    instances=1-3
    invoke the model service via api    service_name=${tc17_cli_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc17_cli_model_service_name}    manifest_file=${tc17_cli_modify_scale_up_single_model_as}
    check if model service is running via cli    service_name=${tc17_cli_model_service_name}    autoscaling=cpu:300m    instances=2-4  
    invoke the model service via api    service_name=${tc17_cli_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc17_cli_model_service_name}    manifest_file=${tc17_cli_modify_scale_down_single_model_as}
    check if model service is running via cli    service_name=${tc17_cli_model_service_name}    autoscaling=cpu:300m    instances=1-3  
    invoke the model service via api    service_name=${tc17_cli_model_service_name}    input=${model_inception_input_file}

modify model of single model deployment (auto-scaling) from via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify model - invoke - delete the model service via cli
    [Tags]    cli    single-model    modify-model    auto-scaling    tc18    modify    test-weekly-off
    [Setup]    Run Keywords    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${single_model2_id_cli_ext}    model_author=${single_model2_author_cli_ext}    model_title=${single_model2_title_cli_ext}    model_description=${single_model2_description_cli_ext}    model_version=${single_model2_version_cli_ext}    model_registry_path=${single_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc18_cli_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    ...           AND    delete model    model_id=${single_model2_id_cli_ext}    model_version=${single_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc18_cli_single_model_as}
    check if model service is running via cli    service_name=${tc18_cli_model_service_name}    autoscaling=cpu:300m    instances=1-3    model=${single_model1_id_cli_ext}:${single_model1_version_cli_ext}
    invoke the model service via api    service_name=${tc18_cli_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc18_cli_model_service_name}    manifest_file=${tc18_cli_modify_single_model_as}
    check if model service is running via cli    service_name=${tc18_cli_model_service_name}    autoscaling=cpu:300m    instances=1-3    model=${single_model2_id_cli_ext}:${single_model2_version_cli_ext}     
    invoke the model service via api    service_name=${tc18_cli_model_service_name}    input=${model_inception_input_file}

modify metric type, instances and model of single model deployment (auto-scaling) from via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify metric type, instances and model - invoke - delete the model service via cli
    [Tags]    cli    single-model    modify-metric    modify-instance    modify-model    auto-scaling    tc19    modify    test-dev-off    test-weekly-off
    [Setup]    Run Keywords    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${single_model2_id_cli_ext}    model_author=${single_model2_author_cli_ext}    model_title=${single_model2_title_cli_ext}    model_description=${single_model2_description_cli_ext}    model_version=${single_model2_version_cli_ext}    model_registry_path=${single_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc19_cli_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    ...           AND    delete model    model_id=${single_model2_id_cli_ext}    model_version=${single_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc19_cli_single_model_as}
    check if model service is running via cli    service_name=${tc19_cli_model_service_name}    autoscaling=memory:1500Mi    instances=1-3    model=${single_model1_id_cli_ext}:${single_model1_version_cli_ext}
    invoke the model service via api    service_name=${tc19_cli_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc19_cli_model_service_name}    manifest_file=${tc19_cli_modify_single_model_as}
    check if model service is running via cli    service_name=${tc19_cli_model_service_name}    autoscaling=cpu:300m    instances=2-4    model=${single_model2_id_cli_ext}:${single_model2_version_cli_ext}     
    invoke the model service via api    service_name=${tc19_cli_model_service_name}    input=${model_inception_input_file}

verify the help options in model service cli command
    [Documentation]    To verify mxe-model command options as given below:
    ...    mxe-service help
    ...    mxe-service version
    ...    mxe-service list --help
    ...    mxe-service list -h
    ...    mxe-service list --verbose
    ...    mxe-service list -v
    [Tags]    cli    test-dev    tc20    test-weekly
    check model service command options

delete a single model service that does not exist via cli
    [Documentation]    To verify valid error message while deleting a model that does not exist
    [Tags]    cli    single-model    negative    test-dev    tc21    test-weekly
    ${msg}=    Run Keyword And Expect Error    *    delete model service via cli    service_name=unavailable-service
    Should Contain    ${msg}    Error: Model service "unavailable-service" does not exist

create a single model service that already exists via cli
    [Documentation]    To verify valid error message while trying to create a model service with a name that already exists
    [Tags]    cli    single-model    negative    test-dev    tc22    test-weekly
    [Setup]    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc22_cli_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    create model service via cli    manifest_file=${tc22_cli_single_model}
    check if model service is running via cli    service_name=${tc22_cli_model_service_name}
    ${msg}=    Run Keyword And Expect Error    *    create model service via cli    manifest_file=${tc22_cli_single_model_negative}
    Should Contain    ${msg}    Error: Model service "${tc22_cli_model_service_name}" is already running on cluster

create a single model service from a non-existing model via cli
    [Documentation]    To verify valid error message while trying to create a model service from a non-existing model
    [Tags]    cli    single-model    negative    test-dev    tc23    test-weekly
    ${msg}=    Run Keyword And Expect Error    *    create model service via cli    manifest_file=${tc23_cli_single_model_negative}
    Should Contain    ${msg}    Error: Could not perform operation for model service, Model with name "${tc23_unavilable_model_name}:${tc23_unavilable_model_version}" is not onboarded

modify a single model service to a non-existing model via cli
    [Documentation]    To verify valid error message while trying to modify a model service to a non-existing model
    [Tags]    cli    single-model    negative    test-dev    tc24    test-weekly
    [Setup]    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc24_cli_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    create model service via cli    manifest_file=${tc24_cli_single_model}
    check if model service is running via cli    service_name=${tc24_cli_model_service_name}    model=${single_model1_id_cli_ext}:${single_model1_version_cli_ext}
    ${msg}=    Run Keyword And Expect Error    *    modify model service via cli    service_name=${tc24_cli_model_service_name}    manifest_file=${tc24_cli_modify_single_model_negative}
    Should Contain    ${msg}    Error: Could not perform operation for model service, Model with name "${tc24_unavilable_model_name}:${tc24_unavilable_model_version}" is not onboarded

modify a non-existing single model service via cli
    [Documentation]    To verify valid error message while trying to modify a model service that does not exist
    [Tags]    cli    single-model    negative    test-dev    tc25    test-weekly
    [Setup]    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc25_cli_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    create model service via cli    manifest_file=${tc25_cli_single_model}
    check if model service is running via cli    service_name=${tc25_cli_model_service_name}    model=${single_model1_id_cli_ext}:${single_model1_version_cli_ext}
    ${msg}=    Run Keyword And Expect Error    *    modify model service via cli    service_name=${tc25_cli_unavailable_model_service_name}    manifest_file=${tc25_cli_modify_single_model_negative}
    Should Contain    ${msg}    Error: Model service "${tc25_cli_unavailable_model_service_name}" does not exist

modify a single model service with a negative instance via cli
    [Documentation]    To verify valid error message while trying to modify a model service with a negative instance
    [Tags]    cli    single-model    negative    tc26    test-dev    test-weekly
    [Setup]    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc26_cli_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    create model service via cli    manifest_file=${tc26_cli_single_model}
    check if model service is running via cli    service_name=${tc26_cli_model_service_name}    instances=1
    ${msg}=    Run Keyword And Expect Error    *    modify model service via cli    service_name=${tc26_cli_model_service_name}    manifest_file=${tc26_cli_modify_single_model_negative}
    Should Contain    ${msg}    Error: Invalid number of replicas: -2. Number of replicas can not be less than 1.

create a single model service (auto-scaling) with min replicas greater than max replicas via cli
    [Documentation]    To verify valid error message while trying to create a model service (auto-scaling) with min replicas greater than max replicas
    [Tags]    cli    single-model    negative    tc27    test-dev    test-weekly
    [Setup]    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    [Teardown]    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    ${msg}=    Run Keyword And Expect Error    *    create model service via cli    manifest_file=${tc27_cli_single_model}
    Should Contain    ${msg}    Error: Invalid number of maxReplicas: 1. Number of maxReplicas can not be less than number of minReplicas.

modify single model deployment from manual-scaling to auto-scaling via cli
    [Documentation]    To deploy a manul scaling based model service and then modifying it to auto scaling based model service
    [Tags]    cli    single-model    modify    tc28    test-weekly-off
    [Setup]    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc28_cli_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    create model service via cli    manifest_file=${tc28_cli_single_model_ms}
    check if model service is running via cli    service_name=${tc28_cli_model_service_name}    instances=1    model=${single_model1_id_cli_ext}:${single_model1_version_cli_ext}   
    invoke the model service via api    service_name=${tc28_cli_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc28_cli_model_service_name}    manifest_file=${tc28_cli_modify_single_model_as}
    check if model service is running via cli    service_name=${tc28_cli_model_service_name}    autoscaling=cpu:300m    instances=2-4    model=${single_model1_id_cli_ext}:${single_model1_version_cli_ext}     
    invoke the model service via api    service_name=${tc28_cli_model_service_name}    input=${model_inception_input_file}

modify single model deployment from auto-scaling to manual-scaling via cli
    [Documentation]    To deploy a auto scaling based model service and then modifying it to manual scaling based model service
    [Tags]    cli    single-model    modify    tc29    test-weekly-off
    [Setup]    onboard model from external registry    model_id=${single_model1_id_cli_ext}    model_author=${single_model1_author_cli_ext}    model_title=${single_model1_title_cli_ext}    model_description=${single_model1_description_cli_ext}    model_version=${single_model1_version_cli_ext}    model_registry_path=${single_model1_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc29_cli_model_service_name}
    ...           AND    delete model    model_id=${single_model1_id_cli_ext}    model_version=${single_model1_version_cli_ext}
    create model service via cli    manifest_file=${tc29_cli_single_model_as}
    check if model service is running via cli    service_name=${tc29_cli_model_service_name}    autoscaling=cpu:300m    instances=2-4    model=${single_model1_id_cli_ext}:${single_model1_version_cli_ext}     
    invoke the model service via api    service_name=${tc29_cli_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc29_cli_model_service_name}    manifest_file=${tc29_cli_modify_single_model_ms}
    check if model service is running via cli    service_name=${tc29_cli_model_service_name}    instances=1    model=${single_model1_id_cli_ext}:${single_model1_version_cli_ext}   
    invoke the model service via api    service_name=${tc29_cli_model_service_name}    input=${model_inception_input_file}

single java model deployment (manual-scaling) from external registry via cli
    [Documentation]    To onboard a java based model from external registry and perform single model lcm (deploy with manual scaling-invoke-delete) via cli
    ...                The manifest will have just the model id in the image name parameter.
    [Tags]    cli    single-model    manual-scaling    tc30    test-dev    test-weekly
    [Setup]    onboard model from external registry    model_id=${single_model3_id_cli_ext}    model_author=${single_model3_author_cli_ext}    model_title=${single_model3_title_cli_ext}    model_description=${single_model3_description_cli_ext}    model_version=${single_model3_version_cli_ext}    model_registry_path=${single_model3_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc30_cli_model_service_name}
    ...           AND    delete model    model_id=${single_model3_id_cli_ext}    model_version=${single_model3_version_cli_ext}
    create model service via cli    manifest_file=${tc30_cli_single_model_ms}
    check if model service is running via cli    service_name=${tc30_cli_model_service_name}
    invoke the java model service via api    service_name=${tc30_cli_model_service_name}    input=${model_java_input_file}

single model deployment
    [Documentation]    This test case is used as pre-population before upgrade in nightly pipeline. 
    ...                Same pre-populated data will be vaidated post upgrade with a different test case.
    [Tags]    pre-upgrade    tc31
    onboard model from sourcecode    source_code_path=${model4_source_code_filepath}    model_id=${model4_id_int}    model_version=${model4_version_int}
    create model service via cli    manifest_file=${tc31_cli_single_model_ms}
    check if model service is running via cli    service_name=${tc31_cli_model_service_name}    
    invoke the model service via api    service_name=${tc31_cli_model_service_name}    input=${model_iris_input_file}

verify and invoke deployed model post upgrade
    [Documentation]    This test case is used to verify pre-populated data post upgrade.
    ...                onboard a model from source code and perform single model lcm
    [Tags]    post-upgrade    tc32
    check model available status via cli    model_id=${model4_id_int}    model_version=${model4_version_int}
    check if model service is running via cli    service_name=${tc31_cli_model_service_name}
    invoke the model service via api    service_name=${tc31_cli_model_service_name}    input=${model_iris_input_file}
    create model service via cli    manifest_file=${tc32_cli_single_model_ms}
    check if model service is running via cli    service_name=${tc32_cli_model_service_name}      
    invoke the model service via api    service_name=${tc32_cli_model_service_name}    input=${model_iris_input_file}

verify and invoke deployed model post rollback
    [Documentation]    This test case is used to verify pre-populated data post rollback.
    ...                onboard a model from source code and perform single model lcm
    [Tags]    post-rollback    tc33
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc31_cli_model_service_name}
    ...           AND    delete model service    interface='cli'    service_name=${tc32_cli_model_service_name}
    ...           AND    delete model service    interface='cli'    service_name=${tc33_cli_model_service_name}
    ...           AND    delete model    model_id=${model4_id_int}    model_version=${model4_version_int}
    check model available status via cli    model_id=${model4_id_int}    model_version=${model4_version_int}
    check if model service is running via cli    service_name=${tc31_cli_model_service_name}
    invoke the model service via api    service_name=${tc31_cli_model_service_name}    input=${model_iris_input_file}
    create model service via cli    manifest_file=${tc33_cli_single_model_ms}
    check if model service is running via cli    service_name=${tc33_cli_model_service_name}      
    invoke the model service via api    service_name=${tc33_cli_model_service_name}    input=${model_iris_input_file}

*** Keywords ***

update manifest with full image path of internal registry
    [Documentation]     To update the image name in the model deployment manifest file with full path of internal registry
    [Tags]    sample
    [Arguments]    ${model_deployment_manifest_file}    ${model_id}    ${model_version}    
    ${manifest_file}=  Get File  ${model_deployment_manifest_file}    # file should be in valid YAML format
    ${load_manifest}=  yaml.Safe Load  ${manifest_file}
    ${mxe_end_point}=    Remove String    ${mxe_host}    https://
    Set To Dictionary  ${load_manifest}[spec][predictors][0][componentSpecs][0][spec][containers][0]  image=${mxe_end_point}/${model_id}:${model_version}
    ${updated_manifest}=  yaml.Dump  ${load_manifest}
    log    ${updated_manifest}
    [Return]    ${updated_manifest}
