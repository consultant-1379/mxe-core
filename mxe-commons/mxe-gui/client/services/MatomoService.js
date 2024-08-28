/**
 * @class MatomoService
 * @classdesc Matomo analytics helper service
 */
class MatomoService {
  /**
   * Track site search
   * @param {string} keyword - Search keyword
   * @param {string} category - Search category
   * @param {Number} numberOfResults - Number of search results
   */
  static trackSiteSearch(keyword, category, numberOfResults) {
    if (!window._paq) {
      return;
    }

    window._paq.push([
      'trackSiteSearch',
      // Search keyword searched for
      keyword,
      // Search category selected in your search engine. If you do not need this, set to false
      category,
      // Number of results on the Search results page
      numberOfResults,
    ]);
  }

  /**
   * Track event
   * @param {string} category - Event category
   * @param {string} action - Event action
   * @param {string} name - Event target name
   * @param {String | Number | Object} [value] - Event target value
   */
  static trackEvent(category, action, name, value = '') {
    if (!window._paq) {
      return;
    }

    window._paq.push([
      'trackEvent',
      // Event category
      category,
      // Event action
      action,
      // Event target name
      name,
      // Event target value
      value,
    ]);
  }
}

export default MatomoService;
