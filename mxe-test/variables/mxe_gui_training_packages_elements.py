# This files defines the JS (Java Script) path of GUI elements in TRAINING PACKAGES MENU page.
# ------TRAINING PACKAGES MENU PAGE ITEMS------

# JS path to find the view of 'Training Packages' window. By default it is card view (element name: view-list). It can 
# also be table view (element name: view-tiles) 
training_packages_window_view_type_element = 'document.querySelector("body > ' \
                                             'eui-container").shadowRoot.querySelector("#container > ' \
                                             'eui-container-layout-holder").shadowRoot.querySelector(' \
                                             '"#LayoutHolder-app-content > ' \
                                             'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                                             'e-training-packages").shadowRoot.querySelector(' \
                                             '"e-package-container").shadowRoot.querySelector(' \
                                             '"eui-layout-v0-multi-panel-tile > div > ' \
                                             'e-view-change").shadowRoot.querySelector("eui-v0-icon").name '

# JS path of "View Change" button in 'Training Packages' window
view_change_button_in_training_packages = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                                          '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                                          '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                                          '"#AppContent-inner > e-training-packages").shadowRoot.querySelector(' \
                                          '"e-package-container").shadowRoot.querySelector(' \
                                          '"eui-layout-v0-multi-panel-tile > div > ' \
                                          'e-view-change").shadowRoot.querySelector("eui-v0-icon") '

# JS path of "Onboard package" button in 'Training Packages' window
onboard_package_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                         'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                         'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                         'e-training-packages").shadowRoot.querySelector(' \
                         '"e-package-container").shadowRoot.querySelector("eui-layout-v0-multi-panel-tile > div > ' \
                         'eui-base-v0-button").shadowRoot.querySelector("button") '

# JS path to upload training package source code in 'Upload Training Package' window
upload_training_package_sourcecode = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                                     '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                                     '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                                     '"#AppContent-inner > e-training-packages").shadowRoot.querySelector(' \
                                     '"e-package-container").shadowRoot.querySelector("eui-base-v0-dialog > ' \
                                     'e-upload-component").shadowRoot.querySelector("#file-input") '

# JS path to fetch the enabled status of OK button in 'Upload Training Package' window
ok_button_status_in_upload_training_package = 'document.querySelector("body > ' \
                                              'eui-container").shadowRoot.querySelector("#container > ' \
                                              'eui-container-layout-holder").shadowRoot.querySelector(' \
                                              '"#LayoutHolder-app-content > ' \
                                              'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                                              'e-training-packages").shadowRoot.querySelector(' \
                                              '"e-package-container").shadowRoot.querySelector("eui-base-v0-dialog > ' \
                                              'e-upload-component").shadowRoot.querySelector("#footer > ' \
                                              'div.button-container > ' \
                                              'eui-base-v0-button.onboard-btn.button").shadowRoot.querySelector(' \
                                              '"button") '

# JS path of OK button in 'Upload Training Package' window
ok_button_in_upload_training_package = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                                       '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                                       '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                                       '"#AppContent-inner > e-training-packages").shadowRoot.querySelector(' \
                                       '"e-package-container").shadowRoot.querySelector("eui-base-v0-dialog > ' \
                                       'e-upload-component").shadowRoot.querySelector("#footer > div.button-container ' \
                                       '> eui-base-v0-button.onboard-btn.button") '

# JS path to list the 'Package id' in the training packages table
training_packages_table_list_package_id = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                                          '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                                          '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                                          '"#AppContent-inner > e-training-packages").shadowRoot.querySelector(' \
                                          '"e-package-container").shadowRoot.querySelector(' \
                                          '"eui-layout-v0-multi-panel-tile > ' \
                                          'e-package-table").shadowRoot.querySelector(' \
                                          '"#package-table").shadowRoot.querySelectorAll("div > table > tbody > tr > ' \
                                          'td:nth-child(5) > span") '

# JS path of training packages table
training_packages_table = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                          'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                          'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                          'e-training-packages").shadowRoot.querySelector(' \
                          '"e-package-container").shadowRoot.querySelector("eui-layout-v0-multi-panel-tile > ' \
                          'e-package-table").shadowRoot.querySelector("#package-table") '

# JS path of Delete button in training package versions sub page
training_packages_delete_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                                  '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                                  '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                                  '"#AppContent-inner > e-training-packages").shadowRoot.querySelector(' \
                                  '"e-package-container").shadowRoot.querySelector("#versions > ' \
                                  'e-package-versions").shadowRoot.querySelector("div.version-card.available > ' \
                                  'eui-v0-icon") '

# JS path of "Execute" button in "Training Packages" window
training_packages_execute_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                                  '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                                  '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                                  '"#AppContent-inner > e-training-packages").shadowRoot.querySelector(' \
                                  '"e-package-container").shadowRoot.querySelector("#versions > ' \
                                  'e-package-versions").shadowRoot.querySelector("div.version-card.available > ' \
                                  'eui-base-v0-button") '

# JS path of "Package versions" pane
training_package_versions_pane_search_box = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                                            '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                                            '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                                            '"#AppContent-inner > e-training-packages").shadowRoot.querySelector(' \
                                            '"e-package-container").shadowRoot.querySelector("#versions > ' \
                                            'e-package-versions").shadowRoot.querySelector("div.header > ' \
                                            'eui-base-v0-text-field") '

# JS path of version card inside "Package versions" pane
training_package_version_card = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container ' \
                                '> eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content ' \
                                '> eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                                'e-training-packages").shadowRoot.querySelector(' \
                                '"e-package-container").shadowRoot.querySelector("#versions > ' \
                                'e-package-versions").shadowRoot.querySelector("div.version-card.available > ' \
                                'div.title") '


