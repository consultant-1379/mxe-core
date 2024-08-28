/**
 * Component JobTable is defined as
 * `<e-job-table>`
 *
 * Imperatively create component
 * @example
 * let component = new JobTable();
 *
 * Declaratively create component
 * @example
 * <e-job-table></e-job-table>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, nothing } from '@eui/lit-component';
import 'components/shared/custom-table/src/CustomTable';
import { formatDateToLocalDate, loc, preventDefaultEvent } from 'utils/Utils';
import { boundMethod } from 'autobind-decorator';
import SortingService from 'services/SortingService';
import {
  HIDE_ERROR_DIALOG,
  INVOKE_UPDATE,
  JOB_STATUS_COMPLETED,
  JOB_STATUS_FAILED,
  JOB_STATUS_RUNNING,
  JOB_TABLE_ROW_SELECTION,
  STATUS_ERROR,
} from 'utils/Enums';
import TrainingJobsService from 'services/TrainingJobsService';
import { toTrainingPackageDetail } from 'utils/Navigator';
import { addNotification } from 'store/actions';
import style from './jobTable.css';

/**
 * @property {Array} jobs - Job list array
 * @property {HTMLElement} table - Table DOM element
 */
@definition('e-job-table', {
  style,
  home: 'job-table',
  props: {
    jobs: { attribute: false, type: Array },
    table: { attribute: false, default: null },
    showDetails: { attribute: false, type: Boolean, default: true },
    showDeleteConfirmationDialog: { attribute: false, type: Boolean, default: false },
  },
})
export default class JobTable extends LitComponent {
  didRender() {
    if (!this.table && this.jobs && this.jobs.length > 0) {
      this.table = this.shadowRoot.getElementById('job-table');
      this.table.addEventListener('eui-table:sort', this.sortTable, false);
      this.table.addEventListener('row-selected', this.handleSelection, false);
      window.addEventListener(HIDE_ERROR_DIALOG, this.hideErrorDialog, false);
    }
  }

  didDisconnect() {
    if (this.table) {
      this.table.removeEventListener('eui-table:sort', this.sortTable, false);
      this.table.removeEventListener('row-selected', this.handleSelection, false);
      window.removeEventListener(HIDE_ERROR_DIALOG, this.hideErrorDialog, false);

      this.table = null;
    }
  }

  get rows() {
    return this.jobs.map((job) => this.createRow(job));
  }

  createRow(job) {
    return {
      col1: job.id,
      col2: formatDateToLocalDate(job.created),
      col3: this.getJobStatusMarkup(job),
      col4: formatDateToLocalDate(job.completed),
      col5: this.getActionButtonMarkup(job),
      col6: this.getMoreButtonMarkup(job),
      disabled: job.status === JOB_STATUS_RUNNING,
    };
  }

  @boundMethod
  getJobStatusMarkup(job) {
    const { status } = job;
    let icon = '';
    let color = '';
    let rotating = false;
    switch (status) {
      case JOB_STATUS_COMPLETED:
        icon = 'check';
        color = 'var(--green)';
        break;
      case JOB_STATUS_FAILED:
        icon = 'cross';
        color = 'var(--red)';
        break;
      case JOB_STATUS_RUNNING:
        icon = 'dial';
        color = 'var(--orange)';
        rotating = true;
        break;
      default:
        break;
    }

    return html`
      <eui-v0-icon
        class="${job.status} ${rotating ? 'rotating' : ''}"
        name="${icon}"
        color="${color}"
      ></eui-v0-icon>
      <span>${loc(job.status.toUpperCase())}</span>
    `;
  }

  @boundMethod
  getMoreButtonMarkup(job) {
    const viewPackage = this.showDetails
      ? html`<eui-base-v0-menu-item
          .label="${loc('VIEW_PACKAGE_DETAILS')}"
          tabindex="0"
          @click="${() => this.viewPackageDetails(job)}"
          ;
        >
        </eui-base-v0-menu-item>`
      : nothing;
    return html`
      <eui-base-v0-dropdown data-type="click" label="${this.job}" more>
        ${viewPackage}
        </eui-base-v0-menu-item>
        <eui-base-v0-menu-item
          .label="${loc('DELETE_JOB')}"
          tabindex="1"
          @click="${() => {
            this._handleDelete(job);
          }}"
        >
        </eui-base-v0-menu-item>
      </eui-base-v0-dropdown>
    `;
  }

