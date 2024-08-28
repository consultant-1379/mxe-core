/**
 * Component ChartLegend is defined as
 * `<e-chart-legend>`
 *
 * Imperatively create component
 * @example
 * let component = new ChartLegend();
 *
 * Declaratively create component
 * @example
 * <e-chart-legend></e-chart-legend>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent, repeat } from '@eui/lit-component';
import { MONITORING_CHART_LEGEND } from 'utils/Defaults';
import { loc } from 'utils/Utils';
import style from './chartLegend.css';

/**
 * @property {array} selection - selected versions to show on the chart
 */
@definition('e-chart-legend', {
  style,
  home: 'chart-legend',
  props: {
    data: { attribute: false, type: Object, default: {} },
    selection: { attribute: false, type: Object, default: {} },
    unit: { attribute: false, default: null },
  },
})
export default class ChartLegend extends LitComponent {
  didConnect() {
    this.buildLegend();
  }

  didChangeProps(changedProps) {
    if (changedProps.has('data')) {
      this.buildLegend();
    }
  }

  /**
   * Build legend data
   */
  buildLegend() {
    if (!this.data?.values?.length) {
      return;
    }

    const legendItem = {};
    const data = [...this.data.values].map((item) => item ?? 0);
    const avg = [...data].reduce((p, c) => p + c, 0) / data.length;

    legendItem.min = Math.min(...data).toFixed(2);
    legendItem.max = Math.max(...data).toFixed(2);
    legendItem.avg = avg.toFixed(2);
    legendItem.current = data[data.length - 1].toFixed(2);
    this.selection = legendItem;
  }

  /**
   * Get column content
   * @param {string} column - Current columns
   * @return {*}
   */
  getColumn(column) {
    return html`
      <div class="column">
        <div class="name">${loc(column.toUpperCase())}</div>
        <div class="value">
          <span class="key">${this.selection[column]}</span>
          <span class="unit"> ${this.unit ?? loc('SECONDS')}</span>
        </div>
      </div>
    `;
  }

  /**
   * Get columns markup
   * @return {*}
   */
  get columns() {
    return repeat(MONITORING_CHART_LEGEND, (column) => this.getColumn(column));
  }

  /**
   * Render the <e-chart-legend> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <div class="title">${loc('LEGEND')}</div>
      <div class="legend">
        ${this.columns}
      </div>
    `;
  }
}

/**
 * Register the component as e-chart-legend.
 * Registration can be done at a later time and with a different name
 */
ChartLegend.register();
