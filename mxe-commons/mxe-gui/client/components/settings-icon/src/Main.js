/**
 * Component SettingsIcon is defined as
 * `<e-settings-icon>`
 *
 * Imperatively create component
 * @example
 * let component = new SettingsIcon();
 *
 * Declaratively create component
 * @example
 * <e-settings-icon></e-settings-icon>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, nothing } from '@eui/lit-component';
import PermissionService from 'services/PermissionService';
import { toSettings } from 'utils/Navigator';
import { IS_STANDALONE_STR } from 'utils/Config';
import style from './settingsIcon.css';

/**
 * @property {Boolean} propOne - show active/inactive state.
 * @property {string} propTwo - shows the "Hello World" string.
 */
@definition('e-settings-icon', {
  style,
  home: 'settings-icon',
})
export default class Main extends LitComponent {
  handleEvent() {
    toSettings();
  }

  /**
   * Render the <e-settings-icon> component. This function is called each time a
   * prop changes.
   */
  render() {
    if (PermissionService.isAdministrator() && !localStorage.getItem(IS_STANDALONE_STR)) {
      return html`<eui-v0-icon name="settings" color="white" @click="${this}"></eui-v0-icon>`;
    }
    return nothing;
  }
}

/**
 * Register the component as e-settings-icon.
 * Registration can be done at a later time and with a different name
 */
Main.register();
