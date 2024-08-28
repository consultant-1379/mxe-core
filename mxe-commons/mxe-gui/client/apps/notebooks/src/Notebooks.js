/**
 * Notebooks is defined as
 * `<e-notebooks>`
 *
 * Imperatively create application
 * @example
 * let app = new Notebooks();
 *
 * Declaratively create application
 * @example
 * <e-notebooks></e-notebooks>
 *
 * @extends {App}
 */
import { definition } from '@eui/component';
import { App, html } from '@eui/app';
import 'components/notebooks/notebook-container/src/NotebookContainer';
import style from './notebooks.css';

@definition('e-notebooks', {
  style,
  props: {
    response: { attribute: false },
  },
})
export default class Notebooks extends App {
  /**
   * Render the <e-notebooks> app. This function is called each time a
   * prop changes.
   */
  render() {
    return html` <e-notebook-container></e-notebook-container> `;
  }
}

/**
 * Register the component as e-notebooks.
 * Registration can be done at a later time and with a different name
 * Uncomment the below line to register the App if used outside the container
 */
// Notebooks.register();
