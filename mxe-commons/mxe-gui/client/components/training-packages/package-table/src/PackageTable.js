/**
 * Component PackageTable is defined as
 * `<e-package-table>`
 *
 * Imperatively create component
 * @example
 * let component = new PackageTable();
 *
 * Declaratively create component
 * @example
 * <e-package-table></e-package-table>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html } from '@eui/lit-component';
import 'components/shared/custom-table/src/CustomTable';
import { formatDateToLocalDate, loc, preventDefaultEvent } from 'utils/Utils';
import { boundMethod } from 'autobind-decorator';
import SortingService from 'services/SortingService';
import {
  STATUS_AVAILABLE,
  STATUS_ERROR,
  STATUS_PACKAGING,
  INVOKE_PACKAGE_ACTION,
} from 'utils/Enums';
import { PACKAGES_TABLE_COLUMNS } from 'utils/Defaults';
import style from './packageTable.css';

/**
 * @property {Array} packages - Package list array
 * @property {HTMLElement} table - Table DOM element
 */
@definition('e-package-table', {
  style,
  home: 'package-table',
  props: {
    packages: { attribute: false, type: Array, default: [] },
    table: { attribute: false, default: null },
  },
})
export default class PackageTable extends LitComponent {
  didRender() {
    this.table = this.shadowRoot.getElementById('package-table');
    this.table.addEventListener('eui-table:sort', this.sortTable, false);
    this.table.addEventListener('row-click', this.handleRowClick, false);
  }

  didDisconnect() {
    this.table.removeEventListener('eui-table:sort', this.sortTable, false);
    this.table.removeEventListener('row-click', this.handleRowClick, false);
    this.table = null;
  }

  get rows() {
    return this.packages.map((package_) => this.createRow(package_[1][0]));
  }

  /**
   * Returns structured object for table
   * @param {Object} package_
   * @return {object}
   */
  createRow(package_) {
    return {
      col1: package_.title,
      col2: formatDateToLocalDate(package_.created),
      col3: package_.author,
      col4: this.getPackageStatusMarkup(package_),
      col5: package_.id,
      col6: package_.version,
    };
  }

  /**
   * Returns package status markup
   * @param {Object} package_
   * @return {*}
   */
  @boundMethod
  getPackageStatusMarkup(package_) {
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
      column.attribute === 'col4' ||
      column.attribute === 'col5' ||
      column.attribute === 'col6'
    ) {
      return html` <eui-base-v0-tooltip
        message="${column.attribute === 'col4'
          ? row[column.attribute].values[0]
          : row[column.attribute]}"
        ><div class="status">${row[column.attribute]}</div>
      </eui-base-v0-tooltip>`;
    }
    // IMPORTANT
    return null;
  }

  /**
   * Handles clicks on the card element
   * @param event
   */
  @boundMethod
  handleRowClick(event) {
    preventDefaultEvent(event);
    const { detail } = event;
    const currentPackage = this.packages.find((package_) => package_[0] === detail.col5);
    this.bubble(INVOKE_PACKAGE_ACTION, [currentPackage[0], currentPackage[1], detail.col1]);
  }

  /**
   * Get package name
   * @returns {string} - Model name
   */
  get packageName() {
    return this.lastPackage.displayName || this.lastPackage.title;
  }

  /**
   * Render the <e-package-table> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <e-custom-table
        id="package-table"
        .data="${this.rows}"
        .columns=${PACKAGES_TABLE_COLUMNS}
        sortable
        striped
        .cellFn=${this.customCell}
      ></e-custom-table>
    `;
  }
}

/**
 * Register the component as e-package-table.
 * Registration can be done at a later time and with a different name
 */
PackageTable.register();
