/**
 * Component ServicesTable is defined as
 * `<e-service-table>`
 *
 * Imperatively create component
 * @example
 * let component = new ServicesTable();
 *
 * Declaratively create component
 * @example
 * <e-service-table></e-service-table>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, nothing } from '@eui/lit-component';
import 'components/shared/custom-table/src/CustomTable';
import { formatDateToLocalDate, loc } from 'utils/Utils';
import { boundMethod } from 'autobind-decorator';
import SortingService from 'services/SortingService';
import { toTrainingPackageDetail, toModelServiceDetail } from 'utils/Navigator';
import {
  STATUS_RUNNING,
  STATUS_DEPLOYMENT_ERROR,
  STATUS_CREATING,
  STATUS_ERROR,
} from 'utils/Enums';
import { addNotification } from 'store/actions';
import { SERVICES_TABLE_COLUMNS } from 'utils/Defaults';
import style from './servicesTable.css';

/**
 * @property {Array} services - Services list array
 * @property {HTMLElement} table - Table DOM element
 * @property {boolean} showNoAccess - Show no access snippet
 */
@definition('e-services-table', {
  style,
  home: 'services-table',
  props: {
    services: { attribute: false, type: Array },
    table: { attribute: false, default: null },
    showNoAccess: { attribute: false, default: false },
  },
})
export default class ServicesTable extends LitComponent {
  didRender() {
    if (this.services && this.services.length > 0) {
      this.table = this.shadowRoot.getElementById('service-table');
      this.table.addEventListener('eui-table:sort', this.sortTable, false);
      this.table.addEventListener('row-click', this.viewServiceDetails, false);
    }
  }

  didDisconnect() {
    if (this.table) {
      this.table.removeEventListener('eui-table:sort', this.sortTable, false);
      this.table.removeEventListener('row-click', this.viewServiceDetails, false);

      this.table = null;
    }
  }

  /**
   * View package details page
   * @param service
   */
  viewServiceDetails(service) {
    const { col1, col8 } = service.detail;

    // Sorry
    if (col8 && col8.strings) {
      if (col8.strings[0].includes('red')) {
        return;
      }
    }
    toModelServiceDetail(col1);
  }

  /**
   * Returns talbe row for each service
   * @return {*}
   */
  get rows() {
    return this.services.map((service) => this.createRow(service));
  }

  /**
   * Returns services table columns
   * @return {Object[]}
   */
  get columns() {
    if (this.showNoAccess) {
      return [
        ...SERVICES_TABLE_COLUMNS,
        { title: loc('HAS_ACCESS'), attribute: 'col9', sortable: true },
      ];
    }
    return SERVICES_TABLE_COLUMNS;
  }

  /**
   * Returns structured object for table
   * @param {Object} service
   * @return {string}
   */
  createRow(service) {
    const columns = {
      col1: service.name,
      col3: this.getServiceStatusMarkup(service),
      col4: this.getInstancesMarkup(service),
      col5: this.getModelNumberMarkup(service),
      col6: this.formatModelType(service),
      col7: formatDateToLocalDate(service.created),
      col8: service.createdByUserName,
    };

    if (this.showNoAccess) {
      columns.col9 = this.hasAccess(service);
    }

    return columns;
  }

  hasAccess(service) {
    if (service?.noAccess) {
      return html`<eui-v0-icon name="cross" color="var(--red)"></eui-v0-icon>`;
    }
    return html`<eui-v0-icon name="check" color="var(--green)"></eui-v0-icon>`;
  }

  /**
   * Returns number of instances
   * @param {Object} service
   * @return {string}
   */
  getInstancesMarkup(service) {
    return service.autoScaling
      ? `${service.autoScaling.minReplicas}-${service.autoScaling.maxReplicas}`
      : service.replicas;
  }

  /**
   * Returns markup for model numbers in service
   * @param {Object} service
   * @return {*}
   */
  getModelNumberMarkup(service) {
    const messageBody = service.models.map(
      (model, i) => html` <span class="inline">${model.id}:${model.version}</span> `
    );

    return html`
      <eui-base-v0-tooltip id="tooltip"
        >${service.models.length}
        <div slot="message">
          ${messageBody}
        </div>
      </eui-base-v0-tooltip>
    `;
  }

  formatModelType(service) {
    return service.type;
  }

  /**
   * Returns markup for service status
   * @param {Object} service
   * @return {*}
   */
  getServiceStatusMarkup(service) {
    const { status } = service;
    let icon = '';
    let color = '';
    let rotating = false;
    switch (status) {
      case STATUS_RUNNING:
        icon = 'check';
        color = 'var(--green)';
        break;
      case STATUS_DEPLOYMENT_ERROR:
        icon = 'cross';
        color = 'var(--red)';
        break;
      case STATUS_CREATING:
        icon = 'dial';
        color = 'var(--orange)';
        rotating = true;
        break;
      default:
        break;
    }

    return html`
      <eui-v0-icon
        class="${service.status} ${rotating ? 'rotating' : ''}"
        name="${icon}"
        color="${color}"
      ></eui-v0-icon>
      <span>${loc(service.status.toUpperCase())}</span>
    `;
  }

  /**
   * Show error dialog
   * @param {Object} service - service
   */
  @boundMethod
  showErrorDialog(service) {
    const { message, errorLog } = service;
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
   * @param service
   */
  viewPackageDetails(service) {
    toTrainingPackageDetail(service.packageId, service.packageVersion);
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

  customCell(row, column, rowIndex, colIndex) {
    if (
      column.attribute === 'col1' ||
      column.attribute === 'col2' ||
      column.attribute === 'col3' ||
      column.attribute === 'col6' ||
      column.attribute === 'col7' ||
      column.attribute === 'col8'
    ) {
      return html` <eui-base-v0-tooltip
        message="${column.attribute === 'col3'
          ? row[column.attribute].values[0]
          : row[column.attribute]}"
        ><div class="status">${row[column.attribute]}</div>
      </eui-base-v0-tooltip>`;
    }

    // IMPORTANT
    return null;
  }

  /**
   * Render the <e-service-table> component. This function is called each time a
   * prop changes.
   */
  render() {
    if (!this.services || this.services.length === 0) {
      return nothing;
    }

    return html`
      <e-custom-table
        id="service-table"
        .data="${this.rows}"
        .columns=${this.columns}
        .cellFn=${this.customCell}
        sortable
        striped
      ></e-custom-table>
    `;
  }
}

/**
 * Register the component as e-services-table.
 * Registration can be done at a later time and with a different name
 */
ServicesTable.register();
