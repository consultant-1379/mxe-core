/**
 * Component MonitoringInformation is defined as
 * `<e-monitoring-information>`
 *
 * Imperatively create component
 * @example
 * let component = new MonitoringInformation();
 *
 * Declaratively create component
 * @example
 * <e-monitoring-information></e-monitoring-information>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, nothing, repeat, until } from '@eui/lit-component';
import { ERROR } from 'utils/Enums';
import { getChartStep, loc } from 'utils/Utils';
import 'components/shared/line-chart-component/src/LineChartComponent';
import { boundMethod } from 'autobind-decorator';
import { DEFAULT_CHART_UPDATE_INTERVAL_MS, MONITORING_INTERVALS } from 'utils/Defaults';
import PrometheusService from 'services/PrometheusService';
import style from './monitoringInformation.css';
/**
 * @property {Object} model - Model object
 */
@definition('e-monitoring-information', {
  style,
  home: 'monitoring-information',
  props: {
    service: { attribute: false, default: null },
    latency: { attribute: false, type: Object, default: {} },
    requestRate: { attribute: false, type: Object, default: {} },
    isLoading: { attribute: false, default: true },
    error: { attribute: false, default: false },
    currentInterval: { attribute: false, default: { ...MONITORING_INTERVALS[0] } },
  },
})
export default class MonitoringInformation extends LitComponent {
  didConnect() {
    this.resetDataFetching();
  }

  didDisconnect() {
    this.stopDataFetching();
  }

  /**
   * Get service name
   * @return {string} - Service name in string
   */
  get serviceName() {
    return this.service?.name ?? '';
  }

  /**
   * Get model status
   * @returns {string} - Card status
   */
  get serviceStatus() {
    return this.service?.status ?? '';
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
   * Stop fetching data after closing the dialog
   */
  @boundMethod
  resetDataFetching() {
    clearInterval(this.timer);
    this.timer = setInterval(() => {
      this.executeRender();
    }, DEFAULT_CHART_UPDATE_INTERVAL_MS);
  }

  /**
   * Get real interval value
   * @return {number}
   */
  get calculatedInterval() {
    return this.currentInterval.value ? this.currentInterval.value * 60000 : 0;
  }

  /**
   * No data markup
   * @return {*}
   */
  get noDataMarkup() {
    return html` <div class="centered-text">${loc('NO_DATA')}</div> `;
  }

  /**
   * Generate chart markup
   * @param {Object} metricData - Prometheus metric data
   * @param {string} title - Chart title
   * @param {string} unit - Legend unit
   * @return {*}
   */
  @boundMethod
  generateChart(metricData, title, unit) {
    if (metricData?.status !== 'success' || !metricData?.data?.result?.[0]?.values?.length) {
      this.isLoading = false;
      return this.noDataMarkup;
    }

    const rawData = metricData.data.result[0].values;

    const data = { dates: [], values: [] };
    for (let i = 0, { length } = rawData; i < length; i++) {
      const [date, value] = rawData[i];
      const checkedValue = value === 'NaN' ? null : parseFloat(value);
      data.dates.push(new Date(date * 1000));
      data.values.push(checkedValue);
    }

    return html`
      <div class="container">
        <div class="left-side">
          <e-chart-legend .data=${data} .unit="${unit}"></e-chart-legend>
        </div>
        <div class="right-side">
          <div class="title">${title}</div>
          <e-line-chart-component
            .name="${title}"
            .data="${data}"
            .dateFormat="${this.currentInterval}"
            class="chart"
          ></e-line-chart-component>
        </div>
      </div>
    `;
  }

  /**
   * Handle interval selection
   * @param {Object} interval - Interval object
   * @return {Promise<void>}
   */
  @boundMethod
  async handleIntervalSelect(interval) {
    this.currentInterval = interval;
    this.resetDataFetching();
  }

  /**
   * Get range selector markup
   * @return {*}
   */
  get rangeSelectorMarkup() {
    return html`
      <div class="range-selector">
        <div class="title">${loc('CHOOSE_PERIOD')}:</div>
        <eui-base-v0-dropdown
          class="interval"
          width="100%"
          label="${this.currentInterval.name}"
          data-type="single"
        >
          ${this.intervalOptionsMarkup}
        </eui-base-v0-dropdown>
      </div>
    `;
  }

  /**
   * Get range selector options
   * @return {*}
   */
  get intervalOptionsMarkup() {
    return MONITORING_INTERVALS.map(
      (option, i) =>
        html`
          <eui-base-v0-menu-item
            .label="${option.name}"
            tabindex="${i}"
            @click="${() => this.handleIntervalSelect(option)}"
          >
          </eui-base-v0-menu-item>
        `
    );
  }

  /**
   * Get loading screen
   * @return {*}
   */
  get loadingMarkup() {
    if (this.isLoading) {
      return html`
        <div class="loader-overlay">
          <eui-base-v0-loader></eui-base-v0-loader>
        </div>
      `;
    }
    return nothing;
  }

  /**
   * Get error markup
   * @param {Error} error - Error object
   * @return {*}
   */
  getErrorMarkup(error) {
    return html`
      <div class="centered-text">
        <img height="48" class="icon" src="/assets/icons/error-icon.svg" alt="error" />
        <div>${loc('ERROR_HAPPENED')}</div>
        <div class="message">${error.message ?? ''}</div>
      </div>
    `;
  }

  /**
   * Get latency chart data from Prometheus
   * @return {Promise<any>}
   */
  @boundMethod
  getLatencyChart() {
    return PrometheusService.getLatency(
      this.serviceName,
      getChartStep(this.currentInterval.value),
      this.calculatedInterval,
      '20s'
    );
  }

  /**
   * Get request rate data from Prometheus
   * @return {Promise<any>}
   */
  @boundMethod
  getRequestRateChart() {
    return PrometheusService.getRequestRate(
      this.serviceName,
      getChartStep(this.currentInterval.value),
      this.calculatedInterval,
      '20s'
    );
  }

  /**
   * Get charts markup
   * @return {Promise<[any, any]>}
   */
  get chartsMarkup() {
    return Promise.all([this.getLatencyChart(), this.getRequestRateChart()])
      .then((responses) => {
        const [latency = {}, requestRate = {}] = responses;
        this.isLoading = false;

        return html`
          <div class="request-rate">
            ${this.generateChart(requestRate, loc('REQUEST_RATE_TITLE'), '[req/s]')}
          </div>
          <div class="latency">
            ${this.generateChart(latency, loc('LATENCY_TITLE'), '[s]')}
          </div>
        `;
      })
      .catch((e) => {
        this.isLoading = false;
        return this.getErrorMarkup(e);
      });
  }

  /**
   * Render the <e-monitoring-information> component. This function is called each time a
   * prop changes.
   */
  render() {
    if (this.error) {
      return html`<div class="centered-text">${loc('ERROR_HAPPENED')}</div>`;
    }
    if (this.serviceStatus === ERROR) {
      return html`<div class="centered-text">${loc('NO_INFORMATION_FOR_MODEL')}</div>`;
    }

    return html`
      ${this.loadingMarkup}
      <div class="monitoring-information">
        ${this.rangeSelectorMarkup}
        <div class="charts">
          ${until(this.chartsMarkup)}
        </div>
      </div>
    `;
  }
}
/**
 * Register the component as e-monitoring-information.
 * Registration can be done at a later time and with a different name
 */
MonitoringInformation.register();
