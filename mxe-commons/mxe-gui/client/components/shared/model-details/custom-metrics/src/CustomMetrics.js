/**
 * Component CustomMetrics is defined as
 * `<e-custom-metrics>`
 *
 * Imperatively create component
 * @example
 * let component = new CustomMetrics();
 *
 * Declaratively create component
 * @example
 * <e-custom-metrics></e-custom-metrics>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent, nothing, repeat, until } from '@eui/lit-component';
import PrometheusService from 'services/PrometheusService';
import { boundMethod } from 'autobind-decorator';
import { DEFAULT_CHART_UPDATE_INTERVAL_MS, MONITORING_INTERVALS } from 'utils/Defaults';
import 'components/shared/chart-legend/src/ChartLegend';
import 'components/shared/line-chart-component/src/LineChartComponent';
import { getChartStep, loc } from 'utils/Utils';
import style from './customMetrics.css';

/**
 * @property {Object} model
 * @property {Object} metrics
 * @property {Object} customMetrics
 * @property {Object} timerMetrics
 * @property {Object} counterMetrics
 * @property {Object} chartSpec
 * @property {Object} currentInterval
 * @property {Number} timer
 */
@definition('e-custom-metrics', {
  style,
  home: 'custom-metrics',
  props: {
    service: { attribute: false, type: Object },
    currentInterval: { attribute: false, type: Object, default: { ...MONITORING_INTERVALS[0] } },
    isLoading: { attribute: false, default: true },
  },
})
export default class CustomMetrics extends LitComponent {
  didConnect() {
    this.resetDataFetching();
  }

