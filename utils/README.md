# Commonly used Utils

## TLS CERT generation helper

### Step1: Configuration

Configure values for following keys in cert_config.json

```text
    ericsson_email_address    :       your ericsson email address
    cwd                       :       output dir where the files would be created
    namespace                 :       namespace in which {config.app_name}-tls secret would created
    endpoints                 :       array of dicts where each dict contains a domain_address and the associated appName
```

Make sure your kubecontext is set correctly to the cluster which contains {config.namespace}

### Step2: Generate csr

Execute `./create_cert.py csr`

This command runs the following steps for each endpoint in config.endpoints:

  - first creates {config.cwd}/{endpoint.app_name}.conf

  - runs openssl command to create {config.cwd}/{endpoint.app_name}.csr and {config.cwd}/{endpoint.app_name}.key

### Step3: Get certificate from ECS

a) Login into
   [ECS](https://ecs.internal.ericsson.com/CertificateServices/Index)
   and navigate to certificate request form

b) Configure the parameters:

  - Set the Alternative Contact E-mail to a group email list.

  - Set the Validity as needed.
    The certificate must be replaced before the expiry of the validity period

  - Set the Server type to Apache.

  - Select the Trust as Internal trust.

  - For Internal Trust
    set the Signing Algorithm to sha256 With RSAEncryption SHA256 Root.

  - Fill the CSR field with the contents of the .csr file generated earlier.

  - Click Validate Request to submit the form, then click Submit Request.

c) Save the certificate, and rename it as {config.app_name}.cer. Copy it to {config.cwd}

### Step 4: Generate the tls secret

Execute `./create_cert.py cert`

This internally runs the following steps for each endpoint configured in config.endpoints:

  - Removes end of file BOM characters from {endpoint.app_name}.cer
  - Adds intermediate cert
    [EGADIssuingCA3](http://pki.ericsson.se/CertData/EGADIssuingCA3.crt)
    to {endpoint.app_name}.cer
  - Creates tls secret {endpoint.app_name}-tls in kubernetes in the namespace {config.namespace}

#### Note

It is also possible to only create the tls secret
  `./create_cert.py cert --skip-steps`

This is useful when secret had been created once already
i.e. when {endpoint.app_name}.cer already has intermediate cert

## Sync Docker images from Dockerhub

### Prequisites

- Skopeo is [installed](https://github.com/containers/skopeo/blob/master/install.md)

### Step1: Add list of images to be synced

Add a new dictionary into the `syncMap` dictionary in docker_images_sync.py
with the set of images that you wish to sync.

Each entry inside the dictionary is of the form `3PP source : Arm destination`

For example to sync seldon-core-operator images tagged with version 1.5.1:

```text
    "seldon-core-operator": {
        "seldonio/seldon-core-executor:1.5.1": f"{mxeRegistry}/seldonio/seldonio/seldon-core-executor:1.5.1",
        "seldonio/seldon-core-operator:1.5.1": f"{mxeRegistry}/seldonio/seldon-core-operator:1.5.1",
        "seldonio/engine:1.5.1": f"{mxeRegistry}/seldonio/engine:1.5.1"
    }
```

A glance at syncMap hence would give an idea of all 3PP image sources,
and where they are copied to within arm

### Step2: Update list keysToSync

Only the images corresponding to the keys listed in keysToSync would be synced.
This is required because we only want to be syncing new images
(and not those which are already copied)

Considering the same example as above, so to sync seldon-core-operator images; set

```text
    keysToSync = ["seldon-core-operator"]
```

### Step3: Export Docker credentials

Using your arm access credentials,
set DOCKER_USER and DOCKER_PASSWORD environment variables.
This would be used by skopeo to push the images into arm

### Step4: Execute the script docker_images_sync.py

Execute `./docker_images_sync.py`
which behind the scenes would be issuing `skopeo copy`
commands using asyncio's `subprocess` module

For multi-arch images, images for all available arch are copied
