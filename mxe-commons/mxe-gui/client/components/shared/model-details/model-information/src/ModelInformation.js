/**
 * Component ModelInformation is defined as
 * `<e-model-information>`
 *
 * Imperatively create component
 * @example
 * let component = new ModelInformation();
 *
 * Declaratively create component
 * @example
 * <e-model-information></e-model-information>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, nothing, repeat } from '@eui/lit-component';
import { formatDateToLocalDate, loc, preventDefaultEvent } from 'utils/Utils';
import { boundMethod } from 'autobind-decorator';
import { STATUS_DEPLOYMENT_ERROR, STATUS_MODEL_ERROR } from 'utils/Enums';
import style from './modelInformation.css';

/**
 * @property {Object} model - Model object
 * @property {boolean} showConfirmDialog - Boolean whether to show the confirm dialog or not
 * @property {boolean} isEdit -  Is in edit mode
 * @property {Object} defaultModelInfoValues - Default values
 * @property {boolean} hasUnsavedChanges -  Model info has unsaved changes
 * @property {boolean} instanceInputWarning -  Instance warning
 */
@definition('e-model-information', {
  style,
  home: 'model-information',
  props: {
    model: { attribute: false, type: Object, default: null },
  },
})
export default class ModelInformation extends LitComponent {
  /**
   * Get model status
   * @returns {string} - Card status
   */
  get modelStatus() {
    return this.model ? this.model.status : '';
  }

  /**
   * Get formatted date for model
   * @return {string}
   */
  get formattedDate() {
    if (this.model && this.model.created) {
      return formatDateToLocalDate(this.model.created);
    }
    return '';
  }

  /**
   * Get model author
   * @returns {string} - Card status
   */
  get modelAuthor() {
    return this.model?.author ?? '';
  }

  /**
   * Get model id
   * @returns {string} - Card status
   */
  get modelId() {
    return this.model?.id ?? '';
  }

  /**
   * Get model onboard user
   * @returns {string} - Onboarded info
   */
  get onboardedBy() {
    return this.model?.createdByUserName ?? '';
  }

  /**
   * Get model onboard user
   * @returns {string} - Onboarded info
   */
  get signedBy() {
    return this.model?.signedByName ?? '';
  }

  /**
   * Get model description
   * @returns {string} - Card status
   */
  get modelDescription() {
    return this.model?.description ?? '';
  }

  get modelImageMarkup() {
    // Error
    if (this.modelStatus === STATUS_MODEL_ERROR || this.modelStatus === STATUS_DEPLOYMENT_ERROR) {
      return html`
        <eui-v0-icon class="model-icon" size="80" name="triangle-warning"></eui-v0-icon>
      `;
    }

    // We have an icon hopefully
    if (this.model.icon !== null && this.model.icon !== '') {
      return html` <img class="model-icon" src="${this.model.icon}" alt="model icon" /> `;
    }

    // Default
    return html`
      <img class="model-icon" src="/assets/icons/standard-icon.svg" alt="model icon" />
    `;
  }

  /**
   * Render the <e-model-information> component. This function is called each time a
   * prop changes.
   */
  render() {
    if (!this.modelStatus) {
      return html`
        <div class="model-info">
          <div class="top">
            <div class="title">${loc('VERSION')} ${this.model.version}</div>
            <div class="subtitle">${loc('BASIC_INFORMATION')}</div>
          </div>
          <div class="container error">
            ${loc('ERROR_HAPPENED')}
          </div>
        </div>
      `;
    }
    return html`
      <div class="model-info">
        <div class="top">
          <div class="title">${loc('VERSION')} ${this.model.version}</div>
          <div class="subtitle">${loc('BASIC_INFORMATION')}</div>
        </div>
        <div class="container">
          <div class="image">
            ${this.modelImageMarkup}
          </div>
          <div class="wrapper">
            <div class="column onboard-info">
              <div class="column-name">${loc('AUTHOR')}:</div>
              <div class="column-value">${this.modelAuthor}</div>
              <div class="column-name">${loc('DATE_OF_ONBOARDING')}:</div>
              <div class="column-value">${this.formattedDate}</div>
            </div>
            <div class="column info-table">
              <div class="column-name">${loc('MODEL_ID')}:</div>
              <div class="column-value">${this.modelId}</div>
              <div class="column-name">${loc('ONBOARDED_BY')}:</div>
              <div class="column-value">${this.onboardedBy}</div>
            </div>
            <div class="column description">
              <div class="column-name">${loc('DESCRIPTION')}:</div>
              <div class="column-value">${this.modelDescription}</div>
              <div class="column-name">${loc('SIGNED_BY')}:</div>
              <div class="column-value">${this.signedBy}</div>
            </div>
          </div>
        </div>
      </div>
    `;
  }
}

/**
 * Register the component as e-model-information.
 * Registration can be done at a later time and with a different name
 */
ModelInformation.register();
