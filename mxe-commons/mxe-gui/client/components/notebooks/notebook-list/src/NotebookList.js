/**
 * Component NotebookList is defined as
 * `<e-notebook-list>`
 *
 * Imperatively create component
 * @example
 * let component = new NotebookList();
 *
 * Declaratively create component
 * @example
 * <e-notebook-list></e-notebook-list>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, repeat } from '@eui/lit-component';
import 'components/notebooks/notebook-card/src/NotebookCard';
import { boundMethod } from 'autobind-decorator';
import style from './notebookList.css';
/**
 * @property {Array} notebooks - Notebook list array
 */
@definition('e-notebook-list', {
  style,
  home: 'notebook-list',
  props: {
    notebooks: { attribute: false },
  },
})
export default class NotebookList extends LitComponent {
  /**
   * Returns a card component for each notebook
   * @return {*}
   */
  @boundMethod
  notebookCardsMarkup() {
    return repeat(
      this.notebooks,
      (notebook) => notebook.name,
      (notebook, i) => html`
        <e-notebook-card
          style="z-index: ${99 - i}"
          class="notebook-card"
          .notebook="${notebook}"
        ></e-notebook-card>
      `
    );
  }

  render() {
    return html`
      <div class="notebook-list">
        ${this.notebookCardsMarkup()}
      </div>
    `;
  }
}
/**
 * Register the component as e-notebook-list.
 * Registration can be done at a later time and with a different name
 */
NotebookList.register();
