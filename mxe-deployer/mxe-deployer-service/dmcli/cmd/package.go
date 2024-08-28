package cmd

import (
	"bufio"
	"context"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"strings"
	"text/tabwriter"
	"time"

	log "github.com/sirupsen/logrus"

	argocdclient "github.com/argoproj/argo-cd/v2/pkg/apiclient"
	"github.com/argoproj/argo-cd/v2/pkg/apiclient/application"
	applicationpkg "github.com/argoproj/argo-cd/v2/pkg/apiclient/application"
	"github.com/argoproj/argo-cd/v2/pkg/apis/application/v1alpha1"
	"github.com/argoproj/argo-cd/v2/util/errors"
	"github.com/argoproj/gitops-engine/pkg/health"
	"github.com/spf13/cobra"
	"k8s.io/kubectl/pkg/util/templates"
	dmclientpkg "mxe.ericsson/depmanager/dmserver/client"
	"mxe.ericsson/depmanager/dmserver/pkg/dmserver/deploy"
	fileUtils "mxe.ericsson/depmanager/utils/file"
)

// Print simple list of application names
func printApplicationNames(apps []v1alpha1.Application) {
	for _, app := range apps {
		fmt.Println(app.Name)
	}
}

// Print table of application data
func printApplicationTable(apps []v1alpha1.Application, output *string) {
	w := tabwriter.NewWriter(os.Stdout, 0, 0, 2, ' ', 0)
	var fmtStr string
	headers := []interface{}{"NAME", "CLUSTER", "NAMESPACE", "PROJECT", "STATUS", "HEALTH", "SYNCPOLICY", "CONDITIONS"}
	if *output == "wide" {
		fmtStr = "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n"
		headers = append(headers, "REPO", "PATH", "TARGET")
	} else {
		fmtStr = "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n"
	}
	_, _ = fmt.Fprintf(w, fmtStr, headers...)
	for _, app := range apps {
		vals := []interface{}{
			app.Name,
			app.Spec.Destination.Server,
			app.Spec.Destination.Namespace,
			app.Spec.GetProject(),
			app.Status.Sync.Status,
			app.Status.Health.Status,
			formatSyncPolicy(app),
			formatConditionsSummary(app),
		}
		if *output == "wide" {
			vals = append(vals, app.Spec.Source.RepoURL, app.Spec.Source.Path, app.Spec.Source.TargetRevision)
		}
		_, _ = fmt.Fprintf(w, fmtStr, vals...)
	}
	_ = w.Flush()
}

func formatSyncPolicy(app v1alpha1.Application) string {
	if app.Spec.SyncPolicy == nil || app.Spec.SyncPolicy.Automated == nil {
		return "<none>"
	}
	policy := "Auto"
	if app.Spec.SyncPolicy.Automated.Prune {
		policy = policy + "-Prune"
	}
	return policy
}

func formatConditionsSummary(app v1alpha1.Application) string {
	typeToCnt := make(map[string]int)
	for i := range app.Status.Conditions {
		condition := app.Status.Conditions[i]
		if cnt, ok := typeToCnt[condition.Type]; ok {
			typeToCnt[condition.Type] = cnt + 1
		} else {
			typeToCnt[condition.Type] = 1
		}
	}
	items := make([]string, 0)
	for cndType, cnt := range typeToCnt {
		if cnt > 1 {
			items = append(items, fmt.Sprintf("%s(%d)", cndType, cnt))
		} else {
			items = append(items, cndType)
		}
	}
	summary := "<none>"
	if len(items) > 0 {
		summary = strings.Join(items, ",")
	}
	return summary
}

func getInputFile(path string) (*fileUtils.InputFile, error) {
	file, err := os.Open(path)
	if err != nil {
		return nil, err
	}
	fileInfo, err := os.Stat(path)
	if err != nil {
		return nil, err
	}

	return &fileUtils.InputFile{
		Reader:   file,
		FileName: fileInfo.Name(),
		//Size:     fileInfo.Size(),
	}, nil

}

