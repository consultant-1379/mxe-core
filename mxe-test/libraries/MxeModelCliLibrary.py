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

# Resuable variable to call mxe-model command in all the defined functions
model_executable = 'mxe-model'

logger = logging.getLogger(__name__)

# Possible model status in MXE post onboarding
class ModelStatus(str, Enum):
    AVAILABLE = "available"
    PACKAGING = "packaging"

class MxeModelCliLibrary(MxeCliBaseLibrary):
    """
    MxeModelCliLibrary is a mxe-model cli testing library for Robot Framework.

    The approach taken by this library is to provide easy to access mxe-model cli commands that can
    be then accessed to define highlevel keywords for tests.

    | ***** Settings *****
    | Library           <Path_To_Directory>/MxeModelCliLibrary.py
    """
    def check_model_command_options(self):
        """
        To check whether the below commands works without any error:
        mxe-model help
        mxe-model version
        mxe-model list --help
        mxe-model list -h
        mxe-model list --verbose
        mxe-model list -v
        """
        command1 = [model_executable, "help"]
        command2 = [model_executable, "version"]
        command3 = [model_executable, "list", "--help"]
        command4 = [model_executable, "list", "-h"]
        command5 = [model_executable, "list", "--verbose"]
        command6 = [model_executable, "list", "-v"]
        commands_list = [command1, command2, command3, command4, command5, command6]
        for command in commands_list:
            logger.info(command)
            self.execute(command)

    def package_model_from_sourcecode_via_cli(self, source_code_path, private_key_file_path, public_key_file_path):
        """
        To package a model from the model source code without an MXE cluster.
        :param source_code_path: Directory containing the source code.
        :param private_key_file_path: Path to the user private key.
        :param public_key_file_path: Path to the user public key.
        """
        command = [model_executable, "package", "--source", source_code_path, "--privatekey", private_key_file_path, "--publickey", public_key_file_path]
        logger.info(command)
        self.execute(command)

    def onboard_model_from_sourcecode_via_cli(self, source_code_path):
        """
        To onboard a model from the directory containing the source code to the MXE cluster.
        :param source_code_path: Directory containing the source code.
        """
        command = [model_executable, "onboard", "--source", source_code_path]
        logger.info(command)
        self.execute(command)

    def onboard_model_from_archive_via_cli(self, archive_file_path):
        """
        To onboard a model from the package archive file to the MXE cluster.
        :param archive_file_path: Directory containing the model package archive file.
        """
        command = [model_executable, "onboard", "--archive", archive_file_path]
        logger.info(command)
        self.execute(command)

    def onboard_model_from_external_registry_via_cli(self, model_id, model_description, model_author, model_title, model_version, model_registry_path):
        """
        To onboard a model from the repository that container docker image of the model to the MXE cluster.
        :param model_id: Unique model ID
        :param model_description: Short description about the model
        :param model_author: Author who designed the model
        :param model_title: Title for the model
        :param model_version: Version of the model (x.y.z)
        :param model_repo_path: Path to the docker image of the model
        """
        command = [model_executable, "onboard", "--id", model_id, "--description", model_description, "--author", model_author, "--title", model_title, "--version", model_version, "--docker", model_registry_path]
        logger.info(command)
        self.execute(command)
    
    def delete_model_via_cli(self, model_id, model_version):
        """
        To delete an onboarded model from the MXE cluster.
        :param model_id: Model ID to be deleted and it should match the onboarded models in MXE cluster.
        :param model_version: Version of the model to be deleted
        and it should match the onboarded models in MXE cluster.
        """
        command = [model_executable, "delete", "--id", model_id, "--version", model_version]
        logger.info(command)
        self.execute(command)

    def list_model_via_cli(self):
        """
        List the models that are onboarded in MXE cluster
        """
        command = [model_executable, "list"]
        logger.info(command)
        command_output = self.execute(command).decode('ascii')
        list_of_onboarded_models = self.convert_output(command_output)
        return list_of_onboarded_models

    def check_model_availability(self, model_id, model_version):
        """
        CAUTION: This function should not be used as a direct keyword in test cases.
                 The actual function to be used as keyword is "check_model_available_status_via_cli"
        This function checks the status of model post onboarding.
        :param model_id: Model ID to be deleted and it should match the onboarded models in MXE cluster.
        :param model_version: Version of the model to be deleted
        and it should match the onboarded models in MXE cluster.
        """
        logger.info("####list of onboarded models####")
        list_of_onboarded_models = self.list_model_via_cli()
        assert list_of_onboarded_models.empty == False, f"No onboarded models found"
        # filter the model based on model_id and model_version from the list of models
        desired_model = list_of_onboarded_models[(list_of_onboarded_models['ID'] == model_id) & (list_of_onboarded_models['VERSION'] == model_version)]
        logger.info("####desired model for this test####")
        logger.info(desired_model)
        assert desired_model.empty == False, f"Desired models not found"
        status =  desired_model['STATUS'].iloc[0]
        assert status == ModelStatus.AVAILABLE, f"Model status is {status}. Need to wait until it is {ModelStatus.AVAILABLE}"
        logger.info(f"Model {model_id}:{model_version} is onboarded")
        
    def check_model_available_status_via_cli(self, model_id, model_version, no_of_retry=45, delay_between_each_retry=20):
        """
        Check whether the onboarded models are in the ‘available’ status.
        If not deleted within the defined timer (default: 15 minutes), this keyword would timeout.
        :param model_id: Model ID to be deleted and it should match the onboarded models in MXE cluster.
        :param model_version: Version of the model to be deleted
        and it should match the onboarded models in MXE cluster.
        :param no_of_retry: number of times the retry_call should execute
        :param delay_between_each_retry: the delay between each retry_call
        """  
        return retry_call(f=self.check_model_availability, 
                         fkwargs={'model_id': model_id, 'model_version': model_version},
                         tries=no_of_retry,
                         delay=delay_between_each_retry
                         )
        
    def check_if_model_is_deleted(self, model_id, model_version):
        """
        CAUTION: This function should not be used as a direct keyword in test cases.
                 The actual function to be used as keyword is "check_if_model_is_deleted_via_cli"
        This function checks the status of model post deletion.
        :param model_id: Model ID to be deleted and it should match the onboarded models in MXE cluster.
        :param model_version: Version of the model to be deleted
        and it should match the onboarded models in MXE cluster.
        """        
        prev_del_status = False 
        status = False
        iterations=3

        for i in range(0, iterations):
            logger.info("####list of onboarded models####")
            list_of_onboarded_models = self.list_model_via_cli()
            if list_of_onboarded_models.empty:
                status = True
            else:
                # filter the model based on model_id and model_version from the list of models
                desired_model = list_of_onboarded_models[(list_of_onboarded_models['ID'] == model_id) & (list_of_onboarded_models['VERSION'] == model_version)]
                logger.info("####desired model for this test. Ideally this should be empty as it is already deleted####")
                logger.info(desired_model)
                if desired_model.empty:
                    status = True
                else:
                    raise Exception(f"Model {model_id}:{model_version} is not yet deleted")
            
            if status == prev_del_status:
                return
            else:
                prev_del_status = status 
                time.sleep(10)
        
        if status != prev_del_status:
            raise Exception(f"Model {model_id}:{model_version} is not yet deleted")
       
    def check_if_model_is_deleted_via_cli(self, model_id, model_version, no_of_retry=15, delay_between_each_retry=20):
        """
        Check whether the onboarded models are deleted.
        If not deleted within the defined timer (default: 5 minutes), this keyword would timeout.
        :param model_id: Model ID to be deleted and it should match the onboarded models in MXE cluster.
        :param model_version: Version of the model to be deleted
        and it should match the onboarded models in MXE cluster.
        :param no_of_retry: number of times the retry_call should execute
        :param delay_between_each_retry: the delay between each retry_call
        """  
        return retry_call(f=self.check_if_model_is_deleted, 
                         fkwargs={'model_id': model_id, 'model_version': model_version},
                         tries=no_of_retry,
                         delay=delay_between_each_retry
                         )
