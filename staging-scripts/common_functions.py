"""This module contains some common functions."""
import os
import logging
import subprocess
import sys
import yaml

LOG = logging.getLogger(__name__)
logging.basicConfig(format="%(asctime)s %(levelname)s %(message)s", level=logging.INFO)


class MyDumper(yaml.Dumper):

    def increase_indent(self, flow=False, indentless=False):
        return super(MyDumper, self).increase_indent(flow, False)


def exit_and_fail(msg):
    """This function logs the given error, and then exits with a 1 exit code."""
    if msg:
        LOG.error(msg)
    sys.exit(1)


def run_cmd(working_directory, cmd, dry_run=False):
    """This function runs the given command in the given working directory. If dry_run is set, it wont run the command."""
    if dry_run:
        LOG.info("DryRun: " + cmd)
        return ""
    else:
        LOG.info("Execute: " + cmd)

        try:
            output = subprocess.check_output(cmd, cwd=working_directory, shell=True)
        except subprocess.CalledProcessError as error:
            exit_and_fail("Command execution failed: %s. Output: %s" % (cmd, error.output))

        LOG.info("--OUT--")
        LOG.info(output)
        LOG.info("--END--")

        return output


def read_yaml(yaml_file):
    """This function reads the given yaml file and returns it as an object."""
    if not os.path.exists(yaml_file):
        exit_and_fail("Could not find %s" % yaml_file)
    with open(yaml_file, 'r') as stream:
        try:
            doc = yaml.load(stream, Loader=yaml.FullLoader)
        except yaml.YAMLError as exc:
            LOG.info(exc)
    return doc


def write_doc_to_yaml(doc, yaml_file):
    """This function writes the given yaml document to file."""
    with open(yaml_file, 'w') as stream:
        yaml.dump(doc, stream, Dumper=MyDumper, default_flow_style=False)


def step_version(version):
    """This function increments the given version."""
    taglist = version.split(".")
    if len(taglist) == 3:
        digit = int(taglist[2])
        digit = digit + 1
        new_post_fix = str(digit)
        return "%s.%s.%s" % (taglist[0], taglist[1], new_post_fix)
    else:
        exit_and_fail("Wrong version format: %s. Should be '0.0.0' format" % version)


def update_requirements(requirements, chart_name, chart_version, chart_repo):
    """This function updates the given chart name, version, repo in the requirements object."""
    found = False
    for dependency in requirements["dependencies"]:
        if dependency["name"] == chart_name:
            found = True
            dependency["version"] = chart_version
            if chart_repo:
                dependency["repository"] = chart_repo
            break
    if not found:
        exit_and_fail("Could not find " + chart_name + " in dependencies")
