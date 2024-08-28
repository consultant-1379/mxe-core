import { definition } from '@eui/component';
import { Panel } from '@eui/panel';
import { html, repeat } from '@eui/lit-component';
import '@eui/base';
import { boundMethod } from 'autobind-decorator';
import { loc } from 'utils/Utils';
import { CLEAR_FILTERS } from 'utils/Enums';

@definition('eui-menu-panel', {
  props: {
    activeApp: {
      attribute: false,
      type: Object,
    },
    apps: {
      attribute: false,
      type: Array,
      default: window.EUI.MenuConfig?.app.menu.data,
    },
  },
})
export default class LeftMenuPanel extends Panel {
  /**
   * @private
   * @function didConnect
   * @description Component did connect callback
   */

  didConnect() {
    this.renderMenu();
    this.setActiveApp();
    window.addEventListener('hashchange', this.setActiveApp, false);
  }

  /** Evaluate availability of the applications. */
  _evaluateApps(apps) {
    const newApps = [];
    apps.forEach((app) => {
      if (app.type === 'group') {
        ({ newApps: app.children } = this._evaluateApps(app.children));

        if (app.children.length === 0) app.active = false;
      }

      if (app?.active) {
        newApps.push(app);
      }
    });
    return { newApps };
  }

  @boundMethod
  renderMenu() {
    if (this.apps) {
      const data = [...this.apps];
      ({ newApps: this.apps } = this._evaluateApps(data));
    }
  }

  @boundMethod
  setActiveApp() {
    if (window.location.hash) {
      this.apps.forEach((app) => {
        if (app.children && app.children.length > 0) {
          app.children.forEach((child) => {
            if (window.location.hash.includes(child.data.path)) {
              if (child.active) {
                this.activeApp = child;
                app.open = true;
              }
            }
          });
        }
        if (app.data && window.location.hash.includes(app.data.path)) {
          if (app.active) {
            this.activeApp = app;
          } else {
            this.appSelect(this.apps[0]);
          }
        }
      });
    }
  }

  @boundMethod
  appSelect(app) {
    this.activeApp = app;
    const { path, url } = app.data;
    this.bubble(CLEAR_FILTERS);
    if (app?.type === 'external') {
      window.open(url);
    } else {
      window.EUI.Router.goto(`/${path}`);
    }
    this.dispatch('CLOSE_NAV_MENU');
  }

  didDisconnect() {
    window.removeEventListener('hashchange', this.setActiveApp, false);
  }

  render() {
    return html`
      <div class="left-menu-panel">
        <eui-base-v0-tree navigation>
          ${repeat(
            this.apps,
            (app) => app.id,
            (app) => {
              if (!app.data && app.children.length > 0) {
                if (app.open) {
                  return html`
                    <eui-base-v0-tree-item id=${app.id} class="tree-item tree--navigation" open>
                      ${loc(app.label)} ${this._renderTreeItems(app)}
                    </eui-base-v0-tree-item>
                  `;
                }
                return html`
                  <eui-base-v0-tree-item class="tree-item tree--navigation" id=${app.id}>
                    ${loc(app.label)} ${this._renderTreeItems(app)}
                  </eui-base-v0-tree-item>
                `;
              }
              return this._renderTreeItem(app);
            }
          )}
        </eui-base-v0-tree>
      </div>
    `;
  }

  _renderTreeItems(app) {
    return repeat(
      app.children,
      (children) => children.id,
      (children) => this._renderTreeItem(children)
    );
  }

  _renderTreeItem(app) {
    return html`<eui-base-v0-tree-item
      id=${app.id}
      .active="${this.activeApp?.id === app.id}"
      class="tree-item tree--navigation"
      @click="${() => this.appSelect(app)}"
    >
      ${loc(app.label)}
    </eui-base-v0-tree-item>`;
  }
}
