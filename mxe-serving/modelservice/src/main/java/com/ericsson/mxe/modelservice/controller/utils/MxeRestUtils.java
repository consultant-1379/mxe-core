package com.ericsson.mxe.modelservice.controller.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import com.ericsson.mxe.backendservicescommon.exception.MxeBadRequestException;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.modelservice.controller.input.RequestModelData;
import com.ericsson.mxe.modelservice.controller.input.SeldonManifestHolder;
import com.ericsson.mxe.modelservice.controller.input.SeldonManifestRequest;
import com.ericsson.mxe.modelservice.controller.output.GetSeldonManifestResponse;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentType;
import com.ericsson.mxe.modelservice.dto.MxeModelDetails;
import com.ericsson.mxe.modelservice.modelcatalog.ModelCatalogResolver;
import com.ericsson.mxe.modelservice.modelcatalog.dto.ModelPackageData;
import com.ericsson.mxe.modelservice.seldondeployment.MxeModelInfo;
import com.ericsson.mxe.modelservice.seldondeployment.MxeSeldonDeploymentInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class MxeRestUtils {

    private static final Logger logger = LogManager.getLogger(MxeRestUtils.class);

    private static final String SELDON_DEPLOY_MANIFEST_FILE_NAME = "custom_manifest";
    private static final String SELDON_DEPLOY_MANIFEST_MODEL_DATA = "modeldata";

    private static final String INVALID_REQUEST_MANIFEST_FILE_NOT_FOUND =
            "Deployment Manifest file with name \"%s\" is missing in multipart request"
                    .formatted(SELDON_DEPLOY_MANIFEST_FILE_NAME);
    private static final String INVALID_MANIFEST_DEPLOYMENT_NAME_NOT_FOUND =
            "Invalid Manifest file: Deployment name is missing in manifest";
    private static final String INVALID_MANIFEST_DEPLOYMENT_NAME_MISMATCH =
            "Invalid Manifest file: Deployment name in request \"%s\" does not match with manifest \"%s\"";

    private static final String INVALID_REQUEST_DEPLOYMENT_TYPE_NOT_SUPPORTED =
            "Deployment Type  \"%s\" is not one of supported type - ['model', 'static']";

    private MxeRestUtils() {}

    public static SeldonManifestRequest parseMultipartRequest(HttpServletRequest request) {
        final JakartaServletFileUpload upload = new JakartaServletFileUpload();

        SeldonManifestRequest inputRequest = new SeldonManifestRequest();

        try {
            final FileItemInputIterator fileItemIterator = upload.getItemIterator(request);
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

            while (fileItemIterator.hasNext()) {
                FileItemInput fileItemStream = fileItemIterator.next();
                readFileItems(inputRequest, yamlMapper, fileItemStream);

            }
        } catch (IOException e) {
            throw new MxeInternalException(e);
        }

        return inputRequest;
    }

    public static void validateMultipartPostRequest(SeldonManifestRequest seldonManifestRequest) {
        if (Optional.ofNullable(seldonManifestRequest.getSeldonManifestHolder()).isEmpty()) {
            throw new MxeBadRequestException(INVALID_REQUEST_MANIFEST_FILE_NOT_FOUND);
        }

        if (Optional.ofNullable(seldonManifestRequest.getSeldonManifestHolder().getDeploymentName()).isEmpty()) {
            throw new MxeBadRequestException(INVALID_MANIFEST_DEPLOYMENT_NAME_NOT_FOUND);
        }

        RequestModelData requestModelData =
                Optional.ofNullable(seldonManifestRequest.getRequestModelData()).orElse(new RequestModelData());

        requestModelData.modelType = seldonManifestRequest.getSeldonManifestHolder().getDeploymentType();
        seldonManifestRequest.setRequestModelData(requestModelData);

        Optional.ofNullable(seldonManifestRequest.getRequestModelData()).map(m -> m.modelType)
                .ifPresentOrElse(deployType -> {
                    if (!(MxeModelDeploymentType.MODEL.name().toLowerCase().equals(deployType)
                            || MxeModelDeploymentType.STATIC.name().toLowerCase().equals(deployType))) {
                        throw new MxeBadRequestException(
                                INVALID_REQUEST_DEPLOYMENT_TYPE_NOT_SUPPORTED.formatted(deployType));
                    }
                }, () -> {
                    throw new MxeBadRequestException(
                            INVALID_REQUEST_DEPLOYMENT_TYPE_NOT_SUPPORTED.formatted("UNKNOWN"));
                });

    }

    public static void validateMultipartPatchRequest(String deploymentName,
            SeldonManifestRequest seldonManifestRequest) {
        if (Optional.ofNullable(seldonManifestRequest.getSeldonManifestHolder()).isEmpty()) {
            throw new MxeBadRequestException(INVALID_REQUEST_MANIFEST_FILE_NOT_FOUND);
        }

        String manifestName =
                Optional.ofNullable(seldonManifestRequest.getSeldonManifestHolder().getDeploymentName()).orElse("");

        if (manifestName.isEmpty()) {
            throw new MxeBadRequestException(INVALID_MANIFEST_DEPLOYMENT_NAME_NOT_FOUND);
        } else if (!manifestName.equals(deploymentName)) {
            throw new MxeBadRequestException(
                    INVALID_MANIFEST_DEPLOYMENT_NAME_MISMATCH.formatted(deploymentName, manifestName));
        }

        RequestModelData requestModelData =
                Optional.ofNullable(seldonManifestRequest.getRequestModelData()).orElse(new RequestModelData());

        requestModelData.modelType = seldonManifestRequest.getSeldonManifestHolder().getDeploymentType();
        seldonManifestRequest.setRequestModelData(requestModelData);

        if (!(MxeModelDeploymentType.MODEL.name().toLowerCase().equals(requestModelData.modelType)
                || MxeModelDeploymentType.STATIC.name().toLowerCase().equals(requestModelData.modelType))) {
            throw new MxeBadRequestException(
                    INVALID_REQUEST_DEPLOYMENT_TYPE_NOT_SUPPORTED.formatted(requestModelData.modelType));
        }
    }



    public static GetSeldonManifestResponse mxeSeldonDeploymentInfoToGetSeldonManifestResponse(
            ModelCatalogResolver resolver, MxeSeldonDeploymentInfo d) {
        GetSeldonManifestResponse result = new GetSeldonManifestResponse();

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

    private static void readFileItems(SeldonManifestRequest inputRequest, ObjectMapper yamlMapper,
            FileItemInput fileItemStream) {
        String fieldName = fileItemStream.getFieldName();
        try (InputStream item = fileItemStream.getInputStream();) {

            if (!fileItemStream.isFormField()) {
                String fileName = fileItemStream.getName();
                logger.debug("Incoming Multipart form fieldName {} file {} ", fieldName, fileName);
                if (fieldName.equals(SELDON_DEPLOY_MANIFEST_FILE_NAME)) {
                    LinkedHashMap manifestMap = yamlMapper.readValue(item, LinkedHashMap.class);
                    inputRequest.setSeldonManifestHolder(new SeldonManifestHolder(manifestMap));
                }

                String fileExtn = FilenameUtils.getExtension(fileName);
                if (!(fileExtn.equalsIgnoreCase("yaml") || fileExtn.equalsIgnoreCase("yml"))) {
                    throw new MxeBadRequestException("The custom manifest file is not in expected format (yaml|yml)");
                }
            } else {
                logger.debug("Incoming Multipart form data {} ", fieldName);
                if (fieldName.equals(SELDON_DEPLOY_MANIFEST_MODEL_DATA)) {
                    RequestModelData reqModelData = new ObjectMapper().readValue(item, RequestModelData.class);
                    inputRequest.setRequestModelData(reqModelData);
                }
            }
        } catch (MxeBadRequestException e) {
            throw e;
        } catch (MismatchedInputException e) {
            throw new MxeBadRequestException("The custom manifest file is not in expected format (yaml|yml)");
        } catch (Exception e) {
            throw new MxeInternalException(e);
        }
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
