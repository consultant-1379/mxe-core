import { definition } from '@eui/component';
import { Panel } from '@eui/panel';
import { html, nothing } from '@eui/lit-component';
import { boundMethod } from 'autobind-decorator';
import { capitalize, loc } from 'utils/Utils';
import { SUCCESS } from 'utils/Enums';
import { addNotification } from 'store/actions';
import { IS_STANDALONE_STR } from '../../../utils/Config';
import style from './userSettingsPanel.css';

@definition('eui-user-settings-panel', { style })
export default class SettingsPanel extends Panel {
  /**
   * Get username
   * @return {string | string}
   */
  get username() {
    return window.localStorage.getItem('username') || 'mxe-no-username';
  }

  get lastLoginTime() {
    return window.localStorage.getItem('lastLoginTime') || 'Not available';
  }

  /**
   * @private
   * @function didConnect
   * @description Component did connect callback
   */
  didConnect() {
    // Connect this application to the appStore to observe for state
    // changes for a specified state property or list of properties
    this.state = this.storeConnect('locale');
    this.permissions = store.getState().permissions;
  }

  /**
   * @private
   * @function _handleThemeToggle
   * @description Handles dispatching the switch theme event
   */
  @boundMethod
  _handleThemeToggle(e) {
    const theme = e.detail.on ? 'dark' : 'light';
    this.dispatch('SWITCH_THEME', theme);
  }

  /**
   * @private
   * @function _handleLogout
   * @description Handles the logout action from the button
   */
  @boundMethod
  _handleLogout() {
    // Dispatch an action to the store to update the state and execute any
    // pre state change functionality
    this.dispatch('ACTION_LOGOUT', { isLoggedIn: false });

    // Execute a method on the authentication plugin to clear the user
    // session, however that may have been stored
    this.plugin('authentication', 'clearSession');
  }

  /**
   * @private
   * @function _handleLocaleChange
   * @description Handles the locale change event
   */
  @boundMethod
  _handleLocaleChange(e) {
    this.dispatch('UPDATE_LOCALE', { localeId: e.currentTarget.value });
  }

  /**
   * Get profile markup
   * @return {*}
   */
  get profileMarkup() {
    return html`<div class="profile">
      <eui-v0-icon name="profile" color="white" size="50px"></eui-v0-icon>
      <div class="username">${this.username}</div>
      <div class="prevLogin">${loc('Last Logon Time:')} ${this.lastLoginTime}</div>
    </div>`;
  }

  /**
   * Get settings markup
   * @return {*}
   */
  get settingsMarkup() {
    return html`
      <div class="settings">
        <div class="title">${loc('MY_SETTINGS')}</div>
        <div class="item">
          <div class="left">${loc('SWITCH_THEME')}</div>
          <div class="right">
            <eui-base-v0-switch
              label-on="${loc('DARK')}"
              label-off="${loc('LIGHT')}"
              @change=${this._handleThemeToggle}
            ></eui-base-v0-switch>
          </div>
        </div>
      </div>
    `;
  }

  /**
   * Get footer markup
   * @return {*}
   */
  get footerMarkup() {
    return html`<div class="footer">
      <eui-base-v0-button class="bottom" big fullwidth @click=${this._handleLogout}
        >${loc('SIGN_OUT')}
      </eui-base-v0-button>
      ${window.EUI.MenuConfig?.app?.options?.version
        ? html`<div class="version">
            ${loc('APP_VERSION')} ${window.EUI.MenuConfig.app.options.version}
          </div>`
        : nothing}
    </div>`;
  }

  render() {
    return html`<div class="settings-panel">
      <div class="content">
        <div class="container">
          ${this.profileMarkup}
          <div class="content">
            ${this.settingsMarkup}
          </div>
        </div>
      </div>
      ${this.footerMarkup}
    </div>`;
  }
}
