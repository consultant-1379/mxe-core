/**
 * Component ChangeModel is defined as
 * `<e-change-model>`
 *
 * Imperatively create component
 * @example
 * let component = new ChangeModel();
 *
 * Declaratively create component
 * @example
 * <e-change-model></e-change-model>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html } from '@eui/lit-component';
import { boundMethod } from 'autobind-decorator';
import 'components/shared/model-selector-container/src/ModelSelectorContainer';
import { loc } from 'utils/Utils';
import {
  CLEAR_MODEL_SELECTION,
  CLOSE_DIALOG,
  INVOKE_UPDATE,
  MODEL_VALIDATED,
  RESET_MODEL_SELECTOR,
} from 'utils/Enums';
import ModelServiceService from 'services/ModelServiceService';
import CreateManifestService from 'services/CreateManifestService';
import style from './changeModel.css';

/**
 * @property {Object} selectedModel
 */
@definition('e-change-model', {
  style,
  home: 'change-model',
  props: {
    service: { attribute: false },
    isABTest: { attribute: false },
    selectedModels: { attribute: false, type: Array, default: [] },
    changeDisabled: { attribute: false, default: true },
  },
})
export default class ChangeModel extends LitComponent {
  didConnect() {
    window.addEventListener(CLOSE_DIALOG, this.reset, false);
    window.addEventListener(MODEL_VALIDATED, this.handleModelSelection, false);
    window.addEventListener(CLEAR_MODEL_SELECTION, this.handleModelSelection, false);
  }

  didDisconnect() {
    window.removeEventListener(CLOSE_DIALOG, this.reset, false);
    window.removeEventListener(MODEL_VALIDATED, this.handleModelSelection, false);
    window.removeEventListener(CLEAR_MODEL_SELECTION, this.handleModelSelection, false);
  }

  @boundMethod
  handleModelSelection(event) {
    this.selectedModels = event.detail;
    this.validate();
  }

  /**
   * Checks if change models button is disabled
   */
  @boundMethod
  validate() {
    if (this.isABTest && this.selectedModels.length < 2) {
      this.changeDisabled = true;
    } else {
      this.changeDisabled = !this.isABTest && this.selectedModels.length < 1;
    }
  }

  /**
   * Resets model selection
   */
  @boundMethod
  reset() {
    this.selectedModels = [];
    this.bubble(RESET_MODEL_SELECTOR);
  }

  /**
   * Closes dialog
   */
  @boundMethod
  closeDialog() {
    this.bubble(CLOSE_DIALOG);
  }

  /**
   * Returns models of service
   * @return {Object[]}
   */
  get serviceModels() {
    return this.service ? this.service.models : null;
  }

  /**
   * Sends the request for model changing
   */
  @boundMethod
  async changeModels() {
    try {
      if (!this.selectedModels) {
        return;
      }
      if (this.service.autoScaling) {
        this.scalingData = [];
        this.scalingData.autoScaling = this.service.autoScaling;
      } else {
        this.scalingData = { replicas: this.service?.replicas };
      }
      const file = await CreateManifestService.constructCreateManifest(
        this.service.name,
        this.scalingData,
        this.selectedModels
      );
      await ModelServiceService.patchModelService(this.service.name, file);
      this.bubble(CLOSE_DIALOG);
      this.bubble(INVOKE_UPDATE);
    } catch (e) {
      console.error(e);
    }
  }

  /**
   * Render the <e-change-model> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <e-model-selector-container
        id="modelSelectorContainer"
        .hideAddButton="${true}"
        .showDeleteButton="${false}"
        .isABTest="${this.isABTest}"
        .preFillData="${this.serviceModels}"
      ></e-model-selector-container>
      <div class="button-wrapper">
        <eui-base-v0-button class="button" @click="${this.closeDialog}">
          ${loc('CANCEL')}
        </eui-base-v0-button>
        <eui-base-v0-button
          class="button"
          primary
          id="change-confirm-models-model-service"
          @click="${this.changeModels}"
          slot="bottom"
          ?disabled="${this.changeDisabled}"
        >
          ${loc('CHANGE_MODEL')}s
        </eui-base-v0-button>
      </div>
    `;
  }
}

/**
 * Register the component as e-change-model.
 * Registration can be done at a later time and with a different name
 */
ChangeModel.register();
