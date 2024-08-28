import { httpService } from 'utils/HttpService';
import { API_PROMETHEUS_PATH } from 'utils/Config';

/**
 * @class PrometheusService
 * @classdesc HTTP service for handling Prometheus API requests
 */
class PrometheusService {
  /**
   * Get chart data
   * https://prometheus.io/docs/prometheus/latest/querying/api/
   * @param {Object} query - Chart query
   * @returns {Promise<any>}
   */
  static getChartData(query) {
    return httpService.postURLEncodedRequest(`${API_PROMETHEUS_PATH}/query_range`, query);
  }

  /**
   * Get data from Prometheus
   * https://prometheus.io/docs/prometheus/latest/querying/api/
   * @param {Object} query - Chart query
   * @returns {Promise<any>}
   */
  static getData(query) {
    return httpService.postURLEncodedRequest(`${API_PROMETHEUS_PATH}/query`, query);
  }

  /**
   * Cluster memory usage used/total *100
   * @return {Promise<any>}
   */
  static getMemoryPercentage() {
    return this.getData({
      query: 'sum (container_memory_working_set_bytes{id="/"}) / sum (machine_memory_bytes) * 100',
    });
  }

  /**
   * Cluster memory total in bytes
   * @return {Promise<any>}
   */
  static getMemoryTotal() {
    return this.getData({
      query: 'sum (machine_memory_bytes)',
    });
  }

  /**
   * Cluster memory usage in bytes
   * @return {Promise<any>}
   */
  static getMemoryUsed() {
    return this.getData({
      query: 'sum (container_memory_working_set_bytes{id="/"})',
    });
  }

  /**
   * Cluster CPU usage used/total *100
   * @return {Promise<any>}
   */
  static getCPUPercentage() {
    return this.getData({
      query:
        'scalar(sum(rate(container_cpu_usage_seconds_total{id="/"}[1m])) or container_cpu_limit_usage{id="/"}) / sum (machine_cpu_cores) * 100',
    });
  }

  /**
   * Cluster CPU used in cores
   * @return {Promise<any>}
   */
  static getCPUUsed() {
    return this.getData({
      query:
        'sum(rate(container_cpu_usage_seconds_total{id="/"}[1m])) or container_cpu_limit_usage{id="/"}',
    });
  }

  /**
   * Cluster CPU total in cores
   * @return {Promise<any>}
   */
  static getCPUTotal() {
    return this.getData({
      query: 'sum (machine_cpu_cores)',
    });
  }

  /**
   * Cluster filesystem usage used/total *100
   * @return {Promise<any>}
   */
  static getFileSystemPercentage() {
    return this.getData({
      query:
        'sum (container_fs_usage_bytes{device=~"^/dev/[sv]da[0-9]$",id="/"}) / sum (container_fs_limit_bytes{device=~"^/dev/[sv]da[0-9]$",id="/"}) * 100',
    });
  }

  /**
   * Cluster filesystem usage used/total *100
   * @return {Promise<any>}
   */
  static getFileSystemUsed() {
    return this.getData({
      query: 'sum (container_fs_usage_bytes{device=~"^/dev/[sv]da[0-9]$",id="/"})',
    });
  }

  /**
   * Cluster filesystem usage used/total *100
   * @return {Promise<any>}
   */
  static getFileSystemTotal() {
    return this.getData({
      query: 'sum (container_fs_limit_bytes{device=~"^/dev/[sv]da[0-9]$",id="/"})',
    });
  }

  /**
   * Global request rate req/s
   * @deprecated
   * @return {Promise<any>}
   */
  static getGlobalRequestRate() {
    return this.getData({
      query: 'round(sum(rate(seldon_api_executor_client_requests_seconds_count[1m])), 0.001)',
    });
  }

  /**
   * Get request rate
   * @param {string} deploymentName - Deployment name
   * @param {string} step - Chart steps
   * @param {Number} interval - Interval in seconds
   * @param {string} time - Time
   * @return {Promise<any>}
   */
  static getRequestRate(deploymentName, step, interval, time = '1m') {
    return this.getChartData({
      query: `round(sum(rate(seldon_api_executor_client_requests_seconds_count{deployment_name="${deploymentName}"}[${time}])), 0.001)`,
      start: Math.floor((Date.now() - interval) / 1000),
      end: Math.floor(Date.now() / 1000),
      step,
    });
  }

