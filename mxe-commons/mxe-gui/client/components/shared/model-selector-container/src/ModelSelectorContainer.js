/**
 * Component ModelSelectorContainer is defined as
 * `<e-model-selector-container>`
 *
 * Imperatively create component
 * @example
 * let component = new ModelSelectorContainer();
 *
 * Declaratively create component
 * @example
 * <e-model-selector-container></e-model-selector-container>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, nothing } from '@eui/lit-component';
import 'components/shared/model-selector/src/ModelSelector';
import { boundMethod } from 'autobind-decorator';
import {
  INVALIDATE_FORM,
  MODEL_SELECTION,
  MODEL_VALIDATED,
  REMOVE_MODEL,
  RESET_MODEL_SELECTOR,
} from 'utils/Enums';
import { loc } from 'utils/Utils';
import style from './modelSelectorContainer.css';

/**
 * @property {Boolean} propOne - show active/inactive state.
 * @property {string} propTwo - shows the "Hello World" string.
 */
@definition('e-model-selector-container', {
  style,
  home: 'model-selector-container',
  props: {
    disableAddButton: { attribute: false },
    hideAddButton: { attribute: false },
    showDeleteButton: { attribute: false, default: true },
    preFillData: { attribute: false, default: null },
    isMultipleModels: { attribute: false, default: false },
    isABTest: { attribute: false, default: false },
    selectedModels: { attribute: false, default: [] },
  },
})
export default class ModelSelectorContainer extends LitComponent {
  didConnect() {
    window.addEventListener(REMOVE_MODEL, this.removeModel, false);
    window.addEventListener(RESET_MODEL_SELECTOR, this.reset, false);
    window.addEventListener(MODEL_SELECTION, this.handleModelSelection, false);
  }

  didDisconnect() {
    window.removeEventListener(REMOVE_MODEL, this.removeModel, false);
    window.removeEventListener(RESET_MODEL_SELECTOR, this.reset, false);
    window.removeEventListener(MODEL_SELECTION, this.handleModelSelection, false);
    this.firstModel = null;
  }

  /**
   * Removes selected model
   */
  @boundMethod
  removeModel() {
    this.isMultipleModels = false;
    if (this.selectedModels.length > 0) {
      this.selectedModels[0].weight = 1;
      this.selectedModels.splice(1, 1);
    }
    this.validate();
  }

  /**
   * Resets model selector
   */
  @boundMethod
  reset() {
    this.isMultipleModels = false;
    this.selectedModels = [];
  }

  /**
   * Handles model selection
   * @param {Object} event
   */
  @boundMethod
  handleModelSelection(event) {
    this.firstModel = this.shadowRoot.getElementById('firstModel');
    const { detail } = event;
    const { weight, index, id, version } = detail;

    if (index >= 0 && id && version) {
      this.selectedModels[index] = { id, version };
    }
    if (weight && this.selectedModels.length > 0) {
      this.selectedModels[0].weight = ((100 - weight) / 100).toFixed(2);
      if (this.selectedModels[index]) {
        this.selectedModels[index] = {
          ...this.selectedModels[index],
          weight: (weight / 100).toFixed(2),
        };
        this.firstModel?.setWeight(100 - weight);
      }
    }

    this.validate();
  }

  /**
   * validates models
   */
  @boundMethod
  validate() {
    const isValid = [];
    this.selectedModels.forEach((model, i) => {
      isValid[i] = model.id && model.version;
    });

    if (
      (isValid.length === 1 && isValid[0]) ||
      (isValid.length === 2 && isValid[0] && isValid[1])
    ) {
      this.bubble(MODEL_VALIDATED, this.selectedModels);
    }
  }

  /**
   * Returns second model selector markup
   * @return {*}
   */
  get secondModelMarkup() {
    if (!this.isMultipleModels && !this.isABTest) {
      return nothing;
    }

    return html`
      <e-model-selector
        id="second-model"
        .index="${1}"
        .multiple="${true}"
        .preFillData="${this.preFillData}"
        .showDeleteButton="${this.showDeleteButton}"
      ></e-model-selector>
    `;
  }

  /**
   * Returns add second model button
   * @return {*}
   */
  get addButton() {
    if (this.isMultipleModels || this.hideAddButton) {
      return nothing;
    }

    return html`
      <eui-base-v0-button
        class="button"
        icon="plus"
        @click="${() => {
          this.isMultipleModels = true;
          this.selectedModels[1] = { id: null, version: null, weight: null };
          this.bubble(INVALIDATE_FORM);
        }}"
      >
        ${loc('ADD_ANOTHER')}
      </eui-base-v0-button>
    `;
  }

  /**
   * Render the <e-model-selector-container> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <e-model-selector
        id="first-model"
        .index="${0}"
        .preFillData="${this.preFillData}"
        .multiple="${this.isMultipleModels || this.isABTest}"
      ></e-model-selector>
      ${this.addButton} ${this.secondModelMarkup}
    `;
  }
}

/**
 * Register the component as e-model-selector-container.
 * Registration can be done at a later time and with a different name
 */
ModelSelectorContainer.register();
