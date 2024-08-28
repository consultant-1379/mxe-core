*** Settings ***

Library    SeleniumLibrary
Variables  variables/mxe_gui_model_services_elements.py

*** Keywords ***

click create model service button
    [Documentation]  To perform click operation in the 'create model service' button in the 'Model Services' window
    Execute Javascript    ${create_model_service_button}.click()
    Sleep    2    wait until the "Create model service" window is loaded

click upload manifest tab
    [Documentation]    To open the "Upload Manifest" tab in 'create model service' window
    Execute Javascript    ${upload_manifest_tab}.click()
    Sleep    2    wait until the "Upload Manifest" tab is loaded
    
upload model deployment manifest
    [Documentation]    To upload the model deployment manifest file available in the yaml/yml format
    [Arguments]    ${manifest_file}
    ${upload_manifest_file_browse_element}=    Execute Javascript    return ${upload_manifest_file_browse}
    Choose File    ${upload_manifest_file_browse_element}    ${manifest_file}
    Sleep    2    wait until the upload of manifest file is completed
    Execute Javascript    ${upload_manifest_ok_button}.click()

provide manual scaling inputs to create a single model service
    [Documentation]  To provide manual scaling inputs in 'create model services' window and create a single model service
    [Arguments]    ${model_id}    ${model_version}    ${service_name}    ${replicas}
    Execute Javascript    ${model_Service_name_text_box}.value="${service_name}";
    Execute Javascript    ${model_Service_name_text_box}.dispatchEvent(new InputEvent('input', { bubbles: true }))
    Execute Javascript    ${trigger_model_service_name_textbox_event}.dispatchEvent(new InputEvent('input', { bubbles: true }))
    Execute Javascript    ${select_model_id_dropdown}.shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item[label='${model_id}']").click()
    Execute Javascript    ${select_model_Version_dropdown}.shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item[label=' ${model_version}']").click()
    Execute Javascript    ${scaling_accordion}.click()
    Execute Javascript    ${manual_scaling_radio_button}.click()
    Execute Javascript    ${instances_input_box}.value=${replicas}

provide manual scaling inputs to create an ab model service
    [Documentation]  To provide manual scaling inputs in 'create model services' window and create an ab model service
    [Arguments]    ${modelA_id}    ${modelA_version}    ${modelB_id}    ${modelB_version}    ${service_name}    ${replicas}
    Execute Javascript    ${model_Service_name_text_box}.value="${service_name}";
    Execute Javascript    ${model_Service_name_text_box}.dispatchEvent(new InputEvent('input', { bubbles: true }))
    Execute Javascript    ${trigger_model_service_name_textbox_event}.dispatchEvent(new InputEvent('input', { bubbles: true }))
    Execute Javascript    ${select_model_id_dropdown}.shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item[label='${modelA_id}']").click()
    Execute Javascript    ${select_model_Version_dropdown}.shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item[label=' ${modelA_version}']").click()
    Execute Javascript    ${add_another_button}.click()
    Sleep    5    wait until the dropdown options are visible
    Execute Javascript    ${select_another_model_id_dropdown}.shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item[label='${modelB_id}']").click()
    Execute Javascript    ${select_another_model_version_dropdown}.shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item[label=' ${modelB_version}']").click()
    Execute Javascript    ${scaling_accordion}.click()
    Execute Javascript    ${manual_scaling_radio_button}.click()
    Execute Javascript    ${instances_input_box}.value=${replicas}

