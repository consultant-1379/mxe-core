*** Settings ***

#Library    SeleniumLibrary
Library    JupyterLibrary
Library    XvfbRobot
Library    yaml
Library    OperatingSystem
Library    Collections
Library    String
Library    libraries/MxeModelCliLibrary.py
Library    libraries/MxeModelServiceCliLibrary.py
Library    libraries/MxeKeycloakLibrary.py
Library    libraries/MxeKubernetesLibrary.py
Library    libraries/MxeFlowCliLibrary.py
Variables    variables/mxe_cluster_details.py
Variables    variables/model_onboarding_inputs.py
Variables    variables/model_service_inputs.py
Variables    variables/mxe_gui_dashboard_elements.py
Variables    variables/mxe_gui_notebooks_elements.py
Resource    keywords/mxe_gui_utilities_higher_level_keywords.robot
Resource    keywords/mxe_api_utilities_keywords.robot
Resource  keywords/argo_workflow_gui_utilities_higher_level_keywords.robot
Variables    variables/security/domain_based_access_control_inputs.py
Variables    variables/flow_deployment_inputs.py
# Suite Setup    Run Keywords     create test users in keycloak
# ...           AND    create test roles and map with users in keycloak
# Suite Teardown    Run Keywords    login with new user    username=${mxe_username}    password=${mxe_password}    
# ...           AND    delete roles in keycloak
# ...           AND    delete users in keycloak


*** Variables ***
${URL}=    https://mxe.olah015.rnd.gic.ericsson.se

*** Test Cases ***

Demo Test Case
    [Documentation]   Demo test case to Login and Logout of MXE GUI
    [Tags]  sample-test
    [Setup]
    [Teardown]
    login to mxe gui
    logout of mxe gui

sample command options
    [Documentation]    to test command options
    [tags]    print
    check model command options
    
*** keywords ***

Login To MXE GUI
    [Documentation]  To open MXE GUI
    # Start Virtual Display    1920    1080
    # Open Browser    ${URL}
    # Set Window Size    1920    1080
    Open Browser    ${URL}    headlessfirefox
    Set Browser Implicit Wait    5
    Input Text    id=username    ${mxe_username}
    Input Text    id=password    ${mxe_password}
    Click Button    //input[@value='Sign In']
    Sleep   20

Logout Of MXE GUI
    [Documentation]  To sign-out MXE GUI
    Execute Javascript    ${user_name_item}.click();
    Sleep   10
    Execute Javascript    ${sign_out_button}.click();
    Close Browser

update manifest with dynamic namespace
    [Documentation]    To update the namespace in the workflow manifest file
    [Arguments]    ${workflow_manifest_file}    ${namespace}    
    ${manifest_file}=  Get File  ${workflow_manifest_file}    # file should be in valid YAML format
    ${load_manifest}=  yaml.Safe Load  ${manifest_file}
    Set To Dictionary  ${load_manifest}[metadata]  namespace=${namespace}
    ${updated_manifest}=  yaml.Dump  ${load_manifest}
    log    ${updated_manifest}
    [Return]    ${updated_manifest}

delete data flow
    [Documentation]    Delete the onboarded data flow. 
    ...                This keyword to be used as a teardown in all the test cases in this suite.
    ...                Even if any test step fails as part of a test case, 
    ...                the onboarded data flow is removed to keep the test environment clean for next test case.
    [Arguments]    ${data_flow_name}
    delete data flow via cli     ${data_flow_name}
    check if onboarded data flow is deleted via cli    ${data_flow_name}

delete data flow deployment
    [Documentation]    Delete the data-flow deployment.  
    ...                This keyword to be used as a teardown in all the test cases in this suite.
    ...                Even if any test step fails as part of a test case, 
    ...                the flow deployment is removed to keep the test environment clean for next test case.
    [Arguments]    ${interface}    ${deployment_name}
    Run Keyword If    ${interface} == 'cli'    Run Keywords
    ...    delete data flow deployment via cli    ${deployment_name}
    ...    AND    check if data flow deployment is deleted via cli    ${deployment_name}
    ...    ELSE IF    ${interface} == 'gui'    Run Keywords
    ...    delete data flow deployment via gui    ${deployment_name}
    ...    AND    check if data flow deployment is deleted via gui    ${deployment_name}