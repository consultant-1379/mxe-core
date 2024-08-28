/**
 * TrainingJobs is defined as
 * `<e-training-jobs>`
 *
 * Imperatively create application
 * @example
 * let app = new TrainingJobs();
 *
 * Declaratively create application
 * @example
 * <e-training-jobs></e-training-jobs>
 *
 * @extends {App}
 */
import { definition } from '@eui/component';
import { App, html } from '@eui/app';
import { MultiPanelTile } from '@eui/layout';
import 'components/training-jobs/job-container/src/JobContainer';
import style from './trainingJobs.css';

@definition('e-training-jobs', {
  style,
  props: {
    response: { attribute: false },
  },
})
export default class TrainingJobs extends App {
  /**
   * Render the <e-training-jobs> app. This function is called each time a
   * prop changes.
   */
  render() {
    return html` <e-job-container></e-job-container> `;
  }
}

/**
 * Register the component as e-training-jobs.
 * Registration can be done at a later time and with a different name
 * Uncomment the below line to register the App if used outside the container
 */
// TrainingJobs.register();
