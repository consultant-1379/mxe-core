*** Settings ***
Library    Browser
Variables    variables/mxe_cluster_details.py

*** Test Cases ***

Validate JupyterLab Instance Creation
    [Documentation]   Check if notebeook instance created
    [Tags]  notebook    test-dev    test-weekly    tc1
    Launch MxE GUI    url=${mxe_host}    username=${mxe_username}    password=${mxe_password}
    Access GUI application    group=Exploration    application=Notebooks
    Launch Jupyter Instance    username=${mxe_username}    profile=minimal
    Validate Jupyter Version    version=Version 3.6.5

*** Keywords ***

Launch MxE GUI
    [Documentation]    Launch MxE GUI
    ...                This keyword accepts the following arguments:
    ...                url                    :    MxE GUI url
    ...                username               :    Username to access the Jupyter lab
    ...                password               :    Password to access the Jupyter lab
    [Arguments]    ${url}    ${username}    ${password}
    New Browser
    New Context    ignoreHTTPSErrors=True
    New Page    ${url}
    Fill Text    text=Username    ${username}
    Fill Text    text=Password    ${password}
    Click    text=Sign In
    Wait For Elements State    text=Machine Learning Execution Environment

Access GUI application
    [Documentation]    Access the given GUI Application
    ...                This keyword accepts the following arguments:
    ...                group               :    Group in which application belongs
    ...                application         :    Application to be opened
    [Arguments]    ${group}    ${application}
    Click    id=AppBar-menu-toggle
    Click    text=${group}
    Click    text=${application}

Launch Jupyter Instance
    [Documentation]    To Launch Jupyter Instance for Logged in user
    ...                This keyword accepts the following arguments:
    ...                username               :    Username to access the Jupyter lab
    ...                profile                :    Server options to choose (minimal, medium and large)
    [Arguments]    ${username}    ${profile}
    Click    text=${username}'s JupyterLab
    Sleep   15s
    ${all_pages}=      Get Page Ids
    Switch Page    ${all_pages}[0]
    Set Selector Prefix    iframe#notebook >>>
    Wait For Elements State    text=Server Options
    Click    id=profile-item-${profile}-profile
    Click    text=Start
    Wait For Elements State    id=main >> text="Help"    timeout=60s

Validate Jupyter Version
    [Documentation]    Validate the JupyterLab version
    ...                This keyword accepts the following arguments:
    ...                version               :    Expected Jupyter Notebook version
    [Arguments]    ${version}
    Click    id=main >> text="Help"
    Click    id=jp-mainmenu-help >> text="About JupyterLab"
    Get Text    css=.jp-About-version    ==    ${version}