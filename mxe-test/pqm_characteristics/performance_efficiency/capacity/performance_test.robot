*** Settings ***
Documentation    Tests to verify the performance use cases of MXE
Library    Process
Library    OperatingSystem
Library    Collections
Library    String
Library    yaml
Library    libraries/MxeKeycloakLibrary.py
Library    libraries/MxeKubernetesLibrary.py
Library    keywords/MxeKubernetesKeyword.py
Resource    keywords/mxe_api_utilities_keywords.robot
Resource    keywords/prometheus_gui_utilities_higher_level_keywords.robot
Resource    keywords/mxe_cli_utilities_keywords.robot
Variables    variables/mxe_cluster_details.py
Variables    variables/performance_efficiency/capacity/performance_test_inputs.py
Suite Setup     Run Keywords    update access token lifespan in keycloak    lifespan=86400
...             AND    create cluster role    manifest_file=${perf_cluster_role_manifest}    namespace=${mxe_namespace}
...             AND    create cluster role binding    manifest_file=${perf_cluster_role_binding_manifest}    namespace=${mxe_namespace}
...             AND    patch prometheus configmap    mode='add'   configmap_name=${perf_configmap_name}    namespace=${mxe_namespace}   body=${perf_prometheus_yml_body}

Suite Teardown     Run Keywords    update access token lifespan in keycloak    lifespan=300
...             AND    delete cluster role    cluster_role_name=${perf_cluster_role_name}
...             AND    delete cluster role binding    cluster_role_binding_name=${perf_cluster_role_binding_name}
...             AND    patch prometheus configmap    mode='del'    configmap_name=${perf_configmap_name}    namespace=${mxe_namespace}  body=${perf_prometheus_yml_body}



*** Variables ***

*** Test Cases ***

performance test of a single model service with single replica
    [Documentation]    to test the performance of the single model service with single replica
    [tags]    tc1    performance    test-weekly
    [Setup]    onboard model from external registry    model_id=${perf_model_id}    model_author=${perf_model_author}    model_title=${perf_model_title}    model_description=${perf_model_description}    model_version=${perf_model_version}    model_registry_path=${perf_model_registry_path}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${perf_sample_service_name}
    ...           AND    delete model    model_id=${perf_model_id}    model_version=${perf_model_version}
    # creating a model service
    create model service via cli    manifest_file=${perf_sample_model_service}
    check if model service is running via cli    service_name=${perf_sample_service_name}
    invoke the model service via api    service_name=${perf_sample_service_name}    input=${model_inception_input_file}
    # running a performance test using load generator tool https://github.com/rakyll/hey
    # 20 requests per second for 10 mins
    run performance test    service_name=${perf_sample_service_name}    model_input=${model_inception_input_file}    number_of_concurrent_users=20    duration_of_test=600s
    sleep    60s    # wait for next run
    # 25 requests per second for 10 mins
    run performance test    service_name=${perf_sample_service_name}    model_input=${model_inception_input_file}    number_of_concurrent_users=25    duration_of_test=600s
    sleep    60s    # wait for next run
    # 30 requests per second for 10 mins
    run performance test    service_name=${perf_sample_service_name}    model_input=${model_inception_input_file}    number_of_concurrent_users=30    duration_of_test=600s
    sleep    60s    # wait before the metrics are collected
    # collect cpu and memory statistics of model container of model service from prometheus
    Run Keyword And Ignore Error    collect metrics    query=(rate (container_cpu_usage_seconds_total{namespace="${mxe_namespace}",pod=~"${perf_sample_service_name}.*",container="model"}[1m]))
    Run Keyword And Ignore Error    collect metrics    query=container_memory_working_set_bytes{namespace="${mxe_namespace}",pod=~"${perf_sample_service_name}.*",container="model"}

