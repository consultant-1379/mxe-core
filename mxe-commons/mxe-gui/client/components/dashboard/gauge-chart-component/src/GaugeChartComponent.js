/**
 * Component GaugeChartComponent is defined as
 * `<e-gauge-chart-component>`
 *
 * Imperatively create component
 * @example
 * let component = new GaugeChartComponent();
 *
 * Declaratively create component
 * @example
 * <e-gauge-chart-component></e-gauge-chart-component>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html } from '@eui/lit-component';
import 'components/shared/gauge-chart/src/GaugeChart';
import { formatBytes, loc } from 'utils/Utils';
import style from './gaugeChartComponent.css';

/**
 * @property {string} title - Chart component title
 * @property {number} used - Usage value
 * @property {number} total - Total value
 * @property {string} unit - Unit string
 */
@definition('e-gauge-chart-component', {
  style,
  home: 'gauge-chart-component',
  props: {
    title: { attribute: true, type: String, default: '' },
    used: { attribute: false, type: Number, default: 0 },
    total: { attribute: false, type: Number, default: 0 },
    unit: { attribute: true, type: String, default: '' },
    percentage: { attribute: false, type: Number },
  },
})
export default class GaugeChartComponent extends LitComponent {
  didChangeProps() {
    this.calculatePercentage();
    this.data = this.convertData();
  }

  /**
   * Calculate percentage
   */
  calculatePercentage() {
    if (this.used > 0 && this.total > 0) {
      this.percentage = ((this.used / this.total) * 100).toFixed(1);
    }
  }

  /**
   * Convert data to be displayed in the markup
   * @return {{total: *, used: *}|{total: {unit: *, value: *}, used: {unit: *, value: *}}}
   */
  convertData() {
    if (this.unit === 'Cores') {
      return {
        used: { value: this.used, unit: this.unit },
        total: { value: this.total, unit: this.unit },
      };
    }

    return {
      used: { ...formatBytes(this.used) },
      total: { ...formatBytes(this.total) },
    };
  }

  /**
   * Get markup for displaying used, and total values
   * @param {Object} field - data field to display: this.data.used | this.data.total
   * @return {*}
   */
  getValueMarkup(field) {
    // eslint-disable-next-line no-restricted-globals
    if (isNaN(field.value) || field.value < 0) {
      return loc('NO_DATA');
    }
    return html` ${field.value} <span>${field.unit}</span> `;
  }

  /**
   * Render the <e-gauge-chart-component> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <div class="title">${this.title}</div>
      <e-gauge-chart .percentage="${this.percentage}"></e-gauge-chart>
      <div class="bottom-fields">
        <div class="used">
          <div class="bottom-title">Used</div>
          <div class="bottom-value">${this.getValueMarkup(this.data.used)}</div>
        </div>
        <div class="total">
          <div class="bottom-title">Total</div>
          <div class="bottom-value">${this.getValueMarkup(this.data.total)}</div>
        </div>
      </div>
    `;
  }
}

/**
 * Register the component as e-gauge-chart-component.
 * Registration can be done at a later time and with a different name
 */
GaugeChartComponent.register();
