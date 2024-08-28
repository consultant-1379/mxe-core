/**
 * Component ViewChange is defined as
 * `<e-view-change>`
 *
 * Imperatively create component
 * @example
 * let component = new ViewChange();
 *
 * Declaratively create component
 * @example
 * <e-view-change></e-view-change>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html } from '@eui/lit-component';
import style from './viewChange.css';

/**
 * @property {Boolean} propOne - show active/inactive state.
 * @property {string} propTwo - shows the "Hello World" string.
 */
@definition('e-view-change', {
  style,
  home: 'view-change',
  props: {
    isListView: { attribute: false, type: Boolean },
    toggleView: { attribute: false },
  },
})
export default class ViewChange extends LitComponent {
  /**
   * Render the <e-view-change> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <eui-v0-icon
        name="${this.isListView ? 'view-list' : 'view-tiles'}"
        class="view-change-icon"
        @click="${this.toggleView}"
      ></eui-v0-icon>
    `;
  }
}

/**
 * Register the component as e-view-change.
 * Registration can be done at a later time and with a different name
 */
ViewChange.register();