performance test of a single model service with two replicas
    [Documentation]    to test the performance of the single model service with two replicas
    [tags]    tc2    performance    test-weekly
    [Setup]    onboard model from external registry    model_id=${perf_model_id}    model_author=${perf_model_author}    model_title=${perf_model_title}    model_description=${perf_model_description}    model_version=${perf_model_version}    model_registry_path=${perf_model_registry_path}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${perf_sample_service_name}
    ...           AND    delete model    model_id=${perf_model_id}    model_version=${perf_model_version}
    # creating a model service
    ${generated_manifest}=    update manifest with dynamic replicas    model_deployment_manifest_file=${perf_sample_model_service}    number_of_replicas=${2}
    Create File    updated_sample_manifest_file.yaml    ${generated_manifest}
    create model service via cli    manifest_file=updated_sample_manifest_file.yaml
    check if model service is running via cli    service_name=${perf_sample_service_name}
    invoke the model service via api    service_name=${perf_sample_service_name}    input=${model_inception_input_file}
    # running a performance test using load generator tool https://github.com/rakyll/hey
    # 40 requests per second for 10 mins
    run performance test    service_name=${perf_sample_service_name}    model_input=${model_inception_input_file}    number_of_concurrent_users=40    duration_of_test=600s
    sleep    60s    # wait for next run
    # 50 requests per second for 10 mins
    run performance test    service_name=${perf_sample_service_name}    model_input=${model_inception_input_file}    number_of_concurrent_users=50    duration_of_test=600s
    sleep    60s    # wait for next run
    # 60 requests per second for 10 mins
    run performance test    service_name=${perf_sample_service_name}    model_input=${model_inception_input_file}    number_of_concurrent_users=60    duration_of_test=600s
    sleep    60s    # wait before the metrics are collected
    # collect cpu and memory statistics of model container of model service from prometheus
    Run Keyword And Ignore Error    collect metrics    query=(rate (container_cpu_usage_seconds_total{namespace="${mxe_namespace}",pod=~"${perf_sample_service_name}.*",container="model"}[1m]))
    Run Keyword And Ignore Error    collect metrics    query=container_memory_working_set_bytes{namespace="${mxe_namespace}",pod=~"${perf_sample_service_name}.*",container="model"}

performance test of a single model service with four replicas
    [Documentation]    to test the performance of the single model service with four replicas
    [tags]    tc3    performance    test-weekly
    [Setup]    onboard model from external registry    model_id=${perf_model_id}    model_author=${perf_model_author}    model_title=${perf_model_title}    model_description=${perf_model_description}    model_version=${perf_model_version}    model_registry_path=${perf_model_registry_path}
    [Teardown]    Run Keywords    delete model service    interface='cli'    service_name=${perf_sample_service_name}
    ...           AND    delete model    model_id=${perf_model_id}    model_version=${perf_model_version}
    # creating a model service
    ${generated_manifest}=    update manifest with dynamic replicas    model_deployment_manifest_file=${perf_sample_model_service}    number_of_replicas=${4}
    Create File    updated_sample_manifest_file.yaml    ${generated_manifest}
    create model service via cli    manifest_file=updated_sample_manifest_file.yaml
    check if model service is running via cli    service_name=${perf_sample_service_name}
    invoke the model service via api    service_name=${perf_sample_service_name}    input=${model_inception_input_file}
    # running a performance test using load generator tool https://github.com/rakyll/hey
    # 80 requests per second for 10 mins
    run performance test    service_name=${perf_sample_service_name}    model_input=${model_inception_input_file}    number_of_concurrent_users=80    duration_of_test=600s
    sleep    60s    # wait for next run
    # 100 requests per second for 10 mins
    run performance test    service_name=${perf_sample_service_name}    model_input=${model_inception_input_file}    number_of_concurrent_users=100    duration_of_test=600s
    sleep    60s    # wait for next run
    # 120 requests per second for 10 mins
    run performance test    service_name=${perf_sample_service_name}    model_input=${model_inception_input_file}    number_of_concurrent_users=120    duration_of_test=600s
    sleep    60s    # wait before the metrics are collected
    # collect cpu and memory statistics of model container of model service from prometheus
    Run Keyword And Ignore Error    collect metrics    query=(rate (container_cpu_usage_seconds_total{namespace="${mxe_namespace}",pod=~"${perf_sample_service_name}.*",container="model"}[1m]))
    Run Keyword And Ignore Error    collect metrics    query=container_memory_working_set_bytes{namespace="${mxe_namespace}",pod=~"${perf_sample_service_name}.*",container="model"}

*** keywords ***

run performance test
    [Documentation] 
    [Arguments]    ${service_name}    ${model_input}    ${number_of_concurrent_users}    ${duration_of_test}   
    ${access_token}=    Get Access Token
    ${result}=    Run Process    hey -c ${number_of_concurrent_users} -z ${duration_of_test} -m POST -H "Content-Type: application/json" -H "Authorization: Bearer ${access_token}" -D ${model_input} ${mxe_host}/model-endpoints/${service_name}    shell=True  
    Log    ${result.stdout}

update manifest with dynamic replicas
    [Documentation]    To update the relicas in the model deployment manifest file
    [Arguments]    ${model_deployment_manifest_file}    ${number_of_replicas}   
    ${manifest_file}=  Get File  ${model_deployment_manifest_file}    # file should be in valid YAML format
    ${load_manifest}=  yaml.Safe Load  ${manifest_file}
    Set To Dictionary  ${load_manifest}[spec][predictors][0]  replicas=${number_of_replicas}
    ${updated_manifest}=  yaml.Dump  ${load_manifest}
    log    ${updated_manifest}
    [Return]    ${updated_manifest}