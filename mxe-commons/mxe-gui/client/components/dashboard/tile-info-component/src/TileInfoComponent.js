/**
 * Component TileInfoComponent is defined as
 * `<e-tile-info-component>`
 *
 * Imperatively create component
 * @example
 * let component = new TileInfoComponent();
 *
 * Declaratively create component
 * @example
 * <e-tile-info-component></e-tile-info-component>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html } from '@eui/lit-component';
import style from './tileInfoComponent.css';

/**
 * @property {number} data - Info component data
 * @property {string} title - Info component title
 * @property {string} color - Info component color
 * @property {boolean} clickable - Info component is clickable or not
 */
@definition('e-tile-info-component', {
  style,
  home: 'tile-info-component',
  props: {
    data: { attribute: false, type: Number, default: 0 },
    title: { attribute: false, type: String, default: '' },
    color: { attribute: false, type: String, default: 'var(--blue)' },
    clickable: { attribute: false, type: Boolean, default: false },
  },
})
export default class TileInfoComponent extends LitComponent {
  /**
   * Get tile info class
   * @return {string}
   */
  get class() {
    return this.clickable ? 'container clickable' : 'container';
  }

  /**
   * Returns 0 if there is no data
   * @return {number}
   */
  get computedData() {
    return this.data < 0 ? 0 : this.data;
  }

  render() {
    return html`
      <div class="${this.class}">
        <span class="text">${this.title}</span>
        <span class="data" style="color:${this.color}">${this.computedData}</span>
      </div>
    `;
  }
}

/**
 * Register the component as e-tile-info-component.
 * Registration can be done at a later time and with a different name
 */
TileInfoComponent.register();
