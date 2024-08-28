import { definition } from '@eui/component';
import { Panel } from '@eui/panel';
import { html, nothing, repeat } from '@eui/lit-component';
import { preventDefaultEvent, loc } from 'utils/Utils';
import { boundMethod } from 'autobind-decorator';
import { removeNotification, removeNotifications } from 'store/actions';
import style from './notificationPanel.css';

@definition('eui-notification-panel', { style, props: { state: { attribute: false } } })
export default class NotificationPanel extends Panel {
  /**
   * @private
   * @function didConnect
   * @description Component did connect callback
   */
  didConnect() {
    this.state = store.getState();
    // eslint-disable-next-line no-return-assign
    this.unsubscribe = store.subscribe(() => {
      this.state = store.getState();
    });
  }

  didDisconnect() {
    this.unsubscribe();
  }

  /**
   * Remove notification by the index position
   * @param event - Remove event
   * @param {Number} position - Notification array position
   */
  @boundMethod
  removeNotification(event, position) {
    preventDefaultEvent(event);
    console.log(position);
    store.dispatch(removeNotification(position));
  }

  /**
   * Clear all notification
   */
  @boundMethod
  clearNotifications() {
    store.dispatch(removeNotifications());
  }

  /**
   * Navigate to the data
   * @param event - Click event
   * @param item - Notification item
   * @param i - Item position
   */
  @boundMethod
  navigate(event, item, i) {
    if (item.navigate) {
      preventDefaultEvent(event);
      this.provider.store.dispatch('TOGGLE_SYSTEM_PANEL', false);
      // this.removeNotification(event, i);
      window.EUI.Router.goto(item.navigate);
    }
  }

  get notificationCardsMarkup() {
    if (!this.state || !this.state.notifications) {
      return nothing;
    }
    return repeat(
      this.state.notifications,
      (notification) => notification.title,
      (notification, i) => html`
        <div
          class="notification-log-item ${notification.status}"
          @click="${(event) => {
            this.navigate(event, notification, i);
          }}"
        >
          <div class="title">${notification.title}</div>
          <div class="date">${notification.date.toLocaleString()}</div>
          <div class="description">${notification.description}</div>
          <eui-v0-icon
            class="close"
            name="cross-small"
            @click="${(event) => {
              this.removeNotification(event, i);
            }}"
          ></eui-v0-icon>
        </div>
      `
    );
  }

  get clearAllMarkup() {
    if (this.state.notifications.length === 0) {
      return nothing;
    }
    return html` <div class="clear" @click="${this.clearNotifications}">${loc('CLEAR_ALL')}</div> `;
  }

  render() {
    return html`
      <div class="notification-panel">
        <div class="panel-title">Notifications</div>
        ${this.clearAllMarkup} ${this.notificationCardsMarkup}
      </div>
    `;
  }
}
