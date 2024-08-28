/**
 * Component ModelDetails is defined as
 * `<e-model-details>`
 *
 * Imperatively create component
 * @example
 * let component = new ModelDetails();
 *
 * Declaratively create component
 * @example
 * <e-model-details></e-model-details>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent, nothing } from '@eui/lit-component';
import { boundMethod } from 'autobind-decorator';
import '../model-information/src/ModelInformation';
import '../contained-services/src/ContainedServices';
import ModelService from 'services/ModelService';
import { INVOKE_UPDATE, STATUS_AVAILABLE, STATUS_ERROR, STATUS_PACKAGING } from 'utils/Enums';
import { loc, preventDefaultEvent } from 'utils/Utils';
import ModelListingService from 'services/ModelListingService';
import ModelServiceService from 'services/ModelServiceService';
import PermissionService from 'services/PermissionService';
import { toModelCatalogue } from 'utils/Navigator';
import 'components/shared/error-screen/src/ErrorScreen';
import style from './modelDetails.css';

/**
 * @property {string} modelId - Model id
 * @property {string} modelVersion - Model version
 * @property {Object} model - Merged model
 * @property {Array} services - Services using the current model
 * @property {Boolean} showConfirmDeleteDialog - Whether to show the delete confirmation or not
 * @property {Boolean} error - Error happened
 * @property {Boolean} isLoading - Request are loading
 */
@definition('e-model-details', {
  style,
  home: 'model-details',
  props: {
    modelId: { attribute: false, type: String, default: '' },
    modelVersion: { attribute: false, type: String, default: '' },
    model: { attribute: false, type: Object, default: null },
    services: { attribute: false, type: Array, default: [] },
    showConfirmDeleteDialog: { attribute: false, type: Boolean, default: false },
    error: { attribute: false, type: Boolean, default: false },
    isLoading: { attribute: false, type: Boolean, default: true },
  },
})
export default class ModelDetails extends LitComponent {
  async didConnect() {
    await this.getModel();
  }

  didChangeProps(props) {
    if (props.has('modelId') || props.has('modelVersion')) {
      this.getModel();
    }
  }

  /**
   * Get corresponding model
   * @return {Promise<void>}
   */
  @boundMethod
  async getModel() {
    this.isLoading = true;
    this.error = false;
    try {
      const models = await ModelListingService.getModels();
      const isIdExist = models.find((model) => model[0] === this.modelId);
      if (isIdExist) {
        this.model = isIdExist[1].find((model) => model.version === this.modelVersion);
        if (this.model) {
          await this.getServices();
        }
      }

      this.error = !this.model;
      this.isLoading = false;
    } catch (e) {
      console.error(e);
      this.error = true;
    }
  }

  /**
   * Get services containing the model
   * @return {Promise<void>}
   */
  @boundMethod
  async getServices() {
    try {
      const services = await ModelServiceService.getModelServices();
      const containedServices = await ModelServiceService.getModelServiceByModel(
        this.model.id,
        this.model.version
      );

      const result = [];

      containedServices.forEach((containedService) => {
        const hasAccess = services.find((service) => service.name === containedService.name);
        if (hasAccess) {
          result.push(hasAccess);
        } else {
          result.push({ ...containedService, noAccess: true });
        }
      });

      this.services = result;
    } catch (e) {
      console.error(e);
    }
  }

  /**
   * Get model status
   * @returns {string} - Card status
   */
  get modelStatus() {
    return this.model ? this.model.status : '';
  }

  /**
   * Get model name
   * @returns {string} - Model name
   */
  get modelName() {
    return this.model.displayName || this.model.title;
  }

  /**
   * Toggles confirm delete dialog
   * @param event
   */
  @boundMethod
  toggleConfirmDeleteDialog(event) {
    if (event) {
      preventDefaultEvent(event);
    }
    this.showConfirmDeleteDialog = !this.showConfirmDeleteDialog;
  }

