/**
 * Component ServiceScaling is defined as
 * `<e-service-scaling>`
 *
 * Imperatively create component
 * @example
 * let component = new ServiceScaling();
 *
 * Declaratively create component
 * @example
 * <e-service-scaling></e-service-scaling>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, repeat } from '@eui/lit-component';
import { boundMethod } from 'autobind-decorator';
import { SET_SCALING, CLOSE_DIALOG, RESET_SCALING_DATA } from 'utils/Enums';
import { loc, evaluateNumberInput, preventDefaultEvent, getEventPath } from 'utils/Utils';
import { MAX_NUMBER_OF_INSTANCES, TARGET_METRIC_PLACEHOLDER } from 'utils/Defaults';
import style from './serviceScaling.css';
/**
 * @property {Boolean} propOne - show active/inactive state.
 * @property {string} propTwo - shows the "Hello World" string.
 */
@definition('e-service-scaling', {
  style,
  home: 'service-scaling',
  props: {
    isManual: { attribute: false, type: Boolean, default: true },
    replicas: { attribute: false, type: Number, default: 1 },
    autoScaling: { attribute: false, type: Object, default: { metrics: [{}] } },
    loadData: { attribute: false },
    service: { attribute: false },
    selectedMetric: { attribute: false, type: String, default: TARGET_METRIC_PLACEHOLDER },
  },
})
export default class ServiceScaling extends LitComponent {
  didConnect() {
    window.addEventListener(CLOSE_DIALOG, this.loadDataToMarkup, false);
    window.addEventListener(RESET_SCALING_DATA, this.reset, false);
  }

  didRender() {
    this.dropdown = this.shadowRoot.querySelector('.selected-metric');
    this.replicasInput = this.shadowRoot.getElementById('edit-manual-replicas');
    this.targetAverageValueInput = this.shadowRoot.getElementById('edit-target-average-value');
    this.minReplicasInput = this.shadowRoot.getElementById('edit-auto-min-replicas');
    this.maxReplicasInput = this.shadowRoot.getElementById('edit-auto-max-replicas');
  }

  didChangeProps(props) {
    if (props.has('service')) {
      this.loadDataToMarkup();
    }
  }

  didDisconnect() {
    window.removeEventListener(CLOSE_DIALOG, this.loadDataToMarkup, false);
    window.removeEventListener(RESET_SCALING_DATA, this.reset, false);
    this.reset();
  }

  /**
   * Loads scaling data to markup
   */
  @boundMethod
  loadDataToMarkup() {
    if (!this.loadData) {
      return;
    }
    if (!this.loadData.metrics && !this.loadData.replicas) {
      return;
    }

    this.isManual = !this.service.autoScaling;

    if (this.loadData.replicas) {
      this.reset();
      const { replicas } = this.loadData;
      this.replicasInput.value = replicas;
      this.replicas = replicas;
    } else {
      this.autoScaling = { ...this.loadData };
      this.autoScaling.metrics = [{ ...this.loadData.metrics[0] }];

      const { minReplicas, maxReplicas, metrics } = this.autoScaling;

      this.replicasInput.value = 1;
      if (metrics) {
        const { targetAverageValue, name } = metrics[0];
        this.targetAverageValueInput.value = targetAverageValue;
        if (name === 'cpuMilliCores') {
          this.selectedMetric = `${loc('CPU_USAGE')} (${loc('MILLICORES')})`;
        } else if (name === 'memoryMegaBytes') {
          this.selectedMetric = `${loc('MEMORY_USAGE')} (${loc('MEGABYTES')})`;
        }
      }
      this.minReplicasInput.value = parseInt(minReplicas, 10);
      this.maxReplicasInput.value = parseInt(maxReplicas, 10);
      this.isManual = false;
    }
  }

  /**
   * Resets scaling markup
   */
  @boundMethod
  reset() {
    this.isManual = true;
    this.replicas = 1;
    this.autoScaling = { metrics: [{}] };
    this.replicasInput.value = 1;
    this.targetAverageValueInput.value = '';
    this.minReplicasInput.value = '';
    this.maxReplicasInput.value = '';
    this.selectedMetric = TARGET_METRIC_PLACEHOLDER;
  }

  /**
   * Handles change between auto and manual scaling
   * @param {Object} event
   */
  @boundMethod
  handleChange(event) {
    this.isManual = event.detail.name === 'manual';

    const { replicas, autoScaling } = this;
    const bubbleValue = this.isManual ? { replicas } : { autoScaling };
    this.bubble(SET_SCALING, bubbleValue);
  }

  /**
   * Sets the selected metric
   * @param {Object} event
   * @param {string} name
   * @param {string} value
   */
  @boundMethod
  selectMetric(event, name, value) {
    this.dropdown.visible = false;
    preventDefaultEvent(event);
    const { autoScaling } = this;
    this.selectedMetric = name;
    autoScaling.metrics[0].name = value;
    const bubbleValue = { autoScaling };
    this.bubble(SET_SCALING, bubbleValue);
  }

