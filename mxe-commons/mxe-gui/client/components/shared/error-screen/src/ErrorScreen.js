/**
 * Component ErrorScreen is defined as
 * `<e-error-screen>`
 *
 * Imperatively create component
 * @example
 * let component = new ErrorScreen();
 *
 * Declaratively create component
 * @example
 * <e-error-screen></e-error-screen>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html } from '@eui/lit-component';
import { loc } from 'utils/Utils';
import style from './errorScreen.css';

/**
 * @property {boolean} propOne - show active/inactive state.
 */
@definition('e-error-screen', {
  style,
  home: 'error-screen',
  props: {
    title: { attribute: false, type: String, default: loc('ERROR_HAPPENED') },
    subtitle: { attribute: false, type: String, default: '' },
  },
})
export default class ErrorScreen extends LitComponent {
  /**
   * Render the <e-error-screen> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <div class="error">
        <eui-v0-icon name="triangle-warning" class="icon"></eui-v0-icon>
        <div class="title">${this.title}</div>
        <div class="subtitle">${this.subtitle}</div>
      </disubv>
    `;
  }
}

/**
 * Register the component as e-error-screen.
 * Registration can be done at a later time and with a different name
 */
ErrorScreen.register();
