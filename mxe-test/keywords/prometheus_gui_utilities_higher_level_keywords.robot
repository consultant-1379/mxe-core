*** Settings ***
Documentation    Higher level keywords for fetching metrics from prometheus gui
Library    SeleniumLibrary
Library    XvfbRobot
Variables    variables/mxe_cluster_details.py
Variables    variables/prometheus_gui_elements.py

*** Keywords ***

login to prometheus gui
    [Documentation]  To open prometheus gui
    Open Browser    url=${mxe_host}/v1/prometheus/graph    browser=headlessfirefox
    Set Browser Implicit Wait    5
    Input Text    id=username    ${mxe_username}
    Input Text    id=password    ${mxe_password}
    Click Button    //input[@value='Sign In']
    Sleep   60s    #wait until the prometheus gui loads
    Capture Page Screenshot

collect metrics
    [Documentation]  To collect metrics from prometheus gui
    ...    PromQL(Prometheus Query Language) of a specific metrics to be passed as an input to this keyword
    [Arguments]    ${query}
    [Teardown]    Close Browser
    login to prometheus gui
    # provide the query in the command line
    Execute Javascript    ${command_line}.innerText='${query}'
    # click the Execute button
    Execute Javascript     ${execute_button}.click()
    # click the Graph tab
    Execute Javascript    ${graph_tab}.click()
    sleep    10s    # wait until the graph is displayed
    Capture Page Screenshot
