import os

# training_model_package_details
training_model1_package_id = 'sample.model1.training'
training_model1_package_version = '0.0.1'

training_model2_package_id = 'sample.model2.training'
training_model2_package_version = '0.0.1'

# TEST DATA DIR
test_data_dir = os.getenv("TEST_DATA_DIR", "/home/eilmnqq/robot_test_data")

# RESULTS DIR
training_results_directory = os.getenv("ROBOT_REPORTS_DIR", "/home/eilmnqq/robot_test_data")

# model source code data
training_source_code_archive_file = f'{test_data_dir}/training_packages/model1_training_test.zip'

# model source code path
training_source_code_filepath = f'{test_data_dir}/training_packages/tf-mnist-training-python/'