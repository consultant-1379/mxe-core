/**
 * Component UploadComponent is defined as
 * `<e-upload-component>`
 *
 * Imperatively create component
 * @example
 * let component = new UploadComponent();
 *
 * Declaratively create component
 * @example
 * <e-upload-component></e-upload-component>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent } from '@eui/lit-component';
import { boundMethod } from 'autobind-decorator';
import { preventDefaultEvent, loc } from 'utils/Utils';
import { DRAG_DROP_EVENTS } from 'utils/Defaults';
import { ProgressBar } from '@eui/base';
import {
  INVOKE_UPDATE,
  UPLOAD_PROGRESS,
  UPLOAD_DIALOG_CLOSE_REQUESTED,
  UPLOAD_DIALOG_CLOSE_REQUEST_APPROVED,
  UPLOAD_DIALOG_CLOSE_REQUEST_DENIED,
  CLOSE_ONBOARD,
} from 'utils/Enums';
import style from './uploadComponent.css';

/**
 * @property {Number} progress - Upload progress
 */
@definition('e-upload-component', {
  style,
  home: 'upload-component',
  props: {
    progress: { attribute: false, type: Number },
    error: { attribute: false, type: String },
    showConfirmDialog: { attribute: false, type: Boolean, default: false },
    serviceReference: { attribute: false },
  },
})
export default class UploadComponent extends LitComponent {
  didConnect() {
    window.addEventListener(UPLOAD_DIALOG_CLOSE_REQUESTED, this.handleCloseRequest, false);
  }

  didRender() {
    this.fileInput = this.shadowRoot.getElementById('file-input');
    DRAG_DROP_EVENTS.forEach((eventType) => {
      this.fileInput.addEventListener(eventType, preventDefaultEvent, false);
    });
  }

  didDisconnect() {
    window.removeEventListener(UPLOAD_DIALOG_CLOSE_REQUESTED, this.handleCloseRequest, false);
    DRAG_DROP_EVENTS.forEach((eventType) => {
      this.fileInput.removeEventListener(eventType, preventDefaultEvent, false);
    });
  }

  /**
   * Adds class on drag enter
   * @param {Object} event
   */
  dragEnter(event) {
    preventDefaultEvent(event);
    this.parentNode.classList.add('dragover');
  }

  /**
   * Remove class on drag leave
   * @param {Object} event
   */
  dragLeave(event) {
    preventDefaultEvent(event);
    this.parentNode.classList.remove('dragover');
  }

  /**
   * Start uploading and listen to upload progress
   * @param {Blob} file - Zip file to be uploaded
   */
  @boundMethod
  uploadFile(file) {
    this.error = null;
    window.addEventListener(UPLOAD_PROGRESS, this.uploadProgress, false);
    if (this.serviceReference) {
      this.serviceReference(file)
        .then(() => {
          window.removeEventListener(UPLOAD_PROGRESS, this.uploadProgress, false);
          this.bubble(INVOKE_UPDATE);
        })
        .catch((e) => {
          console.error(e);
          this.error = e;
        });
    }
  }

  /**
   * Handles upload progress update
   * @param {Object} event
   */
  @boundMethod
  uploadProgress(event) {
    this.progress = event.detail;
  }

  /**
   * Clears data from last upload when the conainer dialog is closed
   */
  @boundMethod
  handleCloseRequest(done) {
    if (this.file || !done) {
      this.bubble(UPLOAD_DIALOG_CLOSE_REQUEST_DENIED);
      this.toggleConfirmDialog();
    } else {
      this.closeUploadDialog();
    }
  }

  /**
   * Handles onChange and onDrop event
   * Looks for selected or dropped files
   * @param {Object} event
   */
  @boundMethod
  handleFileChange(event) {
    preventDefaultEvent(event);
    if (event.dataTransfer && event.dataTransfer.files) {
      // eslint-disable-next-line prefer-destructuring
      this.file = event.dataTransfer.files[0];
    } else if (event.currentTarget && event.currentTarget.files) {
      // eslint-disable-next-line prefer-destructuring
      this.file = event.currentTarget.files[0];
    }

    if (this.file && this.file.name.includes('zip')) {
      // MatomoService.trackEvent(
      //   MATOMO_CATEGORY_MODEL_CATALOGUE,
      //   MATOMO_ACTION_UPLOAD,
      //   'Upload model'
      // );
      this.uploadFile(this.file);
    } else {
      this.error = loc('WRONG_FILE_FORMAT');
    }
  }

