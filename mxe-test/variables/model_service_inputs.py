import os
from variables.mxe_cluster_details import mxe_host

# TEST DATA DIR
test_data_dir = os.getenv("TEST_DATA_DIR", "/home/eilmnqq/robot_test_data")

# ----- STATELESS MODEL SERVICE INPUTS -----
# Data to invoke stateless model service
model_inception_input_file = f'{test_data_dir}/model_input/model_inception_input.json'
model_iris_input_file = f'{test_data_dir}/model_input/model_iris_input.json'
model_java_input_file = f'{test_data_dir}/model_input/model_java_h2o_input.json'
model_inception_input_data = '{"data": {"ndarray": [["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"]]}}'

model_inception_search_element = 'tusker'

model_iris_input_data = '{"data": {"ndarray": [[5.964, 4.006, 2.081, 1.031]]}}'

model_iris_search_element = '0.9548873249364059'

# single_model
#---Manual Scaling---#
tc1_cli_single_model_ms_int = f'{test_data_dir}/model_manifests/single_model/tc1_cli_single_model_ms_int.yml'
tc1_cli_model_service_name = 'tc1-cli-single-model-ms-int'

tc2_cli_single_model_ms_int_fullpath = f'{test_data_dir}/model_manifests/single_model/tc2_cli_single_model_ms_int_fullpath.yaml'
tc2_cli_model_service_name = 'tc2-cli-single-model-ms-int-fullpath'

tc3_cli_single_model_ms = f'{test_data_dir}/model_manifests/single_model/tc3_cli_single_model_ms.yaml'
tc3_cli_model_service_name = 'tc3-cli-single-model-ms'

tc4_cli_single_model_ms_fullpath = f'{test_data_dir}/model_manifests/single_model/tc4_cli_single_model_ms_fullpath.yml'
tc4_cli_model_service_name = 'tc4-cli-single-model-ms-fullpath'

tc5_gui_single_model_ms = f'{test_data_dir}/model_manifests/single_model/tc5_gui_single_model_ms.yaml'
tc5_gui_model_service_name = 'tc5-gui-single-model-ms'

tc6_gui_model_service_name = 'tc6-gui-single-model-ms'

tc13_cli_single_model_ms = f'{test_data_dir}/model_manifests/single_model/tc13_cli_single_model_ms.yaml'
tc13_cli_model_service_name = 'tc13-cli-single-model-ms'
tc13_cli_modify_scale_up_single_model_ms = f'{test_data_dir}/model_manifests/single_model/tc13_cli_modify_scale_up_single_model_ms.yaml'
tc13_cli_modify_scale_down_single_model_ms = f'{test_data_dir}/model_manifests/single_model/tc13_cli_modify_scale_down_single_model_ms.yaml'

tc14_cli_single_model_ms = f'{test_data_dir}/model_manifests/single_model/tc14_cli_single_model_ms.yaml'
tc14_cli_model_service_name = 'tc14-cli-single-model-ms'
tc14_cli_modify_single_model_ms = f'{test_data_dir}/model_manifests/single_model/tc14_cli_modify_single_model_ms.yaml'

tc15_cli_single_model_ms = f'{test_data_dir}/model_manifests/single_model/tc15_cli_single_model_ms.yaml'
tc15_cli_model_service_name = 'tc15-cli-single-model-ms'
tc15_cli_modify_single_model_ms = f'{test_data_dir}/model_manifests/single_model/tc15_cli_modify_single_model_ms.yaml'

tc22_cli_single_model = f'{test_data_dir}/model_manifests/single_model/tc22_cli_single_model.yaml'
tc22_cli_model_service_name = 'tc22-cli-single-model'
tc22_cli_single_model_negative = f'{test_data_dir}/model_manifests/single_model/tc22_cli_single_model_negative.yaml'

tc23_cli_single_model_negative = f'{test_data_dir}/model_manifests/single_model/tc23_cli_single_model_negative.yaml'
tc23_unavilable_model_name = 'unavailable.model'
tc23_unavilable_model_version = '0.0.1'

tc24_cli_single_model = f'{test_data_dir}/model_manifests/single_model/tc24_cli_single_model.yaml'
tc24_cli_model_service_name = 'tc24-cli-single-model'
tc24_cli_modify_single_model_negative = f'{test_data_dir}/model_manifests/single_model/tc24_cli_modify_single_model_negative.yaml'
tc24_unavilable_model_name = 'unavailable.model'
tc24_unavilable_model_version = '0.0.1'

