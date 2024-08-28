import { combineReducers } from 'redux';
import {
  ADD_NOTIFICATION,
  REMOVE_NOTIFICATIONS,
  SET_GLOBAL_ACTIONS,
  SET_LOADING_OVERLAY_STATE,
} from './actions';

/**
 * Notifications reducer
 * @param {Object} state
 * @param {Object} action
 * @return {{}}
 */
function notifications(state = {}, action) {
  switch (action.type) {
    case ADD_NOTIFICATION:
      return { ...action.payload };
    case REMOVE_NOTIFICATIONS:
      return {};
    default:
      return state;
  }
}

/**
 * Global permission reducer
 * @param {Object} state
 * @param {Object} action
 * @return {{}}
 */
function permissions(state = {}, action) {
  if (action.type === SET_GLOBAL_ACTIONS) {
    return { ...action.payload };
  }

  return state;
}

function loadingOverlay(state = {}, action) {
  if (action.type === SET_LOADING_OVERLAY_STATE) {
    return { ...action.payload };
  }

  return state;
}

export const reducers = combineReducers({
  notifications,
  permissions,
  loadingOverlay,
});
