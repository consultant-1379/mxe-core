package com.ericsson.mxe.jcat.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ericsson.commonlibrary.beanadapter.BeanAdapterProvider;
import com.ericsson.commonlibrary.cf.AdapterBuilder;
import com.ericsson.commonlibrary.cf.ConfigurationFacade;
import com.ericsson.commonlibrary.cf.spi.ConfigurationData;
import com.ericsson.commonlibrary.cf.spi.ConfigurationFacadeAdapter;

public class Config extends CompositeConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    private static Config instance;

    private static final String CONFIG_PROPERTY_NAME = "config";
    private static final String DOT = ".";
    private static final String EXTENSION_CFG = "cfg";
    private static final String EXTENSION_JSON = "json";
    private static final String EXTENSION_PROPERTIES = "properties";
    private static final String EXTENSION_XML = "xml";
    private static final String JAR = "jar";
    private static final String KEY_CONFIG_ROOT = "configroot";

    private static final List<String> extensions =
            Arrays.asList(EXTENSION_CFG, EXTENSION_JSON, EXTENSION_PROPERTIES, EXTENSION_XML);
    private static Path tempDirectory = null;

    private Path configRoot;
    private List<Path> allConfigFilePaths;
    private Map<Class<? extends ConfigurationData>, Set<? extends ConfigurationData>> allConfigurationData;

    private Set<MxeCluster> testExecutionClusters;

    private Config() throws IOException {
        init();
    }

    public static synchronized Config getInstance() throws IOException {
        if (instance == null) {
            try {
                instance = new Config();
            } finally {
                registerShutDownHook();
            }
        }

        return instance;
    }

    private static void registerShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (Objects.nonNull(tempDirectory)) {
                LOGGER.info("############################## Cleanup ##################################");
                LOGGER.info("---> Trying to cleanup temp config directory, deleting [{}]", tempDirectory.toString());
                try {
                    FileUtils.deleteDirectory(tempDirectory.toFile());
                } catch (IOException e) {
                    LOGGER.error("Unable to delete temp config directory", e);
                }
                LOGGER.info("############################ End cleanup #################################");
            }
        }));
    }

    private void init() throws IOException {
        LOGGER.info("############################ Init configuration #################################");

        configRoot = resolveConfigRoot();
        LOGGER.info("Config root [{}]", configRoot);

        LOGGER.info("---> Identifying configuration files");
        try (Stream<Path> configs = Files.walk(configRoot)) {
            allConfigFilePaths = configs.filter(path -> path.toFile().isFile())
                    .filter(file -> extensions.stream().parallel()
                            .anyMatch(extension -> file.getFileName().toString().toLowerCase().endsWith(extension)))
                    .peek(file -> LOGGER.info("Configuration file [{}]", file)).collect(Collectors.toList());
        }

        LOGGER.debug("allConfigFilePaths: {}", allConfigFilePaths);

        loadConfigurationDatas();
        loadPropertyConfigurations();

        LOGGER.info("######################### End of init configuration #############################");
    }

    @SuppressWarnings("squid:S3776")
    private static Path resolveConfigRoot() {
        Path configRoot = null;
        File providedConfigRoot;
        try {
            String configRootPropertyValue = System.getProperty(KEY_CONFIG_ROOT);
            providedConfigRoot =
                    StringUtils.isNotEmpty(configRootPropertyValue) ? new File(configRootPropertyValue) : null;
            if (Objects.nonNull(providedConfigRoot) && providedConfigRoot.exists()
                    && providedConfigRoot.isDirectory()) {
                configRoot = Paths.get(providedConfigRoot.getPath());
                LOGGER.info("---> Using user provided configuration root");
            } else {
                URI uri = Config.class.getResource("/" + CONFIG_PROPERTY_NAME).toURI();
                if (JAR.equals(uri.getScheme())) {
                    final Path basePath = Paths.get(uri.toString().replaceAll("jar:file:", StringUtils.EMPTY)
                            .replaceAll("!/" + CONFIG_PROPERTY_NAME, StringUtils.EMPTY)).getParent();

                    if (Objects.isNull(basePath)) {
                        LOGGER.error("Base path is null");
                        return null;
                    }

                    Path path = Paths.get(basePath.toString(), CONFIG_PROPERTY_NAME);
                    LOGGER.info("Config path: {}", path);

                    File configDirectory = path.toFile();
                    if (configDirectory.exists() && configDirectory.isDirectory()) {
                        LOGGER.info("---> Using configuration root located besides one-jar");
                        configRoot = path;
                    } else {
                        LOGGER.info("---> Using configuration packed into one-jar");
                        tempDirectory = extractConfigResources(uri);
                        configRoot = Paths.get(tempDirectory.toString(), CONFIG_PROPERTY_NAME);
                    }
                } else {
                    LOGGER.info("---> Using predefined configuration root, execution within IDE");
                    configRoot = Paths.get(uri);
                }
            }
        } catch (URISyntaxException | IOException | ConfigurationException e) {
            LOGGER.error("Error while resolving config path: ", e);
        }

        return configRoot;
    }

    private static Path extractConfigResources(URI uri) throws IOException, ConfigurationException {
        final JarURLConnection con = (JarURLConnection) uri.toURL().openConnection();
        final Path tempDirectory = Files.createTempDirectory("MXE-JCAT_temp");

        try (final JarFile archive = con.getJarFile()) {
            final List<JarEntry> jarConfigResourceEntries = archive.stream().filter(entry -> !entry.isDirectory())
                    .filter(entry -> entry.getName().startsWith(CONFIG_PROPERTY_NAME))
                    .filter(entry -> extensions.stream().parallel()
                            .anyMatch(extension -> entry.getName().toLowerCase().endsWith(extension)))
                    .peek(entry -> LOGGER.info(entry.getName())).collect(Collectors.toList());

            for (JarEntry configEntry : jarConfigResourceEntries) {
                final String resource = configEntry.getName();

                try (final InputStream link = Config.class.getResourceAsStream("/" + resource)) {
                    Path target = Paths.get(tempDirectory.toString(), resource);

                    LOGGER.debug("Trying to extract [{}] resource to [{}]", resource, target);

                    final Path parentPath = target.getParent();

                    if (parentPath == null) {
                        throw new ConfigurationException("Could not create temporary directory");
                    } else {
                        final File file = parentPath.toFile();
                        boolean mkdirsResult = file.mkdirs();

                        if (!mkdirsResult || !parentPath.toFile().exists()) {
                            throw new ConfigurationException("Could not create temporary directory");
                        }
                    }

                    Files.copy(link, target);
                } catch (IOException e) {
                    LOGGER.error("Error", e);
                }
            }
        }

        return tempDirectory;
    }

    private void loadConfigurationDatas() {
        LOGGER.info("---> Loading bean configuration datas");
        final BiFunction<String, String, Predicate<String>> getFilter = (fileName,
                extension) -> predicate -> predicate.matches("^.*" + fileName + DOT + extension.trim() + "$");

        allConfigurationData = new HashMap<>();
        testExecutionClusters =
                loadConfigurationData(MxeCluster.class, getFilter.apply("mxe-cluster-data", EXTENSION_XML));
        allConfigurationData.put(TestExecutionHost.class, testExecutionClusters);
    }

    private <T extends ConfigurationData> Set<T> loadConfigurationData(Class<T> type, Predicate<String> filter) {
        LOGGER.info("Loading configuration data for [{}]", type.getSimpleName());
        ConfigurationFacadeAdapter adapter;
        final AdapterBuilder adapterBuilder = ConfigurationFacade.newAdapterBuilder();

        allConfigFilePaths.stream().filter(path -> filter.test(path.toString()))
                .peek(path -> LOGGER.info(path.toString())).map(Path::toString)
                .forEach(path -> adapterBuilder.addProvider(new BeanAdapterProvider(path)));

        adapter = adapterBuilder.setReturnValueAnnotationEnabled(true).build();

        return adapter.getAllIds().stream().flatMap(id -> {
            try {
                return Stream.of(adapter.get(type, id));
            } catch (IllegalArgumentException iae) {
                LOGGER.debug("Intentional, found type and requested type mismatch", iae);
                return Stream.empty();
            }
        }).peek(path -> LOGGER.debug(path.toString())).collect(Collectors.toSet());
    }

    private void loadPropertyConfigurations() {
        LOGGER.info("---> Loading properties");

        allConfigFilePaths.stream().filter(path -> path.getFileName().toString().endsWith(DOT + EXTENSION_PROPERTIES))
                .peek(path -> LOGGER.info("Loading property [{}]", path.toString())).forEach(path -> {
                    try {
                        addConfiguration(new Configurations().properties(path.toString()));
                    } catch (ConfigurationException e) {
                        LOGGER.error("Could not load configuration property [{}]", path.toString(), e);
                    }
                });
    }

    /**
     * Getter for the used configuration directory (provided or temp)
     * 
     * @return configuration root directory
     */
    public Path getConfigRoot() {
        return configRoot;
    }

    @SuppressWarnings("unchecked")
    public <T extends ConfigurationData> Optional<T> getConfigurationDataById(Class<T> type, String id) {
        return getConfiguraionDataById((Set<T>) allConfigurationData.get(type), id);
    }

    private static <T extends ConfigurationData> Optional<T> getConfiguraionDataById(Set<T> collection, String id) {
        return getConfigurationDataByPredicate(collection, configurationData -> id.equals(configurationData.getId()));
    }

    private static <T extends ConfigurationData> Optional<T> getConfigurationDataByPredicate(Set<T> collection,
            Predicate<T> filter) {
        return collection.stream().filter(filter).findFirst();
    }

    public Optional<MxeCluster> getMxeCluster(String id) {
        return testExecutionClusters.stream().filter(host -> host.getId().equals(id)).findFirst();
    }
}
