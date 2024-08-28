package com.ericsson.mxe.modelcatalogueservice.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.ericsson.mxe.backendservicescommon.dto.status.PackageStatus;
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.backendservicescommon.kubernetes.ServicePortResolverService;
import com.ericsson.mxe.modelcatalogueservice.IntegrationTests;
import com.ericsson.mxe.modelcatalogueservice.dto.request.CreateModelRequest;
import com.ericsson.mxe.modelcatalogueservice.persistence.domain.ModelEntity;
import com.ericsson.mxe.modelcatalogueservice.persistence.domain.ModelEntityId;

@IntegrationTests
public class CreateModelTests extends ModelTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestEntityManager entityManager;

    @MockBean
    KubernetesService kubernetesService;
    @MockBean
    ServicePortResolverService servicePortResolverService;

    @BeforeEach
    void init() {
        final String namespace = "namespace";

        when(kubernetesService.getNamespace()).thenReturn(namespace);
        when(servicePortResolverService.resolve(any(), any(), any())).thenReturn(Optional.of(22));
    }

    @Test
    void shouldStorePostedModel() throws Exception {
        CreateModelRequest request = new CreateModelRequest("name1", "version1", "title1", "author", "description1",
                "image1", "icon1", null);

        mockMvc.perform(getCreateModelRequest(request)).andExpect(status().isOk());

        ModelEntity storedModel =
                entityManager.find(ModelEntity.class, new ModelEntityId(request.getId(), request.getVersion()));

        assertThat(storedModel).isNotNull();
    }

    @Test
    void shouldStorePostedModelWithoutIcon() throws Exception {
        CreateModelRequest request =
                new CreateModelRequest("name1", "version1", "title1", "author", "description1", "image1", null, null);

        mockMvc.perform(getCreateModelRequest(request)).andExpect(status().isOk());

        ModelEntity storedModel =
                entityManager.find(ModelEntity.class, new ModelEntityId(request.getId(), request.getVersion()));

        assertThat(storedModel).isNotNull();
    }

    @Test
    void shouldStoreCreationTimeWithPostedModel() throws Exception {
        OffsetDateTime before = OffsetDateTime.now();

        CreateModelRequest request = new CreateModelRequest("name1", "version1", "title1", "author", "description1",
                "image1", "icon1", null);

        mockMvc.perform(getCreateModelRequest(request));

        OffsetDateTime after = OffsetDateTime.now();

        ModelEntity storedModel =
                entityManager.find(ModelEntity.class, new ModelEntityId(request.getId(), request.getVersion()));

        assertThat(storedModel.getCreated()).isBetween(before, after);
    }

    @Test
    void shouldStorePostedModelWithStatusAvailable() throws Exception {
        CreateModelRequest request = new CreateModelRequest("name1", "version1", "title1", "author", "description1",
                "image1", "icon1", null);

        mockMvc.perform(getCreateModelRequest(request));

        ModelEntity storedModel =
                entityManager.find(ModelEntity.class, new ModelEntityId(request.getId(), request.getVersion()));

        assertThat(storedModel.getStatus()).isEqualTo(PackageStatus.Available);
    }

    @Test
    void shouldStorePostedModelWithoutMessage() throws Exception {
        CreateModelRequest request = new CreateModelRequest("name1", "version1", "title1", "author", "description1",
                "image1", "icon1", null);

        mockMvc.perform(getCreateModelRequest(request));

        ModelEntity storedModel =
                entityManager.find(ModelEntity.class, new ModelEntityId(request.getId(), request.getVersion()));

        assertThat(storedModel.getMessage()).isNull();
    }

    @Test
    void shouldReturnConflictWhenThereIsAlreadyAStoredModelWithTheSameNameAndVersion() throws Exception {
        CreateModelRequest request1 = new CreateModelRequest("name1", "version1", "title1", "author", "description1",
                "image1", "icon1", null);

        mockMvc.perform(getCreateModelRequest(request1));

        CreateModelRequest request2 = new CreateModelRequest("name1", "version1", "title1", "author", "description2",
                "image2", "icon2", null);

        mockMvc.perform(getCreateModelRequest(request2)).andExpect(status().isConflict());
    }

}
