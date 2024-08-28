#!/usr/bin/python3

import anchore_scan
import argparse
import sys
from concurrent.futures import ThreadPoolExecutor,ProcessPoolExecutor,as_completed
import importlib
import trivy_scan


def initialize():
    global num_of_threads
    num_of_threads=8
def usage():
    pass
def execute_scan(scan_type, image_list,scan_config_dict):
    try:
        batch_size=int(scan_config_dict['batch_size'])
        image_chunk_list= [image_list[i:i+batch_size] for i in range(0,len(image_list),batch_size)]
        # print (image_chunk_list)
        # sys.exit(1)
        thread_executor=ProcessPoolExecutor()
        results=[]
        scan_module=importlib.import_module(scan_type+'_scan')
        for chunk in image_chunk_list:
            thread=thread_executor.submit(scan_module.do_scan,scan_config_dict,chunk)
            results.append(thread)
        ret_code=[]
        for thread in as_completed(results):
            ret_code.append(thread.result())
        if False in ret_code:
            return False
        return True
    except Exception as e :
        print(e)
        return False
def parse_config(config_file):
    try:
        # yaml_data=yaml.load(open(config_file,'r'),yaml.FullLoader)
        with open(config_file,'r') as fp:
            lines = [line.rstrip() for line in fp.readlines()]
        data={}
        for line in lines:
            line_data=line.split(':')
            data[line_data[0].rstrip(' ')] = line_data[1]
        return data
    except Exception as e:
        print(e)
        sys.exit(1)
def main():
    Usage=" "+sys.argv[0]+" -i <image_list> -c <scan config file> -t <scan type>"
    o=argparse.ArgumentParser(usage = Usage,add_help=True)
    o.add_argument('--scan_type','-t',action="store",dest="scan_type",help="Type of scan to be performed. (Anchore/Trivy)",required=True)
    o.add_argument('--image_list','-i',action="store",dest="image_list",help="List of images to be scanned",required=True)
    o.add_argument('--config_file','-c',action="store",dest="cfg_file",help="Scan Config File",required=True)
    opt=o.parse_args()
    # if None in [opt.report_path,opt.image_list,opt.cfg_file]:
    #     print("Error : Wrong arguments")
    #     print(Usage)
    #     sys.exit(1)
    initialize()
    scan_config_dict=parse_config(opt.cfg_file)
    return_value=execute_scan(opt.scan_type,opt.image_list.rstrip(',').split(','),scan_config_dict)
    print (return_value)
    if not return_value :
        print("%s Scan failed"%(opt.scan_type.title()))
if __name__ == "__main__":
    main()