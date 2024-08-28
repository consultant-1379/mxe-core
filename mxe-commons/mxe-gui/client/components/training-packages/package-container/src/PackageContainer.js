/**
 * Component PackageContainer is defined as
 * `<e-package-container>`
 *
 * Imperatively create component
 * @example
 * let component = new PackageContainer();
 *
 * Declaratively create component
 * @example
 * <e-package-container></e-package-container>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, nothing } from '@eui/lit-component';
import { MultiPanelTile } from '@eui/layout';
import 'components/shared/sidebar/src/Sidebar';
import 'components/training-packages/package-card/src/PackageCard';
import 'components/training-packages/package-list/src/PackageList';
import 'components/training-packages/package-table/src/PackageTable';
import 'components/training-packages/package-versions/src/PackageVersions';
import 'components/shared/error-screen/src/ErrorScreen';
import 'components/shared/upload-component/src/UploadComponent';
import 'components/shared/list-sorting/src/ListSorting';
import 'components/shared/filter-pill/src/FilterPill';
import 'components/shared/view-change/src/ViewChange';
import { DEFAULT_INTERVAL_MS, SORTING_OPTIONS, TRAINING_PACKAGES_FILTERS } from 'utils/Defaults';
import { loc } from 'utils/Utils';
import { boundMethod } from 'autobind-decorator';
import {
  CLOSE_ONBOARD,
  CLOSE_VERSIONS_PANEL,
  CREATED,
  HIDE_ERROR_DIALOG,
  INVOKE_PACKAGE_ACTION,
  INVOKE_UPDATE,
  SHOW_ERROR_DIALOG,
  SIDEBAR_FILTER_CHANGE,
  UPLOAD_DIALOG_CLOSE_REQUEST_APPROVED,
  UPLOAD_DIALOG_CLOSE_REQUEST_DENIED,
  UPLOAD_DIALOG_CLOSE_REQUESTED,
  OPEN_FILTERS,
  STATUS_ERROR,
} from 'utils/Enums';
import TrainingPackageListingService from 'services/TrainingPackageListingService';
import FilteringService from 'services/FilteringService';
import SortingService from 'services/SortingService';
import TrainingPackagesService from 'services/TrainingPackagesService';
import { addNotification } from 'store/actions';
import style from './packageContainer.css';

/**
 * @property {boolean} propOne - show active/inactive state.
 * @property {boolean} propTwo - show active/inactive state.
 */
@definition('e-package-container', {
  style,
  home: 'package-container',
  props: {
    packages: { attribute: false, type: Array, default: [] },
    filteredPackages: { attribute: false, type: Array, default: [] },
    selectedPackage: { attribute: false, type: Object, default: {} },
    showOnboardingDialog: { attribute: false, type: Boolean, default: false },
    timer: { attribute: false, type: Number, default: null },
    error: { attribute: false, default: null },
    isLoading: { attribute: false, type: Boolean, default: true },
    isListView: { attribute: false, type: Boolean, default: true },
  },
})
export default class PackageContainer extends LitComponent {
  constructor() {
    super();
    this.activeFilters = [];
    this.activeQuery = '';
  }

  async didConnect() {
    this.sortingOptions = [...SORTING_OPTIONS];
    // eslint-disable-next-line prefer-destructuring
    this.sortBy = this.sortingOptions[0];
    await this.getTrainingPackages();
  }

  didRender() {
    window.addEventListener(INVOKE_PACKAGE_ACTION, this.handlePackageCardClick, false);
    window.addEventListener(SIDEBAR_FILTER_CHANGE, this.onSidebarFilterChange, false);
    window.addEventListener(INVOKE_UPDATE, this.getTrainingPackages, false);
    window.addEventListener(SHOW_ERROR_DIALOG, this.showErrorDialog, false);
    window.addEventListener(HIDE_ERROR_DIALOG, this.hideErrorDialog, false);
    window.addEventListener(CLOSE_VERSIONS_PANEL, this.closeVersionsPanel, false);
    window.addEventListener(CLOSE_ONBOARD, this.closeDialogRequest, false);
    window.addEventListener(OPEN_FILTERS, this.openFilters, false);

    this.filtersPanel = this.shadowRoot.getElementById('filter');
    this.versionPanel = this.shadowRoot.getElementById('versions');

    if (!this.timer) {
      this.timer = setInterval(async () => {
        await this.getTrainingPackages();
      }, DEFAULT_INTERVAL_MS);
    }
  }

