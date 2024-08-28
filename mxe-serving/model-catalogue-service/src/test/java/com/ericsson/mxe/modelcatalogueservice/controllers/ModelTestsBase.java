package com.ericsson.mxe.modelcatalogueservice.controllers;

import com.ericsson.mxe.backendservicescommon.dto.status.PackageStatus;
import com.ericsson.mxe.modelcatalogueservice.dto.request.CreateModelRequest;
import com.ericsson.mxe.modelcatalogueservice.persistence.domain.ModelEntity;
import com.ericsson.mxe.modelcatalogueservice.persistence.domain.ModelEntityId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class ModelTestsBase {

    @Autowired
    protected ObjectMapper objectMapper;
    private static MediaType mediaType;
    private static final String BOUNDARY = "------------------------9b698ba20448223d";
    protected static final String API_URL = "/v1/models";

    @BeforeAll
    static void initBase() {
        Map<String, String> contentTypeParams = new HashMap<>();
        contentTypeParams.put("boundary", BOUNDARY);
        mediaType = new MediaType("multipart", "form-data", contentTypeParams);
    }

    protected ModelEntity initEntity(final ModelEntityId id, final String title, final String author,
            final String description, final String image, final OffsetDateTime created, final String icon,
            final PackageStatus status, final String message, final String errorLog, final Boolean internal,
            final String dockerRegistrySecretName) {
        return initEntity(id, title, author, description, image, created, icon, status, message, errorLog, internal,
                dockerRegistrySecretName, "mxe-user-id", "mxe-user-name");
    }

    protected ModelEntity initEntity(final ModelEntityId id, final String title, final String author,
            final String description, final String image, final OffsetDateTime created, final String icon,
            final PackageStatus status, final String message, final String errorLog, final Boolean internal,
            final String dockerRegistrySecretName, final String userId, final String userName) {
        return new ModelEntity(title, author, image, created, icon, status, message, errorLog, internal, userId,
                userName, id, description, dockerRegistrySecretName, false);
    }

    protected RequestBuilder getCreateUnknownModelRequest(String id, CreateModelRequest request) throws IOException {
        return MockMvcRequestBuilders.post(API_URL + "/" + id + "/unknown")
                .content(objectMapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Mxe-Original-Authorization", "tokenValue");
    }

    protected RequestBuilder getCreateModelRequest(CreateModelRequest request) throws IOException {
        final String multipartFile = getMultiPart(request);
        return MockMvcRequestBuilders.post(API_URL).content(multipartFile.getBytes()).contentType(mediaType);
    }

    protected RequestBuilder getDeleteModelRequest(String id, String version) throws IOException {
        return MockMvcRequestBuilders.delete(API_URL + "/" + id + "/" + version).contentType(mediaType);
    }

    private String getMultiPart(Object request) throws IOException {
        return "\r\n--" + BOUNDARY + "\r\n" + "Content-Disposition: form-data; name=\"modeldata\"\r\n"
                + "Content-Type: application/json\r\n\r\n" + objectMapper.writeValueAsString(request) + "\r\n--"
                + BOUNDARY + "--\r\n";
    }

}
