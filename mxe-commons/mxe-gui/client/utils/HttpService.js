import NetworkError from 'utils/NetworkError';
import { setLoadingScreenState } from 'store/actions';
import { API_BASE_URL, API_V2_BASE_URL } from './Config';

/**
 * @class HttpService
 * @classdesc Service for Http requests
 */
class HttpService {
  /**
   * Make API Request
   * @param {string} path - path to query
   * @param {Object} options - options config
   * @param {string} [baseUrl] - optional baseUrl
   * @returns {Promise<*>}
   * @private
   */
  async _makeRequest(path, options, baseUrl = API_BASE_URL) {
    const url = `${baseUrl}${path}`;
    const response = await fetch(url, options);
    const text = await response.text(); // Parse it as text
    const data = text.length ? JSON.parse(text) : {}; // Try to parse it as json
    if (response.status >= 400 || response.status < 200) {
      store.dispatch(setLoadingScreenState(false));
      throw new NetworkError(options, response, data);
    }
    store.dispatch(setLoadingScreenState(false));
    return data;
  }

  async _makeRequestWithV2Baseurl(path, options, baseUrl = API_V2_BASE_URL) {
    const url = `${baseUrl}${path}`;
    const response = await fetch(url, options);
    const text = await response.text(); // Parse it as text
    const data = text.length ? JSON.parse(text) : {}; // Try to parse it as json
    if (response.status >= 400 || response.status < 200) {
      store.dispatch(setLoadingScreenState(false));
      throw new NetworkError(options, response, data);
    }
    store.dispatch(setLoadingScreenState(false));
    return data;
  }

  /**
   * Get from API endpoint
   * @param {string} path - path to query
   * @returns {Promise<*>}
   */
  async getRequest(path) {
    const options = {
      method: 'GET',
    };
    return this._makeRequest(path, options);
  }

  async getRequestWithv2Baseurl(path) {
    const options = {
      method: 'GET',
    };
    return this._makeRequestWithV2Baseurl(path, options);
  }

  /**
   * Post to API endpoint
   * @param {string} path - path to query
   * @param {Object} data - data to send
   * @param {Object} [headers] - optional headers
   * @param {string} [baseUrl] - optional base url
   * @returns {Promise<*>}
   */
  async postRequest(path, data, headers = null, baseUrl) {
    store.dispatch(setLoadingScreenState(true));
    const options = {
      method: 'POST',
      headers: headers || {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    };
    return this._makeRequest(path, options, baseUrl);
  }

  /**
   * Post to API endpoint
   * @param {string} path - path to query
   * @param {Object} data - data to send
   * @param {Object} [headers] - optional headers
   * @param {string} [baseUrl] - optional base url
   * @returns {Promise<*>}
   */
  async postRawRequest(path, data, headers = null, baseUrl) {
    store.dispatch(setLoadingScreenState(true));
    const options = {
      method: 'POST',
      headers: headers || {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: data,
    };
    return this._makeRequest(path, options, baseUrl);
  }

  /**
   * Post url encoded to API endpoint
   * @param {string} path - path to query
   * @param {Object} data - data to send
   * @returns {Promise<*>}
   */
  async postURLEncodedRequest(path, data) {
    // Encode the data
    const searchParams = Object.keys(data)
      .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(data[key])}`)
      .join('&');

    const options = {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
      },
      body: searchParams,
      prometheus: true,
    };
    return this._makeRequest(path, options);
  }

  /**
   * Post to API endpoint
   * @param {string} path - path to query
   * @returns {Promise<*>}
   */
  async deleteRequest(path) {
    store.dispatch(setLoadingScreenState(true));
    const options = {
      method: 'DELETE',
    };
    return this._makeRequest(path, options);
  }

  async deleteRequestWithV2Baseurl(path) {
    store.dispatch(setLoadingScreenState(true));
    const options = {
      method: 'DELETE',
    };
    return this._makeRequestWithV2Baseurl(path, options);
  }

  /**
   * Patch to API endpoint
   * @param {string} path - path to query
   * @param {Object} data - data to delete
   * @returns {Promise<*>}
   */
  async patchRequest(path, data) {
    store.dispatch(setLoadingScreenState(true));
    const options = {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    };
    return this._makeRequest(path, options);
  }
}

export const httpService = new HttpService();