func validateFlags(errorMsg string, invalidCombinations ...bool) {
	for _, combination := range invalidCombinations {
		if combination {
			log.Fatalln(errorMsg)
		}
	}

}

func cleanup(filePaths ...string) {
	for _, filePath := range filePaths {
		if info, _ := os.Stat(filePath); info != nil {
			os.Remove(filePath)
		}
	}
}

func processInputFiles(manifestArchiveLocation, manifestDir string) (*fileUtils.InputFile, []string) {
	var filePathsToCleanup = []string{}
	var archiveFile *fileUtils.InputFile = nil
	var err error

	if manifestArchiveLocation != "" {
		archiveFile, err = getInputFile(manifestArchiveLocation)
		if err != nil {
			log.Fatalf("\nUnable to read archive file from %s. Error : %s", manifestArchiveLocation, err.Error())
		}
	} else if manifestDir != "" {
		tarArchiveName := fmt.Sprintf("%s.%s", filepath.Base(manifestDir), "tar.gz")
		tarArchive, err := os.Create(tarArchiveName)
		if err != nil {
			log.Fatalln("Error writing archive:", err)
		}
		filePathsToCleanup = append(filePathsToCleanup, tarArchiveName)

		err = fileUtils.Tar(manifestDir, tarArchive)
		if err != nil {
			log.Fatalln("Error writing archive:", err)
		}
		archiveFile, err = getInputFile(tarArchiveName)
		if err != nil {
			log.Fatalf("\nUnable to read process manifests from %s. Error : %s", manifestDir, err.Error())
		}
	}

	return archiveFile, filePathsToCleanup
}

var (
	appExample = templates.Examples(fmt.Sprintf(`
	# Deploy a system package.
	%s package create myapp OPTIONS

	# Patch a system package
	%s package patch myapp OPTIONS

	# List system packages
	%s package list -l SELECTOR
	`, cliName, cliName, cliName))
)

func NewPackageCommand(clientOpts *argocdclient.ClientOptions) *cobra.Command {
	var command = &cobra.Command{
		Use:     "package",
		Short:   "Manage system packages",
		Example: appExample,
		Run: func(c *cobra.Command, args []string) {
			c.HelpFunc()(c, args)
			os.Exit(1)
		},
	}
	command.AddCommand(NewPackageCreateCommand(clientOpts))
	command.AddCommand(NewPackageGetCommand(clientOpts))
	command.AddCommand(NewPackageListCommand(clientOpts))
	command.AddCommand(NewPackageWaitCommand(clientOpts))
	command.AddCommand(NewPackageDeleteCommand(clientOpts))
	command.AddCommand(NewPackagePatchCommand(clientOpts))
	command.AddCommand(NewPackageSyncCommand(clientOpts))
	return command

}

func NewPackageSyncCommand(clientOpts *argocdclient.ClientOptions) *cobra.Command {
	var appName string
	var revision string
	var prune bool
	var force bool

	var command = &cobra.Command{
		Use:   "sync",
		Short: "Sync package asynchronously",
		Example: fmt.Sprintf(`  
		# Sync app
		%s package sync <appName>

		`, cliName),
		Run: func(c *cobra.Command, args []string) {

			if len(args) == 1 {
				appName = args[0]
				if appName == "" {
					log.Fatalf("invalid name argument '%s'", appName)
				}
			} else {
				log.Println("APPNAME is not supplied, see Usage")
				c.HelpFunc()(c, args)
				os.Exit(1)
			}

			dmClient := dmclientpkg.NewClientOrDie(clientOpts)

			packageEps, err := dmClient.PackageClient()
			if err != nil {
				log.Fatalf("Unable to retrieve package endpoints due to error: %#v", err)
			}

			appSyncReq := application.ApplicationSyncRequest{
				Name:  &appName,
				Prune: &prune,
			}

			if revision != "" {
				appSyncReq.Revision = &revision
			}

			if force {
				appSyncReq.Strategy = &v1alpha1.SyncStrategy{Hook: &v1alpha1.SyncStrategyHook{}}
				appSyncReq.Strategy.Hook.Force = force
			}

			response, err := packageEps.SyncPackage(context.Background(), &appSyncReq)
			if err != nil {
				log.Fatalf("Sync Request failed with error: %s", err.Error())
			}

			if response.Err != nil {
				log.Fatalf("Sync Request failed with error: %s", response.Err.Error())
			}
			fmt.Println("Sync for App:", appName, " has been initiated successfully")
		},
	}
	command.Flags().BoolVar(&prune, "prune", false, "Allow deleting unexpected resources")
	command.Flags().StringVar(&revision, "revision", "", "Sync to a specific revision. Preserves parameter overrides")
	command.Flags().BoolVar(&force, "force", false, "Use a force apply")

	return command
}

