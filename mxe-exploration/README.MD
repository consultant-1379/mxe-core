# MXE Exploration

MXE Exploration is based on [zero-to-jupyterhub-k8s](https|//github.com/jupyterhub/zero-to-jupyterhub-k8s)

## Direct 3PPs

## JupyterLab Image

3PP                     | Version
------------------------|---------
jupyterlab              | 3.6.5
jupyterhub              | 4.0.1
jupyterlab-git          | 0.41.0
nbgitpuller             | 1.1.1
pyspark                 | 3.4.1

## JupyterHub Image

3PP                     | Version
------------------------|---------
jupyterhub              | 4.0.1
jupyterhub-idle-culler  | 1.2.1
jupyterhub-kubespawner  | 6.0.0
pycurl                  | 7.45.2

## Configurable HTTP Proxy Image

3PP                     | Version
------------------------|---------
configurable-http-proxy | 4.5.5

For 3PP ugrade, update respecrive version in requirements.in (pakcage.json
for Proxy) and create a requirements.txt using ```pip-compile```.
