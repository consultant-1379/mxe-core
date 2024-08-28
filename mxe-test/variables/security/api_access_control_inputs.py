import os

# TEST DATA DIR
test_data_dir = os.getenv("TEST_DATA_DIR", "/home/eilmnqq/robot_test_data")

# Test users
test_user_username = 'training-user-a'
test_user_password = 'training-user-a-password'

# Test roles
test_role = 'mxe_training_admin'

# Authorization policy manifests
tc1_auth_policy_manifest_file = f'{test_data_dir}/api_access_control_manifests/tc1_auth_policy.yaml'


# auth policy custom resource inputs
auth_policy_custom_resource_group="security.istio.io"
auth_policy_custom_resource_version="v1beta1"
auth_policy_custom_resource_plural_name="authorizationpolicies"
