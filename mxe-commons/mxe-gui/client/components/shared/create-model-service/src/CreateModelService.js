/**
 * Component CreateModelService is defined as
 * `<e-create-model-service>`
 *
 * Imperatively create component
 * @example
 * let component = new CreateModelService();
 *
 * Declaratively create component
 * @example
 * <e-create-model-service></e-create-model-service>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, nothing } from '@eui/lit-component';
import { loc, getEventPath, isNumeric } from 'utils/Utils';
import { boundMethod } from 'autobind-decorator';
import { saveAs } from 'file-saver';
import {
  CLEAR_MODEL_SELECTION,
  CLOSE_DIALOG,
  INVALIDATE_FORM,
  INVOKE_UPDATE,
  MODEL_VALIDATED,
  REMOVE_MODEL,
  RESET_MODEL_SELECTOR,
  RESET_SCALING_DATA,
  SET_SCALING,
} from 'utils/Enums';
import {
  MODEL_DEPLOYMENT_NAME_REGEXP,
  MAX_NUMBER_OF_INSTANCES,
  MODEL_DEPLOYMENT_NAME_REGEXP_STRING,
} from 'utils/Defaults';

import 'components/shared/model-selector-container/src/ModelSelectorContainer';
import 'components/shared/model-tabs/src/ModelTabs';
import 'components/shared/service-scaling/src/ServiceScaling';
import ModelServiceService from 'services/ModelServiceService';
import CreateManifestService from 'services/CreateManifestService';
import style from './createModelService.css';

/**
 * @property {Boolean} propOne - show active/inactive state.
 * @property {string} propTwo - shows the "Hello World" string.
 */
@definition('e-create-model-service', {
  style,
  home: 'create-model-service',
  props: {
    models: { attribute: false, type: Object },
    serviceName: { attribute: false, type: String, default: '' },
    isNameValid: { attribute: false, type: Boolean, default: false },
    instanceInputWarning: { attribute: false, type: Boolean, default: false },
    selectedModels: { attribute: false, type: Array, default: [] },
    toggleDialog: { attribute: false },
    disableCreate: { attribute: false, type: Boolean, default: true },
    disableAddButton: { attribute: false, type: Boolean, default: true },
    scalingData: { attribute: false, type: Object, default: { replicas: 1 } },
    jsonFile: { attribute: false, type: Object },
  },
})
export default class CreateModelService extends LitComponent {
  didConnect() {
    window.addEventListener(REMOVE_MODEL, this.validate, false);
    window.addEventListener(CLOSE_DIALOG, this.reset, false);
    window.addEventListener(MODEL_VALIDATED, this.handleModelSelection, false);
    window.addEventListener(INVALIDATE_FORM, this.invalidate, false);
    window.addEventListener(CLEAR_MODEL_SELECTION, this.handleModelSelection, false);
    window.addEventListener(SET_SCALING, this.setScaling, false);
  }

  didDisconnect() {
    window.removeEventListener(REMOVE_MODEL, this.validate, false);
    window.removeEventListener(CLOSE_DIALOG, this.reset, false);
    window.removeEventListener(MODEL_VALIDATED, this.handleModelSelection, false);
    window.removeEventListener(INVALIDATE_FORM, this.invalidate, false);
    window.removeEventListener(CLEAR_MODEL_SELECTION, this.handleModelSelection, false);
    window.removeEventListener(SET_SCALING, this.setScaling, false);
  }

  didRender() {
    this.instanceInput = this.shadowRoot.getElementById('instance-input');
  }

  /**
   * Sets selected models
   * @param {Object} event
   */
  @boundMethod
  handleModelSelection(event) {
    this.selectedModels = event.detail;
    this.validate();
  }

  /**
   * Resets every field of dialog
   */
  @boundMethod
  reset() {
    this.selectedModels = [];
    this.serviceName = '';
    this.jsonFile = '';
    this.disableCreate = true;
    this.scalingData = { type: 'manual', replicas: 1 };
    const scalingAccordion = this.shadowRoot.getElementById('scaling-accordion');
    scalingAccordion.open = false;
    this.bubble(RESET_MODEL_SELECTOR);
    this.bubble(RESET_SCALING_DATA);
  }

  /**
   * Check event for input max length
   * @param event
   */
  maxLengthCheck(event) {
    const input = getEventPath(event);

    if (input.value > MAX_NUMBER_OF_INSTANCES) {
      input.value = MAX_NUMBER_OF_INSTANCES;
    }
  }

