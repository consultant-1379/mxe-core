/**
 * Component PackageVersions is defined as
 * `<e-package-versions>`
 *
 * Imperatively create component
 * @example
 * let component = new PackageVersions();
 *
 * Declaratively create component
 * @example
 * <e-package-versions></e-package-versions>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, repeat, nothing } from '@eui/lit-component';
import { loc, preventDefaultEvent, formatDateToLocalDate, getEventPath } from 'utils/Utils';
import SortingService from 'services/SortingService';
import FilteringService from 'services/FilteringService';
import {
  CLOSE_VERSIONS_PANEL,
  DISPATCH_NOTIFICATIONS,
  ERROR,
  INVOKE_UPDATE,
  SHOW_ERROR_DIALOG,
  STATUS_AVAILABLE,
  STATUS_ERROR,
  STATUS_PACKAGING,
  SUCCESS,
} from 'utils/Enums';
import TrainingPackagesService from 'services/TrainingPackagesService';
import TrainingJobsService from 'services/TrainingJobsService';
import 'components/shared/view-change/src/ViewChange';
import { boundMethod } from 'autobind-decorator';
import TrainingPackageListingService from 'services/TrainingPackageListingService';
import { toTrainingPackageDetail } from 'utils/Navigator';
import {
  DEFAULT_ERROR_NOTIFICATION,
  DEFAULT_SUCCESSFUL_DELETE_NOTIFICATION,
  VERSIONS_TABLE_COLUMNS,
  SORTING_OPTIONS,
  SORTING_VERSIONS,
} from 'utils/Defaults';
import style from './packageVersions.css';

/**
 * @property {Object} package - package object.
 * @property {boolean} isListView - List or grid view
 * @property {Object} sortBy - Sorting object
 * @property {string} query - Search query
 * @property {Array} filteredVersions - Filtered versions list
 */
@definition('e-package-versions', {
  style,
  home: 'package-versions',
  props: {
    package: { attribute: false },
    isListView: { attribute: false, type: Boolean, default: true },
    sortBy: { attribute: false },
    query: { attribute: false, default: '' },
    filteredVersions: { attribute: false },
    showDeleteConfirmationDialog: { attribute: false, type: Boolean, default: false },
  },
})
export default class PackageVersions extends LitComponent {
  didConnect() {
    this.sortBy = SORTING_OPTIONS;
  }

  didRender() {
    if (this.table) {
      this.table.removeEventListener('row-click', this.handleRowClick, false);
      this.table.removeEventListener('eui-table:sort', this.sortTable, false);
      this.table = null;
    }
    if (!this.isListView && this.packageVersions.length > 0) {
      this.table = this.shadowRoot.getElementById('versions-table');
      this.table.addEventListener('row-click', this.handleRowClick, false);
      this.table.addEventListener('eui-table:sort', this.sortTable, false);
    }
  }

  didChangeProps(props) {
    if (props.has('package') && this.package && this.packageVersions) {
      this.filteredVersions = this.packageVersions;
      this.sortBy = SORTING_OPTIONS;
      this.package[1] = [...SortingService.sortPackagesByVersion('asc', this.packageVersions)];
      this.sortVersions(this.sortBy[0], true);
    }
  }

  /**
   * Sorts table
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
   * Toggle between List or Table view
   */
  @boundMethod
  toggleView() {
    this.isListView = !this.isListView;
  }

  /**
   * Returns package id if package exists
   * @return {string}
   */
  get packageId() {
    return this.package ? this.package[0] : '';
  }

  /**
   * Returns package versions if package exists
   * @return {*}
   */
  get packageVersions() {
    return this.package ? this.package[1] : '';
  }

  /**
   * Returns selected markup
   * @return {*}
   */
  get viewMarkup() {
    if (!this.isListView) {
      return this.versionsTableMarkup;
    }

    return this.versionCardListMarkup;
  }

  /**
   * Returns list view markup
   * @return {*}
   */
  get versionCardListMarkup() {
    if (!this.filteredVersions || this.filteredVersions.length === 0) {
      return nothing;
    }

    return this.filteredVersions.map((packageVersion) => this.getVersionCardMarkup(packageVersion));
  }

