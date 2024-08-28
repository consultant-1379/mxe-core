/**
 * Component AuthorList is defined as
 * `<e-author-list>`
 *
 * Imperatively create component
 * @example
 * let component = new AuthorList();
 *
 * Declaratively create component
 * @example
 * <e-author-list></e-author-list>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent, nothing } from '@eui/lit-component';
import 'components/shared/error-screen/src/ErrorScreen';
import 'components/shared/custom-table/src/CustomTable';
import { loc, preventDefaultEvent } from 'utils/Utils';
import { boundMethod } from 'autobind-decorator';
import SortingService from 'services/SortingService';
import { INVOKE_UPDATE } from 'utils/Enums';
import AuthorService from 'services/AuthorService';
import { AUTHOR_TABLE_COLUMNS, DEFAULT_INTERVAL_MS } from 'utils/Defaults';
import style from './authorList.css';

/**
 * @property {Array} authors - Author list
 * @property {HTMLElement} table - Table DOM element
 */
@definition('e-author-list', {
  style,
  home: 'author-list',
  props: {
    authors: { attribute: false, type: Array, default: [] },
    table: { attribute: false, default: null },
    error: { attribute: false, type: Boolean, default: false },
    showDialog: { attribute: false, type: Boolean, default: false },
    timer: { attribute: false, type: Number },
  },
})
export default class AuthorList extends LitComponent {
  async didConnect() {
    await this.getAuthors();
    window.addEventListener(INVOKE_UPDATE, this.getAuthors, false);
    this.timer = setInterval(this.getAuthors, DEFAULT_INTERVAL_MS, false);
  }

  didRender() {
    if (!this.table && this.authors && this.authors.length > 0) {
      this.table = this.shadowRoot.getElementById('authors-table');
      this.table.addEventListener('eui-table:sort', this.sortTable, false);
    }
  }

  didDisconnect() {
    if (this.table) {
      this.table.removeEventListener('eui-table:sort', this.sortTable, false);

      this.table = null;
    }
    window.removeEventListener(INVOKE_UPDATE, this.getAuthors, false);

    clearInterval(this.timer);
    this.timer = null;
  }

  @boundMethod
  async getAuthors() {
    try {
      this.authors = await AuthorService.getAuthors();
    } catch (err) {
      this.error = true;
      console.log(err);
    }
  }

  /**
   * Author table rows
   * @return {Array}
   */
  get rows() {
    return this.authors.map((author) => this.createRow(author));
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

  /**
   * Returns formatted object for table
   * @param {Object} author
   * @return {Object}
   */
  createRow(author) {
    const { name, publicKey } = author;

    return {
      col1: name,
      col2: publicKey,
      col3: this.getActionButtonMarkup(author),
    };
  }

  /**
   * Returns action button markup
   * @param {Object} author
   * @return {*}
   */
  @boundMethod
  getActionButtonMarkup(author) {
    return html`
      <eui-base-v0-button
        primary
        @click="${(event) => {
          preventDefaultEvent(event);
          this.authorToDelete = author;
          this.showDialog = true;
        }}"
      >
        ${loc('DELETE')}
      </eui-base-v0-button>
    `;
  }

  /**
   * Deletes selected author
   */
  @boundMethod
  async deleteAuthor() {
    try {
      const { name } = this.authorToDelete;
      await AuthorService.deleteAuthor(name);
      this.getAuthors();
      this.authorToDelete = null;
      this.showDialog = false;
    } catch (e) {
      console.error(e);
    }
  }

  customCell(row, column, rowIndex, colIndex) {
    if (column.attribute === 'col1' || column.attribute === 'col2') {
      return html` <eui-base-v0-tooltip message="${row[column.attribute]}"
        ><div class="status">${row[column.attribute]}</div>
      </eui-base-v0-tooltip>`;
    }
    if (column.attribute === 'col3') {
      return html`<div class="actions">${row[column.attribute]}</div>`;
    }

    // IMPORTANT
    return null;
  }

  get deleteDialog() {
    if (!this.showDialog) {
      return nothing;
    }

    // eslint-disable-next-line no-return-assign
    const hideDialog = () => (this.showDialog = false);

    return html`<eui-base-v0-dialog
      class="delete-dialog"
      label=${loc('DIALOG_CONFIRM_TITLE')}
      no-cancel
      show
    >
      <div slot="content" class="details">
        <span>${loc('DIALOG_CONFIRM_AUTHOR')}</span>
      </div>
      <eui-base-v0-button slot="bottom" primary @click=${hideDialog}>
        ${loc('CANCEL')}
      </eui-base-v0-button>
      <eui-base-v0-button slot="bottom" warning @click=${this.deleteAuthor}>
        ${loc('BUTTON_DELETE')}
      </eui-base-v0-button>
    </eui-base-v0-dialog>`;
  }

  /**
   * Render the <e-author-list> component. This function is called each time a
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

    return html`
      <e-custom-table
        id="authors-table"
        .data="${this.rows}"
        .columns=${AUTHOR_TABLE_COLUMNS}
        sortable
        striped
        .cellFn=${this.customCell}
      ></e-custom-table>
      ${this.deleteDialog}
    `;
  }
}

/**
 * Register the component as e-author-list.
 * Registration can be done at a later time and with a different name
 */
AuthorList.register();
