const initStore = require('../../../store/index');

let loadingOverlayElement;

/**
 * EUI sdk needed to resolve app load, because it throws a warning if not
 * @param params
 * @return {Function}
 */
const onBeforeAppLoad = (params) => async (resolve) => resolve();

/**
 * Initialize notification array on container load
 * @param params
 * @return {Function}
 */
const onBeforeContainerLoad = (params) => (resolve, reject) => {
  initStore();

  loadingOverlayElement = document.querySelector('.loading-overlay');

  store.subscribe(() => {
    const { loadingOverlay } = store.getState();

    if (loadingOverlay?.show === true) {
      loadingOverlayElement.classList.add('show');
    } else {
      loadingOverlayElement.classList.remove('show');
    }
  });
  resolve();
};

module.exports = {
  onBeforeContainerLoad,
  onBeforeAppLoad,
};
