import os

# TEST DATA DIR
test_data_dir = os.getenv("TEST_DATA_DIR", "/home/eilmnqq/robot_test_data")

# ----- MODEL INPUTS -----

# model_source_code_file_path
model1_source_code_filepath = f'{test_data_dir}/models/iris_model1/'
model2_source_code_filepath = f'{test_data_dir}/models/iris_model2/'
model3_source_code_filepath = f'{test_data_dir}/models/iris_model3/'
model4_source_code_filepath = f'{test_data_dir}/models/iris_model4/'

# model_source_code_archive_file
model1_source_code_archive_file = f'{test_data_dir}/models/iris_model1_test.zip'
model2_source_code_archive_file = f'{test_data_dir}/models/iris_model2_test.zip'

# inputs_for_onboarding_model_from_source_code
model1_id_int = 'sample.model1.int'
model1_version_int = '3.1.1'
model2_id_int = 'sample.model2.int'
model2_version_int = '3.1.2'
model3_id_int = 'sample.model3.int'
model3_version_int = '3.1.3'
model4_id_int = 'sample.model4.int'
model4_version_int = '3.1.4'

# inputs_for_onboarding_model_from_external_registry and create a model service -- to test negative scenario --
negative_model_id_cli_ext = 'test.model1.cli.ext'
negative_model_description_cli_ext = 'Image Recognition Model'
negative_model_author_cli_ext = 'MXE'
negative_model_title_cli_ext = 'Test Model'
negative_model_version_cli_ext = '4.1.6'
negative_model_registry_path_cli_ext = 'armdocker.rnd.ericsson.se/proj-mxe-models/image/img_inception3:4.1.6'

negative_random_model_registry_path_cli_ext = 'armdocker.rnd.ericsson.se/proj-mxe-models/image/img_inception3:4.1.7'

negative_test_manifest = f'{test_data_dir}/models/negative_model_test_manifest.yaml'
negative_test_service_name = 'negative-test-service'


# inputs_for_onboarding_model_from_external_registry -- single_model_deployment_inputs_for_cli_and_gui --
single_model1_id_cli_ext = 'sample.model1.cli.ext'
single_model1_description_cli_ext = 'Image Recognition Model'
single_model1_author_cli_ext = 'MXE'
single_model1_title_cli_ext = 'Test Model'
single_model1_version_cli_ext = '4.1.1'
single_model1_registry_path_cli_ext = 'armdocker.rnd.ericsson.se/proj-mxe-models/image/img_inception3:4.1.1'

single_model2_id_cli_ext = 'sample.model2.cli.ext'
single_model2_description_cli_ext = 'Image Recognition Model'
single_model2_author_cli_ext = 'MXE'
single_model2_title_cli_ext = 'Test Model'
single_model2_version_cli_ext = '4.1.2'
single_model2_registry_path_cli_ext = 'armdocker.rnd.ericsson.se/proj-mxe-models/image/img_inception3:4.1.2'

single_model3_id_cli_ext = 'sample.java.model.cli.ext'
single_model3_description_cli_ext = 'Java H2O Model'
single_model3_author_cli_ext = 'MXE'
single_model3_title_cli_ext = 'Test Model'
single_model3_version_cli_ext = '0.1.0'
single_model3_registry_path_cli_ext = 'armdocker.rnd.ericsson.se/proj-mxe-models/image/h2o-java-test-model:0.1'

single_model1_id_gui_ext = 'sample.model1.gui.ext'
single_model1_description_gui_ext = 'Image Recognition Model'
single_model1_author_gui_ext = 'MXE'
single_model1_title_gui_ext = 'Test Model'
single_model1_version_gui_ext = '4.1.1'
single_model1_registry_path_gui_ext = 'armdocker.rnd.ericsson.se/proj-mxe-models/image/img_inception3:4.1.1'

single_model2_id_gui_ext = 'sample.model2.gui.ext'
single_model2_description_gui_ext = 'Image Recognition Model'
single_model2_author_gui_ext = 'MXE'
single_model2_title_gui_ext = 'Test Model'
single_model2_version_gui_ext = '4.1.2'
single_model2_registry_path_gui_ext = 'armdocker.rnd.ericsson.se/proj-mxe-models/image/img_inception3:4.1.2'

# inputs_for_onboarding_model_from_external_registry -- ab_model_deployment_inputs_for_cli_and_gui --
ab_model1_id_cli_ext = 'sample.abmodel1.cli.ext'
ab_model1_description_cli_ext = 'Image Recognition Model'
ab_model1_author_cli_ext = 'MXE'
ab_model1_title_cli_ext = 'Test Model'
ab_model1_version_cli_ext = '4.1.3'
ab_model1_registry_path_cli_ext = 'armdocker.rnd.ericsson.se/proj-mxe-models/image/img_inception3:4.1.3'

ab_model2_id_cli_ext = 'sample.abmodel2.cli.ext'
ab_model2_description_cli_ext = 'Image Recognition Model'
ab_model2_author_cli_ext = 'MXE'
ab_model2_title_cli_ext = 'Test Model'
ab_model2_version_cli_ext = '4.1.4'
ab_model2_registry_path_cli_ext = 'armdocker.rnd.ericsson.se/proj-mxe-models/image/img_inception3:4.1.4'

ab_model3_id_cli_ext = 'sample.abmodel3.cli.ext'
ab_model3_description_cli_ext = 'Image Recognition Model'
ab_model3_author_cli_ext = 'MXE'
ab_model3_title_cli_ext = 'Test Model'
ab_model3_version_cli_ext = '4.1.5'
ab_model3_registry_path_cli_ext = 'armdocker.rnd.ericsson.se/proj-mxe-models/image/img_inception3:4.1.5'

ab_model1_id_gui_ext = 'sample.abmodel1.gui.ext'
ab_model1_description_gui_ext = 'Image Recognition Model'
ab_model1_author_gui_ext = 'MXE'
ab_model1_title_gui_ext = 'Test Model'
ab_model1_version_gui_ext = '4.1.3'
ab_model1_registry_path_gui_ext = 'armdocker.rnd.ericsson.se/proj-mxe-models/image/img_inception3:4.1.3'

ab_model2_id_gui_ext = 'sample.abmodel2.gui.ext'
ab_model2_description_gui_ext = 'Image Recognition Model'
ab_model2_author_gui_ext = 'MXE'
ab_model2_title_gui_ext = 'Test Model'
ab_model2_version_gui_ext = '4.1.4'
ab_model2_registry_path_gui_ext = 'armdocker.rnd.ericsson.se/proj-mxe-models/image/img_inception3:4.1.4'

ab_model3_id_gui_ext = 'sample.abmodel3.gui.ext'
ab_model3_description_gui_ext = 'Image Recognition Model'
ab_model3_author_gui_ext = 'MXE'
ab_model3_title_gui_ext = 'Test Model'
ab_model3_version_gui_ext = '4.1.5'
ab_model3_registry_path_gui_ext = 'armdocker.rnd.ericsson.se/proj-mxe-models/image/img_inception3:4.1.5'
