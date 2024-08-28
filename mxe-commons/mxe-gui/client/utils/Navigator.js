/**
 * Query parameters are double encoded to enfoce
 * some of the special characters
 * ~!@#$&*()=:/,;?+'
 */

export function toDashboard() {
  window.EUI.Router.goto('/dashboard');
}

export function toModelInfo(modelId, modelVersion) {
  window.EUI.Router.goto(
    `/model-catalogue/model-info/?modelId=${encodeURIComponent(
      encodeURIComponent(modelId)
    )}&modelVersion=${encodeURIComponent(encodeURIComponent(modelVersion))}`
  );
}

export function toModelCatalogue() {
  window.EUI.Router.goto('/model-catalogue');
}

export function toModelServices() {
  window.EUI.Router.goto('/model-services');
}

export function toModelServiceDetail(serviceName) {
  window.EUI.Router.goto(
    `/model-services/model-service-detail/?serviceName=${encodeURIComponent(
      encodeURIComponent(serviceName)
    )}`
  );
}

export function toTrainingPackage() {
  window.EUI.Router.goto('/training-packages');
}

export function toTrainingPackageDetail(packageId, packageVersion) {
  window.EUI.Router.goto(
    `/training-packages/training-package-detail/?packageId=${encodeURIComponent(
      encodeURIComponent(packageId)
    )}&packageVersion=${encodeURIComponent(encodeURIComponent(packageVersion))}`
  );
}

export function toSettings() {
  window.EUI.Router.goto('/settings');
}
