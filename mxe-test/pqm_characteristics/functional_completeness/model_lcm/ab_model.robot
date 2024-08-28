*** Settings ***
Documentation    Tests to verify the use cases of ab model deployment via cli and gui interface
Metadata    Version    0.1
Library  libraries/MxeModelCliLibrary.py
Library  libraries/MxeModelServiceCliLibrary.py
Resource  keywords/mxe_api_utilities_keywords.robot
Resource  keywords/mxe_gui_utilities_higher_level_keywords.robot
Resource  keywords/mxe_cli_utilities_keywords.robot
Variables  variables/model_onboarding_inputs.py
Variables  variables/model_service_inputs.py 

*** Variables ***

*** Test Cases ***

ab model deployment (manual-scaling) via cli
    [Documentation]    To onboard a model from external registry and perform ab model lcm (deploy with manual scaling-invoke-delete) via cli
    [Tags]    cli    ab-model    manual-scaling    tc1    test-dev    test-weekly
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc1_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc1_cli_ab_model_ms}
    check if model service is running via cli    service_name=${tc1_cli_ab_model_service_name}
    invoke the model service via api    service_name=${tc1_cli_ab_model_service_name}    input=${model_inception_input_file}

ab model deployment (manual-scaling) using manifest via gui
    [Documentation]    To onboard a model from external registry and perform ab model lcm (deploy with manual scaling-invoke-delete) via gui using manifest file
    [Tags]    gui    ab-model    manual-scaling    tc2
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_gui_ext}    model_author=${ab_model1_author_gui_ext}    model_title=${ab_model1_title_gui_ext}    model_description=${ab_model1_description_gui_ext}    model_version=${ab_model1_version_gui_ext}    model_registry_path=${ab_model1_registry_path_gui_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_gui_ext}    model_author=${ab_model2_author_gui_ext}    model_title=${ab_model2_title_gui_ext}    model_description=${ab_model2_description_gui_ext}    model_version=${ab_model2_version_gui_ext}    model_registry_path=${ab_model2_registry_path_gui_ext}
    ...        AND    login to mxe gui
    [Teardown]    Run Keywords    delete model service    interface='gui'    service_name=${tc2_gui_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_gui_ext}    model_version=${ab_model1_version_gui_ext}
    ...           AND    delete model    model_id=${ab_model2_id_gui_ext}    model_version=${ab_model2_version_gui_ext}
    ...           AND    logout of mxe gui
    create model service using manifest via gui    manifest_file=${tc2_gui_ab_model_ms}
    check if model service is running via gui    service_name=${tc2_gui_ab_model_service_name}
    invoke the model service via gui    service_name=${tc2_gui_ab_model_service_name}    input=${model_iris_input_data}    search_element=${model_iris_search_element}

ab model deployment (manual-scaling) using parameters via gui
    [Documentation]    To onboard a model from external registry and perform ab model lcm (deploy with manual scaling-invoke-delete) via gui using parameters
    [Tags]    gui    ab-model    manual-scaling    tc3
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_gui_ext}    model_author=${ab_model1_author_gui_ext}    model_title=${ab_model1_title_gui_ext}    model_description=${ab_model1_description_gui_ext}    model_version=${ab_model1_version_gui_ext}    model_registry_path=${ab_model1_registry_path_gui_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_gui_ext}    model_author=${ab_model2_author_gui_ext}    model_title=${ab_model2_title_gui_ext}    model_description=${ab_model2_description_gui_ext}    model_version=${ab_model2_version_gui_ext}    model_registry_path=${ab_model2_registry_path_gui_ext}
    ...        AND    login to mxe gui
    [Teardown]    Run Keywords    delete model service    interface='gui'    service_name=${tc3_gui_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_gui_ext}    model_version=${ab_model1_version_gui_ext}
    ...           AND    delete model    model_id=${ab_model2_id_gui_ext}    model_version=${ab_model2_version_gui_ext}
    ...           AND    logout of mxe gui
    create ab manual scale model service using parameters via gui    modelA_id=${ab_model1_id_gui_ext}    modelA_version=${ab_model1_version_gui_ext}    modelB_id=${ab_model2_id_gui_ext}    modelB_version=${ab_model2_version_gui_ext}    service_name=${tc3_gui_ab_model_service_name}    replicas=2
    check if model service is running via gui    service_name=${tc3_gui_ab_model_service_name}
    invoke the model service via gui    service_name=${tc3_gui_ab_model_service_name}    input=${model_iris_input_data}    search_element=${model_iris_search_element}

