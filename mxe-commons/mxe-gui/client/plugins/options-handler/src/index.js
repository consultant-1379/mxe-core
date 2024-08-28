import { register } from '../../../utils/MicroServiceRequestHandler';
import {
  OPTIONS_PATH,
  API_PATH,
  DEFAULT_GUI_MODE,
  IS_STANDALONE_STR,
  STANDALONE_GUI_MODE,
  PROMETHEUS_BASE_PATH_STR,
  API_BASE_URL,
  API_PROMETHEUS_PATH,
  OPTIONS_STR,
} from '../../../utils/Config';

const onBeforeContainerLoad = () => (resolve) => {
  fetch(OPTIONS_PATH)
    .then((resp) => resp.json())
    .then((options) => {
      // GUI Mode
      const mode =
        options?.mode !== undefined && options?.mode !== null && options?.mode !== ''
          ? options.mode
          : DEFAULT_GUI_MODE;
      localStorage.setItem(IS_STANDALONE_STR, mode === STANDALONE_GUI_MODE);
      // Prometheus Base Path
      const prometheusBasePath =
        options?.prometheusBasePath !== undefined &&
        options?.prometheusBasePath !== null &&
        options?.prometheusBasePath !== ''
          ? options.prometheusBasePath
          : API_BASE_URL + API_PROMETHEUS_PATH;
      localStorage.setItem(PROMETHEUS_BASE_PATH_STR, prometheusBasePath);
      // Other Options
      localStorage.setItem(OPTIONS_STR, JSON.stringify(options));
      fetch(API_PATH)
        .then((resp) => resp.json())
        .then((apis) => {
          if (apis[mode] !== undefined && apis[mode] !== null && apis[mode] !== '') {
            window.MXEAPI = apis[mode];
          }
        });
    })
    .catch((error) => console.log('Options set, using defaults. Reason : ', error));

  register();
  resolve();
};

const onBeforeAppLoad = () => (resolve) => resolve();

export { onBeforeContainerLoad, onBeforeAppLoad };
