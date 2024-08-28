*** Settings ***
Documentation    Tests to verify the authenticity use cases of MXE
Metadata    Version    0.1
Library    SeleniumLibrary
Library    XvfbRobot
Variables  ../../Variables/mxe_cluster_details.py
Variables  ../../Variables/mxe_gui_dashboard_elements.py

*** Variables ***

*** Test Cases ***

Login MXE GUI with valid credentials
    [Documentation]  Testing the MXI GUI login with valid credentials
    [Tags]  mxe-gui    login    valid-login
    # Open Browser    ${mxe_host}    chrome
    Start Virtual Display    1920    1080
    Open Browser    ${mxe_host}
    Set Window Size    1920    1080
    Set Browser Implicit Wait    5
    Input Text    id=username    ${mxe_username}
    Input Text    id=password    ${mxe_password}
    Click Button    //input[@value='Sign in']
    Sleep   20
    Execute Javascript    ${user_name_item}.click();
    Sleep   10
    Execute Javascript    ${sign_out_button}.click();
    Close Browser

Login MXE GUI with invalid credentials
    [Documentation]  Testing the MXI GUI login with invalid credentials
    [Tags]  mxe-gui    login    invalid-login
    # Open Browser    ${mxe_host}    chrome
    Start Virtual Display    1920    1080
    Open Browser    ${mxe_host}
    Set Window Size    1920    1080
    Set Browser Implicit Wait    5
    Input Text    id=username    ${mxe_username}
    Input Text    id=password    ${invalid_mxe_password}
    Click Button    //input[@value='Sign in']
    sleep   2
    Element Should Be Visible    class=message-text
    Close Browser