/**
 * Component ModelVersions is defined as
 * `<e-model-versions>`
 *
 * Imperatively create component
 * @example
 * let component = new ModelVersions();
 *
 * Declaratively create component
 * @example
 * <e-model-versions></e-model-versions>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent, nothing } from '@eui/lit-component';
import {
  formatDateToLocalDate,
  getEventPath,
  loc,
  preventDefaultEvent,
  shorten,
} from 'utils/Utils';
import SortingService from 'services/SortingService';
import 'components/shared/custom-table/src/CustomTable';
import {
  INVOKE_DELETE,
  SHOW_ERROR_DIALOG,
  STATUS_AVAILABLE,
  STATUS_ERROR,
  STATUS_PACKAGING,
} from 'utils/Enums';
import {
  MAX_TEXT_LENGTH,
  SORTING_OPTIONS,
  SORTING_VERSIONS,
  VERSIONS_TABLE_COLUMNS,
} from 'utils/Defaults';
import { toModelInfo } from 'utils/Navigator';
import { boundMethod } from 'autobind-decorator';
import 'components/shared/view-change/src/ViewChange';
import FilteringService from 'services/FilteringService';
import PermissionService from 'services/PermissionService';
import style from './modelVersions.css';

/**
 * @property {Object} model - Model object
 * @property {Array} filteredModels - Filtered models array
 * @property {Object} sortBy - Sort by object
 * @property {string} query - Query string
 * @property {boolean} isListView - Is list view or card view
 */
@definition('e-model-versions', {
  style,
  home: 'model-versions',
  props: {
    model: { attribute: false },
    filteredModels: { attribute: false },
    sortBy: { attribute: false },
    query: { attribute: false, default: '' },
    isListView: { attribute: false, type: Boolean, default: true },
  },
})
export default class ModelVersions extends LitComponent {
  didConnect() {
    this.sortBy = SORTING_OPTIONS;
  }

  didChangeProps(props) {
    if (props.has('model')) {
      this.query = '';
    }

    if (props.has('model') && this.model) {
      this.filteredModels = this.modelVersions;
      this.sortBy = SORTING_OPTIONS;
      this.sortVersions(this.sortBy[0], true);
    }
  }

  didRender() {
    if (this.table) {
      this.table.removeEventListener('row-click', this.handleRowClick, false);
      this.table.removeEventListener('eui-table:sort', this.sortTable, false);
      this.table = null;
    }
    if (!this.isListView && this.filteredModels.length > 0) {
      this.table = this.shadowRoot.getElementById('versions-table');
      this.table.addEventListener('row-click', this.handleRowClick, false);
      this.table.addEventListener('eui-table:sort', this.sortTable, false);
    }
  }

  get modelId() {
    return this.model ? this.model[0].id : '';
  }

  get modelVersions() {
    return this.model ?? [];
  }

  /**
   * Toggle between List or Table view
   */
  @boundMethod
  toggleView() {
    this.isListView = !this.isListView;
  }

  /**
   * Sorts the table
   * @param {Object} event
   */
  @boundMethod
  sortTable(event) {
    const { detail } = event;
    if (detail) {
      this.table.data = SortingService.sortTable(
        detail.sort,
        this.table.data,
        detail.column.attribute
      );
    }
  }

  /**
   * Returns list or Table markup
   * @return {*}
   */
  get viewMarkup() {
    if (this.isListView) {
      return this.versionCardListMarkup;
    }

    return this.versionsTableMarkup;
  }

  /**
   * Returns a card for each model
   * @return {*}
   */
  get versionCardListMarkup() {
    if (!this.filteredModels || this.filteredModels.length === 0) {
      return nothing;
    }
    return this.filteredModels.map((modelVersion) => this.getVersionCardMarkup(modelVersion));
  }

  /**
   * Returns the custom table for versions
   * @return {*}
   */
  get versionsTableMarkup() {
    if (!this.filteredModels || this.filteredModels.length === 0) {
      return nothing;
    }
    return html`
      <e-custom-table
        id="versions-table"
        .data="${this.rows}"
        .columns=${VERSIONS_TABLE_COLUMNS}
        sortable
        striped
        .cellFn=${this.customCell}
      ></e-custom-table>
    `;
  }

