/**
 * Component CreateNotebook is defined as
 * `<e-create-notebook>`
 *
 * Imperatively create component
 * @example
 * let component = new CreateNotebook();
 *
 * Declaratively create component
 * @example
 * <e-create-notebook></e-create-notebook>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html } from '@eui/lit-component';
import style from './createNotebook.css';

/**
 * @property {string} notebookName - Notebook name
 * @property {boolean} wrongInput - Input validator
 */
@definition('e-create-notebook', {
  style,
  home: 'create-notebook',
  props: {
    notebookName: { attribute: false, type: String, default: '' },
    wrongInput: { attribute: false, type: Boolean, default: false },
  },
})
export default class CreateNotebook extends LitComponent {
  /**
   * Render the <e-create-notebook> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html``;
  }
}

/**
 * Register the component as e-create-notebook.
 * Registration can be done at a later time and with a different name
 */
CreateNotebook.register();
