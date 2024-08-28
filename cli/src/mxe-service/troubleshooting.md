# Troubleshooting Guide

If the provided logs are not informative enough, then verbose mode can be
using the "verbose" flag. Verbose mode prints logs for the underlying
processes started during the service command as well.

mxe-service create --name=knn-model-test --models=model:1.0.0 --verbose

## Common

Errors the can arise from all subcommands

- Cluster does not exist: \<cluster\>

The provided cluster name doesn't match any of your environment in the
MXE configuration file.

- No connection to MXE cluster.

Cluster is not available or your MXE configuration file contains faulty cluster addresses.

## Create

- required flag(s) "models", "name" not set

Mandatory parameters are not set.

- Model service name can only contain lower case alphanumeric characters
 and dash (-)

Wrong model service name format used in parameter.

- Model must contain the id and version, separated by a colon: \<id\>:\<version\>

Models parameter should contain image in correct format

- Model Id can only contain lower case alphanumeric characters and dots (.)

Wrong model name format used in parameter.

- Incorrect version format.
  Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4

Use correct version format in parameters

- Invalid number of instances. It should be a positive number.

If you provide the instance parameter you should make sure that it's a
positive, round number.

- Model service \<model service name\> is already running on cluster.

There is an already running model service with the given name.
Please check running model services and try to delete it,
or try to create model service with different name.

- There is no permission to create model service \<service_name>

The user has no permission to create service without domain.
For checking and modifying access rights see
Access Control administration section of GUI guide.

- There is no permission to create model service \<service_name> in domain \<domain_name>

The user has no permission to create service in the specified domain.
Check if the domain of the service is correct.
For checking and modifying access rights see
Access Control administration section of GUI guide.

- Model with ID "\<model_id>" and version "<model_version>" does not exist,
 or there is no permission to use it

The model does not exist,
or the user has no permission to create service with the given model.
Check if the domain prefix of the model id is correct.
For checking and modifying access rights see
Access Control administration section of GUI guide.

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

- Expected model service is not visible in the list.

The user has no permission to get/list model services in the specified domain.
For checking and modifying access rights see
Access Control administration section of GUI guide.

## Modify

- Both models and instances could not be empty

At least one of the models or instances parameter should be given.

- Model service name can only contain lower case alphanumeric characters
 and dash (-)

Wrong model service name format used in parameter.

- Model must contain the id and version, separated by a colon: \<id\>:\<version\>

Models parameter should contain image in correct format

- Model Id can only contain lower case alphanumeric characters and dots (.)

Wrong model name format used in parameter.

- Incorrect version format.
  Valid format: a.b.c where a, b, and c are non-negative numbers. For example: 0.1.4

Use correct version format in parameters

- Invalid number of instances. It should be a positive number.

If you provide the instance parameter you should make sure that it's a
positive, round number.

- Model-service \<model service name\> does not exist

There is no model service running with the given name.
Check running model services.

- Model \<id\> does not have version \<version\>

Service model could not be changed No model found with given id and version.
Check onboarded models.

- Model identified by \<id\> in model-service \<model service name\>
 is already on version \<version\>

The service is already running with the given model version. No change in the service.

- Nothing to do, model-service \<model service name\> already contains model
 \<id\>:\<version\>, and already has \<instances\> instances.

The service is already running with the given model version and given instances.
No change in the service.

- There is no permission to modify model service \<service_name>
in domain \<domain_name>

The user has no permission to modify service
in the domain of the given model service.
For checking and modifying access rights see
Access Control administration section of GUI guide.

- Model with ID "\<model_id>" and version "<model_version>" does not exist,
or there is no permission to use it

The model does not exist,
or the user has no permission to modify service with the given model.
Check if the domain prefix of the model id is correct.
For checking and modifying access rights see
Access Control administration section of GUI guide.

## Delete

- required flag(s) "name" not set / flag needs an argument: --name

The name flag of the delete command (the name of the model service to be deleted)
is mandatory.

- Model service \<model service name\> is not running on cluster.

The model service you wanted to delete hasn't been created.

- There is no permission to delete model service \<service_name> in domain \<domain_name>

The user has no permission to delete service in the domain of the given model service.
For checking and modifying access rights see
Access Control administration section of GUI guide.

## Version

- Version is empty

Make sure that You use an official release of cli.
Always make sure that You use the same version as the MXE version.
