/**
 * Settings is defined as
 * `<e-settings>`
 *
 * Imperatively create application
 * @example
 * let app = new Settings();
 *
 * Declaratively create application
 * @example
 * <e-settings></e-settings>
 *
 * @extends {App}
 */
import { definition } from '@eui/component';
import { App } from '@eui/app';
import { Tab } from '@eui/layout';
import { getEventPath, loc } from 'utils/Utils';

import { html, nothing } from '@eui/lit-component';
import 'components/settings/author-list/src/AuthorList';
import AuthorService from 'services/AuthorService';
import { boundMethod } from 'autobind-decorator';
import { INVOKE_UPDATE } from 'utils/Enums';
import PermissionService from 'services/PermissionService';
import { toDashboard } from 'utils/Navigator';
import { IS_STANDALONE_STR } from 'utils/Config';
import style from './settings.css';

@definition('e-settings', {
  style,
  props: {
    authors: { attribute: false },
    showAddAuthorDialog: { attribute: false },
    authorName: { attribute: false, type: String, default: '' },
    publicKey: { attribute: false, type: String, default: '' },
  },
})
export default class Settings extends App {
  didConnect() {
    if (!PermissionService.isAdministrator() || localStorage.getItem(IS_STANDALONE_STR)) {
      toDashboard();
    }
  }

  /**
   * Returns markup for the Author editing dialog
   * @return {*}
   */
  get addAuthorDialog() {
    if (!this.showAddAuthorDialog) {
      return nothing;
    }

    return html`
      <eui-base-v0-dialog class="edit-domain-dialog" label="${loc('ADD_AUTHOR')}" no-cancel show>
        <div slot="content" style="width: 500px;">
          <eui-base-v0-text-field
            class="author-name"
            labelText="${loc('AUTHOR_NAME')}:"
            fullwidth
            placeholder="${loc('ENTER_AUTHOR')}"
            @input="${(event) => {
              const { value } = getEventPath(event);
              this.authorName = value;
            }}"
            .value="${this.authorName}"
          ></eui-base-v0-text-field>
          <eui-base-v0-textarea
            class="public-key"
            labelText="${loc('PUBLIC_KEY')}:"
            fullwidth
            placeholder="${loc('ENTER_PUBLIC_KEY')}"
            @input="${(event) => {
              const { value } = getEventPath(event);
              this.publicKey = value;
            }}"
            .value="${this.publicKey}"
            rows="10"
          ></eui-base-v0-textarea>
        </div>
        <eui-base-v0-button slot="bottom" @click=${this.toggleAddAuthorDialog}>
          ${loc('CANCEL')} </eui-base-v0-button
        ><eui-base-v0-button
          slot="bottom"
          primary
          @click=${this.addAuthor}
          ?disabled="${!this.authorName.length || !this.publicKey.length}"
        >
          ${loc('ADD_AUTHOR')}
        </eui-base-v0-button>
      </eui-base-v0-dialog>
    `;
  }

  /**
   * Toggles edit Autor dialog
   */
  @boundMethod
  toggleAddAuthorDialog() {
    this.showAddAuthorDialog = !this.showAddAuthorDialog;
    this.authorName = '';
    this.publicKey = '';
  }

  /**
   * Returns create service button if user has permission
   * @return {*}
   */
  get addAuthorButton() {
    // if (!PermissionService.canDoGlobalAction('model-services')) {
    //   return nothing;
    // }

    return html`
      <eui-base-v0-button primary @click="${this.toggleAddAuthorDialog}">
        ${loc('ADD_AUTHOR')}
      </eui-base-v0-button>
    `;
  }

  @boundMethod
  async addAuthor() {
    try {
      await AuthorService.postAuthor(this.authorName, this.publicKey);
      this.toggleAddAuthorDialog();
      this.bubble(INVOKE_UPDATE);
    } catch (err) {
      console.log(err);
    }
  }

  /**
   * Render the <e-settings> app. This function is called each time a
   * prop changes.
   */
  render() {
    return html`
      <div class="top">
        <div class="button-group">
          ${this.addAuthorButton}
        </div>
      </div>
      <eui-layout-v0-tabs>
        <eui-layout-v0-tab selected>
          <eui-v0-icon name="group"></eui-v0-icon>
          <label>${loc('AUTHORS')} </label>
        </eui-layout-v0-tab>
        <e-author-list slot="content"></e-author-list>
      </eui-layout-v0-tabs>
      ${this.addAuthorDialog}
    `;
  }
}

/**
 * Register the component as e-settings.
 * Registration can be done at a later time and with a different name
 * Uncomment the below line to register the App if used outside the container
 */
// Settings.register();
