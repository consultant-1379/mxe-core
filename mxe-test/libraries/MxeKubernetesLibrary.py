from kubernetes import client, config, utils
import yaml
import logging
from retry.api import retry_call

logger = logging.getLogger(__name__)

configuration=config.load_kube_config()

class MxeKubernetesLibrary():
    """
    MxeKubernetesLibrary is a kubernetes library for Robot Framework.
    This library provides highlevel keywords to perform operations using kubernetes commands.
    This is based on "Official Python client library for kubernetes".
    https://github.com/kubernetes-client/python

    | ***** Settings *****
    | Library           <Path_To_Directory>/MxeKubernetesLibrary.py
    """
    def create_custom_resource_deployment_in_namespace(self, custom_resource_group_name, custom_resource_version, custom_resource_namespace, custom_resource_plural_name, custom_resource_manifest_file):
        """
        Creates a custom resource deployment in a namespace.
        Returns create custom deployment name.
        :param custom_resource_group_name: The custom resource's group name
        :param custom_resource_version: The custom resource's version
        :param custom_resource_namespace: The namespace in which custom resource to be deployed
        :param custom_resource_plural_name: The custom resource's plural name
        :param custom_resource_manifest_file: The custom resource's manifest file to be created
        """
        api = client.CustomObjectsApi()
        with open(custom_resource_manifest_file) as f:
            manifest = yaml.safe_load(f)
        custom_resource_deployment_resource = api.create_namespaced_custom_object(group=custom_resource_group_name, version=custom_resource_version, namespace=custom_resource_namespace, plural=custom_resource_plural_name, body= manifest)
        logger.info(custom_resource_deployment_resource)
        custom_resource_deployment_name = custom_resource_deployment_resource['metadata']['name']
        logger.info(custom_resource_deployment_name)
        return custom_resource_deployment_name

    def get_custom_resource_deployment_in_namespace(self, custom_resource_group_name, custom_resource_version, custom_resource_namespace, custom_resource_plural_name, custom_resource_deployment_name):
        """
        Get a custom resource deployment matching the deployment name in a namespace.
        :param custom_resource_group_name: The custom resource's group name
        :param custom_resource_version: The custom resource's version
        :param custom_resource_namespace: The namespace in which custom resource to be deployed
        :param custom_resource_plural_name: The custom resource's plural name
        :param custom_resource_deployment_name: The name of the custom resource's deployment that was created
        """        
        api = client.CustomObjectsApi()
        custom_resource_deployment_resource = api.get_namespaced_custom_object(group=custom_resource_group_name, version=custom_resource_version, namespace=custom_resource_namespace, plural=custom_resource_plural_name, name=custom_resource_deployment_name)
        logger.info(custom_resource_deployment_resource)
        return  custom_resource_deployment_resource

    def delete_custom_resource_deployment_in_namespace(self, custom_resource_group_name, custom_resource_version, custom_resource_namespace, custom_resource_plural_name, custom_resource_deployment_name):
        """
        Delete a custom resource deployment matching the deployment name in a namespace.
        :param custom_resource_group_name: The custom resource's group name
        :param custom_resource_version: The custom resource's version
        :param custom_resource_namespace: The namespace in which custom resource to be deployed
        :param custom_resource_plural_name: The custom resource's plural name
        :param custom_resource_deployment_name: The name of the custom resource's deployment that was created
        """        
        api = client.CustomObjectsApi()
        result = api.delete_namespaced_custom_object(group=custom_resource_group_name, version=custom_resource_version, namespace=custom_resource_namespace, plural=custom_resource_plural_name, name=custom_resource_deployment_name)
        logger.info(result)

    def check_argo_workflow(self, custom_resource_group_name, custom_resource_version, custom_resource_namespace, custom_resource_plural_name, custom_resource_deployment_name):
        """
        CAUTION: This function should not be used as a direct keyword in test cases.
                 The actual function to be used as keyword is "check_argo_workflow_status"
        This function checks the status of argo workflow post submitting.
        :param custom_resource_group_name: The custom resource's group name
        :param custom_resource_version: The custom resource's version
        :param custom_resource_namespace: The namespace in which custom resource to be deployed
        :param custom_resource_plural_name: The custom resource's plural name
        :param custom_resource_deployment_name: The name of the custom resource's deployment that was created
        """   
        workflow_status = self.get_custom_resource_deployment_in_namespace(custom_resource_group_name=custom_resource_group_name, custom_resource_version=custom_resource_version, custom_resource_namespace=custom_resource_namespace, custom_resource_plural_name=custom_resource_plural_name, custom_resource_deployment_name=custom_resource_deployment_name)
        phase_of_workflow = workflow_status['status']['phase']
        logger.info(phase_of_workflow)
        if phase_of_workflow == 'Succeeded':
            logger.info(f"workflow:{custom_resource_deployment_name} is successfully completed")
        elif phase_of_workflow == 'Failed':
            raise Exception(f"workflow:{custom_resource_deployment_name} failed. 'message': {workflow_status['status']['message']}")
        else:
            raise Exception(f"workflow:{custom_resource_deployment_name} is not yet completed")

    def check_argo_workflow_status(self, custom_resource_group_name, custom_resource_version, custom_resource_namespace, custom_resource_plural_name, custom_resource_deployment_name, no_of_retry=15, delay_between_each_retry=20):
        """
        Check whether the argo workflow phase is in ‘Succeeded’ status.
        If not deleted within the defined timer (default: 5 minutes), this keyword would timeout.
        :param custom_resource_group_name: The custom resource's group name
        :param custom_resource_version: The custom resource's version
        :param custom_resource_namespace: The namespace in which custom resource to be deployed
        :param custom_resource_plural_name: The custom resource's plural name
        :param custom_resource_deployment_name: The name of the custom resource's deployment that was created
        :param no_of_retry: number of times the retry_call should execute
        :param delay_between_each_retry: the delay between each retry_call
        """  
        return retry_call(f=self.check_argo_workflow,
                         fkwargs={'custom_resource_group_name': custom_resource_group_name, 'custom_resource_version': custom_resource_version, 'custom_resource_namespace': custom_resource_namespace, 'custom_resource_plural_name': custom_resource_plural_name, 'custom_resource_deployment_name': custom_resource_deployment_name},
                         tries=no_of_retry,
                         delay=delay_between_each_retry
                         )
    
    def create_resource(self, resource_manifest_file = None, resource_namespace = None, input_data = None): 

        """
        Create any resource from manifest file
        :param resource_manifest_file: The resource manifest file path
        :param resource_namespace: The namespace in which resource to be deployed
        :param input_data: A dictionary holding valid kubernetes objects that needs to be deployed
        """ 

        try:
            k8s_client = client.api_client.ApiClient()
            if resource_manifest_file:
                res = utils.create_from_yaml(k8s_client= k8s_client, yaml_file=resource_manifest_file, namespace= resource_namespace)
                logging.info(res)
            if input_data:
                res = utils.create_from_dict(k8s_client= k8s_client, namespace= resource_namespace, data= input_data)
                logging.info(res)
        except Exception as e:
            logging.error(e) #raise exception
            raise
    
    def create_cluster_role(self, manifest_file, namespace):

        """
        Create cluster role from manifest file
        :param manifest_file: Cluster role resource manifest file 
        :param namespace: The namespace in which cluster role to be deployed
        """ 
        return self.create_resource(resource_manifest_file= manifest_file, resource_namespace=namespace)
            
    
    def create_cluster_role_binding(self, manifest_file, namespace):

        """
        Create cluster role from manifest file
        :param manifest_file: Cluster role binding resource manifest file 
        :param namespace: The namespace in which cluster role binding to be deployed
        """ 

        try:
            with open (manifest_file, "r") as f:
                yaml_data = yaml.safe_load(f)
                for i in yaml_data["subjects"]:
                    i["namespace"] = namespace
            return self.create_resource(input_data= yaml_data, resource_namespace= namespace)
            
        except Exception as e:
            logging.error(e)
            raise


    def get_cluster_role(self, cluster_role_name, namespace):
        """
        Get the details of specified cluster role
        :param cluster_role_name: Name of the cluster role, details are required
        :param namespace: The namespace in which cluster role exist

        """ 
        try:
            rbac_api = client.RbacAuthorizationV1Api()
            rbac_role = rbac_api.read_cluster_role(name= cluster_role_name, namespace= namespace)
            logging.info(rbac_role)
            return rbac_role
        except Exception as e:
            logging.error(e)
            raise

    def delete_cluster_role(self, cluster_role_name):
        """
        Delete cluster role
        :param cluster_role_name: Name of the cluster role to be deleted
        """ 
        try:
            rbac_api = client.RbacAuthorizationV1Api()
            rbac_role = rbac_api.delete_cluster_role(name= cluster_role_name)
            logging.info(rbac_role)
        except Exception as e:
            logging.error(e)
            raise

    def get_cluster_role_binding(self, cluster_role_binding_name, namespace):
        """
        Get the details of specified cluster role binding
        :param cluster_role_name: Name of the cluster role binding, details are required
        :param namespace: The namespace in which cluster role binding exist

        """ 
        try:
            rbac_api = client.RbacAuthorizationV1Api()
            rbac_role = rbac_api.read_cluster_role_binding(name= cluster_role_binding_name, namespace= namespace)
            logging.info(rbac_role)
            return rbac_role
        except Exception as e:
            logging.error(e)
            raise

    def delete_cluster_role_binding(self, cluster_role_binding_name):
        """
        Delete cluster role binding
        :param cluster_role_name: Name of the cluster role binding to be deleted
        """ 
        try:
            rbac_api = client.RbacAuthorizationV1Api()
            rbac_role = rbac_api.delete_cluster_role_binding(name= cluster_role_binding_name)
            logging.info(rbac_role)
        except Exception as e:
            logging.error(e)
            raise

    def get_config_map(self, configmap_name, namespace):
        """
        Patch the config map

        :param configmap_name: Name of the configmap, which details are required
        :param namespace: The namespace in which config map exist
        """ 
        try:
            api = client.CoreV1Api()
            resp = api.read_namespaced_config_map(name= configmap_name, namespace= namespace)
            logging.info(resp)
            return(resp)      
        except Exception as e:
            logging.error(e)
            raise

    def patch_config_map(self, configmap_name, namespace, body):
        """
        Patch the config map

        :param configmap_name: Name of the configmap to be patched
        :param namespace: The namespace in which config map exist
        """ 
        try:
            api = client.CoreV1Api()
            resp = api.patch_namespaced_config_map(name= configmap_name, namespace= namespace, body= body)
            logging.info(resp)       
        except Exception as e:
            logging.error(e)
            raise