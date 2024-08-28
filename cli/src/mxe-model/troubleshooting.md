# Troubleshooting Guide

If the provided logs are not informative enough, then verbose mode can be
using the "verbose" flag. Verbose mode prints logs for the underlying
processes started during the model command as well.

mxe-model-package --name=knn-model-test --version=1.0.0 --source=C:\models\knn --verbose

## Common

Errors the can arise from all subcommands

- Cluster does not exist: \<cluster>

The provided cluster name doesn't match any of your environment in the
MXE configuration file.

- No connection to MXE cluster.

Cluster is not available or your MXE configuration file contains faulty cluster addresses.

## Onboard

- Both docker and source flag is set, only one of them can be used

Use docker or source onboarding

- At least one of the the following flags should be set: source, docker

Use docker or source onboarding

- model Id is missing from \<model source>/MXE-META-INF/INFO

There is no Id attribute in model source INFO file, or the file does not exist

- model Version is missing from \<model source>/MXE-META-INF/INFO

There is no Version attribute in model source INFO file, or the file does not exist

- Failed to create zip archive from \<model source>

Failed to create zip archive from model source, check directory permissions

- required flag(s) "id", "title", "version" not set

Mandatory command parameters of docker onbording are missing

- Error: flag needs an argument --id, --title, --version

Mandatory command parameters of docker onbording are missing

- Model ID must be at most 32 characters length.

Check the model id length in the command parameters

- Description must be at most 120 characters length.

Check the model id length in the command parameters

- Incorrect version format.
  Valid format: a.b.c where a, b and c are non-negative numbers. For example: 0.1.4

Use correct version format in parameters

- The id \<id> shall only contain lower case alphanumeric characters, and dots (.).

Check the model id in the command parameters

- Error during the onboard request!

Couldn't get a response from the MXE server, are you sure it's running?

- MXE resource conflict

The model to be installed was already present on the cluster.
Currently you should delete and onboard it again if you would like to replace it.

- Server responded with error status code: \<status code>

The server responded with an unhandled status code, maybe it's not available
or an older version?

- There is no permission to onboard model \<model_id>

The user has no permission to onboard model in the specified domain.
Check if the domain prefix in the model id is correct.
For checking and modifying access rights see
Access Control administration section of GUI guide.

## Start

- Model Id can only contain lower case alphanumeric characters and dots (.)

Wrong model name format used in parameter.

- Incorrect version format.
  Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4

Use correct version format in parameters

- Invalid number of instances. It should be a positive number.

If you provide the instance parameter you should make sure that it's a
positive, round number.

- Model service name \<model service name> is invalid.
  Should be up to maximum length of 253 characters and consist of
  lower case alphanumeric characters and - (dash), and must start
  and end with an alphanumeric character (e.g. 'aio-v0-0-1',
  or '123-aio-v0-0-1', regex used for validation is ^[a-z0-9]+(-[a-z0-9]+)*$.)

Use a valid model service name.

- Model service \<model service name> is already running with model
  \<model name> version \<model version> on cluster.

There is an already running instance from the given name and/or given version.
Please check running model instances and try to stop it,
or try to start an other model.

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

- Expected model is not visible in the list.

The user has no permission to get/list models in the specified domain.
For checking and modifying access rights see
Access Control administration section of GUI guide.

## Package

- Source code is not recognizable.

The model source code did not meet all the requirements detailed in the
documentation.

- required flag(s) "source" not set / flag needs an argument: --source

The source parameter must be provided to run this command.

- open .\<source code directory>/MXE-META-INF/INFO: no such file or directory

No MXE-META-INF/INFO in the model source directory.
Create it with the following mandatory attributes: Id, Version

- model Id is missing from \<source code directory>/MXE-META-INF/INFO

Add Id attribute to MXE-META-INF/INFO file

- model Id can only contain lower case alphanumeric characters and dots (.)

Use a valid model id in MXE-META-INF/INFO.

- model Version is missing from \<source code directory>/MXE-META-INF/INFO

Add Version attribute to MXE-META-INF/INFO file

- invalid model Version format.
  Valid format: a.b.c where a, b and c are non-negative numbers. For example: 0.1.4

Use correct version format in MXE-META-INF/INFO file

- open \<source code directory>/.s2i/environment: no such file or directory

s2i configuration seems to be missing, please check your model source.

- MODEL_NAME is missing from \<source code directory>/.s2i/environment

The entrypoint of your model should be filled in correctly.

- no supported source file with name \<model name> can be found
  in \<source code directory>

Source code directory does not contain supported source file.
The entrypoint of your model should be filled in correctly.
There should be supported source code file in Your source code directory.

- Model deployment of a newly packaged python model fails with python exceptions

Check python version (version 3 required) an python dependencies in Your environment

## Delete

- Error: required flag(s) "id", "version" not set

The id and version argument of the delete command is mandatory.

- Error: flag needs an argument --id, --version

The id and version argument of the delete command is mandatory.

- Failed to create a new HTTP request

Erroneous response received from the server. Are you sure that the MXE server
is not an older version?

- Model \<id\> version \<version\> not found!

The model you wanted to delete can not be found in the cluster

- There is no permission to delete model \<model_id>

The user has no permission to delete model in the specified domain.
For checking and modifying access rights see
Access Control administration section of GUI guide.

## Version

- Version is empty

Make sure that You use an official release of cli.
Always make sure that You use the same version as the MXE version.