  /**
   * Deletes model after model delete dialog confirmed
   */
  @boundMethod
  async deleteModel() {
    this.toggleConfirmDeleteDialog();
    try {
      await ModelService.deleteModel(this.model.id, this.model.version);
      this.model.message = loc('SUCCESSFULLY_DELETED');
      this.bubble(INVOKE_UPDATE);
      toModelCatalogue();
    } catch (error) {
      console.error('Model delete error: ', error);
    }
  }

  /**
   * Delete button markup with actions included
   * @return {html | nothing}
   */
  get deleteButton() {
    return html`
      <eui-base-v0-button
        warning
        id="delete-${this.modelId}-${this.modelVersion}"
        icon="trashcan"
        @click="${(e) => {
          preventDefaultEvent(e);
          this.toggleConfirmDeleteDialog();
        }}"
        >${loc('BUTTON_DELETE')}</eui-base-v0-button
      >
    `;
  }

  /**
   * Status pill markup
   * @return {*}
   */
  get statusPill() {
    if (!this.modelStatus) {
      return nothing;
    }

    let icon = '';
    let color = '';
    let rotating = false;
    switch (this.modelStatus) {
      case STATUS_AVAILABLE:
        icon = 'check';
        color = 'var(--green)';
        break;
      case STATUS_ERROR:
        icon = 'cross';
        color = 'var(--red)';
        break;
      case STATUS_PACKAGING:
        icon = 'dial';
        color = 'var(--orange)';
        rotating = true;
        break;
      default:
        break;
    }

    return html`
      <eui-base-v0-pill class="pill"
        ><eui-v0-icon
          class="${this.modelStatus} ${rotating ? 'rotating' : ''}"
          name="${icon}"
          color="${color}"
        ></eui-v0-icon>
        <span>${loc(this.modelStatus.toUpperCase())}</span>
      </eui-base-v0-pill>
    `;
  }

  /**
   * Error message markup
   * @return {*}
   */
  get errorMessage() {
    if (!this.model.message) {
      return nothing;
    }

    return html`<div class="error-message">
      <img src="/assets/icons/error-icon.svg" alt="training model status" />
      <div class="message">${this.model.message}</div>
    </div>`;
  }

  get confirmDialog() {
    if (!this.showConfirmDeleteDialog) {
      return nothing;
    }

    return html`<eui-base-v0-dialog
      class="confirm-dialog delete"
      label=${loc('DIALOG_CONFIRM_TITLE')}
      no-cancel
      show
    >
      <div slot="content" class="details">
        <span>${loc('DIALOG_CONFIRM_MODEL_DELETE')}</span>
      </div>
      <eui-base-v0-button slot="bottom" @click=${this.toggleConfirmDeleteDialog}>
        ${loc('CANCEL')}
      </eui-base-v0-button>
      <eui-base-v0-button
        slot="bottom"
        warning
        id="delete-confirm-${this.modelId}-${this.modelVersion}"
        @click=${this.deleteModel}
      >
        ${loc('BUTTON_DELETE')}
      </eui-base-v0-button>
    </eui-base-v0-dialog>`;
  }

  /**
   * Render the <e-model-details> component. This function is called each time a
   * prop changes.
   */
  render() {
    if (this.isLoading) {
      return html` <eui-base-v0-loader></eui-base-v0-loader> `;
    }

    if (this.error) {
      return html` <e-error-screen .title="${loc('ERROR_HAPPENED')}"></e-error-screen> `;
    }

    return html`
      <div class="top">
        <div class="title">${this.modelName}</div>
        ${this.statusPill}
        <div class="button-group">
          ${this.deleteButton}
        </div>
      </div>
      ${this.errorMessage}
      <div class="container">
        <e-model-information .model="${this.model}"></e-model-information>
        <e-contained-services .services="${this.services}"></e-contained-services>
      </div>
      ${this.confirmDialog}
    `;
  }
}

/**
 * Register the component as e-model-details.
 * Registration can be done at a later time and with a different name
 */
ModelDetails.register();
