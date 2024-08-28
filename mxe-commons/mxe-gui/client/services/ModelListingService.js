import ModelService from 'services/ModelService';
import PermissionService from 'services/PermissionService';

/**
 * @class ModelService
 * @classdesc MXE Model Service
 */
class ModelListingService {
  /**
   * Get models and deployments and merge them together
   * @returns {Promise<any>}
   */
  static async getModels(filterByStatus = false, hideModelsWithoutPermission = false) {
    return new Promise(async (resolve, reject) => {
      try {
        const models = await ModelService.getModels();

        /**
         * Merged models with deployment
         * @type {Object}
         */
        const merged = {};

        const addModel = (model) => {
          if (filterByStatus && model.status !== filterByStatus) {
            return;
          }
          // Creating groups by name
          if (Array.isArray(merged[model.id])) {
            merged[model.id].push({ ...model });
          } else {
            merged[model.id] = [{ ...model }];
          }
        };

        // Loop through models
        for (let i = 0, modelsLength = models.length; i < modelsLength; i++) {
          const model = models[i];

          addModel(model);

          // // Gathering notifications
          // if (model.status === STATUS_ERROR) {
          //   store.dispatch(
          //     addNotification({
          //       title: model.title,
          //       description: model.description,
          //       navigate: `/model-info/?modelId=${model.id}&modelVersion=${model.version}`,
          //       status: STATUS_ERROR,
          //     })
          //   );
          // }
        }

        /**
         * Create [key, value] pair array from the merged object
         * @type {[string, any][]}
         */
        const result = Object.entries(merged);

        resolve(result);
      } catch (error) {
        reject(error);
      }
    });
  }
}

export default ModelListingService;
