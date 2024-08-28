/**
 * Component PackageList is defined as
 * `<e-package-list>`
 *
 * Imperatively create component
 * @example
 * let component = new PackageList();
 *
 * Declaratively create component
 * @example
 * <e-package-list></e-package-list>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, repeat } from '@eui/lit-component';
import style from './packageList.css';

/**
 * @property {Array} packages - Package list array
 * @property {Array} selected - Selected packages array
 */
@definition('e-package-list', {
  style,
  home: 'package-list',
  props: {
    packages: { attribute: false, type: Array, default: [] },
    selected: { attribute: false, type: Array, default: [] },
  },
})
export default class PackageList extends LitComponent {
  /**
   * Returns package card for each package
   * @return {*}
   */
  get packageListMarkup() {
    return repeat(
      this.packages,
      (package_) => package_[0],
      (package_) => {
        const isSelected = this.selected ? this.selected[0] === package_[0] : false;
        return html`
          <e-package-card .selected="${isSelected}" .package="${package_}"></e-package-card>
        `;
      }
    );
  }

  /**
   * Render the <e-package-list> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html` <div class="package-list">${this.packageListMarkup}</div> `;
  }
}

/**
 * Register the component as e-package-list.
 * Registration can be done at a later time and with a different name
 */
PackageList.register();