func getApp(clientOpts *argocdclient.ClientOptions, appName string) *v1alpha1.ApplicationList {
	dmClient := dmclientpkg.NewClientOrDie(clientOpts)

	packageEps, err := dmClient.PackageClient()
	if err != nil {
		log.Fatalf("Unable to retrieve package endpoints due to error: %#v", err)
	}
	appSelector := applicationpkg.ApplicationQuery{Name: &appName}

	response, err := packageEps.GetPackages(context.Background(), &appSelector)
	if err != nil {
		log.Fatalf("Get Package Request failed with error: %s", err.Error())
	}

	if response.Err != nil {
		log.Fatalf("Get Package Request failed with error: %s", response.Err.Error())
	}
	return response.ApplicationsList
}

type AppStatus struct {
	SyncStatusCode    v1alpha1.SyncStatusCode
	HealthStatusCode  health.HealthStatusCode
	ConditionsSummary string
	OperationState    *v1alpha1.OperationState
}

func (appStatus *AppStatus) IsSyncedAndHealthy() bool {
	return appStatus.SyncStatusCode == v1alpha1.SyncStatusCodeSynced &&
		appStatus.HealthStatusCode == health.HealthStatusHealthy &&
		appStatus.ConditionsSummary == "<none>" &&
		appStatus.OperationState.Phase.Successful()
}

func printOperationResult(opState *v1alpha1.OperationState) {
	const printOpFmtStr = "\t%-20s%s\n"

	if opState == nil {
		return
	}
	if opState.SyncResult != nil {
		fmt.Printf(printOpFmtStr, "Operation:", "Sync")
		fmt.Printf(printOpFmtStr, "Sync Revision:", opState.SyncResult.Revision)
	}
	fmt.Printf(printOpFmtStr, "Phase:", opState.Phase)
	fmt.Printf(printOpFmtStr, "Start:", opState.StartedAt)
	fmt.Printf(printOpFmtStr, "Finished:", opState.FinishedAt)
	var duration time.Duration
	if !opState.FinishedAt.IsZero() {
		duration = time.Second * time.Duration(opState.FinishedAt.Unix()-opState.StartedAt.Unix())
	} else {
		duration = time.Second * time.Duration(time.Now().UTC().Unix()-opState.StartedAt.Unix())
	}
	fmt.Printf(printOpFmtStr, "Duration:", duration)
	if opState.Message != "" {
		fmt.Printf(printOpFmtStr, "Message:", opState.Message)
	}
}

func checkSyncStatus(clientOpts *argocdclient.ClientOptions, appName string) AppStatus {

	app := getApp(clientOpts, appName).Items[0]
	return AppStatus{
		SyncStatusCode:    app.Status.Sync.Status,
		HealthStatusCode:  app.Status.Health.Status,
		ConditionsSummary: formatConditionsSummary(app),
		OperationState:    app.Status.OperationState,
	}
}

