/**
 * Component JobContainer is defined as
 * `<e-job-container>`
 *
 * Imperatively create component
 * @example
 * let component = new JobContainer();
 *
 * Declaratively create component
 * @example
 * <e-job-container></e-job-container>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent, nothing } from '@eui/lit-component';
import { DEFAULT_INTERVAL_MS, TRAINING_JOBS_FILTERS } from 'utils/Defaults';
import {
  COMPLETED,
  CREATED,
  INVOKE_UPDATE,
  JOB_TABLE_ROW_SELECTION,
  OPEN_FILTERS,
  SIDEBAR_FILTER_CHANGE,
} from 'utils/Enums';
import { boundMethod } from 'autobind-decorator';
import FilteringService from 'services/FilteringService';
import 'components/shared/sidebar/src/Sidebar';
import 'components/training-jobs/job-table/src/JobTable';
import 'components/shared/error-screen/src/ErrorScreen';
import 'components/shared/filter-pill/src/FilterPill';
import { loc } from 'utils/Utils';
import { API_BASE_URL } from 'utils/Config';
import TrainingJobsListingService from 'services/TrainingJobsListingService';
import style from './jobContainer.css';

/**
 * @property {Array} jobs - Job list array
 * @property {Array} filteredJobs - Filtered job list array
 * @property {Array} selectedRows - Selected rows
 * @property {Object} selectedJob - Selected job
 * @property {Object} error - Error object
 * @property {Boolean} isLoading - Is loading state
 * @property {Number} timer - Interval timer
 */
@definition('e-job-container', {
  style,
  home: 'job-container',
  props: {
    jobs: { attribute: false, type: Array, default: [] },
    filteredJobs: { attribute: false, type: Array, default: [] },
    selectedRows: { attribute: false, type: Array, default: [] },
    selectedJob: { attribute: false, type: Object, default: {} },
    error: { attribute: false, default: null },
    isLoading: { attribute: false, type: Boolean, default: true },
    timer: { attribute: false, type: Number, default: null },
  },
})
export default class JobContainer extends LitComponent {
  constructor() {
    super();
    this.activeFilters = [];
    this.activeQuery = '';
    this.selectedDates = [];
  }

  async didConnect() {
    await this.getTrainingJobs();
    if (!this.timer) {
      this.timer = setInterval(async () => {
        await this.getTrainingJobs();
      }, DEFAULT_INTERVAL_MS);
    }
  }

  didRender() {
    // window.addEventListener(INVOKE_PACKAGE_ACTION, this.handlePackageCardClick, false);
    window.addEventListener(SIDEBAR_FILTER_CHANGE, this.onSidebarFilterChange, false);
    window.addEventListener(JOB_TABLE_ROW_SELECTION, this.handleRowSelection, false);
    window.addEventListener(INVOKE_UPDATE, this.getTrainingJobs, false);
    window.addEventListener(OPEN_FILTERS, this.openFilters, false);

    this.filtersPanel = this.shadowRoot.getElementById('filter');
  }

  didDisconnect() {
    // window.removeEventListener(INVOKE_PACKAGE_ACTION, this.handlePackageCardClick, false);
    window.removeEventListener(SIDEBAR_FILTER_CHANGE, this.onSidebarFilterChange, false);
    window.removeEventListener(JOB_TABLE_ROW_SELECTION, this.handleRowSelection, false);
    window.removeEventListener(INVOKE_UPDATE, this.getTrainingJobs, false);
    window.removeEventListener(OPEN_FILTERS, this.openFilters, false);

    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }

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
    this.filterJobs();
  }

  @boundMethod
  handleRowSelection(event) {
    if (event.detail) {
      this.selectedRows = event.detail.filter((row) => !row.disabled);
    }
  }

  @boundMethod
  downloadResults() {
    this.selectedRows.forEach((row) =>
      this.download(`${API_BASE_URL}/training-jobs/${row.col1}/result`)
    );
  }

  delay(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }

  @boundMethod
  async download(url) {
    const a = document.createElement('a');
    a.download = url;
    a.href = url;
    a.style.display = 'none';
    document.body.append(a);
    a.click();

    // Chrome requires the timeout
    await this.delay(100);
    a.remove();
  }

  /**
   * Filter and sort jobs
   */
  filterJobs() {
    this.filteredJobs = this.jobs;
    if (this.activeFilters && this.activeFilters.size) {
      this.filteredJobs = FilteringService.filterJobsByStatus(
        this.filteredJobs,
        this.activeFilters
      );
    }
    if (this.activeQuery) {
      this.filteredJobs = FilteringService.filterJobsByName(this.filteredJobs, this.activeQuery);
    }

    if (this.selectedDates) {
      this.filteredJobs = FilteringService.filterJobsByDate(this.filteredJobs, this.selectedDates);
    }
  }

  /**
   * Get merged packages
   */
  @boundMethod
  async getTrainingJobs() {
    try {
      this.isLoading = true;
      this.jobs = await TrainingJobsListingService.getTrainingJobs();
      this.filteredJobs = this.jobs;
      this.isLoading = false;
    } catch (error) {
      console.error(error);
      this.error = true;
      this.isLoading = false;
    }
  }

  get isDownloadButtonAvailable() {
    return this.selectedRows.length === 0;
  }

  get tileTitle() {
    if (this.isLoading) {
      return loc('LOADING');
    }
    return `${this.filteredJobs.length} jobs`;
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
      <eui-layout-v0-multi-panel-tile tile-title="${this.tileTitle}">
        <e-job-table .jobs=${this.filteredJobs} slot="content"></e-job-table>
        <div class="action" slot="action" position="left">
          ${this.filtersPillMarkup}
          <eui-base-v0-button
            class="download"
            ?disabled="${this.isDownloadButtonAvailable}"
            primary
            @click="${this.downloadResults}"
          >
            ${loc('DOWNLOAD_RESULTS')}
          </eui-base-v0-button>
        </div>
        <eui-layout-v0-tile-panel
          id="filter"
          tile-title="${loc('SEARCH_AND_FILTER')}"
          slot="left"
          icon-name="filter"
        >
          <e-sidebar
            class="panel"
            .filterList="${TRAINING_JOBS_FILTERS}"
            slot="content"
            .dateFilters="${[CREATED, COMPLETED]}"
          ></e-sidebar>
        </eui-layout-v0-tile-panel>
      </eui-layout-v0-multi-panel-tile>
    `;
  }
}

/**
 * Register the component as e-job-container.
 * Registration can be done at a later time and with a different name
 */
JobContainer.register();
