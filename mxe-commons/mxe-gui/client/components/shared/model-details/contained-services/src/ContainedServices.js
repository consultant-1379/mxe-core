/**
 * Component ContainedServices is defined as
 * `<e-contained-services>`
 *
 * Imperatively create component
 * @example
 * let component = new ContainedServices();
 *
 * Declaratively create component
 * @example
 * <e-contained-services></e-contained-services>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, nothing } from '@eui/lit-component';
import 'components/model-services/services-table/src/ServicesTable';
import { loc } from 'utils/Utils';
import style from './contained-services.css';

/**
 * @property {Object} model - Model object
 */
@definition('e-contained-services', {
  style,
  home: 'contained-services',
  props: {
    services: { attribute: false, type: Array, default: [] },
  },
})
export default class ContainedServices extends LitComponent {
  get contentMarkup() {
    if (this.services.length === 0) {
      return loc('NO_SERVICE_BY_MODEL');
    }
    return html`<e-services-table
      .services="${this.services}"
      .showNoAccess="${true}"
    ></e-services-table> `;
  }

  /**
   * Render the <e-contained-services> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <div class="top">
        <div class="title">${loc('SERVICES_BY_MODEL')}</div>
        <div class="subtitle">${this.services.length} ${loc('ITEMS')}</div>
      </div>
      <div class="content">
        ${this.contentMarkup}
      </div>
    `;
  }
}

/**
 * Register the component as e-contained-services.
 * Registration can be done at a later time and with a different name
 */
ContainedServices.register();