  /**
   * Get Latency
   * @param {string} deploymentName - Deployment name
   * @param {string} step - Chart steps
   * @param {Number} interval - Interval in seconds
   * @param {string} time - Time
   * @return {Promise<any>}
   */
  static getLatency(deploymentName, step, interval, time = '1m') {
    return this.getChartData({
      query: `histogram_quantile(0.99, sum(rate(seldon_api_executor_client_requests_seconds_bucket{deployment_name='${deploymentName}'}[${time}])) by (mxe_metric_name,deployment_name,le))`,
      start: Math.floor((Date.now() - interval) / 1000),
      end: Math.floor(Date.now() / 1000),
      step,
    });
  }

  /**
   * Get Custom Metrics
   * @param {string} deploymentName - Deployment name
   * @return {Promise<any>}
   */
  static getMetrics(deploymentName) {
    return this.getData({
      query: `topk(1,sum({deployment_name="${deploymentName}"}) by (__name__)) by (__name__)`,
    });
  }

  /**
   * Get metrics finishing in 'seconds'
   * @param {string} metricName - Deployment name
   * @param {string} deploymentName - Deployment name
   * @param {string} step - Chart steps
   * @param {Number} interval - Interval in seconds
   * @param {string} time - Time
   * @return {Promise<any>}
   */
  static getTimerMetric(metricName, deploymentName, step, interval, time = '1m') {
    return this.getChartData({
      query: `histogram_quantile(0.99, sum(rate(${metricName}{deployment_name='${deploymentName}'}[${time}])) by (mxe_metric_name,deployment_name,le))`,
      start: Math.floor((Date.now() - interval) / 1000),
      end: Math.floor(Date.now() / 1000),
      step,
    });
  }

  /**
   * Get metrics finishing in 'total'
   * @param {string} metricName - Metric name
   * @param {string} deploymentName - Deployment name
   * @param {string} step - Chart steps
   * @param {Number} interval - Interval in seconds
   * @param {string} time - Time
   * @return {Promise<any>}
   */
  static getCounterTotalMetric(metricName, deploymentName, step, interval, time = '1m') {
    return this.getChartData({
      query: `${metricName}{deployment_name="${deploymentName}"}`,
      start: Math.floor((Date.now() - interval) / 1000),
      end: Math.floor(Date.now() / 1000),
      step,
    });
  }

  /**
   * Get metrics finishing in 'total' rate
   * @param {string} metricName - Metric name
   * @param {string} deploymentName - Deployment name
   * @param {string} step - Chart steps
   * @param {Number} interval - Interval in seconds
   * @param {string} time - Time
   * @return {Promise<any>}
   */
  static getCounterRateMetric(metricName, deploymentName, step, interval, time = '1m') {
    return this.getChartData({
      query: `label_replace(round(sum(rate(${metricName}{deployment_name="${deploymentName}"}[${time}])) by (mxe_metric_name,deployment_name), 0.001), "mxe_metric_name", "$1-rate", "mxe_metric_name", "(.*)")`,
      start: Math.floor((Date.now() - interval) / 1000),
      end: Math.floor(Date.now() / 1000),
      step,
    });
  }

  /**
   * Get metrics finishing in other than 'seconds' or 'total'
   * @param {string} metricName - Metric name
   * @param {string} deploymentName - Deployment name
   * @param {string} step - Chart steps
   * @param {Number} interval - Interval in seconds
   * @return {Promise<any>}
   */
  static getGaugeMetric(metricName, deploymentName, step, interval) {
    return this.getChartData({
      query: `${metricName}{deployment_name="${deploymentName}"}`,
      start: Math.floor((Date.now() - interval) / 1000),
      end: Math.floor(Date.now() / 1000),
      step,
    });
  }
}
export default PrometheusService;
