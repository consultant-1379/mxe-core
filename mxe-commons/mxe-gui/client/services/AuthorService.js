import { httpService } from 'utils/HttpService';
import { API_AUTHORS_PATH } from 'utils/Config';

/**
 * @class AuthorService
 * @classdesc MXE Author Service
 */
class AuthorService {
  /**
   * Get author list from the API
   * @returns {Promise<Array>}
   */
  static getAuthors() {
    return httpService.getRequest(API_AUTHORS_PATH);
  }

  /**
   * Post author to the API
   * @param {string} publicKey
   * @param {string} name
   * @returns {Promise<any>}
   */
  static postAuthor(name, publicKey) {
    return httpService.postRequest(API_AUTHORS_PATH, {
      name,
      publicKey,
    });
  }

  /**
   * Delete author from the API
   * @param {string} name - Author name
   * @returns {Promise<any>}
   */
  static deleteAuthor(name) {
    return httpService.deleteRequest(`${API_AUTHORS_PATH}/${name}`);
  }
}

export default AuthorService;
