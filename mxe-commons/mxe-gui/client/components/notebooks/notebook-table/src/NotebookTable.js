/**
 * Component NotebookTable is defined as
 * `<e-notebook-table>`
 *
 * Imperatively create component
 * @example
 * let component = new NotebookTable();
 *
 * Declaratively create component
 * @example
 * <e-notebook-table></e-notebook-table>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent } from '@eui/lit-component';
import { formatDateToLocalDate, loc, openCenteredWindow, preventDefaultEvent } from 'utils/Utils';
import SortingService from 'services/SortingService';
import 'components/shared/custom-table/src/CustomTable';
import { boundMethod } from 'autobind-decorator';
import { INVOKE_DELETE } from 'utils/Enums';
import { NOTEBOOK_TABLE_COLUMNS } from 'utils/Defaults';
import style from './notebookTable.css';

/**
 * @property {Array} notebooks - Notebook list array
 * @property {HTMLElement} table - Table DOM element
 */
@definition('e-notebook-table', {
  style,
  home: 'notebook-table',
  props: {
    notebooks: { attribute: false, type: Array, default: [] },
    table: { attribute: false, default: null },
  },
})
export default class NotebookTable extends LitComponent {
  didRender() {
    this.table = this.shadowRoot.getElementById('notebook-table');
    this.table.addEventListener('eui-table:sort', this.sortTable, false);
    this.table.addEventListener('row-click', this.handleRowClick, false);
  }

  didDisconnect() {
    this.table.removeEventListener('eui-table:sort', this.sortTable, false);
    this.table.removeEventListener('row-click', this.handleRowClick, false);
  }

  /**
   * Handles clicks on the row element
   * @param event
   */
  @boundMethod
  handleRowClick(event) {
    preventDefaultEvent(event);
    // Object is set in window.
    // This will be used to close when logout performed.
    if (window.notebookWindow && !window.notebookWindow.closed) {
      window.notebookWindow.focus();
    } else {
      window.notebookWindow = openCenteredWindow('notebook.html', 'notebook');
    }
  }

  get rows() {
    return this.notebooks.map((notebook) => this.createRow(notebook));
  }

  /**
   * Returns formatte object for table
   * @param {Object} notebook
   * @return {Object}
   */
  createRow(notebook) {
    return {
      col1: notebook.name,
    };
  }

  /**
   * returns delete button markup
   * @param {Object} notebook
   * @return {*}
   */
  @boundMethod
  getDeleteButtonMarkup(notebook) {
    return html`
      <eui-base-v0-button
        class="action"
        warning
        fullwidth
        @click="${(event) => {
          preventDefaultEvent(event);
          this.bubble(INVOKE_DELETE, { notebook });
        }}"
      >
        ${loc('DELETE')}
      </eui-base-v0-button>
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
    if (column.attribute === 'col1' || column.attribute === 'col2' || column.attribute === 'col5') {
      return html` <eui-base-v0-tooltip message="${row[column.attribute]}"
        ><div class="status">${row[column.attribute]}</div>
      </eui-base-v0-tooltip>`;
    }
    if (column.attribute === 'col4') {
      // provide some custom CSS styling for this cell. "custom-table__cell" is
      // defined in the accompanying CSS file for this component.
      return html` <div class="actions">${row[column.attribute]}</div> `;
    }
    // IMPORTANT
    return null;
  }

  /**
   * Render the <e-notebook-table> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <e-custom-table
        id="notebook-table"
        .data="${this.rows}"
        .columns=${NOTEBOOK_TABLE_COLUMNS}
        sortable
        striped
        .cellFn=${this.customCell}
      ></e-custom-table>
    `;
  }
}
/**
 * Register the component as e-notebook-table.
 * Registration can be done at a later time and with a different name
 */
NotebookTable.register();
