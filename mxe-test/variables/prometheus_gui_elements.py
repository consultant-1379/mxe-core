# This files defines the JS (Java Script) path of prometheus GUI elements

# JS path to find the command line box where the queries are entered in 'prometheus page"
command_line = 'document.querySelector("#root > div > div.panel > div:nth-child(1) > div > div > ' \
               'div.cm-expression-input > div > div.cm-scroller > div > div") '

# JS path of "Execute" button in 'prometheus page"
execute_button = 'document.querySelector("#root > div > div.panel > div:nth-child(1) > div > div > ' \
                 'div.input-group-append > button.execute-btn.btn.btn-primary") '

# JS path of "Graph" tab in 'prometheus page" to switch to graph view
graph_tab = 'document.querySelector("#root > div > div.panel > div:nth-child(3) > div > ul > li:nth-child(2) > a")'
