/**
 * Component ModelTabs is defined as
 * `<e-model-tabs>`
 *
 * Imperatively create component
 * @example
 * let component = new ModelTabs();
 *
 * Declaratively create component
 * @example
 * <e-model-tabs></e-model-tabs>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, nothing } from '@eui/lit-component';
import { loc } from 'utils/Utils';
import ModelServiceService from 'services/ModelServiceService';
import style from './modelTabs.css';
import 'components/shared/upload-component-with-input/src/UploadComponentWithInput';

// import {tabs} from '@eds/vanilla/tab'

/**
 * @property {Boolean} propOne - show active/inactive state.
 * @property {string} propTwo - shows the "Hello World" string.
 */
@definition('e-model-tabs', {
  style,
  home: 'model-tabs',
  props: {
    authors: { attribute: false },
    showAddAuthorDialog: { attribute: false },
    authorName: { attribute: false, type: String, default: '' },
    publicKey: { attribute: false, type: String, default: '' },
  },
})
export default class ModelTabs extends LitComponent {
  /**
   * Returns second model selector markup
   * @return {*}
   */
  didConnect() {}

  render() {
    return html`
      <div class="top">
        <div class="button-group"></div>
      </div>
      <eui-layout-v0-tabs>
        <eui-layout-v0-tab selected id="parameters-tab">
          <label>${loc('Parameters')} </label>
        </eui-layout-v0-tab>
        <eui-layout-v0-tab id="upload-manifest-tab">
          <label>${loc('Upload Manifest')} </label>
        </eui-layout-v0-tab>
        <div slot="content"><e-create-model-service></e-create-model-service></div>
        <div slot="content">
          <e-upload-component-with-input
            .serviceReference="${ModelServiceService.postModelServiceWithinput}"
          ></e-upload-component-with-input>
        </div>
      </eui-layout-v0-tabs>
    `;
  }
}

/**
 * Register the component as e-model-tabs.
 * Registration can be done at a later time and with a different name
 */
ModelTabs.register();