tc25_cli_single_model = f'{test_data_dir}/model_manifests/single_model/tc25_cli_single_model.yaml'
tc25_cli_model_service_name = 'tc25-cli-single-model'
tc25_cli_unavailable_model_service_name = 'tc25-unavailable-single-model'
tc25_cli_modify_single_model_negative = f'{test_data_dir}/model_manifests/single_model/tc25_cli_modify_single_model_negative.yaml'

tc26_cli_single_model = f'{test_data_dir}/model_manifests/single_model/tc26_cli_single_model.yaml'
tc26_cli_model_service_name = 'tc26-cli-single-model'
tc26_cli_modify_single_model_negative = f'{test_data_dir}/model_manifests/single_model/tc26_cli_modify_single_model_negative.yaml'

tc28_cli_single_model_ms = f'{test_data_dir}/model_manifests/single_model/tc28_cli_single_model_ms.yaml'
tc28_cli_model_service_name = 'tc28-cli-single-model-ms'
tc28_cli_modify_single_model_as = f'{test_data_dir}/model_manifests/single_model/tc28_cli_modify_single_model_as.yaml'

tc29_cli_single_model_as = f'{test_data_dir}/model_manifests/single_model/tc29_cli_single_model_as.yaml'
tc29_cli_model_service_name = 'tc29-cli-single-model-as'
tc29_cli_modify_single_model_ms = f'{test_data_dir}/model_manifests/single_model/tc29_cli_modify_single_model_ms.yaml'

tc30_cli_single_model_ms = f'{test_data_dir}/model_manifests/single_model/tc30_cli_single_model_ms.yaml'
tc30_cli_model_service_name = 'tc30-cli-single-model-ms'

#---Auto Scaling---#
tc7_cli_single_model_as_cpu = f'{test_data_dir}/model_manifests/single_model/tc7_cli_single_model_as_cpu.yaml'
tc7_cli_model_service_cpu_name = 'tc7-cli-single-model-as-cpu'

tc8_cli_single_model_as_mem = f'{test_data_dir}/model_manifests/single_model/tc8_cli_single_model_as_mem.yaml'
tc8_cli_model_service_mem_name = 'tc8-cli-single-model-as-mem'

tc9_gui_single_model_as_cpu = f'{test_data_dir}/model_manifests/single_model/tc9_gui_single_model_as_cpu.yaml'
tc9_gui_model_service_cpu_name = 'tc9-gui-single-model-as-cpu'

tc10_gui_model_service_cpu_name = 'tc10-gui-single-model-as-cpu'

tc11_gui_single_model_as_mem = f'{test_data_dir}/model_manifests/single_model/tc11_gui_single_model_as_mem.yaml'
tc11_gui_model_service_mem_name = 'tc11-gui-single-model-as-mem'

tc12_gui_model_service_mem_name = 'tc12-gui-single-model-as-mem'

tc16_cli_single_model_as = f'{test_data_dir}/model_manifests/single_model/tc16_cli_single_model_as.yaml'
tc16_cli_model_service_name = 'tc16-cli-single-model-as'
tc16_cli_modify_single_model_as = f'{test_data_dir}/model_manifests/single_model/tc16_cli_modify_single_model_as.yaml'

tc17_cli_single_model_as = f'{test_data_dir}/model_manifests/single_model/tc17_cli_single_model_as.yaml'
tc17_cli_model_service_name = 'tc17-cli-single-model-as'
tc17_cli_modify_scale_up_single_model_as = f'{test_data_dir}/model_manifests/single_model/tc17_cli_modify_scale_up_single_model_as.yaml'
tc17_cli_modify_scale_down_single_model_as = f'{test_data_dir}/model_manifests/single_model/tc17_cli_modify_scale_down_single_model_as.yaml'

tc18_cli_single_model_as = f'{test_data_dir}/model_manifests/single_model/tc18_cli_single_model_as.yaml'
tc18_cli_model_service_name = 'tc18-cli-single-model-as'
tc18_cli_modify_single_model_as = f'{test_data_dir}/model_manifests/single_model/tc18_cli_modify_single_model_as.yaml'

tc19_cli_single_model_as = f'{test_data_dir}/model_manifests/single_model/tc19_cli_single_model_as.yaml'
tc19_cli_model_service_name = 'tc19-cli-single-model-as'
tc19_cli_modify_single_model_as = f'{test_data_dir}/model_manifests/single_model/tc19_cli_modify_single_model_as.yaml'

tc27_cli_single_model = f'{test_data_dir}/model_manifests/single_model/tc27_cli_single_model.yaml'

tc31_cli_single_model_ms = f'{test_data_dir}/model_manifests/single_model/tc31_cli_single_model_ms.yml'
tc31_cli_model_service_name = 'tc31-cli-single-model-ms'

