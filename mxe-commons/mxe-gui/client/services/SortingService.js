/**
 * @class SortingService
 * @classdesc Model list sorting functions
 */

class SortingService {
  /**
   * Sorting models by name and order
   * @param {string} order - ascending | descending
   * @param {Array} models - Models array
   * @param {boolean} isModelInfo - Is model info page ?
   * @param {boolean} isFirstFilter - Is first filter action
   * @returns {Array} - Sorted array of models
   */
  static sortModelsByName(order, models, isModelInfo, isFirstFilter) {
    const reverse = order === 'asc' ? -1 : 1;
    let result = [...models];
    if (isModelInfo) {
      result = result[0][1].sort((a, b) => reverse * a.displayName.localeCompare(b.displayName));
      return [[models[0].id, result]];
    }

    if (!isFirstFilter) {
      // MatomoService.trackEvent(
      //   MATOMO_CATEGORY_MODEL_CATALOGUE,
      //   MATOMO_ACTION_SORT,
      //   MATOMO_SORT_BY_NAME,
      //   order
      // );
    }
    return result.sort((a, b) => reverse * a[1][0].title.localeCompare(b[1][0].title));
  }

  /**
   * Sorting models by status and order
   * @param {string} order - ascending | descending
   * @param {Array} models - Models array
   * @param {boolean} isModelInfo - Is model info page ?
   * @returns {Array} - Sorted array of models
   */
  static sortModelsByStatus(order, models, isModelInfo) {
    const reverse = order === 'asc' ? -1 : 1;
    let result = [...models];
    if (isModelInfo) {
      result = result[0][1].sort((a, b) => reverse * a.status.localeCompare(b.status));
      return [[models[0].id, result]];
    }

    // MatomoService.trackEvent(
    //   MATOMO_CATEGORY_MODEL_CATALOGUE,
    //   MATOMO_ACTION_SORT,
    //   MATOMO_SORT_BY_STATUS,
    //   order
    // );

    return result.sort((a, b) => reverse * a[1][0].status.localeCompare(b[1][0].status));
  }

  /**
   * Sorting models by name and order
   * @param {string} order - ascending | descending
   * @param {Array} models - Models array
   * @param isFirstFilter
   * @returns {Array} - Sorted array of packages
   */
  static sortModelsByVersion(order, models, isFirstFilter) {
    const reverse = order === 'asc' ? -1 : 1;
    const result = [...models];
    if (!isFirstFilter) {
      // MatomoService.trackEvent(
      //   MATOMO_CATEGORY_MODEL_CATALOGUE,
      //   MATOMO_ACTION_SORT,
      //   MATOMO_SORT_BY_VERSION,
      //   order
      // );
    }
    return result.sort((a, b) => reverse * a.version.localeCompare(b.version));
  }

  /**
   * Sorting models by creation date and order
   * @param {string} order - ascending | descending
   * @param {Array} models - Models array
   * @param {boolean} isFirstFilter - Is first filter action
   * @returns {Array} - Sorted array of models
   */
  static sortModelsByDate(order, models, isFirstFilter) {
    const reverse = order === 'asc' ? -1 : 1;
    if (!isFirstFilter) {
      // MatomoService.trackEvent(
      //   MATOMO_CATEGORY_MODEL_CATALOGUE,
      //   MATOMO_ACTION_SORT,
      //   MATOMO_SORT_BY_DATE,
      //   order
      // );
    }
    return [...models].sort(
      (a, b) => reverse * (new Date(b[1][0].created) - new Date(a[1][0].created))
    );
  }

  /**
   * Sorting flows by name and order
   * @param {string} order - ascending | descending
   * @param {Array} flows - Flows array
   * @returns {Array} - Sorted array of flows
   */
  static sortFlowsByName(order, flows) {
    const reverse = order === 'asc' ? -1 : 1;
    // MatomoService.trackEvent(
    //   MATOMO_CATEGORY_FLOW_DEPLOYMENT_LIST,
    //   MATOMO_ACTION_SORT,
    //   MATOMO_SORT_BY_NAME,
    //   order
    // );
    return [...flows].sort((a, b) => reverse * a.name.localeCompare(b.name));
  }

