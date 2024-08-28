/**
 * Needed for prevent default events in the browser, in the event of drag and drop
 * @param {Object} event - Event type: any
 */
export function preventDefaultEvent(event) {
  if (event) {
    event.preventDefault();
    event.stopPropagation();
  }
}

/**
 * Localizer function
 * @param {string} key - Key to be localized
 * @return {*}
 */
export const loc = (key) => {
  if (window && window.EUI && window.EUI.Localizer) {
    return window.EUI.Localizer.loc[key] || key;
  }
  return key;
};

/**
 * Open new browser window centered (hopefully)
 * @param {string} url - URL to be opened
 * @param {Number} width - width of the window
 * @param {Number} height - height of the window
 * @return {Window}
 */
export function openCenteredWindow(url, name = '_blank', width = 800, height = 600) {
  if (!window || !window.screen) {
    throw new Error('No window object or screen data');
  }
  const pos = {
    x: window.screen.width / 2 - width / 2,
    y: window.screen.height / 2 - height / 2,
  };

  const features = `width=${width} height=${height} left=${pos.x} top=${pos.y}`;

  return window.open(url, name, features);
}

/**
 * Check object for deep nesting
 * @param {Object} obj - Object to be tested
 * @param {string} parameter - Parameter name
 * @param {string} rest
 * @return {boolean}
 */
export function checkNested(obj, parameter, ...rest) {
  if (obj === undefined) return false;
  // eslint-disable-next-line no-prototype-builtins
  if (rest.length === 0 && obj.hasOwnProperty(parameter)) return true;
  return checkNested(obj[parameter], ...rest);
}

/**
 * Format bytes
 * @param bytes
 * @param decimals
 * @return {*}
 */
export function formatBytes(bytes, decimals = 2) {
  if (bytes === 0) return { value: -1, unit: '' };

  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return {
    // eslint-disable-next-line no-restricted-properties
    value: parseFloat((bytes / Math.pow(k, i)).toFixed(dm)),
    unit: sizes[i],
  };
}

/**
 * Convert a date to local date
 * @param {string} date - Date to be formatted
 * @return {string}
 */
export function formatDateToLocalDate(date) {
  if (!date) {
    return loc('NO_DATE_AVAILABLE');
  }
  try {
    return new Date(date).toLocaleString();
  } catch (e) {
    console.error(e);
    return date.toString();
  }
}

/**
 * Get chart steps
 * @return {string}
 */
export function getChartStep(value) {
  switch (value) {
    default:
    case 5:
      return '5s';
    case 60:
      return '30s';
    case 360:
      return '3m';
    case 720:
      return '6m';
    case 1440:
      return '12m';
    case 10080:
      return '84m';
  }
}

/**
 * Get input field by given event
 * @return {*}
 */
// eslint-disable-next-line consistent-return
export const getEventPath = (event) => {
  if (!event) {
    return '';
  }
  if (event.path) {
    return event.path[0];
  }
  if (!event.path && event.composedPath().length > 0) {
    return event.composedPath()[0];
  }
  if (!event.path && event.originalTarget) {
    return event.originalTarget;
  }
};

// eslint-disable-next-line consistent-return
export const getEventPathTarget = (event, target) => {
  if (event.path) {
    return event.path.find((object) => object.nodeName === target);
  }
  if (!event.path && event.composedPath().length > 0) {
    return event.composedPath().find((object) => object.nodeName === target);
  }
  if (!event.path && event.originalTarget) {
    return event.originalTarget;
  }
};

/**
 * Check event value for being numeric
 * @param event
 */
export function isNumeric(event) {
  const currentEvent = event || window.event;
  let key = currentEvent.keyCode || currentEvent.which;
  key = String.fromCharCode(key);
  const regex = /^\d+$/;
  if (!regex.test(key)) {
    currentEvent.returnValue = false;
    if (currentEvent.preventDefault) currentEvent.preventDefault();
  }
}

/**
 * Evaluate input for max number
 * @param event
 */
export function evaluateNumberInput(event) {
  preventDefaultEvent(event);
  const input = getEventPath(event);

  const max = parseInt(input.max, 10);
  if (input.value > max) {
    input.value = max;
  }

  const min = parseInt(input.min, 10);
  if (input.value < min) {
    input.value = min;
  }
}

/**
 * Shorten a string to less than maxLen characters without truncating words.
 * @param {string} str
 * @param {number} maxLen
 * @param {string} separator
 * @return {string|*}
 */
export function shorten(str, maxLen, separator = ' ') {
  if (str.length <= maxLen) return str;
  return `${str.substr(0, str.lastIndexOf(separator, maxLen))} ...`;
}

/**
 * Capitalize string
 * @param {string} string - String to capitalize
 * @return {string|*}
 */
export function capitalize(string) {
  return string.replace(/(^\w)|(\s\w)/g, (match) => match.toUpperCase());
}