tc32_cli_single_model_ms = f'{test_data_dir}/model_manifests/single_model/tc32_cli_single_model_ms.yml'
tc32_cli_model_service_name = 'tc32-cli-single-model-ms'

tc33_cli_single_model_ms = f'{test_data_dir}/model_manifests/single_model/tc33_cli_single_model_ms.yml'
tc33_cli_model_service_name = 'tc33-cli-single-model-ms'

# ab_model
#---Manual Scaling---#
tc1_cli_ab_model_ms = f'{test_data_dir}/model_manifests/ab_model/tc1_cli_ab_model_ms.yaml'
tc1_cli_ab_model_service_name = 'tc1-cli-ab-model-ms'

tc2_gui_ab_model_ms = f'{test_data_dir}/model_manifests/ab_model/tc2_gui_ab_model_ms.yaml'
tc2_gui_ab_model_service_name = 'tc2-gui-ab-model-ms'

tc3_gui_ab_model_service_name = 'tc3-gui-ab-model-ms'

tc10_cli_ab_model_ms = f'{test_data_dir}/model_manifests/ab_model/tc10_cli_ab_model_ms.yaml'
tc10_cli_ab_model_service_name = 'tc10-cli-ab-model-ms'
tc10_cli_modify_scale_up_ab_model_ms = f'{test_data_dir}/model_manifests/ab_model/tc10_cli_modify_scale_up_ab_model_ms.yaml'
tc10_cli_modify_scale_down_ab_model_ms = f'{test_data_dir}/model_manifests/ab_model/tc10_cli_modify_scale_down_ab_model_ms.yaml'

tc11_cli_ab_model_ms = f'{test_data_dir}/model_manifests/ab_model/tc11_cli_ab_model_ms.yaml'
tc11_cli_ab_model_service_name = 'tc11-cli-ab-model-ms'
tc11_cli_modify_ab_model_ms = f'{test_data_dir}/model_manifests/ab_model/tc11_cli_modify_ab_model_ms.yaml'

tc12_cli_ab_model_ms = f'{test_data_dir}/model_manifests/ab_model/tc12_cli_ab_model_ms.yaml'
tc12_cli_ab_model_service_name = 'tc12-cli-ab-model-ms'
tc12_cli_modify_ab_model_ms = f'{test_data_dir}/model_manifests/ab_model/tc12_cli_modify_ab_model_ms.yaml'

tc13_cli_ab_model_ms = f'{test_data_dir}/model_manifests/ab_model/tc13_cli_ab_model_ms.yaml'
tc13_cli_ab_model_service_name = 'tc13-cli-ab-model-ms'
tc13_cli_modify_ab_model_ms = f'{test_data_dir}/model_manifests/ab_model/tc13_cli_modify_ab_model_ms.yaml'

tc19_cli_ab_model = f'{test_data_dir}/model_manifests/ab_model/tc19_cli_ab_model.yaml'
tc19_cli_ab_model_service_name = 'tc19-cli-ab-model'
tc19_cli_modify_ab_model_negative = f'{test_data_dir}/model_manifests/ab_model/tc19_cli_modify_ab_model_negative.yaml'

tc21_cli_ab_model = f'{test_data_dir}/model_manifests/ab_model/tc21_cli_ab_model.yaml'
tc21_cli_ab_model_service_name = 'tc21-cli-ab-model'
tc21_cli_modify_ab_model_negative = f'{test_data_dir}/model_manifests/ab_model/tc21_cli_ab_model_negative.yaml'

tc22_cli_ab_model_negative = f'{test_data_dir}/model_manifests/ab_model/tc22_cli_ab_model_negative.yaml'
tc22_unavilable_model_name = 'unavailable.model'
tc22_unavilable_model_version = '0.0.1'

tc23_cli_ab_model = f'{test_data_dir}/model_manifests/ab_model/tc23_cli_ab_model.yaml'
tc23_cli_ab_model_service_name = 'tc23-cli-ab-model'
tc23_cli_modify_ab_model_negative = f'{test_data_dir}/model_manifests/ab_model/tc23_cli_modify_ab_model_negative.yaml'
tc23_unavilable_model_name = 'unavailable.model'
tc23_unavilable_model_version = '0.0.1'

tc24_cli_ab_model = f'{test_data_dir}/model_manifests/ab_model/tc24_cli_ab_model.yaml'
tc24_cli_ab_model_service_name = 'tc24-cli-ab-model'
tc24_cli_unavailable_model_service_name = 'tc24-unavailable-ab-model'
tc24_cli_modify_ab_model_negative = f'{test_data_dir}/model_manifests/ab_model/tc24_cli_modify_ab_model_negative.yaml'

