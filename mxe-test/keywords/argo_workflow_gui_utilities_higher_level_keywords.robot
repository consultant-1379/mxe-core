*** Settings ***
Documentation    Higher level keywords for argo workflow use cases
Library    SeleniumLibrary
Library    XvfbRobot
Variables  variables/mxe_cluster_details.py

*** Keywords ***

login to argo workflow gui
    [Documentation]  To open argo workflow gui and login with valid credentials
    [Arguments]    ${url}    ${username}    ${password}    
    Open Browser    url=${url}/argo    browser=headlessfirefox
    Maximize Browser Window
    Set Browser Implicit Wait    5
    Input Text    id=username    ${username}
    Input Text    id=password    ${password}
    Click Button    //input[@value='Sign In']
    Wait Until Page Contains Element    xpath://*[@id="app"]/div/div[1]/div/div[2]/i

click workflows tab
    [Documentation]  To click the workflows tab and submit new workflow button in argo UI
    # Close 2 pop windows introduced in verion 3.3.8
    Click Element    xpath://*[@id="app"]/div/div[4]/div/span
    Click Element    xpath://*[@id="app"]/div/div[4]/div/span
    # Click "workflow" tab
    Click Element    xpath://*[@id="app"]/div/div[1]/div/div[2]/i
    # Wait until "SUBMIT NEW WORKFLOW" button appears in the workflow page
    Wait Until Page Contains Element    xpath://*[@id="app"]/div/div[3]/div[1]/div/div[2]/div[1]/div/button  
    Close Browser



