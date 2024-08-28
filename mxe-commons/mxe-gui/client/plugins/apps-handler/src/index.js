let allApps;
/**
 * Handles the application avaialble and provide default route
 */

const onBeforeContainerLoad = () => (resolve) => {
  const data = window.EUI.MenuConfig?.app.menu.data || [];

  allApps = data.reduce((acc, val) => {
    if (val.children && val.active) {
      return acc.concat(val.children);
    }
    if (val.active) {
      return acc.concat(val);
    }
    return acc;
  }, []);

  resolve();
};

const onBeforeAppLoad = (params) => (resolve, reject) => {
  const appMenu = allApps.find((app) => {
    const path = app.data?.path || app.data?.url;
    return window.location.hash.indexOf(path) > -1;
  });

  if (params.appName !== 'settings' && !appMenu?.active) {
    const error = new Error('Requested application not found. Please contact your administrator.');
    error.name = 'Application Unavailable';
    reject(error);
  }

  if (params.appName !== 'settings' && appMenu === undefined) {
    const error = new Error('No active applications found. Please contact your administrator.');
    error.name = 'Application Unavailable';
    reject(error);
  }
  resolve();
};

module.exports = { onBeforeContainerLoad, onBeforeAppLoad };
