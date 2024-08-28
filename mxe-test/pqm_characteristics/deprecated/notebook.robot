*** Settings ***
Documentation    Tests to verify the jupyterlab notebook basic use cases in MXE
Metadata    Version    0.1
Library    JupyterLibrary
Resource  keywords/notebook_gui_utilities_higher_level_keywords.robot

Suite Teardown    Run Keyword and Ignore Error    Close All Browsers
Test Teardown     Run Keyword and Ignore Error    Reset JupyterLab and Close

*** Variables ***

*** Test Cases ***

accessing jupyterlab from mxe gui
    [Documentation]  To open jupyter notebook gui from mxe gui and check basic operations
    [Tags]   notebook    test-dev    test-weekly    tc1
    Launch jupyterlab gui
    check jupyterlab version

jupyterlab support for installing pip packages
    [Documentation]  To open jupyter notebook and installing pip packages
    [Tags]   notebook    test-dev-beta    tc2
    Launch jupyterlab gui
    installing pip packages in jupyterlab

jupyterlab support for using git
    [Documentation]  To open jupyter notebook and check git support is available
    [Tags]   notebook    test-dev-beta    tc3
    Launch jupyterlab gui
    verify git in jupyterlab

jupyterlab support for using pyspark
    [Documentation]  To open jupyter notebook and run sample pyspark application
    [Tags]   notebook    test-dev-beta    tc4
    Launch jupyterlab gui
    verify pyspark example in jupyterlab