func NewPackageWaitCommand(clientOpts *argocdclient.ClientOptions) *cobra.Command {
	var appName string
	var statusCheckInterval uint
	var noOfIterations uint
	const (
		defaultTickerSeconds  = 10
		defaultNoOfIterations = 5
		timeLayout            = time.RFC3339
		printFmtStr           = "%-20s%s\n"
	)

	var command = &cobra.Command{
		Use:   "wait",
		Short: "Wait for application to get synced/healthy",
		Example: fmt.Sprintf(`  
		# Get app mxe-serving and check status every 60s for maximum of 10 times
		%s package wait mxe-serving --check-interval 60 --max-attempts 10
		
		# Get app mxe-serving and check status every 60s for maximum of 10 times (using shortnames)
		%s package wait mxe-serving -i 60 -n 10
		
		`, cliName, cliName),
		Run: func(c *cobra.Command, args []string) {

			if len(args) == 1 {
				appName = args[0]
				if appName == "" {
					log.Fatalf("invalid name argument '%s'", appName)
				}
			} else {
				log.Println("APPNAME is not supplied, see Usage")
				c.HelpFunc()(c, args)
				os.Exit(1)
			}
			fmt.Println("Monitoring status of", appName)
			ticker := time.NewTicker(time.Duration(statusCheckInterval) * time.Second)
			done := make(chan bool)
			defer close(done)
			go func() {
				var i uint = 0
				for i = 0; i < noOfIterations; i++ {
					fmt.Println()
					fmt.Println()
					fmt.Println("Waiting for", statusCheckInterval, "seconds")
					t := <-ticker.C
					appStatus := checkSyncStatus(clientOpts, appName)
					fmt.Printf(printFmtStr, "Time:", t.Format(timeLayout))
					fmt.Printf(printFmtStr, "Iteration", fmt.Sprintf("%d", (i+1)))
					fmt.Printf(printFmtStr, "Application:", appName)
					fmt.Printf(printFmtStr, "SyncStatus:", appStatus.SyncStatusCode)
					fmt.Printf(printFmtStr, "HealthStatus:", appStatus.HealthStatusCode)
					fmt.Printf(printFmtStr, "Conditions:", appStatus.ConditionsSummary)
					fmt.Println("OperationState:")
					printOperationResult(appStatus.OperationState)

					if appStatus.IsSyncedAndHealthy() {
						fmt.Printf("\n\n Application %s is synced and healthy\n", appName)
						done <- true
					}
				}
				done <- false
			}()

			status := <-done
			ticker.Stop()
			if !status {
				log.Fatalln("Application", appName, "is not yet synced and healthy")
			}
		},
	}
	command.Flags().UintVarP(&statusCheckInterval, "check-interval", "i", defaultTickerSeconds, "the interval at which the status should be checked")
	command.Flags().UintVarP(&noOfIterations, "max-attempts", "n", defaultNoOfIterations, "maximum number of times status should be checked before timing out")

	return command
}

func NewPackageGetCommand(clientOpts *argocdclient.ClientOptions) *cobra.Command {
	var appName string
	var output string

	var command = &cobra.Command{
		Use:   "get",
		Short: "Get an application",
		Example: fmt.Sprintf(`  
		# Get app mxe-serving
		%s package get mxe-serving`, cliName),
		Run: func(c *cobra.Command, args []string) {

			if len(args) == 1 {
				appName = args[0]
				if appName == "" {
					log.Fatalf("invalid name argument '%s'", appName)
				}
			} else {
				log.Println("APPNAME is not supplied, see Usage")
				c.HelpFunc()(c, args)
				os.Exit(1)
			}

			appList := getApp(clientOpts, appName).Items
			switch output {
			case "yaml", "json":
				err := PrintResourceList(appList, output, true)
				errors.CheckError(err)
			case "wide", "":
				printApplicationTable(appList, &output)
			default:
				errors.CheckError(fmt.Errorf("unknown output format: %s", output))
			}

		},
	}
	command.Flags().StringVarP(&output, "output", "o", "yaml", "Output format. One of: wide|json|yaml")
	return command
}

