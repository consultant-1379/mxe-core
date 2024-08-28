/**
 * Component NotebookContainer is defined as
 * `<e-notebook-container>`
 *
 * Imperatively create component
 * @example
 * let component = new NotebookContainer();
 *
 * Declaratively create component
 * @example
 * <e-notebook-container></e-notebook-container>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import '@eui/layout';
import { html, LitComponent, nothing } from '@eui/lit-component';
import { boundMethod } from 'autobind-decorator';
import 'components/notebooks/notebook-list/src/NotebookList';
import 'components/notebooks/notebook-table/src/NotebookTable';
import 'components/notebooks/create-notebook/src/CreateNotebook';
import 'components/shared/sidebar/src/Sidebar';
import 'components/shared/list-sorting/src/ListSorting';
import 'components/shared/error-screen/src/ErrorScreen';
import 'components/shared/filter-pill/src/FilterPill';
import 'components/shared/view-change/src/ViewChange';
import FilteringService from 'services/FilteringService';
import NotebookListingService from 'services/NotebookListingService';
import SortingService from 'services/SortingService';
import { DEFAULT_INTERVAL_MS, NOTEBOOK_FILTERS, SORTING_OPTIONS_INFO_PAGE } from 'utils/Defaults';
import { loc } from 'utils/Utils';
import {
  INVOKE_DELETE,
  INVOKE_UPDATE,
  OPEN_FILTERS,
  SIDEBAR_FILTER_CHANGE,
  UPLOAD_DIALOG_CLOSE_REQUEST_APPROVED,
  UPLOAD_DIALOG_CLOSE_REQUEST_DENIED,
  UPLOAD_DIALOG_CLOSE_REQUESTED,
} from 'utils/Enums';
import style from './notebookContainer.css';

/**
 * Main container
 */
@definition('e-notebook-container', {
  style,
  home: 'notebook-container',
  props: {
    error: { attribute: false, type: Boolean, default: false },
    filteredNotebooks: { attribute: false, type: Array, default: [] },
    timer: { attribute: false, type: Number, default: null },
    showOnboardingDialog: { attribute: false, type: Boolean, default: false },
    showConfirmDeleteDialog: { attribute: false, type: Boolean, default: false },
    isLoading: { attribute: false, type: Boolean, default: true },
    isListView: { attribute: false, type: Boolean, default: true },
  },
})
export default class NotebookContainer extends LitComponent {
  constructor() {
    super();
    this.notebooks = [];
    this.activeFilters = [];
    this.activeQuery = '';
    this.selectedNotebook = null;
  }

  didRender() {
    window.addEventListener(SIDEBAR_FILTER_CHANGE, this.onSidebarFilterChange, false);
    window.addEventListener(INVOKE_UPDATE, this.getNotebooks, false);
    window.addEventListener(INVOKE_DELETE, this.showDeleteDialog, false);
    window.addEventListener(OPEN_FILTERS, this.openFilters, false);

    this.filtersPanel = this.shadowRoot.getElementById('filter');

    if (!this.timer) {
      this.timer = setInterval(async () => {
        await this.getNotebooks();
      }, DEFAULT_INTERVAL_MS);
    }
  }

