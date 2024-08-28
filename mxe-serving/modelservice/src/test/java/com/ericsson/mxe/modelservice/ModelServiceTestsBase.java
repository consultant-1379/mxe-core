package com.ericsson.mxe.modelservice;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
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
import com.ericsson.mxe.modelservice.config.properties.ServiceMeshProperties;
import com.ericsson.mxe.modelservice.deployer.response.DeleteDeploymentResponse;
import com.ericsson.mxe.modelservice.modelcatalog.ModelCatalogService;
import com.ericsson.mxe.modelservice.modelcatalog.dto.ModelPackageData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class ModelServiceTestsBase {

    protected static final String URL = "/v2/model-services";
    protected static final String EMPTY_DOMAIN = "";
    protected static final String EMPTY_DEPLOY_REPO_URL = "";
    protected static final String TEST_DEPLOY_REPO_URL = "https://gitlab.internal.ericsson.com/mxe-test/mxe-gitops.git";
    protected static final String MANIFEST_FILE_LOC = "seldonmanifest/request/";

    protected static final String REQ_MANIFEST_NAME = "custom_manifest";
    protected static final String REQ_MODELDATA = "modeldata";

    protected static final String TEST_SINGLE_STATELESS_NAME = "single-stateless-model";
    protected static final String TEST_DEPLOY_MODEL_NA = "model-not-available";
    protected static final String TEST_AB_STATELESS_NAME = "test-ab-model";
    protected static final String SELDON_GROUP = "machinelearning.seldon.io";
    protected static final String SELDON_VERSION = "v1";
    protected static final String SELDON_PLURAL = "seldondeployments";
    protected static final String NAMESPACE = "testnamespace";

    @Autowired
    protected ObjectMapper objectMapper;

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

    @MockBean
    protected ServiceMeshProperties serviceMeshProperties;

    private static MediaType mediaType;
    private static final String BOUNDARY = "------------------------9b698ba20448223d";
    private static final String X_AUTH_SUB = "x-auth-subject";
    private static final String X_AUTH_NAME = "x-auth-userid";
    private static final String USER_ID = "1234-5678-9012";
    private static final String USER_NAME = "mxe-user";


    @BeforeAll
    static void initBase() {
        Map<String, String> contentTypeParams = new HashMap<>();
        contentTypeParams.put("boundary", BOUNDARY);
        mediaType = new MediaType("multipart", "form-data", contentTypeParams);
    }

    protected RequestBuilder getCreateModelDeploymentRequest(String fileName, String domain, String gitRepoUrl)
            throws Exception {

        if (!StringUtils.isEmpty(fileName)) {
            byte[] manifestFile = createFileContent(REQ_MANIFEST_NAME, getByteArrayFromResource(fileName), BOUNDARY,
                    "text/yaml", fileName);

            byte[] formdataContents =
                    Optional.ofNullable(domain).isEmpty() && Optional.ofNullable(gitRepoUrl).isEmpty() ? manifestFile
                            : createFileAndModelDataContent(getModelData(domain, gitRepoUrl).getBytes(), BOUNDARY,
                                    fileName, getByteArrayFromResource(fileName));

            return MockMvcRequestBuilders.post(URL).content(formdataContents).contentType(mediaType)
                    .header(X_AUTH_SUB, USER_ID).header(X_AUTH_NAME, USER_NAME);
        } else {
            return MockMvcRequestBuilders.post(URL).contentType(mediaType).header(X_AUTH_SUB, USER_ID)
                    .header(X_AUTH_NAME, USER_NAME);
        }

    }

    protected RequestBuilder getCreateModelDeploymentRequest(String fileName, String domain) throws Exception {
        return getCreateModelDeploymentRequest(fileName, domain, StringUtils.EMPTY);
    }

    protected RequestBuilder getPatchModelDeploymentRequest(String deploymentName, String fileName, String domain)
            throws Exception {
        if (!StringUtils.isEmpty(fileName)) {
            byte[] manifestFile = createFileContent(REQ_MANIFEST_NAME, getByteArrayFromResource(fileName), BOUNDARY,
                    "text/yaml", fileName);

            byte[] formdataContents = Optional.ofNullable(domain).isEmpty() ? manifestFile
                    : createFileAndModelDataContent(getModelData(domain).getBytes(), BOUNDARY, fileName,
                            getByteArrayFromResource(fileName));

            return MockMvcRequestBuilders.patch(URL + "/" + deploymentName).content(formdataContents)
                    .contentType(mediaType).header(X_AUTH_SUB, USER_ID).header(X_AUTH_NAME, USER_NAME);
        } else {
            return MockMvcRequestBuilders.post(URL).contentType(mediaType).header(X_AUTH_SUB, USER_ID)
                    .header(X_AUTH_NAME, USER_NAME);
        }
    }

    protected RequestBuilder getDeleteModelDeploymentRequest(String deploymentName) throws IOException {
        return MockMvcRequestBuilders.delete(URL + "/" + deploymentName).contentType(MediaType.APPLICATION_JSON)
                .header(X_AUTH_SUB, USER_ID).header(X_AUTH_NAME, USER_NAME);
    }

    @BeforeEach
    void initModelService() throws IOException {
        when(kubernetesService.getNamespace()).thenReturn(NAMESPACE);
        when(modelCatalogService.getModelCatalog(anyBoolean()))
                .thenReturn(getModelCatalogFromResource("getModelCatalog.json"));

        final SeldonCrdProperties seldonCrdProperties = new SeldonCrdProperties("SeldonDeployment");

        when(seldonProperties.getCrd()).thenReturn(seldonCrdProperties);
        when(serviceMeshProperties.isMtlsEnabled()).thenReturn(false);
        when(dockerProperties.getRegistryHostname()).thenReturn("vmx-eea166.ete.ka.sw.ericsson.se:32222");
        when(dockerProperties.getRegistryHostname()).thenReturn("vmx-eea166.ete.ka.sw.ericsson.se:32222");

    }

    protected List<ModelPackageData> getModelCatalogFromResource(String fileName) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        return new ObjectMapper().readValue(is, new TypeReference<List<ModelPackageData>>() {});
    }

    protected byte[] createFileContent(String name, byte[] data, String boundary, String contentType, String fileName) {
        String start = "--" + boundary + "\r\n Content-Disposition: form-data; name=\"" + name + "\"; filename=\""
                + fileName + "\"\r\n" + "Content-type: " + contentType + "\r\n\r\n";

        String end = "\r\n--" + boundary + "--";
        return ArrayUtils.addAll(start.getBytes(), ArrayUtils.addAll(data, end.getBytes()));
    }

    protected byte[] createFileAndModelDataContent(byte[] data, String boundary, String fileName, byte[] filedata) {
        String start = "--" + boundary + "\r\n Content-Disposition: form-data; name=\"" + REQ_MODELDATA + "\"\r\n"
                + "Content-type: \"application/json\"\r\n\r\n";
        String middle = "\r\n--" + boundary + "\r\n Content-Disposition: form-data; name=\"" + REQ_MANIFEST_NAME
                + "\"; filename=\"" + fileName + "\"\r\n" + "Content-type: \"text/yaml\"\r\n\r\n";

        String end = "\r\n--" + boundary + "--";

        byte[] formcontents = ArrayUtils.addAll(start.getBytes(), data);

        return ArrayUtils.addAll(ArrayUtils.addAll(formcontents, middle.getBytes()),
                ArrayUtils.addAll(filedata, end.getBytes()));
    }


    protected byte[] getByteArrayFromResource(String fileName) throws Exception {
        String filePath = MANIFEST_FILE_LOC + fileName;
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(filePath).getFile());
        System.out.println("File Found : " + file.exists());

        byte[] data = Files.readAllBytes(file.toPath());
        String content = new String(data);
        System.out.println(content);

        return data;
    }

    protected String getModelData(String domain, String gitRepoUrl) {
        return "{ \"domain\": \"" + domain + "\", \"deployerRepoUrl\": \"" + gitRepoUrl + "\"}";
    }

    protected String getModelData(String domain) {
        return "{ \"domain\": \"" + domain + "\"}";
    }

    protected DeleteDeploymentResponse mockDeployerResponse(boolean status) {
        DeleteDeploymentResponse resp = new DeleteDeploymentResponse();
        resp.status = status;
        return resp;
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
