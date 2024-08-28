const API_BASE_URL = '/v2';
const MODEL_LCM_API_BASE_URL = '/model-lcm/v1';
const modelServices = [
  {
    models: [
      {
        id: 'com.ericsson.imagerecognition.vgg16',
        version: '0.0.1',
        weight: '0.2',
      },
      {
        id: 'com.ericsson.imagerecognition.inception7',
        version: '0.0.1',
        weight: '0.8',
      },
    ],
    created: '2018-12-07T14:52:56Z',
    autoScaling: {
      minReplicas: 1,
      maxReplicas: 5,
      metrics: [
        {
          name: 'cpuMilliCores',
          targetAverageValue: 100,
        },
      ],
    },
    name: 'com-ericsson-imagerecognition-vgg16',
    type: 'static',
    status: 'running',
    createdByUserId: '2499600f-bf68-4c4d-b86f-270409d66e8f',
    createdByUserName: 'mxe-user',
    actions: ['all'],
  },
  {
    models: [
      {
        id: 'com.ericsson.imagerecognition.inception3',
        version: '0.0.1',
      },
    ],
    created: '2018-12-08T14:52:56Z',
    replicas: 5,
    name: 'com-ericsson-imagerecognition-inception3',
    type: 'model',
    status: 'error',
    message: 'Failed to create deployment seldon-246b3bb',
    createdByUserId: '2499600f-bf68-4c4d-b86f-270409d66e8f',
    createdByUserName: 'mxe-user',
    actions: ['all'],
  },
  {
    models: [
      {
        id: 'com.ericsson.imagerecognition.inception3',
        version: '0.0.1',
        weight: '0.4',
      },
      {
        id: 'com.ericsson.imagerecognition.inception7',
        version: '0.0.1',
        weight: '0.6',
      },
    ],
    created: '2018-12-08T14:52:56Z',
    replicas: 5,
    name: 'yolo',
    type: 'model',
    status: 'creating',
    message: 'Failed to create deployment seldon-246b3bb',
    createdByUserId: '2499600f-bf68-4c4d-b86f-270409d66e8f',
    createdByUserName: 'mxe-user',
    actions: [],
  },
];
let creating = true;
const invokeResponse = {
  meta: {
    puid: 'hkcmohedql70j3o7epv03h5ad5',
    tags: {},
    routing: {},
    requestPath: {
      imginception3: 'armdocker.rnd.ericsson.se/proj-mxe-models/image/img_inception3:0.0.1',
    },
    metrics: [],
  },
  data: {
    names: ['t:0', 't:1', 't:2'],
    ndarray: [
      ['n02504458', 'African_elephant', '0.9389396'],
      ['n01871265', 'tusker', '0.040452186'],
      ['n02408429', 'water_buffalo', '0.0038187227'],
    ],
  },
};

const SERVICE_LOGS = {
  'iot-test1-main-e278548-54f57b47df-lkgh6/seldon-container-engine':
    '2020-01-03 14:46:29.572  INFO 7 --- [           main] io.seldon.engine.App                     : Started App in 9.446 seconds (JVM running for 10.691)\n',
  'iot-test1-main-e278548-54f57b47df-p24sq/seldon-container-engine':
    '2020-01-03 14:46:46.813  INFO 8 --- [           main] io.seldon.engine.App                     : Started App in 6.329 seconds (JVM running for 7.068)\n',
  'iot-test1-main-e278548-54f57b47df-lkgh6/model':
    '14:46:25.182531 139899188122624 _internal.py:122]  * Running on http://0.0.0.0:9000/ (Press CTRL+C to quit)\n',
  'iot-test1-main-e278548-54f57b47df-p24sq/model':
    '14:46:43.885142 140183029613568 _internal.py:122]  * Running on http://0.0.0.0:9000/ (Press CTRL+C to quit)\n',
};

const _listServices = (req, res) => {
  let allModelServices = modelServices;
  if (req.url.startsWith(MODEL_LCM_API_BASE_URL)) {
    allModelServices = {
      services: modelServices,
    };
    allModelServices = JSON.stringify(allModelServices).replaceAll('"created"', '"createdAt"');
  }
  res.send(allModelServices);
};

const _createService = (req, res) => {
  const modelDeploymentToAdd = req.body;
  modelDeploymentToAdd.status = creating ? 'creating' : 'running';
  creating = !creating;
  modelServices.push(modelDeploymentToAdd);
  if (req.url.startsWith(MODEL_LCM_API_BASE_URL)) {
    res.send({
      message:
        'Model service single-stateless-manual has been created with model img.inception3:0.0.1, with 3 instances',
    });
  } else {
    res.send(modelServices);
  }
};

const _getServiceDetails = (req, res) => {
  const name = decodeURI(req.params.name);
  for (let i = 0, { length } = modelServices; i < length; i++) {
    if (modelServices[i].name === name) {
      if (req.url.startsWith(MODEL_LCM_API_BASE_URL)) {
        res.send(JSON.stringify(modelServices[i]).replaceAll('"created"', '"createdAt"'));
      } else {
        res.send(modelServices[i]);
      }
      break;
    }
  }
};

const _getServiceLogs = (req, res) => {
  const name = decodeURI(req.params.name);
  if (!name) {
    return res.send({});
  }
  const { query } = req;
  console.info(query);
  return res.send(SERVICE_LOGS);
};

const _deleteService = (req, res) => {
  const name = decodeURI(req.params.name);
  for (let i = 0, { length } = modelServices; i < length; i++) {
    if (modelServices[i].name === name) {
      modelServices.splice(i, 1);
      break;
    }
  }
  res.send();
};

module.exports = (app) => {
  app.get(`${API_BASE_URL}/model-services`, _listServices);
  app.get(`${MODEL_LCM_API_BASE_URL}/model-services`, _listServices);

  app.post(`${API_BASE_URL}/model-services`, _createService);
  app.post(`${MODEL_LCM_API_BASE_URL}/model-services`, _createService);

  app.get(`${API_BASE_URL}/model-services/:name`, _getServiceDetails);
  app.get(`${MODEL_LCM_API_BASE_URL}/model-services/name=:name`, _getServiceDetails);

  app.get(`${API_BASE_URL}/model-services/:name/logs`, _getServiceLogs);
  app.get(`${MODEL_LCM_API_BASE_URL}/model-services/name=:name/logs`, _getServiceLogs);

  app.get(`${API_BASE_URL}/model-services`, (req, res) => {
    const { query } = req;
    if (query.modelId && query.modelVersion) {
      const id = decodeURI(query.modelId);
      const version = decodeURI(query.modelVersion);
      const results = [];

      for (let i = 0, { length } = modelServices; i < length; i++) {
        const filteredModels = [...modelServices[i].models].filter(
          (model) => model.id === id && model.version === version
        );
        if (filteredModels && filteredModels.length > 0) {
          results.push(modelServices[i]);
        }
      }

      res.send(results);
    }
    res.send([]);
  });

  app.post('/model-endpoints/:name', (req, res) => {
    const name = decodeURI(req.params.name);
    res.send(invokeResponse);
  });

  app.delete(`${API_BASE_URL}/model-services/:name`, _deleteService);
  app.delete(`${MODEL_LCM_API_BASE_URL}/model-services/name=:name`, _deleteService);

  app.patch(`${API_BASE_URL}/model-services/:name`, (req, res) => {
    res.send(modelServices);
  });
  app.patch(`${MODEL_LCM_API_BASE_URL}/model-services/name=:name`, (req, res) => {
    res.send({
      message:
        'Model service single-stateless-manual has been updated with model img.inception3:0.0.1, with 3 instances',
    });
  });
};