  didDisconnect() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
    window.removeEventListener(SIDEBAR_FILTER_CHANGE, this.onSidebarFilterChange);
    window.removeEventListener(INVOKE_UPDATE, this.getNotebooks);
    window.removeEventListener(INVOKE_DELETE, this.showDeleteDialog);
    window.removeEventListener(OPEN_FILTERS, this.openFilters, false);
  }

  async didConnect() {
    this.sortingOptions = [...SORTING_OPTIONS_INFO_PAGE];
    // eslint-disable-next-line prefer-destructuring
    this.sortBy = this.sortingOptions[0];
    await this.getNotebooks();
  }

  /**
   * Opens sidebar
   */
  @boundMethod
  openFilters() {
    if (this.filtersPanel) {
      this.filtersPanel.setAttribute('show', 'true');
    }
  }

  /**
   * Returns markup for filter pill
   * @return {*}
   */
  get filtersPillMarkup() {
    if ((this.activeFilters && this.activeFilters.size) || this.activeQuery.length > 0) {
      return html` <e-filter-pill></e-filter-pill> `;
    }
    return nothing;
  }

  /**
   * Opens delete dialog
   * @param {Object} event
   */
  @boundMethod
  showDeleteDialog(event) {
    this.notebookToBeDeleted = event.detail.notebook || null;
    this.toggleConfirmDeleteDialog();
  }

  /**
   * Toggles confirm dialog
   */
  @boundMethod
  toggleConfirmDeleteDialog() {
    this.showConfirmDeleteDialog = !this.showConfirmDeleteDialog;
  }

  /**
   * Toggles deploy dialog
   */
  @boundMethod
  toggleDeployDialog() {
    // MatomoService.trackEvent(
    //   MATOMO_CATEGORY_NOTEBOOK_LIST,
    //   MATOMO_ACTION_CLICK,
    //   loc('CREATE_NOTEBOOK')
    // );
    this.showOnboardingDialog = !this.showOnboardingDialog;
  }

  /**
   * Get merged notebooks
   */
  @boundMethod
  async getNotebooks() {
    try {
      this.isLoading = true;
      this.notebooks = await NotebookListingService.getNotebooks();
      this.filteredNotebooks = this.notebooks;
      this.filterNotebooks();
      this.isLoading = false;
    } catch (error) {
      console.error(error);
      this.error = true;
      this.isLoading = false;
    }
  }

  /**
   * Deletes notebook
   */
  @boundMethod
  async deleteNotebook() {
    if (this.notebookToBeDeleted) {
      this.toggleConfirmDeleteDialog();
      this.bubble(INVOKE_UPDATE);
    }
  }

  /**
   * Handle filter events from the sidebar component
   * @param {Object} event
   */
  @boundMethod
  onSidebarFilterChange(event) {
    const { filters, query } = event.detail;
    this.activeFilters = filters;
    this.activeQuery = query;
    this.filterNotebooks();
  }

  /**
   * Filter and sort notebooks
   */
  filterNotebooks() {
    this.filteredNotebooks = this.notebooks;
    if (this.activeQuery) {
      this.filteredNotebooks = FilteringService.filterNotebooksByName(
        this.filteredNotebooks,
        this.activeQuery
      );
    }
    this.sortNotebooks(this.sortBy);
  }

  /**
   * Handle notebook sorting
   * @param {Object} option - Selected sorting method
   */
  @boundMethod
  sortNotebooks(option) {
    this.sortBy = option;
    const { by, order } = option;
    if (by === 'name') {
      this.filteredNotebooks = SortingService.sortNotebooksByName(order, this.filteredNotebooks);
    } else {
      this.filteredNotebooks = this.notebooks;
    }
  }

  /**
   * Returns title with number of results
   */
  get tileTitle() {
    if (this.isLoading) {
      return loc('LOADING');
    }
    return `${this.filteredNotebooks.length} notebook deployments`;
  }

  /**
   * Opens onboarding dialog
   * Sets the style if created for the first time
   */
  @boundMethod
  openOnboardingDialog() {
    this.showOnboardingDialog = true;
    if (this.showOnboardingDialog) {
      const uploadDialog = this.shadowRoot.querySelector('.upload-dialog');
      const dialog = uploadDialog.shadowRoot.querySelector('.dialog');
      const dialogBody = uploadDialog.shadowRoot.querySelector('.dialog__body');
      dialog.setAttribute('style', 'max-width: 600px; max-height: 600px');
      dialogBody.setAttribute('style', 'display: flex');
    }
  }

  /**
   * Dialog close request handler
   * Opens confirmation dialog
   * @param event
   */
  @boundMethod
  closeDialogRequest(event) {
    const dialog = event.target;
    if (dialog.showDialog) {
      dialog.showDialog();
      window.addEventListener(
        UPLOAD_DIALOG_CLOSE_REQUEST_APPROVED,
        () => this.handleCloseDialogRequestResult(dialog, true),
        false
      );
      window.addEventListener(
        UPLOAD_DIALOG_CLOSE_REQUEST_DENIED,
        () => this.handleCloseDialogRequestResult(dialog, false),
        false
      );
      this.bubble(UPLOAD_DIALOG_CLOSE_REQUESTED);
    }
  }

  /**
   * Handles close confirmation dialog result for upload dialog
   * @param {HTMLElement} dialog
   * @param {Boolean} closable
   */
  @boundMethod
  handleCloseDialogRequestResult(dialog, closable) {
    if (closable) {
      dialog.classList.remove('show');
      this.showOnboardingDialog = false;
    }
    window.removeEventListener(
      UPLOAD_DIALOG_CLOSE_REQUEST_APPROVED,
      () => this.handleCloseDialogRequestResult(dialog, true),
      false
    );
    window.removeEventListener(
      UPLOAD_DIALOG_CLOSE_REQUEST_DENIED,
      () => this.handleCloseDialogRequestResult(dialog, false),
      false
    );
  }

  /**
   * Returns markup for sorting
   * @return {*}
   */
  get sortingMarkup() {
    return html`
      <span class="sort-title">Sort by &nbsp;</span>
      <e-list-sorting
        .sortFn="${this.sortNotebooks}"
        .sortingOptions="${this.sortingOptions}"
      ></e-list-sorting>
    `;
  }

  /**
   * Return chosen markup
   * @return {*}
   */
  get viewMarkup() {
    if (this.isListView) {
      return html`
        <e-notebook-list slot="content" .notebooks=${this.filteredNotebooks}></e-notebook-list>
      `;
    }
    return html`
      <e-notebook-table slot="content" .notebooks=${this.filteredNotebooks}></e-notebook-table>
    `;
  }

  /**
   * Toggles between list and table view
   */
  @boundMethod
  toggleView() {
    this.isListView = !this.isListView;
  }

  /**
   * Render the <e-main-container> component. This function is called each time a
   * prop changes.
   */
  render() {
    if (this.error) {
      return html`
        <e-error-screen
          .title="${loc('NOTEBOOK_DEPLOYMENT_ERROR_TITLE')}"
          .subtitle="${loc('NOTEBOOK_DEPLOYMENT_ERROR_SUBTITLE')}"
        ></e-error-screen>
      `;
    }
    return html`
      <eui-layout-v0-multi-panel-tile tile-title="${this.tileTitle}">
        <eui-layout-v0-tile-panel
          id="filter"
          tile-title="${loc('SEARCH_AND_FILTER')}"
          slot="left"
          icon-name="filter"
        >
          <e-sidebar class="panel" .filterList="${NOTEBOOK_FILTERS}" slot="content"></e-sidebar>
        </eui-layout-v0-tile-panel>
        ${this.viewMarkup}
        <div class="action" slot="action" position="left">
          ${this.filtersPillMarkup} ${this.sortingMarkup}
          <e-view-change
            .isListView="${this.isListView}"
            .toggleView="${this.toggleView}"
          ></e-view-change>
        </div>
        <eui-base-v0-dialog
          no-cancel
          class="deploy-dialog"
          label="${loc('CREATE_DEPLOYMENT')}"
          @eui-dialog:cancel="${this.toggleDeployDialog}"
          .show="${this.showOnboardingDialog}"
          slot="content"
        >
          <e-create-deployment slot="content" class="content"></e-create-deployment>
        </eui-base-v0-dialog>
        <eui-base-v0-dialog
          class="confirm-dialog delete"
          label=${loc('DIALOG_CONFIRM_TITLE')}
          @eui-dialog:cancel="${this.toggleConfirmDeleteDialog}"
          .show="${this.showConfirmDeleteDialog}"
          slot="content"
        >
          <div slot="content" class="details">
            <span>${loc('DIALOG_CONFIRM_NOTEBOOK_DELETE')}</span>
          </div>
          <eui-base-v0-button slot="bottom" warning @click=${this.deleteNotebook}
            >${loc('BUTTON_DELETE')}</eui-base-v0-button
          >
        </eui-base-v0-dialog>
      </eui-layout-v0-multi-panel-tile>
    `;
  }
}
/**
 * Register the component as e-main-container.
 * Registration can be done at a later time and with a different name
 */
NotebookContainer.register();
