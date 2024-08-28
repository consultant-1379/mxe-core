*** Settings ***

Library    SeleniumLibrary
Variables  variables/training_package_inputs.py
Variables  variables/mxe_gui_training_packages_elements.py

*** Keywords ***

click onboard package button
    [Documentation]    To perform click operation in the 'Onboard package' button in the 'Training Packages' window and wait until the 'Upload Model' window is loaded
    Execute Javascript    ${onboard_package_button}.click()
    Sleep   3    wait until the 'Upload Training Package' window is loaded 

upload training package source code file
    [Documentation]    To upload the training package source code file available in the zip format
    [Arguments]    ${training_source_code_file}
    ${upload_training_package_sourcecode_element}=    Execute Javascript    return ${upload_training_package_sourcecode}
    Choose File    ${upload_training_package_sourcecode_element}    ${training_source_code_file}

wait until the training package source code file is uploaded
    [Documentation]    To wait until the 'Ok' button is enabled post upload of training package source code file and click it to close the 'Upload Training Package' window
    ${ok_button_status_in_upload_training_package_element}=    Execute Javascript    return document.querySelector("body > eui-container").shadowRoot.querySelector("#container > eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector("#AppContent-inner > e-training-packages").shadowRoot.querySelector("e-package-container").shadowRoot.querySelector("eui-base-v0-dialog > e-upload-component").shadowRoot.querySelector("#footer > div.button-container > eui-base-v0-button.onboard-btn.button").shadowRoot.querySelector("button")
    Wait Until Element Is Enabled    ${ok_button_status_in_upload_training_package_element}    timeout=600
    Sleep    5    wait until the ok button in 'Upload Training Package' window is enabled
    ${ok_button_in_upload_training_package}=    Execute Javascript    return document.querySelector("body > eui-container").shadowRoot.querySelector("#container > eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector("#AppContent-inner > e-training-packages").shadowRoot.querySelector("e-package-container").shadowRoot.querySelector("eui-base-v0-dialog > e-upload-component").shadowRoot.querySelector("#footer > div.button-container > eui-base-v0-button.onboard-btn.button")
    Click Button    ${ok_button_in_upload_training_package}

change the view of training packages window
    [Documentation]    To change the view of 'Training Packages' window from card to table view. Default: card view
    Execute Javascript    ${view_change_button_in_training_packages}.click()

verify the onboarded training package becomes available
    [Documentation]    To change the view of 'Training Packages' window from card to table view and wait until the Training packaging is completed and it is in available status
    [Arguments]    ${training_package_id}    ${training_package_version}
    # Check the view type [card (element name: view-list) or table (element name: view-tiles)] of training packages window.
    ${training_packages_window_view_type}=    Execute Javascript   return ${training_packages_window_view_type_element}
    # Change the view only if the training packages window is in card view (element name: view-list)
    Run Keyword If    '${training_packages_window_view_type}' == 'view-list'    change the view of training packages window
    Sleep  60    wait for gui auto refresh (default: 60 seconds) so that the expected package id in the "Package id" column is visible
    ${row_number_of_onboarded_training_package}=    Execute Javascript    return Array.from(${training_packages_table_list_package_id}).filter(e => e.innerText == "${training_package_id}")[0].parentNode.parentNode.rowIndex
    FOR    ${i}    IN RANGE    600    # Retry for max. 10 mins until the training package becomes available
        Sleep    1s
        ${training_packages_table_list_status}=    Execute Javascript    return ${training_packages_table}.shadowRoot.querySelector("div > table > tbody > tr:nth-child(${row_number_of_onboarded_training_package}) > td:nth-child(4) > div > span")
        ${check_status_of_onboarding}=    Get Text    ${training_packages_table_list_status}
        ${check_status_of_onboarding_is_available}=    Evaluate    '${check_status_of_onboarding}' == 'Available'
        Exit For Loop If    ${check_status_of_onboarding_is_available}
    END
    Should Be True    ${check_status_of_onboarding_is_available}
    verify the training package version    ${training_package_id}   ${training_package_version}

verify the training package version
    [Documentation]    To verify the training package version
    [Arguments]    ${training_package_id}    ${training_package_version}
    ${row_number_of_onboarded_training_package}=    Execute Javascript    return Array.from(${training_packages_table_list_package_id}).filter(e => e.innerText == "${training_package_id}")[0].parentNode.parentNode.rowIndex
    Execute Javascript    ${training_packages_table}.shadowRoot.querySelector("div > table > tbody > tr:nth-child(${row_number_of_onboarded_training_package})").click()
    Sleep    5    wait until the training package version card pane elements are visible
    Execute Javascript    ${training_package_versions_pane_search_box}.value='${training_package_version}'
    Execute Javascript    ${training_package_versions_pane_search_box}.dispatchEvent(new InputEvent('input', { bubbles: true, inputType: "insertText", data: "${training_package_version}" }))
    Sleep   2    wait until the searched training package version is visible
    ${training_package_version_element}=    Execute Javascript  return ${training_package_version_card}
    ${actual_training_package_version}=    Get Text    ${training_package_version_element}
    ${validate_expected_training_package_version}=  Evaluate    '${actual_training_package_version}' == 'Version ${training_package_version}'
    Should Be True    ${validate_expected_training_package_version}

select the training package to be deleted
    [Documentation]  To identify and click the training package to be deleted based on the package id
    [Arguments]    ${training_package_id}    ${training_package_version}
    # Check the view type [card (element name: view-list) or table (element name: view-tiles)] of training packages window.
    ${training_packages_window_view_type}=    Execute Javascript   return ${training_packages_window_view_type_element}
    # Change the view only if the training packages window is in card view (element name: view-list)
    Run Keyword If    '${training_packages_window_view_type}' == 'view-list'    change the view of training packages window
    Sleep   2    wait until the elements in the table are visible
    verify the training package version    ${training_package_id}   ${training_package_version}

delete the training package
    [Documentation]  To delete the selected training package
    Execute Javascript    ${training_packages_delete_button}.click()
    Sleep    2    wait until the delete button click operation is completed

start the training job
    [Documentation]  To start the training job from the onboarded training package
    [Arguments]    ${training_package_id}    ${training_package_version}
    # Validate and select the required training package version to be started
    verify the training package version    ${training_package_id}   ${training_package_version}
    Execute Javascript    ${training_packages_execute_button}.click()
    Sleep    2    wait until the execute button click operation is completed

verify the training package is deleted
    [Documentation]  To check the training package is deleted
    [Arguments]    ${training_package_id}    ${training_package_version}
    ${number_of_onboarded_training_packages}=    Execute Javascript    return document.querySelector("body > eui-container").shadowRoot.querySelector("#container > eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector("#AppContent-inner > e-training-packages").shadowRoot.querySelector("e-package-container").shadowRoot.querySelector("eui-layout-v0-multi-panel-tile").shadowRoot.querySelector("#main-panel-title").innerText
    Run Keyword If  '${number_of_onboarded_training_packages}' == '0 packages'   execute when there are zero number of onboarded training packages
    ...  ELSE IF  '${number_of_onboarded_training_packages}' != '0 packages'    execute when there are non-zero number of onboarded training packages    ${training_package_id}    ${training_package_version}

execute when there are zero number of onboarded training packages
    Log     'Training package deleted successfully'

execute when there are non-zero number of onboarded training packages
    [Arguments]    ${training_package_id}    ${training_package_version}
    # Check the view type [card (element name: view-list) or table (element name: view-tiles)] of training packages window.
    ${training_packages_window_view_type}=    Execute Javascript   return ${training_packages_window_view_type_element}
    # Change the view only if the training packages window is in card view (element name: view-list)
    Run Keyword If    '${training_packages_window_view_type}' == 'view-list'    change the view of training packages window
    ${check_desired_training_package}=    Execute Javascript    return Array.from(${training_packages_table_list_package_id}).filter(e => e.innerText == "${training_package_id}").length
    Run Keyword If    ${check_desired_training_package} == 0    Log     'Training package deleted successfully'
    ...  ELSE IF  ${check_desired_training_package} != 0    verify the deleted training package version    ${training_package_id}   ${training_package_version}

verify the deleted training package version
    [Documentation]    To verify the training package version
    [Arguments]    ${training_package_id}    ${training_package_version}
    ${row_number_of_onboarded_training_package}=    Execute Javascript    return Array.from(${training_packages_table_list_package_id}).filter(e => e.innerText == "${training_package_id}")[0].parentNode.parentNode.rowIndex
    Execute Javascript    ${training_packages_table}.shadowRoot.querySelector("div > table > tbody > tr:nth-child(${row_number_of_onboarded_training_package})").click()
    Sleep    5    wait until the training package version card pane elements are visible
    Execute Javascript    ${training_package_versions_pane_search_box}.value='${training_package_version}'
    Execute Javascript    ${training_package_versions_pane_search_box}.dispatchEvent(new InputEvent('input', { bubbles: true, inputType: "insertText", data: "${training_package_version}" }))
    Sleep   2    wait until the searched training package version is visible
    ${training_package_version_element}=    Execute Javascript  return ${training_package_version_card}
    ${validate_expected_training_package_version}=  Evaluate    '${training_package_version_element}' == 'None'
    Should Be True    ${validate_expected_training_package_version}
