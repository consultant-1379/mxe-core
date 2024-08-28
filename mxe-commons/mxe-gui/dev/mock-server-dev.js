const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const fileUpload = require('express-fileupload');
const authService = require('./services/auth-service');
const modelService = require('./services/model-catalogue-service');
const modelDeploymentService = require('./services/model-service-service');
const prometheusService = require('./services/prometheus-service');
const trainingPackagesService = require('./services/training-packages-service');
const trainingJobsService = require('./services/training-jobs-service');
const authorService = require('./services/author-service');
const menuDataService = require('./services/menu-data-service');
const notebookService = require('./services/notebook-service');

const app = express();
const port = 3000;

app.use(cors());

app.use(
  bodyParser.raw({
    type: 'application/octet-stream',
    limit: '100mb',
  })
);
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(fileUpload({}));

authService(app);
modelService(app);
modelDeploymentService(app);
prometheusService(app);
trainingPackagesService(app);
trainingJobsService(app);
authorService(app);
menuDataService(app);
notebookService(app);

app.listen(port, () => console.log(`Mock server listening on port ${port}!`));
