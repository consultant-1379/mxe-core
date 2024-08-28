/**
 * Component ServiceDetail is defined as
 * `<e-service-detail>`
 *
 * Imperatively create component
 * @example
 * let component = new ServiceDetail();
 *
 * Declaratively create component
 * @example
 * <e-service-detail></e-service-detail>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent, nothing } from '@eui/lit-component';
import { boundMethod } from 'autobind-decorator';
import {
  INVOKE_DELETE,
  INVOKE_UPDATE,
  STATUS_CREATING,
  STATUS_DEPLOYMENT_ERROR,
  STATUS_RUNNING,
  SET_SCALING,
  RESET_SCALING_DATA,
} from 'utils/Enums';
import { formatDateToLocalDate, getEventPath, loc, preventDefaultEvent } from 'utils/Utils';
import { DEFAULT_INTERVAL_MS } from 'utils/Defaults';
import { IS_STANDALONE_STR } from 'utils/Config';
import ModelServiceService from 'services/ModelServiceService';
import CreateManifestService from 'services/CreateManifestService';
import '../service-models/src/ServiceModels';
import 'components/shared/model-details/invoke-model/src/InvokeModel';
import 'components/shared/model-details/monitoring-information/src/MonitoringInformation';
import 'components/shared/model-details/custom-metrics/src/CustomMetrics';
import 'components/shared/service-scaling/src/ServiceScaling';
import 'components/shared/change-model/src/ChangeModel';
import 'components/shared/error-screen/src/ErrorScreen';
import 'components/model-services/service-logs/src/ServiceLogs';
import ModelService from 'services/ModelService';
import { toModelServices } from 'utils/Navigator';
import base64 from 'react-native-base64';
import YAML from 'yaml';
import style from './serviceDetail.css';

/**
 * @property {Object}  service
 * @property {string} serviceName
 * @property {boolean} isEdit
 * @property {boolean} isLoading
 * @property {boolean} error
 * @property {Object} defaultModelInfoValues
 * @property {boolean} showConfirmDeleteDialog
 * @property {boolean} showScalingDialog
 * @property {Array} selectedModels
 * @property {boolean} changeDisabled
 * @property {boolean} scalingDisabled
 * @property {Object} currentScaling
 */
@definition('e-service-detail', {
  style,
  home: 'service-detail',
  props: {
    service: { attribute: false },
    serviceName: { attribute: false },
    manifestDetails: { attribute: false, default: '' },
    isEdit: { attribute: false, type: Boolean, default: false },
    isLoading: { attribute: false, type: Boolean, default: false },
    error: { attribute: false, type: Boolean, default: false },
    defaultModelInfoValues: { attribute: false, type: Object, default: null },
    showConfirmDeleteDialog: { attribute: false, type: Boolean, default: false },
    showEditManifestDialog: { attribute: false, type: Boolean, default: false },
    showScalingDialog: { attribute: false, type: Boolean, default: false },
    selectedModels: { attribute: false, type: Array, default: [] },
    manifestDataDetails: { attribute: false, default: '' },
    changeDisabled: { attribute: false, type: Boolean, default: true },
    scalingDisabled: { attribute: false, type: Boolean, default: false },
    currentScaling: { attribute: false, type: Object, default: null },
  },
})
export default class ServiceDetail extends LitComponent {
  async didConnect() {
    this.bubble(RESET_SCALING_DATA);
    await this.fetchService();

    this.timer = setInterval(async () => {
      await this.fetchService();
    }, DEFAULT_INTERVAL_MS);
  }

  didRender() {
    window.addEventListener(INVOKE_UPDATE, this.fetchService, false);
    window.addEventListener(INVOKE_DELETE, this.toggleConfirmDeleteDialog, false);
    window.addEventListener(SET_SCALING, this.setScaling, false);
    this.instanceInput = this.shadowRoot.getElementById('instance-input');
  }

  didDisconnect() {
    window.removeEventListener(INVOKE_DELETE, this.toggleConfirmDeleteDialog, false);
    window.removeEventListener(INVOKE_UPDATE, this.fetchService, false);
    this.bubble(RESET_SCALING_DATA);
    window.removeEventListener(SET_SCALING, this.setScaling, false);
    this.service = null;
    this.showScalingDialog = false;

    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }

  /**
   * Gets models and services through service requests
   */
  @boundMethod
  async fetchService() {
    try {
      this.isLoading = true;
      this.error = false;
      const allModels = await ModelService.getModels();
      const services = await ModelServiceService.getModelServiceByName(this.serviceName);
      const models = [];

      services.models.forEach((model) => {
        const detailedModel = allModels.find(
          (item) => item.id === model.id && item.version === model.version
        );

        if (detailedModel) {
          detailedModel.availableVersions = [];
          allModels.forEach((item) => {
            if (item.id === model.id) {
              detailedModel.availableVersions.push(item);
            }
          });

          models.push({ ...detailedModel, weight: model.weight });
        }
        if (!detailedModel) {
          models.push({ ...model, actions: [] });
        }
      });

      this.service = { ...services, models };
      this.isLoading = false;
    } catch (error) {
      this.isLoading = false;
      this.error = true;
      console.log(error);
    }
  }

  /**
   * Returns title
   * @return {string}
   */
  get title() {
    return this.service?.name ?? '';
  }

  /**
   * Returns status
   * @return {string}
   */
  get serviceStatus() {
    return this.service ? this.service.status : '';
  }

  async manifestData() {
    if (this.service.autoScaling) {
      this.scalingData = [];
      this.scalingData.autoScaling = this.service.autoScaling;
    } else {
      this.scalingData = { replicas: this.service?.replicas };
    }
    const file = await CreateManifestService.constructCreateManifestDataURI(
      this.serviceName,
      this.scalingData,
      this.service.models
    );
    this.manifestDataDetails = file;
  }

  /**
   * Returns markup of status
   * @return {*}
   */
  get statusPill() {
    if (this.serviceStatus === '') {
      return nothing;
    }

    let icon = '';
    let color = '';
    let rotating = false;
    switch (this.serviceStatus) {
      case STATUS_RUNNING:
        icon = 'check';
        color = 'var(--green)';
        break;
      case STATUS_DEPLOYMENT_ERROR:
        icon = 'cross';
        color = 'var(--red)';
        break;
      case STATUS_CREATING:
        icon = 'dial';
        color = 'var(--orange)';
        rotating = true;
        break;
      default:
        break;
    }

    return html`
      <eui-base-v0-pill class="pill"
        ><eui-v0-icon
          class="${this.serviceStatus} ${rotating ? 'rotating' : ''}"
          name="${icon}"
          color="${color}"
        ></eui-v0-icon>
        <span>${loc(this.serviceStatus.toUpperCase())}</span>
      </eui-base-v0-pill>
    `;
  }

  /**
   * Returns delete button if user has permissions
   * @return {*}
   */
  get deleteButtonMarkup() {
    return html`
      <eui-base-v0-button
        @click=${this.toggleConfirmDeleteDialog}
        warning
        id="delete-${this.serviceName}"
        icon="trashcan"
      >
        ${loc('DELETE')}
      </eui-base-v0-button>
    `;
  }

  get editManifestMarkup() {
    return html`
      <eui-base-v0-button
        id="edit-manifest-model-service"
        @click=${this.toggleEditManifestDialog}
        icon="edit"
      >
        ${loc('EDIT_MANIFEST')}
      </eui-base-v0-button>
    `;
  }

  /**
   * Returns edit scaling button if user has permissions
   * @return {*}
   */
  get editScalingButtonMarkup() {
    return html`
      <eui-base-v0-button id="edit-model-service-scaling" @click="${this.toggleScalingDialog}">
        ${loc('EDIT_SCALING')}
      </eui-base-v0-button>
    `;
  }

  /**
   * Creates backup from model info in case editing is cancelled
   */
  @boundMethod
  createDataBackup() {
    this.defaultModelInfoValues = { ...this.service.models[0] };
  }

