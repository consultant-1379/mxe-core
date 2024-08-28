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

# Resuable variable to call mxe-training command in all the defined functions
model_training_executable = 'mxe-training'

logger = logging.getLogger(__name__)

# Possible training package status in MXE post onboarding
class TrainingPackageStatus(str, Enum):
    AVAILABLE = "available"
    PACKAGING = "packaging"

# Possible training job status in MXE post starting a training package
class TrainingJobStatus(str, Enum):
    RUNNING = "running"
    COMPLETED = "completed"

class MxeTrainingCliLibrary(MxeCliBaseLibrary):
    """
    MxeTrainingCliLibrary is a mxe-training cli testing library for Robot Framework.

    The approach taken by this library is to provide easy to access mxe-training cli commands that can
    be then accessed to define highlevel keywords for tests.

    | ***** Settings *****
    | Library           <Path_To_Directory>/MxeTrainingCliLibrary.py
    """
    def check_training_command_options(self):
        """
        To check whether the below commands works without any error:
        mxe-training help
        mxe-training version
        mxe-training list packages --help
        mxe-training list packages -h
        mxe-training list packages --verbose
        mxe-training list packages -v
        """
        command1 = [model_training_executable, "help"]
        command2 = [model_training_executable, "version"]
        command3 = [model_training_executable, "list", "packages", "--help"]
        command4 = [model_training_executable, "list", "packages", "-h"]
        command5 = [model_training_executable, "list", "packages", "--verbose"]
        command6 = [model_training_executable, "list", "packages", "-v"]
        commands_list = [command1, command2, command3, command4, command5, command6]
        for command in commands_list:
            logger.info(command)
            self.execute(command)

    def onboard_training_package_from_sourcecode_via_cli(self, training_source_code_path):
        """
        To onboard a training model package from the directory containing the training source code to the MXE cluster.
        :param training_source_code_path: Directory containing the model training source code.
        """
        command = [model_training_executable, "onboard", "--source", training_source_code_path]
        logger.info(command)
        self.execute(command)

    def list_training_packages_via_cli(self):
        """
        List the training packages that are onboarded in MXE cluster
        """
        command = [model_training_executable, "list", "packages"]
        logger.info(command)
        command_output = self.execute(command).decode('ascii')
        list_of_onboarded_training_packages = self.convert_output(command_output)
        return list_of_onboarded_training_packages

    def check_training_package_availability(self, training_package_id, training_package_version):
        """
        CAUTION: This function should not be used as a direct keyword in test cases.
                 The actual function to be used as keyword is "check_training_package_available_status_via_cli"
        This function checks the status of training package post onboarding.
        :param training_package_id: training model package identifier and it should match the ID of the onboarded training package
        :param training_package_version: training model package version and it should match the version of the onboarded training package
        """   
        logger.info("####list of onboarded training packages####")
        list_of_onboarded_training_packages = self.list_training_packages_via_cli()
        assert list_of_onboarded_training_packages.empty == False, f"no onboarded training packages found"
        # filter the training package based on training_package_id and training_package_version
        desired_training_package = list_of_onboarded_training_packages[(list_of_onboarded_training_packages['ID'] == training_package_id) & (list_of_onboarded_training_packages['VERSION'] == training_package_version)]
        logger.info("####desired training package for this test####")
        logger.info(desired_training_package)
        assert desired_training_package.empty == False, f"desired training package not found"
        status =  desired_training_package['STATUS'].iloc[0]
        assert status == TrainingPackageStatus.AVAILABLE, f"Training package status is {status}. Need to wait until it is {TrainingPackageStatus.AVAILABLE}"
        logger.info(f"Training package {training_package_id}:{training_package_version} is onboarded")                  

    def check_training_package_available_status_via_cli(self, training_package_id, training_package_version, no_of_retry=45, delay_between_each_retry=20):
        """
        Check whether the onboarded training model package are in the ‘available’ status.
        If not available within the defined timer (default: 15 minutes), this keyword would timeout.
        :param training_package_id: training model package identifier and it should match the ID of the onboarded training package
        :param training_package_version: training model package version and it should match the version of the onboarded training package
        :param no_of_retry: number of times the retry_call should execute
        :param delay_between_each_retry: the delay between each retry_call
        """  
        return retry_call(f=self.check_training_package_availability, 
                         fkwargs={'training_package_id': training_package_id, 'training_package_version': training_package_version},
                         tries=no_of_retry,
                         delay=delay_between_each_retry
                         )
    
    def check_if_training_package_is_deleted(self, training_package_id, training_package_version):
        """
        CAUTION: This function should not be used as a direct keyword in test cases.
                 The actual function to be used as keyword is "check_if_training_package_is_deleted_via_cli"
        This function checks the status of training package post deletion.
        :param training_package_id: training model package identifier and it should match the ID of the onboarded training package
        :param training_package_version: training model package version and it should match the version of the onboarded training package
        """        
        prev_del_status = False 
        status = False
        iterations=3

        for i in range(0, iterations):
            logger.info("####list of onboarded training packages####")
            list_of_onboarded_training_packages = self.list_training_packages_via_cli()
            if list_of_onboarded_training_packages.empty:
                status = True
            else:
                # filter the training package based on training_package_id and training_package_version
                desired_training_package = list_of_onboarded_training_packages[(list_of_onboarded_training_packages['ID'] == training_package_id) & (list_of_onboarded_training_packages['VERSION'] == training_package_version)]
                logger.info("####desired training package for this test. Ideally this should be empty as it is already deleted####")
                logger.info(desired_training_package)
                if desired_training_package.empty:
                    status = True
                else:
                    raise Exception(f"Training package {training_package_id}:{training_package_version} is not yet deleted")
            
            if status == prev_del_status:
                return
            else:
                prev_del_status = status 
                time.sleep(10)
        
        if status != prev_del_status:
            raise Exception(f"Training package {training_package_id}:{training_package_version} is not yet deleted")
    
    def check_if_training_package_is_deleted_via_cli(self, training_package_id, training_package_version, no_of_retry=15, delay_between_each_retry=20):
        """
        Check whether the training package is deleted.
        If not deleted within the defined timer (default: 5 minutes), this keyword would timeout.
        :param training_package_id: training model package identifier and it should match the ID of the onboarded training package
        :param training_package_version: training model package version and it should match the version of the onboarded training package
        :param no_of_retry: number of times the retry_call should execute
        :param delay_between_each_retry: the delay between each retry_call
        """  
        return retry_call(f=self.check_if_training_package_is_deleted, 
                         fkwargs={'training_package_id': training_package_id, 'training_package_version': training_package_version},
                         tries=no_of_retry,
                         delay=delay_between_each_retry
                         )

    def start_training_job_via_cli(self, training_package_id, training_package_version):
        """
        To start a training job to execute the onboarded training model package.
        :param training_package_id: training model package identifier and it should match the ID of the onboarded training package
        :param training_package_version: training model package version and it should match the version of the onboarded training package
        """
        command = [model_training_executable, "start", "--packageId", training_package_id, "--packageVersion", training_package_version]
        logger.info(command)
        self.execute(command)

    def list_training_jobs_via_cli(self):
        """
        List the training jobs
        """
        command = [model_training_executable, "list", "jobs"]
        logger.info(command)
        command_output = self.execute(command).decode('ascii')
        list_of_training_jobs = self.convert_output(command_output)
        return list_of_training_jobs

    def check_training_job_completion(self, training_package_id, training_package_version):
        """
        CAUTION: This function should not be used as a direct keyword in test cases.
                 The actual function to be used as keyword is "check_if_training_job_is_completed_via_cli"
        This function checks the status of training job post starting.
        :param training_package_id: training model package identifier and it should match the ID of the onboarded training package
        :param training_package_version: training model package version and it should match the version of the onboarded training package
        """
        logger.info("####list of training jobs####")
        list_of_training_jobs = self.list_training_jobs_via_cli()
        assert list_of_training_jobs.empty == False, f"no training jobs found"
        # filter the training jobs based on training_package_id and training_package_version
        desired_training_job = list_of_training_jobs[(list_of_training_jobs['PACKAGE ID'] == training_package_id) & (list_of_training_jobs['PACKAGE VERSION'] == training_package_version)]
        logger.info("####desired training job for this test####")
        logger.info(desired_training_job)
        assert desired_training_job.empty == False, f"desired training job not found"
        status =  desired_training_job['STATUS'].iloc[0]
        assert status == TrainingJobStatus.COMPLETED, f"Training job status is {status}. Need to wait until it is {TrainingJobStatus.COMPLETED}"
        logger.info(f"Training job is completed")                          

    def check_if_training_job_is_completed_via_cli(self, training_package_id, training_package_version, no_of_retry=15, delay_between_each_retry=20):
        """
        Check whether the started training job is in the ‘completed’ status.
        If not completed within the defined timer (default: 5 minutes), this keyword would timeout.
        :param training_package_id: training model package identifier and it should match the ID of the onboarded training package
        :param training_package_version: training model package version and it should match the version of the onboarded training package
        :param no_of_retry: number of times the retry_call should execute
        :param delay_between_each_retry: the delay between each retry_call
        """ 
        return retry_call(f=self.check_training_job_completion, 
                         fkwargs={'training_package_id': training_package_id, 'training_package_version': training_package_version},
                         tries=no_of_retry,
                         delay=delay_between_each_retry
                         )

    def check_if_training_job_is_deleted(self, training_package_id, training_package_version):
        """
        CAUTION: This function should not be used as a direct keyword in test cases.
                 The actual function to be used as keyword is "check_if_training_job_is_deleted_via_cli"
        This function checks the status of training job post deletion.
        :param training_package_id: training model package identifier and it should match the ID of the onboarded training package
        :param training_package_version: training model package version and it should match the version of the onboarded training package
        """        
        prev_del_status = False 
        status = False
        iterations=3

        for i in range(0, iterations):
            logger.info("####list of training jobs####")
            list_of_training_jobs = self.list_training_jobs_via_cli()
            if list_of_training_jobs.empty:
                status = True
            else:
                # filter the training package based on training_package_id and training_package_version
                desired_training_job = list_of_training_jobs[(list_of_training_jobs['PACKAGE ID'] == training_package_id) & (list_of_training_jobs['PACKAGE VERSION'] == training_package_version)]
                logger.info("####desired training job for this test. Ideally this should be empty as it is already deleted####")
                logger.info(desired_training_job)
                if desired_training_job.empty:
                    status = True
                else:
                    raise Exception(f"Training job is not yet deleted")
            
            if status == prev_del_status:
                return
            else:
                prev_del_status = status 
                time.sleep(10)
        
        if status != prev_del_status:
            raise Exception(f"Training job is not yet deleted")
    
    def check_if_training_job_is_deleted_via_cli(self, training_package_id, training_package_version, no_of_retry=15, delay_between_each_retry=20):
        """
        Check whether the training job is deleted.
        If not deleted within the defined timer (default: 5 minutes), this keyword would timeout.
        :param training_package_id: training model package identifier and it should match the ID of the onboarded training package
        :param training_package_version: training model package version and it should match the version of the onboarded training package
        :param no_of_retry: number of times the retry_call should execute
        :param delay_between_each_retry: the delay between each retry_call
        """  
        return retry_call(f=self.check_if_training_job_is_deleted, 
                         fkwargs={'training_package_id': training_package_id, 'training_package_version': training_package_version},
                         tries=no_of_retry,
                         delay=delay_between_each_retry
                         )

    def delete_training_job_via_cli(self, training_package_id, training_package_version):
        """
        To delete the training job.
        :param training_package_id: training model package identifier and it should match the ID of the onboarded training package
        :param training_package_version: training model package version and it should match the version of the onboarded training package
        """
        command = [model_training_executable, "delete", "job", "--packageId", training_package_id, "--packageVersion", training_package_version]
        logger.info(command)
        self.execute(command)
    
    def delete_training_package_via_cli(self, training_package_id, training_package_version):
        """
        To delete an onboarded training model package from the MXE cluster.
        :param training_package_id: training model package identifier and it should match the ID of the onboarded training package
        :param training_package_version: training model package version and it should match the version of the onboarded training package
        """
        command = [model_training_executable, "delete", "package", "--id", training_package_id, "--version", training_package_version]
        logger.info(command)
        self.execute(command)

    def fetch_the_training_job_id_via_cli(self, training_package_id, training_package_version):
        """
        Fetch the training job id of a completed training job.
        :param training_package_id: training model package identifier and it should match the ID of the onboarded training package
        :param training_package_version: training model package version and it should match the version of the onboarded training package
        """        
        logger.info("####list of training jobs####")
        list_of_training_jobs = self.list_training_jobs_via_cli()
        desired_training_job = list_of_training_jobs[(list_of_training_jobs['PACKAGE ID'] == training_package_id) & (list_of_training_jobs['PACKAGE VERSION'] == training_package_version)]
        logger.info("####desired training job for this test####")
        logger.info(desired_training_job)
        assert desired_training_job.empty == False, f"no training jobs found"
        desired_training_job_id = desired_training_job['ID'].iloc[0]
        logger.info(f"training_job_id = {desired_training_job_id}")
        return  desired_training_job_id

    def download_training_results_via_cli(self, training_job_id, output_directory):
        """
        To download the training result post training job execution.
        :param job_id: Job identifier. It is created during the excution of onboarded training package
        :param output_directory: Target directory where the results should be saved.
        """
        # training_job_id = self.fetch_the_training_job_id_via_cli(training_package_id, training_package_version)
        # logger.info(f"training_job_id = {training_job_id}")
        command = [model_training_executable, "download-results", "--jobId", training_job_id, "--toDir", output_directory]
        logger.info(command)
        self.execute(command)
        download_training_result_file = os.path.join(output_directory, 'training-job-result-' + training_job_id + '.zip')
        logger.info(f"training_result_file = {download_training_result_file}")
        assert os.path.isfile(download_training_result_file) and os.access(download_training_result_file, os.R_OK), f"either the training result file \"{download_training_result_file}\" is missing or not readable"
        logger.info(f"training result file \"{download_training_result_file}\" exists and is readable")