  /**
   * Sets the scaling instances
   * @param {Object} event
   * @param {string} label
   */
  setInstances(event, label) {
    preventDefaultEvent(event);
    const input = getEventPath(event);
    if (!this.isManual) {
      if (label === 'targetAverageValue') {
        this.autoScaling.metrics[0][label] = input.value;
      } else {
        this.autoScaling[label] = parseInt(input.value, 10);
      }
    } else {
      this.replicas = input.value;
    }
    const { replicas, autoScaling } = this;
    const bubbleValue = this.isManual ? { replicas } : { autoScaling };
    this.bubble(SET_SCALING, bubbleValue);
  }

  /**
   * Radio buttons markup
   * @return {*}
   */
  get radioButtonsMarkup() {
    return html`<div class="radio-buttons">
      <eui-base-v0-radio-button
        name="manual"
        group="scaling"
        id="select-manual-model-service-scaling"
        .checked="${this.isManual}"
        @change="${this.handleChange}"
        class="manual spacing-bottom ${this.isManual ? '' : 'faded'}"
      >
        ${loc('MANUAL_SCALING')}
      </eui-base-v0-radio-button>
      <eui-base-v0-radio-button
        name="auto"
        group="scaling"
        id="select-auto-model-service-scaling"
        .checked="${!this.isManual}"
        @change="${this.handleChange}"
        class="auto ${this.isManual ? 'faded' : ''}"
      >
        ${loc('AUTO_SCALING')}
      </eui-base-v0-radio-button>
    </div>`;
  }

  /**
   * Get manual instance selector markup
   * @return {*}
   */
  get manualInstanceMarkup() {
    return html`<div class="flex center spacing-bottom ${this.isManual ? '' : 'faded disabled'}">
      <span class="manual-instances">${loc('INSTANCES')}</span>
      <input
        id="edit-manual-replicas"
        class="instance-input ${this.instanceInputWarning ? 'warning' : ''}"
        type="number"
        min="1"
        value="1"
        max="${MAX_NUMBER_OF_INSTANCES}"
        step="1"
        @change=${(e) => {
          evaluateNumberInput(e);
          this.setInstances(e);
        }}
      />
    </div>`;
  }

  /**
   * Get target metric type selector markup
   * @return {*}
   */
  get targetMetricMarkup() {
    return html`<div class="flex center spacing-bottom ${this.isManual ? 'faded disabled' : ''}">
      <span class="target-metric">${loc('TARGET_METRIC')}</span>
      <eui-base-v0-dropdown
        class="selected-metric"
        id="select-metric-type"
        label="${this.selectedMetric}"
        data-type="single"
        width="190px"
      >
        <eui-base-v0-menu-item
          .label="${`${loc('CPU_USAGE')} (${loc('MILLICORES')})`}"
          tabindex="0"
          @click="${(e) =>
            this.selectMetric(e, `${loc('CPU_USAGE')} (${loc('MILLICORES')})`, 'cpuMilliCores')}"
        >
        </eui-base-v0-menu-item>
        <eui-base-v0-menu-item
          tabindex="1"
          .label="${`${loc('MEMORY_USAGE')} (${loc('MEGABYTES')})`}"
          @click="${(e) =>
            this.selectMetric(
              e,
              `${loc('MEMORY_USAGE')} (${loc('MEGABYTES')})`,
              'memoryMegaBytes'
            )}"
        >
        </eui-base-v0-menu-item>
      </eui-base-v0-dropdown>
      <input
        id="edit-target-average-value"
        class="instance-input ${this.instanceInputWarning ? 'warning' : ''}"
        type="number"
        min="1"
        step="1"
        @change=${(e) => {
          evaluateNumberInput(e);
          this.setInstances(e, 'targetAverageValue');
        }}
      />
    </div>`;
  }

  /**
   * Get automatic scaling instances markup
   * @return {*}
   */
  get instancesMinMaxMarkup() {
    return html`<div class="flex center ${this.isManual ? 'faded disabled' : ''}">
      <span class="auto-instances">${loc('INSTANCES')}</span>
      <span class="min-max">${loc('MIN')}</span>
      <input
        id="edit-auto-min-replicas"
        class="instance-input ${this.instanceInputWarning ? 'warning' : ''}"
        type="number"
        min="0"
        max="${MAX_NUMBER_OF_INSTANCES}"
        step="1"
        @change=${(e) => {
          evaluateNumberInput(e);
          this.setInstances(e, 'minReplicas');
        }}
      />
      <span class="min-max">${loc('MAX')}</span>
      <input
        id="edit-auto-max-replicas"
        class="instance-input ${this.instanceInputWarning ? 'warning' : ''}"
        type="number"
        min="0"
        max="${MAX_NUMBER_OF_INSTANCES}"
        step="1"
        @change=${(e) => {
          evaluateNumberInput(e);
          this.setInstances(e, 'maxReplicas');
        }}
      />
    </div>`;
  }

  get optionsMarkup() {
    return html`<div class="options">
      ${this.manualInstanceMarkup} ${this.targetMetricMarkup}${this.instancesMinMaxMarkup}
    </div>`;
  }

  /**
   * Render the <e-service-scaling> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <div class="flex">
        ${this.radioButtonsMarkup} ${this.optionsMarkup}
      </div>
    `;
  }
}
/**
 * Register the component as e-service-scaling.
 * Registration can be done at a later time and with a different name
 */
ServiceScaling.register();
