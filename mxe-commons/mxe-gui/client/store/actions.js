/*
 * action types
 */
export const ADD_NOTIFICATION = 'ADD_NOTIFICATION';
export const REMOVE_NOTIFICATION = 'REMOVE_NOTIFICATION';
export const REMOVE_NOTIFICATIONS = 'REMOVE_NOTIFICATIONS';
export const SET_GLOBAL_ACTIONS = 'SET_GLOBAL_ACTIONS';
export const SET_LOADING_OVERLAY_STATE = 'SET_LOADING_OVERLAY_STATE';

/**
 * Add notification
 * @param {Object} notification - Notification object
 * @return {{payload: {notification: object, date: string}, type: string}}
 */
export function addNotification(notification) {
  return {
    type: ADD_NOTIFICATION,
    payload: { ...notification, date: new Date().toLocaleTimeString() },
  };
}

/**
 * Remove notification
 * @param {Number} index - Index to delete
 * @return {{payload: number, type: string}}
 */
export function removeNotification(index) {
  return { type: REMOVE_NOTIFICATION, payload: index };
}

/**
 * Remove notifications
 * @return {{type: string}}
 */
export function removeNotifications() {
  return { type: REMOVE_NOTIFICATIONS };
}

/**
 * Set global actions
 * @param {Object} actions - Actions from auth token
 * @return {{payload: object, type: string}}
 */
export function setGlobalActions(actions) {
  return {
    type: SET_GLOBAL_ACTIONS,
    payload: { ...actions },
  };
}

export function setLoadingScreenState(show) {
  return {
    type: SET_LOADING_OVERLAY_STATE,
    payload: { show },
  };
}