  /**
   * Handles name input changes
   * @param {Object} event
   */
  @boundMethod
  handleInput(event) {
    const { value } = getEventPath(event);

    this.isNameValid = MODEL_DEPLOYMENT_NAME_REGEXP.test(value);
    if (this.isNameValid) {
      this.serviceName = value;
    }
    this.validate();
  }

  /**
   * Checks if create button is disabled
   */
  @boundMethod
  validate() {
    let metricHasAllValues = true;
    if (this.scalingData.autoScaling) {
      metricHasAllValues =
        this.scalingData.autoScaling.minReplicas &&
        this.scalingData.autoScaling.maxReplicas &&
        this.scalingData.autoScaling.metrics[0].name &&
        this.scalingData.autoScaling.metrics[0].targetAverageValue;
    }

    this.disableCreate = !(
      this.isNameValid &&
      this.serviceName.length > 0 &&
      this.selectedModels.length > 0 &&
      metricHasAllValues
    );
    if (!this.disableCreate) {
      // this.constructManifest();
    }
  }

  /* Biggest cahnge in GUI create a function to create the json file step
   by step based on user input data */

  /**
   * Sets scaling
   * @param {Object} event
   */
  @boundMethod
  setScaling(event) {
    if (!event.detail) {
      return;
    }
    this.scalingData = event.detail;

    this.validate();
  }

  /**
   * Sets create button to disabled
   */
  @boundMethod
  invalidate() {
    this.disableCreate = true;
  }

  /**
   * Closes dialog
   */
  @boundMethod
  closeDialog() {
    this.bubble(CLOSE_DIALOG);
  }

  /**
   * Creates the model service
   */
  @boundMethod
  async createModelService() {
    const file = await CreateManifestService.constructCreateManifest(
      this.serviceName,
      this.scalingData,
      this.selectedModels
    );
    await ModelServiceService.postModelServiceWithinput(file);
    await this.closeDialog();
    await this.reset();
    await this.bubble(INVOKE_UPDATE);
  }

  // Usage example:

  @boundMethod
  async downloadManifest() {
    this.jsonFile = await CreateManifestService.constructCreateManifest(
      this.serviceName,
      this.scalingData,
      this.selectedModels
    );
    saveAs(this.jsonFile, `${this.serviceName}.yml`);
  }

  /**
   * Render the <e-create-model-service> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`<div class="create-modal-service">
      <eui-base-v0-accordion category-title="Basic information" line open>
        <div class="input-wrapper">
          <eui-base-v0-text-field
            id="model-service-name"
            class="service-name"
            labelText="${loc('MODEL_SERVICE_NAME')}"
            fullwidth
            placeholder="${loc('MODEL_SERVICE_VALIDATION_TEXT')}"
            pattern="${MODEL_DEPLOYMENT_NAME_REGEXP_STRING}"
            @input=${this.handleInput}
            .value="${this.serviceName}"
            custom-validation="${loc('MODEL_SERVICE_VALIDATION_TEXT')}"
          ></eui-base-v0-text-field>
        </div>

        <div class="subtitle">${loc('SELECT_MODEL')}</div>
        <e-model-selector-container
          id="modelSelectorContainer"
          .disableAddButton="${this.disableAddButton}"
        ></e-model-selector-container>
      </eui-base-v0-accordion>
      <eui-base-v0-accordion id="scaling-accordion" category-title="Scaling" line>
        <e-service-scaling></e-service-scaling>
      </eui-base-v0-accordion>
      <div class="button-wrapper">
        <eui-base-v0-button
          class="button"
          primary
          @click="${this.downloadManifest}"
          ?disabled="${this.disableCreate}"
          >${loc('Download Manifest')}
        </eui-base-v0-button>
      </div>
      <div class="button-wrapper">
        <eui-base-v0-button class="button" @click="${this.closeDialog}">
          ${loc('CANCEL')}
        </eui-base-v0-button>
        <eui-base-v0-button
          class="button"
          primary
          id="create-model-service"
          @click="${this.createModelService}"
          ?disabled="${this.disableCreate}"
          >${loc('CREATE')}
        </eui-base-v0-button>
      </div>
    </div>`;
  }
}

/**
 * Register the component as e-create-model-service.
 * Registration can be done at a later time and with a different name
 */
CreateModelService.register();
