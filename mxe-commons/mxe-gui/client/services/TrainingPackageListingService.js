import { JOB_STATUS_FAILED, STATUS_ERROR } from 'utils/Enums';
import TrainingPackagesService from 'services/TrainingPackagesService';
import { addNotification } from 'store/actions';

/**
 * @class TrainingPackageListingService
 * @classdesc MXE Training Package Listing Service
 */
class TrainingPackageListingService {
  /**
   * Get models and deployments and merge them together
   * @returns {Promise<any>}
   */
  static async getTrainingPackages(trainingPackageId = null) {
    return new Promise(async (resolve, reject) => {
      try {
        const trainingPackages = await TrainingPackagesService.getTrainingPackages();

        /**
         * Merged models with deployment
         * @type {Object}
         */
        const merged = {};

        const addTrainingPackage = (model) => {
          // Creating groups by name
          if (Array.isArray(merged[model.id])) {
            merged[model.id].push({ ...model });
          } else {
            merged[model.id] = [{ ...model }];
          }
        };

        // Loop through models
        for (let i = 0, { length } = trainingPackages; i < length; i++) {
          if (trainingPackageId && trainingPackages[i].id !== trainingPackageId) {
            // eslint-disable-next-line no-continue
            continue;
          }
          const trainingPackage = trainingPackages[i];

          addTrainingPackage(trainingPackage);

          // Gathering notifications
          // if (trainingPackage.status === STATUS_ERROR) {
          //   store.dispatch(
          //     addNotification({
          //       title: trainingPackage.title,
          //       description: trainingPackage.description,
          // eslint-disable-next-line max-len
          //       navigate: `/training-package-detail/?packageId=${trainingPackage.id}&packageVersion=${trainingPackage.version}`,
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

export default TrainingPackageListingService;