  didDisconnect() {
    this.stopDataFetching();
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
   * Get service name
   * @return {string}
   */
  get serviceName() {
    return this.service?.name ?? '';
  }

  /**
   * Reset fetching
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
   * Get all type of metrics from Prometheus
   * @return {Promise<any>}
   */
  get allMetrics() {
    if (!this.serviceName.length) {
      return new Promise(() => {});
    }
    return PrometheusService.getMetrics(this.serviceName);
  }

  /**
   * Get custom metrics
   * @param {Object} metrics - Metrics from Prometheus
   * @return {*[]|*}
   */
  @boundMethod
  getCustomMetrics(metrics) {
    const seldonMetrics = 'executor_client';
    if (metrics && metrics.data && metrics.data.result) {
      return metrics.data.result.filter((item) => !item.metric.__name__.includes(seldonMetrics));
    }
    return [];
  }

  /**
   * Get custom metrics
   * @param {Object} customMetrics - Filtered custom metrics
   * @return {*[]|*}
   */
  @boundMethod
  getTimerMetrics(customMetrics) {
    if (customMetrics) {
      return customMetrics.filter((item) => item.metric.__name__.includes('seconds_bucket'));
    }
    return [];
  }

  /**
   * Get custom metrics
   * @param {Object} customMetrics - Filtered custom metrics
   * @return {*[]|*}
   */
  @boundMethod
  getCounterMetrics(customMetrics) {
    if (customMetrics) {
      return customMetrics.filter((item) => item.metric.__name__.includes('counter'));
    }
    return [];
  }

  /**
   * Get custom metrics
   * @param {Object} customMetrics - Filtered custom metrics
   * @return {*[]|*}
   */
  @boundMethod
  getGaugeMetrics(customMetrics) {
    if (customMetrics) {
      return customMetrics.filter((item) => item.metric.__name__.includes('gauge'));
    }
    return [];
  }

  /**
   * Get all queries from the metric type
   * @param {Array} metrics - Metrics to gather from
   * @param {string} type - Type of metric
   * @return {[]|*}
   */
  @boundMethod
  getMetricsQueries(metrics, type) {
    if (!metrics && !type) {
      return [];
    }

    const chartStep = getChartStep(this.currentInterval.value);
    const interval = this.calculatedInterval;

    const queryArray = [];

    if (type === 'timer') {
      metrics.forEach((item) => {
        queryArray.push(
          PrometheusService.getTimerMetric(
            item.metric.__name__,
            this.serviceName,
            chartStep,
            interval,
            '20s'
          )
        );
      });
    }
    if (type === 'counter') {
      metrics.forEach((item) => {
        queryArray.push(
          PrometheusService.getCounterTotalMetric(
            item.metric.__name__,
            this.serviceName,
            chartStep,
            interval
          ),
          PrometheusService.getCounterRateMetric(
            item.metric.__name__,
            this.serviceName,
            chartStep,
            interval
          )
        );
      });
    }
    if (type === 'gauge') {
      metrics.forEach((item) => {
        queryArray.push(
          PrometheusService.getGaugeMetric(
            item.metric.__name__,
            this.serviceName,
            chartStep,
            interval
          )
        );
      });
    }

    return queryArray;
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
          data-type="click"
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
   * No data markup
   * @return {*}
   */
  get noDataMarkup() {
    return html` <div class="centered-text">${loc('NO_DATA')}</div> `;
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
   * Generate chart markup
   * @param {Object} metricData - Prometheus metric data
   * @param {string} unit - Legend unit
   * @return {*}
   */
  @boundMethod
  generateChart(metricData, unit = '[s]') {
    if (metricData?.status !== 'success' || !metricData?.data?.result?.[0]?.values?.length) {
      this.isLoading = false;
      return this.noDataMarkup;
    }

    const rawData = metricData.data.result[0].values;
    const metricTitle = metricData.data.result[0].metric.mxe_metric_name;
    let title = metricTitle;

    // Timer title
    if (metricTitle.includes('seconds_bucket')) {
      title = `99% quantile ${metricTitle} latency`;
      unit = '[s]';
    }

    // Counter rate title
    if (metricTitle.includes('-rate')) {
      title = `per-second rate of ${metricTitle.replace('-rate', '')} over the last 1 minute`;
    }

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
          <div class="title">
            ${title || ''}
            <span class="subtitle">${loc('CUSTOM_METRIC')}</span>
          </div>
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
   * Get charts markup
   * @return {Promise<any>}
   */
  get chartsMarkup() {
    return this.allMetrics
      .then(async (response) => {
        const customMetrics = this.getCustomMetrics(response);
        if (!customMetrics.length) {
          this.isLoading = false;
          return this.noDataMarkup;
        }

        const timerMetrics = Promise.all(
          this.getMetricsQueries(this.getTimerMetrics(customMetrics), 'timer')
        );
        const counterMetrics = Promise.all(
          this.getMetricsQueries(this.getCounterMetrics(customMetrics), 'counter')
        );
        const gaugeMetrics = Promise.all(
          this.getMetricsQueries(this.getGaugeMetrics(customMetrics), 'gauge')
        );

        return Promise.all([timerMetrics, counterMetrics, gaugeMetrics]).then((responses) => {
          const [timerData = [], counterData = [], gaugeData = []] = responses;

          const allCustomMetricsData = [...timerData, ...counterData, ...gaugeData].sort((a, b) =>
            a.data.result[0].metric.mxe_metric_name.localeCompare(
              b.data.result[0].metric.mxe_metric_name
            )
          );

          this.isLoading = false;

          return allCustomMetricsData.map((model) => this.generateChart(model, ''));
        });
      })
      .catch((e) => {
        this.isLoading = false;
        console.log(e);
        return this.getErrorMarkup(e);
      });
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
   * Render the <e-custom-metrics> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      ${this.loadingMarkup}
      <div class="custom-metrics">
        ${this.rangeSelectorMarkup}
        <div class="charts">
          ${until(this.chartsMarkup)}
        </div>
      </div>
    `;
  }
}

/**
 * Register the component as e-custom-metrics.
 * Registration can be done at a later time and with a different name
 */
CustomMetrics.register();
