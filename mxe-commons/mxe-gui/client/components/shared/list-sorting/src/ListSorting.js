/**
 * Component ListSorting is defined as
 * `<e-list-sorting>`
 *
 * Imperatively create component
 * @example
 * let component = new ListSorting();
 *
 * Declaratively create component
 * @example
 * <e-list-sorting></e-list-sorting>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, repeat } from '@eui/lit-component';
import { loc } from 'utils/Utils';
import style from './listSorting.css';

/**
 * @property {Boolean} propOne - show active/inactive state.
 * @property {string} propTwo - shows the "Hello World" string.
 */
@definition('e-list-sorting', {
  style,
  home: 'list-sorting',
  props: {
    sortFn: { attribute: false },
    sortingOptions: { attribute: false, type: Array, default: [] },
    selectedOption: { attribute: false, default: {} },
  },
})
export default class ListSorting extends LitComponent {
  didChangeProps() {
    const [defaultOption] = this.sortingOptions;
    this.selectedOption = defaultOption;
  }

  /**
   * Render the <e-list-sorting> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <eui-base-v0-dropdown class="sorting" label="${this.selectedOption.name}" data-type="single">
        ${repeat(
          this.sortingOptions,
          (option) => option.name,
          (option, i) =>
            html`
              <eui-base-v0-menu-item
                class="sort-item"
                .label="${option.name}"
                tabindex="${i}"
                @click="${() => {
                  this.selectedOption = option;
                  this.sortFn(option);
                  this.executeRender();
                }}"
              >
              </eui-base-v0-menu-item>
            `
        )}
      </eui-base-v0-dropdown>
    `;
  }
}

/**
 * Register the component as e-list-sorting.
 * Registration can be done at a later time and with a different name
 */
ListSorting.register();
