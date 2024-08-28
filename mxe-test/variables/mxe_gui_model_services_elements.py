# This files defines the JS (Java Script) path of GUI elements in MODEL SERVICES MENU page.
# ------MODEL SERVICE MENU PAGE ITEMS------

# JS path of 'Create model service' button in 'Model Services' window
create_model_service_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                              'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                              'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                              'e-model-services").shadowRoot.querySelector(' \
                              '"e-services-container").shadowRoot.querySelector("eui-layout-v0-multi-panel-tile > div ' \
                              '> eui-base-v0-button") '

# JS path of "Parameters" tab in the 'Create Model Service' window
parameters_tab =  'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                  'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                  'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                  'e-model-services").shadowRoot.querySelector(' \
                  '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                  '"eui-layout-v0-tabs > eui-layout-v0-tab:nth-child(1) > label")'

# JS path of 'Model service name' input box in the 'Create Model Service' window.
model_Service_name_text_box = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                              'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                              'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                              'e-model-services").shadowRoot.querySelector(' \
                              '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                              '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector("div > ' \
                              'eui-base-v0-accordion:nth-child(1) > div:nth-child(1) > ' \
                              'eui-base-v0-text-field").shadowRoot.querySelector("#item") '

# Below event is required to enable 'create' button in the 'Create Model Service' window. This is because after
# providing 'Model Service Name' in the input box, an event is required to enable the 'create' button.
trigger_model_service_name_textbox_event = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                                           '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                                           '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                                           '"#AppContent-inner > e-model-services").shadowRoot.querySelector(' \
                                           '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                                           '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector("div > ' \
                                           'eui-base-v0-accordion:nth-child(1) > div:nth-child(1) > ' \
                                           'eui-base-v0-text-field") '

# JS path of "Select model id" dropdown in the 'Create Model Service' window.
select_model_id_dropdown = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                           'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                           'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                           'e-model-services").shadowRoot.querySelector(' \
                           '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                           '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector(' \
                           '"#modelSelectorContainer").shadowRoot.querySelector(' \
                           '"#firstModel").shadowRoot.querySelector("#id-selector")'

# JS path of "Select model version" dropdown in the 'Create Model Service' window.
select_model_Version_dropdown = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                           'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                           'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                           'e-model-services").shadowRoot.querySelector(' \
                           '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                           '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector(' \
                           '"#modelSelectorContainer").shadowRoot.querySelector(' \
                           '"#firstModel").shadowRoot.querySelector("#version-selector")'

# JS path of "+ Add another" button in the 'Create Model Service' window
add_another_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                     '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                     '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                     '"#AppContent-inner > e-model-services").shadowRoot.querySelector(' \
                     '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                     '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector(' \
                     '"#modelSelectorContainer").shadowRoot.querySelector("eui-base-v0-button")'

# JS path of "Select model id" dropdown after clicking "+ Add another" button in the 'Create Model Service' window.
select_another_model_id_dropdown = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                                   'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                                   'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                                   'e-model-services").shadowRoot.querySelector(' \
                                   '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                                   '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector(' \
                                   '"#modelSelectorContainer").shadowRoot.querySelector(' \
                                   '"e-model-selector:nth-child(3)").shadowRoot.querySelector("#id-selector")'

# JS path of "Select model version" dropdown after clicking "+ Add another" button in the 'Create Model Service' window.
select_another_model_version_dropdown = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                                        'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                                        'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                                        'e-model-services").shadowRoot.querySelector(' \
                                        '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                                        '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector(' \
                                        '"#modelSelectorContainer").shadowRoot.querySelector(' \
                                        '"e-model-selector:nth-child(3)").shadowRoot.querySelector("#version-selector")'

# JS path of "select_another_model_weight" after clicking "+ Add another" button in the 'Create Model Service' window.
select_another_model_weight = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                              'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                              'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                              'e-model-services").shadowRoot.querySelector(' \
                              '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                              '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector(' \
                              '"#modelSelectorContainer").shadowRoot.querySelector(' \
                              '"e-model-selector:nth-child(3)").shadowRoot.querySelector("#weight")'

# JS path of "Scaling" accordion in the 'Create Model Service' window
scaling_accordion = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                    'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                    'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                    'e-model-services").shadowRoot.querySelector(' \
                    '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                    '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector(' \
                    '"#scaling-accordion").shadowRoot.querySelector("ul > li > div.accordion__item__title > eui-base-v0-icon") '

