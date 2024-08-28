/**
 * Component NotebookCard is defined as
 * `<e-notebook-card>`
 *
 * Imperatively create component
 * @example
 * let component = new NotebookCard();
 *
 * Declaratively create component
 * @example
 * <e-notebook-card></e-notebook-card>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent, nothing } from '@eui/lit-component';
import { boundMethod } from 'autobind-decorator';
import { openCenteredWindow, preventDefaultEvent, loc } from 'utils/Utils';
import { PLACEHOLDER_IMAGE } from 'utils/Defaults';
import { INVOKE_DELETE, INVOKE_UPDATE } from 'utils/Enums';
import style from './notebookCard.css';

/**
 * Modal card component
 * @property {boolean} info - Is info
 * @property {boolean} stacked - Is stacked
 * @property {Object} notebook - Notebook data
 */
@definition('e-notebook-card', {
  style,
  home: 'notebook-card',
  props: {
    info: { attribute: false, type: Boolean, default: false },
    stacked: { attribute: false, type: Boolean, default: false },
    notebook: { attribute: false, type: Object, default: null },
  },
})
/**
 * @class NotebookCard
 */
export default class NotebookCard extends LitComponent {
  /**
   * Returns icon src
   * @return {string}
   */
  get deleteIcon() {
    return '/assets/icons/purge-icon-grey.svg';
  }

  /**
   * Get notebook status
   * @returns {string} - Card status
   */
  get notebookStatus() {
    return this.notebook.status;
  }

  /**
   * Get notebook name
   * @returns {string} - Model name
   */
  get notebookName() {
    return this.notebook.name || '';
  }

  /**
   * Get notebook version
   * @returns {string} - Model version
   */
  get notebookVersion() {
    return this.notebook.version;
  }

  /**
   * Get subtitle depending on the notebook state
   * @returns {string} - Card subtitle
   */
  get subtitle() {
    return '';
  }

  /**
   * Get Card class
   * @return {string}
   */
  get cardClass() {
    return `notebook-card ${this.notebookStatus}`;
  }

  /**
   * Update notebook status
   * @param event
   */
  @boundMethod
  async updateFlowStatus(event) {
    preventDefaultEvent(event);

    await this.bubble(INVOKE_UPDATE);
  }

  /**
   * Handles clicks on the card element
   * @param event
   */
  @boundMethod
  handleCardClick(event) {
    preventDefaultEvent(event);
    // Object is set in window.
    // This will be used to close when logout performed.
    if (window.notebookWindow && !window.notebookWindow.closed) {
      window.notebookWindow.focus();
    } else {
      window.notebookWindow = openCenteredWindow('notebook.html', 'notebook');
    }
  }

  /**
   * Bubbles delete event
   * @param {Object} event
   */
  @boundMethod
  bubbleDelete(event) {
    preventDefaultEvent(event);
    this.bubble(INVOKE_DELETE, { notebook: this.notebook });
  }

  /**
   * Returns src based on type
   * @param {string} type
   * @return {string}
   */
  getIcon(type) {
    switch (type) {
      case 'start':
        return '/assets/icons/play-icon.svg';
      case 'stop':
        return '/assets/icons/stop-icon.svg';
      default:
        return PLACEHOLDER_IMAGE;
    }
  }

  /**
   * Returns markup for action button
   * @param {string} type
   * @return {*}
   */
  @boundMethod
  getIconMarkup(type) {
    return html`
      <img
        class="action"
        src="${this.getIcon(type)}"
        @click="${this.updateFlowStatus}"
        alt="notebook status"
      />
    `;
  }

  /**
   * Render the <e-notebook-card> component. This function is called each time a
   * prop changes.
   */
  render() {
    if (!this.notebook) {
      return nothing;
    }
    return html`
      <div class="${this.cardClass}" @click="${this.handleCardClick}">
        <div class="header">
          <div class="left title">${this.notebookName}</div>
        </div>
      </div>
    `;
  }
}

/**
 * Register the component as e-notebook-card.
 * Registration can be done at a later time and with a different name
 */
NotebookCard.register();