ab model deployment (auto-scaling cpu) via cli
    [Documentation]    To onboard a model from external registry and perform ab model lcm (deploy with auto scaling via cpu-invoke-delete) via cli
    [Tags]    cli    ab-model    auto-scaling    cpu    tc4    test-dev    test-weekly
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc4_cli_ab_model_service_cpu_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc4_cli_ab_model_as_cpu}
    check if model service is running via cli    service_name=${tc4_cli_ab_model_service_cpu_name}
    invoke the model service via api    service_name=${tc4_cli_ab_model_service_cpu_name}    input=${model_inception_input_file}

ab model deployment (auto-scaling memory) via cli
    [Documentation]    To onboard a model from external registry and perform ab model lcm (deploy with auto scaling via memory-invoke-delete) via cli
    [Tags]    cli    ab-model    auto-scaling    memory    tc5    test-dev    test-weekly
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc5_cli_ab_model_service_mem_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc5_cli_ab_model_as_mem}
    check if model service is running via cli    service_name=${tc5_cli_ab_model_service_mem_name}
    invoke the model service via api    service_name=${tc5_cli_ab_model_service_mem_name}    input=${model_inception_input_file}

ab model deployment (auto-scaling cpu) using manifest via gui
    [Documentation]    To onboard a model from external registry and perform ab model lcm (deploy with auto scaling via cpu-invoke-delete) via gui using manifest file
    [Tags]    gui    ab-model    auto-scaling    cpu    tc6
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_gui_ext}    model_author=${ab_model1_author_gui_ext}    model_title=${ab_model1_title_gui_ext}    model_description=${ab_model1_description_gui_ext}    model_version=${ab_model1_version_gui_ext}    model_registry_path=${ab_model1_registry_path_gui_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_gui_ext}    model_author=${ab_model2_author_gui_ext}    model_title=${ab_model2_title_gui_ext}    model_description=${ab_model2_description_gui_ext}    model_version=${ab_model2_version_gui_ext}    model_registry_path=${ab_model2_registry_path_gui_ext}
    ...        AND    login to mxe gui
    [Teardown]    Run Keywords    delete model service    interface='gui'    service_name=${tc6_gui_ab_model_service_cpu_name}
    ...           AND    delete model    model_id=${ab_model1_id_gui_ext}    model_version=${ab_model1_version_gui_ext}
    ...           AND    delete model    model_id=${ab_model2_id_gui_ext}    model_version=${ab_model2_version_gui_ext}
    ...           AND    logout of mxe gui
    create model service using manifest via gui    manifest_file=${tc6_gui_ab_model_as_cpu}
    check if model service is running via gui    service_name=${tc6_gui_ab_model_service_cpu_name}
    invoke the model service via gui    service_name=${tc6_gui_ab_model_service_cpu_name}    input=${model_iris_input_data}    search_element=${model_iris_search_element}

