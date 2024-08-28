import os

# TEST DATA DIR
test_data_dir = os.getenv("TEST_DATA_DIR", "/home/eilmnqq/robot_test_data")

# model onboarding inputs
op_model_id = 'op.model'
op_model_description = 'Image Recognition Model'
op_model_author = 'MXE'
op_model_title = 'Test Model'
op_model_version = '4.1.1'
op_model_registry_path = 'armdocker.rnd.ericsson.se/proj-mxe-models/image/img_inception3:4.1.1'

# model manifest
op_sample_model_service = f'{test_data_dir}/model_manifests/overload_protection/op_sample_model_service.yaml'
op_sample_service_name = 'op-model-service'

# envoy filter custom resource inputs
envoy_filter_custom_resource_group="networking.istio.io"
envoy_filter_custom_resource_version="v1alpha3"
envoy_filter_custom_resource_plural_name="envoyfilters"

# envoy filter manifest file
op_envoy_filter_manifest_file = f'{test_data_dir}/model_manifests/overload_protection/envoy_filter_manifest.yaml'