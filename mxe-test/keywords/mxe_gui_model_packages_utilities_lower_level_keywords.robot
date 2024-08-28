*** Settings ***

Library    SeleniumLibrary
Variables  variables/mxe_gui_model_packages_elements.py

*** Keywords ***

click onboard model button
    [Documentation]    To perform click operation in the 'Onboard Model' button in the 'Model Packages' window and wait until the 'Upload Model' window is loaded
    Execute Javascript    ${onboard_model_button}.click()
    Sleep   3    wait until the 'Upload Model' window is loaded    

upload model source code file
    [Documentation]    To upload the model source code file available in the zip format
    [Arguments]    ${source_code_file}
    ${upload_model_sourcecode_element}=    Execute Javascript    return ${upload_model_sourcecode}
    Choose File    ${upload_model_sourcecode_element}    ${source_code_file}

wait until the model source code file is uploaded
    [Documentation]    To wait until the 'Ok' button is enabled post upload of model source code file and click it to close the 'Upload Model' window
    ${ok_button_status_in_upload_model_element}=    Execute Javascript    return document.querySelector("body > eui-container").shadowRoot.querySelector("#container > eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector("#AppContent-inner > e-model-catalogue").shadowRoot.querySelector("e-model-container").shadowRoot.querySelector("eui-base-v0-dialog.upload-dialog > e-upload-component").shadowRoot.querySelector("#footer > div.button-container > eui-base-v0-button.onboard-btn.button").shadowRoot.querySelector("button")
    Wait Until Element Is Enabled    ${ok_button_status_in_upload_model_element}    timeout=600
    Sleep    5    wait until the ok button in 'Upload Model' window is enabled
    ${ok_button_in_upload_model}=    Execute Javascript    return document.querySelector("body > eui-container").shadowRoot.querySelector("#container > eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector("#AppContent-inner > e-model-catalogue").shadowRoot.querySelector("e-model-container").shadowRoot.querySelector("eui-base-v0-dialog.upload-dialog > e-upload-component").shadowRoot.querySelector("#footer > div.button-container > eui-base-v0-button.onboard-btn.button")
    Click Button    ${ok_button_in_upload_model}

change the view of model packages window
    [Documentation]    To change the view of 'Model Packages' window from card to table view. Default: card view
    Execute Javascript    ${view_change_button_in_model_packages}.click()

verify the onboarded model becomes available
    [Documentation]    To change the view of 'Model Packages' window from card to table view and wait until the model packaging is completed and it is in available status
    [Arguments]    ${model_id}    ${model_version}
    # Check the view type [card (element name: view-list) or table (element name: view-tiles)] of model packages window.
    ${model_packages_window_view_type}=    Execute Javascript   return ${model_packages_window_view_type_element}
    # Change the view only if the model packages window is in card view (element name: view-list)
    Run Keyword If    '${model_packages_window_view_type}' == 'view-list'    change the view of model packages window
    Sleep  60    wait for gui auto refresh (default: 60 seconds) so that the expected model name in the "Model id" column is visible
    ${row_number_of_onboarded_model}=    Execute Javascript    return Array.from(${model_packages_table_list_model_id}).filter(e => e.innerText == "${model_id}")[0].parentNode.parentNode.rowIndex
    FOR    ${i}    IN RANGE    900    # Retry for max. 15 mins until the model becomes available
        Sleep    1s
        ${model_packages_table_list_status}=    Execute Javascript    return ${model_packages_table}.shadowRoot.querySelector("div > table > tbody > tr:nth-child(${row_number_of_onboarded_model}) > td:nth-child(4) > div > span")
        ${check_status_of_onboarding}=    Get Text    ${model_packages_table_list_status}
        ${check_status_of_onboarding_is_available}=    Evaluate    '${check_status_of_onboarding}' == 'Available'
        Exit For Loop If    ${check_status_of_onboarding_is_available}
    END
    Should Be True    ${check_status_of_onboarding_is_available}
    verify the model version    ${model_id}    ${model_version}

verify the model version
    [Documentation]    To verify the model version
    [Arguments]    ${model_id}    ${model_version}
    ${row_number_of_onboarded_model}=    Execute Javascript    return Array.from(${model_packages_table_list_model_id}).filter(e => e.innerText == "${model_id}")[0].parentNode.parentNode.rowIndex
    Execute Javascript    ${model_packages_table}.shadowRoot.querySelector("div > table > tbody > tr:nth-child(${row_number_of_onboarded_model})").click()
    Sleep    5    wait until the model version card pane elements are visible
    Execute Javascript    ${model_versions_pane_search_box}.value='${model_version}'
    Execute Javascript    ${model_versions_pane_search_box}.dispatchEvent(new InputEvent('input', { bubbles: true, inputType: "insertText", data: "${model_version}" }))
    Sleep   2    wait until the searched model version is visible 
    ${model_version_element}=    Execute Javascript  return ${model_version_card}
    ${actual_model_version}=    Get Text    ${model_version_element}
    ${validate_expected_model_version}=  Evaluate    '${actual_model_version}' == 'Version ${model_version}'
    Should Be True    ${validate_expected_model_version}    

select the model to be deleted
    [Documentation]  To identify and click the model to be deleted based on the model id
    [Arguments]    ${model_id}    ${model_version}
    # Check the view type [card (element name: view-list) or table (element name: view-tiles)] of model packages window.
    ${model_packages_window_view_type}=    Execute Javascript   return ${model_packages_window_view_type_element}
    # Change the view only if the model packages window is in card view (element name: view-list)
    Run Keyword If    '${model_packages_window_view_type}' == 'view-list'    change the view of model packages window
    Sleep   2    wait until the elements in the table are visible
    verify the model version    ${model_id}    ${model_version}

delete the model
    [Documentation]  To delete the selected model
    Execute Javascript    ${model_packages_delete_button}.click()
    Sleep    2    wait until the delete button click operation is completed
    Execute Javascript    ${model_packages_delete_confirmation_button}.click()
    Sleep    5    wait until the delete confirmation button click operation is completed

verify the model is deleted
    [Documentation]  To check the model is deleted
    [Arguments]    ${model_id}    ${model_version}
    ${number_of_onboarded_models}=    Execute Javascript    return document.querySelector("body > eui-container").shadowRoot.querySelector("#container > eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector("#AppContent-inner > e-model-catalogue").shadowRoot.querySelector("e-model-container").shadowRoot.querySelector("eui-layout-v0-multi-panel-tile").shadowRoot.querySelector("#main-panel-title").innerText
    Run Keyword If  '${number_of_onboarded_models}' == '0 models in the list'   execute when there are zero number of onboarded models
    ...  ELSE IF  '${number_of_onboarded_models}' != '0 models in the list'    execute when there are non-zero number of onboarded models    ${model_id}    ${model_version}

execute when there are zero number of onboarded models
    Log     'model deleted successfully'

execute when there are non-zero number of onboarded models
    [Arguments]    ${model_id}    ${model_version}
    # Check the view type [card (element name: view-list) or table (element name: view-tiles)] of model packages window.
    ${model_packages_window_view_type}=    Execute Javascript   return ${model_packages_window_view_type_element}
    # Change the view only if the model packages window is in card view (element name: view-list)
    Run Keyword If    '${model_packages_window_view_type}' == 'view-list'    change the view of model packages window
    ${check_desired_model}=    Execute Javascript    return Array.from(${model_packages_table_list_model_id}).filter(e => e.innerText == "${model_id}").length
    Run Keyword If    ${check_desired_model} == 0    Log     'model deleted successfully'
    ...  ELSE IF  ${check_desired_model} != 0    verify the deleted model version    ${model_id}    ${model_version}

verify the deleted model version
    [Documentation]    To verify the model version
    [Arguments]    ${model_id}    ${model_version}
    ${row_number_of_onboarded_model}=    Execute Javascript    return Array.from(${model_packages_table_list_model_id}).filter(e => e.innerText == "${model_id}")[0].parentNode.parentNode.rowIndex
    Execute Javascript    ${model_packages_table}.shadowRoot.querySelector("div > table > tbody > tr:nth-child(${row_number_of_onboarded_model})").click()
    Sleep    5    wait until the model version card pane elements are visible
    Execute Javascript    ${model_versions_pane_search_box}.value='${model_version}'
    Execute Javascript    ${model_versions_pane_search_box}.dispatchEvent(new InputEvent('input', { bubbles: true, inputType: "insertText", data: "${model_version}" }))
    Sleep   2    wait until the searched model version is visible 
    ${model_version_element}=    Execute Javascript  return ${model_version_card}
    ${validate_expected_model_version}=  Evaluate    '${model_version_element}' == 'None'
    Should Be True    ${validate_expected_model_version}
