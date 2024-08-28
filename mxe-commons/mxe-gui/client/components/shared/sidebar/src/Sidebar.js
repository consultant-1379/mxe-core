/**
 * Component Sidebar is defined as
 * `<e-sidebar>`
 *
 * Imperatively create component
 * @example
 * let component = new Sidebar();
 *
 * Declaratively create component
 * @example
 * <e-sidebar></e-sidebar>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent, nothing, repeat } from '@eui/lit-component';
import { DEFAULT_FILTERS } from 'utils/Defaults';
import { CLEAR_FILTERS, SIDEBAR_FILTER_CHANGE } from 'utils/Enums';
import { boundMethod } from 'autobind-decorator';
import { getEventPath, loc } from 'utils/Utils';
import style from './sidebar.css';

@definition('e-sidebar', {
  style,
  home: 'sidebar',
  props: {
    filterList: { attribute: false, type: Array, default: [...DEFAULT_FILTERS] },
    query: { attribute: false, type: String, default: '' },
    isFiltersEmpty: { attribute: false, type: Boolean, default: true },
    isDateFiltersEmpty: { attribute: false, type: Object, default: {} },
    dateFilters: { attribute: false, type: Array, default: [] },
    selectedDates: { attribute: false, type: Object, default: {} },
  },
})
export default class Sidebar extends LitComponent {
  constructor() {
    super();
    this.selectedFilters = new Set();
  }

  didConnect() {
    window.addEventListener(CLEAR_FILTERS, this.clearAllFilters, false);
  }

  didDisconnect() {
    window.removeEventListener(CLEAR_FILTERS, this.clearAllFilters, false);
  }

  /**
   * Clears all filters
   */
  @boundMethod
  clearAllFilters() {
    this.clearQuery();
    this.clearStatusFilter();
    this.selectedDates = [];
    this.sendEvent(true);
    this.executeRender();
  }

  /**
   * Handles query filter changes
   * @param {Object} event
   */
  @boundMethod
  filterNames(event) {
    this.query = getEventPath(event).value;
    this.sendEvent();
  }

  debounce = (callback, wait = 500) => {
    let timeout = null;
    return (...args) => {
      const next = () => callback(...args);
      clearTimeout(timeout);
      timeout = setTimeout(next, wait);
    };
  };

  /**
   * Handles checkbox filter changes
   * @param {Object} event
   */
  @boundMethod
  filterStatuses(event) {
    const { detail } = event;
    if (this.selectedFilters.has(detail.name)) {
      this.selectedFilters.delete(detail.name);
    } else {
      this.selectedFilters.add(detail.name);
    }

    this.isFiltersEmpty = this.isFiltersEmptyCheck;

    this.sendEvent();
  }

  /**
   * Handles date filter
   * @param {Object} event
   * @param {string} item
   * @param {string} filter
   */
  @boundMethod
  handleDateFilter(event, item, filter) {
    if (getEventPath(event).value && item) {
      const { value } = getEventPath(event);
      if (item) {
        if (!this.selectedDates[item]) {
          this.selectedDates[item] = {};
        }
        this.selectedDates[item][filter] = value;
      }

      this.isDateFiltersEmpty = this.isDateFiltersEmptyCheck(item);

      this.sendEvent();
    }
  }

  /**
   * Bubbles up events to the parents
   */
  @boundMethod
  sendEvent(reset = false) {
    if (reset) {
      this.bubble(SIDEBAR_FILTER_CHANGE, {
        query: '',
        filters: [],
        selectedDates: [],
      });
      return;
    }
    this.bubble(SIDEBAR_FILTER_CHANGE, {
      query: this.query.toLowerCase(),
      filters: this.selectedFilters,
      selectedDates: this.selectedDates,
    });
  }

  /**
   * Clear status filters, with a timeout trick,
   * because somehow it didn't re-render
   */
  @boundMethod
  clearStatusFilter() {
    this.filterList = [];
    // Wouldn't re-render
    setTimeout(() => {
      this.filterList = this.filterList || [...DEFAULT_FILTERS];
      this.selectedFilters = new Set();
      this.isFiltersEmpty = true;
      this.sendEvent();
    });
  }

  /**
   * Clears date filters
   * @param {string} item
   */
  @boundMethod
  clearDateFilters(item) {
    try {
      delete this.selectedDates[item];
      // Wouldn't re-render
      setTimeout(() => {
        this.isDateFiltersEmpty[item] = true;
        this.sendEvent();
      });
    } catch (e) {
      console.log(e);
    }
  }

  /**
   * Clear query input
   */
  @boundMethod
  clearQuery() {
    this.query = '';
    this.sendEvent();
  }

  /**
   * Check if none of the filters are selected
   * @return {boolean}
   */
  get isFiltersEmptyCheck() {
    return this.selectedFilters.size === 0;
  }

  @boundMethod
  isDateFiltersEmptyCheck(item) {
    return !Object.prototype.hasOwnProperty.call(this.selectedDates, item);
  }

  /**
   * Check if query input is empty
   * @return {boolean}
   */
  get isQueryEmpty() {
    return this.query.length === 0;
  }

  /**
   * Returns formatted date
   * @return {string}
   */
  get today() {
    return new Date().toLocaleDateString();
  }

  /**
   * Returns date filter markup
   * @return {*}
   */
  get dateFiltersMarkup() {
    if (!this.dateFilters.length) {
      return nothing;
    }

    return repeat(
      this.dateFilters,
      (item) => item,
      (item) => html`
        <div>
          <div class="filter-name-container">
            <span class="filter-name">${loc(item.toUpperCase())}</span>
            <sup
              class="clear-filter ${this.isDateFiltersEmptyCheck(item) ? 'hidden' : ''}"
              @click="${() => this.clearDateFilters(item)}"
              >clear</sup
            >
          </div>
          <div class="datepicker-wrapper">
            <div class="datepicker-container">
              <span class="label">${loc('FROM')}</span>
              <eui-base-v0-datepicker
                date="${this.today}"
                @change=${(event) => {
                  this.handleDateFilter(event, item, 'from');
                }}
              ></eui-base-v0-datepicker>
            </div>
            <div class="datepicker-container">
              <span class="label">${loc('TO')}</span>
              <eui-base-v0-datepicker
                date="${this.today}"
                @change=${(event) => {
                  this.handleDateFilter(event, item, 'to');
                }}
              ></eui-base-v0-datepicker>
            </div>
          </div>
        </div>
      `
    );
  }

  /**
   * Returns status filter markup
   * @return {*}
   */
  get statusFiltersMarkup() {
    if (this.filterList.length === 0) {
      return nothing;
    }

    return html`
      <div class="filter-name-container">
        <span class="filter-name">${loc('FILTER_BY_STATUS')}</span>
        <sup
          class="clear-filter ${this.isFiltersEmpty ? 'hidden' : ''}"
          @click="${this.clearStatusFilter}"
          >clear</sup
        >
      </div>
      ${repeat(
        this.filterList,
        (filter) => filter.name,
        (filter) => html`
          <eui-base-v0-checkbox
            .name="${filter.name}"
            ?checked="${filter.checked}"
            @change="${this.filterStatuses}"
            .forid="${filter.name}"
          >
            ${filter.label}
          </eui-base-v0-checkbox>
        `
      )}
    `;
  }

  /**
   * Render the <e-sidebar> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <div class="sidebar-content">
        <div class="filter-name-container">
          <span class="filter-name">Search</span>
          <sup
            class="clear-filter ${this.isQueryEmpty ? 'hidden' : ''}"
            @click="${this.clearQuery}"
          >
            ${loc('CLEAR')}
          </sup>
        </div>
        <eui-base-v0-text-field
          fullwidth
          class="search-field"
          name="input-search"
          placeholder="${loc('SIDEBAR_SEARCH')}"
          value="${this.query}"
          @input="${this.debounce(this.filterNames)}"
        >
          <eui-v0-icon slot="icon" name="search"></eui-v0-icon>
        </eui-base-v0-text-field>
        <div class="filters">
          ${this.statusFiltersMarkup}
        </div>
        <div class="date-filters">
          ${this.dateFiltersMarkup}
        </div>
      </div>
    `;
  }
}

/**
 * Register the component as e-sidebar.
 * Registration can be done at a later time and with a different name
 */
Sidebar.register();
