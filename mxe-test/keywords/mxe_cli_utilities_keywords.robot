*** Settings ***

Documentation    Higher level keywords for mxe cli commands
Library    libraries/MxeModelCliLibrary.py
Library    libraries/MxeModelServiceCliLibrary.py

*** Keywords ***

onboard model from sourcecode
    [Documentation]    Onboard a model from source code and verify that it is available.
    ...                This keyword to be used as a setup in all the test cases in this suite.
    [Arguments]    ${source_code_path}    ${model_id}    ${model_version}
    onboard model from sourcecode via cli   ${source_code_path}
    check model available status via cli    ${model_id}    ${model_version}

onboard model from external registry
    [Documentation]    Onboard a model from external registry and verify that it is available.
    ...                This keyword to be used as a setup in all the test cases in this suite.
    [Arguments]    ${model_id}    ${model_author}    ${model_title}    ${model_description}    ${model_version}    ${model_registry_path}
    onboard model from external registry via cli   ${model_id}    ${model_author}    ${model_title}    ${model_description}    ${model_version}    ${model_registry_path}
    check model available status via cli    ${model_id}    ${model_version}

delete model
    [Documentation]    Delete the onboarded model and verify it is deleted. 
    ...                This keyword to be used as a teardown in all the test cases in this suite.
    ...                Even if any test step fails as part of a test case, 
    ...                the onboarded model is removed to keep the test environment clean for next test case.
    [Arguments]    ${model_id}    ${model_version}
    delete model via cli    ${model_id}    ${model_version}
    check if model is deleted via cli     ${model_id}    ${model_version}

delete model service
    [Documentation]    Delete the created model service and verify it is deleted. 
    ...                This keyword to be used as a teardown in all the test cases in this suite.
    ...                Even if any test step fails as part of a test case, 
    ...                the created/failed model service is removed to keep the test environment clean for next test case.
    [Arguments]    ${interface}    ${service_name}
    Run Keyword If    ${interface} == 'cli'    Run Keywords
    ...    delete model service via cli    ${service_name}
    ...    AND    check if model service is deleted via cli     ${service_name}
    ...    ELSE IF    ${interface} == 'gui'    Run Keywords
    ...    delete model service via gui    ${service_name}
    ...    AND    check if model service is deleted via gui    ${service_name}
