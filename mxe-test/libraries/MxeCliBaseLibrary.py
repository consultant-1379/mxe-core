import subprocess
import time
import sys
import re
import os
import pathlib
import pandas as pd
from retry.api import retry_call
import logging 

logging.basicConfig(level=logging.DEBUG, format='%(asctime)s %(name)s %(levelname)s:%(message)s')
logger = logging.getLogger(__name__)

class MxeCliBaseLibrary(object):
    """
    MxeCLILibrary is a CLI testing library for Robot Framework.

    The approach taken by this library is to provide easy to access MXE CLI commands that can
    be then accessed to define highlevel keywords for tests.

    | ***** Settings *****
    | Library           <Path_To_Directory>/MxeCLILibrary.py
    """
    def execute(self, cmd):
        """
        Generic function to execute and print all the "mxe-<model/service/training>" commands
        """
        result = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        out, err = result.communicate()
        logger.info(out)
        if result.returncode != 0 : 
            # raise Exception(f"Command exited with non-zero error code: {result.returncode}")
            raise Exception(f"{out}") 
        return out

    def convert_output(self, output):
        """
        Convert the "mxe-<model/service> list" command output to perform search or filter operations
        """  
        output = output.splitlines()
        # get headers
        headers = [h for h in ','.join(re.split('  +',output[0])).split(',') if h]
        # find index of header in string to split based on space between headers, as output can also be
        # empty sometimes
        indexes = list(map(lambda s: output[0].index(s),headers))
        # make a dict for easy search of index 
        headers_dict = dict(zip(headers,indexes))
        rowList = [] # get individual row as k:v pairs
        # excluding headers and last row ( which is empty) from loop
        for i in range(1, len(output[:-1])):
            list1 = []
            for j,k in enumerate(headers):
                if j<=len(headers)-2:
                    list1.append(output[i][headers_dict[headers[j]]:headers_dict[headers[j+1]]].strip())
                else:
                    list1.append(output[i][headers_dict[headers[j]]:].strip())
            rowList.append(list1)

        final_dict = [dict(zip(headers, r)) for r in rowList]
        df = pd.DataFrame(final_dict) # convert to Dataframe to perform search or filter operations
        return df
    