func NewPackageListCommand(clientOpts *argocdclient.ClientOptions) *cobra.Command {
	var selector string
	var output string

	var command = &cobra.Command{
		Use:   "list",
		Short: "List applications",
		Example: fmt.Sprintf(`  
		# List all apps
		%s package list

		# List apps by label
		%s package list -l packageType=system`, cliName, cliName),
		Run: func(c *cobra.Command, args []string) {
			dmClient := dmclientpkg.NewClientOrDie(clientOpts)

			packageEps, err := dmClient.PackageClient()
			if err != nil {
				log.Fatalf("Unable to retrieve package endpoints due to error: %#v", err)
			}
			appSelector := applicationpkg.ApplicationQuery{Selector: &selector}

			response, err := packageEps.GetPackages(context.Background(), &appSelector)
			if err != nil {
				log.Fatalf("List Package Request failed with error: %s", err.Error())
			}

			if response.Err != nil {
				log.Fatalf("List Package Request failed with error: %s", response.Err.Error())
			}
			appList := response.ApplicationsList.Items
			switch output {
			case "yaml", "json":
				err := PrintResourceList(appList, output, false)
				errors.CheckError(err)
			case "name":
				printApplicationNames(appList)
			case "wide", "":
				printApplicationTable(appList, &output)
			default:
				errors.CheckError(fmt.Errorf("unknown output format: %s", output))
			}

		},
	}
	command.Flags().StringVarP(&selector, "selector", "l", "", "List apps by label")
	command.Flags().StringVarP(&output, "output", "o", "wide", "Output format. One of: wide|name|json|yaml")
	return command
}

func getUserConfirmation(s string, tries int, in io.Reader) (bool, bool) {
	r := bufio.NewReader(in)
	for ; tries > 0; tries-- {
		fmt.Printf("%s [Enter y/n]: ", s)

		res, err := r.ReadString('\n')
		if err != nil {
			log.Fatal(err)
		}

		input := strings.ToLower(strings.TrimSpace(res))

		if len(input) != 1 {
			fmt.Println("Incorrect input received. Valid inputs : y or n")
			continue
		}
		// user gave a single char input within `tries`
		return input[0] == 'y', true
	}
	// user gave only invalid tries even after n attempts
	return false, false
}

func NewPackageDeleteCommand(clientOpts *argocdclient.ClientOptions) *cobra.Command {

	var (
		appName           string
		quiet             bool
		propagationPolicy string
	)
	var command = &cobra.Command{
		Use:   "delete APPNAME",
		Short: "Delete applications",
		Example: fmt.Sprintf(`  
		# Delete an app by name
		%s package delete appName

		# Delete an app by name using specific propagation policy
		%s package delete appName -p background
		`, cliName, cliName),
		Run: func(c *cobra.Command, args []string) {
			if len(args) == 1 {
				appName = args[0]
				if appName == "" {
					log.Fatalf("invalid name argument '%s'", appName)
				}
			} else {
				log.Println("APPNAME is not supplied, see Usage")
				c.HelpFunc()(c, args)
				os.Exit(1)
			}
			dmClient := dmclientpkg.NewClientOrDie(clientOpts)
			deployerServer, err := dmClient.GetProperty(serverAddr)
			if err != nil {
				log.Fatalf("Unable to retrieve deployer server details. Retry after logging into deployer")
			}

			if !quiet {
				confirmationQuestion := fmt.Sprintf("Do you really want to delete %s from server %s", appName, deployerServer)
				canDelete, validInput := getUserConfirmation(confirmationQuestion, 3, os.Stdin)
				if !validInput {
					fmt.Println("Max Retries exceeded. Try again")
					os.Exit(0)
				}
				if !canDelete {
					fmt.Printf("\nThe command to delete %s was cancelled", appName)
					os.Exit(0)
				}
			}

			if propagationPolicy != "background" && propagationPolicy != "foreground" {
				log.Fatalf("Propogation policy cannot be set to %s. It can only be one of foreground|background", propagationPolicy)
			}

			packageEps, err := dmClient.PackageClient()
			if err != nil {
				log.Fatalf("Unable to retrieve package endpoints due to error: %#v", err)
			}

			response, err := packageEps.DeletePackage(context.Background(), &appName, &propagationPolicy)
			if err != nil {
				log.Fatalf("Delete Package Request failed with error: %s", err.Error())
			}

			if response.Err != nil {
				log.Fatalf("Delete Package Request failed with error: %s", response.Err.Error())
			}

			fmt.Printf("\n Successfully deleted %s", appName)

		},
	}
	command.Flags().BoolVar(&quiet, "quiet", false, "Runs delete without asking for confirmation if set")
	command.Flags().StringVarP(&propagationPolicy, "propagation-policy", "p", "foreground", "Specify propagation policy for deletion of application's resources. One of: foreground|background")

	return command
}