ab model deployment (auto-scaling cpu) using parameters via gui
    [Documentation]    To onboard a model from external registry and perform ab model lcm (deploy with auto scaling via cpu-invoke-delete) via gui using parameters
    [Tags]    gui    ab-model    auto-scaling    cpu    tc7
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_gui_ext}    model_author=${ab_model1_author_gui_ext}    model_title=${ab_model1_title_gui_ext}    model_description=${ab_model1_description_gui_ext}    model_version=${ab_model1_version_gui_ext}    model_registry_path=${ab_model1_registry_path_gui_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_gui_ext}    model_author=${ab_model2_author_gui_ext}    model_title=${ab_model2_title_gui_ext}    model_description=${ab_model2_description_gui_ext}    model_version=${ab_model2_version_gui_ext}    model_registry_path=${ab_model2_registry_path_gui_ext}
    ...        AND    login to mxe gui
    [Teardown]    Run Keywords    delete model service    interface='gui'    service_name=${tc7_gui_ab_model_service_cpu_name}
    ...           AND    delete model    model_id=${ab_model1_id_gui_ext}    model_version=${ab_model1_version_gui_ext}
    ...           AND    delete model    model_id=${ab_model2_id_gui_ext}    model_version=${ab_model2_version_gui_ext}
    ...           AND    logout of mxe gui
    create ab auto scale model service using parameters via gui    metric_type='cpu'    modelA_id=${ab_model1_id_gui_ext}    modelA_version=${ab_model1_version_gui_ext}    modelB_id=${ab_model2_id_gui_ext}    modelB_version=${ab_model2_version_gui_ext}    service_name=${tc7_gui_ab_model_service_cpu_name}    average_metric_value=200    min_replicas=1    max_replicas=3
    check if model service is running via gui    service_name=${tc7_gui_ab_model_service_cpu_name}
    invoke the model service via gui    service_name=${tc7_gui_ab_model_service_cpu_name}    input=${model_iris_input_data}    search_element=${model_iris_search_element}

ab model deployment (auto-scaling memory) using manifest via gui
    [Documentation]    To onboard a model from external registry and perform ab model lcm (deploy with auto scaling via memory-invoke-delete) via gui using manifest file
    [Tags]    gui    ab-model    auto-scaling    memory    tc8
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_gui_ext}    model_author=${ab_model1_author_gui_ext}    model_title=${ab_model1_title_gui_ext}    model_description=${ab_model1_description_gui_ext}    model_version=${ab_model1_version_gui_ext}    model_registry_path=${ab_model1_registry_path_gui_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_gui_ext}    model_author=${ab_model2_author_gui_ext}    model_title=${ab_model2_title_gui_ext}    model_description=${ab_model2_description_gui_ext}    model_version=${ab_model2_version_gui_ext}    model_registry_path=${ab_model2_registry_path_gui_ext}
    ...        AND    login to mxe gui
    [Teardown]    Run Keywords    delete model service    interface='gui'    service_name=${tc8_gui_ab_model_service_mem_name}
    ...           AND    delete model    model_id=${ab_model1_id_gui_ext}    model_version=${ab_model1_version_gui_ext}
    ...           AND    delete model    model_id=${ab_model2_id_gui_ext}    model_version=${ab_model2_version_gui_ext}
    ...           AND    logout of mxe gui
    create model service using manifest via gui    manifest_file=${tc8_gui_ab_model_as_mem}
    check if model service is running via gui    service_name=${tc8_gui_ab_model_service_mem_name}
    invoke the model service via gui    service_name=${tc8_gui_ab_model_service_mem_name}    input=${model_iris_input_data}    search_element=${model_iris_search_element}

ab model deployment (auto-scaling memory) using parameters via gui
    [Documentation]    To onboard a model from external registry and perform ab model lcm (deploy with auto scaling via memory-invoke-delete) via gui using parameters
    [Tags]    gui    ab-model    auto-scaling    memory    tc9
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_gui_ext}    model_author=${ab_model1_author_gui_ext}    model_title=${ab_model1_title_gui_ext}    model_description=${ab_model1_description_gui_ext}    model_version=${ab_model1_version_gui_ext}    model_registry_path=${ab_model1_registry_path_gui_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_gui_ext}    model_author=${ab_model2_author_gui_ext}    model_title=${ab_model2_title_gui_ext}    model_description=${ab_model2_description_gui_ext}    model_version=${ab_model2_version_gui_ext}    model_registry_path=${ab_model2_registry_path_gui_ext}
    ...        AND    login to mxe gui
    [Teardown]    Run Keywords    delete model service    interface='gui'    service_name=${tc9_gui_ab_model_service_mem_name}
    ...           AND    delete model    model_id=${ab_model1_id_gui_ext}    model_version=${ab_model1_version_gui_ext}
    ...           AND    delete model    model_id=${ab_model2_id_gui_ext}    model_version=${ab_model2_version_gui_ext}
    ...           AND    logout of mxe gui
    create ab auto scale model service using parameters via gui    metric_type='memory'    modelA_id=${ab_model1_id_gui_ext}    modelA_version=${ab_model1_version_gui_ext}    modelB_id=${ab_model2_id_gui_ext}    modelB_version=${ab_model2_version_gui_ext}    service_name=${tc9_gui_ab_model_service_mem_name}    average_metric_value=1500    min_replicas=1    max_replicas=3
    check if model service is running via gui    service_name=${tc9_gui_ab_model_service_mem_name}
    invoke the model service via gui    service_name=${tc9_gui_ab_model_service_mem_name}    input=${model_iris_input_data}    search_element=${model_iris_search_element}

