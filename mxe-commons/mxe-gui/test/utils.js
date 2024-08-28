import locales from '../client/locale/en-us';

export function stubLocales() {
  window.EUI = { Localizer: { loc: locales } };
}

/**
 * Listen for the first of a certain type of event.
 *
 * @param {HTMLElement}   el
 * @param {string}        type
 * @param {Function|void} afterListener Some code to run once the listener is
 *                                      in place.
 */
export async function firstEvent(el, type, afterListener) {
  return new Promise((resolve) => {
    const handler = (e) => {
      el.removeEventListener(type, handler);
      resolve(e);
    };
    el.addEventListener(type, handler);

    afterListener();
  });
}

export function inShadow(el, selector) {
  return el.shadowRoot.querySelector(selector);
}

export function allInShadow(el, selector) {
  return el.shadowRoot.querySelectorAll(selector);
}

export async function nextTick() {
  return new Promise((resolve) => {
    setTimeout(resolve, 0);
  });
}

/**
 * Inject HTML markup into the DOM, and return a reference to the element.
 */
export async function injectHTMLElement(container, markup) {
  container.innerHTML = markup;

  // Pick out the top-most element.  I.e. the reference this returns won't be
  // useful if it's inside other markup.
  const el = container.children[0];
  await nextTick();
  return el;
}

export function simulateEvent(el, name) {
  el.dispatchEvent(
    new CustomEvent(name, {
      bubbles: true,
      cancelable: true,
    })
  );
}