provide auto scaling inputs to create a single model service
    [Documentation]    To provide auto scaling inputs in 'create model services' window and create a single model service
    [Arguments]    ${metric_type}    ${model_id}    ${model_version}    ${service_name}    ${average_metric_value}    ${min_replicas}    ${max_replicas}
    Execute Javascript    ${model_Service_name_text_box}.value="${service_name}"
    Execute Javascript    ${model_Service_name_text_box}.dispatchEvent(new InputEvent('input', { bubbles: true }))
    Execute Javascript    ${trigger_model_service_name_textbox_event}.dispatchEvent(new InputEvent('input', { bubbles: true }))
    Execute Javascript    ${select_model_id_dropdown}.shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item[label='${model_id}']").click()
    Execute Javascript    ${select_model_Version_dropdown}.shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item[label=' ${model_version}']").click()
    Execute Javascript    ${scaling_accordion}.click()
    Execute Javascript    ${auto_scaling_radio_button}.click()
    Run Keyword If     ${metric_type} == 'cpu'    Execute Javascript    ${metric_type_dropdown}.shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item[label='CPU usage (millicores)']").click()
    ...  ELSE IF  ${metric_type} == 'memory'   Execute Javascript    ${metric_type_dropdown}.shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item[label='Memory usage (MegaBytes)']").click()
    Execute Javascript    ${metric_value_input_box}.value=${average_metric_value}
    Execute Javascript    ${metric_value_input_box}.dispatchEvent(new Event('change', { bubbles: true }))
    Execute Javascript    ${instances_min_input_box}.value=${min_replicas}
    Execute Javascript    ${instances_min_input_box}.dispatchEvent(new Event('change', { bubbles: true }))
    Execute Javascript    ${instances_max_input_box}.value=${max_replicas}
    Execute Javascript    ${instances_max_input_box}.dispatchEvent(new Event('change', { bubbles: true }))

provide auto scaling inputs to create an ab model service
    [Documentation]    To provide auto scaling inputs in 'create model services' window and create an ab model service
    [Arguments]    ${metric_type}    ${modelA_id}    ${modelA_version}    ${modelB_id}    ${modelB_version}    ${service_name}    ${average_metric_value}    ${min_replicas}    ${max_replicas}
    Execute Javascript    ${model_Service_name_text_box}.value="${service_name}"
    Execute Javascript    ${model_Service_name_text_box}.dispatchEvent(new InputEvent('input', { bubbles: true }))
    Execute Javascript    ${trigger_model_service_name_textbox_event}.dispatchEvent(new InputEvent('input', { bubbles: true }))
    Execute Javascript    ${select_model_id_dropdown}.shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item[label='${modelA_id}']").click()
    Execute Javascript    ${select_model_Version_dropdown}.shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item[label=' ${modelA_version}']").click()
    Execute Javascript    ${add_another_button}.click()
    Sleep    5    wait until the dropdown options are visible
    Execute Javascript    ${select_another_model_id_dropdown}.shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item[label='${modelB_id}']").click()
    Execute Javascript    ${select_another_model_version_dropdown}.shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item[label=' ${modelB_version}']").click()
    Execute Javascript    ${scaling_accordion}.click()
    Execute Javascript    ${auto_scaling_radio_button}.click()
    Run Keyword If     ${metric_type} == 'cpu'    Execute Javascript    ${metric_type_dropdown}.shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item[label='CPU usage (millicores)']").click()
    ...  ELSE IF  ${metric_type} == 'memory'   Execute Javascript    ${metric_type_dropdown}.shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item[label='Memory usage (MegaBytes)']").click()
    Execute Javascript    ${metric_value_input_box}.value=${average_metric_value}
    Execute Javascript    ${metric_value_input_box}.dispatchEvent(new Event('change', { bubbles: true }))
    Execute Javascript    ${instances_min_input_box}.value=${min_replicas}
    Execute Javascript    ${instances_min_input_box}.dispatchEvent(new Event('change', { bubbles: true }))
    Execute Javascript    ${instances_max_input_box}.value=${max_replicas}
    Execute Javascript    ${instances_max_input_box}.dispatchEvent(new Event('change', { bubbles: true }))

click create button
    [Documentation]  To perform click operation in the 'Create' button in the 'Create model service' window
    Execute Javascript    ${click_model_service_create_button}.click()

