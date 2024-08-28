# MxE GUI

## Standalone

### Helm Parameters

Following parameters must be set with pre-defined below values to enable
Standalone Deployment.

| Parameter                                    | Value      |
| -------------------------------------------- | ---------- |
| global.mxeDisableDefaultIngressControllerUse | true       |
| mode                                         | standalone |
| uiApps.training                              | false      |
| uiApps.exploration                           | false      |
| uiApps.workflow                              | false      |

It is possible to linke MxE GUI as external application in GUI Aggregator.
In order to enable GAS intgeration, set the following parameters,

| Parameter           | Value                  |
| ------------------- | ---------------------- |
| gas.enabled         | true                   |
| gas.appExternalHost | _MxE GUI Ingress Host_ |

### Package

MxE GUI is part of MxE Commons package. Follow the below steps to get
the MxE GUI package for standalone installation.

1. Download a mxe-commons chart for dev helm repo.

2. Extract the mxe-commons chart.

3. Target the helm installtion towards _eric-mxe-gui_ directory

```sh
helm upgrade --install --debug mxe-gui ./mxe-commons/eric-mxe-gui --set global.mxeDisableDefaultIngressControllerUse=true --set global.serviceMesh.enabled=false --set global.registry.url=armdocker.rnd.ericsson.se --set mode=standalone --set uiApps.training=false --set uiApps.exploration=false --set uiApps.workflow=false --set gas.enabled=true --set gas.appExternalHost=gui.vcluster2.kroto011.rnd.gic.ericsson.se --set podPriority.mxeGui.priorityClassName=""
```

### Ingress

MxE GUI Ingress must be updated to route _/model-lcm_ prefix to
_eric-aiml-model-lcm_ service.
