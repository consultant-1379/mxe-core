package com.ericsson.mxe.modelcatalogueservice.services;

import java.util.List;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import com.ericsson.mxe.backendservicescommon.dto.status.Status;
import com.ericsson.mxe.modelcatalogueservice.dto.request.CreateRequest;
import com.ericsson.mxe.modelcatalogueservice.dto.response.ModelCatalogueServiceResponse;

public abstract class CatalogueService<REQUEST extends CreateRequest, STATUS extends Status> {

    public static final String UNKNOWN = "unknown";

    @Autowired
    @Qualifier("entityAndDtoMapper")
    protected ModelMapper entityAndDtoMapper;

    public abstract ModelCatalogueServiceResponse create(String userId, String userName, REQUEST request, STATUS status,
            boolean internal);

    protected abstract String createDummyEntity(String userId, String userName, String name);

    protected abstract List<String> getValidFormFields();

    protected String getUniqueName(String name, List<String> existingNames) {
        String candidate;
        do {
            candidate = name + "-" + UUID.randomUUID();
        } while (existingNames.contains(candidate));
        return candidate;
    }
}
