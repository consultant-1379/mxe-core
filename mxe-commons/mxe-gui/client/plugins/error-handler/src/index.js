module.exports = {
  onBeforeContainerLoad: () => (resolve) => {
    if (window._paq) {
      window.onerror = (message) => {
        window._paq.push([
          'trackEvent',
          // Event category
          'error',
          // Event action
          'error',
          // Event target name
          message,
        ]);
      };
    }

    resolve();
  },
  onBeforeAppLoad: () => (resolve) => resolve(),
};
