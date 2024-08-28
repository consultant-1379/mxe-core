/**
 * @class FilteringService
 * @classdesc Service for filtering models
 */

class FilteringService {
  /**
   * Filtering models by status
   * @param {Array} models - Models array to filter
   * @param {Set} filters - Array of status string
   * @param {boolean} isModelInfo - Is model info page ?
   * @param {boolean} isFirstFilter - Is first filter action
   * @returns {Array} - Filtered models array
   */
  static filterModelsByStatus(models, filters, isModelInfo, isFirstFilter) {
    if (isModelInfo) {
      const result = models[0][1].filter((model) => filters.has(model.status));
      return [[models[0].id, result]];
    }
    const results = models.filter((model) => filters.has(model[1][0].status));
    if (!isFirstFilter) {
      // MatomoService.trackSiteSearch([...filters].
      // join(', '), MATOMO_ACTION_FILTER, results.length);
    }
    return results;
  }

  /**
   * Filtering models by name
   * @param {Array} models - Models array to filter
   * @param {string} query - Query string from the input field
   * @param {boolean} isModelInfo - Is model info page ?
   * @param {boolean} isFirstFilter - Is first filter action
   * @returns {Array} - Filtered models array
   */
  static filterModelsByName(models, query, isModelInfo, isFirstFilter) {
    if (isModelInfo) {
      const result = models[0][1].filter((model) =>
        model.displayName.toLowerCase().includes(query)
      );
      return [[models[0].id, result]];
    }
    const results = [];
    models.forEach((model) => {
      const returnItem = [null, []];
      model[1].forEach((item) => {
        if (item.title.toLowerCase().includes(query)) {
          returnItem[0] = item.id;
          returnItem[1].push(item);
        }
      });

      if (returnItem[0]) {
        results.push(returnItem);
      }
    });

    if (!isFirstFilter) {
      // MatomoService.trackSiteSearch(query, MATOMO_ACTION_SEARCH, results.length);
    }
    return results;
  }

  /**
   * Filter models by date
   * @param {Array} models - models array to filter
   * @param date
   * @returns {Array} - Filtered models array
   */
  static filterModelsByDate(models, date) {
    let results = [...models];

    if (date.created) {
      if (date.created.from) {
        results = results.filter(
          (model) => new Date(model[1][0].created) > new Date(date.created.from)
        );
      }
      if (date.created.to) {
        results = results.filter(
          (model) => new Date(model[1][0].created) < new Date(date.created.to)
        );
      }
    }

    // MatomoService.trackSiteSearch(JSON.stringify(date), MATOMO_ACTION_SEARCH, results.length);

    return results;
  }

  /**
   * Filtering packages by status
   * @param {Array} packages - Packages array to filter
   * @param {Set} filters - Array of status string
   * @returns {Array} - Filtered packages array
   */
  static filterPackagesByStatus(packages, filters) {
    const results = packages.filter((package_) => filters.has(package_[1][0].status));
    // MatomoService.trackSiteSearch([...filters].join(', '), MATOMO_ACTION_FILTER, results.length);
    return results;
  }

  /**
   * Filtering packages by name
   * @param {Array} packages - Packages array to filter
   * @param {string} query - Query string from the input field
   * @returns {Array} - Filtered packages array
   */
  static filterPackagesByName(packages, query) {
    const results = packages.filter((package_) =>
      package_[1][0].title.toLowerCase().includes(query)
    );
    // MatomoService.trackSiteSearch(query, MATOMO_ACTION_SEARCH, results.length);
    return results;
  }

  /**
   * Filter packages by date
   * @param {Array} packages - packages array to filter
   * @param date
   * @returns {Array} - Filtered packages array
   */
  static filterPackagesByDate(packages, date) {
    let results = [...packages];

    if (date.created) {
      if (date.created.from) {
        results = results.filter(
          (package_) => new Date(package_[1][0].created) > new Date(date.created.from)
        );
      }
      if (date.created.to) {
        results = results.filter(
          (package_) => new Date(package_[1][0].created) < new Date(date.created.to)
        );
      }
    }

    // MatomoService.trackSiteSearch(JSON.stringify(date), MATOMO_ACTION_SEARCH, results.length);

    return results;
  }

