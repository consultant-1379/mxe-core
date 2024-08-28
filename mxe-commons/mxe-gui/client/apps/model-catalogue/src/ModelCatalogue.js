/**
 * ModelList is defined as
 * `<e-model-catalogue>`
 *
 * Imperatively create application
 * @example
 * let app = new ModelList();
 *
 * Declaratively create application
 * @example
 * <e-model-catalogue></e-model-catalogue>
 *
 * @extends {App}
 */
import { App, html } from '@eui/app';
import { definition } from '@eui/component';
import 'components/model-list/model-container/src/ModelContainer';
import 'components/shared/model-details/src/ModelDetails';
import 'components/shared/upload-component/src/UploadComponent';
import style from './modelCatalogue.css';

@definition('e-model-catalogue', {
  style,
})
export default class ModelCatalogue extends App {
  render() {
    return html` <e-model-container></e-model-container> `;
  }
}
