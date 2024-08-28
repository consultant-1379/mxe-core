/**
 * Component ModelCard is defined as
 * `<e-model-card>`
 *
 * Imperatively create component
 * @example
 * let component = new ModelCard();
 *
 * Declaratively create component
 * @example
 * <e-model-card></e-model-card>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent, nothing } from '@eui/lit-component';
import { boundMethod } from 'autobind-decorator';
import { preventDefaultEvent, loc } from 'utils/Utils';
import {
  CHANGE_MODEL,
  SELECT_MODEL,
  STATUS_ERROR,
  STATUS_MODEL_ERROR,
  STATUS_PACKAGING,
} from 'utils/Enums';
import { toModelInfo } from 'utils/Navigator';
import style from './modelCard.css';

/**
 * Modal card component
 * @property {boolean} shouldNavigate - Should the card navigate on click
 * @property {boolean} showVersion - Show version on the card
 * @property {Object} model - Model data
 * @property {boolean} selected - Is the model card selected or not
 */
@definition('e-model-card', {
  style,
  home: 'model-card',
  props: {
    shouldNavigate: { attribute: false, type: Boolean, default: false },
    showVersion: { attribute: false, type: Boolean, default: false },
    model: { attribute: false, type: Object, default: null },
    selected: { attribute: false, type: Boolean, default: false },
  },
})
/**
 * @class ModelCard
 */
export default class ModelCard extends LitComponent {
  /**
   * Last model selection
   * @return {*}
   */
  get lastModel() {
    if (Array.isArray(this.model)) {
      const { length } = this.model[1];
      return this.model[1][length - 1];
    }
    return this.model;
  }

  /**
   * Is card stacked
   * @return {boolean}
   */
  get isStacked() {
    if (Array.isArray(this.model)) {
      return this.model[1].length > 1;
    }
    return false;
  }

  /**
   * Is card selected
   * @return {*}
   */
  get isSelected() {
    return this.selected;
  }

  /**
   * Number of version
   * @return {string}
   */
  get numberOfVersions() {
    if (this.showVersion) {
      return `v${this.modelVersion}`;
    }
    if (this.model[1].length === 1) {
      return `1 ${loc('VERSION')}`;
    }
    return `${this.model[1].length} ${loc('VERSION')}s`;
  }

  /**
   * Get icon size depending on the model state
   * @returns {string} - Icon size
   */
  get iconSize() {
    switch (this.modelStatus) {
      case STATUS_PACKAGING:
        return '55px';
      case STATUS_MODEL_ERROR:
        return '36px';
      default:
        return '80px';
    }
  }

  /**
   * Get icon name depending on the model state
   * @returns {string} - Icon name
   */
  get iconName() {
    switch (this.modelStatus) {
      case STATUS_PACKAGING:
        return 'dial';
      case STATUS_MODEL_ERROR:
        return 'info';
      default:
        return 'dashboard';
    }
  }

  /**
   * Get model icon, it's either plain img, or eui sdk icon
   * @return {*}
   */
  get modelIcon() {
    // Packaging
    if (this.modelStatus === STATUS_PACKAGING) {
      return html`
        <eui-v0-icon class="model-icon" size="${this.iconSize}" name="dial"></eui-v0-icon>
      `;
    }

    // Error
    if (this.modelStatus === STATUS_ERROR) {
      return html`
        <eui-v0-icon
          class="model-icon"
          size="${this.iconSize}"
          name="triangle-warning"
        ></eui-v0-icon>
      `;
    }

    // We have an icon hopefully
    if (
      this.lastModel.icon !== null &&
      this.lastModel.icon !== undefined &&
      this.lastModel.icon !== '' &&
      this.lastModel.icon.length > 0
    ) {
      return html` <img class="model-icon" src="${this.lastModel.icon}" alt="model icon" /> `;
    }

    // Default
    return html`
      <img class="model-icon" src="/assets/icons/standard-icon.svg" alt="model icon" />
    `;
  }

  /**
   * Get model status
   * @returns {string} - Card status
   */
  get modelStatus() {
    return this.lastModel.status;
  }

  /**
   * Get model name
   * @returns {string} - Model name
   */
  get modelName() {
    return this.lastModel.displayName || this.lastModel.title;
  }

  /**
   * Get model id
   * @returns {string} - Model id
   */
  get modelId() {
    return this.lastModel.id;
  }

  /**
   * Get model version
   * @returns {string} - Model version
   */
  get modelVersion() {
    return this.lastModel.version;
  }

  /**
   * Get model description
   * @returns {String|null}
   */
  get description() {
    if (this.isStacked) {
      return nothing;
    }
    let description = '';

    switch (this.modelStatus) {
      case STATUS_PACKAGING:
        description = loc('PACKAGING_IN_PROGRESS');
        break;
      case STATUS_MODEL_ERROR:
        return html` <div class="text">${loc('PACKAGING_FAILED')}</div> `;
      default:
        description = this.lastModel.message;
        break;
    }

    return html` <div class="text">${description}</div> `;
  }

  /**
   * Get Card class
   * @return {string}
   */
  get cardClass() {
    const selected = this.isSelected ? 'selected' : '';
    return `model-card ${this.modelStatus} ${selected}`;
  }

  /**
   * Handles clicks on the card element
   * @param event
   */
  @boundMethod
  handleEvent(event) {
    preventDefaultEvent(event);

    if (this.shouldNavigate) {
      toModelInfo(this.modelId, this.modelVersion);
      return;
    }
    // MatomoService.trackEvent(
    //   MATOMO_CATEGORY_MODEL_CARD,
    //   MATOMO_ACTION_OPEN_MODEL_DETAIL,
    //   this.modelName
    // );
    this.bubble(SELECT_MODEL, this.model);
  }

  /**
   * Render the <e-model-card> component. This function is called each time a
   * prop changes.
   */
  render() {
    if (!this.lastModel) {
      return nothing;
    }
    return html`
      <div class="${this.cardClass}" @click="${this}">
        <div class="header">
          <div class="left title">${this.modelName}</div>
        </div>
        <div class="id">${this.modelId}</div>
        <div class="versions">${this.numberOfVersions}</div>
        <div class="content">${this.modelIcon} ${this.description}</div>
      </div>
    `;
  }
}

/**
 * Register the component as e-model-card.
 * Registration can be done at a later time and with a different name
 */
ModelCard.register();