# JS path of "Manual Scaling" radio button inside "Scaling" accordion in the 'Create Model Service' window
manual_scaling_radio_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                            'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                            'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                            'e-model-services").shadowRoot.querySelector(' \
                            '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                            '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector(' \
                            '"#scaling-accordion > e-service-scaling").shadowRoot.querySelector(' \
                            '"div > div.radio-buttons > eui-base-v0-radio-button.manual.spacing-bottom") '                          

# JS path of "Instances" insice "scaling" accordion in the "Create Model Service" window
instances_input_box = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                      'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                      'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                      'e-model-services").shadowRoot.querySelector(' \
                      '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                      '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector(' \
                      '"#scaling-accordion > e-service-scaling").shadowRoot.querySelector(' \
                      '"#replicas")'

# JS path of "Automatic Scaling" radio button inside "Scaling" accordion in the 'Create Model Service' window
auto_scaling_radio_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                            'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                            'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                            'e-model-services").shadowRoot.querySelector(' \
                            '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                            '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector(' \
                            '"#scaling-accordion > e-service-scaling").shadowRoot.querySelector(' \
                            '"div > div.radio-buttons > eui-base-v0-radio-button.auto.faded") '

# JS path of "Target Metric" dropdown inside "scaling" accordion in the "Create Model Service" window
metric_type_dropdown = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                       'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                       'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                       'e-model-services").shadowRoot.querySelector(' \
                       '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                       '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector(' \
                       '"#scaling-accordion > e-service-scaling").shadowRoot.querySelector(' \
                       '"div > div.options > div:nth-child(2) > eui-base-v0-dropdown")'

# JS path of "Metric Value" input box inside "scaling" accordion in the "Create Model Service" window
metric_value_input_box = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                         'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                         'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                         'e-model-services").shadowRoot.querySelector(' \
                         '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                         '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector(' \
                         '"#scaling-accordion > e-service-scaling").shadowRoot.querySelector(' \
                         '"#targetAverageValue")'

# JS path of "Instances Min" input box inside "scaling" accordion in the "Create Model Service" window
instances_min_input_box = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                          'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                          'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                          'e-model-services").shadowRoot.querySelector(' \
                          '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                          '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector(' \
                          '"#scaling-accordion > e-service-scaling").shadowRoot.querySelector(' \
                          '"#minReplicas")'

# JS path of "Instances Max" input box inside "scaling" accordion in the "Create Model Service" window
instances_max_input_box = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                          'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                          'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                          'e-model-services").shadowRoot.querySelector(' \
                          '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                          '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector(' \
                          '"#scaling-accordion > e-service-scaling").shadowRoot.querySelector(' \
                          '"#maxReplicas")'

# JS path of "Upload Manifest" tab in the 'Create Model Service' window
upload_manifest_tab =  'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                       'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                       'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                       'e-model-services").shadowRoot.querySelector(' \
                       '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                       '"eui-layout-v0-tabs > eui-layout-v0-tab:nth-child(2) > label")'

# JS path to upload manifest yaml as a file input in "Upload Manifest" tab
upload_manifest_file_browse =  'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                               'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                               'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                               'e-model-services").shadowRoot.querySelector(' \
                               '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                               '"eui-layout-v0-tabs > div:nth-child(4) > e-upload-component-with-input").shadowRoot.querySelector("#file-input")'

# JS path to click 'Ok' button in "Upload Manifest" tab
upload_manifest_ok_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                            'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                            'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                            'e-model-services").shadowRoot.querySelector(' \
                            '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                            '"eui-layout-v0-tabs > div:nth-child(4) > e-upload-component-with-input").shadowRoot.querySelector(' \
                            '"#footer > div.button-container > eui-base-v0-button.onboard-btn.button")'

# JS path of "Download Manifest" button in the 'Create Model Service' window
download_manifest_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                                    '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                                    '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                                    '"#AppContent-inner > e-model-services").shadowRoot.querySelector(' \
                                    '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                                    '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector("div > ' \
                                    'div:nth-child(3) > eui-base-v0-button") '

# JS path of 'Create' button in the 'Create Model Service' window
click_model_service_create_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                                    '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                                    '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                                    '"#AppContent-inner > e-model-services").shadowRoot.querySelector(' \
                                    '"e-services-container").shadowRoot.querySelector("#modelTabs").shadowRoot.querySelector(' \
                                    '"eui-layout-v0-tabs > div:nth-child(3) > e-create-model-service").shadowRoot.querySelector("div > ' \
                                    'div:nth-child(4) > eui-base-v0-button:nth-child(2)") '

