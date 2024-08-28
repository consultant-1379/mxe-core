import { httpService } from 'utils/HttpService';
import { xmlHttpService } from 'utils/XMLHttpService';
import { API_MODEL_SERVICES_PATH } from 'utils/Config';
import {
  UPLOAD_FORM_FILE_NAME_MODEL_SRVICES,
  UPLOAD_FORM_FILE_NAME_MODEL_INPUT,
} from 'utils/Enums';
/**
 * @class ModelServiceService
 * @classdesc MXE Model Service
 */
class ModelServiceService {
  /**
   * Get model list from the API
   * @param {Boolean} showPermittedActions - Show permitted actions field or not
   * @returns {Promise<any>}
   */
  static getModelServices(showPermittedActions = true) {
    return httpService.getRequestWithv2Baseurl(
      `${API_MODEL_SERVICES_PATH}?showPermittedActions=${showPermittedActions}`
    );
  }

  /**
   * Get model list from the API
   * @param {String} modelServiceName - Model Service name
   * @param {Object} data - Query data
   * @param {number} limit - Data limit
   * @returns {Promise<any>}
   */
  static getModelServiceLogs(modelServiceName, data, limit = 500000) {
    if (data.unit === 'l') {
      return httpService.getRequestWithv2Baseurl(
        `${API_MODEL_SERVICES_PATH}/${modelServiceName}/logs?lines=${data.value}&limit=${limit}`
      );
    }

    return httpService.getRequestWithv2Baseurl(
      `${API_MODEL_SERVICES_PATH}/${modelServiceName}/logs?seconds=${data.value}&limit=${limit}`
    );
  }

  /**
   * Get model list from the API
   * @param {String} modelServiceName - Model Service name
   * @param {Boolean} showPermittedActions - Show permitted actions field or not
   * @returns {Promise<any>}
   */
  static getModelServiceByName(modelServiceName, showPermittedActions = true) {
    return httpService.getRequestWithv2Baseurl(
      `${API_MODEL_SERVICES_PATH}/${modelServiceName}?showPermittedActions=${showPermittedActions}`
    );
  }

  /**
   * Get model services list from the API by the contained model
   * @param {string} id - Model id
   * @param {string} version - Model version
   * @returns {Promise<any>}
   */
  static getModelServiceByModel(id, version) {
    return httpService.getRequestWithv2Baseurl(
      `${API_MODEL_SERVICES_PATH}?modelId=${id}&modelVersion=${version}`
    );
  }

  /**
   * Delete model list from the API
   * @param {string} modelServiceName - Model Service name
   * @returns {Promise<any>}
   */
  static deleteModelServices(modelServiceName) {
    return httpService.deleteRequestWithV2Baseurl(
      `${API_MODEL_SERVICES_PATH}/${encodeURI(modelServiceName)}`
    );
  }

  /**
   * Patch model deployment to the API
   * @param {string} modelServiceName - Model Service name
   * @param {Object} modelService - Model data
   * @returns {Promise<any>}
   */
  static patchModelService(modelServiceName, manifestFile) {
    return xmlHttpService.patchFileWithInput(
      `${API_MODEL_SERVICES_PATH}/${modelServiceName}`,
      UPLOAD_FORM_FILE_NAME_MODEL_SRVICES,
      manifestFile,
      'yml'
    );
  }

  /**
   * Post modelService to the API
   * @param {Object} service - Service object
   * @returns {Promise<any>}
   */
  static postModelService(service) {
    return httpService.postRequest(API_MODEL_SERVICES_PATH, service);
  }

  static postModelServiceWithinput(file) {
    if (file.name.includes('yml')) {
      return xmlHttpService.uploadFileWithInput(
        API_MODEL_SERVICES_PATH,
        UPLOAD_FORM_FILE_NAME_MODEL_SRVICES,
        file,
        'yml'
      );
    }
    return xmlHttpService.uploadFileWithInput(
      API_MODEL_SERVICES_PATH,
      UPLOAD_FORM_FILE_NAME_MODEL_SRVICES,
      file,
      'yaml'
    );
  }

  /**
   * Invoke model with custom data
   * @param {string} path - model endpoint path
   * @param {Object} request - data to invoke the model
   * @return {Promise<any>}
   */
  static invokeModel(path, request, rawContentType) {
    if (rawContentType) {
      return httpService.postRawRequest(path, request, null, '');
    }

    return httpService.postRequest(path, request, null, '');
  }
}

export default ModelServiceService;