func NewPackagePatchCommand(clientOpts *argocdclient.ClientOptions) *cobra.Command {
	var (
		appName, manifestArchiveLocation, manifestDir string
	)
	var archiveFile *fileUtils.InputFile = nil

	var command = &cobra.Command{
		Use:   "patch APPNAME",
		Short: "Patch deployed package",
		Example: fmt.Sprintf(`
		# Patch a deployed manifest app with updated manifest archive
		%s package patch my-app --manifest-archive /home/mxeuser/data/my-app-updated.<zip/tar.gz/tgz>

		# Patch a deployed manifest app with updated manifests from a local directory
		%s package patch my-app --manifest-dir /home/mxeuser/data/my-app-updated/
		`, cliName, cliName),
		Run: func(c *cobra.Command, args []string) {
			var filePathsToCleanup = []string{}

			if len(args) == 1 {
				appName = args[0]
			} else {
				log.Println("APPNAME is not supplied. See Usage")
				c.HelpFunc()(c, args)
				os.Exit(1)
			}
			dmClient := dmclientpkg.NewClientOrDie(clientOpts)

			packageEps, err := dmClient.PackageClient()
			if err != nil {
				log.Fatalf("Unable to create package endpoints due to error: %#v", err)
			}

			appQuery := applicationpkg.ApplicationQuery{
				Name: &appName,
			}

			archiveFile, filePathsToCleanup = processInputFiles(manifestArchiveLocation, manifestDir)
			defer cleanup(filePathsToCleanup...)

			response, err := packageEps.PatchPackage(context.Background(), &appQuery, archiveFile)
			if err != nil {
				log.Fatalf("Package Create Request failed with error: %s", err.Error())
			}

			if response.Err != nil {
				log.Fatalf("Package Create Request failed with error: %s", response.Err.Error())
			}

			fmt.Printf("Application %s patched successfully", appName)

		},
	}
	command.Flags().StringVar(&manifestArchiveLocation, "manifest-archive", "", "Manifest archive containing updated manifests")
	command.Flags().StringVar(&manifestDir, "manifest-dir", "", "Manifest dir containing updated manifests")

	err := command.Flags().SetAnnotation("manifest-archive", cobra.BashCompFilenameExt, []string{"zip", "tar.gz", "tgz"})
	if err != nil {
		log.Fatal(err)
	}

	return command
}

type syncOptions struct {
	syncPolicy  string
	syncOptions []string
	autoPrune   bool
	selfHeal    bool
	allowEmpty  bool
}

