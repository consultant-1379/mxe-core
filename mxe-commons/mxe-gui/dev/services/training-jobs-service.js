const API_BASE_URL = '/v1';
const jobs = [
  {
    id: 'aaa-bbb-ccc',
    packageId: 'com.ericsson.imagerecognition.inception3-trainer',
    packageVersion: '0.0.1',
    created: '2019-07-05T12:32:35.478Z',
    completed: '2019-07-05T12:32:35.478Z',
    status: 'failed',
    errorLog: 'Error log.....',
    message: 'Error happened',
  },
  {
    id: 'aaa-bbb-ddd',
    packageId: 'com.ericsson.imagerecognition.inception3-trainer',
    packageVersion: '0.0.1',
    created: '2019-07-06T12:32:35.478Z',
    completed: null,
    status: 'running',
    errorLog: null,
    message: null,
  },
  {
    id: 'aaa-bbb-eee',
    packageId: 'com.ericsson.imagerecognition.inception3-trainer',
    packageVersion: '0.0.1',
    created: '2019-07-07T12:32:35.478Z',
    completed: '2019-07-07T12:32:35.478Z',
    status: 'completed',
    errorLog: null,
    message: null,
  },
];

module.exports = (app) => {
  app.get(`${API_BASE_URL}/training-jobs`, (req, res) => {
    const { query } = req;
    if (query.packageId) {
      res.send(jobs.filter((job) => job.packageId === query.packageId));
      return;
    }
    if (query.packageId && query.packageVersion) {
      res.send(
        jobs.filter(
          (job) => job.packageId === query.packageId && job.packageVersion === query.packageVersion
        )
      );
      return;
    }
    // res.status(404).send('Not found');
    res.send(jobs);
  });

  app.get(`${API_BASE_URL}/training-jobs/:id`, (req, res) => {
    const { id } = req.params;
    res.send(jobs.filter((job) => job.id === id));
  });

  app.delete(`${API_BASE_URL}/training-jobs/:id/`, (req, res) => {
    const { id, version } = req.params;
    for (let i = 0, { length } = jobs; i < length; i++) {
      if (jobs[i].id === id) {
        jobs.splice(i, 1);
        break;
      }
    }
    res.send();
  });

  app.post(`${API_BASE_URL}/training-jobs`, (req, res) => {
    const packageToAdd = {
      id: 'aaa-bbb-fff',
      packageId: 'com.ericsson.imagerecognition.inception3-trainer',
      packageVersion: '0.0.1',
      created: '2019-07-07T12:32:35.478Z',
      completed: '2019-07-07T12:32:35.478Z',
      status: 'failed',
      errorLog: null,
      message: null,
    };

    jobs.push(packageToAdd);

    res.send(jobs);
  });
};