tc25_cli_ab_model = f'{test_data_dir}/model_manifests/ab_model/tc25_cli_ab_model.yaml'
tc25_cli_ab_model_service_name = 'tc25-cli-ab-model'
tc25_cli_modify_ab_model_negative = f'{test_data_dir}/model_manifests/ab_model/tc25_cli_modify_ab_model_negative.yaml'

tc26_cli_ab_model_ms = f'{test_data_dir}/model_manifests/ab_model/tc26_cli_ab_model_ms.yaml'
tc26_cli_ab_model_service_name = 'tc26-cli-ab-model-ms'
tc26_cli_modify_ab_model_as = f'{test_data_dir}/model_manifests/ab_model/tc26_cli_modify_ab_model_as.yaml'

tc27_cli_ab_model_as = f'{test_data_dir}/model_manifests/ab_model/tc27_cli_ab_model_as.yaml'
tc27_cli_ab_model_service_name = 'tc27-cli-ab-model-as'
tc27_cli_modify_ab_model_ms = f'{test_data_dir}/model_manifests/ab_model/tc27_cli_modify_ab_model_ms.yaml'

#---Auto Scaling---#
tc4_cli_ab_model_as_cpu = f'{test_data_dir}/model_manifests/ab_model/tc4_cli_ab_model_as_cpu.yaml'
tc4_cli_ab_model_service_cpu_name = 'tc4-cli-ab-model-as-cpu'

tc5_cli_ab_model_as_mem = f'{test_data_dir}/model_manifests/ab_model/tc5_cli_ab_model_as_mem.yaml'
tc5_cli_ab_model_service_mem_name = 'tc5-cli-ab-model-as-mem'

tc6_gui_ab_model_as_cpu = f'{test_data_dir}/model_manifests/ab_model/tc6_gui_ab_model_as_cpu.yaml'
tc6_gui_ab_model_service_cpu_name = 'tc6-gui-ab-model-as-cpu'

tc7_gui_ab_model_service_cpu_name = 'tc7-gui-ab-model-as-cpu'

tc8_gui_ab_model_as_mem = f'{test_data_dir}/model_manifests/ab_model/tc8_gui_ab_model_as_mem.yaml'
tc8_gui_ab_model_service_mem_name = 'tc8-gui-ab-model-as-mem'

tc9_gui_ab_model_service_mem_name = 'tc9-gui-ab-model-as-mem'

tc14_cli_ab_model_as = f'{test_data_dir}/model_manifests/ab_model/tc14_cli_ab_model_as.yaml'
tc14_cli_ab_model_service_name = 'tc14-cli-ab-model-as'
tc14_cli_modify_ab_model_as = f'{test_data_dir}/model_manifests/ab_model/tc14_cli_modify_ab_model_as.yaml'

tc15_cli_ab_model_as = f'{test_data_dir}/model_manifests/ab_model/tc15_cli_ab_model_as.yaml'
tc15_cli_ab_model_service_name = 'tc15-cli-ab-model-as'
tc15_cli_modify_scale_up_ab_model_as = f'{test_data_dir}/model_manifests/ab_model/tc15_cli_modify_scale_up_ab_model_as.yaml'
tc15_cli_modify_scale_down_ab_model_as = f'{test_data_dir}/model_manifests/ab_model/tc15_cli_modify_scale_down_ab_model_as.yaml'

tc16_cli_ab_model_as = f'{test_data_dir}/model_manifests/ab_model/tc16_cli_ab_model_as.yaml'
tc16_cli_ab_model_service_name = 'tc16-cli-ab-model-as'
tc16_cli_modify_ab_model_as = f'{test_data_dir}/model_manifests/ab_model/tc16_cli_modify_ab_model_as.yaml'

tc17_cli_ab_model_as = f'{test_data_dir}/model_manifests/ab_model/tc17_cli_ab_model_as.yaml'
tc17_cli_ab_model_service_name = 'tc17-cli-ab-model-as'
tc17_cli_modify_ab_model_as = f'{test_data_dir}/model_manifests/ab_model/tc17_cli_modify_ab_model_as.yaml'

tc18_cli_ab_model_as = f'{test_data_dir}/model_manifests/ab_model/tc18_cli_ab_model_as.yaml'
tc18_cli_ab_model_service_name = 'tc18-cli-ab-model-as'
tc18_cli_modify_ab_model_as = f'{test_data_dir}/model_manifests/ab_model/tc18_cli_modify_ab_model_as.yaml'

tc20_cli_ab_model = f'{test_data_dir}/model_manifests/ab_model/tc20_cli_ab_model.yaml'