func (s *syncOptions) getSyncPolicy() *v1alpha1.SyncPolicy {
	var appSyncPolicy *v1alpha1.SyncPolicy = &v1alpha1.SyncPolicy{}

	switch s.syncPolicy {
	case "none":
		appSyncPolicy.Automated = nil
		if appSyncPolicy.IsZero() {
			appSyncPolicy = nil
		}
	case "automated", "automatic", "auto":
		appSyncPolicy.Automated = &v1alpha1.SyncPolicyAutomated{}
	default:
		log.Fatalf("Invalid sync-policy: %s", s.syncPolicy)
	}

	if s.syncOptions != nil {
		if appSyncPolicy == nil {
			appSyncPolicy = &v1alpha1.SyncPolicy{}
		}
	}
	for _, option := range s.syncOptions {
		// `!` means remove the option
		if strings.HasPrefix(option, "!") {
			option = strings.TrimPrefix(option, "!")
			appSyncPolicy.SyncOptions = appSyncPolicy.SyncOptions.RemoveOption(option)
		} else {
			appSyncPolicy.SyncOptions = appSyncPolicy.SyncOptions.AddOption(option)
		}
	}

	if s.autoPrune {
		if appSyncPolicy == nil || appSyncPolicy.Automated == nil {
			log.Fatal("Cannot set --auto-prune: application not configured with automatic sync")
		}
		appSyncPolicy.Automated.Prune = s.autoPrune
	}
	if s.selfHeal {
		if appSyncPolicy == nil || appSyncPolicy.Automated == nil {
			log.Fatal("Cannot set --self-heal: application not configured with automatic sync")
		}
		appSyncPolicy.Automated.SelfHeal = s.selfHeal
	}
	if s.allowEmpty {
		if appSyncPolicy == nil || appSyncPolicy.Automated == nil {
			log.Fatal("Cannot set --allow-empty: application not configured with automatic sync")
		}
		appSyncPolicy.Automated.AllowEmpty = s.allowEmpty
	}

	return appSyncPolicy
}

