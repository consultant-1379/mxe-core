# This files defines the JS (Java Script) path of GUI elements in MODEL PACKAGES MENU page.
# ------MODEL PACKAGES MENU PAGE ITEMS------

# JS path to find the view of 'Model Packages' window. By default it is card view (element name: view-list). It can 
# also be table view (element name: view-tiles) 
model_packages_window_view_type_element = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                               'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                               'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                               'e-model-catalogue").shadowRoot.querySelector(' \
                               '"e-model-container").shadowRoot.querySelector("eui-layout-v0-multi-panel-tile > div > ' \
                               'e-view-change").shadowRoot.querySelector("eui-v0-icon").name '

# JS path of "View Change" button in 'Model Packages' window
view_change_button_in_model_packages = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                     'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                     'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                     'e-model-catalogue").shadowRoot.querySelector("e-model-container").shadowRoot.querySelector(' \
                     '"eui-layout-v0-multi-panel-tile > div > e-view-change").shadowRoot.querySelector("eui-v0-icon") '

# JS path of "Onboard model' button in 'Model Packages' window
onboard_model_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                       'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                       'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                       'e-model-catalogue").shadowRoot.querySelector("e-model-container").shadowRoot.querySelector(' \
                       '"eui-layout-v0-multi-panel-tile > div > eui-base-v0-button").shadowRoot.querySelector(' \
                       '"button") '

# JS path to upload model source code in 'Upload Model' window
upload_model_sourcecode = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                          'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                          'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                          'e-model-catalogue").shadowRoot.querySelector(' \
                          '"e-model-container").shadowRoot.querySelector("eui-base-v0-dialog.upload-dialog > ' \
                          'e-upload-component").shadowRoot.querySelector("#file-input") '

# JS path to fetch the enabled status of OK button in 'Upload Model' window
ok_button_status_in_upload_model = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                   'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                   'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                   'e-model-catalogue").shadowRoot.querySelector("e-model-container").shadowRoot.querySelector(' \
                   '"eui-base-v0-dialog.upload-dialog > e-upload-component").shadowRoot.querySelector("#footer > ' \
                   'div.button-container > eui-base-v0-button.onboard-btn.button").shadowRoot.querySelector("button") '

# JS path of OK button in 'Upload Model' window
ok_button_in_upload_model = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
            'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
            'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
            'e-model-catalogue").shadowRoot.querySelector("e-model-container").shadowRoot.querySelector(' \
            '"eui-base-v0-dialog.upload-dialog > e-upload-component").shadowRoot.querySelector("#footer > ' \
            'div.button-container > eui-base-v0-button.onboard-btn.button") '

# JS path to list the 'Model id' in the model packages table
model_packages_table_list_model_id = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                                     '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                                     '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                                     '"#AppContent-inner > e-model-catalogue").shadowRoot.querySelector(' \
                                     '"e-model-container").shadowRoot.querySelector("eui-layout-v0-multi-panel-tile > ' \
                                     'e-model-table").shadowRoot.querySelector(' \
                                     '"#model-table").shadowRoot.querySelectorAll("div > table > tbody > tr > ' \
                                     'td:nth-child(2) > span") '

# JS path of model packages table
model_packages_table = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                       'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                       'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                       'e-model-catalogue").shadowRoot.querySelector("e-model-container").shadowRoot.querySelector(' \
                       '"eui-layout-v0-multi-panel-tile > e-model-table").shadowRoot.querySelector("#model-table") '

# JS path of model versions pane
model_versions_pane_search_box = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container ' \
                                 '> eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content ' \
                                 '> eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                                 'e-model-catalogue").shadowRoot.querySelector(' \
                                 '"e-model-container").shadowRoot.querySelector("#versions > ' \
                                 'e-model-versions").shadowRoot.querySelector("div.header > eui-base-v0-text-field") '

# JS path of Delete button in Model versions sub page
model_packages_delete_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                               'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                               'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                               'e-model-catalogue").shadowRoot.querySelector(' \
                               '"e-model-container").shadowRoot.querySelector("#versions > ' \
                               'e-model-versions").shadowRoot.querySelector("div.version-card.available > ' \
                               'eui-v0-icon") '

# JS path of Delete confirmation button in Model versions sub page after clicking the delete button
model_packages_delete_confirmation_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                                            '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                                            '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                                            '"#AppContent-inner > e-model-catalogue").shadowRoot.querySelector(' \
                                            '"e-model-container").shadowRoot.querySelector(' \
                                            '"eui-base-v0-dialog.confirm-dialog.delete > eui-base-v0-button") '

# JS path of version card inside "Model versions" pane
model_version_card = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                     'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                     'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                     'e-model-catalogue").shadowRoot.querySelector("e-model-container").shadowRoot.querySelector(' \
                     '"#versions > e-model-versions").shadowRoot.querySelector("div.version-card.available > ' \
                     'div.title.link") '
