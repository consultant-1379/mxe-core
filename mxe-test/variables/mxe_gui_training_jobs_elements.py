# This files defines the JS (Java Script) path of GUI elements in TRAINING JOBS MENU page.
# ------TRAINING JOBS MENU PAGE ITEMS------


# JS path to list the 'Job id' in the training packages table
training_jobs_table_list_job_id = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                                  '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                                  '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                                  '"#AppContent-inner > e-training-jobs").shadowRoot.querySelector(' \
                                  '"e-job-container").shadowRoot.querySelector("eui-layout-v0-multi-panel-tile > ' \
                                  'e-job-table").shadowRoot.querySelector("#job-table").shadowRoot.querySelectorAll(' \
                                  '"div > table > tbody > tr > td:nth-child(2) > span") '

# JS path of training jobs table
training_jobs_table = 'document.querySelector("body > eui-container").shadowRoot.querySelector("#container > ' \
                      'eui-container-layout-holder").shadowRoot.querySelector("#LayoutHolder-app-content > ' \
                      'eui-app-content").shadowRoot.querySelector("#AppContent-inner > ' \
                      'e-training-jobs").shadowRoot.querySelector("e-job-container").shadowRoot.querySelector(' \
                      '"eui-layout-v0-multi-panel-tile > e-job-table").shadowRoot.querySelector("#job-table") '

# JS path of download results button in top right corner of Training jobs windoe
training_jobs_download_results_button = 'document.querySelector("body > eui-container").shadowRoot.querySelector(' \
                                        '"#container > eui-container-layout-holder").shadowRoot.querySelector(' \
                                        '"#LayoutHolder-app-content > eui-app-content").shadowRoot.querySelector(' \
                                        '"#AppContent-inner > e-training-jobs").shadowRoot.querySelector(' \
                                        '"e-job-container").shadowRoot.querySelector("eui-layout-v0-multi-panel-tile ' \
                                        '> div > eui-base-v0-button") '
