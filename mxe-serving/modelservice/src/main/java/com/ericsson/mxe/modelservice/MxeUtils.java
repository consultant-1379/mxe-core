package com.ericsson.mxe.modelservice;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bouncycastle.est.ESTAuth;
import org.springframework.stereotype.Service;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentType;
import com.ericsson.mxe.modelservice.dto.MxeModelDetails;
import com.ericsson.mxe.modelservice.dto.request.CreateModelDeploymentRequest;
import com.ericsson.mxe.modelservice.dto.request.PatchModelDeploymentRequest;
import com.ericsson.mxe.modelservice.dto.response.GetModelDeploymentResponse;
import com.ericsson.mxe.modelservice.modelcatalog.ModelCatalogResolver;
import com.ericsson.mxe.modelservice.modelcatalog.dto.ModelPackageData;
import com.ericsson.mxe.modelservice.seldondeployment.MxeModelInfo;
import com.ericsson.mxe.modelservice.seldondeployment.MxeSeldonDeploymentData;
import com.ericsson.mxe.modelservice.seldondeployment.MxeSeldonDeploymentInfo;

@Service
public class MxeUtils {

    public static GetModelDeploymentResponse mxeSeldonDeploymentInfoToGetModelDeploymentResponse(
            ModelCatalogResolver resolver, MxeSeldonDeploymentInfo d) {
        GetModelDeploymentResponse result = new GetModelDeploymentResponse();

        result.created = d.created;
        result.name = d.name;
        result.replicas = d.replicas;
        result.autoScaling = d.autoScaling;
        result.status = d.status;
        result.message = d.message;
        result.type = d.type;
        result.models =
                d.models.stream().map(mxeModel -> mxeModel2Package(resolver, mxeModel)).collect(Collectors.toList());
        result.createdByUserId = d.createdByUserId;
        result.createdByUserName = d.createdByUserName;

        return result;
    }

    public static MxeSeldonDeploymentData createModelDeploymentRequest2MxeSeldonDeploymentData(
            ModelCatalogResolver resolver, CreateModelDeploymentRequest request, String userId, String userName) {
        MxeSeldonDeploymentData result = new MxeSeldonDeploymentData();

        result.name = MxeParameterValidator.createDeploymentName(request.name);
        result.replicas = MxeParameterValidator.validateReplicas(request.replicas, true);
        result.autoScaling = MxeParameterValidator.validateAutoScaling(request.autoScaling);
        result.type = MxeParameterValidator.validateType(request.type, request.models.size());
        result.models = request.models.stream()
                .map(mxeModelData -> MxeParameterValidator.mxePackage2Model(resolver, mxeModelData))
                .collect(Collectors.toList());
        result.weights = MxeParameterValidator.validateWeights(request.models, true, result.models.size());
        result.createdByUserId = userId == null ? "null" : userId;
        result.createdByUserName = userName == null ? "<unknown>" : userName;
        return result;
    }

    public static MxeSeldonDeploymentData patchModelDeploymentRequest2MxeSeldonDeploymentData(
            ModelCatalogResolver resolver, String serviceName, PatchModelDeploymentRequest request,
            int numberOfModelsInService) {
        MxeSeldonDeploymentData result = new MxeSeldonDeploymentData();

        result.name = serviceName;
        result.replicas = MxeParameterValidator.validateReplicas(request.replicas, false);
        result.autoScaling = MxeParameterValidator.validateAutoScaling(request.autoScaling);
        result.type = MxeModelDeploymentType.UNKNOWN;
        result.models = request.models == null ? Collections.emptyList()
                : MxeParameterValidator.validateModelsPatch(serviceName, request.models, numberOfModelsInService,
                        resolver);
        result.weights = request.models == null ? Collections.emptyList()
                : MxeParameterValidator.validateWeights(request.models, false, numberOfModelsInService);
        return result;
    }

    private static MxeModelDetails mxeModel2Package(ModelCatalogResolver resolver, MxeModelInfo model) {
        Optional<ModelPackageData> hit = resolver.getPackageForImage(model.image);
        MxeModelDetails mxeModel = new MxeModelDetails();
        if (hit.isPresent()) {
            mxeModel.id = hit.get().id;
            mxeModel.version = hit.get().version;
            mxeModel.endpointType = model.endpointType;
            mxeModel.weight = model.weight;
        } else {
            // the docker image can not be looked up in the models table
            // TODO: later throw exception here??
            mxeModel.id = "<<" + model.image + ">>";
            mxeModel.version = "UNKNOWN";
            mxeModel.endpointType = model.endpointType;
        }
        return mxeModel;
    }
}
