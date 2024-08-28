/**
 * Component GaugeChart is defined as
 * `<e-gauge-chart>`
 *
 * Imperatively create component
 * @example
 * let component = new GaugeChart();
 *
 * Declaratively create component
 * @example
 * <e-gauge-chart></e-gauge-chart>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, nothing } from '@eui/lit-component';
import style from './gaugeChart.css';

/**
 * @property {number} percentage - show active/inactive state.
 * @property {string} color - show active/inactive state.
 */
@definition('e-gauge-chart', {
  style,
  home: 'gauge-chart',
  props: {
    percentage: { attribute: false, type: Number, default: -1 },
    color: { attribute: false, type: String },
  },
})
export default class GaugeChart extends LitComponent {
  /**
   * Get svg dash array
   * @return {string}
   */
  get dashArray() {
    return `${this.percentage} , 100`;
  }

  /**
   * Get color based on the percentage value
   * @return {string}
   */
  get colorFromPercentage() {
    if (this.percentage < 0) {
      return 'var(--grey)';
    }
    if (this.percentage <= 50 && this.percentage > 0) {
      return 'var(--green)';
    }
    if (this.percentage <= 90 && this.percentage > 50) {
      return 'var(--orange)';
    }
    return 'var(--red)';
  }

  /**
   * Get percentage markup, if the percentage is below zero then it's nothing,
   * else the value is being shown
   * @return {props.percentage|{default, attribute, type}|{attribute, type}|number|*|*}
   */
  get percentageMarkup() {
    if (this.percentage < 0) {
      return nothing;
    }

    return `${this.percentage}%`;
  }

  /**
   * Render the <e-gauge-chart> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <svg
        class="circle-chart"
        width="200"
        height="200"
        viewBox="0 0 36 36"
        version="1.1"
        xmlns="http://www.w3.org/2000/svg"
      >
        <circle
          class="circle-chart__background"
          stroke="#efefef"
          stroke-width="2"
          fill="none"
          cx="18"
          cy="18"
          r="15.9155"
        ></circle>
        <circle
          class="circle-chart__circle"
          style="stroke:${this.colorFromPercentage}; stroke-dasharray: ${this.dashArray}"
          stroke-width="2"
          stroke-linecap="round"
          fill="none"
          cx="18"
          cy="18"
          r="15.9155"
          stroke-dashoffset="0"
        ></circle>
        <g class="circle-chart__info">
          <text
            class="circle-chart__percent"
            x="50%"
            y="50%"
            alignment-baseline="central"
            text-anchor="middle"
            style="fill:${this.colorFromPercentage}"
            dominant-baseline="middle"
            font-size="8"
          >
            ${this.percentageMarkup}
          </text>
        </g>
      </svg>
    `;
  }
}

/**
 * Register the component as e-gauge-chart.
 * Registration can be done at a later time and with a different name
 */
GaugeChart.register();
