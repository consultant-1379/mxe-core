/**
 * Component ServiceLogs is defined as
 * `<e-service-logs>`
 *
 * Imperatively create component
 * @example
 * let component = new ServiceLogs();
 *
 * Declaratively create component
 * @example
 * <e-service-logs></e-service-logs>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, repeat, nothing } from '@eui/lit-component';
import { getEventPath, loc } from 'utils/Utils';
import { boundMethod } from 'autobind-decorator';
import { SERVICE_LOG_QUERY_UNITS, SERVICE_LOG_QUICK_SEARCH_ITEMS } from 'utils/Defaults';
import ModelServiceService from 'services/ModelServiceService';
import stripAnsi from 'strip-ansi';
import style from './serviceLogs.css';

/**
 * @property {Object} service - Service data
 * @property {Object} _service - Inner service object
 * @property {Object} logs - Log list
 * @property {boolean} isSidebarOpen - Is sidebar open variable
 * @property {Object} selectedUnit - Selected unit
 * @property {number} selectedValue - Selected value
 * @property {Object} selectedLogEntry - Selected log entry
 */
@definition('e-service-logs', {
  style,
  home: 'service-logs',
  props: {
    service: { attribute: false },
    _service: { attribute: false },
    logs: { attribute: false },
    isSidebarOpen: { attribute: false, type: Boolean, default: true },
    selectedUnit: { attribute: false, type: Object, default: SERVICE_LOG_QUERY_UNITS[0] },
    selectedValue: { attribute: false, type: Number, default: 10 },
    selectedLogEntry: { attribute: false, default: null },
  },
})
export default class ServiceLogs extends LitComponent {
  async didChangeProps(props) {
    if (!this._service && props.has('service')) {
      this._service = this.service;
      await this.getLogs();
    }
  }

  get serviceName() {
    return this._service?.name ?? '';
  }

  didDisconnect() {
    this.service = null;
    this._service = null;
    this.logs = null;
    this.isSidebarOpen = true;
    // eslint-disable-next-line prefer-destructuring
    this.selectedUnit = SERVICE_LOG_QUERY_UNITS[0];
    this.selectedValue = 10;
    this.selectedLogEntry = null;
  }

  @boundMethod
  async getLogs() {
    try {
      const { unit } = this.selectedUnit;
      let value;

      switch (unit) {
        case 'm':
          value = this.selectedValue * 60;
          break;
        case 'h':
          value = this.selectedValue * 3600;
          break;
        case 'l':
        default:
          value = this.selectedValue;
          break;
      }
      this.logs = this.serviceName
        ? await ModelServiceService.getModelServiceLogs(this.serviceName, { value, unit })
        : [];
      this.logEntries = Object.entries(this.logs);
      this.logEntries.sort((a, b) => a[0].localeCompare(b[0]));
      if (this.selectedLogEntry && this.selectedLogEntry.length > 0) {
        const isEntryExist = this.logEntries.find((entry) => entry[0] === this.selectedLogEntry[0]);
        this.selectedLogEntry = isEntryExist ?? this.logEntries[0];
      } else {
        // eslint-disable-next-line prefer-destructuring
        this.selectedLogEntry = this.logEntries[0];
      }
    } catch (e) {
      this.logs = [];
      this.lologEntriesgs = [];
      this.selectedLogEntry = null;
      console.error(e);
    }
  }

  @boundMethod
  toggleSidebar() {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  get sidebarMarkup() {
    return html`
      <div class="header">
        <div class="title">${loc('SELECT_CONTAINER')}</div>
        <eui-v0-icon
          name="${this.isSidebarOpen ? 'sidemenu-right-open' : 'sidemenu-left-open'}"
          class="icon"
          @click="${this.toggleSidebar}"
        ></eui-v0-icon>
      </div>
      ${this.containersMarkup}
    `;
  }

  get containersMarkup() {
    if (!this.logEntries) {
      return nothing;
    }

    return html`
      <div class="containers">
        ${repeat(
          this.logEntries,
          (entry) => entry[0],
          (entry, i) =>
            html`
              <div
                class="entry ${this.isSelected(entry) ? 'selected' : ''}"
                @click="${() => {
                  // eslint-disable-next-line prefer-destructuring
                  this.selectedLogEntry = entry;
                }}"
              >
                ${entry[0]}
              </div>
            `
        )}
      </div>
    `;
  }

  @boundMethod
  isSelected(entry) {
    return this.selectedLogEntry?.[0] === entry[0];
  }

  get filtersMarkup() {
    return html`
      <div class="title">${loc('DISPLAY_LAST')}</div>
      <input
        id="query-data"
        type="number"
        min="0"
        .value="${this.selectedValue}"
        @change="${(event) => {
          const { value } = getEventPath(event);
          this.selectedValue = value;
        }}"
      />
      <eui-base-v0-dropdown
        class="unit-selector"
        width="90px"
        label="${this.selectedUnit.title}"
        data-type="single"
      >
        ${repeat(
          SERVICE_LOG_QUERY_UNITS,
          (option, i) => i,
          (option, i) =>
            html`
              <eui-base-v0-menu-item
                class="sort-item"
                .label="${option.title}"
                tabindex="${i}"
                @click="${() => {
                  this.selectedUnit = option;
                }}"
              >
              </eui-base-v0-menu-item>
            `
        )}
      </eui-base-v0-dropdown>
      <eui-base-v0-button primary @click="${this.getLogs}">${loc('APPLY')}</eui-base-v0-button>
      <div class="quick-search">${this.quickSearchMarkup}</div>
    `;
  }

  get quickSearchMarkup() {
    return html` <div class="title">${loc('QUICK_SEARCH')}</div>
      ${repeat(
        SERVICE_LOG_QUICK_SEARCH_ITEMS,
        (item, i) => i,
        (item, i) =>
          html`<eui-base-v0-pill
            @click="${async () => {
              this.selectedUnit = item.unit;
              this.selectedValue = item.value;
              await this.getLogs();
            }}"
            >${item.title}
          </eui-base-v0-pill>`
      )}`;
  }

  get logViewer() {
    const logEntry = this.selectedLogEntry?.[1] ? stripAnsi(this.selectedLogEntry[1]) : '';

    return html`
      <div class="filters">${this.filtersMarkup}</div>
      <textarea class="viewer" readonly>${logEntry}</textarea>
    `;
  }

  /**
   * Render the <e-service-logs> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <div class="sidebar ${this.isSidebarOpen ? 'open' : ''}">
        ${this.sidebarMarkup}
      </div>
      <div class="log-viewer">
        ${this.logViewer}
      </div>
    `;
  }
}

/**
 * Register the component as e-service-logs.
 * Registration can be done at a later time and with a different name
 */
ServiceLogs.register();
