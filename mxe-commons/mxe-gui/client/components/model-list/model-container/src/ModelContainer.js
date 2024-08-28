/**
 * Component ModelContainer is defined as
 * `<e-model-container>`
 *
 * Imperatively create component
 * @example
 * let component = new ModelContainer();
 *
 * Declaratively create component
 * @example
 * <e-model-container></e-model-container>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent, repeat, nothing } from '@eui/lit-component';
import { MultiPanelTile } from '@eui/layout';
import { boundMethod } from 'autobind-decorator';
import 'components/model-list/model-card/src/ModelCard';
import 'components/model-list/model-list/src/ModelList';
import 'components/model-list/model-table/src/ModelTable';
import 'components/model-list/model-versions/src/ModelVersions';
import 'components/shared/list-sorting/src/ListSorting';
import 'components/shared/model-details/src/ModelDetails';
import 'components/shared/sidebar/src/Sidebar';
import 'components/shared/filter-pill/src/FilterPill';
import 'components/shared/error-screen/src/ErrorScreen';
import 'components/shared/view-change/src/ViewChange';
import { loc } from 'utils/Utils';
import {
  CLOSE_ONBOARD,
  CREATED,
  INVOKE_DELETE,
  INVOKE_UPDATE,
  SELECT_MODEL,
  SHOW_ERROR_DIALOG,
  SIDEBAR_FILTER_CHANGE,
  OPEN_FILTERS,
  STATUS_ERROR,
} from 'utils/Enums';
import { DEFAULT_FILTERS, DEFAULT_INTERVAL_MS, SORTING_OPTIONS } from 'utils/Defaults';
import ModelListingService from 'services/ModelListingService';
import FilteringService from 'services/FilteringService';
import SortingService from 'services/SortingService';
import ModelService from 'services/ModelService';
import PermissionService from 'services/PermissionService';
import { addNotification } from 'store/actions';
import style from './modelContainer.css';

/**
 * Model container
 */
@definition('e-model-container', {
  style,
  home: 'model-container',
  props: {
    error: { attribute: false, type: Boolean, default: false },
    filteredModels: { attribute: false, type: Array, default: [] },
    timer: { attribute: false, type: Number, default: null },
    selectedModel: { attribute: false, type: Array, default: [] },
    showOnboardingDialog: { attribute: false, type: Boolean, default: false },
    showConfirmDeleteDialog: { attribute: false, type: Boolean, default: false },
    isLoading: { attribute: false, type: Boolean, default: true },
    isListView: { attribute: false, type: Boolean, default: true },
  },
})
export default class ModelContainer extends LitComponent {
  constructor() {
    super();
    this.models = [];
    this.activeFilters = [];
    this.activeQuery = '';
  }

  async didConnect() {
    this.sortingOptions = [...SORTING_OPTIONS];
    const [defaultSort] = this.sortingOptions;
    this.sortBy = defaultSort;
    this.permissions = store.getState().permissions ?? {};
    await this.getModels();
  }

  didRender() {
    window.addEventListener(SIDEBAR_FILTER_CHANGE, this.onSidebarFilterChange, false);
    window.addEventListener(INVOKE_UPDATE, this.getModels, false);
    window.addEventListener(SELECT_MODEL, this.handleModelSelection, false);
    window.addEventListener(INVOKE_DELETE, this.handleModelDeletion, false);
    window.addEventListener(SHOW_ERROR_DIALOG, this.showErrorDialog, false);
    window.addEventListener(CLOSE_ONBOARD, this.closeDialogRequest, false);
    window.addEventListener(OPEN_FILTERS, this.openFilters, false);

    this.versionPanel = this.shadowRoot.getElementById('versions');
    this.filtersPanel = this.shadowRoot.getElementById('filter');

    if (!this.timer) {
      this.timer = setInterval(async () => {
        await this.getModels();
      }, DEFAULT_INTERVAL_MS);
    }
  }

  didDisconnect() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
    window.removeEventListener(SIDEBAR_FILTER_CHANGE, this.onSidebarFilterChange, false);
    window.removeEventListener(INVOKE_UPDATE, this.getModels, false);
    window.removeEventListener(SELECT_MODEL, this.handleModelSelection, false);
    window.removeEventListener(INVOKE_DELETE, this.handleModelDeletion, false);
    window.removeEventListener(SHOW_ERROR_DIALOG, this.showErrorDialog, false);
    window.removeEventListener(CLOSE_ONBOARD, this.closeDialogRequest, false);
    window.removeEventListener(OPEN_FILTERS, this.openFilters, false);

