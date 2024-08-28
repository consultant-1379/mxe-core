/**
 * ModelInfo is defined as
 * `<e-model-info>`
 *
 * Imperatively create application
 * @example
 * let app = new ModelInfo();
 *
 * Declaratively create application
 * @example
 * <e-model-info></e-model-info>
 *
 * @extends {App}
 */
import { App, html } from '@eui/app';
import { definition } from '@eui/component';
import 'components/shared/model-details/src/ModelDetails';
import style from './modelInfo.css';

@definition('e-model-info', {
  style,
  props: {
    modelId: { attribute: false, type: String, default: '' },
    modelVersion: { attribute: false, type: String, default: '' },
  },
})
export default class ModelInfo extends App {
  /**
   * Render the <e-model-info> app. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <e-model-details
        .modelId="${decodeURIComponent(this.modelId)}"
        .modelVersion="${decodeURIComponent(this.modelVersion)}"
      ></e-model-details>
    `;
  }
}
