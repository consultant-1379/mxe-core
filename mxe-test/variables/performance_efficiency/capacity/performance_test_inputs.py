import os

# TEST DATA DIR
test_data_dir = os.getenv("TEST_DATA_DIR", "/home/eilmnqq/robot_test_data")

# model onboarding inputs
perf_model_id = 'perf.model'
perf_model_description = 'Image Recognition Model'
perf_model_author = 'MXE'
perf_model_title = 'Test Model'
perf_model_version = '4.1.1'
perf_model_registry_path = 'armdocker.rnd.ericsson.se/proj-mxe-models/image/img_inception3:4.1.1'

# model manifest
perf_sample_model_service = f'{test_data_dir}/model_manifests/performance_test/perf_sample_model_service.yaml'
perf_sample_service_name = 'perf-model-service'
perf_prometheus_yml_body = {"job_name":"kubernetes-resources-cadvisor","scheme":"https","tls_config":{"ca_file":"/var/run/secrets/kubernetes.io/serviceaccount/ca.crt"},"bearer_token_file":"/var/run/secrets/kubernetes.io/serviceaccount/token","kubernetes_sd_configs":[{"role":"node"}],"relabel_configs":[{"action":"labelmap","regex":"__meta_kubernetes_node_label_(.+)"},{"target_label":"__address__","replacement":"kubernetes.default.svc:443"},{"source_labels":["__meta_kubernetes_node_name"],"regex":"(.+)","target_label":"__metrics_path__","replacement":"/api/v1/nodes/${1}/proxy/metrics/cadvisor"}],"metric_relabel_configs":[{"source_labels":["__name__"],"regex":"(container_memory_working_set_bytes|machine_memory_bytes|container_fs_limit_bytes|container_fs_usage_bytes|machine_cpu_cores|container_cpu_usage_seconds_total)","action":"keep"}]}
perf_cluster_role_name = "mxe-prometheus-clusterrole"
perf_cluster_role_binding_name = "mxe-prometheus-clusterrole-binding"
perf_cluster_role_manifest = f'{test_data_dir}/model_manifests/performance_test/perf_cluster_role.yaml'
perf_cluster_role_binding_manifest = f'{test_data_dir}/model_manifests/performance_test/perf_cluster_role_binding.yaml'
perf_configmap_name = 'eric-mxe-prometheus-config-map'