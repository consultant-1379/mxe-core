package config

import (
	"fmt"
	"os"
	"sync"

	argocdclient "github.com/argoproj/argo-cd/v2/pkg/apiclient"
	appsv1 "github.com/argoproj/argo-cd/v2/pkg/apis/application/v1alpha1"
	argoprojsync "github.com/argoproj/pkg/sync"
	"github.com/fsnotify/fsnotify"
	"github.com/sirupsen/logrus"
	"github.com/spf13/viper"
	"k8s.io/client-go/kubernetes"
)

const (
	// Constants for viper variable names. Will be used to set
	// default values as well as to get each value
	varLogLevel              = "log.level"
	varPathToConfig          = "config.path"
	dmConfigFileEnvKey       = "DM_CONFIG_FILE"
	committerInfoFileEnvKey  = "COMMIT_AUTHORS_CONFIG_FILE"
	configKey                = "authors"
	defaultCommitAuthorName  = "MXE Deployer"
	defaultCommitAuthorEmail = "mxedeployer@ericsson.com"
	defaultDomain            = "deployerDefault"
)

// ResourceStore loaded based on Config
type ResourceStore struct {
	RepositoryStoreMtx      sync.RWMutex
	RepositoryCredsStoreMtx sync.RWMutex
	RepositoryStore         map[string]appsv1.Repository
	RepositoryCredsStore    map[string]appsv1.RepoCreds
	RepoLocks               argoprojsync.KeyLock
	ArgoCDClientOptions     *argocdclient.ClientOptions
	ConfiguredCommitAuthors []CommitAuthor
	DefaultCommitAuthor     CommitAuthor
}

// Config struct
type Config struct {
	DMProps          *viper.Viper
	CommitAuthorInfo *viper.Viper
	Store            ResourceStore
	client           *kubernetes.Clientset
	argoCDNamespace  string
}

type CommitAuthor struct {
	Domain string
	Name   string
	Email  string
}

func loadConfig(v *viper.Viper, configFileKey string) {
	configFilePath := getPathToConfig(configFileKey)
	v.SetDefault(varLogLevel, "info")
	v.AutomaticEnv()
	v.SetTypeByDefaultValue(true)
	v.SetConfigFile(configFilePath)
	logrus.WithField(configFileKey, configFilePath).Warn("loading config")
	err := v.ReadInConfig()
	if err != nil {
		panic(fmt.Errorf("fatal error while reading the config file: %#v", err))
	}
}

func init() {
	setLogLevel()
}

func (conf *Config) unmarshalConfig() {
	conf.unmarshalClientOptions()
}

func (conf *Config) unmarshalCommitterInfoCfg() {
	var committers []CommitAuthor

	err := conf.CommitAuthorInfo.UnmarshalKey(configKey, &committers)
	if err != nil {
		logrus.Fatalf("failed to unmarshal argocd client options %#v", err)
	}

	conf.Store.ConfiguredCommitAuthors = committers

	logrus.Info("CommitAuthors information is set as per authors Config")
}

// New - creates a new configuration object and loads the config yaml
func New(client *kubernetes.Clientset, namespace string) *Config {
	conf := Config{
		DMProps:          viper.New(),
		CommitAuthorInfo: viper.New(),
		client:           client,
		argoCDNamespace:  namespace,
		Store: ResourceStore{
			RepoLocks: argoprojsync.NewKeyLock(),
			DefaultCommitAuthor: CommitAuthor{
				Domain: defaultDomain,
				Name:   defaultCommitAuthorName,
				Email:  defaultCommitAuthorEmail,
			},
			RepositoryStore:      make(map[string]appsv1.Repository),
			RepositoryCredsStore: make(map[string]appsv1.RepoCreds),
		},
	}

	loadConfig(conf.DMProps, dmConfigFileEnvKey)
	conf.unmarshalConfig()

	loadConfig(conf.CommitAuthorInfo, committerInfoFileEnvKey)
	conf.unmarshalCommitterInfoCfg()

	conf.DMProps.WatchConfig()
	conf.DMProps.OnConfigChange(func(e fsnotify.Event) {
		logrus.WithField("file", e.Name).Warn("\nConfig file changed")
		conf.unmarshalConfig()
	})

	conf.CommitAuthorInfo.WatchConfig()
	conf.CommitAuthorInfo.OnConfigChange(func(e fsnotify.Event) {
		logrus.WithField("file", e.Name).Warn("\nConfig file changed")
		conf.unmarshalCommitterInfoCfg()
	})

	return &conf
}

func getPathToConfig(key string) string {
	filePath, ok := os.LookupEnv(key)
	if !ok {
		logrus.Fatalf("Env property %s not found", key)
	}
	return filePath
}

func setLogLevel() {
	level, _ := getLogLevel()
	logrus.SetLevel(level)
}

func getLogLevel() (logrus.Level, error) {
	var level logrus.Level = logrus.InfoLevel

	strLevel, ok := os.LookupEnv("LOG_LEVEL")
	if !ok {
		return level, nil
	}
	level, err := logrus.ParseLevel(strLevel)
	if err != nil {
		return level, err
	}
	return level, err
}

func (c *Config) GetArgoCDNamespace() string {
	return c.argoCDNamespace
}
