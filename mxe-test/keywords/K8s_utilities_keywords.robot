*** Settings ***
Library  KubeLibrary

*** Variables ***

${namespace}    mxe-microdegree

*** Keywords ***

Get the pods in mxe namepsace
    [Documentation]  List the pods in mxe namespace
    @{namespace_pods}=    Get Pod Names in Namespace    \    ${namespace}
    ${number_of_pods}=   Get Length    ${namespace_pods}
    Should Be Equal As Integers    ${number_of_pods}    41