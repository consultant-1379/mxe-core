import os

# TEST DATA DIR
test_data_dir = os.getenv("TEST_DATA_DIR", "/home/eilmnqq/robot_test_data")

# argo workflow custom resource inputs
argo_custom_resource_group="argoproj.io"
argo_custom_resource_version="v1alpha1"
argo_custom_resource_plural_name="workflows"

# argo workflow manifest file
tc1_argo_workflow_manifest_file = f'{test_data_dir}/argo_workflow_manifests/tc1_argo_workflow.yaml'
tc2_argo_workflow_manifest_file = f'{test_data_dir}/argo_workflow_manifests/tc2_argo_workflow_spark_operator_scala.yaml'
tc3_argo_workflow_manifest_file = f'{test_data_dir}/argo_workflow_manifests/tc3_argo_workflow_spark_operator_python.yaml'