  /**
   * Filtering jobs by status
   * @param {Array} jobs - Jobs array to filter
   * @param {Set} filters - Array of status string
   * @returns {Array} - Filtered jobs array
   */
  static filterJobsByStatus(jobs, filters) {
    const results = jobs.filter((job) => filters.has(job.status));
    // MatomoService.trackSiteSearch([...filters].join(', '), MATOMO_ACTION_FILTER, results.length);
    return results;
  }

  /**
   * Filtering jobs by name
   * @param {Array} jobs - Jobs array to filter
   * @param {string} query - Query string from the input field
   * @returns {Array} - Filtered jobs array
   */
  static filterJobsByName(jobs, query) {
    const results = jobs.filter((job) => job.id.toLowerCase().includes(query));
    // MatomoService.trackSiteSearch(query, MATOMO_ACTION_SEARCH, results.length);
    return results;
  }

  /**
   * Filter jobs by date
   * @param {Array} jobs - Jobs array to filter
   * @param date
   * @returns {Array} - Filtered jobs array
   */
  static filterJobsByDate(jobs, date) {
    let results = [...jobs];

    if (date.created) {
      if (date.created.from) {
        results = results.filter((job) => new Date(job.created) > new Date(date.created.from));
      }
      if (date.created.to) {
        results = results.filter((job) => new Date(job.created) < new Date(date.created.to));
      }
    }

    if (date.completed) {
      if (date.completed.from) {
        results = results.filter((job) => new Date(job.completed) > new Date(date.completed.from));
      }
      if (date.completed.to) {
        results = results.filter((job) => new Date(job.completed) < new Date(date.completed.to));
      }
    }

    // MatomoService.trackSiteSearch(JSON.stringify(date), MATOMO_ACTION_SEARCH, results.length);

    return results;
  }

  /**
   * Filtering services by status
   * @param {Array} services - services array to filter
   * @param {Set} filters - Array of status string
   * @returns {Array} - Filtered services array
   */
  static filterServicesByStatus(services, filters) {
    const results = services.filter((service) => filters.has(service.status));
    // MatomoService.trackSiteSearch([...filters].join(', '), MATOMO_ACTION_FILTER, results.length);
    return results;
  }

  /**
   * Filtering services by name
   * @param {Array} services - Services array to filter
   * @param {string} query - Query string from the input field
   * @returns {Array} - Filtered services array
   */

  static filterServicesByName(services, query) {
    const results = services.filter((service) => service.name.toLowerCase().includes(query));
    // MatomoService.trackSiteSearch(query, MATOMO_ACTION_SEARCH, results.length);
    return results;
  }

  /**
   * Filter services by date
   * @param {Array} services - services array to filter
   * @param date
   * @returns {Array} - Filtered services array
   */
  static filterServicesByDate(services, date) {
    let results = [...services];

    if (date.created) {
      if (date.created.from) {
        results = results.filter(
          (service) => new Date(service.created) > new Date(date.created.from)
        );
      }
      if (date.created.to) {
        results = results.filter(
          (service) => new Date(service.created) < new Date(date.created.to)
        );
      }
    }

    // MatomoService.trackSiteSearch(JSON.stringify(date), MATOMO_ACTION_SEARCH, results.length);

    return results;
  }

  /**
   * Filter versions
   * @param models
   * @param query
   * @return {*}
   */
  static filterVersions(models, query) {
    const results = models.filter((model) => JSON.stringify(model).includes(query));
    // MatomoService.trackSiteSearch(query, MATOMO_ACTION_SEARCH, results.length);
    return results;
  }

  /**
   * Filtering notebooks by name
   * @param {Array} notebooks - notebooks array to filter
   * @param {string} query - Query string from the input field
   * @returns {Array} - Filtered services array
   */
  static filterNotebooksByName(notebooks, query) {
    const results = notebooks.filter((notebook) =>
      notebook.name.toLowerCase().includes(query.toLowerCase())
    );
    // MatomoService.trackSiteSearch(query, MATOMO_ACTION_SEARCH, results.length);
    return results;
  }
}

export default FilteringService;
