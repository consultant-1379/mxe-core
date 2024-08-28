import { addNotification } from 'store/actions';
import { STATUS_ERROR } from 'utils/Enums';
import { loc } from 'utils/Utils';

/**
 * @class NetworkError
 * @classdesc Network error class
 */
export default class NetworkError extends Error {
  /**
   * @constructor
   * @param {Object} options - Network request options
   * @param {Response} response - Response object
   * @param {Object} data - Parsed response data
   */
  constructor(options, response, data) {
    super();
    this.status = response.status;
    this.response = response;
    this.message = data?.message || '';
    this.name = 'NetworkError';
    const showNotification = options.method !== 'GET';

    if (showNotification && !options.prometheus) {
      store.dispatch(
        addNotification({
          title: loc('ERROR_HAPPENED'),
          description: this.message,
          status: STATUS_ERROR,
        })
      );
    }
  }

  toString() {
    return `Network Error happened, status: ${this.status}, response: ${JSON.stringify(
      this.response
    )}`;
  }
}