  @boundMethod
  getActionButtonMarkup(job) {
    switch (job.status) {
      case JOB_STATUS_COMPLETED:
        return html`
          <eui-base-v0-button
            class="action"
            primary
            fullwidth
            href="${`/v1/training-jobs/${job.id}/result`}"
            >${loc('DOWNLOAD_RESULT')}</eui-base-v0-button
          >
        `;
      case JOB_STATUS_FAILED:
        return html`
          <eui-base-v0-button
            class="action"
            fullwidth
            primary
            @click="${(event) => {
              preventDefaultEvent(event);
              this.showErrorDialog(job);
            }}"
            >${loc('VIEW_LOG')}</eui-base-v0-button
          >
        `;
      case JOB_STATUS_RUNNING:
      default:
        return nothing;
    }
  }

  @boundMethod
  _handleDelete(job) {
    this.jobToDelete = job;
    this.showDeleteConfirmationDialog = true;
  }

  @boundMethod
  _handleDeleteCancel() {
    this.showDeleteConfirmationDialog = false;
    this.jobToDelete = undefined;
  }

  @boundMethod
  _handleDeleteConfirm() {
    this.showDeleteConfirmationDialog = false;
    this.deleteJob(this.jobToDelete);
  }

  @boundMethod
  async deleteJob(job) {
    const { id } = job;
    await TrainingJobsService.deleteTrainingJob(id);
    await this.bubble(INVOKE_UPDATE);
  }

  /**
   * Show error dialog
   * @param {Object} job - job
   */
  @boundMethod
  showErrorDialog(job) {
    const { message, errorLog } = job;
    return store.dispatch(
      addNotification({
        title: message ?? loc('ERROR_HAPPENED'),
        description: errorLog ?? '',
        status: STATUS_ERROR,
      })
    );
  }

  /**
   * View package details page
   * @param job
   */
  viewPackageDetails(job) {
    toTrainingPackageDetail(job.packageId, job.packageVersion);
  }

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

  @boundMethod
  handleSelection(event) {
    if (event.detail) {
      this.bubble(JOB_TABLE_ROW_SELECTION, event.detail);
    }
  }

  customCell(row, column, rowIndex, colIndex) {
    if (
      column.attribute === 'col1' ||
      column.attribute === 'col2' ||
      column.attribute === 'col3' ||
      column.attribute === 'col4'
    ) {
      return html` <eui-base-v0-tooltip
        message="${column.attribute === 'col3'
          ? row[column.attribute].values[0]
          : row[column.attribute]}"
        ><div class="status">${row[column.attribute]}</div>
      </eui-base-v0-tooltip>`;
    }
    if (column.attribute === 'col5' || column.attribute === 'col6') {
      // provide some custom CSS styling for this cell. "custom-table__cell" is
      // defined in the accompanying CSS file for this component.
      return html`<div class="actions">${row[column.attribute]}</div>`;
    }

    // IMPORTANT
    return null;
  }

  _deleteConfirmationDialogMarkup() {
    return html`<eui-base-v0-dialog
      label=${loc('DIALOG_CONFIRM_TITLE')}
      .show="${this.showDeleteConfirmationDialog}"
      @eui-dialog:cancel="${this._handleDeleteCancel}"
    >
      <div slot="content">
        ${loc('DIALOG_CONFIRM_TRAINING_JOB_DELETE')}
      </div>
      <eui-base-v0-button slot="bottom" @click=${this._handleDeleteConfirm} warning
        >${loc('BUTTON_DELETE')}</eui-base-v0-button
      >
    </eui-base-v0-dialog>`;
  }

  /**
   * Render the <e-job-table> component. This function is called each time a
   * prop changes.
   */
  render() {
    if (!this.jobs || this.jobs.length === 0) {
      return nothing;
    }

    return html`
      <e-custom-table
        id="job-table"
        .data="${this.rows}"
        sortable
        multi-select
        striped
        .cellFn=${this.customCell}
      ></e-custom-table>
      ${this._deleteConfirmationDialogMarkup()}
    `;
  }
}

/**
 * Register the component as e-job-table.
 * Registration can be done at a later time and with a different name
 */
JobTable.register();
