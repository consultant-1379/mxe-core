#!/usr/bin/env python
"""This module updates a given chart name, repo, version in the charts requirements.yaml."""
import sys
import argparse
import logging
import yaml
from common_functions import read_yaml, write_doc_to_yaml, update_requirements, MyDumper

LOG = logging.getLogger(__name__)
logging.basicConfig(format="%(asctime)s %(levelname)s %(message)s", level=logging.INFO)


def main():
    """This is the main function that does all of the work."""
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--appChartDir',
        help="""Full path to the application chart directory
        """,
        required=True
    )
    parser.add_argument(
        '--chartName',
        help="""The name of the updated chart.
        """,
        required=True
    )
    parser.add_argument(
        '--chartRepo',
        help="""The repo of the updated chart.
        """,
        required=True
    )
    parser.add_argument(
        '--chartVersion',
        help="""The version of the updated chart.
        """,
        required=True
    )
    parser.add_argument(
        '--dryRun',
        help="""Do a dry run of the commands
        """,
        action="store_true",
        required=False
    )
    args = parser.parse_args()

    # Initialize variables
    requirements_yaml_file = "%s/requirements.yaml" % args.appChartDir
    requirements = read_yaml(requirements_yaml_file)

    # Update requirements.yaml
    LOG.info("Updating requirements.yaml with " + args.chartName + " version " + args.chartVersion)
    update_requirements(requirements, args.chartName, args.chartVersion, args.chartRepo)
    yaml.dump(requirements, sys.stdout, Dumper=MyDumper, default_flow_style=False)
    if not args.dryRun:
        write_doc_to_yaml(requirements, requirements_yaml_file)


if __name__ == "__main__":
    main()
