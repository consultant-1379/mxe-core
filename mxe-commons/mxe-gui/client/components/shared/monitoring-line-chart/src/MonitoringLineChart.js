/**
 * Component MonitoringLineChart is defined as
 * `<e-monitoring-line-chart>`
 *
 * Imperatively create component
 * @example
 * let component = new MonitoringLineChart();
 *
 * Declaratively create component
 * @example
 * <e-monitoring-line-chart></e-monitoring-line-chart>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent, nothing } from '@eui/lit-component';
import 'components/shared/chart-legend/src/ChartLegend';
import 'components/shared/line-chart-component/src/LineChartComponent';
import PrometheusService from 'services/PrometheusService';
import {
  DEFAULT_CHART_UPDATE_INTERVAL_MS,
  DEFAULT_CHART_TITLES,
  MONITORING_INTERVALS,
} from 'utils/Defaults';
import { boundMethod } from 'autobind-decorator';
import { MODEL_INFO_DIALOG_CLOSED } from 'utils/Enums';
import { getChartStep } from 'utils/Utils';
import style from './monitoringLineChart.css';

@definition('e-monitoring-line-chart', {
  style,
  home: 'monitoring-line-chart',
  props: {
    service: { attribute: false, type: Object, default: null },
    title: { attribute: false, type: String, default: '' },
    unit: { attribute: false, type: String, default: '' },
    chartSpec: { attribute: false, type: Object, default: {} },
    chartData: { attribute: false, type: Array, default: [] },
    dates: { attribute: false, type: Array, default: [] },
    values: { attribute: false, type: Array, default: [] },
    currentInterval: { attribute: false, type: Object, default: { ...MONITORING_INTERVALS[0] } },
    timer: { attribute: false, type: Number },
  },
})
export default class MonitoringLineChart extends LitComponent {
  async didConnect() {
    if (!this.service) {
      return;
    }
    await this.getChartData();
    window.addEventListener(MODEL_INFO_DIALOG_CLOSED, this.stopDataFetching, false);
    if (!this.timer) {
      this.timer = setInterval(async () => {
        await this.getChartData();
      }, DEFAULT_CHART_UPDATE_INTERVAL_MS);
    }
  }

  /**
   * Stop fetching data after closing the dialog
   */
  @boundMethod
  stopDataFetching() {
    clearInterval(this.timer);
    this.timer = null;
  }

  /**
   * Get real interval value
   * @return {number}
   */
  get calculatedInterval() {
    return this.currentInterval.value ? this.currentInterval.value * 60000 : 0;
  }

  /**
   * Force chart responsibility, by deleting the height, width attributes
   */
  @boundMethod
  forceChartResponsibility() {
    const chartSVG = this.shadowRoot
      .querySelector('eui-chart-v0-vega')
      .shadowRoot.querySelector('.marks');
    chartSVG.removeAttribute('width');
    chartSVG.removeAttribute('height');
    chartSVG.style.width = '100%';
    chartSVG.style.height = '100%';
  }

  /**
   * Get chart data
   * @return {Promise<void>}
   */
  async getChartData() {
    const { name } = this.service;

    const response = await PrometheusService.getChartData({
      start: Math.floor((Date.now() - this.calculatedInterval) / 1000),
      end: Math.floor(Date.now() / 1000),
      step: getChartStep(this.currentInterval.value),
      query: this.getQuery(name),
    });

    // test if response is empty
    // response = { status: 'success', data: { resultType: 'matrix', result: [] } };

    if (response.status !== 'success') return;
    if (!response.data) return;
    if (!response.data.result) return;
    if (response.data.result.length < 1) return;

    const rawData = response.data.result[0].values;

    const result = [];
    const dates = [];
    const values = [];
    this.chartData = [];

    const multiplier = this.unit === 'ms' ? 1000 : 1;

    for (let i = 0, { length } = rawData; i < length; i++) {
      const data = rawData[i];
      result.push({
        date: new Date(data[0] * 1000).toLocaleTimeString(),
        value: parseFloat(data[1]) * multiplier,
        color: '',
      });
      dates.push(new Date(data[0] * 1000));
      values.push(parseFloat(data[1]) * multiplier);
    }

    this.chartData = result;
    this.dates = dates;
    this.values = values;
  }

  /**
   * Create Prometheus query
   * @param {string} serviceName - Service name
   * @return {string}
   */
  getQuery(serviceName) {
    switch (this.title) {
      case DEFAULT_CHART_TITLES[0].name:
        return `avg(rate(seldon_api_executor_client_requests_seconds_bucket{deployment_name="${serviceName}"}[20s])) by (deployment_name)`;
      case DEFAULT_CHART_TITLES[1].name:
      default:
        return `sum(rate(seldon_api_executor_client_requests_seconds_count{deployment_name="${serviceName}"}[1m]))`;
    }
  }

  /**
   * Handle interval selection
   * @param {Object} interval - Interval object
   * @return {Promise<void>}
   */
  async handleIntervalSelect(interval) {
    this.currentInterval = interval;
    await this.getChartData();
  }

  /**
   * Render the <e-monitoring-line-chart> component. This function is called each time a
   * prop changes.
   */
  render() {
    if (!this.chartSpec) {
      return nothing;
    }
    if (this.chartData.length < 1) {
      return html`
        <div class="left-side">
          <e-chart-legend .data=${this.chartData} .unit=${this.unit}></e-chart-legend>
        </div>
        <div class="chart">
          <span class="title">${this.title} [${this.unit}]</span>
          <div class="subtitle center">No ${this.title} data available</div>
        </div>
      `;
    }
    return html`
      <div class="left-side">
        <e-chart-legend .data=${this.chartData} .unit=${this.unit}></e-chart-legend>
        <eui-base-v0-dropdown
          class="interval"
          width="100%"
          label="${this.currentInterval.name}"
          data-type="click"
        >
          ${MONITORING_INTERVALS.map(
            (option) =>
              html`
                <div menu-item tabindex="0" @click="${() => this.handleIntervalSelect(option)}">
                  ${option.name}
                </div>
              `
          )}
        </eui-base-v0-dropdown>
      </div>
      <div class="chart">
        <span class="title">${this.title} [${this.unit}]</span>
        <e-line-chart-component
          .name="${this.title} [${this.unit}]"
          .dates="${this.dates}"
          .values="${this.values}"
          class="chart"
        ></e-line-chart-component>
      </div>
    `;
  }
}

/**
 * Register the component as e-monitoring-line-chart.
 * Registration can be done at a later time and with a different name
 */
MonitoringLineChart.register();
