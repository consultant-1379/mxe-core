import { JOB_STATUS_FAILED, STATUS_ERROR } from 'utils/Enums';
import TrainingJobsService from 'services/TrainingJobsService';
import { addNotification } from 'store/actions';

/**
 * @class TrainingJobsListingService
 * @classdesc MXE Training Jobs Listing Service
 */
class TrainingJobsListingService {
  /**
   * Get models and deployments and merge them together
   * @returns {Promise<any>}
   */
  static async getTrainingJobs() {
    return new Promise(async (resolve, reject) => {
      try {
        const trainingJobs = await TrainingJobsService.getTrainingJobs();

        // trainingJobs.forEach((trainingJob) => {
        //   // Gathering notifications
        //   if (trainingJob.status === JOB_STATUS_FAILED) {
        //     store.dispatch(
        //       addNotification({
        //         title: trainingJob.id,
        //         description: trainingJob.message,
        // eslint-disable-next-line max-len
        //         navigate: `/training-package-detail/?packageId=${trainingJob.packageId}&packageVersion=${trainingJob.packageVersion}`,
        //         status: STATUS_ERROR,
        //       })
        //     );
        //   }
        // });

        resolve(trainingJobs);
      } catch (error) {
        reject(error);
      }
    });
  }
}

export default TrainingJobsListingService;
