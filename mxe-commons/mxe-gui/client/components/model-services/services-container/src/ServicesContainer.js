/**
 * Component ServicesContainer is defined as
 * `<e-services-container>`
 *
 * Imperatively create component
 * @example
 * let component = new ServicesContainer();
 *
 * Declaratively create component
 * @example
 * <e-services-container></e-services-container>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, nothing } from '@eui/lit-component';
import { DEFAULT_INTERVAL_MS, MODEL_SERVICES_FILTERS } from 'utils/Defaults';
import {
  INVOKE_UPDATE,
  JOB_TABLE_ROW_SELECTION,
  SIDEBAR_FILTER_CHANGE,
  CREATED,
  OPEN_FILTERS,
  OPEN_CHANGE_MODEL_DIALOG,
} from 'utils/Enums';
import { boundMethod } from 'autobind-decorator';
import FilteringService from 'services/FilteringService';
import 'components/shared/sidebar/src/Sidebar';
import 'components/model-services/services-table/src/ServicesTable';
import 'components/shared/error-screen/src/ErrorScreen';
import 'components/shared/create-model-service/src/CreateModelService';
import 'components/shared/model-tabs/src/ModelTabs';
import 'components/shared/filter-pill/src/FilterPill';
import { loc } from 'utils/Utils';
import ModelServiceService from 'services/ModelServiceService';
import PermissionService from 'services/PermissionService';
import style from './servicesContainer.css';

/**
 * @property {Array} services - Service list array
 * @property {Array} filteredServices - Filtered Service list array
 * @property {Array} selectedRows - Selected rows
 * @property {boolean} error - Error variable
 * @property {boolean} isLoading - Is loading from network
 * @property {number} timer - Interval timer variable
 * @property {boolean} showDialog - Show dialog variable
 */
@definition('e-services-container', {
  style,
  home: 'services-container',
  props: {
    services: { attribute: false, type: Array, default: [] },
    filteredServices: { attribute: false, type: Array, default: [] },
    selectedRows: { attribute: false, type: Array, default: [] },
    error: { attribute: false, default: null },
    isLoading: { attribute: false, type: Boolean, default: true },
    timer: { attribute: false, type: Number, default: null },
    showDialog: { attribute: false, type: Boolean, default: false },
  },
})
export default class ServicesContainer extends LitComponent {
  constructor() {
    super();
    this.activeFilters = [];
    this.activeQuery = '';
    this.selectedDates = [];
  }

  async didConnect() {
    await this.getServices();
    if (!this.timer) {
      this.timer = setInterval(async () => {
        await this.getServices();
      }, DEFAULT_INTERVAL_MS);
    }
  }

  didRender() {
    // window.addEventListener(INVOKE_PACKAGE_ACTION, this.handlePackageCardClick, false);
    window.addEventListener(SIDEBAR_FILTER_CHANGE, this.onSidebarFilterChange, false);
    window.addEventListener(JOB_TABLE_ROW_SELECTION, this.handleRowSelection, false);
    window.addEventListener(INVOKE_UPDATE, this.getServices, false);
    window.addEventListener(OPEN_FILTERS, this.openFilters, false);

    this.filtersPanel = this.shadowRoot.getElementById('filter');
  }

  didDisconnect() {
    // window.removeEventListener(INVOKE_PACKAGE_ACTION, this.handlePackageCardClick, false);
    window.removeEventListener(SIDEBAR_FILTER_CHANGE, this.onSidebarFilterChange, false);
    window.removeEventListener(JOB_TABLE_ROW_SELECTION, this.handleRowSelection, false);
    window.removeEventListener(INVOKE_UPDATE, this.getServices, false);
    window.removeEventListener(OPEN_FILTERS, this.openFilters, false);

    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }

  /**
   * Toggles change modal dialog
   */
  @boundMethod
  toggleDialog() {
    this.showDialog = !this.showDialog;
    this.OnboardingDialog();
    this.bubble(OPEN_CHANGE_MODEL_DIALOG);
  }

  /**
   * Handle filter events from the sidebar component
   * @param {Object} event
   */
  @boundMethod
  onSidebarFilterChange(event) {
    const { filters, query, selectedDates } = event.detail;
    this.activeFilters = filters;
    this.activeQuery = query;
    this.selectedDates = selectedDates;
    this.filterServices();
  }

