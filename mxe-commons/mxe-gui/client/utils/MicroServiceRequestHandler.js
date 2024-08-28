import fetchIntercept from 'fetch-intercept';
import jq from 'jsonpath';
import {
  API_BASE_URL,
  API_PROMETHEUS_PATH,
  OPTIONS_KEY,
  OPTIONS_STR,
  PROMETHEUS_BASE_PATH_STR,
} from './Config';

const options = JSON.parse(localStorage.getItem(OPTIONS_STR));

const _rewriteUrl = (url) => {
  let prometheusBasePath = localStorage.getItem(PROMETHEUS_BASE_PATH_STR);
  if (url.startsWith(API_BASE_URL + API_PROMETHEUS_PATH) && prometheusBasePath) {
    prometheusBasePath = prometheusBasePath.startsWith('/')
      ? prometheusBasePath
      : `/${prometheusBasePath}`;
    return url.replace(API_BASE_URL + API_PROMETHEUS_PATH, prometheusBasePath);
  }

  if (window.MXEAPI === undefined) {
    return url;
  }
  for (let index = 0; index < window.MXEAPI.length; index++) {
    const api = window.MXEAPI[index];
    const pathRegex = new RegExp(api.oldPath);
    const match = pathRegex.exec(url);
    if (match !== null) {
      if (api.path.startsWith(OPTIONS_KEY)) {
        const optionsValue = options[api.path.slice(OPTIONS_KEY.length)];
        url =
          optionsValue !== undefined && optionsValue !== null && optionsValue !== ''
            ? optionsValue
            : url;
      } else {
        url = api.path.replaceAll('^', '').replaceAll('$', '');
      }
      const { groups } = match;
      if (groups !== undefined) {
        // eslint-disable-next-line no-loop-func
        Object.keys(groups).forEach((key) => {
          url = url.replace(`{${key}}`, groups[key]);
        });
      }
      break;
    }
  }
  return url;
};

const _fetchRequestProcesser = (url, config) => {
  url = _rewriteUrl(url);
  return [url, config];
};

const _fetchRequestErrorProcesser = async (error) => Promise.reject(error);

const _addAdditionalData = (obj, additionalResponseData) => {
  additionalResponseData.forEach((addt) => {
    const { key, value } = addt;
    if (!obj[key]) obj[key] = [value];
  });
  return obj;
};

const _fetchResponseProcesser = async (response) => {
  if (window.MXEAPI === undefined) return response;

  const url = new URL(response.url);
  const apis = window.MXEAPI?.filter((api) => {
    const _path = api.path
      .replace('{id}', '([a-zA-Z0-9.-]*)')
      .replace(new RegExp('{.*}'), '([\\s\\S])'); // Workdaround for {id} match
    return new RegExp(_path).test(url.pathname);
  });

  if (apis.length === 0) return response;

  let reponseBody = await response.clone().text();
  if (!reponseBody) return response;

  // First matched result
  const { responseDataPath, responseDataMapping, additionalResponseData } = apis[0];
  reponseBody = responseDataPath
    ? jq.query(JSON.parse(reponseBody), responseDataPath)
    : JSON.parse(reponseBody);

  /** Added for mocked actions */
  if (additionalResponseData !== undefined) {
    if (!Array.isArray(reponseBody)) {
      reponseBody = _addAdditionalData(reponseBody, additionalResponseData);
    } else {
      reponseBody.forEach((res) => {
        res = _addAdditionalData(res, additionalResponseData);
      });
    }
  }

  reponseBody = JSON.stringify(reponseBody);
  if (responseDataMapping !== undefined) {
    for (let i = 0; i < responseDataMapping.length; i++) {
      reponseBody = reponseBody.replaceAll(
        responseDataMapping[i].actual,
        responseDataMapping[i].expected
      );
    }
  }

  return new Response(reponseBody, response);
};

const _fetchResponseErrorProcessor = (error) => Promise.reject(error);

const _intercept = {
  request: _fetchRequestProcesser,
  requestError: _fetchRequestErrorProcesser,
  response: _fetchResponseProcesser,
  responseError: _fetchResponseErrorProcessor,
};

const { open: orgOpen, send: orgSend } = XMLHttpRequest.prototype;

function _xhrRequestHeaderHandler(...args) {
  [this._method, this._url] = args;
  args[1] = _rewriteUrl(this._url); // 0 - HTTP Method, 1 - URI
  // If response need to be handled add event listener for 'load'
  orgOpen.apply(this, args);
}

function _xhrRequestDataHandler(...args) {
  const apis = window.MXEAPI?.filter((api) => new RegExp(api.oldPath).test(this._url));
  if (apis && apis.length > 0) {
    const { requestDataMapping } = apis[0];
    if (requestDataMapping !== undefined) {
      for (let i = 0; i < requestDataMapping.length; i++) {
        const formData = args[0].get(requestDataMapping[i].actual);
        if (formData) {
          args[0].set(requestDataMapping[i].expected, formData);
          args[0].delete(requestDataMapping[i].actual);
        }
      }
    }
  }

  orgSend.apply(this, args);
}

export const register = () => {
  // Fetch Request
  fetchIntercept.register(_intercept);
  // XHR Request
  XMLHttpRequest.prototype.open = _xhrRequestHeaderHandler;
  XMLHttpRequest.prototype.send = _xhrRequestDataHandler;
};
