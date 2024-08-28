from keycloak import KeycloakAdmin
import variables.mxe_cluster_details as cluster_details
import logging

logger = logging.getLogger(__name__)

class MxeKeycloakLibrary():
    """
    MxeKeycloakLibrary is a keycloak library for Robot Framework.
    This is used to connect to keycloak in mxe to perform basic operations like create user, roles and map them both.
    It can be then accessed to define highlevel keywords for tests.

    | ***** Settings *****
    | Library           <Path_To_Directory>/MxeKeycloakLibrary.py
    """
    def get_keycloak_admin_token(self):
        """
        To get the token from keycloak. It is valid for 60 seconds.
        """
        keycloak_admin = KeycloakAdmin(server_url=cluster_details.keycloak_url, username=cluster_details.keycloak_username, password=cluster_details.keycloak_password, realm_name='mxe', user_realm_name="master", client_id='admin-cli', verify=True)
        return keycloak_admin
    
    def create_new_user_in_keycloak(self, username, password):
        """
        To create a new user in keycloak to access mxe.
        :param username: username to access mxe
        :param password: username to access mxe
        """
        keycloak_admin = self.get_keycloak_admin_token()
        keycloak_admin.create_user({"username": username, "enabled": True, "credentials": [{"value": password,"type": "password","temporary": "False"}]})
    
    def create_new_role_in_keycloak(self, rolename, domain_name=None, access_rights=None):
        """
        To create a new role in keycloak that defines the permission to access mxe.
        :param rolename: name of the role
        :param domain_name: domain name for the respective role that enables a virtual space for respective user mapped with this role
        :param access_rights: permissions to allow in model and model-services
        """
        keycloak_admin = self.get_keycloak_admin_token()
        if domain_name == None:
            keycloak_admin.create_realm_role(payload={'name': rolename, 'description': 'example role'})
        else:
            keycloak_admin.create_realm_role(payload={ "name": rolename})
            keycloak_admin.update_realm_role(role_name=rolename, payload={'attributes': {domain_name: [access_rights]}, 'name': rolename, 'composite': False, 'clientRole': False, "description": "example role"})
        

    def assign_role_with_user(self, username, rolename):
        """
        To map a user and role in keycloak.
        :param username: name of the user created in keycloak
        :param rolename: name of the role created in keycloak
        """
        keycloak_admin = self.get_keycloak_admin_token()
        user_id = keycloak_admin.get_user_id(username)
        role_information = keycloak_admin.get_realm_role(role_name=rolename)
        role_id = role_information['id']
        keycloak_admin.assign_realm_roles(user_id=user_id, client_id='', roles=[{'id': role_id, 'name': rolename}])
    
    def delete_user_in_keycloak(self, username):
        """
        To delete a user in keycloak.
        :param username: name of the user created in keycloak
        """
        keycloak_admin = self.get_keycloak_admin_token()
        user_id = keycloak_admin.get_user_id(username)
        keycloak_admin.delete_user(user_id=user_id)

    def delete_role_in_keycloak(self, rolename):
        """
        To delete a role in keycloak.
        :param rolename: name of the role created in keycloak
        """
        keycloak_admin = self.get_keycloak_admin_token()
        keycloak_admin.delete_realm_role(role_name=rolename)                

    def update_access_token_lifespan_in_keycloak(self, lifespan):
        """
        To update the access token lifespan from default 5 minutes.
        :param lifespan: lifespan of access token in (seconds)
        """
        keycloak_admin = self.get_keycloak_admin_token()
        keycloak_admin.update_realm(realm_name='mxe', payload={"accessTokenLifespan": lifespan})
    