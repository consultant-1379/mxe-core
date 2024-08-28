from libraries.MxeCliBaseLibrary import MxeCliBaseLibrary
import subprocess
import time
import sys
import re
import os
import pathlib
import pandas as pd
from retry.api import retry_call
import logging 
from enum import Enum

# Resuable variable to call mxe-service command in all the defined functions
model_service_executable= 'mxe-service'

logger = logging.getLogger(__name__)

# Possible model service status in MXE post creating
class ModelServiceStatus(str, Enum):
    RUNNING = "running"
    CREATING = "creating"

class MxeModelServiceCliLibrary(MxeCliBaseLibrary):
    """
    MxeModelServiceCLILibrary is a mxe-service cli testing library for Robot Framework.

    The approach taken by this library is to provide easy to access mxe-service cli commands that can
    be then accessed to define highlevel keywords for tests.

    | ***** Settings *****
    | Library           <Path_To_Directory>/MxeModelServiceCLILibrary.py
    """
    def check_model_service_command_options(self):
        """
        To check whether the below commands works without any error:
        mxe-service help
        mxe-service version
        mxe-service list --help
        mxe-service list -h
        mxe-service list --verbose
        mxe-service list -v
        """
        command1 = [model_service_executable, "help"]
        command2 = [model_service_executable, "version"]
        command3 = [model_service_executable, "list", "--help"]
        command4 = [model_service_executable, "list", "-h"]
        command5 = [model_service_executable, "list", "--verbose"]
        command6 = [model_service_executable, "list", "-v"]
        commands_list = [command1, command2, command3, command4, command5, command6]
        for command in commands_list:
            logger.info(command)
            self.execute(command)

    def create_model_service_via_cli(self, manifest_file, domain_name=''):
        """
        To create a model service in the MXE cluster.
        :param manifest_file: The manifest file for a model deployment 
        """
        if domain_name != '':
            command = [model_service_executable, "create", "--manifest", manifest_file, "--domain", domain_name]
        else:
            command = [model_service_executable, "create", "--manifest", manifest_file]
        logger.info(command)
        self.execute(command)

    def modify_model_service_via_cli(self, service_name, manifest_file):
        """
        To start a model service in the MXE cluster.
        :param service_name: Name for the model service to be modified
        :param manifest_file: The manifest file for a model deployment 
        """
        command = [model_service_executable, "modify", "--manifest", manifest_file, "--name", service_name]
        logger.info(command)
        self.execute(command)
    
    def delete_model_service_via_cli(self, service_name):
        """
        To delete a model service from the MXE cluster.
        :param service_name: Name for the model service to be deleted
        and it should match the started model service in MXE cluster.
        """
        command = [model_service_executable, "delete", "--name", service_name]
        logger.info(command)
        self.execute(command)
    
    def list_model_service_via_cli(self):
        """
        List the model services that are started in MXE cluster
        """
        command = [model_service_executable, "list"]
        logger.info(command)
        command_output = self.execute(command).decode('ascii')
        list_of_model_services = self.convert_output(command_output)
        return list_of_model_services

    def get_desired_model_service(self, service_name):
        """
        CAUTION: This function should not be used as a direct keyword in test cases.
                 The actual function to be used as keyword is "check_if_model_service_is_running_via_cli"
        This function returns the desired model service post creation.
        :param service_name: Name of the model service to be checked
        """        
        logger.info("####list of models services####")
        list_of_model_services = self.list_model_service_via_cli()
        assert list_of_model_services.empty == False, f"No model services found"
        # filter the model service based on model service name from the list of model services
        desired_model_service = list_of_model_services[list_of_model_services['NAME'] == service_name]
        logger.info("####desired model service for this test####")
        logger.info(desired_model_service)
        assert desired_model_service.empty == False, f"Desired model service not found"
        status =  desired_model_service['STATUS'].iloc[0]
        assert status == ModelServiceStatus.RUNNING, f"Model service {service_name} is {status}"
        return desired_model_service
    
    def verify_model_service_paramaters(self, service_name, **kwargs):
        """
        CAUTION: This function should not be used as a direct keyword in test cases.
                 The actual function to be used as keyword is "check_if_model_service_is_running_via_cli"
        This function verifies all the model service paramaters of the desired model service post creation.
        :param service_name: Name of the model service to be checked
        :param kwargs: The respective column names of the "mxe-service list" output and its expected value.
                       Check the actual usage in "check_if_model_service_is_running_via_cli" function.
                       In the below example, we can check instances="1" by passing as an arugument to this function.
                       Example:
                       STARTED  NAME          DOMAIN  INSTANCES  TYPE   STATUS    USER      MODEL                        ENDPOINT
                       06:54    test-service          1          model  creating  mxe-user  sample.model3.gui.ext:4.1.3  <mxe-host>/model-endpoints/test-service 
                       This is used to verify the additional parameters as seen in the output of "mxe-service list"
        """   
        desired_model_service = self.get_desired_model_service(service_name=service_name)
        for arg in kwargs:
            modified_arg = arg 
            if arg.upper() == 'MODEL' and 'MODEL' not in  desired_model_service.columns:
                modified_arg = 'MODEL_A'   
            assert desired_model_service[modified_arg.upper()].iloc[0] == kwargs[arg] , f"Expected: {arg} is {kwargs[arg]} Actual: {modified_arg} is {desired_model_service[modified_arg.upper()].iloc[0]}"

    def check_if_model_service_is_running_via_cli(self, *positional_args, no_of_retry=30, delay_between_each_retry=20, **keyword_args):
        """
        Check whether the model services are started.
        If not started within the defined timer (default: 10 minutes), this keyword would timeout.
        :param *positional_args: Positional arguments of the function to execute in retry_call
        :param no_of_retry: number of times the retry_call should execute
        :param delay_between_each_retry: the delay between each retry_call
        :param **keyword_args: Keyword arguments of the function to execute in retry_call
        Usage:
        check if model service is running via cli    service_name="test-service"    instances="2"
        """ 
        return retry_call(self.verify_model_service_paramaters, 
                         fargs= positional_args,
                         fkwargs= keyword_args,
                         tries= no_of_retry,
                         delay= delay_between_each_retry
                         )

    def check_if_model_service_is_deleted(self, service_name):
        """
        CAUTION: This function should not be used as a direct keyword in test cases.
                 The actual function to be used as keyword is "check_if_model_service_is_deleted_via_cli"
        This function checks the status of model service post deletion.
        :param service_name: Name of the model service to be checked
        """        
        prev_del_status = False 
        status = False
        iterations=3

        for i in range(0, iterations):
            logger.info("####list of model services####")
            list_of_model_services = self.list_model_service_via_cli()
            if list_of_model_services.empty:
                status = True
            else:
                # filter the model services based on service_name
                desired_model_service = list_of_model_services[list_of_model_services['NAME'] == service_name]
                logger.info("####desired model service for this test. Ideally this should be empty as it is already deleted####")
                logger.info(desired_model_service)
                if desired_model_service.empty:
                    status = True
                else:
                    raise Exception(f"Model service {service_name} is not yet deleted")
            
            if status == prev_del_status:
                return
            else:
                prev_del_status = status 
                time.sleep(10)
        
        if status != prev_del_status:
            raise Exception(f"Model service {service_name} is not yet deleted")

    def check_if_model_service_is_deleted_via_cli(self, service_name, no_of_retry=30, delay_between_each_retry=20):
        """
        Check whether the model services are deleted.
        If not deleted within the defined timer (default: 10 minutes), this keyword would timeout.
        :param service_name: Name of the model service to be checked
        :param no_of_retry: Number of times the retry_call should execute
        :param delay_between_each_retry: The delay between each retry_call
        """  
        return retry_call(f=self.check_if_model_service_is_deleted, 
                         fkwargs={'service_name': service_name},
                         tries=no_of_retry,
                         delay=delay_between_each_retry
                         )
