const express = require('express');
const path = require('path');

module.exports = (app) => {
  /**
   * components
   */
  app.use(
    '/components/logo',
    express.static(path.join(__dirname, '../node_modules/@eui/container/components/logo'))
  );

  app.use(
    '/components/user-display',
    express.static(path.join(__dirname, '../node_modules/@eui/container/components/user-display'))
  );

  app.use(
    '/components/shared/model-pagination/0/config.json',
    express.static(path.join(__dirname, '../client/components/shared/model-pagination/config.json'))
  );
  /**
   * panels
   */
  app.use(
    '/panels/menu-panel',
    express.static(path.join(__dirname, '../node_modules/@eui/container/panels/menu-panel'))
  );
  app.use(
    '/panels/notification-panel',
    express.static(path.join(__dirname, '../client/panels/sample-system-panel'))
  );
  app.use(
    '/panels/left-menu-panel',
    express.static(path.join(__dirname, '../client/panels/left-menu-panel'))
  );
  app.use(
    '/panels/user-settings-panel',
    express.static(path.join(__dirname, './client/panels/user-settings-panel'))
  );

  /**
   * plugins
   */
  app.use(
    '/plugins/authentication',
    express.static(path.join(__dirname, '../client/plugins/authentication'))
  );
  app.use(
    '/plugins/notifications',
    express.static(path.join(__dirname, '../client/plugins/notifications'))
  );
  app.use(
    '/plugins/error-handler',
    express.static(path.join(__dirname, '../client/plugins/error-handler'))
  );
  app.use('/plugins/loading', express.static(path.join(__dirname, '../client/plugins/loading')));

  /**
   * libs
   */
  app.use(
    '/libs/system.js',
    express.static(path.join(__dirname, '../node_modules/@eui/container/libs/system.js'))
  );
  app.use(
    '/libs/@eui',
    express.static(path.join(__dirname, '../node_modules/@eui/container/libs/@eui'))
  );

  /**
   * assets
   */
  app.use(
    '/assets/fonts',
    express.static(path.join(__dirname, '../node_modules/@eui/theme/0/fonts'))
  );
  app.use(
    '/assets/css',
    express.static(path.join(__dirname, '../node_modules/@eui/container/assets/css'))
  );
  app.use(
    '/assets/icons',
    express.static(path.join(__dirname, '../node_modules/@eui/container/assets/icons'))
  );
  app.use(
    '/assets/img',
    express.static(path.join(__dirname, '../node_modules/@eui/container/assets/img'))
  );
  app.use(
    '/assets/favicon.ico',
    express.static(path.join(__dirname, '../node_modules/@eui/container/assets/favicon.ico'))
  );
  app.use(
    '/assets/warning_icon.svg',
    express.static(path.join(__dirname, '../node_modules/@eui/container/assets/warning_icon.svg'))
  );

  /**
   * polyfills
   */
  app.use(
    '/libs/polyfills',
    express.static(path.join(__dirname, '../node_modules/@eui/container/libs/polyfills'))
  );

  app.use(
    '/components/dashboard/gauge-chart-component/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/dashboard/gauge-chart-component/config.json')
    )
  );
  app.use(
    '/components/dashboard/tile-info-component/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/dashboard/tile-info-component/config.json')
    )
  );

  app.use(
    '/components/model-list/model-container/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/model-list/model-container/config.json')
    )
  );
  app.use(
    '/components/model-list/model-card/0/config.json',
    express.static(path.join(__dirname, '../client/components/model-list/model-card/config.json'))
  );

  app.use(
    '/components/model-list/model-list/0/config.json',
    express.static(path.join(__dirname, '../client/components/model-list/model-list/config.json'))
  );
  app.use(
    '/components/model-list/model-table/0/config.json',
    express.static(path.join(__dirname, '../client/components/model-list/model-table/config.json'))
  );
  app.use(
    '/components/model-list/model-versions/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/model-list/model-versions/config.json')
    )
  );

  app.use(
    '/components/notebooks/notebook-container/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/notebooks/notebook-container/config.json')
    )
  );
  app.use(
    '/components/notebooks/notebook-card/0/config.json',
    express.static(path.join(__dirname, '../client/components/notebooks/notebook-card/config.json'))
  );

  app.use(
    '/components/notebooks/create-notebook/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/notebooks/create-notebook/config.json')
    )
  );

  app.use(
    '/components/notebooks/notebook-list/0/config.json',
    express.static(path.join(__dirname, '../client/components/notebooks/notebook-list/config.json'))
  );
  app.use(
    '/components/notebooks/notebook-table/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/notebooks/notebook-table/config.json')
    )
  );

  app.use(
    '/components/shared/sidebar/0/config.json',
    express.static(path.join(__dirname, '../client/components/shared/sidebar/config.json'))
  );

  app.use(
    '/components/shared/upload-component/0/config.json',
    express.static(path.join(__dirname, '../client/components/shared/upload-component/config.json'))
  );

  app.use(
    '/components/shared/model-details/0/config.json',
    express.static(path.join(__dirname, '../client/components/shared/model-details/config.json'))
  );

  app.use(
    '/components/shared/model-details/model-information/0/config.json',
    express.static(
      path.join(
        __dirname,
        '../client/components/shared/model-details/model-information/config.json'
      )
    )
  );

  app.use(
    '/components/shared/model-details/monitoring-information/0/config.json',
    express.static(
      path.join(
        __dirname,
        '../client/components/shared/model-details/monitoring-information/config.json'
      )
    )
  );

  app.use(
    '/components/shared/model-details/contained-services/0/config.json',
    express.static(
      path.join(
        __dirname,
        '../client/components/shared/model-details/contained-services/config.json'
      )
    )
  );

  app.use(
    '/components/shared/model-details/custom-metrics/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/shared/custom-metrics/reward/config.json')
    )
  );

  app.use(
    '/components/shared/monitoring-line-chart/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/shared/monitoring-line-chart/config.json')
    )
  );

  app.use(
    '/components/shared/chart-legend/0/config.json',
    express.static(path.join(__dirname, '../client/components/shared/chart-legend/config.json'))
  );

  app.use(
    '/components/shared/gauge-chart/0/config.json',
    express.static(path.join(__dirname, '../client/components/shared/gauge-chart/config.json'))
  );

  app.use(
    '/components/shared/error-screen/0/config.json',
    express.static(path.join(__dirname, '../client/components/shared/error-screen/config.json'))
  );

  app.use(
    '/components/shared/custom-table/0/config.json',
    express.static(path.join(__dirname, '../client/components/shared/custom-table/config.json'))
  );

  app.use(
    '/components/invoke-model/0/config.json',
    express.static(path.join(__dirname, '../client/components/invoke-model/config.json'))
  );

  app.use(
    '/components/training-packages/package-container/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/training-packages/package-container/config.json')
    )
  );
  app.use(
    '/components/training-packages/package-card/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/training-packages/package-card/config.json')
    )
  );
  app.use(
    '/components/training-packages/package-list/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/training-packages/package-list/config.json')
    )
  );
  app.use(
    '/components/training-packages/package-versions/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/training-packages/package-versions/config.json')
    )
  );

  app.use(
    '/components/training-jobs/job-container/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/training-jobs/job-container/config.json')
    )
  );

  app.use(
    '/components/training-jobs/job-table/0/config.json',
    express.static(path.join(__dirname, '../client/components/training-jobs/job-table/config.json'))
  );

  app.use(
    '/components/model-services/services-container/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/model-services/services-container/config.json')
    )
  );

  app.use(
    '/components/model-services/services-table/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/model-services/services-table/config.json')
    )
  );

  app.use(
    '/components/model-services/service-detail/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/model-services/service-detail/config.json')
    )
  );

  app.use(
    '/components/model-services/service-detail/service-models/0/config.json',
    express.static(
      path.join(
        __dirname,
        '../client/components/model-services/service-detail/service-models/config.json'
      )
    )
  );

  app.use(
    '/components/shared/create-model-service/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/shared/create-model-service/config.json')
    )
  );

  app.use(
    '/components/shared/model-selector/0/config.json',
    express.static(path.join(__dirname, '../client/components/shared/model-selector/config.json'))
  );

  app.use(
    '/components/shared/change-model/0/config.json',
    express.static(path.join(__dirname, '../client/components/shared/change-model/config.json'))
  );

  app.use(
    '/components/training-packages/package-table/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/training-packages/package-table/config.json')
    )
  );

  app.use(
    '/components/shared/list-sorting/0/config.json',
    express.static(path.join(__dirname, '../client/components/shared/list-sorting/config.json'))
  );

  app.use(
    '/components/shared/filter-pill/0/config.json',
    express.static(path.join(__dirname, '../client/components/shared/filter-pill/config.json'))
  );

  app.use(
    '/components/view-change/0/config.json',
    express.static(path.join(__dirname, '../client/components/view-change/config.json'))
  );

  app.use(
    '/components/settings-icon/0/config.json',
    express.static(path.join(__dirname, '../client/components/settings-icon/config.json'))
  );

  app.use(
    '/components/settings/author-list/0/config.json',
    express.static(path.join(__dirname, '../client/components/settings/author-list/config.json'))
  );

  app.use(
    '/components/shared/model-selector-container/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/shared/model-selector-container/config.json')
    )
  );

  app.use(
    '/components/shared/line-chart-component/0/config.json',
    express.static(
      path.join(__dirname, '../client/components/shared/line-chart-component/config.json')
    )
  );

  app.use(
    '/components/shared/service-scaling/0/config.json',
    express.static(path.join(__dirname, '../client/components/shared/service-scaling/config.json'))
  );

  app.use(
    '/components/model-services/service-logs/0/config.json',
    express.static(path.join(__dirname, '../client/components/service-logs/config.json'))
  );
};
