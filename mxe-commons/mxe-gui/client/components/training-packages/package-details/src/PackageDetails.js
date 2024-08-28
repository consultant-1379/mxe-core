/**
 * Component PackageDetails is defined as
 * `<e-package-details>`
 *
 * Imperatively create component
 * @example
 * let component = new PackageDetails();
 *
 * Declaratively create component
 * @example
 * <e-package-details></e-package-details>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, nothing } from '@eui/lit-component';
import { Tile } from '@eui/layout';
import { formatDateToLocalDate, loc } from 'utils/Utils';
import TrainingPackagesService from 'services/TrainingPackagesService';
import 'components/training-jobs/job-table/src/JobTable.js';
import 'components/shared/error-screen/src/ErrorScreen';
import TrainingJobsService from 'services/TrainingJobsService';
import { toTrainingPackage } from 'utils/Navigator';
import { boundMethod } from 'autobind-decorator';
import style from './packageDetails.css';

/**
 * @property {string} packageId - package id from url
 * @property {string} packageVersion - package version from url
 * @property {Object} package - package object
 * @property {Array} jobs - job list array
 * @property {boolean} error - Error state
 * @property {boolean} isLoading - Loading state
 */
@definition('e-package-details', {
  style,
  home: 'package-details',
  props: {
    packageId: { attribute: false, type: String },
    packageVersion: { attribute: false, type: String },
    package: { attribute: false, type: Object },
    jobs: { attribute: false, type: Array },
    error: { attribute: false, default: false },
    isLoading: { attribute: false, default: false },
  },
})
export default class PackageDetails extends LitComponent {
  async didConnect() {
    if (this.packageId && this.packageVersion) {
      await this.fetchDetails();
    }
  }

  async didChangeProps(props) {
    if (props.get('packageVersion') !== this.packageVersion && props.has('packageVersion')) {
      await this.fetchDetails();
    }
  }

  /**
   * Gets jobs and packages through requests
   */
  @boundMethod
  async fetchDetails() {
    this.error = false;
    this.isLoading = true;
    try {
      this.package = await TrainingPackagesService.getTrainingPackagesByIdAndVersion(
        this.packageId,
        this.packageVersion
      );

      this.jobs = await TrainingJobsService.getTrainingJobByPackageIdVersion(
        this.packageId,
        this.packageVersion
      );
      this.isLoading = false;
    } catch (e) {
      this.error = true;
      this.isLoading = false;
    }
  }

  /**
   * Returns title if package exists
   * @return {string}
   */
  get packageTitle() {
    return this.package ? `${this.package.title} ${this.package.version}` : '';
  }

  /**
   * Returns status if package exists
   * @return {string}
   */
  get packageStatus() {
    return this.package ? this.package.status : '';
  }

  /**
   * Get model icon, it's either plain img, or eui sdk icon
   * @return {*}
   */
  get packageIcon() {
    if (!this.package) {
      return nothing;
    }
    // We have an icon hopefully
    if (this.package.icon !== null && this.package.icon !== '') {
      return html` <img class="model-icon" src="${this.package.icon}" alt="package icon" /> `;
    }

    // Default
    return html`
      <img class="model-icon" src="/assets/icons/standard-icon.svg" alt="package icon" />
    `;
  }

  /**
   * Returns filed of package
   * @param {string} field
   * @return {string}
   */
  getPackageField(field) {
    return this.package ? this.package[field] : '';
  }

  /**
   * Executes package
   */
  @boundMethod
  async executePackage() {
    const { id, version } = this.package;
    await TrainingJobsService.postTrainingJob(id, version);
    this.jobs = await TrainingJobsService.getTrainingJobByPackageIdVersion(id, version);
  }

  /**
   * Deletes package
   */
  @boundMethod
  async deletePackage() {
    const { id, version } = this.package;
    await TrainingPackagesService.deleteTrainingPackage(id, version);
    toTrainingPackage();
  }

  /**
   * Render the <e-package-details> component. This function is called each time a
   * prop changes.
   */
  render() {
    if (this.error) {
      return html` <e-error-screen .title="${loc('ERROR_HAPPENED')}"></e-error-screen> `;
    }
    return html`
      <eui-layout-v0-tile class="tile" tile-title="${this.packageTitle}">
        <div class="content" slot="content">
          <div class="section package-info">
            <div class="title">${loc('BASIC_INFORMATION')}</div>
            <div class="container">
              ${this.packageIcon}
              <div class="info-table">
                <div class="info-cell">
                  <div class="cell-title">${loc('AUTHOR')}</div>
                  <div class="cell-content">${this.getPackageField('author')}</div>
                </div>
                <div class="info-cell">
                  <div class="cell-title">${loc('TITLE')}</div>
                  <div class="cell-content">${this.getPackageField('title')}</div>
                </div>
                <div class="info-cell">
                  <div class="cell-title">${loc('STATUS')}</div>
                  <div class="cell-content">${this.getPackageField('status')}</div>
                </div>
                <div class="info-cell">
                  <div class="cell-title">${loc('DATE_OF_ONBOARDING')}</div>
                  <div class="cell-content">
                    ${formatDateToLocalDate(this.getPackageField('created'))}
                  </div>
                </div>
                <div class="info-cell">
                  <div class="cell-title">${loc('VERSION')}</div>
                  <div class="cell-content">${this.getPackageField('version')}</div>
                </div>
                <div class="info-cell">
                  <div class="cell-title">${loc('ID')}</div>
                  <div class="cell-content">${this.getPackageField('id')}</div>
                </div>
              </div>
              <div class="description">
                <div class="title">${loc('DESCRIPTION')}</div>
                <div>${this.getPackageField('description')}</div>
              </div>
            </div>
          </div>
          <div class="section package-jobs">
            <div class="title">
              ${loc('EXECUTION_HISTORY')}
              <span class="subtitle"></span>
            </div>
            <e-job-table .jobs="${this.jobs}" .showDetails="${this.showDetails}"></e-job-table>
          </div>
        </div>
        <div class="action" slot="action">
          <eui-base-v0-button class="onboard" @click="${this.deletePackage}" icon="trashcan">
            ${loc('DELETE')}
          </eui-base-v0-button>
          <eui-base-v0-button
            class="onboard"
            primary
            @click="${this.executePackage}"
            icon="video-play"
          >
            ${loc('EXECUTE')}
          </eui-base-v0-button>
        </div>
      </eui-layout-v0-tile>
    `;
  }
}

/**
 * Register the component as e-package-details.
 * Registration can be done at a later time and with a different name
 */
PackageDetails.register();
