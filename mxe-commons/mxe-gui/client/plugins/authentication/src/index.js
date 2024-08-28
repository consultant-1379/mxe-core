const initStore = require('../../../store/index');
const { setGlobalActions } = require('../../../store/actions');

const onBeforeContainerLoad = (params) => (resolve, reject) => {
  initStore();

  fetch('/oauth/token')
    .then((resp) => resp.json())
    .then((tokenData) => {
      if (tokenData.sub && window._paq) {
        window._paq.push(['setUserId', tokenData.sub]);
        window._paq.push(['trackPageView']);
      }
      if (tokenData.preferred_username) {
        localStorage.setItem('username', tokenData.preferred_username);
      } else {
        localStorage.setItem('username', 'mxe-no-username');
      }
      if (tokenData.prev_auth_time) {
        const date = new Date(tokenData.prev_auth_time);
        const strDate = `${`00${date.getDate()}`.slice(-2)}/${`00${date.getMonth() + 1}`.slice(
          -2
        )}/${date.getFullYear()} ${`00${date.getHours()}`.slice(
          -2
        )}:${`00${date.getMinutes()}`.slice(-2)}:${`00${date.getSeconds()}`.slice(-2)}`;
        localStorage.setItem('lastLoginTime', strDate);
      } else {
        localStorage.setItem('lastLoginTime', 'Not Available');
      }
      const actions = tokenData?.['mxe-access-control'] ?? {};
      store.dispatch(setGlobalActions(actions));
      resolve();
    })
    .catch(() => {
      localStorage.setItem('username', 'mxe-no-username');
      resolve();
    });
};
const onBeforeAppLoad = (params) => (resolve) => resolve();
const checkStatus = () => true; // () => /^(.*;)?\s*(kc-access)\s*=\s*[^;]/.test(document.cookie),
const clearSession = (params) => {
  fetch('/oauth/logout')
    .then((resp) => {
      // Set the window location to the login page
      window.location = '/';
    }) // Transform the data into json
    .catch((error) => {
      console.log(error);
    });

  // Additional call to jupyter hub, to clear off
  // cookies, if open any
  fetch('jupyter/hub/logout?redirect=False')
    .then((resp) => {
      // Close the opened notebookWindow
      window.notebookWindow?.close();
      // Set the window location to the login page
      window.location = '/';
    }) // Transform the data into json
    .catch((error) => {
      console.log(error);
    });
};

module.exports = { onBeforeContainerLoad, onBeforeAppLoad, checkStatus, clearSession };
