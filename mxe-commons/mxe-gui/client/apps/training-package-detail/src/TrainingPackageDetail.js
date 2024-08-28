/**
 * TrainingPackageDetail is defined as
 * `<e-training-package-detail>`
 *
 * Imperatively create application
 * @example
 * let app = new TrainingPackageDetail();
 *
 * Declaratively create application
 * @example
 * <e-training-package-detail></e-training-package-detail>
 *
 * @extends {App}
 */
import { definition } from '@eui/component';
import { App, html } from '@eui/app';
import 'components/training-packages/package-details/src/PackageDetails';
import style from './trainingPackageDetail.css';

@definition('e-training-package-detail', {
  style,
  props: {
    packageId: { attribute: false, type: String },
    packageVersion: { attribute: false, type: String },
  },
})
export default class TrainingPackageDetail extends App {
  /**
   * Render the <e-training-package-detail> app. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <e-package-details
        .packageId="${decodeURIComponent(this.packageId)}"
        .packageVersion="${decodeURIComponent(this.packageVersion)}"
      ></e-package-details>
    `;
  }
}

/**
 * Register the component as e-training-package-detail.
 * Registration can be done at a later time and with a different name
 * Uncomment the below line to register the App if used outside the container
 */
// TrainingPackageDetail.register();
