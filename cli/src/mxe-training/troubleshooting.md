# Troubleshooting Guide

If the provided logs are not informative enough, then verbose mode can be
turned on using the "verbose" flag. Verbose mode prints logs for the underlying
processes started during the model command as well.

mxe-training start --packageId package.id --packageVersion 1.1.1 --verbose

## Common

Errors can arise from all subcommands

- Cluster does not exist: \<cluster\>

The provided cluster name doesn't match any of your environment in the
MXE configuration file.

- No connection to MXE cluster.

Cluster is not available or your MXE configuration file contains faulty cluster addresses.

## Onboard

- required flag(s) "package" not set

Set package path

- training package Id can only contain lower case alphanumeric characters and dots

Incorrect training package id format

- training package Id/Version/Type/Title/Author/Description is missing from /MXE-META-INF/INFO

Mandatory attribute is missing from training source INFO file,
or the file does not exist

- invalid training package Version format.
  Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4

Incorrect training package version format

- invalid training package type. Valid type: Training

Type should be Training in MXE-META-INF/INFO file

- Failed to create zip archive from \<training source\>

Failed to create zip archive from model source, check directory permissions

- Failed to read file

Source archive reading failed. Please check the permissions

- Error during the onboard request!

Couldn't get a response from the MXE server, are you sure it's running?

- MXE resource conflict

The training to be onboarded was already present on the cluster.
Currently you should delete and onboard it again if you would like to replace it.

- Server responded with error status code: \<status code\>

The server responded with an unhandled status code, maybe it's not available
or an older version?

- Pip install related error messages during packaging

Please check requirements.txt in Your training source code directory

- Could not open requirements file

Please check requirements.txt in Your training source code directory

## Start

- required flag(s) "packageId", "packageVersion" not set

Package id and version should be set

- training package Id can only contain lower case alphanumeric characters and dots

Incorrect training package id format

- invalid training package Version format.
  Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4

Incorrect training package version format

- Training is already running with packageId \<packageId\> packageVersion \<packageVersion\>

There is an already running training job from the given package.
Wait until the previous job finishes

- Training package with packageId \<id\> packageVersion \<version\> not found!

There is no onboarded training package with the given id and version.
Please onboard it first

- Python related error messages during starting

Check Your training python code for errors

## List

- Failed to parse creation date received from the server!

The server sent a date format that did not meet the client side expectation.
Are you sure you're not running an older version of the MXE server?

- Request failed

The server sent a malformed response.
Are you sure you're not running an older version of the MXE server?

- Failed to decode response from server!

Erroneous response received from the server. Are you sure that the MXE server
is not an older version?

## Delete package

- Error: required flag(s) "id", "version" not set

The id and version argument of the delete command is mandatory.

- Incorrect version format.
Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4

Incorrect training package version format

- Failed to create a new HTTP request

Erroneous response received from the server. Are you sure that the MXE server
is not an older version?

- Training package \<id\> version \<version\> not found!

The training package you wanted to delete can not be found in the cluster

## Delete job

- Incorrect command usage. Valid usage:
mxe-training delete job
--packageId com.ericsson.bdgs.oss.oss.eea.aio --packageVersion 0.0.1
mxe-training delete job --id 31415926"

There are two ways to delete jobs, please check command help for more information.
Deleting job with packageId and packageVersion will delete all of the jobs pods
and stored files, if the job was started from the given id and version.
Deleting job with id will delete the job pod and stored files
of the given training job.

- Incorrect version format.
Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4

Incorrect training package version format

- No training job found with packageId \<packageId\> and packageVersion \<packageVersion\>

There is no job started from the given packageId and packageVersion,
or it is already deleted

- Training job with id \<id\> not found

There is no job with the given id, or it is already deleted

- Failed to delete stored training results of job \<id\>.
- Failed to delete training pod \<pod_name\>.
- Failed to delete training job \<job_name\>.

There was MXE cluster level fault when deleting
files, kubernetes pods, kubernetes jobs.
Please collect logs and contact the support

- Failed to create a new HTTP request

Erroneous response received from the server. Are you sure that the MXE server
is not an older version?

## Version

- Version is empty

Make sure that You use an official release of cli.
Always make sure that You use the same version as the MXE version.