verify the created model service is running
    [Documentation]  To check the created model service is in running status
    [Arguments]    ${service_name}
    Sleep  65    wait for gui auto refresh (default: 60 seconds) so that the expected "service" is visible in "Model Services" window
    ${row_number_of_created_service}=    Execute Javascript    return Array.from(${model_services_table_list_service_name}).filter(e => e.innerText == "${service_name}")[0].parentNode.parentNode.rowIndex
    FOR    ${i}    IN RANGE    999999
        ${status_of_created_service}=    Execute Javascript    return ${model_services_table}.shadowRoot.querySelector("div > table > tbody > tr:nth-child(${row_number_of_created_service}) > td:nth-child(3) > div > span").innerText
        ${check_status_of_created_service_is_running}=    Evaluate    '${status_of_created_service}' == 'Running'
        Exit For Loop If    ${check_status_of_created_service_is_running}
    END
    Should Be True    ${check_status_of_created_service_is_running}

select the model service
    [Documentation]  To select the model service to be invoked or deleted
    [Arguments]    ${service_name}
    ${row_number_of_service_to_be_deleted}=    Execute Javascript    return Array.from(${model_services_table_list_service_name}).filter(e => e.innerText == "${service_name}")[0].parentNode.parentNode.rowIndex
    Execute Javascript    ${model_services_table}.shadowRoot.querySelector("div > table > tbody > tr:nth-child(${row_number_of_service_to_be_deleted})").click()
    Sleep    5    wait until the "Model Service Detail" page is visible

delete the model service
    [Documentation]  To delete the model service
    Execute Javascript    ${model_service_delete_button}.click()
    Sleep    2    wait until the delete button click operation is completed
    Execute Javascript    ${model_service_delete_confirmation_button}.click()
    Sleep    5    wait until the delete confirmation button click operation is completed

provide input to invoke the model service
    [Documentation]  To provide input to the text area of Invoke model service in Model Service Detail Page
    [Arguments]    ${invoke_model_input_data}
    Execute Javascript  ${invoke_model_service_tab}.click()
    Execute Javascript  ${invoke_model_service_text_area}.value='${invoke_model_input_data}';

invoke the model service
    [Documentation]  To invoke the model service with the given input
    Execute Javascript  ${invoke_model_service_button}.click()
    Sleep    5    wait until the results are available

verify the result of invoke model service
    [Documentation]  To verify the inovke model service with the given input data provided the correct result
    [Arguments]    ${search_element}
    Execute Javascript    ${search_text_box}.value='${search_element}'
    Execute Javascript    ${search_text_box}.dispatchEvent(new InputEvent('input', { bubbles: true, inputType: "insertText", data: "${search_element}" }))
    Sleep    2    wait until the searched element appears in the result
    ${check_the_result_status_in_search}=    Execute Javascript  return ${validate_the_result}
    ${result}=  Get Text    ${check_the_result_status_in_search}
    ${search_available_in_result}=  Evaluate    '${result}' == '1 result'
    Should Be True    ${search_available_in_result}

verify the model service is deleted
    [Documentation]  To check the model service is deleted
    [Arguments]    ${service_name}
    ${number_of_running_model_Services}=    Execute Javascript     return ${model_services_main_title_panel}.innerText
    Run Keyword If  '${number_of_running_model_Services}' == '0 services'   execute when there are zero number of running model services
    ...  ELSE IF  '${number_of_running_model_Services}' != '0 services'  execute when there are non-zero number of running model services    ${service_name}

execute when there are zero number of running model services
    Log     'Model service deleted successfully'

execute when there are non-zero number of running model services
    [Arguments]    ${service_name}
    FOR    ${i}    IN RANGE    200    #To wait until the desired model service is deleted
        sleep    500ms    wait for every for-loop because the status reads the intermediate loading state during the transition between x services to 0 services
        ${number_of_running_model_Services}=    Execute Javascript     return ${model_services_main_title_panel}.innerText
        ${check_model_service_is_deleted}=    Evaluate    '${number_of_running_model_Services}' == '0 services'  
        Exit For Loop If    ${check_model_service_is_deleted}   
        ${checking_model_service_presence}=    Execute Javascript    return Array.from(${model_services_table_list_service_name}).filter(e => e.innerText == "${service_name}").length
        ${check_model_service_is_deleted}=    Evaluate    ${checking_model_service_presence} == 0
        Exit For Loop If    ${check_model_service_is_deleted}
    END
    Should Be True    ${check_model_service_is_deleted}
       