modify instances of ab model deployment (manual-scaling) via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify instances - invoke - delete the ab model service via cli
    [Tags]    cli    ab-model    modify-instance    manual-scaling    tc10    modify    test-weekly
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc10_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc10_cli_ab_model_ms}
    check if model service is running via cli    service_name=${tc10_cli_ab_model_service_name}    instances=1
    invoke the model service via api    service_name=${tc10_cli_ab_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc10_cli_ab_model_service_name}    manifest_file=${tc10_cli_modify_scale_up_ab_model_ms}
    check if model service is running via cli    service_name=${tc10_cli_ab_model_service_name}    instances=2
    invoke the model service via api    service_name=${tc10_cli_ab_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc10_cli_ab_model_service_name}    manifest_file=${tc10_cli_modify_scale_down_ab_model_ms}
    check if model service is running via cli    service_name=${tc10_cli_ab_model_service_name}    instances=1
    invoke the model service via api    service_name=${tc10_cli_ab_model_service_name}    input=${model_inception_input_file}

modify weights of ab model deployment (manual-scaling) via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify instances - invoke - delete the ab model service via cli
    [Tags]    cli    ab-model    modify-weights    manual-scaling    tc11    modify    test-weekly
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc11_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc11_cli_ab_model_ms}
    check if model service is running via cli    service_name=${tc11_cli_ab_model_service_name}    instances=1    weights=0.5,0.5
    invoke the model service via api    service_name=${tc11_cli_ab_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc11_cli_ab_model_service_name}    manifest_file=${tc11_cli_modify_ab_model_ms}
    check if model service is running via cli    service_name=${tc11_cli_ab_model_service_name}    instances=1    weights=0.8,0.2
    invoke the model service via api    service_name=${tc11_cli_ab_model_service_name}    input=${model_inception_input_file}

modify model of ab model deployment (manual-scaling) via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify instances - invoke - delete the ab model service via cli
    [Tags]    cli    ab-model    modify-model    manual-scaling    tc12    modify    test-weekly
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model3_id_cli_ext}    model_author=${ab_model3_author_cli_ext}    model_title=${ab_model3_title_cli_ext}    model_description=${ab_model3_description_cli_ext}    model_version=${ab_model3_version_cli_ext}    model_registry_path=${ab_model3_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc12_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model3_id_cli_ext}    model_version=${ab_model3_version_cli_ext}
    create model service via cli    manifest_file=${tc12_cli_ab_model_ms}
    check if model service is running via cli    service_name=${tc12_cli_ab_model_service_name}    instances=1    model_a=${ab_model1_id_cli_ext}:${ab_model1_version_cli_ext}    model_b=${ab_model2_id_cli_ext}:${ab_model2_version_cli_ext}   
    invoke the model service via api    service_name=${tc12_cli_ab_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc12_cli_ab_model_service_name}    manifest_file=${tc12_cli_modify_ab_model_ms}
    check if model service is running via cli    service_name=${tc12_cli_ab_model_service_name}    instances=1    model_a=${ab_model1_id_cli_ext}:${ab_model1_version_cli_ext}    model_b=${ab_model3_id_cli_ext}:${ab_model3_version_cli_ext}   
    invoke the model service via api    service_name=${tc12_cli_ab_model_service_name}    input=${model_inception_input_file}

