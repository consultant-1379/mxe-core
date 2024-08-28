const initStore = require('../../../store/index');
const actions = require('../../../store/actions');

let notificationContainer;

/**
 * Get notification html markup
 * @param title
 * @param description
 * @return {string}
 */
const getNotificationMarkup = () => `
  <div class="header">
    <img class="icon" src="/assets/icons/error-icon.svg" alt="error" />
    <div id="notification_title" class="title"/></div>
  </div>
  <div id="notification_description" class="description"></div>
  <div class="footer">
    <eui-base-v0-button class="close">Ok</eui-base-v0-button>
  </div>
`;

/**
 * Add notification
 * @param {Object} notification
 */
const addNotification = (notification) => {
  const { title, description, status } = notification;

  const notificationElement = document.createElement('div');
  notificationElement.classList.add('notification', status);
  notificationElement.innerHTML = getNotificationMarkup();
  notificationElement.querySelector('#notification_title').textContent = title;
  notificationElement.querySelector('#notification_description').textContent = description;
  notificationElement.addEventListener('click', (event) => {
    event.preventDefault();
    event.stopPropagation();

    const isCloseButtonClick = event.composedPath().find((el) => el.className === 'close');

    if (!isCloseButtonClick && notification.navigate) {
      window.EUI.Router.goto(notification.navigate);
    }

    if (isCloseButtonClick) {
      notificationContainer.classList.toggle('show');
      notificationContainer.removeChild(notificationElement);
      store.dispatch(actions.removeNotifications());
    }
  });
  notificationContainer.innerHTML = '';
  notificationContainer.appendChild(notificationElement);
  notificationContainer.classList.toggle('show');
};

/**
 * EUI sdk needed to resolve app load, because it throws a warning if not
 * @param params
 * @return {Function}
 */
const onBeforeAppLoad = (params) => async (resolve) => resolve();

/**
 * Initialize notification array on container load
 * @param params
 * @return {Function}
 */
const onBeforeContainerLoad = (params) => (resolve, reject) => {
  initStore();

  notificationContainer = document.querySelector('.notifications');

  store.subscribe(() => {
    const { notifications } = store.getState();

    if (notifications?.title) {
      addNotification(notifications);
    }
  });
  resolve();
};

module.exports = {
  onBeforeContainerLoad,
  onBeforeAppLoad,
};