# JS path to list the 'Service Name' in the model services table
model_services_table_list_service_name = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                                         '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                                         '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                                         '"#AppContent-inner > e-model-services").shadowRoot.querySelector(' \
                                         '"e-services-container").shadowRoot.querySelector(' \
                                         '"eui-layout-v0-multi-panel-tile > ' \
                                         'e-services-table").shadowRoot.querySelector(' \
                                         '"#service-table").shadowRoot.querySelectorAll("div > table > tbody > tr > ' \
                                         'td:nth-child(1) > span") '

# JS path of model services table
model_services_table = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                       'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                       'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                       'e-model-services").shadowRoot.querySelector("e-services-container").shadowRoot.querySelector(' \
                       '"eui-layout-v0-multi-panel-tile > e-services-table").shadowRoot.querySelector(' \
                       '"#service-table") '

# JS path of model services window main title panel that displays the number of running model services
model_services_main_title_panel = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                                  'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                                  'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                                  'e-model-services").shadowRoot.querySelector("e-services-container").shadowRoot.querySelector(' \
                                  '"eui-layout-v0-multi-panel-tile").shadowRoot.querySelector(' \
                                  '"#main-panel-title") '

# JS path of Delete button in Model Service Detail page
model_service_delete_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                              'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                              'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                              'e-model-service-detail").shadowRoot.querySelector(' \
                              '"e-service-detail").shadowRoot.querySelector("div.top > div.button-group > ' \
                              'eui-base-v0-button") '

# JS path of Delete confirmation button in Model Service Detail page after clicking the delete button
model_service_delete_confirmation_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                                           '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                                           '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                                           '"#AppContent-inner > e-model-service-detail").shadowRoot.querySelector(' \
                                           '"e-service-detail").shadowRoot.querySelector("div.content > ' \
                                           'eui-base-v0-dialog:nth-child(5) > eui-base-v0-button") '

# JS path to click the invoke model service in Model Service Details page
invoke_model_service_tab = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                           'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                           'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                           'e-model-service-detail").shadowRoot.querySelector(' \
                           '"e-service-detail").shadowRoot.querySelector("div.content > div.accordions > ' \
                           'div.box.invoke > eui-base-v0-accordion").shadowRoot.querySelector("ul > li > ' \
                           'div.accordion__item__title") '

# JS path to invoke model service text area
invoke_model_service_text_area = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container ' \
                                 '> eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content ' \
                                 '> eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                                 'e-model-service-detail").shadowRoot.querySelector(' \
                                 '"e-service-detail").shadowRoot.querySelector("div.content > div.accordions > ' \
                                 'div.box.invoke > eui-base-v0-accordion > e-invoke-model").shadowRoot.querySelector(' \
                                 '"#invoke-model-input > div > div.jsoneditor-outer.has-main-menu-bar.has-status-bar ' \
                                 '> textarea") '

# JS path to invoke model service button
invoke_model_service_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                              'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                              'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                              'e-model-service-detail").shadowRoot.querySelector(' \
                              '"e-service-detail").shadowRoot.querySelector("div.content > div.accordions > ' \
                              'div.box.invoke > eui-base-v0-accordion > e-invoke-model").shadowRoot.querySelector(' \
                              '"#submit") '

# JS path to identify the number of search elements after providing a value to search in the 'Invoke model service' result
validate_the_result = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                      'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                      'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                      'e-model-service-detail").shadowRoot.querySelector(' \
                      '"e-service-detail").shadowRoot.querySelector("div.content > div.accordions > div.box.invoke > ' \
                      'eui-base-v0-accordion > e-invoke-model").shadowRoot.querySelector("#invoke-model-result > div ' \
                      '> div.jsoneditor-menu > div > div.jsoneditor-results") '

# JS path to the search text box in the 'Invoke model service' window
search_text_box = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                  'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                  'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                  'e-model-service-detail").shadowRoot.querySelector("e-service-detail").shadowRoot.querySelector(' \
                  '"div.content > div.accordions > div.box.invoke > eui-base-v0-accordion > ' \
                  'e-invoke-model").shadowRoot.querySelector("#invoke-model-result > div > div.jsoneditor-menu > div ' \
                  '> div.jsoneditor-frame > input[type=text]") '