modify instances, model and weights of ab model deployment (manual-scaling) via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify instances - invoke - delete the ab model service via cli
    [Tags]    cli    ab-model    modify-instance    modify-weights    modify-model    manual-scaling    tc13    modify    test-dev    test-weekly
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model3_id_cli_ext}    model_author=${ab_model3_author_cli_ext}    model_title=${ab_model3_title_cli_ext}    model_description=${ab_model3_description_cli_ext}    model_version=${ab_model3_version_cli_ext}    model_registry_path=${ab_model3_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc13_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model3_id_cli_ext}    model_version=${ab_model3_version_cli_ext}
    create model service via cli    manifest_file=${tc13_cli_ab_model_ms}
    check if model service is running via cli    service_name=${tc13_cli_ab_model_service_name}    instances=1    weights=0.5,0.5    model_a=${ab_model1_id_cli_ext}:${ab_model1_version_cli_ext}    model_b=${ab_model2_id_cli_ext}:${ab_model2_version_cli_ext}   
    invoke the model service via api    service_name=${tc13_cli_ab_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc13_cli_ab_model_service_name}    manifest_file=${tc13_cli_modify_ab_model_ms}
    check if model service is running via cli    service_name=${tc13_cli_ab_model_service_name}    instances=2    weights=0.8,0.2    model_a=${ab_model1_id_cli_ext}:${ab_model1_version_cli_ext}    model_b=${ab_model3_id_cli_ext}:${ab_model3_version_cli_ext}   
    invoke the model service via api    service_name=${tc13_cli_ab_model_service_name}    input=${model_inception_input_file}

modify metric type of ab model deployment (auto-scaling) via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify metric type - invoke - delete the ab model service via cli
    [Tags]    cli    ab-model    modify-metric    auto-scaling    tc14    modify    test-weekly-off
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc14_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc14_cli_ab_model_as}
    check if model service is running via cli    service_name=${tc14_cli_ab_model_service_name}    autoscaling=cpu:300m
    invoke the model service via api    service_name=${tc14_cli_ab_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc14_cli_ab_model_service_name}    manifest_file=${tc14_cli_modify_ab_model_as}
    check if model service is running via cli    service_name=${tc14_cli_ab_model_service_name}    autoscaling=memory:1500Mi
    invoke the model service via api    service_name=${tc14_cli_ab_model_service_name}    input=${model_inception_input_file}

modify instances of ab model deployment (auto-scaling) via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify instances - invoke - delete the ab model service via cli
    [Tags]    cli    ab-model    modify-instance    auto-scaling    tc15    modify    test-weekly-off
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc15_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc15_cli_ab_model_as}
    check if model service is running via cli    service_name=${tc15_cli_ab_model_service_name}    autoscaling=cpu:300m    instances=1-3
    invoke the model service via api    service_name=${tc15_cli_ab_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc15_cli_ab_model_service_name}    manifest_file=${tc15_cli_modify_scale_up_ab_model_as}
    check if model service is running via cli    service_name=${tc15_cli_ab_model_service_name}    autoscaling=cpu:300m    instances=2-4
    invoke the model service via api    service_name=${tc15_cli_ab_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc15_cli_ab_model_service_name}    manifest_file=${tc15_cli_modify_scale_down_ab_model_as}
    check if model service is running via cli    service_name=${tc15_cli_ab_model_service_name}    autoscaling=cpu:300m    instances=1-3
    invoke the model service via api    service_name=${tc15_cli_ab_model_service_name}    input=${model_inception_input_file}

modify weights of ab model deployment (auto-scaling) via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify weights - invoke - delete the ab model service via cli
    [Tags]    cli    ab-model    modify-weights    auto-scaling    tc16    modify    test-weekly-off
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc16_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc16_cli_ab_model_as}
    check if model service is running via cli    service_name=${tc16_cli_ab_model_service_name}    autoscaling=cpu:300m    instances=1-3    weights=0.5,0.5    
    invoke the model service via api    service_name=${tc16_cli_ab_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc16_cli_ab_model_service_name}    manifest_file=${tc16_cli_modify_ab_model_as}
    check if model service is running via cli    service_name=${tc16_cli_ab_model_service_name}    autoscaling=cpu:300m    instances=1-3    weights=0.8,0.2
    invoke the model service via api    service_name=${tc16_cli_ab_model_service_name}    input=${model_inception_input_file}

