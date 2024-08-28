/**
 * Component InvokeModel is defined as
 * `<e-invoke-model>`
 *
 * Imperatively create component
 * @example
 * let component = new InvokeModel();
 *
 * Declaratively create component
 * @example
 * <e-invoke-model></e-invoke-model>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent, nothing } from '@eui/lit-component';
import { loc } from 'utils/Utils';
import ModelServiceService from 'services/ModelServiceService';
import { boundMethod } from 'autobind-decorator';
import { DEFAULT_MODEL_INVOKE } from 'utils/Defaults';
import JSONEditor from 'jsoneditor';
import jsonEditorStyle from 'jsoneditor/dist/jsoneditor.css';
import 'jsoneditor/dist/img/jsoneditor-icons.svg';
import style from './invokeModel.css';

const mergedStyle = style + jsonEditorStyle;

/**
 * @property {Object} model - Model object
 */
@definition('e-invoke-model', {
  style: mergedStyle,
  home: 'invoke-model',
  props: {
    service: { attribute: false, type: Object, default: null },
    result: { attribute: false, default: null },
    error: { attribute: false, default: null },
    term: { attribute: false, type: Object, default: {} },
    rawInputType: { attribute: false, type: Boolean, default: false },
  },
})
export default class InvokeModel extends LitComponent {
  jsonEditor = null;

  jsonViewer = null;

  textEditor = null;

  textViewer = null;

  didRender() {
    if (this.rawInputType) {
      this.removeEditorJson();
      this.removeViewerJson();

      this.createTextEditor();
      this.showTextViewer();
    } else {
      this.removeTextEditor();
      this.removeTextViewer();

      this.createJsonEditor();
      this.showJsonViewer();
    }
  }

  removeTextEditor() {
    if (this.textEditor) {
      this.textEditor.remove();
      this.textEditor = null;
    }
  }

  removeTextViewer() {
    if (this.textViewer) {
      this.textViewer.remove();
      this.textViewer = null;
    }
  }

  removeEditorJson() {
    if (this.jsonEditor) {
      this.jsonEditor.destroy();
      this.jsonEditor = null;
    }
  }

  removeViewerJson() {
    if (this.jsonViewer) {
      this.jsonViewer.destroy();
      this.jsonViewer = null;
    }
  }

  createJsonEditor() {
    if (!this.jsonEditor) {
      // create the editor
      const container = this.shadowRoot.getElementById('invoke-model-input');
      const options = {
        mode: 'text',
        indentation: 2,
      };
      // eslint-disable-next-line no-undef
      this.jsonEditor = new JSONEditor(container, options);
      // set json
      this.jsonEditor.set(DEFAULT_MODEL_INVOKE);
    }
  }

  showJsonViewer() {
    if (this.result) {
      if (!this.jsonViewer) {
        // create the editor
        const container = this.shadowRoot.getElementById('invoke-model-result');
        const options = {
          mode: 'view',
          indentation: 2,
        };
        // eslint-disable-next-line no-undef
        this.jsonViewer = new JSONEditor(container, options);
      }
      // set json
      this.jsonViewer.set(this.result);
    }
  }

  createTextEditor() {
    if (!this.textEditor) {
      const container = this.shadowRoot.getElementById('invoke-model-input');
      this.textEditor = document.createElement('textarea');
      this.textEditor.cols = 205;
      this.textEditor.rows = 13;
      this.textEditor.defaultValue = '{ data: { ndarray: [] } }';
      container.appendChild(this.textEditor);
    }
  }

  showTextViewer() {
    if (this.result) {
      if (!this.textViewer) {
        const container = this.shadowRoot.getElementById('invoke-model-result');
        this.textViewer = document.createElement('textarea');
        this.textViewer.cols = 205;
        this.textViewer.rows = 13;
        container.appendChild(this.textViewer);
      }

      this.textViewer.value = JSON.stringify(this.result);
    }
  }

  didDisconnect() {
    this.removeEditorJson();
    this.removeViewerJson();
    this.removeTextEditor();
    this.removeTextViewer();
  }

  /**
   * Invoke model functionality
   * @return {Promise<void>}
   */
  @boundMethod
  async invokeModel() {
    try {
      this.error = null;
      if (!this.rawInputType) {
        this.term = this.jsonEditor.get();
        if (!this.term) {
          return;
        }

        this.result = await ModelServiceService.invokeModel(this.modelEndpoint, this.term, false);
      } else {
        if (this.textEditor.value === '') {
          return;
        }

        this.result = await ModelServiceService.invokeModel(
          this.modelEndpoint,
          this.textEditor.value,
          true
        );
      }
    } catch (e) {
      this.error = e;
      this.result = null;
      this.removeViewerJson();
      this.removeTextViewer();
    }
  }

  /**
   * Get model endpoint
   * @return {string}
   */
  get modelEndpoint() {
    return `/model-endpoints/${this.service.name}`;
  }

  @boundMethod
  _handleInputType(e) {
    this.rawInputType = e.detail.on;
  }

  /**
   * Get result markup
   * @return {*}
   */
  resultMarkup() {
    if (this.error) {
      return html`
        <div class="title error">${loc('ERROR_OCCURRED')}</div>
        <div class="error">${this.error.toString()}</div>
      `;
    }

    if (!this.result) {
      return nothing;
    }

    return html`
      <div class="title">${loc('RESULT')}</div>
      <div id="invoke-model-result"></div>
    `;
  }

  /**
   * Render the <e-invoke-model> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <div class="invoke-model">
        <eui-base-v0-switch
          label-on="${loc('RAW')}"
          label-off="${loc('JSON')}"
          @change=${this._handleInputType}
        ></eui-base-v0-switch>
        <div id="invoke-model-input"></div>
        <eui-base-v0-button id="invoke" class="field" big primary @click="${this.invokeModel}">
          ${loc('INVOKE')}
        </eui-base-v0-button>
        ${this.resultMarkup()}
      </div>
    `;
  }
}

/**
 * Register the component as e-invoke-model.
 * Registration can be done at a later time and with a different name
 */
InvokeModel.register();
