/**
 * TrainingPackages is defined as
 * `<e-training-packages>`
 *
 * Imperatively create application
 * @example
 * let app = new TrainingPackages();
 *
 * Declaratively create application
 * @example
 * <e-training-packages></e-training-packages>
 *
 * @extends {App}
 */
import { definition } from '@eui/component';
import { App, html } from '@eui/app';
import 'components/training-packages/package-container/src/PackageContainer';
import { boundMethod } from 'autobind-decorator';
import { DISPATCH_NOTIFICATIONS } from 'utils/Enums';
import style from './trainingPackages.css';

@definition('e-training-packages', {
  style,
  props: {
    packageId: { attribute: false, type: String },
    packageVersion: { attribute: false, type: String },
  },
})
export default class TrainingPackages extends App {
  /**
   * Dispatches notification every time it catches a request
   * from the app through DISPATCH_NOTIFICATIONS event
   * @param event
   */
  @boundMethod
  dispatchNotifications(event) {
    this.plugin('notifications', 'addNotification', {
      store: this.provider ? this.provider.store : null,
      notifications: event.detail.notifications,
      config: event.detail.config,
    });
  }

  didConnect() {
    window.addEventListener(DISPATCH_NOTIFICATIONS, this.dispatchNotifications, false);
  }

  didDisconnect() {
    window.removeEventListener(DISPATCH_NOTIFICATIONS, this.dispatchNotifications, false);
  }

  /**
   * Render the <e-training-packages> app. This function is called each time a
   * prop changes.
   */
  render() {
    return html` <e-package-container></e-package-container> `;
  }
}

/**
 * Register the component as e-training-packages.
 * Registration can be done at a later time and with a different name
 * Uncomment the below line to register the App if used outside the container
 */
// TrainingPackages.register();