modify model of ab model deployment (auto-scaling) via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify model - invoke - delete the ab model service via cli
    [Tags]    cli    ab-model    modify-model    auto-scaling    tc17    modify    test-weekly-off
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model3_id_cli_ext}    model_author=${ab_model3_author_cli_ext}    model_title=${ab_model3_title_cli_ext}    model_description=${ab_model3_description_cli_ext}    model_version=${ab_model3_version_cli_ext}    model_registry_path=${ab_model3_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc17_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model3_id_cli_ext}    model_version=${ab_model3_version_cli_ext}
    create model service via cli    manifest_file=${tc17_cli_ab_model_as}
    check if model service is running via cli    service_name=${tc17_cli_ab_model_service_name}    autoscaling=cpu:300m    instances=1-3    weights=0.5,0.5    model_a=${ab_model1_id_cli_ext}:${ab_model1_version_cli_ext}    model_b=${ab_model2_id_cli_ext}:${ab_model2_version_cli_ext}
    invoke the model service via api    service_name=${tc17_cli_ab_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc17_cli_ab_model_service_name}    manifest_file=${tc17_cli_modify_ab_model_as}
    check if model service is running via cli    service_name=${tc17_cli_ab_model_service_name}    autoscaling=cpu:300m    instances=1-3    weights=0.5,0.5    model_a=${ab_model1_id_cli_ext}:${ab_model1_version_cli_ext}    model_b=${ab_model3_id_cli_ext}:${ab_model3_version_cli_ext}
    invoke the model service via api    service_name=${tc17_cli_ab_model_service_name}    input=${model_inception_input_file}

modify metric type, instances, model and weights of ab model deployment (auto-scaling) via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify metric type, instances, model and weights - invoke - delete the ab model service via cli
    [Tags]    cli    ab-model    modify-metric    modify-instance    modify-weights    modify-model    auto-scaling    tc18    modify    test-dev-off    test-weekly-off
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model3_id_cli_ext}    model_author=${ab_model3_author_cli_ext}    model_title=${ab_model3_title_cli_ext}    model_description=${ab_model3_description_cli_ext}    model_version=${ab_model3_version_cli_ext}    model_registry_path=${ab_model3_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc18_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model3_id_cli_ext}    model_version=${ab_model3_version_cli_ext}
    create model service via cli    manifest_file=${tc18_cli_ab_model_as}
    check if model service is running via cli    service_name=${tc18_cli_ab_model_service_name}    autoscaling=memory:1500Mi    instances=1-3    weights=0.5,0.5    model_a=${ab_model1_id_cli_ext}:${ab_model1_version_cli_ext}    model_b=${ab_model2_id_cli_ext}:${ab_model2_version_cli_ext}
    invoke the model service via api    service_name=${tc18_cli_ab_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc18_cli_ab_model_service_name}    manifest_file=${tc18_cli_modify_ab_model_as}
    check if model service is running via cli    service_name=${tc18_cli_ab_model_service_name}    autoscaling=cpu:300m    instances=2-4    weights=0.8,0.2    model_a=${ab_model1_id_cli_ext}:${ab_model1_version_cli_ext}    model_b=${ab_model3_id_cli_ext}:${ab_model3_version_cli_ext}
    invoke the model service via api    service_name=${tc18_cli_ab_model_service_name}    input=${model_inception_input_file}

modify one model of ab model deployment (manual-scaling) via cli
    [Documentation]    To onboard a model from external registry, create - invoke - modify instances - invoke - delete the ab model service via cli
    [Tags]    cli    ab-model    modify-model    manual-scaling    tc19    modify    negative    test-dev    test-weekly
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model3_id_cli_ext}    model_author=${ab_model3_author_cli_ext}    model_title=${ab_model3_title_cli_ext}    model_description=${ab_model3_description_cli_ext}    model_version=${ab_model3_version_cli_ext}    model_registry_path=${ab_model3_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc19_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model3_id_cli_ext}    model_version=${ab_model3_version_cli_ext}
    create model service via cli    manifest_file=${tc19_cli_ab_model}
    check if model service is running via cli    service_name=${tc19_cli_ab_model_service_name}    instances=1    model_a=${ab_model1_id_cli_ext}:${ab_model1_version_cli_ext}    model_b=${ab_model2_id_cli_ext}:${ab_model2_version_cli_ext}
    ${msg}=    Run Keyword And Expect Error    *    modify model service via cli    service_name=${tc19_cli_ab_model_service_name}    manifest_file=${tc19_cli_modify_ab_model_negative}
    Should Contain    ${msg}    Error: Invalid manifest: model type 'static' should have 2 model images

