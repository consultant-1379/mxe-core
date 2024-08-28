#!/usr/bin/python3

import sys
import subprocess

def do_scan(scan_config_dict,image_chunk):
    try :
        print('Starting to scan given images: \n%s'%("\n".join(image_chunk)))
        image_list = " ".join(["--image "+i for i in image_chunk])
        # print(""" grype_scan %s --report-dir %s --grype-parameters %s %s """%(image_list,scan_config_dict['report_dir'],scan_config_dict['param'],scan_config_dict['sbom_opts']))
        exe_out=subprocess.Popen(""" grype_scan %s --report-dir %s --grype-parameters %s %s """%(image_list,scan_config_dict['report_dir'],scan_config_dict['param'],scan_config_dict['sbom_opts']) , stdout=subprocess.PIPE,shell=True,universal_newlines=True)
        out=exe_out.communicate()[0],exe_out.returncode
        if out[1] != 0 :
            return False
        # print(""" grype_scan %s --report-dir %s --grype-parameters %s %s """%(image_list,scan_config_dict['report_dir'],scan_config_dict['param'],scan_config_dict['sbom_opts']))
    except Exception as e:
        print("Error %s"%(e))
        return False