  /**
   * Sorting flows by name and order
   * @param {string} order - ascending | descending
   * @param {Array} flows - Flows array
   * @returns {Array} - Sorted array of flows
   */
  static sortFlowsByStatus(order, flows) {
    const reverse = order === 'asc' ? -1 : 1;
    // MatomoService.trackEvent(
    //   MATOMO_CATEGORY_FLOW_DEPLOYMENT_LIST,
    //   MATOMO_ACTION_SORT,
    //   MATOMO_SORT_BY_STATUS,
    //   order
    // );
    return [...flows].sort((a, b) => reverse * a.status.localeCompare(b.status));
  }

  /**
   * Sorting flows by creation date and order
   * @param {string} order - ascending | descending
   * @param {Array} flows - Flows array
   * @returns {Array} - Sorted array of flows
   */
  static sortFlowsByDate(order, flows) {
    const reverse = order === 'asc' ? -1 : 1;
    // MatomoService.trackEvent(
    //   MATOMO_CATEGORY_FLOW_DEPLOYMENT_LIST,
    //   MATOMO_ACTION_SORT,
    //   MATOMO_SORT_BY_DATE,
    //   order
    // );
    return [...flows].sort(
      (a, b) => reverse * (new Date(b.creationTime) - new Date(a.creationTime))
    );
  }

  /**
   * Sorting packages by name and order
   * @param {string} order - ascending | descending
   * @param {Array} packages - Packages array
   * @param {boolean} isPackageInfo - Is model info page ?
   * @returns {Array} - Sorted array of packages
   */
  static sortPackagesByName(order, packages, isPackageInfo) {
    const reverse = order === 'asc' ? -1 : 1;
    let result = [...packages];
    if (isPackageInfo) {
      result = result[0][1].sort((a, b) => reverse * a.displayName.localeCompare(b.displayName));
      return [[packages[0].id, result]];
    }
    // MatomoService.trackEvent(
    //   MATOMO_CATEGORY_TRAINING_PACKAGE_LIST,
    //   MATOMO_ACTION_SORT,
    //   MATOMO_SORT_BY_NAME,
    //   order
    // );
    return result.sort((a, b) => reverse * a[1][0].title.localeCompare(b[1][0].title));
  }

  /**
   * Sorting packages by name and order
   * @param {string} order - ascending | descending
   * @param {Array} packages - Packages array
   * @param {boolean} isPackageInfo - Is model info page ?
   * @returns {Array} - Sorted array of packages
   */
  static sortPackagesByStatus(order, packages, isPackageInfo) {
    const reverse = order === 'asc' ? -1 : 1;
    let result = [...packages];
    if (isPackageInfo) {
      result = result[0][1].sort((a, b) => reverse * a.status.localeCompare(b.status));
      return [[packages[0].id, result]];
    }
    // MatomoService.trackEvent(
    //   MATOMO_CATEGORY_TRAINING_PACKAGE_LIST,
    //   MATOMO_ACTION_SORT,
    //   MATOMO_SORT_BY_STATUS,
    //   order
    // );
    return result.sort((a, b) => reverse * a[1][0].status.localeCompare(b[1][0].status));
  }

  /**
   * Sorting packages by name and order
   * @param {string} order - ascending | descending
   * @param {Array} packages - Packages array
   * @returns {Array} - Sorted array of packages
   */
  static sortPackagesByVersion(order, packages) {
    const reverse = order === 'asc' ? -1 : 1;
    const result = [...packages];
    // MatomoService.trackEvent(
    //   MATOMO_CATEGORY_TRAINING_PACKAGE_LIST,
    //   MATOMO_ACTION_SORT,
    //   MATOMO_SORT_BY_NAME,
    //   order
    // );
    return result.sort((a, b) => reverse * a.version.localeCompare(b.version));
  }

  /**
   * Sorting packages by creation date and order
   * @param {string} order - ascending | descending
   * @param {Array} packages - Packages array
   * @returns {Array} - Sorted array of packages
   */
  static sortPackagesByDate(order, packages) {
    const reverse = order === 'asc' ? -1 : 1;
    // MatomoService.trackEvent(
    //   MATOMO_CATEGORY_TRAINING_PACKAGE_LIST,
    //   MATOMO_ACTION_SORT,
    //   MATOMO_SORT_BY_DATE,
    //   order
    // );
    return [...packages].sort(
      (a, b) => reverse * (new Date(b[1][0].created) - new Date(a[1][0].created))
    );
  }

