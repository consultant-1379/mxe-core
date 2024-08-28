const token = {
  jti: 'd41cb987-bfcc-48c5-9756-c1400a93edb4',
  exp: 1581949931,
  nbf: 0,
  iat: 1581949631,
  iss: 'http://eric-sec-access-mgmt-http:8080/auth/realms/mxe',
  aud: 'mxe-rest-client',
  sub: 'e35d921d-418c-4c6b-97eb-a51a8a1427b4',
  typ: 'ID',
  azp: 'mxe-rest-client',
  auth_time: 0,
  session_state: 'c48c57b4-dc79-4494-bffa-3d05b8433fef',
  acr: '1',
  email_verified: false,
  'mxe-access-control': {
    global: {
      'model-services': ['all'],
      models: ['all'],
      roles: ['administrator'],
    },
    all: {
      'test.seldon': 'all',
    },
    'model-services': {
      'org.eclipse': 'all',
      'com.ericsson.bdgs': 'read',
    },
    models: {
      'com.ericsson': 'all',
      'org.apache': 'read',
    },
  },
  preferred_username: 'mxe-user',
  given_name: '',
  family_name: '',
  prev_auth_time: 1581949544118,
};

module.exports = (app) => {
  app.get('/oauth/token', (req, res) => {
    res.send(token);
  });
};