  /**
   * Returns table view markup
   * @return {*}
   */
  get versionsTableMarkup() {
    if (!this.filteredVersions || this.filteredVersions.length === 0) {
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
      >
      </e-custom-table>
    `;
  }

  get rows() {
    return this.filteredVersions.map((package_) => this.createRow(package_));
  }

  /**
   * Returns structured object for table
   * @param {Object} package_
   */
  createRow(package_) {
    return {
      col1: this.getStatusMarkup(package_),
      col2: package_.version,
      col3: formatDateToLocalDate(package_.created),
      col4: this._getMoreButtonMarkup(package_),
      status: package_.status,
    };
  }

  _getMoreButtonMarkup(packageVersion) {
    return html`
      <eui-base-v0-dropdown data-type="click" label="${packageVersion}" more>
        <eui-base-v0-menu-item
          .label="${loc('DELETE')}"
          @click="${() => {
            this._handleDelete(packageVersion);
          }}"
        >
        </eui-base-v0-menu-item>
      </eui-base-v0-dropdown>
    `;
  }

  @boundMethod
  _handleDelete(packageVersion) {
    this.versionToDelete = packageVersion;
    this.showDeleteConfirmationDialog = true;
  }

  /**
   * Returns package status markup
   * @param {Object} package_
   */
  @boundMethod
  getStatusMarkup(package_) {
    const { status } = package_;
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
        class="${package_.status} ${rotating ? 'rotating' : ''}"
        name="${icon}"
        color="${color}"
      ></eui-v0-icon>
      <span>${loc(package_.status.toUpperCase())}</span>
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
   * Executes package
   * @param {Object} packageVersion
   */
  async executePackage(packageVersion) {
    try {
      const { id, version } = packageVersion;
      await TrainingJobsService.postTrainingJob(id, version);
      this.package[1] = await TrainingPackagesService.getTrainingPackagesByIdAndVersion(
        id,
        version
      );
      this.bubble(DISPATCH_NOTIFICATIONS, {
        config: { ...DEFAULT_SUCCESSFUL_DELETE_NOTIFICATION, title: loc('JOB_STARTED') },
        notifications: [
          {
            data: packageVersion,
            date: new Date().toISOString(),
            status: SUCCESS,
          },
        ],
      });
    } catch (e) {
      this.bubble(DISPATCH_NOTIFICATIONS, {
        config: DEFAULT_ERROR_NOTIFICATION,
        notifications: [
          {
            data: packageVersion,
            date: new Date().toISOString(),
            status: ERROR,
          },
        ],
      });
    }
  }

  /**
   * Returns execute button markup
   * @param {Object} packageVersion
   * @return {string}
   */
  executeButtonMarkup(packageVersion) {
    const { status } = packageVersion;
    if (status === STATUS_AVAILABLE) {
      return html`
        <eui-base-v0-button
          class="action"
          @click="${async (event) => {
            preventDefaultEvent(event);
            await this.executePackage(packageVersion);
          }}"
          icon="video-play"
          >${loc('EXECUTE')}</eui-base-v0-button
        >
      `;
    }

    if (status === STATUS_ERROR) {
      return html`
        <div class="error">
          <img src="/assets/icons/error-icon.svg" alt="training package status" />
          <div class="text">${loc('PACKAGING_FAILED')}, ${loc('SEE_LOGS')}</div>
        </div>
      `;
    }

    return html`
      <div class="packaging">
        <img src="/assets/icons/creating-icon.svg" alt="training package status" />
        <span>${loc('PACKAGING')}</span>
      </div>
    `;
  }

  /**
   * deletes package
   * @param {Object} packageVersion
   */
  @boundMethod
  async deletePackage(packageVersion) {
    try {
      const { id, version } = packageVersion;
      await TrainingPackagesService.deleteTrainingPackage(id, version);
      const trainingPackages = await TrainingPackageListingService.getTrainingPackages(id);
      // eslint-disable-next-line prefer-destructuring
      this.package = trainingPackages[0];
      this.bubble(INVOKE_UPDATE);
      this.bubble(CLOSE_VERSIONS_PANEL);
    } catch (e) {
      console.error('Package delete error', e);
    }
  }

  /**
   * Returns delete button markup
   * @param {Object} packageVersion
   * @return {*}
   */
  deleteButtonMarkup(packageVersion) {
    return html`
      <eui-v0-icon
        class="delete-icon"
        name="trashcan"
        @click="${async (event) => {
          preventDefaultEvent(event);
          this._handleDelete(packageVersion);
        }}"
      ></eui-v0-icon>
    `;
  }

  /**
   * Returs version card markup
   * @param {Object} packageVersion
   * @return {*}
   */
  getVersionCardMarkup(packageVersion) {
    return html`
      <div
        class="version-card ${packageVersion.status}"
        @click="${(event) => this.handleCardClick(event, packageVersion)}"
      >
        <div class="title">Version&nbsp;${packageVersion.version}</div>
        <div class="date">${formatDateToLocalDate(packageVersion.created)}</div>
        <div class="description">${packageVersion.description}</div>
        ${this.executeButtonMarkup(packageVersion)} ${this.deleteButtonMarkup(packageVersion)}
      </div>
    `;
  }

  /**
   * Filters versions
   * @param {Object} event
   */
  @boundMethod
  filterVersions(event) {
    this.filteredVersions = this.packageVersions;
    this.query = getEventPath(event).value;
    if (this.query && this.query.length > 0) {
      this.filteredVersions = FilteringService.filterVersions(this.filteredVersions, this.query);
    }
    this.sortVersions(this.sortBy);
  }

  /**
   * Returns header markup
   * @return {*}
   */
  get headerMarkup() {
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
    `;
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
   * Handles card click event
   * @param {Object} event
   * @param {Object} packageVersion
   */
  handleCardClick(event, packageVersion) {
    preventDefaultEvent(event);
    const { id, version, status } = packageVersion;
    this.viewPackageDetails(id, status, version);
  }

  /**
   * Handles table row click event
   */
  @boundMethod
  handleRowClick(event) {
    preventDefaultEvent(event);
    const { col2, status } = event.detail;
    this.viewPackageDetails(this.packageId, status, col2);
  }

  viewPackageDetails(id, status, version) {
    if (status === STATUS_PACKAGING) {
      return;
    }
    if (status === STATUS_ERROR) {
      this.bubble(SHOW_ERROR_DIALOG, { ...version });
      return;
    }
    toTrainingPackageDetail(id, version);
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
        this.filteredVersions = SortingService.sortModelsByVersion(
          order,
          this.filteredVersions,
          isFirstFilter
        );
        break;
      case 'date':
        this.filteredVersions = SortingService.sortVersionsByDate(
          order,
          this.filteredVersions,
          isFirstFilter
        );
        break;
      case 'status':
        this.filteredVersions = SortingService.sortVersionsByStatus(
          order,
          this.filteredVersions,
          isFirstFilter
        );
        break;
      default:
        this.filteredVersions = this.packageVersions;
        break;
    }
    this.sortBy = option;
  }

  _deleteConfirmationDialogMarkup() {
    return html`<eui-base-v0-dialog
      label=${loc('DIALOG_CONFIRM_TITLE')}
      .show="${this.showDeleteConfirmationDialog}"
      @eui-dialog:cancel="${this._handleDeleteCancel}"
    >
      <div slot="content">
        ${loc('DIALOG_CONFIRM_VERSION_DELETE')}
      </div>
      <eui-base-v0-button slot="bottom" @click=${this._handleDeleteConfirm} warning
        >${loc('BUTTON_DELETE')}</eui-base-v0-button
      >
    </eui-base-v0-dialog>`;
  }

  @boundMethod
  _handleDeleteCancel() {
    this.showDeleteConfirmationDialog = false;
    this.versionToDelete = undefined;
  }

  @boundMethod
  _handleDeleteConfirm() {
    this.showDeleteConfirmationDialog = false;
    this.deletePackage(this.versionToDelete);
  }

  /**
   * Render the <e-package-versions> component. This function is called each time a
   * prop changes.
   */
  render() {
    if (!this.packageId) {
      return html` <div>${loc('SELECT_PACKAGE')}</div> `;
    }

    return html` ${this.headerMarkup} ${this.viewMarkup} ${this._deleteConfirmationDialogMarkup()}`;
  }
}

/**
 * Register the component as e-package-versions.
 * Registration can be done at a later time and with a different name
 */
PackageVersions.register();
