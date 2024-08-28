/**
 * Component CustomTable is defined as
 * `<e-custom-table>`
 *
 * Imperatively create component
 * @example
 * let component = new CustomTable();
 *
 * Declaratively create component
 * @example
 * <e-custom-table></e-custom-table>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { Table } from '@eui/table';
import { LitComponent, html } from '@eui/lit-component';
import { DEFAULT_JOB_TABLE_COLUMNS } from 'utils/Defaults';
import style from './customTable.css';

/**
 * @property {boolean} propOne - show active/inactive state.
 * @property {boolean} propTwo - show active/inactive state.
 */
@definition('e-custom-table', {
  style,
  home: 'custom-table',
  props: {
    columns: { attribute: false, type: Array, default: DEFAULT_JOB_TABLE_COLUMNS },
    cellFn: { attribute: false },
    rowcount: { attribute: false, type: Number },
  },
})
export default class CustomTable extends Table {
  /**
   * Override cell from base class. Called each time a cell should be rendered.
   * This .
   *
   * @function cell
   * @param {Object} row - Row data.
   * @param {Object} column - Column definition.
   * @param {Number} rowIndex - The row index of the cell.
   * @param {Object} colIndex - The column index of the cell.
   * @returns contents of cell.
   */
  cell(row, column, rowIndex, colIndex) {
    const cell = this.cellFn(row, column, rowIndex, colIndex);
    return !cell ? super.cell(row, column) : cell;
  }
}

/**
 * Register the component as e-custom-table.
 * Registration can be done at a later time and with a different name
 */
CustomTable.register();
