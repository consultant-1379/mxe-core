*** Settings ***

Library    SeleniumLibrary
Library  libraries/MxeTrainingCliLibrary.py
Variables  variables/training_package_inputs.py
Variables  variables/mxe_gui_training_jobs_elements.py

*** Keywords ***

verify the training job is completed
    [Documentation]    To wait until the Training Job is in Completed status
    [Arguments]    ${training_package_id}    ${training_package_version}
    ${training_job_id}=    fetch the training job id via cli    ${training_package_id}    ${training_package_version}
    ${row_number_of_running_training_job}=    Execute Javascript    return Array.from(${training_jobs_table_list_job_id}).filter(e => e.innerText == "${training_job_id}")[0].parentNode.parentNode.rowIndex
    FOR    ${i}    IN RANGE    999999
        ${training_jobs_table_list_status}=    Execute Javascript    return ${training_jobs_table}.shadowRoot.querySelector("div > table > tbody > tr:nth-child(${row_number_of_running_training_job}) > td:nth-child(4) > div > span")
        ${check_status_of_running_job}=    Get Text    ${training_jobs_table_list_status}
        ${check_status_of_running_job_is_completed}=    Evaluate    '${check_status_of_running_job}' == 'Completed'
        Exit For Loop If    ${check_status_of_running_job_is_completed}
    END
    Should Be True    ${check_status_of_running_job_is_completed}

download the training results
    [Documentation]    To download the results of the training job
    [Arguments]    ${training_package_id}    ${training_package_version}
    ${training_job_id}=    fetch the training job id via cli    ${training_package_id}    ${training_package_version}
    ${row_number_of_completed_training_job}=    Execute Javascript    return Array.from(${training_jobs_table_list_job_id}).filter(e => e.innerText == "${training_job_id}")[0].parentNode.parentNode.rowIndex
    Execute Javascript    ${training_jobs_table}.shadowRoot.querySelector("div > table > tbody > tr:nth-child(${row_number_of_completed_training_job}) > td.checkbox > eui-base-v0-checkbox").shadowRoot.querySelector("label").click()
    Execute Javascript    ${training_jobs_download_results_button}.click()
    sleep    5    wait until the download results button click operation is completed

delete the training job
    [Documentation]  To identify and delete the training job based on the job id
    [Arguments]    ${training_package_id}    ${training_package_version}
    ${training_job_id}=    fetch the training job id via cli    ${training_package_id}    ${training_package_version}
    ${row_number_of_running_training_job}=    Execute Javascript    return Array.from(${training_jobs_table_list_job_id}).filter(e => e.innerText == "${training_job_id}")[0].parentNode.parentNode.rowIndex
    Execute Javascript    ${training_jobs_table}.shadowRoot.querySelector("div > table > tbody > tr:nth-child(${row_number_of_running_training_job}) > td:nth-child(7) > div > eui-base-v0-dropdown").shadowRoot.querySelector("div > eui-base-v0-menu > eui-base-v0-menu-item:nth-child(2)").click()
    Sleep    5    wait until the delete job click operation is completed

verify the training job is deleted
    [Documentation]  To check the training job is deleted
    [Arguments]    ${training_package_id}    ${training_package_version}
    ${number_of_running_training_jobs}=    Execute Javascript    return document.querySelector("body > eui-container").shadowRoot.querySelector("#container > eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector("#AppContent-inner > e-training-jobs").shadowRoot.querySelector("e-job-container").shadowRoot.querySelector("eui-layout-v0-multi-panel-tile").shadowRoot.querySelector("#main-panel-title").innerText
    Run Keyword If  '${number_of_running_training_jobs}' == '0 jobs'   execute when there are zero number of running training jobs
    ...  ELSE IF  '${number_of_running_training_jobs}' != '0 jobs'    execute when there are non-zero number of running training jobs    ${training_package_id}    ${training_package_version}

execute when there are zero number of running training jobs
    Log     'Training job deleted successfully'

execute when there are non-zero number of running training jobs
    [Arguments]    ${training_package_id}    ${training_package_version}
    ${training_job_id}=    fetch the training job id via cli    ${training_package_id}    ${training_package_version}
    ${checking_training_job_delete_status}=    Evaluate    '${training_job_id}' == 'None'
    Should Be True    ${checking_training_job_delete_status}
