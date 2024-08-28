import { httpService } from 'utils/HttpService';
import { xmlHttpService } from 'utils/XMLHttpService';
import { UNKNOWN, UPLOAD_FORM_FILE_NAME } from 'utils/Enums';
import { API_MODELS_PATH } from 'utils/Config';

/**
 * @class ModelService
 * @classdesc MXE Model Service
 */
class ModelService {
  /**
   * Get model list from the API
   * @param {Boolean} showPermittedActions - Show permitted actions field or not
   * @returns {Promise<any>}
   */
  static getModels(showPermittedActions = true) {
    return httpService.getRequest(
      `${API_MODELS_PATH}?showPermittedActions=${showPermittedActions}`
    );
  }

  /**
   * Post model to the API
   * @param {Blob} file - Model file to upload
   * @returns {Promise<any>}
   */
  static postModel(file) {
    return xmlHttpService.uploadFile(API_MODELS_PATH, UPLOAD_FORM_FILE_NAME, file, 'zip');
  }

  /**
   * Delete model from the API
   * @param {string} modelId - Model name
   * @param {string} modelVersion - Model version
   * @returns {Promise<any>}
   */
  static deleteModel(modelId, modelVersion) {
    const version = modelVersion || UNKNOWN;
    return httpService.deleteRequest(`${API_MODELS_PATH}/${modelId}/${version}`);
  }
}

export default ModelService;