create an ab model service (auto-scaling) with min replicas greater than max replicas via cli
    [Documentation]    To verify valid error message while trying to create a ab model service (auto-scaling) with min replicas greater than max replicas
    [Tags]    cli    ab-model    auto-scaling    tc20    negative    test-dev    test-weekly
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    ${msg}=    Run Keyword And Expect Error    *    create model service via cli    manifest_file=${tc20_cli_ab_model}
    Should Contain    ${msg}    Error: Invalid number of maxReplicas: 1. Number of maxReplicas can not be less than number of minReplicas.

create an ab model service that already exists via cli
    [Documentation]    To verify valid error message while trying to create an ab model service with a name that already exists
    [Tags]    cli    ab-model    manual-scaling    tc21    negative    test-dev    test-weekly
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc21_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc21_cli_ab_model}
    check if model service is running via cli    service_name=${tc21_cli_ab_model_service_name}
    ${msg}=    Run Keyword And Expect Error    *    create model service via cli    manifest_file=${tc21_cli_modify_ab_model_negative}
    Should Contain    ${msg}    Error: Model service "${tc21_cli_ab_model_service_name}" is already running on cluster

create an ab model service from a non-existing model via cli
    [Documentation]    To verify valid error message while trying to create an ab model service from a non-existing model
    [Tags]    cli    ab-model    manual-scaling    tc22    negative    test-dev    test-weekly
    ${msg}=    Run Keyword And Expect Error    *    create model service via cli    manifest_file=${tc22_cli_ab_model_negative}
    Should Contain    ${msg}    Error: Could not perform operation for model service, Model with name "${tc22_unavilable_model_name}:${tc22_unavilable_model_version}" is not onboarded

modify an ab model service to a non-existing model via cli
    [Documentation]    To verify valid error message while trying to modify an ab model service to a non-existing model
    [Tags]    cli    ab-model    manual-scaling    tc23    negative    test-dev    test-weekly
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc23_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc23_cli_ab_model}
    check if model service is running via cli    service_name=${tc23_cli_ab_model_service_name}    model_a=${ab_model1_id_cli_ext}:${ab_model1_version_cli_ext}    model_b=${ab_model2_id_cli_ext}:${ab_model2_version_cli_ext}   
    ${msg}=    Run Keyword And Expect Error    *    modify model service via cli    service_name=${tc23_cli_ab_model_service_name}    manifest_file=${tc23_cli_modify_ab_model_negative}
    Should Contain    ${msg}    Error: Could not perform operation for model service, Model with name "${tc23_unavilable_model_name}:${tc23_unavilable_model_version}" is not onboarded

modify a non-existing ab model service via cli
    [Documentation]    To verify valid error message while trying to modify an ab model service that does not exist
    [Tags]    cli    ab-model    manual-scaling    tc24    negative    test-dev    test-weekly
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc24_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc24_cli_ab_model}
    check if model service is running via cli    service_name=${tc24_cli_ab_model_service_name}    model_a=${ab_model1_id_cli_ext}:${ab_model1_version_cli_ext}    model_b=${ab_model2_id_cli_ext}:${ab_model2_version_cli_ext}   
    ${msg}=    Run Keyword And Expect Error    *    modify model service via cli    service_name=${tc24_cli_unavailable_model_service_name}    manifest_file=${tc24_cli_modify_ab_model_negative}
    Should Contain    ${msg}    Error: Model service "${tc24_cli_unavailable_model_service_name}" does not exist

