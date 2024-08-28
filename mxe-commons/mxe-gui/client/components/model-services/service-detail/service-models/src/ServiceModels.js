/**
 * Component ServiceModels is defined as
 * `<e-service-models>`
 *
 * Imperatively create component
 * @example
 * let component = new ServiceModels();
 *
 * Declaratively create component
 * @example
 * <e-service-models></e-service-models>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent, nothing } from '@eui/lit-component';
import 'components/shared/model-selector-container/src/ModelSelectorContainer';
import 'components/model-list/model-card/src/ModelCard';
import { loc } from 'utils/Utils';
import { OPEN_CHANGE_MODEL_DIALOG } from 'utils/Enums';
import { boundMethod } from 'autobind-decorator';
import PermissionService from 'services/PermissionService';
import style from './serviceModels.css';

/**
 * @property {Object} service - Service data object
 * @property {boolean} showChangeDialog - Show dialog variable
 */
@definition('e-service-models', {
  style,
  home: 'service-models',
  props: {
    service: { attribute: false },
    showChangeDialog: { attribute: false, type: Boolean, default: false },
  },
})
export default class ServiceModels extends LitComponent {
  @boundMethod
  toggleChangeModelDialog() {
    this.showChangeDialog = !this.showChangeDialog;
    this.bubble(OPEN_CHANGE_MODEL_DIALOG);
  }

  /**
   * Returns a card for each model in the service
   * @return {*}
   */
  get modelListMarkup() {
    if (!this.service || !this.service.models) {
      return nothing;
    }

    return this.service.models.map(
      (model, i) => html`
        <div class="card" style="z-index: ${10 - i}">
          <div class="weight">
            ${this.getWeightMarkup({ ...model })}
          </div>
          <e-model-card
            .showVersion=${true}
            .model="${[[...model.id], [{ ...model }]]}"
            .shouldNavigate="${true}"
          ></e-model-card>
        </div>
      `
    );
  }

  /**
   * Returns the markup for the weight indicator
   * @param {Object} model
   * @return {*}
   */
  @boundMethod
  getWeightMarkup(model) {
    if (!this.isABTest) {
      return nothing;
    }

    const weight = Math.round(model.weight * 100);

    return html` <eui-base-v0-pill>${weight}% ${loc('WEIGHT')}</eui-base-v0-pill> `;
  }

  /**
   * Returns change model button if the user has permissions
   * @return {*}
   */
  get changeButtonMarkup() {
    return html`
      <eui-base-v0-button
        primary
        id="change-models-model-service"
        @click="${this.toggleChangeModelDialog}"
      >
        ${loc('CHANGE_MODEL')}s
      </eui-base-v0-button>
    `;
  }

  /**
   * Returns the title
   * @returns {string}
   */
  get modelListTitle() {
    if (!this.service) {
      return loc('LOADING');
    }

    return `${this.service.models.length} ${loc('ITEMS')}`;
  }

  /**
   * Returns weight
   * @return {number}
   */
  get ratioPercentage() {
    return Math.round(this.ratio * 100) || 100;
  }

  /**
   * Returns if service has one or more models
   * @return {boolean}
   */
  get isABTest() {
    return this.service ? this.service.models.length > 1 : false;
  }

  /**
   * Render the <e-model-service-detail> app. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <div class="top">
        <div class="left">
          <span class="title">${loc('MODELS')}</span>
          <span class="subtitle">${this.modelListTitle}</span>
        </div>
        <div class="right">
          ${this.changeButtonMarkup}
        </div>
      </div>
      <div class="models-wrapper">
        ${this.modelListMarkup}
      </div>
      <eui-base-v0-dialog
        class="change-dialog"
        label="${loc('CHANGE_MODEL')}"
        @eui-dialog:cancel="${this.toggleChangeModelDialog}"
        .show="${this.showChangeDialog}"
        no-cancel
      >
        <e-change-model
          .service="${this.service}"
          .isABTest="${this.isABTest}"
          slot="content"
        ></e-change-model>
      </eui-base-v0-dialog>
    `;
  }
}

/**
 * Register the component as e-service-models.
 * Registration can be done at a later time and with a different name
 */
ServiceModels.register();
