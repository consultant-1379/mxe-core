#!/usr/bin/python3

import os
import subprocess

def do_scan(scan_config_dict,image):
    try :
        print("Starting trivy scan for images %s"%image[0])
        image_name = os.path.basename(image[0])
        print (""" /entrypoint.sh -f json -o  %s/%s.json %s; """%(scan_config_dict['report_dir'],image_name,image[0]))
        exe_out=subprocess.Popen(""" /entrypoint.sh -f json -o  %s/%s.json %s; """%(scan_config_dict['report_dir'],image_name,image[0]) , stdout=subprocess.PIPE,shell=True,universal_newlines=True)
        out=exe_out.communicate()[0],exe_out.returncode
        if out[1] != 0 :
            return False
    except Exception as e:
        print("Error %s"%(e))
        return False