modify an ab model service with a negative instance via cli
    [Documentation]    To verify valid error message while trying to modify an ab model service with a negative instance
    [Tags]    cli    ab-model    manual-scaling    tc25    negative    test-dev    test-weekly
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc25_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc25_cli_ab_model}
    check if model service is running via cli    service_name=${tc25_cli_ab_model_service_name}    instances=1
    ${msg}=    Run Keyword And Expect Error    *    modify model service via cli    service_name=${tc25_cli_ab_model_service_name}    manifest_file=${tc25_cli_modify_ab_model_negative}
    Should Contain    ${msg}    Error: Invalid number of replicas: -2. Number of replicas can not be less than 1.

modify ab model deployment from manual-scaling to auto-scaling via cli
    [Documentation]    To deploy a manul scaling based ab model service and then modifying it to auto scaling based ab model service
    [Tags]    cli    ab-model    modify    tc26    test-weekly-off
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc26_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc26_cli_ab_model_ms}
    check if model service is running via cli    service_name=${tc26_cli_ab_model_service_name}    instances=1    model_a=${ab_model1_id_cli_ext}:${ab_model1_version_cli_ext}    model_b=${ab_model2_id_cli_ext}:${ab_model2_version_cli_ext}
    invoke the model service via api    service_name=${tc26_cli_ab_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc26_cli_ab_model_service_name}    manifest_file=${tc26_cli_modify_ab_model_as}
    check if model service is running via cli    service_name=${tc26_cli_ab_model_service_name}    autoscaling=cpu:300m    instances=1-3    weights=0.5,0.5    model_a=${ab_model1_id_cli_ext}:${ab_model1_version_cli_ext}    model_b=${ab_model2_id_cli_ext}:${ab_model2_version_cli_ext}
    invoke the model service via api    service_name=${tc26_cli_ab_model_service_name}    input=${model_inception_input_file}

modify ab model deployment from auto-scaling to manual-scaling via cli
    [Documentation]    To deploy a auto scaling based ab model service and then modifying it to manual scaling based ab model service
    [Tags]    cli    ab-model    modify    tc27    test-weekly-off
    [Setup]    Run Keywords    onboard model from external registry    model_id=${ab_model1_id_cli_ext}    model_author=${ab_model1_author_cli_ext}    model_title=${ab_model1_title_cli_ext}    model_description=${ab_model1_description_cli_ext}    model_version=${ab_model1_version_cli_ext}    model_registry_path=${ab_model1_registry_path_cli_ext}
    ...        AND    onboard model from external registry    model_id=${ab_model2_id_cli_ext}    model_author=${ab_model2_author_cli_ext}    model_title=${ab_model2_title_cli_ext}    model_description=${ab_model2_description_cli_ext}    model_version=${ab_model2_version_cli_ext}    model_registry_path=${ab_model2_registry_path_cli_ext}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${tc27_cli_ab_model_service_name}
    ...           AND    delete model    model_id=${ab_model1_id_cli_ext}    model_version=${ab_model1_version_cli_ext}
    ...           AND    delete model    model_id=${ab_model2_id_cli_ext}    model_version=${ab_model2_version_cli_ext}
    create model service via cli    manifest_file=${tc27_cli_ab_model_as}
    check if model service is running via cli    service_name=${tc27_cli_ab_model_service_name}    autoscaling=cpu:300m    instances=1-3    weights=0.5,0.5    model_a=${ab_model1_id_cli_ext}:${ab_model1_version_cli_ext}    model_b=${ab_model2_id_cli_ext}:${ab_model2_version_cli_ext}
    invoke the model service via api    service_name=${tc27_cli_ab_model_service_name}    input=${model_inception_input_file}
    modify model service via cli    service_name=${tc27_cli_ab_model_service_name}    manifest_file=${tc27_cli_modify_ab_model_ms}
    check if model service is running via cli    service_name=${tc27_cli_ab_model_service_name}    instances=1    model_a=${ab_model1_id_cli_ext}:${ab_model1_version_cli_ext}    model_b=${ab_model2_id_cli_ext}:${ab_model2_version_cli_ext}
    invoke the model service via api    service_name=${tc27_cli_ab_model_service_name}    input=${model_inception_input_file}