  didDisconnect() {
    window.removeEventListener(INVOKE_PACKAGE_ACTION, this.handlePackageCardClick, false);
    window.removeEventListener(INVOKE_UPDATE, this.getTrainingPackages, false);
    window.removeEventListener(SIDEBAR_FILTER_CHANGE, this.onSidebarFilterChange, false);
    window.removeEventListener(SHOW_ERROR_DIALOG, this.showErrorDialog, false);
    window.removeEventListener(HIDE_ERROR_DIALOG, this.hideErrorDialog, false);
    window.removeEventListener(CLOSE_VERSIONS_PANEL, this.closeVersionsPanel, false);
    window.removeEventListener(CLOSE_ONBOARD, this.closeDialogRequest, false);
    window.removeEventListener(OPEN_FILTERS, this.openFilters, false);

    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }

    this.versionPanel = null;
  }

  /**
   * Opens sidebar with filters
   */
  @boundMethod
  openFilters() {
    if (this.filtersPanel) {
      this.filtersPanel.setAttribute('show', 'true');
    }
  }

  get filtersPillMarkup() {
    if ((this.activeFilters && this.activeFilters.size) || this.activeQuery.length > 0) {
      return html` <e-filter-pill></e-filter-pill> `;
    }
    return nothing;
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
    this.filterPackages();
  }

  /**
   * Closes panels which contain verion cards
   */
  @boundMethod
  closeVersionsPanel() {
    if (this.versionPanel) {
      this.versionPanel.toggleTilePanel();
    }
  }

  /**
   * Handles card click event
   * @param {Object} event
   */
  @boundMethod
  handlePackageCardClick(event) {
    if (event.detail) {
      this.selectedPackage = event.detail;
      if (this.versionPanel) {
        this.versionPanel.setAttribute('show', 'true');
      }
    }
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
   * Get merged packages
   */
  @boundMethod
  async getTrainingPackages() {
    try {
      this.isLoading = true;
      this.packages = await TrainingPackageListingService.getTrainingPackages();
      this.filteredPackages = this.packages;
      this.filterPackages();

      this.selectedPackage = this.filteredPackages.find(
        (_package) => _package[0] === this.selectedPackage[0]
      );

      this.isLoading = false;
    } catch (error) {
      console.error(error);
      this.error = true;
      this.isLoading = false;
    }
  }

  /**
   * Filter and sort packages
   */
  filterPackages() {
    this.filteredPackages = this.packages;
    if (this.activeFilters && this.activeFilters.size) {
      this.filteredPackages = FilteringService.filterPackagesByStatus(
        this.filteredPackages,
        this.activeFilters
      );
    }
    if (this.activeQuery) {
      this.filteredPackages = FilteringService.filterPackagesByName(
        this.filteredPackages,
        this.activeQuery
      );
    }
    if (this.selectedDates) {
      this.filteredPackages = FilteringService.filterPackagesByDate(
        this.filteredPackages,
        this.selectedDates
      );
    }
    this.sortPackages(this.sortBy);
  }

  /**
   * Handle package sorting
   * @param {Object} option - Selected sorting method
   */
  @boundMethod
  sortPackages(option) {
    this.sortBy = option;
    const { by, order } = option;
    switch (by) {
      case 'name':
        this.filteredPackages = SortingService.sortPackagesByName(
          order,
          this.filteredPackages,
          false
        );
        break;
      case 'date':
        this.filteredPackages = SortingService.sortPackagesByDate(order, this.filteredPackages);
        break;
      case 'status':
        this.filteredPackages = SortingService.sortPackagesByStatus(order, this.filteredPackages);
        break;
      default:
        this.filteredPackages = this.packages;
        break;
    }
  }

  /**
   * Opens onboarding dialog
   * Sets the style if created for the first time
   */
  @boundMethod
  openOnboardingDialog() {
    // eslint-disable-next-line no-undef
    // MatomoService.trackEvent(
    //   MATOMO_CATEGORY_TRAINING_PACKAGE_LIST,
    //   MATOMO_ACTION_CLICK,
    //   loc('ONBOARD_MODEL')
    // );
    this.showOnboardingDialog = true;
    if (this.showOnboardingDialog) {
      const uploadDialog = this.shadowRoot.querySelector('.upload-dialog');
      const dialog = uploadDialog.shadowRoot.querySelector('.dialog');
      const dialogBody = uploadDialog.shadowRoot.querySelector('.dialog__body');
      dialog.setAttribute('style', 'width: 600px; height: 600px');
      const bottom = dialog.querySelector('.dialog__bottom');
      bottom.setAttribute('style', 'display: none');
      dialogBody.setAttribute('style', 'display: flex');
    }
  }

  /**
   * Dialog close request handler
   * Opens confirmation dialog
   */
  @boundMethod
  closeDialogRequest(event) {
    const dialog = this.shadowRoot.querySelector('.upload-dialog');
    if (dialog) {
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
   * Returns selected sorting method
   */
  get currentSorting() {
    return this.sortBy;
  }

  /**
   * Returns tile title
   */
  get tileTitle() {
    if (this.isLoading) {
      return loc('LOADING');
    }
    return `${this.filteredPackages.length} packages`;
  }

  /**
   * Returns selected view's markup
   * @return {*}
   */
  get viewMarkup() {
    if (this.isListView) {
      return html`
        <e-package-list
          .packages=${this.filteredPackages}
          .selected="${this.selectedPackage}"
          slot="content"
        ></e-package-list>
      `;
    }
    return html`
      <e-package-table
        .packages=${this.filteredPackages}
        .selected="${this.selectedPackage}"
        slot="content"
      ></e-package-table>
    `;
  }

  /**
   * Returns markup for sorting
   * @return {*}
   */
  get sortingMarkup() {
    return this.isListView
      ? html`
          <e-list-sorting
            .sortFn="${this.sortPackages}"
            .sortingOptions="${this.sortingOptions}"
          ></e-list-sorting>
        `
      : nothing;
  }

  /**
   * Toggles between list and table view
   */
  @boundMethod
  toggleView() {
    this.isListView = !this.isListView;
  }

  /**
   * Render the <e-package-container> component. This function is called each time a
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

    const packageName =
      (this.selectedPackage[1] && this.selectedPackage[1][0].title) ||
      this.selectedPackage[0] ||
      '';

    return html`
      <eui-layout-v0-multi-panel-tile id="multiPanelTile" tile-title="${this.tileTitle}">
        <div class="action" slot="action" position="left">
          ${this.filtersPillMarkup} ${this.sortingMarkup}
          <eui-base-v0-button class="onboard" primary @click="${this.openOnboardingDialog}">
            ${loc('ONBOARD_PACKAGE')}
          </eui-base-v0-button>
          ${this.deleteButtonMarkup}
          <e-view-change
            .isListView="${this.isListView}"
            .toggleView="${this.toggleView}"
          ></e-view-change>
        </div>
        <div slot="content">
          ${this.viewMarkup}
        </div>

        <eui-layout-v0-tile-panel
          id="filter"
          tile-title="${loc('SEARCH_AND_FILTER')}"
          slot="left"
          icon-name="filter"
        >
          <div slot="content">
            <e-sidebar
              class="panel"
              .filterList="${TRAINING_PACKAGES_FILTERS}"
              slot="content"
              .dateFilters="${[CREATED]}"
            ></e-sidebar>
          </div>
        </eui-layout-v0-tile-panel>

        <eui-layout-v0-tile-panel
          id="versions"
          tile-title="${packageName || `${loc('PACKAGE_VERSIONS')}`}"
          subtitle="${packageName !== '' ? loc('PACKAGE_VERSIONS').toLowerCase() : ''}"
          slot="right"
          icon-name="routing"
          width="475"
        >
          <e-package-versions .package=${this.selectedPackage} slot="content"></e-package-versions>
        </eui-layout-v0-tile-panel>
      </eui-layout-v0-multi-panel-tile>
      <eui-base-v0-dialog
        class="upload-dialog"
        label="${loc('UPLOAD_TRAINING_PACKAGES')}"
        @eui-dialog:cancel="${this.closeDialogRequest}"
        .show="${this.showOnboardingDialog}"
        no-cancel
      >
        <e-upload-component
          .serviceReference="${TrainingPackagesService.postTrainingPackage}"
          slot="content"
          class="content"
        ></e-upload-component>
      </eui-base-v0-dialog>
    `;
  }
}

/**
 * Register the component as e-package-container.
 * Registration can be done at a later time and with a different name
 */
PackageContainer.register();
