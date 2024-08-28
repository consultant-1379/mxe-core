/**
 * Component ModelSelector is defined as
 * `<e-model-selector>`
 *
 * Imperatively create component
 * @example
 * let component = new ModelSelector();
 *
 * Declaratively create component
 * @example
 * <e-model-selector></e-model-selector>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, nothing } from '@eui/lit-component';
import { loc, preventDefaultEvent, getEventPath, evaluateNumberInput } from 'utils/Utils';
import ModelListingService from 'services/ModelListingService';
import { boundMethod } from 'autobind-decorator';
import {
  CLEAR_MODEL_SELECTION,
  MODEL_SELECTION,
  OPEN_CHANGE_MODEL_DIALOG,
  REMOVE_MODEL,
  RESET_MODEL_SELECTOR,
  STATUS_AVAILABLE,
} from 'utils/Enums';
import style from './modelSelector.css';

/**
 * @property {Boolean} propOne - show active/inactive state.
 * @property {string} propTwo - shows the "Hello World" string.
 */
@definition('e-model-selector', {
  style,
  home: 'model-selector',
  props: {
    models: { attribute: false, type: Array },
    selectedModel: { attribute: false, default: null },
    selectedVersion: { attribute: false, default: null },
    selectedWeight: { attribute: false, default: 50 },
    weight: { attribute: false, type: Number, default: 100 },
    index: { attribute: false, type: Number, default: 0 },
    multiple: { attribute: false, type: Boolean, default: false },
    modelData: { attribute: false, type: Object, default: null },
    showDeleteButton: { attribute: false, type: Boolean, default: true },
    preFillData: { attribute: false },
    // availableVersions: { attribute: false, type: Array, default: [] },
  },
})
export default class ModelSelector extends LitComponent {
  async didConnect() {
    window.addEventListener(OPEN_CHANGE_MODEL_DIALOG, this.init, false);
    window.addEventListener(RESET_MODEL_SELECTOR, this.reset, false);
    this.models = await ModelListingService.getModels(STATUS_AVAILABLE, true);
  }

  didDisconnect() {
    window.removeEventListener(OPEN_CHANGE_MODEL_DIALOG, this.init, false);
    window.removeEventListener(RESET_MODEL_SELECTOR, this.reset, false);
    this.selectedModel = [];
  }

  @boundMethod
  async init() {
    this.selectedModel = null;
    this.models = await ModelListingService.getModels(STATUS_AVAILABLE, true);
    this.preFill();
  }

  /**
   * Resets combobox
   */
  @boundMethod
  reset() {
    this.clearComboBox(null, 'id-selector', true);
  }

  /**
   * Prefills data if available
   */
  @boundMethod
  preFill() {
    if (this.preFillData) {
      const currentData = this.preFillData[this.index];
      const model = [currentData.id, [{ ...currentData }]];

      const { version } = currentData;
      let { weight } = currentData;
      weight = Math.round(weight * 100);

      this.selectId(null, model, currentData.availableVersions);
      this.selectVersion(null, version);
      this.editWeight(null, weight);
      this.setWeight(weight);
    }
  }

  /**
   * Edits weight of model
   * @param {Object} event
   * @param {boolean} preFill
   */
  @boundMethod
  editWeight(event, preFill = false) {
    const { value } = getEventPath(event);
    if (preFill) {
      this.selectedWeight = preFill;
    } else if (value) {
      this.selectedWeight = value;
    }

    this.bubble(MODEL_SELECTION, {
      id: this.selectedModel[0],
      version: this.selectedVersion,
      weight: this.selectedWeight,
      index: this.index,
    });
  }

  /**
   * Sets weight of model
   * @param {number} weight
   */
  @boundMethod
  setWeight(weight) {
    const weightInput = this.shadowRoot.getElementById('weight');
    if (weightInput) {
      this.selectedWeight = weight;
      weightInput.value = weight;
    }
  }

  /**
   * Removes model
   */
  @boundMethod
  removeModel() {
    this.bubble(REMOVE_MODEL);
  }

  /**
   * Return markup for delete button
   * @return {*}
   */
  get deleteButton() {
    if (!this.showDeleteButton || this.index === 0) {
      return nothing;
    }
    return html`
      <eui-v0-icon class="icon" name="trashcan" @click="${this.removeModel}"></eui-v0-icon>
    `;
  }

  /**
   * Returns title markup
   * @return {*}
   */
  get title() {
    return html`
      <div class="header">
        ${!this.index
          ? html`
              <div class="title">${loc('MODEL')}</div>
              <div class="right">
                <div class="title version">${loc('VERSION')}</div>
                ${this.multiple
                  ? html` <div class="title weight">${loc('WEIGHT')}</div> `
                  : nothing}
              </div>
            `
          : nothing}
      </div>
    `;
  }

  /**
   * Clears model selection
   * @param {Object} event
   * @param {string} field
   * @param {boolean} force
   */
  @boundMethod
  clearComboBox(event, field, force = false) {
    if (!this.selectedModel && !force) {
      return;
    }
    preventDefaultEvent(event);
    const idSelector = this.shadowRoot.getElementById('id-selector');
    const versionSelector = this.shadowRoot.getElementById('version-selector');
    if (field === 'version-selector') {
      this.selectedVersion = null;
      this.selectedModel.version = null;
      const [id, versions] = this.selectedModel;
      this.availableVersions = versions;

      versionSelector.value = '';
      versionSelector.hide();

      this.bubble(MODEL_SELECTION, {
        id: this.selectedModel[0],
        version: '',
        weight: '',
        index: this.index,
      });
    }
    if (field === 'id-selector') {
      this.selectedVersion = null;
      this.selectedModel = null;
      idSelector.value = '';
      if (versionSelector) {
        versionSelector.value = '';
      }
      versionSelector.hide();

      this.bubble(MODEL_SELECTION, {
        id: '',
        version: '',
        weight: this.selectedWeight,
        index: this.index,
      });
    }
    this.bubble(CLEAR_MODEL_SELECTION);
  }

