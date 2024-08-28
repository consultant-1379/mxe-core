/**
 * ModelServices is defined as
 * `<e-model-services>`
 *
 * Imperatively create application
 * @example
 * let app = new ModelServices();
 *
 * Declaratively create application
 * @example
 * <e-model-services></e-model-services>
 *
 * @extends {App}
 */
import { definition } from '@eui/component';
import { App, html } from '@eui/app';
import { MultiPanelTile } from '@eui/layout';
import 'components/model-services/services-container/src/ServicesContainer';
import style from './modelServices.css';

@definition('e-model-services', {
  style,
})
export default class ModelServices extends App {
  /**
   * Render the <e-model-services> app. This function is called each time a
   * prop changes.
   */
  render() {
    return html` <e-services-container></e-services-container> `;
  }
}

/**
 * Register the component as e-model-services.
 * Registration can be done at a later time and with a different name
 * Uncomment the below line to register the App if used outside the container
 */
// ModelServices.register();