  /**
   * Copies given string to clipboard
   * @param {string} string
   */
  copyString(string) {
    const el = document.createElement('textarea');
    el.value = string;
    document.body.appendChild(el);
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);
  }

  /**
   * Saves edited instances
   */
  @boundMethod
  async saveInstanceChanges() {
    try {
      const file = await CreateManifestService.constructCreateManifest(
        this.serviceName,
        this.scalingData,
        this.service.models
      );
      await ModelServiceService.patchModelService(this.serviceName, file);
      this.bubble(INVOKE_UPDATE);
      this.toggleScalingDialog();
    } catch (error) {
      console.error(error);
    }
  }

  /**
   * Returns markup for model endpoint
   * @return {*}
   */
  get modelEndpointMarkup() {
    const { location } = window;
    const endpoint = `${location.origin}/model-endpoints/${this.serviceName}`;
    // protocol + host + port /model-endpoints/model-service-name
    return html`
      <div class="wrapper">
        <span class="label model-endpoint">${loc('MODEL_ENDPOINT')}:</span>

        <div class="flex">
          <a href="${endpoint}" class="value model-endpoint" target="_blank">${endpoint}</a>
          <eui-base-v0-tooltip position="top" message="${loc('COPY_TO_CLIPBOARD')}">
            <eui-v0-icon
              name="copy"
              class="copy-icon"
              @click="${(e) => {
                preventDefaultEvent(e);
                this.copyString(endpoint);
              }}"
            ></eui-v0-icon>
          </eui-base-v0-tooltip>
        </div>
      </div>
    `;
  }

  /**
   * Returns markup for models creator
   * @return {*}
   */
  get createdByMarkup() {
    if (this.service && this.service.createdByUserName) {
      return html`
        <div class="wrapper">
          <span class="label">${loc('CREATED_BY')}</span>
          <span class="value">${this.service.createdByUserName}</span>
        </div>
      `;
    }
    return nothing;
  }

  /**
   * Returns markup for service creation time
   * @return {*}
   */
  get createdAtMarkup() {
    if (this.service && this.service.created) {
      return html`
        <div class="wrapper">
          <span class="label">${loc('CREATED_AT')}</span>
          <span class="value">${formatDateToLocalDate(this.service.created)}</span>
        </div>
      `;
    }
    return nothing;
  }

  /**
   * Returns markup for service scaling information
   * @return {*}
   */
  get instanceMarkup() {
    if (!this.service) {
      return nothing;
    }

    return html`
      <div class="wrapper">
        <span class="label">${loc('SCALING')}</span>
        <span class="value"
          >${this.service.autoScaling ? this.autoScalingMarkup : loc('MANUAL')}</span
        >
      </div>
      <div class="wrapper">
        <span class="label">${loc('INSTANCES')}</span>
        <span class="value"
          >${this.service.autoScaling
            ? `${this.service.autoScaling.minReplicas} - ${this.service.autoScaling.maxReplicas}`
            : this.service.replicas}</span
        >
      </div>
    `;
  }

  /**
   * Returns markup in case of automatic scaling
   * @return {*}
   */
  get autoScalingMarkup() {
    const { metrics } = this.service.autoScaling;
    const { name, targetAverageValue } = metrics[0];
    const method = `${loc('AUTO_SCALING')} ${loc('BASED_ON')} ${
      name === 'cpuMilliCores' ? loc('CPU_USAGE') : loc('MEMORY_USAGE')
    }`;
    const unit = `- ${loc('TARGET_VALUE')}: ${targetAverageValue} ${
      name === 'cpuMilliCores' ? loc('MILLICORES') : loc('MEGABYTES')
    }`;

    return html` ${method}<br />${unit} `;
  }

  /**
   * Returns the number of replicas
   * @return {*}
   */
  get replicaNumbers() {
    return this.service.autoScaling
      ? `${this.service.autoScaling.minReplicas} - ${this.service.autoScaling.maxReplicas}`
      : this.service.replicas;
  }

  /**
   * Toggles confirm delete dialog
   * @param event
   */
  @boundMethod
  toggleConfirmDeleteDialog(event) {
    if (event) {
      preventDefaultEvent(event);
      this.selectedModel = event.detail;
    }
    this.showConfirmDeleteDialog = !this.showConfirmDeleteDialog;
  }

  @boundMethod
  toggleEditManifestDialog() {
    this.showEditManifestDialog = !this.showEditManifestDialog;
    this.manifestData();
    this.OnboardingDialog();
  }

  /**
   * Deletes service
   */
  @boundMethod
  async deleteService() {
    this.toggleConfirmDeleteDialog();
    try {
      await ModelServiceService.deleteModelServices(this.serviceName);
      await toModelServices();
    } catch (e) {
      console.error(e);
    }
  }

  /**
   * Toggles service scaling dialog
   */
  @boundMethod
  toggleScalingDialog() {
    this.showScalingDialog = !this.showScalingDialog;
    if (!this.showScalingDialog) {
      this.bubble(RESET_SCALING_DATA);
    }
  }

  /**
   * Returns the data to be preloaded in scaling dialog
   * @return {Object}
   */
  get loadData() {
    return this.service?.autoScaling || { replicas: this.service?.replicas };
  }

  /**
   * Returns markup for the scaling editing dialog
   * @return {*}
   */
  get editScalingDialog() {
    return html`
      <eui-base-v0-dialog
        class="confirm-dialog delete"
        label=${loc('EDIT_SCALING')}
        @eui-dialog:cancel="${this.toggleScalingDialog}"
        .show="${this.showScalingDialog}"
      >
        <div slot="content" class="details">
          <e-service-scaling
            .loadData="${{ ...this.loadData }}"
            .service="${this.service}"
          ></e-service-scaling>
        </div>
        <eui-base-v0-button
          slot="bottom"
          primary
          id="edit-confirm-model-service-scaling"
          @click=${this.saveInstanceChanges}
          ?disabled="${this.scalingDisabled}"
        >
          ${loc('EDIT_SCALING')}
        </eui-base-v0-button>
      </eui-base-v0-dialog>
    `;
  }

  OnboardingDialog() {
    if (this.showEditManifestDialog) {
      const uploadDialog = this.shadowRoot.querySelector('.edit-manifest-dialog');
      const dialog = uploadDialog.shadowRoot.querySelector('.dialog');
      dialog.setAttribute('style', 'min-width: 42%; min-height: 60%;');
      const manifestinput = this.shadowRoot.querySelector('.manifestinput');
      const inputgroup = manifestinput.children[0];
      const divData = inputgroup.shadowRoot.querySelector('.input__prefix__suffix');
      const divChildren = divData.children[0];
      divChildren.setAttribute('style', 'height:300px');
    }
  }

  /**
   * Sets the status of the confirm button of scaling modal
   */
  @boundMethod
  isScalingDisabled() {
    let metricHasAllValues = true;

    if (this.scalingData && this.scalingData.autoScaling) {
      metricHasAllValues =
        this.scalingData.autoScaling.minReplicas &&
        this.scalingData.autoScaling.maxReplicas &&
        this.scalingData.autoScaling.metrics[0].name &&
        this.scalingData.autoScaling.metrics[0].targetAverageValue;
    }

    this.scalingDisabled = !metricHasAllValues;
  }

  /**
   * Sets the scaling data given the event
   * @param {event} event
   */
  @boundMethod
  setScaling(event) {
    if (!event.detail) {
      return;
    }
    this.scalingData = event.detail;
    this.isScalingDisabled();
  }

  /**
   * Returns markup for the delete confirmation dialog
   * @return {*}
   */
  get deleteConfirmationDialog() {
    return html`
      <eui-base-v0-dialog
        class="confirm-dialog delete"
        label=${loc('DIALOG_CONFIRM_TITLE')}
        @eui-dialog:cancel="${this.toggleConfirmDeleteDialog}"
        .show="${this.showConfirmDeleteDialog}"
      >
        <div slot="content" class="details">
          <span>${loc('DIALOG_CONFIRM_SERVICE_DELETE')}</span>
        </div>
        <eui-base-v0-button
          slot="bottom"
          warning
          id="delete-confirm-${this.serviceName}"
          @click=${this.deleteService}
          >${loc('BUTTON_DELETE')}</eui-base-v0-button
        >
      </eui-base-v0-dialog>
    `;
  }

  get editManifestDialog() {
    if (!this.showEditManifestDialog) {
      return nothing;
    }
    return html``;
  }

  @boundMethod
  async saveManifest() {
    try {
      const dataURI = `data:yml/plain;base64,${base64.encode(this.manifestDetails.value)}`;
      const file = this.dataURLtoFile(dataURI, `${this.serviceName}.yml`);
      await ModelServiceService.patchModelService(this.serviceName, file);
      this.bubble(INVOKE_UPDATE);
      this.toggleEditManifestDialog();
    } catch (error) {
      console.error(error);
    }
  }

  @boundMethod
  getTheEventData(event) {
    this.manifestDetails = getEventPath(event);
  }

  dataURLtoFile = (dataurl, filename) => {
    const arr = dataurl.split(',');
    const mime = arr[0].match(/:(.*?);/)[1];
    const bstr = atob(arr[1]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);

    while (n--) {
      u8arr[n] = bstr.charCodeAt(n);
    }
    const file = new File([u8arr], filename, { type: mime });
    return file;
  };

  /**
   * Render the <e-model-service-detail> app. This function is called each time a
   * prop changes.
   */
  render() {
    if (this.error) {
      return html` <e-error-screen .title="${loc('ERROR_HAPPENED')}"></e-error-screen> `;
    }
    return html`
      <div class="top">
        <div class="title">${this.title}</div>
        ${this.statusPill}
        <div class="button-group">
        ${this.editManifestMarkup}
          ${this.deleteButtonMarkup}
        </div>
      </div>
      <div class="content">
        <div class="box info">
          <div class="top align-center">
            <div class="left">
              <span class="title">
                ${loc('BASIC_INFORMATION')}
              </span>
            </div>
            <div class="right">
              ${this.editScalingButtonMarkup}
            </div>
          </div>
          <div>
            ${this.instanceMarkup} ${this.createdByMarkup} ${this.createdAtMarkup}
            ${this.modelEndpointMarkup}
          </div>
        </div>
        <e-service-models
          id="service-models"
          class="box models"
          .service="${this.service}"
          .changeDisabled="${this.changeDisabled}"
        ></e-service-models>
        <div class="accordions">
          <div class="box charts">
            <span class="title">${loc('MONITORING_INFORMATION')}</span>
            <e-monitoring-information .service="${this.service}" />
          </div>
          <div class="box custom-metrics">
            <span class="title">${loc('CUSTOM_METRICS')}</span>
            <e-custom-metrics .service="${this.service}" />
          </div>
          <div class="box service-logs">
            <eui-base-v0-accordion .categoryTitle=${loc('VIEW_MODEL_SERVICE_LOGS')}>
              <e-service-logs .service="${this.service}"></e-service-logs>
            </eui-base-v0-accordion>
          </div>
          <div class="box invoke">
            <eui-base-v0-accordion .categoryTitle=${loc('INVOKE_MODEL')}>
              <e-invoke-model .service="${this.service}"></e-invoke-model>
            </eui-base-v0-accordion>
          </div>
        </div>
        ${this.editScalingDialog} ${this.deleteConfirmationDialog}
        <eui-base-v0-dialog
        id="dialog"
        class="edit-manifest-dialog"
        label="${loc('EDIT')} ${loc('Manifest').toLowerCase()}"
        no-cancel
        .show="${this.showEditManifestDialog}"
      >
        <div slot="content" class="manifestinput" style="width: 500px;">
          <eui-base-v0-textarea
            class="manifest-name"
            id="content-manifest-model-service"
            labelText="${loc('MODEL_SERVICE_MANIFEST')}:"
            fullwidth
            placeholder="${loc('MODEL_SERVICE_MANIFEST_TEXT')}"
            @input="${this.getTheEventData}"
            .value="${this.manifestDataDetails}"
          ></eui-base-v0-textarea>
        </div>
        <eui-base-v0-button slot="bottom" @click=${this.toggleEditManifestDialog}>
          ${loc('CANCEL')} </eui-base-v0-button
        ><eui-base-v0-button
          slot="bottom"
          primary
          id="edit-confirm-manifest-model-service"
          @click=${this.saveManifest}"
        >
          ${loc('Apply')}
        </eui-base-v0-button>
      </eui-base-v0-dialog>
      </div>
    `;
  }
}

/**
 * Register the component as e-service-detail.
 * Registration can be done at a later time and with a different name
 */
ServiceDetail.register();