  /**
   * Sorting table by column
   * @param {string} order - ascending | descending
   * @param {Array} data - Data array
   * @param {string} column - Column to sort
   * @param {string} origin - Matomo category
   * @returns {Array} - Sorted array of packages
   */
  static sortTable(order, data, column, origin) {
    const reverse = order === 'asc' ? -1 : 1;
    // MatomoService.trackEvent(origin, MATOMO_ACTION_SORT, MATOMO_SORT_BY_DATE, order);

    return [...data].sort((a, b) => {
      if (a[column].values) {
        return reverse * a[column].values[0].localeCompare(b[column].values[0]);
      }
      if (Date.parse(a[column])) {
        return reverse * (new Date(b[column]) - new Date(a[column]));
      }
      return reverse * a[column].localeCompare(b[column]);
    });
  }

  /**
   * Sorting versions by name and order
   * @param {string} order - ascending | descending
   * @param {Array} models - Models array
   * @param {boolean} isModelInfo - Is model info page ?
   * @param {boolean} isFirstFilter - Is first filter action
   * @returns {Array} - Sorted array of models
   */
  static sortVersionsByName(order, models, isModelInfo, isFirstFilter) {
    const reverse = order === 'asc' ? -1 : 1;

    if (!isFirstFilter) {
      // MatomoService.trackEvent(
      //   MATOMO_CATEGORY_MODEL_CATALOGUE,
      //   MATOMO_ACTION_SORT,
      //   MATOMO_SORT_BY_NAME,
      //   order
      // );
    }
    return [...models].sort((a, b) => reverse * a.title.localeCompare(b.title));
  }

  /**
   * Sorting models by status and order
   * @param {string} order - ascending | descending
   * @param {Array} models - Models array
   * @param {boolean} isModelInfo - Is model info page ?
   * @returns {Array} - Sorted array of models
   */
  static sortVersionsByStatus(order, models, isModelInfo) {
    const reverse = order === 'asc' ? -1 : 1;

    // MatomoService.trackEvent(
    //   MATOMO_CATEGORY_MODEL_CATALOGUE,
    //   MATOMO_ACTION_SORT,
    //   MATOMO_SORT_BY_STATUS,
    //   order
    // );

    return [...models].sort((a, b) => reverse * a.status.localeCompare(b.status));
  }

  /**
   * Sorting models by name and order
   * @param {string} order - ascending | descending
   * @param {Array} models - Models array
   * @returns {Array} - Sorted array of packages
   */
  static sortVersionsByVersion(order, models) {
    const reverse = order === 'asc' ? -1 : 1;
    // MatomoService.trackEvent(
    //   MATOMO_CATEGORY_TRAINING_PACKAGE_LIST,
    //   MATOMO_ACTION_SORT,
    //   MATOMO_SORT_BY_NAME,
    //   order
    // );
    return [...models].sort((a, b) => reverse * a.version.localeCompare(b.version));
  }

  /**
   * Sorting models by creation date and order
   * @param {string} order - ascending | descending
   * @param {Array} models - Models array
   * @param {boolean} isFirstFilter - Is first filter action
   * @returns {Array} - Sorted array of models
   */
  static sortVersionsByDate(order, models, isFirstFilter) {
    const reverse = order === 'asc' ? -1 : 1;
    if (!isFirstFilter) {
      // MatomoService.trackEvent(
      //   MATOMO_CATEGORY_MODEL_CATALOGUE,
      //   MATOMO_ACTION_SORT,
      //   MATOMO_SORT_BY_DATE,
      //   order
      // );
    }

    return [...models].sort((a, b) => reverse * (new Date(b.created) - new Date(a.created)));
  }

  /**
   * Sorting flows by name and order
   * @param {string} order - ascending | descending
   * @param {Array} notebooks - notebooks array
   * @returns {Array} - Sorted array of flows
   */
  static sortNotebooksByName(order, notebooks) {
    const reverse = order === 'asc' ? -1 : 1;
    // MatomoService.trackEvent(
    //   MATOMO_CATEGORY_NOTEBOOK_LIST,
    //   MATOMO_ACTION_SORT,
    //   MATOMO_SORT_BY_NAME,
    //   order
    // );
    return [...notebooks].sort((a, b) => reverse * a.name.localeCompare(b.name));
  }
}

export default SortingService;
