/**
 * Component ModelList is defined as
 * `<e-model-list>`
 *
 * Imperatively create component
 * @example
 * let component = new ModelList();
 *
 * Declaratively create component
 * @example
 * <e-model-list></e-model-list>
 *
 * @extends {LitComponent}
 */
import { definition } from '@eui/component';
import { LitComponent, html, repeat } from '@eui/lit-component';
import 'components/model-list/model-card/src/ModelCard';
import style from './modelList.css';

/**
 * @property {Array} models - Model list array
 * @property {Object} selected - Selected model
 */
@definition('e-model-list', {
  style,
  home: 'model-list',
  props: {
    models: { attribute: false, type: Array, default: [] },
    selected: { attribute: false, type: Object, default: {} },
  },
})
export default class ModelList extends LitComponent {
  /**
   * Get model list html markup
   * @return {*}
   */
  get modelListMarkup() {
    return repeat(
      this.models,
      (model) => model[0],
      (model) => {
        const isSelected = this.selected ? this.selected[0] === model[0] : false;
        return html` <e-model-card .selected="${isSelected}" .model="${model}"></e-model-card> `;
      }
    );

    // return repeat(
    //   this.models,
    //   (model) => model[0],
    //   (model) => {
    //     const [id, models] = model;
    //     const isSelected = this.selected ? this.selected[0] === model[0] : false;
    //     const isStacked = models.length > 1;
    //     const modelToBeShown = isStacked ? models[models.length - 1] : models[0];
    //
    //     return html`
    //       <e-model-card
    //         class="model-card"
    //         .stacked="${isStacked}"
    //         .model="${modelToBeShown}"
    //         .selected="${isSelected}"
    //       ></e-model-card>
    //     `;
    //   }
    // );
  }

  /**
   * Render the <e-model-list> component. This function is called each time a
   * prop changes.
   */
  render() {
    return html` <div class="model-list">${this.modelListMarkup}</div> `;
  }
}

/**
 * Register the component as e-model-list.
 * Registration can be done at a later time and with a different name
 */
ModelList.register();
