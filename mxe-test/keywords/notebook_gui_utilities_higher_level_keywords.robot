*** Settings ***
Documentation    Higher level keywords for jupyterlab notebook use cases
Library    JupyterLibrary
Variables    variables/mxe_gui_dashboard_elements.py
Variables    variables/mxe_gui_notebooks_elements.py
Variables    variables/mxe_cluster_details.py

*** Keywords ***

Launch jupyterlab gui
    [Documentation]  To launch jupyterlab gui from mxe gui
    login to mxe gui   
    Execute Javascript    ${notebooks_menu_item}.click()
    Sleep    5    wait until the window is loaded with all the elements
    ${excludes}=    Get Window Handles
    Execute Javascript    ${mxe_user_jupyterlab_deployment}.click()
    Sleep    10    wait until the jupyterlab window is launched
    # Switch to Jupyter Lab window
    Switch Window    ${excludes}
    ${window_title}=    Get Title
    Run Keyword If    '${window_title}' == 'Exploration'    execute when window title is jupyterhub

execute when window title is jupyterhub
    [Documentation]    this keyword gets executed only for the first time launch of jupyter lab gui from mxe gui post maiden installation
    # Select the profile options for jupyterlab instance
    Select Frame     id:notebook
    Click Element    id:profile-item-minimal-profile
    Click Element    locator=xpath://*[@id="spawn_form"]/div[2]/input
    Sleep    180    wait until the jupyterlab pod starts
    Log Source
    Wait Until Page Contains Element    id=jp-main-dock-panel

installing pip packages in jupyterlab
    [Documentation]    Launch a new notebook and run pip install commands from internal and external pypi repository
    [Teardown]    Close All JupyterLab Tabs
    Launch a new JupyterLab Document    kernel=Python 3    category=Notebook    timeout=10s
    Add and Run JupyterLab Code Cell    pip install pandas    #from eric-mxe-pypiserver that is already installed
    Wait Until Page Contains    Requirement already satisfied: pandas
    Add and Run JupyterLab Code Cell    pip install pyparsing    #from eric-mxe-pypiserver
    Wait Until Page Contains    Successfully installed pyparsing-2.4.7
    Add and Run JupyterLab Code Cell    pip install python-keycloak    #from external pypi repo
    Wait Until Page Contains    Successfully built python-keycloak  

verify git in jupyterlab
    [Documentation]    Launch a new notebook and run pip install commands from internal and external pypi repository
    [Teardown]    Close All JupyterLab Tabs    
    Launch a new JupyterLab Document    kernel=Python 3 (ipykernel)    category=Notebook    timeout=30s
    Add and Run JupyterLab Code Cell    !git --version
    Capture Page Screenshot
    Wait Until Page Contains    git version 2.25.1

verify pyspark example in jupyterlab
    [Documentation]    Launch a new notebook and run a pyspark example
    [Teardown]    Close All JupyterLab Tabs
    Launch a new JupyterLab Document    kernel=Python 3 (ipykernel)    category=Notebook    timeout=30s
    Add and Run JupyterLab Code Cell    from pyspark.sql import SparkSession
    ...    spark = SparkSession.builder.getOrCreate()
    ...    from datetime import datetime, date
    ...    from pyspark.sql import Row
    ...    df = spark.createDataFrame([Row(a=1, b=2., c='string1', d=date(2000, 1, 1), e=datetime(2000, 1, 1, 12, 0)),Row(a=2, b=3., c='string2', d=date(2000, 2, 1), e=datetime(2000, 1, 2, 12, 0)),Row(a=4, b=5., c='string3', d=date(2000, 3, 1), e=datetime(2000, 1, 3, 12, 0))])
    ...    df
    Sleep    10
    Capture Page Screenshot
    Wait Until Page Contains    DataFrame[a: bigint, b: double, c: string, d: date, e: timestamp]

check jupyterlab version
    [Documentation]  open the 'About Jupyterlab' in the help menu 
    Open With JupyterLab Menu    Help    About JupyterLab

*** keywords ***

Login To MXE GUI
    [Documentation]  To open MXE GUI
    Open Browser    ${mxe_host}    headlesschrome  options=add_argument("--ignore-certificate-errors");add_argument("--no-sandbox")
    Set Browser Implicit Wait    5
    Input Text    id=username    ${mxe_username}
    Input Text    id=password    ${mxe_password}
    Click Button    //input[@value='Sign In']
    Sleep   20