  /**
   * Returns rows for table markup
   * @return {*}
   */
  get rows() {
    return this.filteredModels.map((model) => this.createRow(model));
  }

  /**
   * Returns structured Object for the table
   * @param {Object} model
   * @return {Object}
   */
  createRow(model) {
    return {
      col1: this.getStatusMarkup(model),
      col2: model.version,
      col3: formatDateToLocalDate(model.created),
      col4: this._getMoreButtonMarkup(model),
      status: model.status,
    };
  }

  /**
   * Delete context menu option in table view
   */
  _getMoreButtonMarkup(modelVersion) {
    return html`
      <eui-base-v0-dropdown data-type="click" label="${modelVersion}" more>
        <eui-base-v0-menu-item
          .label="${loc('DELETE')}"
          @click="${() => {
            this.bubble(INVOKE_DELETE, { ...modelVersion });
          }}"
        >
        </eui-base-v0-menu-item>
      </eui-base-v0-dropdown>
    `;
  }

  customCell(row, column, rowIndex, colIndex) {
    if (column.attribute === 'col1' || column.attribute === 'col2' || column.attribute === 'col3') {
      return html` <eui-base-v0-tooltip
        message="${column.attribute === 'col1'
          ? row[column.attribute].values[0]
          : row[column.attribute]}"
        ><div class="status">${row[column.attribute]}</div>
      </eui-base-v0-tooltip>`;
    }

    // IMPORTANT
    return null;
  }

  /**
   * Returns icon markup evaluated from model status
   * @param {Object} model
   * @return {*}
   */
  @boundMethod
  getStatusMarkup(model) {
    const { status } = model;
    let icon = '';
    let color = '';
    let rotating = false;
    switch (status) {
      case STATUS_AVAILABLE:
        icon = 'check';
        color = 'var(--green)';
        break;
      case STATUS_ERROR:
        icon = 'cross';
        color = 'var(--red)';
        break;
      case STATUS_PACKAGING:
        icon = 'dial';
        color = 'var(--orange)';
        rotating = true;
        break;
      default:
        break;
    }
    return html`
      <eui-v0-icon
        class="${model.status} ${rotating ? 'rotating' : ''}"
        name="${icon}"
        color="${color}"
      ></eui-v0-icon>
      <span>${loc(model.status.toUpperCase())}</span>
    `;
  }

  /**
   * Returns the markup of a model version card
   * @param {Object} modelVersion
   * @return {*}
   */
  getVersionCardMarkup(modelVersion) {
    return html`
      <div
        class="version-card ${modelVersion.status}"
        @click="${(event) => this.handleCardClick(event, modelVersion)}"
      >
        <div class="title link" @click="${(event) => this.handleTitleClick(event, modelVersion)}">
          ${loc('VERSION')}&nbsp;${modelVersion.version}
        </div>
        <div class="date">${formatDateToLocalDate(modelVersion.created)}</div>
        <div class="description">${shorten(modelVersion.description, MAX_TEXT_LENGTH)}</div>
        ${this.deleteButtonMarkup(modelVersion)} ${this.buttonMarkup(modelVersion)}
      </div>
    `;
  }

  /**
   * Returns html according to status
   * @param {Object} modelVersion
   * @return {*}
   */
  buttonMarkup(modelVersion) {
    const { status, message } = modelVersion;

    const errorMessage = message ?? `${loc('PACKAGING_FAILED')}, ${loc('SEE_LOGS')}`;

    switch (status) {
      case STATUS_ERROR:
        return html`
          <div class="error">
            <img src="/assets/icons/error-icon.svg" alt="training model status" />
            <div class="text">${errorMessage}</div>
          </div>
        `;
      case STATUS_PACKAGING:
        return html`
          <div class="packaging">
            <img src="/assets/icons/creating-icon.svg" alt="training model status" />
            <span>${loc('PACKAGING')}</span>
          </div>
        `;
      default:
        return nothing;
    }
  }

