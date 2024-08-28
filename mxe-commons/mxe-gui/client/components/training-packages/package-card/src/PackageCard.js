/**
 * Component PackageCard is defined as
 * `<e-package-card>`
 *
 * Imperatively create component
 * @example
 * let component = new PackageCard();
 *
 * Declaratively create component
 * @example
 * <e-package-card></e-package-card>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, nothing } from '@eui/lit-component';
import { loc, preventDefaultEvent } from 'utils/Utils';
import { boundMethod } from 'autobind-decorator';
import {
  INVOKE_PACKAGE_ACTION,
  STATUS_AVAILABLE,
  STATUS_CREATING,
  STATUS_DEPLOYMENT_ERROR,
  STATUS_ERROR,
  STATUS_MODEL_ERROR,
  STATUS_PACKAGING,
  STATUS_RUNNING,
} from 'utils/Enums';
import style from './packageCard.css';

/**
 * @property {Object} package - package object
 * @property {boolean} selected - Is the card selected
 */
@definition('e-package-card', {
  style,
  home: 'package-card',
  props: {
    package: { attribute: false, type: Object },
    selected: { attribute: false, type: Boolean, default: false },
  },
})
export default class PackageCard extends LitComponent {
  get lastPackage() {
    const { length } = this.package[1];
    return this.package[1][length - 1];
  }

  /**
   * Get status icon name according to the package status
   * @returns {string} - Icon name
   */
  get cardAction() {
    if (!this.isStacked) {
      return nothing;
    }
    return html`
      <img class="action" slot="action" src="/assets/icons/folder-icon.svg" alt="package status" />
    `;
  }

  /**
   * Get icon size depending on the package state
   * @returns {string} - Icon size
   */
  get iconSize() {
    switch (this.packageStatus) {
      case STATUS_PACKAGING:
        return '55px';
      case STATUS_MODEL_ERROR:
      case STATUS_DEPLOYMENT_ERROR:
        return '36px';
      default:
        return '80px';
    }
  }

  /**
   * Get icon name depending on the package state
   * @returns {string} - Icon name
   */
  get iconName() {
    switch (this.packageStatus) {
      case STATUS_PACKAGING:
        return 'dial';
      case STATUS_MODEL_ERROR:
      case STATUS_DEPLOYMENT_ERROR:
        return 'info';
      default:
        return 'dashboard';
    }
  }

  /**
   * Returns if package is stacked
   * @return {boolean}
   */
  get isStacked() {
    return this.package[1].length > 1;
  }

  get isSelected() {
    return this.selected;
  }

  /**
   * Get package icon, it's either plain img, or eui sdk icon
   * @return {*}
   */
  get packageIcon() {
    // Packaging
    if (this.packageStatus === STATUS_PACKAGING) {
      return html`
        <eui-v0-icon class="package-icon" size="${this.iconSize}" name="dial"></eui-v0-icon>
      `;
    }

    // Error
    if (
      this.packageStatus === STATUS_MODEL_ERROR ||
      this.packageStatus === STATUS_DEPLOYMENT_ERROR
    ) {
      return html`
        <eui-v0-icon
          class="package-icon"
          size="${this.iconSize}"
          name="triangle-warning"
        ></eui-v0-icon>
      `;
    }

    // We have an icon hopefully
    if (
      this.lastPackage.icon !== null &&
      this.lastPackage.icon !== undefined &&
      this.lastPackage.icon !== '' &&
      this.lastPackage.icon.length > 0
    ) {
      return html` <img class="package-icon" src="${this.lastPackage.icon}" alt="package icon" /> `;
    }

    // Default
    return html`
      <img class="package-icon" src="/assets/icons/standard-icon.svg" alt="package icon" />
    `;
  }

  /**
   * Get package status
   * @returns {string} - Card status
   */
  get packageStatus() {
    return this.lastPackage.status;
  }

  /**
   * Get package name
   * @returns {string} - Model name
   */
  get packageName() {
    return this.lastPackage.displayName || this.lastPackage.title;
  }

  /**
   * Get package id
   * @returns {string} - Model id
   */
  get packageId() {
    return this.package[0];
  }

  /**
   * Get package name
   * @returns {string} - Model name
   */
  get packageDeploymentName() {
    return this.lastPackage.deployedModel.title;
  }

  /**
   * Get package version
   * @returns {string} - Model version
   */
  get packageVersion() {
    return this.lastPackage.version;
  }

  /**
   * Get subtitle depending on the package state
   * @returns {string} - Card subtitle
   */
  get subtitle() {
    if (this.isStacked) {
      return '';
    }
    switch (this.packageStatus) {
      case STATUS_AVAILABLE:
        return loc('AVAILABLE');
      case STATUS_RUNNING:
        return loc('RUNNING');
      case STATUS_CREATING:
        return loc('CREATING');
      case STATUS_PACKAGING:
      case STATUS_MODEL_ERROR:
      case STATUS_DEPLOYMENT_ERROR:
        return '';
      default:
        return this.packageStatus;
    }
  }

  /**
   * Get package description
   * @returns {String|null}
   */
  get description() {
    let description = '';

    switch (this.packageStatus) {
      case STATUS_PACKAGING:
        description = loc('PACKAGING_IN_PROGRESS');
        break;
      case STATUS_ERROR:
        return html`
          <div class="text">${loc('PACKAGING_FAILED')}</div>
          <div class="text">${loc('SEE_LOGS')}</div>
        `;
      default:
        description = this.lastPackage ? this.lastPackage.message : '';
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
    return `package-card ${this.packageStatus} ${selected}`;
  }

  /**
   * Handles clicks on the card element
   * @param event
   */
  @boundMethod
  handleCardClick(event) {
    preventDefaultEvent(event);
    this.bubble(INVOKE_PACKAGE_ACTION, [this.package[0], this.package[1], this.packageName]);
  }

  /**
   * Render the <e-package-card> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <div class="${this.cardClass}" @click="${this.handleCardClick}">
        <div class="header">
          <div class="left title">${this.packageName}</div>
          <div class="right">${this.cardAction}</div>
        </div>
        <div class="id">${this.packageId}</div>
        <div class="subtitle">${this.subtitle}</div>
        <div class="content">${this.packageIcon} ${this.description}</div>
      </div>
    `;
  }
}

/**
 * Register the component as e-package-card.
 * Registration can be done at a later time and with a different name
 */
PackageCard.register();
