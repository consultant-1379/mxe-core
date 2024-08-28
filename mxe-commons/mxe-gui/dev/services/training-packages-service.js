const API_BASE_URL = '/v1';
const packages = [
  {
    id: 'com.ericsson.imagerecognition.inception3-trainer',
    version: '0.0.1',
    title: 'Inception 3 model',
    author: 'Kovacs Istvan',
    description: 'This is the Inception3 model trainer detailed description',
    image: 'vmx-eea171:5000/img_inception3_trainer:v0.0.1',
    created: '2019-07-05T12:32:35.478Z',
    icon: '',
    status: 'packaging',
    message: null,
  },
  {
    id: 'com.ericsson.imagerecognition.inception3-trainer',
    version: '0.0.2',
    title: 'Inception 3 model',
    author: 'Kovacs Istvan',
    description: 'This is the Inception3 model trainer detailed description',
    image: 'vmx-eea171:5000/img_inception3_trainer:v0.0.1',
    created: '2019-07-06T12:32:35.478Z',
    icon: '',
    status: 'available',
    message: null,
  },
  {
    id: 'com.ericsson.imagerecognition.inception3-trainer',
    version: '0.0.3',
    title: 'Inception 3 model',
    author: 'Kovacs Istvan',
    description: 'This is the Inception3 model trainer detailed description',
    image: 'vmx-eea171:5000/img_inception3_trainer:v0.0.1',
    created: '2019-07-06T12:32:35.478Z',
    icon: '',
    status: 'available',
    message: null,
  },
  {
    id: 'com.ericsson.imagerecognition.inception3-trainer',
    version: '0.0.4',
    title: 'Inception 3 model',
    author: 'Kovacs Istvan',
    description: 'This is the Inception3 model trainer detailed description',
    image: 'vmx-eea171:5000/img_inception3_trainer:v0.0.1',
    created: '2019-07-06T12:32:35.478Z',
    icon: '',
    status: 'available',
    message: null,
  },
  {
    id: 'com.ericsson.imagerecognition.inception3-trainer',
    version: '0.0.5',
    title: 'Inception 3 model',
    author: 'Kovacs Istvan',
    description: 'This is the Inception3 model trainer detailed description',
    image: 'vmx-eea171:5000/img_inception3_trainer:v0.0.1',
    created: '2019-07-06T12:32:35.478Z',
    icon: '',
    status: 'available',
    message: null,
  },
  {
    id: 'com.ericsson.imagerecognition.inception4-trainer',
    version: '0.0.1',
    title: 'Inception 4 model',
    author: 'Kovacs Istvan',
    description: 'This is the Inception3 model trainer detailed description',
    image: 'vmx-eea171:5000/img_inception3_trainer:v0.0.1',
    created: '2019-07-07T12:32:35.478Z',
    icon: '',
    status: 'packaging',
    message: null,
  },
  {
    id: 'com.ericsson.imagerecognition.inception5-trainer',
    version: '0.0.1',
    title: 'Inception 5 model',
    author: 'Kovacs Istvan',
    description: 'This is the Inception3 model trainer detailed description',
    image: 'vmx-eea171:5000/img_inception3_trainer:v0.0.1',
    created: '2019-07-08T12:32:35.478Z',
    icon: '',
    status: 'error',
    message: null,
  },
  {
    id: 'com.ericsson.imagerecognition.inception5-trainer',
    version: '0.0.2',
    title: 'Inception 5 model',
    author: 'Kovacs Istvan',
    description: 'This is the Inception3 model trainer detailed description',
    image: 'vmx-eea171:5000/img_inception3_trainer:v0.0.1',
    created: '2019-07-08T12:32:35.478Z',
    icon: '',
    status: 'error',
    message: null,
  },
  {
    id: 'com.ericsson.inception5-trainer',
    version: '0.0.1',
    title: 'Inception 5 model',
    author: 'Kovacs Bela',
    description: 'This is the model trainer detailed description',
    image: 'vmx-eea171:5000/img_inception3_trainer:v0.0.1',
    created: '2019-07-08T12:32:35.478Z',
    icon: '',
    status: 'error',
    message: null,
  },
];

module.exports = (app) => {
  app.get(`${API_BASE_URL}/training-packages`, (req, res) => {
    // res.status(404).send('Not found');
    res.send(packages);
  });

  app.get(`${API_BASE_URL}/training-packages/:id/:version`, (req, res) => {
    const { id, version } = req.params;
    res.send(packages.find((item) => item.id === id && item.version === version));
  });

  app.delete(`${API_BASE_URL}/training-packages/:id/:version`, (req, res) => {
    const { id, version } = req.params;
    for (let i = 0, { length } = packages; i < length; i++) {
      if (packages[i].id === id && packages[i].version === version) {
        packages.splice(i, 1);
        break;
      }
    }
    res.send();
  });

  app.post(`${API_BASE_URL}/training-packages`, (req, res) => {
    const packageToAdd = {
      id: 'com.ericsson.imagerecognition.vgg16',
      version: '0.0.1',
      title: 'Image Recognition VGG16',
      author: 'Kovacs Istvan',
      description: 'This is the VGG16 image recognition model',
      image: 'vmx-eea171:5000/img_vgg16:v0.0.1',
      created: '2019-04-23T15:12:24.110Z',
      icon: null,
      status: 'available',
      message: null,
    };

    packages.push(packageToAdd);

    res.send(packages);
  });
};
