package com.ericsson.mxe.modelservice;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.modelservice.config.properties.DockerProperties;
import com.ericsson.mxe.modelservice.config.properties.SeldonCrdProperties;
import com.ericsson.mxe.modelservice.config.properties.SeldonProperties;
import com.ericsson.mxe.modelservice.dto.request.CreateModelDeploymentRequest;
import com.ericsson.mxe.modelservice.dto.request.PatchModelDeploymentRequest;
import com.ericsson.mxe.modelservice.modelcatalog.ModelCatalogService;
import com.ericsson.mxe.modelservice.modelcatalog.dto.ModelPackageData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class ModelServiceTestsBaseV1 {

    protected static final String URL = "/v1/model-services/";

    protected static final String DEPLOYMENT_NAME = "alma";
    protected static final String SELDON_GROUP = "machinelearning.seldon.io";
    protected static final String SELDON_VERSION = "v1";
    protected static final String SELDON_PLURAL = "seldondeployments";
    protected static final String NAMESPACE = "testnamespace";
    protected static final String DEPLOYMENT_NAME_IMAGERECOGNITION = "imagerecognitionvgg16";

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected KubernetesService kubernetesService;

    @MockBean
    protected ModelCatalogService modelCatalogService;

    @MockBean
    private SeldonProperties seldonProperties;

    @MockBean
    private DockerProperties dockerProperties;

    protected RequestBuilder getCreateModelDeploymentRequest(CreateModelDeploymentRequest request) throws IOException {
        return MockMvcRequestBuilders.post(URL).content(new ObjectMapper().writeValueAsString(request).getBytes())
                .contentType(MediaType.APPLICATION_JSON);
    }

    protected RequestBuilder getPatchModelDeploymentRequest(String deploymentName, PatchModelDeploymentRequest request)
            throws IOException {
        return MockMvcRequestBuilders.patch(URL + "/" + deploymentName)
                .content(new ObjectMapper().writeValueAsString(request).getBytes())
                .contentType(MediaType.APPLICATION_JSON);
    }

    protected RequestBuilder getDeleteModelDeploymentRequest(String deploymentName) throws IOException {
        return MockMvcRequestBuilders.delete(URL + "/" + deploymentName).contentType(MediaType.APPLICATION_JSON);
    }

    @BeforeEach
    void initModelService() throws IOException {
        when(kubernetesService.getNamespace()).thenReturn(NAMESPACE);
        when(modelCatalogService.getModelCatalog(anyBoolean()))
                .thenReturn(getModelCatalogFromResource("getModelCatalog.json"));

        final SeldonCrdProperties seldonCrdProperties = new SeldonCrdProperties("SeldonDeployment");

        when(seldonProperties.getCrd()).thenReturn(seldonCrdProperties);
        when(dockerProperties.getRegistryHostname()).thenReturn("vmx-eea166.ete.ka.sw.ericsson.se:32222");

    }

    protected Map getSeldonDeploymentFromFile(String fileName) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        return new ObjectMapper().readValue(is, Map.class);
    }

    protected Map getSeldonDeploymentFromFileWithData(String fileName, String deploymentName, String domain, String id,
            String version, int replicas) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        JsonNode tree = new ObjectMapper().readTree(is);
        replace(tree, "/metadata", "name", deploymentName);
        replace(tree, "/metadata/labels", "mxe/domain", domain);
        replace(tree, "/spec/predictors/0", "replicas", replicas);
        replace(tree, "/spec/predictors/0/componentSpecs/0/spec/containers/0", "image",
                "vmx-eea166.ete.ka.sw.ericsson.se:32222/" + id + ":" + version);
        return new ObjectMapper().convertValue(tree, Map.class);
    }

    protected Map getSeldonDeploymentFromFileWithReplicas(String fileName, int replicas) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        JsonNode tree = new ObjectMapper().readTree(is);
        JsonNode located = tree.at("/spec/predictors/0");
        ((ObjectNode) located).put("replicas", replicas);
        return new ObjectMapper().convertValue(tree, Map.class);
    }

    private void replace(JsonNode tree, String location, String key, int value) {
        JsonNode located = tree.at(location);
        ((ObjectNode) located).put(key, value);
    }

    private void replace(JsonNode tree, String location, String key, String value) {
        JsonNode located = tree.at(location);
        ((ObjectNode) located).put(key, value);
    }

    protected List<ModelPackageData> getModelCatalogFromResource(String fileName) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        return new ObjectMapper().readValue(is, new TypeReference<List<ModelPackageData>>() {});
    }

    protected String getStringFromResource(String fileName) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        // TODO: refactor this
        return new BufferedReader(new InputStreamReader(is)).lines()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    protected ResultActions performRequest(MockHttpServletRequestBuilder action, String expectedResourceFileName)
            throws Exception {
        MockHttpServletRequestBuilder builder = action.contentType(MediaType.APPLICATION_JSON)
                .header("x-auth-subject", "1234-5678-9012").header("x-auth-userid", "mxe-user");

        if (expectedResourceFileName != null) {
            builder = builder.content(getStringFromResource(expectedResourceFileName));
        }

        return this.mockMvc.perform(builder);
    }
}
