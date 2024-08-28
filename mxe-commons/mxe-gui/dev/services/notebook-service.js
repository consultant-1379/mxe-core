const API_BASE_URL = '/v1';
const notebooks = [
  {
    name: 'notebook1',
    url: '',
  },
  {
    name: 'notebook2',
    url: '',
  },
  {
    name: 'notebook2',
    url: '',
  },
];

module.exports = (app) => {
  /**
   * Flow deployments
   */
  app.get(`${API_BASE_URL}/notebooks`, (req, res) => {
    res.send(notebooks);
  });

  app.post(`${API_BASE_URL}/notebooks`, (req, res) => {
    const notebookToAdd = req.body;

    notebooks.push(notebookToAdd);

    res.send(notebooks);
  });

  app.delete(`${API_BASE_URL}/notebooks/:name`, (req, res) => {
    const { name } = req.params;

    for (let i = 0; i < notebooks.length; i++) {
      if (notebooks[i].name === name) {
        notebooks.splice(i, 1);
        break;
      }
    }

    res.send(notebooks);
  });
};
