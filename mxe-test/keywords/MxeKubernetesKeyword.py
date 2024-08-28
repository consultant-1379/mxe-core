from kubernetes import config
import yaml
import logging
from retry.api import retry_call
import copy
from libraries.MxeKubernetesLibrary import MxeKubernetesLibrary 

logger = logging.getLogger(__name__)

configuration=config.load_kube_config()



class MxeKubernetesKeyword():

    def patch_prometheus_configmap(self, configmap_name, namespace, body, mode):
        """
        Patch the config map

        :param configmap_name: Name of the configmap to be patched
        :param namespace: The namespace in which resource to be patched
        :param body: Data to be patched
        """ 
        try:
            logging.info(mode)
            res = MxeKubernetesLibrary.get_config_map(self=MxeKubernetesLibrary, configmap_name = configmap_name, namespace = namespace)
            res_copy = copy.deepcopy(res)
            prom = yaml.safe_load(res.data.get("prometheus.yml")) 
            if mode == "'add'" and body not in prom["scrape_configs"]:
                    prom["scrape_configs"].append(body) 
            if mode == "'del'" and (body in prom["scrape_configs"]):
                    prom["scrape_configs"].remove(body)
            res_copy.data["prometheus.yml"] = yaml.safe_dump(prom)
            MxeKubernetesLibrary.patch_config_map(self=MxeKubernetesLibrary, configmap_name = configmap_name, namespace = namespace, body= res_copy)
        except Exception as e:
            logger.info(e)
            raise