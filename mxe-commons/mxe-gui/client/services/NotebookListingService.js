/**
 * @class NotebookListingService
 * @classdesc MXE Notebook listing Service
 */
class NotebookListingService {
  /**
   * Get notebooks and deployments and merge them together
   * @returns {Promise<any>}
   */
  static async getNotebooks() {
    return new Promise(async (resolve, reject) => {
      try {
        const user = localStorage.getItem('username');
        const notebooks = [
          {
            name: `${user}'s JupyterLab`,
          },
        ];
        resolve(notebooks);
      } catch (error) {
        reject(error);
      }
    });
  }
}

export default NotebookListingService;