  /**
   * Handles click event on table rows
   * @param {Object} event
   */
  @boundMethod
  handleRowSelection(event) {
    if (event.detail) {
      this.selectedRows = event.detail;
    }
  }

  /**
   * Open sidebar which contains filters
   */
  @boundMethod
  openFilters() {
    if (this.filtersPanel) {
      this.filtersPanel.setAttribute('show', 'true');
    }
  }

  /**
   * Filter and sort services
   */
  filterServices() {
    this.filteredServices = this.services;
    if (this.activeFilters && this.activeFilters.size) {
      this.filteredServices = FilteringService.filterServicesByStatus(
        this.filteredServices,
        this.activeFilters
      );
    }
    if (this.activeQuery) {
      this.filteredServices = FilteringService.filterServicesByName(
        this.filteredServices,
        this.activeQuery
      );
    }

    if (this.selectedDates) {
      this.filteredServices = FilteringService.filterServicesByDate(
        this.filteredServices,
        this.selectedDates
      );
    }
  }

  /**
   * Get merged packages
   */
  @boundMethod
  async getServices() {
    try {
      this.isLoading = true;
      this.services = await ModelServiceService.getModelServices();
      this.filteredServices = this.services;
      this.isLoading = false;
    } catch (error) {
      console.error(error);
      this.error = true;
      this.isLoading = false;
    }
  }

  /**
   * Returns title
   * @return {string}
   */
  get tileTitle() {
    if (this.isLoading) {
      return loc('LOADING');
    }
    return `${this.filteredServices.length} services`;
  }

  /**
   * Returns markup for selected filters
   * @return {*}
   */
  get filtersPillMarkup() {
    if ((this.activeFilters && this.activeFilters.size) || this.activeQuery.length > 0) {
      return html` <e-filter-pill></e-filter-pill> `;
    }
    return nothing;
  }

  /**
   * Returns create service button if user has permission
   * @return {*}
   */
  get createServiceButtonMarkup() {
    return html`
      <eui-base-v0-button primary id="create-model-service-dialog" @click="${this.toggleDialog}">
        ${loc('CREATE_MODEL_SERVICE')}
      </eui-base-v0-button>
    `;
  }

  @boundMethod
  OnboardingDialog() {
    if (this.showDialog) {
      const uploadDialog = this.shadowRoot.querySelector('.upload-dialog');
      const dialog = uploadDialog.shadowRoot.querySelector('.dialog');
      dialog.setAttribute('style', 'min-width: 50%; min-height: 60%');
    }
  }

  /**
   * Render the <e-job-container> component. This function is called each time a
   * prop changes.
   */
  render() {
    if (this.error) {
      // @TODO
      return html`
        <e-error-screen
          .title="${loc('MODEL_CATALOGUE_ERROR_TITLE')}"
          .subtitle="${loc('MODEL_CATALOGUE_ERROR_SUBTITLE')}"
        ></e-error-screen>
      `;
    }

    return html`
      <eui-layout-v0-multi-panel-tile tile-title="${this.tileTitle}" class="multi-panel-tile">
        <e-services-table .services=${this.filteredServices} slot="content"></e-services-table>
        <div class="action" slot="action" position="left">
          ${this.filtersPillMarkup} ${this.createServiceButtonMarkup}
        </div>
        <eui-layout-v0-tile-panel
          id="filter"
          tile-title="${loc('SEARCH_AND_FILTER')}"
          slot="left"
          icon-name="filter"
        >
          <e-sidebar
            class="panel"
            .filterList="${MODEL_SERVICES_FILTERS}"
            slot="content"
            .dateFilters="${[CREATED]}"
          ></e-sidebar>
        </eui-layout-v0-tile-panel>
      </eui-layout-v0-multi-panel-tile>
      <eui-base-v0-dialog
        id="dialog"
        class="upload-dialog"
        @eui-dialog:cancel="${this.toggleDialog}"
        .show="${this.showDialog}"
        no-cancel
        label="${loc('CREATE_MODEL_SERVICE')}"
      >
        <e-model-tabs id="modelTabs" slot="content"></e-model-tabs>
      </eui-base-v0-dialog>
    `;
  }
}

/**
 * Register the component as e-services-container.
 * Registration can be done at a later time and with a different name
 */
ServicesContainer.register();