    this.versionPanel = null;
    this.filtersPill = null;
    this.errorDialog = null;
  }

  /**
   * Toggles the panel which contains the version list
   */
  @boundMethod
  closeVersionsPanel() {
    if (this.versionPanel) {
      this.versionPanel.toggleTilePanel();
    }
  }

  /**
   * Sets the selected model
   * @param {Object} event
   */
  @boundMethod
  handleModelSelection(event) {
    if (event.detail) {
      this.selectedModel = event.detail;
      if (this.versionPanel) {
        this.versionPanel.setAttribute('show', 'true');
      }
    }
  }

  /**
   * Toggles the panel which contains the filters
   */
  @boundMethod
  openFilters() {
    if (this.filtersPanel) {
      this.filtersPanel.setAttribute('show', 'true');
    }
  }

  /**
   * Closes the onboarding dialog
   */
  @boundMethod
  closeDialogRequest() {
    this.showOnboardingDialog = false;
  }

  /**
   * Sets the Model to be deleted and opens confirm dialog
   * @param {Object} event
   */
  @boundMethod
  handleModelDeletion(event) {
    const { detail } = event;
    if (detail) {
      this.modelToBeDeleted = detail;
      this.toggleConfirmDeleteDialog();
    }
  }

  /**
   * Deletes the previously set model, gets the new list and closes dialogs
   */
  @boundMethod
  async deleteModel() {
    try {
      const { id, version } = this.modelToBeDeleted;
      await ModelService.deleteModel(id, version);
      await this.getModels();
      await this.closeVersionsPanel();
      this.modelToBeDeleted = {};
      this.toggleConfirmDeleteDialog();
    } catch (e) {
      console.error(e);
    }
  }

  /**
   * Toggles the delete confirmation dialog
   */
  @boundMethod
  toggleConfirmDeleteDialog() {
    this.showConfirmDeleteDialog = !this.showConfirmDeleteDialog;
  }

  /**
   * Show error dialog
   */
  @boundMethod
  showErrorDialog(event) {
    const { message, errorLog } = event.detail;
    return store.dispatch(
      addNotification({
        title: message ?? loc('ERROR_HAPPENED'),
        description: errorLog ?? '',
        status: STATUS_ERROR,
      })
    );
  }

  /**
   * Get merged models
   */
  @boundMethod
  async getModels() {
    try {
      this.isLoading = true;
      this.models = await ModelListingService.getModels();
      this.filteredModels = this.models;
      this.filterModels(true);

      this.selectedModel = this.filteredModels.find((model) => model[0] === this.selectedModel[0]);

      this.isLoading = false;
    } catch (error) {
      console.error(error);
      this.error = true;
      this.isLoading = false;
    }
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
    this.selectedModel = [];
    this.filterModels();
  }

  /**
   * Filter and sort models
   */
  filterModels(isFirstFilter = false) {
    this.filteredModels = this.models;
    if (this.activeFilters && this.activeFilters.size) {
      this.filteredModels = FilteringService.filterModelsByStatus(
        this.filteredModels,
        this.activeFilters,
        false,
        isFirstFilter
      );
    }
    if (this.activeQuery) {
      this.filteredModels = FilteringService.filterModelsByName(
        this.filteredModels,
        this.activeQuery,
        false,
        isFirstFilter
      );
    }
    if (this.selectedDates) {
      this.filteredModels = FilteringService.filterModelsByDate(
        this.filteredModels,
        this.selectedDates
      );
    }
    this.sortModels(this.sortBy, isFirstFilter);
  }

  /**
   * Handle model sorting
   * @param {Object} option - Selected sorting method
   * @param {Boolean} isFirstFilter - Is first filter event
   */
  @boundMethod
  sortModels(option, isFirstFilter = false) {
    this.sortBy = option;
    const { by, order } = option;
    switch (by) {
      case 'name':
        this.filteredModels = SortingService.sortModelsByName(
          order,
          this.filteredModels,
          false,
          isFirstFilter
        );
        break;
      case 'date':
        this.filteredModels = SortingService.sortModelsByDate(
          order,
          this.filteredModels,
          isFirstFilter
        );
        break;
      case 'status':
        this.filteredModels = SortingService.sortModelsByStatus(
          order,
          this.filteredModels,
          isFirstFilter
        );
        break;
      default:
        this.filteredModels = this.models;
        break;
    }
  }

  /**
   * Returns title with number of results
   */
  get tileTitle() {
    if (this.isLoading) {
      return loc('LOADING');
    }

    return `${this.filteredModels.length} models in the list`;
  }

  /**
   * Returns the name / displayName of the selected model
   */
  get selectedModelName() {
    if (this.selectedModel) {
      return this.selectedModel.displayName
        ? this.selectedModel.displayName
        : this.selectedModel.title;
    }
    return '';
  }

  /**
   * Opens onboarding dialog
   * Sets the style if created for the first time
   */
  @boundMethod
  openOnboardingDialog() {
    // MatomoService.trackEvent(
    //   MATOMO_CATEGORY_MODEL_CATALOGUE,
    //   MATOMO_ACTION_CLICK,
    //   loc('ONBOARD_MODEL')
    // );
    this.showOnboardingDialog = true;
    if (this.showOnboardingDialog) {
      const uploadDialog = this.shadowRoot.querySelector('.upload-dialog');
      const dialog = uploadDialog.shadowRoot.querySelector('.dialog');

      dialog.setAttribute('style', 'width: 600px; height: 600px');
      const bottom = dialog.querySelector('.dialog__bottom');
      bottom.setAttribute('style', 'display: none');
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
   * Get either the list or the table view
   * @return {*}
   */
  get viewMarkup() {
    if (this.isListView) {
      return html`
        <e-model-list
          .models=${this.filteredModels}
          .selected="${this.selectedModel}"
          slot="content"
        ></e-model-list>
      `;
    }
    return html`
      <e-model-table
        .models=${this.filteredModels}
        .selected="${this.selectedModel}"
        slot="content"
      ></e-model-table>
    `;
  }

  get onBoardButtonMarkup() {
    return html`
      <eui-base-v0-button class="onboard" primary @click="${this.openOnboardingDialog}">
        ${loc('ONBOARD_MODEL')}
      </eui-base-v0-button>
    `;
  }

  get actionsMarkup() {
    if (this.isListView) {
      return html`
        <e-list-sorting
          .sortFn="${this.sortModels}"
          .sortingOptions="${this.sortingOptions}"
        ></e-list-sorting>
      `;
    }
    return nothing;
  }

  get filtersPillMarkup() {
    if ((this.activeFilters && this.activeFilters.size) || this.activeQuery.length > 0) {
      return html`<e-filter-pill></e-filter-pill>`;
    }
    return nothing;
  }

  /**
   * Render the <e-main-container> component. This function is called each time a
   * prop changes.
   */
  render() {
    if (this.error) {
      return html`
        <e-error-screen
          .title="${loc('MODEL_CATALOGUE_ERROR_TITLE')}"
          .subtitle="${loc('MODEL_CATALOGUE_ERROR_SUBTITLE')}"
        ></e-error-screen>
      `;
    }
    const packageTitle = this.selectedModel[0] || this.selectedModel[1];

    return html`
      <eui-layout-v0-multi-panel-tile id="multiPanelTile" tile-title="${this.tileTitle}">
        <eui-layout-v0-tile-panel
          id="filter"
          tile-title="${loc('SEARCH_AND_FILTER')}"
          slot="left"
          icon-name="filter"
        >
          <e-sidebar
            class="panel"
            .filterList="${DEFAULT_FILTERS}"
            slot="content"
            .dateFilters="${[CREATED]}"
          ></e-sidebar>
        </eui-layout-v0-tile-panel>

        ${this.viewMarkup}

        <div class="action" slot="action">
          ${this.filtersPillMarkup} ${this.actionsMarkup} ${this.onBoardButtonMarkup}
          <e-view-change
            .isListView="${this.isListView}"
            .toggleView="${this.toggleView}"
          ></e-view-change>
        </div>

        <eui-layout-v0-tile-panel
          id="versions"
          tile-title="${packageTitle || `${loc('MODEL_VERSIONS')}`}"
          subtitle="${packageTitle ? loc('MODEL_VERSIONS').toLowerCase() : ''}"
          slot="right"
          icon-name="routing"
          width="475"
        >
          <e-model-versions .model=${this.selectedModel[1]} slot="content"></e-model-versions>
        </eui-layout-v0-tile-panel>
      </eui-layout-v0-multi-panel-tile>
      <eui-base-v0-dialog
        class="upload-dialog"
        label="${loc('UPLOAD_MODEL')}"
        @eui-dialog:cancel="${this.closeDialogRequest}"
        .show="${this.showOnboardingDialog}"
        no-cancel
      >
        <e-upload-component
          .serviceReference="${ModelService.postModel}"
          slot="content"
        ></e-upload-component>
      </eui-base-v0-dialog>

      <eui-base-v0-dialog
        class="confirm-dialog delete"
        label=${loc('DIALOG_CONFIRM_TITLE')}
        @eui-dialog:cancel="${this.toggleConfirmDeleteDialog}"
        .show="${this.showConfirmDeleteDialog}"
      >
        <div slot="content" class="details">
          <span>${loc('DIALOG_CONFIRM_MODEL_DELETE')}</span>
        </div>
        <eui-base-v0-button
          slot="bottom"
          warning
          id="delete-confirm-${this.modelToBeDeleted?.id}-${this.modelToBeDeleted?.version}"
          @click=${this.deleteModel}
          >${loc('BUTTON_DELETE')}</eui-base-v0-button
        >
      </eui-base-v0-dialog>
    `;
  }
}

/**
 * Register the component as e-main-container.
 * Registration can be done at a later time and with a different name
 */
ModelContainer.register();