  /**
   * Selects the id of model
   * @param {Object} event
   * @param {Object} model
   * @param {Object[]} availableVersions
   */
  @boundMethod
  selectId(event, model, availableVersions = null) {
    preventDefaultEvent(event);
    if (model) {
      const idSelector = this.shadowRoot.getElementById('id-selector');
      const versionSelector = this.shadowRoot.getElementById('version-selector');
      const [id, versions] = model;
      const sortedByVersions = [...versions].sort((a, b) => a.version.localeCompare(b.version));
      const latestVersion = [...sortedByVersions].pop();
      this.selectedVersion = latestVersion.version;
      idSelector.value = id;
      idSelector.hide();

      if (availableVersions) {
        this.availableVersions = availableVersions;
        this.selectedModel = [id, availableVersions];
      } else {
        this.availableVersions = [...versions];
        this.selectedModel = [id, versions];
      }

      setTimeout(() => {
        versionSelector.value = latestVersion.version;
        versionSelector.hide();
      }, 50);

      this.bubble(MODEL_SELECTION, {
        id: this.selectedModel[0],
        version: this.selectedVersion,
        weight: this.selectedWeight,
        index: this.index,
      });
    }
  }

  /**
   * Selects the verion of the model
   * @param {Object} event
   * @param {Object} version
   */
  @boundMethod
  selectVersion(event, version) {
    preventDefaultEvent(event);
    if (version) {
      const versionSelector = this.shadowRoot.getElementById('version-selector');
      this.selectedVersion = version;

      setTimeout(() => {
        versionSelector.value = version;
        versionSelector.hide();
      });

      const modelSelection = {
        id: this.selectedModel[0],
        version: this.selectedVersion,
        index: this.index,
      };
      if (this.multiple) {
        modelSelection.weight = this.selectedWeight;
      }
      this.bubble(MODEL_SELECTION, modelSelection);
    }
  }

  /**
   * Returns id combo box markup
   * @return {*}
   */
  get idSelectorMarkup() {
    if (!this.models) {
      return nothing;
    }

    return html`
      <eui-base-v0-combo-box
        id="id-selector"
        width="340px"
        .placeholder="${loc('SELECT_MODEL_ID')}"
        @click="${(e) => this.clearComboBox(e, 'id-selector')}"
      >
        ${this.models.length > 0
          ? this.models.map(
              (model, i) => html`
                <eui-base-v0-menu-item
                  .label="${model[0]}"
                  tabindex="${i}"
                  @click="${(e) => this.selectId(e, model)}"
                >
                </eui-base-v0-menu-item>
              `
            )
          : html`
              <eui-base-v0-menu-item
                tabindex="0"
                .label="${loc('NO_RESULT')}"
              ></eui-base-v0-menu-item>
            `}
      </eui-base-v0-combo-box>
    `;
  }

  /**
   * Return version combo box markup
   * @return {*}
   */
  get versionSelectorMarkup() {
    if (!this.models) {
      return nothing;
    }

    return html`
      <eui-base-v0-combo-box
        id="version-selector"
        .placeholder="${loc('SELECT_MODEL_VERSION')}"
        ?disabled="${!this.selectedModel || this.selectedModel.length === 0}"
        @click="${(e) => this.clearComboBox(e, 'version-selector')}"
        @input="${this.filterVersions}"
      >
        ${this.availableVersions && this.availableVersions.length > 0
          ? this.availableVersions.map(
              (model, i) => html`
                <eui-base-v0-menu-item
                  .label=" ${model.version}"
                  tabindex="${i}"
                  @click="${(e) => this.selectVersion(e, model.version)}"
                >
                </eui-base-v0-menu-item>
              `
            )
          : html``}
      </eui-base-v0-combo-box>
    `;
  }

  /**
   * Filters the versions
   */
  @boundMethod
  filterVersions() {
    const versionSelector = this.shadowRoot.getElementById('version-selector');
    this.availableVersions = [...this.selectedModel[1]];

    if (this.selectedModel) {
      this.availableVersions = this.selectedModel[1].filter((model) =>
        model.version.includes(versionSelector.value)
      );
    }
  }

  /**
   * Returns weight input markup
   * @return {*}
   */
  get weightInput() {
    return this.multiple
      ? html`
          <input
            id="weight"
            type="number"
            value="50"
            max="100"
            min="0"
            @change="${(e) => {
              evaluateNumberInput(e);
              this.editWeight(e);
            }}"
            ?disabled="${!this.index || !this.selectedModel}"
          />
          %
        `
      : nothing;
  }

  /**
   * Render the <e-model-selector> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <div class="model-selector">
        ${this.title}
        <div class="combo-box-container">
          ${this.idSelectorMarkup} ${this.versionSelectorMarkup} ${this.weightInput}
          ${this.deleteButton}
        </div>
      </div>
    `;
  }
}

/**
 * Register the component as e-model-selector.
 * Registration can be done at a later time and with a different name
 */
ModelSelector.register();
