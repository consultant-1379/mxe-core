import { IS_STANDALONE_STR } from '../utils/Config';
/**
 * @class PermissionService
 * @classdesc Permission service for handling user interactions and UI elements
 */
class PermissionService {
  /**
   * Checks if user has administrator role
   * @return {boolean}
   */
  static isAdministrator() {
    try {
      // Only for model-lcm
      if (localStorage.getItem(IS_STANDALONE_STR)) return true;

      const { roles } = { ...store.getState().permissions?.global };

      if (!roles) {
        throw new Error('No roles found');
      }

      return roles.includes('administrator');
    } catch (e) {
      console.error(e);
      return false;
    }
  }
}

export default PermissionService;
