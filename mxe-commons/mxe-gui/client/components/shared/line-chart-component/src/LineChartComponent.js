/**
 * Component LineChartComponent is defined as
 * `<e-line-chart-component>`
 *
 * Imperatively create component
 * @example
 * let component = new LineChartComponent();
 *
 * Declaratively create component
 * @example
 * <e-line-chart-component></e-line-chart-component>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { html, LitComponent, nothing } from '@eui/lit-component';
import * as c3 from 'c3';
import c3Style from 'c3/c3.min.css';
import { boundMethod } from 'autobind-decorator';
import style from './lineChartComponent.css';

/**
 * @property {Boolean} propOne - show active/inactive state.
 * @property {string} propTwo - shows the "Hello World" string.
 */
@definition('e-line-chart-component', {
  style: style + c3Style,
  home: 'line-chart-component',
  props: {
    // chart: { attribute: false },
    name: { attribute: false, default: '' },
    data: { attribute: false },
    dateFormat: { attribute: false, default: {} },
  },
})
export default class LineChartComponent extends LitComponent {
  didConnect() {
    this.handleChart();
  }

  didDisconnect() {
    if (this.chart) {
      this.chart = this.chart.destroy();
    }
  }

  didChangeProps(props) {
    if (props.has('data') || props.has('dateFormat')) {
      this.handleChart();
    }
  }

  /**
   * Generates chart or loads new data
   */
  @boundMethod
  handleChart() {
    if (!this.chart) {
      this.chart = c3.generate(this.config);
    } else {
      this.chart.load({
        ...this.configData,
        done: () => {
          this.chart.axis.range({ min: { x: this.minDate } });
        },
      });
    }
  }

  /**
   * Returns configuration data
   * @return {Object}
   */
  get configData() {
    return {
      x: 'x',
      labels: false,
      columns: [
        ['x', ...this.data.dates],
        [this.name, ...this.data.values],
      ],
    };
  }

  /**
   * Returns configuration object
   * @return {Object}
   */
  get config() {
    return {
      grid: {
        y: {
          show: true,
        },
      },
      legend: {
        show: false,
      },
      color: {
        pattern: ['var(--blue)'],
      },
      line: {
        connectNull: true,
      },
      transition: {
        duration: 0,
      },
      data: this.configData,
      axis: {
        x: {
          min: this.minDate,
          type: 'timeseries',
          tick: {
            fit: false,
            format: (x) => this.getDateFormat(x),
          },
        },
        y: {
          min: this.isGauge ? undefined : 0,
          padding: {
            top: 20,
            right: 0,
            bottom: 0,
            left: 0,
          },
        },
      },
    };
  }

  /**
   * Returns start date
   * @return {string}
   */
  get minDate() {
    return new Date(Math.floor(Date.now() - this.dateFormat.value * 60000));
  }

  /**
   * Returns if its a gauge
   * @return {boolean}
   */
  get isGauge() {
    return this.name.includes('gauge');
  }

  /**
   * Returns formatted date
   * @param {string} date
   * @return {string}
   */
  getDateFormat(date) {
    switch (this.dateFormat.value) {
      case 5:
      case 60:
      case 360:
      case 720:
        return date.toLocaleTimeString();
      case 1440:
      case 10080:
        return date.toLocaleDateString();
      default:
        return date.toLocaleString();
    }
  }

  /**
   * Returns chart markup
   * @return {*}
   */
  get chartMarkup() {
    return this.chart?.element ?? nothing;
  }

  /**
   * Render the <e-line-chart-component> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html` ${this.chartMarkup} `;
  }
}

/**
 * Register the component as e-line-chart-component.
 * Registration can be done at a later time and with a different name
 */
LineChartComponent.register();
