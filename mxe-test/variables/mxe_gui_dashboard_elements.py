# This files defines the JS (Java Script) path of GUI elements in MXE DASHBOARD page.

# JS path of 'Sign out' button of MXE GUI
sign_out_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                  'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-settings > ' \
                  'eui-system-panel").shadowRoot.querySelector("#SystemPanel-inner > ' \
                  'eui-user-settings-panel").shadowRoot.querySelector("div > div.footer > ' \
                  'eui-base-v0-button").shadowRoot.querySelector("button > span") '

# JS path of 'User Name' clickable item which is located in top right corner of MXE GUI
user_name_item = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                 'eui-container-system-bar").shadowRoot.querySelector("#bt-user-icon").shadowRoot.querySelector("div ' \
                 '> span") '

# JS path of 'Dashboard' clickable menu. This will open the pane that lists various menu items that are clickable.
dashboard_menu = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                 'eui-container-layout-holder").shadowRoot.querySelector("#Layout-appbar > ' \
                 'eui-app-bar").shadowRoot.querySelector("#AppBar-menu-toggle") '

# JS path of menu panel root.
menu_panel = 'document.querySelector("body > eui-container").shadowRoot.getElementById("container")'\
             '.querySelector("eui-container-layout-holder").shadowRoot.getElementById("LayoutHolder-app-level-nav")'\
             '.querySelector("eui-app-nav").shadowRoot.querySelector("#AppNav-inner > eui-menu-panel").shadowRoot'

# JS path of "Model Packages' clickable item in the Menu
model_packages_menu_item = menu_panel+'.getElementById("MODEL_CATALOGUE")'

# JS path of "Model Services' clickable item in the Menu
model_services_menu_item = menu_panel+'.getElementById("MODEL_SERVICES")'

# JS path of "Training packages' clickable item in the Menu
training_packages_menu_item = menu_panel+'.getElementById("TRAINING_PACKAGES")'

# JS path of "Training jobs' clickable item in the Menu
training_jobs_menu_item = menu_panel+'.getElementById("TRAINING_JOBS")'

# JS path of "Notebooks" clickable item in the Menu
notebooks_menu_item = menu_panel+'.getElementById("NOTEBOOKS")'
                          