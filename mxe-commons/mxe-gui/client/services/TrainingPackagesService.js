import { httpService } from 'utils/HttpService';
import { xmlHttpService } from 'utils/XMLHttpService';
import { UNKNOWN, UPLOAD_FORM_FILE_NAME_TRAINING_PACKAGE } from 'utils/Enums';
import { API_TRAINING_PACKAGES_PATH } from 'utils/Config';

/**
 * @class Training Packages Service
 * @classdesc MXE Training Package Service
 */
class TrainingPackagesService {
  /**
   * Get Training Package list from the API
   * @returns {Promise<any>}
   */
  static getTrainingPackages() {
    return httpService.getRequest(`${API_TRAINING_PACKAGES_PATH}`);
  }

  /**
   * Get Training Package list by id from the API
   * @param {string} id - Training package id
   * @param {string} version - Training package version
   * @returns {Promise<any>}
   */
  static getTrainingPackagesByIdAndVersion(id, version) {
    return httpService.getRequest(`${API_TRAINING_PACKAGES_PATH}/${id}/${version}`);
  }

  /**
   * Post Training Package to the API
   * @param {Blob} file - TrainingPackage file to upload
   * @returns {Promise<any>}
   */
  static postTrainingPackage(file) {
    return xmlHttpService.uploadFile(
      `${API_TRAINING_PACKAGES_PATH}`,
      UPLOAD_FORM_FILE_NAME_TRAINING_PACKAGE,
      file,
      'zip'
    );
  }

  /**
   * Delete Training Package from the API
   * @param {string} trainingPackageId - TrainingPackage name
   * @param {string} trainingPackageVersion - TrainingPackage version
   * @returns {Promise<any>}
   */
  static deleteTrainingPackage(trainingPackageId, trainingPackageVersion) {
    const version = trainingPackageVersion || UNKNOWN;
    return httpService.deleteRequest(
      `${API_TRAINING_PACKAGES_PATH}/${trainingPackageId}/${version}`
    );
  }
}

export default TrainingPackagesService;
