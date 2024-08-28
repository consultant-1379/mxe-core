/**
 * Component FilterPill is defined as
 * `<e-filter-pill>`
 *
 * Imperatively create component
 * @example
 * let component = new FilterPill();
 *
 * Declaratively create component
 * @example
 * <e-filter-pill></e-filter-pill>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent } from '@eui/lit-component';
import { loc, preventDefaultEvent } from 'utils/Utils';
import { boundMethod } from 'autobind-decorator';
import { CLEAR_FILTERS, DISMISS, OPEN_FILTERS } from 'utils/Enums';
import style from './filterPill.css';

/**
 * @property {Boolean} propOne - show active/inactive state.
 * @property {string} propTwo - shows the "Hello World" string.
 */
@definition('e-filter-pill', {
  style,
  home: 'filter-pill',
})
export default class FilterPill extends LitComponent {
  didConnect() {
    this.addEventListener(DISMISS, this.clearFilters, false);
  }

  didDisconnect() {
    this.removeEventListener(DISMISS, this.clearFilters, false);
  }

  /**
   * Clearts all filters
   * @param {Object} event
   */
  @boundMethod
  clearFilters(event) {
    preventDefaultEvent(event);
    this.bubble(CLEAR_FILTERS);
  }

  /**
   * Opens filter panel
   * @param {Object} event
   */
  @boundMethod
  openFilters(event) {
    preventDefaultEvent(event);
    this.bubble(OPEN_FILTERS);
  }

  /**
   * Render the <e-filter-pill> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <eui-base-v0-pill id="filters-pill" removable icon="filter" @click="${this.openFilters}">
        <eui-base-v0-tooltip message="${loc('OPEN_FILTERS')}">
          ${loc('FILTERS_APPLIED')}
        </eui-base-v0-tooltip>
      </eui-base-v0-pill>
    `;
  }
}
/**
 * Register the component as e-filter-pill.
 * Registration can be done at a later time and with a different name
 */
FilterPill.register();
