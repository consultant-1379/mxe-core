import YAML from 'yaml';
import base64 from 'react-native-base64';

class CreateManifestService {
  /**
    getModelSerivce Create manifest and render them in a complete java
        * @returns {Promise<any>}
   */
  static async constructCreateManifest(serviceName, scalingData, selectedModels) {
    return new Promise(async (resolve, reject) => {
      try {
        const dataURLtoFile = (dataurl, filename) => {
          const arr = dataurl.split(',');
          const mime = arr[0].match(/:(.*?);/)[1];
          const bstr = atob(arr[1]);
          let n = bstr.length;
          const u8arr = new Uint8Array(n);

          while (n--) {
            u8arr[n] = bstr.charCodeAt(n);
          }
          const file = new File([u8arr], filename, { type: mime });
          return file;
        };
        const doc = new YAML.Document();
        doc.contents = await CreateManifestService.constructCreateManifestDataURI(
          serviceName,
          scalingData,
          selectedModels
        );
        const dataURI = `data:yml/plain;base64,${base64.encode(doc.toString())}`;
        const file = dataURLtoFile(dataURI, `${serviceName}.yml`);
        resolve(file);
      } catch (error) {
        reject(error);
      }
    });
  }

  static async constructCreateManifestDataURI(serviceName, scalingData, selectedModels) {
    return new Promise(async (resolve, reject) => {
      try {
        const dataURLtoFile = (dataurl, filename) => {
          const arr = dataurl.split(',');
          const mime = arr[0].match(/:(.*?);/)[1];
          const bstr = atob(arr[1]);
          let n = bstr.length;
          const u8arr = new Uint8Array(n);

          while (n--) {
            u8arr[n] = bstr.charCodeAt(n);
          }
          const file = new File([u8arr], filename, { type: mime });
          return file;
        };
        const doc = new YAML.Document();
        const metaDataName = {
          name: serviceName,
        };

        let model = null;
        let implementationName = null;
        let graphName = null;
        let endpoint = null;
        let graph = null;
        let componentSpecs = [];
        let hpaSpacdata;
        let hpaSpacdataTwo;
        if (scalingData.autoScaling) {
          let resourceName = null;
          let resourceTargetAvgValue = null;
          if (scalingData.autoScaling.metrics[0].name === 'cpuMilliCores') {
            resourceName = 'cpu';
            resourceTargetAvgValue = `${scalingData.autoScaling.metrics[0].targetAverageValue}m`;
          } else {
            resourceName = 'memory';
            resourceTargetAvgValue = `${scalingData.autoScaling.metrics[0].targetAverageValue}Mi`;
          }
          const metricesData = {
            metrics: {
              type: 'Resource',
              resource: { name: resourceName, targetAverageValue: resourceTargetAvgValue },
            },
          };
          const metricesDataTwo = {
            metrics: {
              type: 'Resource',
              resource: { name: resourceName, targetAverageValue: resourceTargetAvgValue },
            },
          };
          hpaSpacdata = {
            minReplicas: scalingData.autoScaling.minReplicas,
            maxReplicas: scalingData.autoScaling.maxReplicas,
            metrics: [{ ...metricesData.metrics }],
          };
          hpaSpacdataTwo = {
            minReplicas: scalingData.autoScaling.minReplicas,
            maxReplicas: scalingData.autoScaling.maxReplicas,
            metrics: [{ ...metricesDataTwo.metrics }],
          };
        }
        if (selectedModels.length === 2) {
          const testdata = {
            containers: {
              image: `${selectedModels[0].id}:${selectedModels[0].version}`,
              name: 'model-1',
            },
          };
          const testDataTwo = {
            containers: {
              image: `${selectedModels[1].id}:${selectedModels[1].version}`,
              name: 'model-2',
            },
          };
          const childrenOne = {
            name: 'model-1',
            endpoint: { type: 'REST' },
            type: 'MODEL',
            children: [],
          };
          const childrenTwo = {
            name: 'model-2',
            endpoint: { type: 'REST' },
            type: 'MODEL',
            children: [],
          };
          const containerdataOne = {
            containers: [{ ...testdata.containers }],
          };
          const containerdataTwo = {
            containers: [{ ...testDataTwo.containers }],
          };
          let componentSpecsOne;
          let componentSpecsTwo;
          if (hpaSpacdata) {
            componentSpecsOne = {
              spec: containerdataOne,
              hpaSpec: hpaSpacdata,
            };
            componentSpecsTwo = {
              spec: containerdataTwo,
              hpaSpec: hpaSpacdataTwo,
            };
          } else {
            componentSpecsOne = {
              spec: containerdataOne,
            };

            componentSpecsTwo = {
              spec: containerdataTwo,
            };
          }
          const parameters = {
            name: 'ratioA',
            value: String(selectedModels[0].weight),
            type: 'float',
          };
          implementationName = 'RANDOM_ABTEST';
          graphName = 'ab-test';
          endpoint = {};
          graph = {
            name: graphName,
            implementation: implementationName,
            parameters: [{ ...parameters }],
            endpoint,
            children: [childrenOne, { ...childrenTwo }],
          };
          componentSpecs = [componentSpecsOne, { ...componentSpecsTwo }];
        } else {
          const testdata = {
            containers: {
              image: `${selectedModels[0].id}:${selectedModels[0].version}`,
              name: 'model',
            },
          };
          model = [{ ...testdata.containers }];
          implementationName = 'UNKNOWN_IMPLEMENTATION';
          graphName = 'model';

          endpoint = { type: 'REST' };
          graph = {
            name: graphName,
            type: 'MODEL',
            implementation: implementationName,
            endpoint,
            children: [],
          };
          const containerdata = {
            containers: model,
          };
          if (hpaSpacdata) {
            componentSpecs = [
              {
                spec: containerdata,
                hpaSpec: hpaSpacdata,
              },
            ];
          } else {
            componentSpecs = [
              {
                spec: containerdata,
              },
            ];
          }
        }
        if (scalingData.autoScaling) {
          scalingData.replicas = scalingData.autoScaling.minReplicas;
        } else if (scalingData == null || undefined) {
          scalingData.replicas = 1;
        }
        const predctor = [
          {
            name: 'main',
            annotations: { predictor_version: 'v1' },
            replicas: scalingData.replicas,
            graph,
            componentSpecs,
          },
        ];

        const spec = { predictors: predctor };
        const jsonFile = {
          apiVersion: 'machinelearning.seldon.io/v1',
          kind: 'SeldonDeployment',
          metadata: metaDataName,
          spec,
        };
        doc.contents = jsonFile;
        resolve(doc);
      } catch (error) {
        reject(error);
      }
    });
  }
}

export default CreateManifestService;
