package main

import (
	"flag"
	"fmt"
	"net/http"
	"os"
	"os/signal"
	"syscall"

	"github.com/argoproj/argo-cd/v2/util/cli"
	"github.com/go-kit/kit/log"
	"github.com/go-kit/kit/log/level"

	"k8s.io/client-go/kubernetes"
	_ "k8s.io/client-go/plugin/pkg/client/auth/azure"
	_ "k8s.io/client-go/plugin/pkg/client/auth/gcp"
	_ "k8s.io/client-go/plugin/pkg/client/auth/oidc"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/clientcmd"
	cluster "mxe.ericsson/depmanager/dmserver/pkg/dmserver/cluster"
	deploy "mxe.ericsson/depmanager/dmserver/pkg/dmserver/deploy"
	session "mxe.ericsson/depmanager/dmserver/pkg/dmserver/session"
	"mxe.ericsson/depmanager/utils/config"
	httputil "mxe.ericsson/depmanager/utils/http"
)

func getKubeClientSet(kubeconfig *string) (*kubernetes.Clientset, error) {
	var config *rest.Config
	var err error
	if _, incluster := os.LookupEnv("KUBERNETES_SERVICE_HOST"); incluster == true {
		config, err = rest.InClusterConfig()
	} else {
		config, err = clientcmd.BuildConfigFromFlags("", *kubeconfig)
	}

	if err != nil {
		panic(err.Error())
	}
	kubeClientSet, err := kubernetes.NewForConfig(config)
	if err != nil {
		return nil, err
	}
	return kubeClientSet, nil
}

func getHome() string {
	home, err := os.UserHomeDir()
	if err != nil {
		panic("User home is not defined")
	}
	return home
}

func main() {

	var (
		httpAddr   = flag.String("http.addr", ":7543", "HTTP listen address")
		kubeconfig = flag.String("kubeconfig", fmt.Sprintf("%s/.kube/config", getHome()), "absolute path to the kubeconfig file")
		namespace  = flag.String("namespace", "argocd", "argocd installation namespace")
		debug      = flag.Bool("debug", true, "debug")
	)
	flag.Parse()

	kubeClientSet, err := getKubeClientSet(kubeconfig)
	if err != nil {
		println(err)
	}

	var logger, httpLogger log.Logger
	{
		logger = log.NewLogfmtLogger(os.Stderr)
		logger = log.With(logger, "ts", log.DefaultTimestampUTC)
		if *debug {
			logger = level.NewFilter(logger, level.AllowDebug())
			cli.SetLogLevel("debug")
		} else {
			logger = level.NewFilter(logger, level.AllowInfo())

		}
		logger = log.With(logger, "caller", log.DefaultCaller)
		httpLogger = log.With(logger, "transport", "http")
	}

	var conf *config.Config = config.New(kubeClientSet, *namespace)
	repoCh := make(chan struct{})
	repoCredCh := make(chan struct{})
	go conf.StartWatchingArgoCDRepositories(repoCh)
	go conf.StartWatchingArgoCDRepoCreds(repoCredCh)

	r, options := httputil.NewRouter(logger)

	//deplye service
	var deployService deploy.Service
	{
		deployService = deploy.New(conf, logger)
		deployService = deploy.LoggingMiddleware(logger)(deployService)
	}
	deployEndpoints := deploy.MakeServerEndpoints(deployService)
	deploy.RegisterHTTPHandlers(r, deployEndpoints, options...)

	var sessionService session.Service
	{
		sessionService = session.New(conf, logger)
		sessionService = session.LoggingMiddleware(logger)(sessionService)
	}
	sessionEndpoints := session.MakeServerEndpoints(sessionService)
	session.RegisterHTTPHandlers(r, sessionEndpoints, options...)
	//cluster service
	var clusterService cluster.Service
	clusterService = cluster.New(conf)
	clusterService = cluster.LoggingMiddleware(logger)(clusterService)

	clusterEndpoints := cluster.MakeServerEndpoints(clusterService)
	cluster.RegisterHTTPHandlers(r, clusterEndpoints, options...)

	var handler http.Handler
	if *debug {
		handler = httputil.LoggingMiddleware(logger)(r)
	} else {
		handler = r
	}
	handler = httputil.NewHTTPLogger(httpLogger).Middleware(handler)

	errs := make(chan error)
	go func() {
		c := make(chan os.Signal, 1)
		signal.Notify(c, syscall.SIGINT, syscall.SIGTERM)
		errs <- fmt.Errorf("%s", <-c)
	}()

	go func() {
		logger.Log("transport", "HTTP", "addr", *httpAddr)
		errs <- http.ListenAndServe(*httpAddr, handler)
	}()

	logger.Log("exit", <-errs)
	close(repoCh)
	close(repoCredCh)
}