// NewPackageCreateCommand returns a new instance of an `mxe-deploy package create` command
func NewPackageCreateCommand(clientOpts *argocdclient.ClientOptions) *cobra.Command {
	var (
		appName, repo, appPath, revision, destServer, manifestDir string
		destNamespace, destClusterName, manifestArchiveLocation   string
		labels                                                    []string
		syncParams                                                *syncOptions = &syncOptions{}
		annotations                                               []string
		initSync                                                  bool
	)
	var msgForInvalidFlagCombos = `Illegal combination of flags detected.
	Usage modes:
		a) Deploy k8s manifest zip/tar.gz/tgz archive using
					--manifest-archive <file.zip or tar.gz or .tgz>
		b) Deploy k8s manifests from dir using
					--manifest-dir <path-to-dir>`

	var command = &cobra.Command{
		Use:   "create APPNAME",
		Short: "Deploy a package",
		Example: fmt.Sprintf(`
		# Deploy manifests from an archive
		%s package create myapp --repo https://<git-repo-url>.git --path <appManifest-folder-to-be-created> --revision master --dest-namespace appln --dest-server https://kubernetes.default.svc --label managedBy=dmserver --manifest-archive /home/mxeuser/data/myarchive.<zip/tar.gz/.tgz>

		# Deploy manifests from an archive
		%s package create myapp --repo https://<git-repo-url>.git --path <appManifest-folder-to-be-created> --revision master --dest-namespace appln --dest-server https://kubernetes.default.svc --label managedBy=dmserver --manifest-dir /home/mxeuser/data/myapp/

		# Deploy directly from git source
		%s package create myapp --repo https://<git-repo-url>.git --path <appManifest-folder-already-checked-in> --revision master --dest-namespace appln --dest-server https://kubernetes.default.svc --label managedBy=dmserver
		`, cliName, cliName, cliName),
		Run: func(c *cobra.Command, args []string) {
			var archiveFile *fileUtils.InputFile = nil
			var err error
			var filePathsToCleanup = []string{}

			if len(args) >= 1 {
				appName = args[0]
			} else {
				log.Error("APPNAME is not supplied, see Usage")
				c.HelpFunc()(c, args)
				os.Exit(1)
			}

			invalidConditions := []bool{
				//Both manifest dir and archive are given
				manifestDir != "" && manifestArchiveLocation != "",
			}
			validateFlags(msgForInvalidFlagCombos, invalidConditions...)

			archiveFile, filePathsToCleanup = processInputFiles(manifestArchiveLocation, manifestDir)
			defer cleanup(filePathsToCleanup...)

			dmClient := dmclientpkg.NewClientOrDie(clientOpts)

			packageEps, err := dmClient.PackageClient()
			if err != nil {
				log.Fatalf("Unable to create package endpoints due to error: %#v", err)
			}

			destination := v1alpha1.ApplicationDestination{Namespace: destNamespace}
			if destServer != "" {
				destination.Server = destServer
			} else if destClusterName != "" {
				destination.Name = destClusterName
			}

			packageOptions := deploy.PackageRequestMeta{
				ApplicationName: appName,
				Source: v1alpha1.ApplicationSource{
					RepoURL:        repo,
					Path:           appPath,
					TargetRevision: revision,
				},
				Destination: destination,
				Labels:      labels,
				Annotations: annotations,
				SyncPolicy:  syncParams.getSyncPolicy(),
				InitSync:    initSync,
			}

			response, err := packageEps.PostPackage(context.Background(), &packageOptions, archiveFile)
			if err != nil {
				log.Fatalf("Package Create Request failed with error: %s", err.Error())
			}

			if response.Err != nil {
				log.Fatalf("Package Create Request failed with error: %s", response.Err.Error())
			}

			fmt.Printf("Application %s created successfully", appName)
		},
	}
	command.Flags().StringVar(&appPath, "path", "", "Relative Path within gitops repo where App manifests should be checked in")
	command.Flags().StringVar(&repo, "repo", "", "GitOps Repository URL")
	command.Flags().StringVar(&revision, "revision", "", "Source branch the application would sync to")
	command.Flags().StringVar(&destServer, "dest-server", "", "K8s Cluster URL where the application has to be deployed")
	command.Flags().StringVar(&destNamespace, "dest-namespace", "", "K8s target namespace")
	command.Flags().StringVar(&destClusterName, "dest-name", "", "K8s Cluster name")
	command.Flags().StringVar(&manifestArchiveLocation, "manifest-archive", "", "zip/tar.gz/.tgz archive file containing manifests to be deployed")
	command.Flags().StringVar(&manifestDir, "manifest-dir", "", "Dir containing k8s resource manifests to be deployed")
	command.Flags().StringArrayVarP(&labels, "label", "l", []string{}, "Labels to apply to the app")
	command.Flags().StringArrayVarP(&annotations, "annotation", "a", []string{}, "Annotations to apply to the app")
	command.Flags().StringVar(&syncParams.syncPolicy, "sync-policy", "none", "Set the sync policy (one of: none, automated (aliases of automated: auto, automatic))")
	command.Flags().StringArrayVar(&syncParams.syncOptions, "sync-option", []string{}, "Add or remove a sync options, e.g add `Prune=false`. Remove using `!` prefix, e.g. `!Prune=false`")
	command.Flags().BoolVar(&syncParams.autoPrune, "auto-prune", false, "Set automatic pruning when sync is automated")
	command.Flags().BoolVar(&syncParams.selfHeal, "self-heal", false, "Set self healing when sync is automated")
	command.Flags().BoolVar(&syncParams.allowEmpty, "allow-empty", false, "Set allow zero live resources when sync is automated")
	command.Flags().BoolVar(&initSync, "init-sync", false, "If set sync argocd app immeditately after deployment when sync is manual")

	err := command.Flags().SetAnnotation("manifest-archive", cobra.BashCompFilenameExt, []string{"zip", "tar.gz", "tgz"})
	if err != nil {
		log.Fatal(err)
	}
	return command
}