  /**
   * Toggles confirm dialog
   */
  @boundMethod
  toggleConfirmDialog() {
    this.showConfirmDialog = !this.showConfirmDialog;
  }

  /**
   * Resets the data of this component and emits close event
   */
  @boundMethod
  closeUploadDialog(forced) {
    this.file = null;
    this.progress = null;
    this.fileInput.value = '';
    if (forced) {
      this.bubble(CLOSE_ONBOARD);
    } else {
      this.showConfirmDialog = false;
      this.bubble(UPLOAD_DIALOG_CLOSE_REQUEST_APPROVED);
    }
  }

  /**
   * Returns upload status description
   */
  get footerDescription() {
    if (this.progress === 100) {
      return loc('UPLOAD_READY');
    }
    if (this.file && this.file.name) {
      return `${this.file.name} ${loc('UPLOAD_IN_PROGRESS')} ...`;
    }
    return '';
  }

  /**
   * Render the <e-upload-component> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <div class="drop-zone ${this.error ? 'error' : ''}" id="dropzone">
        ${
          this.file && this.progress === 100
            ? html`
                <img
                  class="success-icon"
                  src="/assets/icons/success-icon.svg"
                  alt="upload success"
                />
                <span>"${this.file.name}" ${loc('UPLOAD_COMPLETE')}</span>
              `
            : html`
                <eui-v0-icon name="upload" class="icon" size="40px"></eui-v0-icon>
                <span class="description">${loc('DRAG_AND_DROP')}</span>
                <eui-base-v0-button primary .disabled=${this.file}
                  >${loc('BROWSE')}</eui-base-v0-button
                >
              `
        }
        <input
          id="file-input"
          type="file"
          accept="application/zip"
          @drop="${this.handleFileChange}"
          @change="${this.handleFileChange}"
          @dragenter="${this.dragEnter}"
          @dragleave="${this.dragLeave}"
        />
      </div>
      <div id="footer">
        <div class="progress-container">
          <eui-base-v0-progress-bar
            class="progress-bar  ${this.file ? 'show' : ''}"
            color="blue"
            .value="${this.progress}"
          ></eui-base-v0-progress-bar>
          <div class="upload-details ${this.progress === 100 || this.error ? 'done' : ''}">
            <img
              class="progress-icon"
              src="/assets/icons/${this.error ? 'error' : 'success'}-icon.svg"
              alt="upload ${this.error ? 'error' : 'success'}"
            />
            <span class="description">${this.error ? this.error : this.footerDescription}</span>
          </div>
        </div>

        <div class="button-container">
          <eui-base-v0-button class="button" @click="${this.closeUploadDialog}">
            ${loc('CANCEL')}
          </eui-base-v0-button>
          <eui-base-v0-button
            class="onboard-btn button"
            @click="${() => this.closeUploadDialog(true)}"
            primary
            .disabled="${this.progress !== 100 || this.error}"
            >${loc('OK')}</eui-base-v0-button
          >
        </div>

          <eui-base-v0-dialog
            class="confirm-dialog"
            label=${loc('DIALOG_CONFIRM_TITLE')}
            @eui-dialog:cancel="${this.toggleConfirmDialog}"
            .show="${this.showConfirmDialog}"
          >
            <div slot="content" class="details">
              <span>${loc('DIALOG_CONFIRM_MODEL_UPLOAD_CANCEL')}</span>
              <span>${loc('DIALOG_CONFIRM_ARE_YOU_SURE')}</span>
            </div>
            <eui-base-v0-button
              slot="bottom"
              primary
              @click=${(this.toggleConfirmDialog, this.closeUploadDialog)}
              >${loc('BUTTON_CONFIRM')}</eui-base-v0-button
            >
          </eui-base-v0-dialog>
        </div>
      </div>
    `;
  }
}

/**
 * Register the component as e-upload-component.
 * Registration can be done at a later time and with a different name
 */
UploadComponent.register();
