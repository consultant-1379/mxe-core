*** Settings ***

Library    SeleniumLibrary
Variables  variables/mxe_gui_dashboard_elements.py

*** Keywords ***

click dashboard menu
    [Documentation]    To perform click operation in the dashboard menu and wait until the menu items are visible
    ${dashboard_menu_element}=    Execute Javascript    return ${dashboard_menu}
    Click Element    ${dashboard_menu_element}
    Sleep      5    wait until the menu items are visible

click model packages menu
    [Documentation]    To perform click operation in the 'Model Packages' menu and wait until the window is loaded
    Execute Javascript   ${model_packages_menu_item}.click()
    Sleep   5    wait until the window is loaded with all the elements

click model services menu
    [Documentation]  To perform click operation in the 'Model Services' menu and wait until the window is loaded
    Execute Javascript    ${model_services_menu_item}.click()
    Sleep    5    wait until the window is loaded with all the elements

click training packages menu
    [Documentation]  To perform click operation in the 'Training packages' menu and wait until the window is loaded
    Execute Javascript    ${training_packages_menu_item}.click()
    Sleep    5    wait until the window is loaded with all the elements

click training jobs menu
    [Documentation]  To perform click operation in the 'Training jobs' menu and wait until the window is loaded
    Execute Javascript    ${training_jobs_menu_item}.click()
    Sleep    5    wait until the window is loaded with all the elements
    