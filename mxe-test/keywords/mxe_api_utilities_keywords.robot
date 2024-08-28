*** Settings ***

Library    RequestsLibrary
Library    Collections
Library    JSONLibrary
Library    OperatingSystem
Library    XML
Library  libraries/MxeModelCliLibrary.py
Library  libraries/MxeModelServiceCliLibrary.py
Variables  variables/mxe_api_endpoints.py
Variables  variables/model_onboarding_inputs.py
Variables  variables/model_service_inputs.py
Variables  variables/mxe_cluster_details.py
Variables  variables/training_package_inputs.py

*** Keywords ***

Get Access Token
    [Documentation]  To generate access token
    [Arguments]    ${username}=${mxe_username}    ${password}=${mxe_password}
    ${headers}=    Create Dictionary    Content-Type=application/x-www-form-urlencoded    Accept=application/json
    Create Session    mxesession    ${mxe_host}    headers=${headers}    verify=true
    ${data}=    Create Dictionary    username=${username}    password=${password}    grant_type=password    client_id=mxe-rest-client    scope=offline_access
    ${kwargs}=    Create Dictionary    data=${data}
    ${response}=   POST On Session    mxesession    ${keycloak_token_endpoint}    expected_status=200    &{kwargs}
    ${token}=    evaluate    $response.json().get("access_token")
    [Return]    ${token}

Get Refresh Access Token
    [Documentation]  To generate refresh token
    Create Session    mxesession    ${mxe_host}    verify=true
    ${data}=    Create Dictionary    grant_type=refresh_token    client_id=mxe-rest-client    scope=offline_access    refresh_token=${refresh_token}
    ${header}=    Create Dictionary    Content-Type=application/x-www-form-urlencoded
    ${response}=   Post Request    mxesession    ${keycloak_token_endpoint}    data=${data}    headers=${header}
    Should Be Equal As Strings    ${response.status_code}    200
    ${token}=    evaluate    $response.json().get("access_token")
    ${accesstoken}=    Catenate    Bearer    ${token}

invoke the model service via api
    [Documentation]  To invoke a single model service
    [Arguments]    ${service_name}    ${input}
    ${access_token}=    Get Access Token
    ${bearer_token}=    Catenate    Bearer    ${access_token}
    ${headers}=    Create Dictionary    Content-Type=application/json    Authorization=${bearer_token}
    Create Session    mxesession    ${mxe_host}    headers=${headers}    verify=true
    ${body}=    Load JSON From File    ${input}
    ${kwargs}=    Create Dictionary    json=${body}
    Wait Until Keyword Succeeds   5x    30s    POST On Session    mxesession    /model-endpoints/${service_name}    expected_status=200    &{kwargs}

invoke the java model service via api
    [Documentation]  To invoke a single java model service
    [Arguments]    ${service_name}    ${input}
    ${access_token}=    Get Access Token
    ${bearer_token}=    Catenate    Bearer    ${access_token}
    ${headers}=    Create Dictionary    Content-Type=application/x-www-form-urlencoded    Authorization=${bearer_token}
    Create Session    mxesession    ${mxe_host}    headers=${headers}    verify=true
    ${body}=    Load JSON From File    ${input}
    ${json}=    Catenate   json=${body}
    ${kwargs}=    Create Dictionary    data=${json}
    Wait Until Keyword Succeeds   5x    30s    POST On Session    mxesession    /model-endpoints/${service_name}    expected_status=200    &{kwargs}

list the onboarded models via api
    [Documentation]  To list the onboarded models
    [Arguments]    ${username}    ${password}    
    ${access_token}=    Get Access Token    ${username}    ${password} 
    ${bearer_token}=    Catenate    Bearer    ${access_token}
    ${headers}=    Create Dictionary    Content-Type=application/json    Authorization=${bearer_token}
    Create Session    mxesession    ${mxe_host}    headers=${headers}    verify=true
    ${response}=    Wait Until Keyword Succeeds   5x    30s    GET On Session    mxesession    ${model_endpoint}    expected_status=403   

list the onboarded training packages via api
    [Documentation]  To list the onboarded training packages
    [Arguments]    ${username}    ${password}    
    ${access_token}=    Get Access Token    ${username}    ${password} 
    ${bearer_token}=    Catenate    Bearer    ${access_token}
    ${headers}=    Create Dictionary    Content-Type=application/json    Authorization=${bearer_token}
    Create Session    mxesession    ${mxe_host}    headers=${headers}    verify=true
    ${response}=    Wait Until Keyword Succeeds   5x    30s    GET On Session    mxesession    ${training_packages_endpoint}    expected_status=200   

invoke prometheus with a query
    [Documentation]  Executes a simple promql query and returns metrics as a result
    [Arguments]    ${service_name}   
    ${access_token}=    Get Access Token
    ${bearer_token}=    Catenate    Bearer    ${access_token}
    ${headers}=    Create Dictionary    Authorization=${bearer_token}
    Create Session    mxesession    ${mxe_host}    headers=${headers}    verify=true
    ${form_data}=  Evaluate    {'query': (None, 'sum(seldon_api_executor_client_requests_seconds_count{deployment_name="${service_name}"})[10s:10s]')}
    ${response}=    POST On Session    mxesession    /v1/prometheus/api/v1/query    expected_status=200    files=${form_data}
    ${json}=    evaluate   json.loads(r"""${response.content}""")    json
    ${value}=    Get Value From Json    ${json}    $.data.result[0].values[0][1]
    Should Not Be Empty    ${value}