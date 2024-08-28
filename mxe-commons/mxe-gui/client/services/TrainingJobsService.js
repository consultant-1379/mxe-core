import { httpService } from 'utils/HttpService';
import { API_TRAINING_JOBS_PATH } from 'utils/Config';

/**
 * @class Training Jobs Service
 * @classdesc MXE Training Job Service
 */
class TrainingJobsService {
  /**
   * Get Training Job list from the API
   * @returns {Promise<any>}
   */
  static getTrainingJobs() {
    return httpService.getRequest(`${API_TRAINING_JOBS_PATH}`);
  }

  /**
   * Get Training Job by ID from the API
   * @param {string} id - Training Job id
   * @returns {Promise<any>}
   */
  static getTrainingJobById(id) {
    return httpService.getRequest(`${API_TRAINING_JOBS_PATH}/${id}`);
  }

  /**
   * Get Training Job by ID from the API
   * @param {string} id - Training package id
   * @returns {Promise<any>}
   */
  static getTrainingJobByPackageId(id) {
    return httpService.getRequest(`${API_TRAINING_JOBS_PATH}?packageId=${id}`);
  }

  /**
   * Get Training Job by ID from the API
   * @param {string} id - Training package id
   * @param {string} version - Training package version
   * @returns {Promise<any>}
   */
  static getTrainingJobByPackageIdVersion(id, version) {
    return httpService.getRequest(
      `${API_TRAINING_JOBS_PATH}?packageId=${id}&packageVersion=${version}`
    );
  }

  /**
   * Get Training Job by ID from the API
   * @param {string} id - Training job id
   * @returns {Promise<any>}
   */
  static getTrainingJobResult(id) {
    return httpService.getRequest(`${API_TRAINING_JOBS_PATH}/${id}/result`);
  }

  /**
   * Post Training Job to the API
   * @param {string} packageId - Training package id
   * @param {string} packageVersion - Training package version
   * @returns {Promise<any>}
   */
  static postTrainingJob(packageId, packageVersion) {
    return httpService.postRequest(`${API_TRAINING_JOBS_PATH}`, { packageId, packageVersion });
  }

  /**
   * Delete Training Job from the API
   * @param {string} trainingJobId - TrainingJob name
   * @returns {Promise<any>}
   */
  static deleteTrainingJob(trainingJobId) {
    return httpService.deleteRequest(`${API_TRAINING_JOBS_PATH}/${trainingJobId}`);
  }
}

export default TrainingJobsService;