  /**
   * Get delete button markup for each modelVersion
   * @param modelVersion
   * @return {*}
   */
  deleteButtonMarkup(modelVersion) {
    return html`
      <eui-v0-icon
        id="delete-${modelVersion.id}-${modelVersion.version}"
        class="delete-icon"
        name="trashcan"
        @click="${(event) => {
          preventDefaultEvent(event);
          this.bubble(INVOKE_DELETE, { ...modelVersion });
        }}"
      ></eui-v0-icon>
    `;
  }

  /**
   * Handles click event on version card
   * @param {Object} event
   * @param {Object} modelVersion
   */
  @boundMethod
  handleCardClick(event, modelVersion) {
    preventDefaultEvent(event);
    const { id, version, status } = modelVersion;

    if (status === STATUS_ERROR) {
      this.bubble(SHOW_ERROR_DIALOG, { ...modelVersion });
    }
  }

  /**
   * Handles click event on card title
   * @param {Object} event
   * @param {Object} modelVersion
   */
  @boundMethod
  handleTitleClick(event, modelVersion) {
    preventDefaultEvent(event);
    const { id, version, status } = modelVersion;
    if (status === STATUS_PACKAGING) {
      return;
    }
    toModelInfo(id, version);
  }

  /**
   * Handles click on version table row
   * @param {Object} event
   */
  @boundMethod
  handleRowClick(event) {
    preventDefaultEvent(event);
    const { col2, status } = event.detail;
    if (status === STATUS_PACKAGING) {
      return;
    }
    if (status === STATUS_ERROR) {
      this.bubble(SHOW_ERROR_DIALOG, { ...col2 });
      return;
    }
    toModelInfo(this.modelId, col2);
  }

  /**
   * Handle model sorting
   * @param {Object} option - Selected sorting method
   * @param {Boolean} isFirstFilter - Is first filter event
   */
  @boundMethod
  sortVersions(option, isFirstFilter = false) {
    const { by, order } = option;
    switch (by) {
      case 'name':
        this.filteredModels = SortingService.sortModelsByVersion(
          order,
          this.filteredModels,
          isFirstFilter
        );
        break;
      case 'date':
        this.filteredModels = SortingService.sortVersionsByDate(
          order,
          this.filteredModels,
          isFirstFilter
        );
        break;
      case 'status':
        this.filteredModels = SortingService.sortVersionsByStatus(
          order,
          this.filteredModels,
          isFirstFilter
        );
        break;
      default:
        this.filteredModels = this.modelVersions;
        break;
    }
    this.sortBy = option;
  }

  @boundMethod
  filterVersions(event) {
    this.filteredModels = this.modelVersions;
    this.query = getEventPath(event).value;
    if (this.query && this.query.length > 0) {
      this.filteredModels = FilteringService.filterVersions(this.filteredModels, this.query);
    }
    this.sortVersions(this.sortBy);
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
   * Render the <e-model-versions> component. This function is called each time a
   * prop changes.
   */
  render() {
    if (!this.modelId) {
      return html` <div>${loc('SELECT_PACKAGE')}</div> `;
    }

    return html`
      <div class="header">
        <eui-base-v0-text-field
          class="search-field"
          placeholder="${loc('SIDEBAR_SEARCH')}"
          value="${this.query}"
          @input="${this.debounce(this.filterVersions)}"
        >
          <eui-v0-icon slot="icon" name="search"></eui-v0-icon>
        </eui-base-v0-text-field>
        <e-list-sorting
          .sortFn="${this.sortVersions}"
          .sortingOptions="${SORTING_VERSIONS}"
        ></e-list-sorting>
        <e-view-change
          .isListView="${this.isListView}"
          .toggleView="${this.toggleView}"
        ></e-view-change>
      </div>
      ${this.viewMarkup}
    `;
  }
}

/**
 * Register the component as e-model-versions.
 * Registration can be done at a later time and with a different name
 */
ModelVersions.register();
