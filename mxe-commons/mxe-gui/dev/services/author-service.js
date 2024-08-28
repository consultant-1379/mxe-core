const API_BASE_URL = '/v1';
const authors = [
  { publicKey: 'asdasd', name: 'Author 1' },
  { publicKey: 'vfdsdfsd', name: 'Author 2' },
];

module.exports = (app) => {
  app.get(`${API_BASE_URL}/authors`, (req, res) => {
    res.send(authors);
  });

  app.post(`${API_BASE_URL}/authors`, (req, res) => {
    authors.push({ publicKey: 'hashed_public_key', name: 'SZTAKI' });

    res.send(authors);
  });

  app.delete(`${API_BASE_URL}/authors/:hash`, (req, res) => {
    const { hash } = req.params;

    for (let i = 0; i < authors.length; i++) {
      if (authors[i].publicKey === hash) {
        authors.splice(i, 1);
        break;
      }
    }
    res.send(authors);
  });
};
