const redux = require('redux');
const reducers = require('./reducers');

module.exports = () => {
  if (!window.store) {
    window.store = redux.createStore(
      reducers.reducers,
      window.__REDUX_DEVTOOLS_EXTENSION__ && window.__REDUX_DEVTOOLS_EXTENSION__()
    );
    console.info('store initialized');
  }
};
