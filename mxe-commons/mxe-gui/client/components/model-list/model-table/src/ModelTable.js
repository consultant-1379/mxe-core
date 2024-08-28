/**
 * Component ModelTable is defined as
 * `<e-model-table>`
 *
 * Imperatively create component
 * @example
 * let component = new ModelTable();
 *
 * Declaratively create component
 * @example
 * <e-model-table></e-model-table>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent } from '@eui/lit-component';
import { boundMethod } from 'autobind-decorator';
import 'components/shared/custom-table/src/CustomTable';
import { MODELS_TABLE_COLUMNS } from 'utils/Defaults';
import { formatDateToLocalDate, loc, preventDefaultEvent } from 'utils/Utils';
import { SELECT_MODEL, STATUS_AVAILABLE, STATUS_ERROR, STATUS_PACKAGING } from 'utils/Enums';
import SortingService from 'services/SortingService';
import style from './modelTable.css';

/**
 * @property {Array} models - Model list
 * @property {HTMLElement} table - Table instance
 */
@definition('e-model-table', {
  style,
  home: 'model-table',
  props: {
    models: { attribute: false, type: Array, default: [] },
    table: { attribute: false, default: null },
  },
})
export default class ModelTable extends LitComponent {
  didRender() {
    this.table = this.shadowRoot.getElementById('model-table');
    this.table.addEventListener('eui-table:sort', this.sortTable, false);
    this.table.addEventListener('row-click', this.handleRowClick, false);
  }

  didDisconnect() {
    this.table.removeEventListener('eui-table:sort', this.sortTable, false);
    this.table.removeEventListener('row-click', this.handleRowClick, false);
    this.table = null;
  }

  /**
   * Get table rows
   * @return {{col4: *, col5: *, col2: *, col3, col1: *}[]}
   */
  get rows() {
    return this.models.map((model) => this.createRow(model));
  }

  /**
   * Create table row markup
   * @param model
   * @return {{col4: *, col5: *, col2: *, col3, col1: *}}
   */
  createRow(model) {
    const latestModel = model[1][model[1].length - 1];
    return {
      col1: latestModel.title,
      col2: latestModel.id,
      col3: model[1].length,
      col4: this.getModelStatusMarkup(latestModel),
      col5: formatDateToLocalDate(latestModel.created),
      col6: latestModel.createdByUserName,
    };
  }

  /**
   * Get status markup
   * @param model
   * @return {*}
   */
  @boundMethod
  getModelStatusMarkup(model) {
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
   * Sort table
   * @param event
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
   * Custom table cell function
   * @param row
   * @param column
   * @return {null|*}
   */
  customCell(row, column) {
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
    const currentModel = this.models.find((model) => model[0] === detail.col2);
    this.bubble(SELECT_MODEL, currentModel);
  }

  /**
   * Render the <e-package-table> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <e-custom-table
        id="model-table"
        .data="${this.rows}"
        .columns=${MODELS_TABLE_COLUMNS}
        sortable
        striped
        .cellFn=${this.customCell}
      ></e-custom-table>
    `;
  }
}

/**
 * Register the component as e-model-table.
 * Registration can be done at a later time and with a different name
 */
ModelTable.register();
