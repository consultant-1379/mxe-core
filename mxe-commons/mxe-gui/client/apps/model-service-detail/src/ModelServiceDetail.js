/**
 * ModelServiceDetail is defined as
 * `<e-model-service-detail>`
 *
 * Imperatively create application
 * @example
 * let app = new ModelServiceDetail();
 *
 * Declaratively create application
 * @example
 * <e-model-service-detail></e-model-service-detail>
 *
 * @extends {App}
 */
import { definition } from '@eui/component';
import { App, html } from '@eui/app';
import 'components/model-services/service-detail/src/ServiceDetail';
import style from './modelServiceDetail.css';

@definition('e-model-service-detail', {
  style,
  props: {
    serviceName: { attribute: false },
  },
})
export default class ModelServiceDetail extends App {
  /**
   * Render the <e-model-service-detail> app. This function is called each time a
   * prop changes.
   */
  render() {
    return html`<e-service-detail .serviceName="${decodeURIComponent(this.serviceName)}">
    </e-service-detail>`;
  }
}

/**
 * Register the component as e-model-service-detail.
 * Registration can be done at a later time and with a different name
 * Uncomment the below line to register the App if used outside the container
 */
// ModelServiceDetail.register();
