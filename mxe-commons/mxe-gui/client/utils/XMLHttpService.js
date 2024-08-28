import NetworkError from 'utils/NetworkError';
import { UPLOAD_PROGRESS } from 'utils/Enums';
import { API_BASE_URL, API_V2_BASE_URL } from './Config';
/**
 * @class XMLHttpService
 * @classdesc Service for XMLHttp requests
 */
class XMLHttpService {
  /**
   * Make API Request
   * @param {string} path - path to query
   * @param {string} method - method, "GET", "POST", "PUT", "DELETE", etc.
   * @param {string} formFileName - Form field file name
   * @param {Blob} file - File to be uploaded
   * @param {string} [fileFormat] - Optional file format checking
   * @returns {Promise<any>}
   * @private
   */
  async _makeRequest(path, method, formFileName, file, fileFormat) {
    try {
      if (fileFormat && !file.name.includes(fileFormat)) {
        throw new Error(
          `Wrong format, should be ${fileFormat}, and the file type is: ${file.type}`
        );
      }
      return await new Promise((resolve, reject) => {
        // eslint-disable-next-line no-undef
        const xhr = new XMLHttpRequest();
        // eslint-disable-next-line no-undef
        const formData = new FormData();
        const url = `${API_BASE_URL}${path}`;
        formData.append(formFileName, file);
        xhr.open(method, url, true);
        xhr.upload.onprogress = this._progressHandler;
        xhr.onload = () => {
          if (xhr.status >= 200 && xhr.status < 300) {
            resolve(xhr.response);
          } else {
            reject(xhr.statusText);
            throw new NetworkError(xhr.status, xhr.statusText);
          }
        };
        xhr.onerror = () => {
          reject(xhr.statusText);
          throw new NetworkError(xhr.status, xhr.statusText);
        };
        xhr.send(formData);
      });
    } catch (error) {
      // TODO: common error handling
      console.error('error happened: ', error);
      throw error;
    }
  }

  async _makeRequestWithInput(path, method, formFileName, file, fileFormat) {
    try {
      if (fileFormat && !file.name.includes(fileFormat)) {
        throw new Error(
          `Wrong format, should be ${fileFormat}, and the file type is: ${file.type}`
        );
      }
      return await new Promise((resolve, reject) => {
        // eslint-disable-next-line no-undef
        const xhr = new XMLHttpRequest();
        // eslint-disable-next-line no-undef
        const formData = new FormData();
        const url = `${API_V2_BASE_URL}${path}`;
        formData.append(formFileName, file);
        xhr.open(method, url, true);
        xhr.upload.onprogress = this._progressHandler;
        xhr.onload = () => {
          if (xhr.status >= 200 && xhr.status < 300) {
            resolve(xhr.response);
          } else {
            reject(xhr.response);
            throw new NetworkError(xhr.status, xhr.statusText, JSON.parse(xhr.response));
          }
        };
        xhr.onerror = () => {
          reject(xhr.statusText);
          throw new NetworkError(xhr.status, xhr.statusText, JSON.parse(xhr.response));
        };
        xhr.send(formData);
      });
    } catch (error) {
      // TODO: common error handling
      console.error('error happened: ', error);
      throw error;
    }
  }

  /**
   * Handling upload progress
   * @param {Object} event - Progress event
   * @returns {boolean}
   * @private
   */
  _progressHandler(event) {
    const progress = Math.round((event.loaded / event.total) * 100);
    const uploadEvent = new CustomEvent(UPLOAD_PROGRESS, { detail: progress });
    return window.dispatchEvent(uploadEvent);
  }

  /**
   * Upload file via XMLHttp request
   * @param {string} path - Upload path
   * @param {string} formFileName - Form field file name
   * @param {Blob} file - File to be uploaded
   * @param {string} [fileFormat] - Optional file format to check
   * @returns {Promise<any>}
   */
  uploadFile(path, formFileName, file, fileFormat) {
    return this._makeRequest(path, 'POST', formFileName, file, fileFormat);
  }

  uploadFileWithInput(path, formFileName, file, fileFormat) {
    return this._makeRequestWithInput(path, 'POST', formFileName, file, fileFormat);
  }

  patchFileWithInput(path, formFileName, file, fileFormat) {
    return this._makeRequestWithInput(path, 'PATCH', formFileName, file, fileFormat);
  }
}

export const xmlHttpService = new XMLHttpService();
