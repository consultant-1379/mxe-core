/**
 * Dashboard is defined as
 * `<e-dashboard>`
 *
 * Imperatively create application
 * @example
 * let app = new Dashboard();
 *
 * Declaratively create application
 * @example
 * <e-dashboard></e-dashboard>
 *
 * @extends {App}
 */
import { App, html } from '@eui/app';
import { definition } from '@eui/component';
import { nothing, repeat } from '@eui/lit-component';
import { boundMethod } from 'autobind-decorator';
import 'components/dashboard/gauge-chart-component/src/GaugeChartComponent';
import 'components/dashboard/tile-info-component/src/TileInfoComponent';
import 'components/shared/model-details/src/ModelDetails';
import 'components/model-list/model-card/src/ModelCard';
import ModelListingService from 'services/ModelListingService';
import PrometheusService from 'services/PrometheusService';
import SortingService from 'services/SortingService';
import {
  DASHBOARD_MODELS_LENGTH,
  DEFAULT_CHART_UPDATE_INTERVAL_MS,
  DEFAULT_INTERVAL_MS,
  SORTING_OPTIONS,
} from 'utils/Defaults';
import { DISPATCH_NOTIFICATIONS, INVOKE_UPDATE, STATUS_ERROR } from 'utils/Enums';
import { loc } from 'utils/Utils';
import { toModelCatalogue } from 'utils/Navigator';
import ModelServiceService from 'services/ModelServiceService';
import NetworkError from 'utils/NetworkError';
import style from './dashboard.css';

@definition('e-dashboard', {
  style,
  props: {
    error: { attribute: false },
    modelTimer: { attribute: false, type: Number, default: null },
    runningModels: { attribute: false, type: Array, default: [] },
    recentModels: { attribute: false, type: Array, default: [] },
    gaugeTimer: { attribute: false, type: Number, default: null },
    gaugeData: { attribute: false, type: Object, default: {} },
    showModelInfoDialog: { attribute: false, type: Boolean, default: false },
    isLoading: { attribute: false, type: Boolean, default: true },
  },
})
export default class Dashboard extends App {
  didRender() {
    window.addEventListener(INVOKE_UPDATE, this.getModels, false);
    window.addEventListener(DISPATCH_NOTIFICATIONS, this.dispatchNotifications, false);

    if (!this.modelTimer) {
      this.modelTimer = setInterval(async () => {
        await this.getModels();
      }, DEFAULT_INTERVAL_MS);
    }
    if (!this.gaugeTimer) {
      this.gaugeTimer = setInterval(async () => {
        await this.getInfo();
      }, DEFAULT_CHART_UPDATE_INTERVAL_MS);
    }
  }

  didDisconnect() {
    if (this.modelTimer) {
      clearInterval(this.modelTimer);
      this.modelTimer = null;
    }
    if (this.gaugeTimer) {
      clearInterval(this.gaugeTimer);
      this.gaugeTimer = null;
    }

    window.removeEventListener(INVOKE_UPDATE, this.getModels, false);
    window.removeEventListener(DISPATCH_NOTIFICATIONS, this.dispatchNotifications, false);
  }

  /**
   * Component did connect
   * @return {Promise<void>}
   */
  async didConnect() {
    await this.getModels();
    await this.getInfo();
  }

  /**
   * Get chart data from Prometheus
   * @return {Promise<void>}
   */
  async getInfo() {
    try {
      const request = await PrometheusService.getGlobalRequestRate();

      this.gaugeData = {
        request: this.prepareGaugeData(request),
      };
    } catch (e) {
      console.error(e);
    }
  }

  /**
   * Prepare gauge data
   * @param {Object} item - Item to be prepared
   * @return {string|number}
   */
  prepareGaugeData(item) {
    if (item?.data?.result?.[0]?.value) {
      return Math.round(parseFloat(item.data.result[0].value[1]));
    }
    return -1;
  }

  /**
   * Get merged models
   */
  @boundMethod
  async getModels() {
    try {
      this.isLoading = true;
      const models = await ModelListingService.getModels();
      const services = await ModelServiceService.getModelServices();

      this.models = SortingService.sortModelsByDate(SORTING_OPTIONS[0].order, models);

      const recentModels = [];
      models.forEach((modelById) => {
        modelById[1].forEach((model) => {
          if (model.status !== STATUS_ERROR) {
            recentModels.push([model.id, [model]]);
          }
        });
      });

      this.runningModels = services;
      this.recentModels = recentModels;
      this.isLoading = false;
    } catch (error) {
      console.error(error);
      this.isLoading = false;
      if (error instanceof NetworkError) {
        this.error = error.message;
      } else {
        this.error = true;
      }
    }
  }

  /**
   * Navigates to model list page
   */
  navigateToModelList() {
    // MatomoService.trackEvent(MATOMO_CATEGORY_DASHBOARD, MATOMO_ACTION_CLICK, loc('SEE_ALL'));
    toModelCatalogue();
  }

  /**
   * Get model list lit-html markup
   * @return {*}
   */
  getModelListMarkup() {
    if (this.error) {
      return html`
        <div class="error">
          <p>${loc('ERROR_HAPPENED')}</p>
          ${this.error.length > 0 ? html` <p>${this.error}</p> ` : nothing}
        </div>
      `;
    }
    return repeat(
      this.recentModels.slice(0, DASHBOARD_MODELS_LENGTH),
      (model) => model.id,
      (model) => html`
        <e-model-card
          class="model-card"
          .showVersion=${true}
          .model="${model}"
          .shouldNavigate="${true}"
        ></e-model-card>
      `
    );
  }

  /**
   * Display Loading... when there is a request going on, else the number of recent items
   * @return {*}
   */
  get numberOfItemsMarkup() {
    if (this.isLoading) {
      return loc('LOADING');
    }
    const length =
      this.recentModels.length > DASHBOARD_MODELS_LENGTH
        ? DASHBOARD_MODELS_LENGTH
        : this.recentModels.length;
    return html`<span>${length} ${loc('ITEMS_IN_THE_LIST')}</span>`;
  }

  /**
   * Render the <e-dashboard> app. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <eui-layout-v0-tile class="dashboard" tile-title="${loc('MXE_VITAL_STATISTICS')}">
        <div class="content" slot="content">
          <div class="vital-statistics">
            <div class="info-list">
              <e-tile-info-component
                .data=${this.runningModels.length}
                .title="${loc('NUMBER_OF_RUNNING_MODELS')}"
              ></e-tile-info-component>
              <e-tile-info-component
                .title="${loc('GLOBAL_REQ_RATE')}"
                .data="${this.gaugeData.request}"
              ></e-tile-info-component>
            </div>
          </div>
          <div class="models">
            <div class="tile-header">
              <div class="title">${loc('RECENTLY_ONBOARDED')}</div>
              <sup @click="${this.navigateToModelList}">${loc('SEE_ALL')}</sup>
              <div class="number-of-items">${this.numberOfItemsMarkup}</div>
            </div>
            <div class="wrapper">${this.getModelListMarkup()}</div>
          </div>
        </div>
      </eui-layout-v0-tile>
    `;
  }
}
