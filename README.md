# Machine Learning Model Execution Environment

Main repo which contains all the components and CI code.

## How to use the repo

After clone initialize the submodule as following:

```shell script
git submodule update --init --recursive
```

## Use Bob for building

Do a bob init this is just once for every new [VERSION_PREFIX](./VERSION_PREFIX).

```shell script
bob/bob init
```

### Linting and static code analyis

Do static code analysis, markdown linting etc,
this is to be run every time you make a change to the code:

```shell script
bob/bob lint
```

### Documentation generation

Building the docs, only if you change the code of the CAL store documentation:

```shell script
bob/bob generate-docs
```

### Source code compilation

Building the source code:

```shell script
bob/bob build
```

If you only want to rebuild one component then you can specify it like this:

```shell script
bob/bob build:modelservice
bob/bob build:default-backend
```

### Docker Images

Building the docker images, is only possible after the build step created the
artifacts which will be included in the image:

```shell script
bob/bob image
```

If you only want to rebuild one image then you can specify it like this:

```shell script
bob/bob image:modelservice
bob/bob image:default-backend
```
