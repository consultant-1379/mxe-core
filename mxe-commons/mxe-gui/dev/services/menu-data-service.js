const API_BASE_URL = '/v1';
const menu = {
  packages: {
    items: [
      {
        packageName: 'mxe-exploration',
        installed: false,
      },
      {
        packageName: 'mxe-serving',
        installed: true,
      },
      {
        packageName: 'mxe-commons',
        installed: false,
      },
      {
        packageName: 'mxe-workflow',
        installed: true,
      },
      {
        packageName: 'mxe-training',
        installed: false,
      },
    ],
  },
};

module.exports = (app) => {
  app.get(`${API_BASE_URL}/package/system`, (req, res) => {
    res.send(menu);
  });
};
