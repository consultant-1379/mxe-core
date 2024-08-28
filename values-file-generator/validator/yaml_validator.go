package validator

import (
	"context"
	"fmt"
	"regexp"
	"sync"

	"net/mail"
	"net/url"
	"os"
	"os/exec"
	"reflect"
	"strings"

	"github.com/jedib0t/go-pretty/v6/table"
	"gopkg.in/yaml.v3"
)

var namespace string
var isDeployer bool
var show_success bool
var result_map = make(map[string]string)
var mutex = &sync.RWMutex{}
var result_count_map = map[string]int{"success": 0, "fail": 0, "warn": 0}

// functions is a map of test functions for each parameter in the config.yaml
// When new parameter is added in config.yaml , a corresponding test function should be added in this map.
// The  name of the function should be the name of the parameter in the yaml from the root level with _ as separator.
// For example if the parameter is api.mxe.host the function name should be api_mxe_host.
// The script will throw a warning when a function is not defined for a parameter.
var functions = map[string]interface{}{

	"encrypted_storage_class":                    encrypted_storage_class,
	"cluster_wide_ingress_class_name":            cluster_wide_ingress_class_name,
	"docker_registry_host":                       docker_registry_host,
	"docker_registry_mn_host":                    docker_registry_mn_host,
	"docker_registry_secret":                     docker_registry_secret,
	"docker_registry_ca_secret_name":             docker_registry_ca_secret_name,
	"bro_service_name":                           bro_service_name,
	"bro_service_port":                           bro_service_port,
	"bro_label_key":                              bro_label_key,
	"mxe_deployer_external_gitops_domain":        mxe_deployer_external_gitops_domain,
	"mxe_deployer_external_gitops_url":           mxe_deployer_external_gitops_url,
	"mxe_deployer_gitops_repo_creds":             mxe_deployer_gitops_repo_creds,
	"mxe_deployer_commit_author_name":            mxe_deployer_commit_author_name,
	"mxe_deployer_commit_author_email":           mxe_deployer_commit_author_email,
	"mxe_deployer_argocd_admin_creds":            mxe_deployer_argocd_admin_creds,
	"mxe_deployer_internal_gitops_enabled":       mxe_deployer_internal_gitops_enabled,
	"mxe_deployer_argocd_redis_ha":               mxe_deployer_argocd_redis_ha,
	"storage_class":                              storage_class,
	"external_pypi_server":                       external_pypi_server,
	"api_mxe_host":                               api_mxe_host,
	"api_mxe_tlsSecretName":                      api_mxe_tlsSecretName,
	"api_deployer_tlsSecretName":                 api_deployer_tlsSecretName,
	"api_deployer_host":                          api_deployer_host,
	"api_oauth_host":                             api_oauth_host,
	"api_oauth_tlsSecretName":                    api_oauth_tlsSecretName,
	"api_argocd_host":                            api_argocd_host,
	"api_argocd_tlsSecretName":                   api_argocd_tlsSecretName,
	"api_gitea_host":                             api_gitea_host,
	"api_gitea_tlsSecretName":                    api_gitea_tlsSecretName,
	"mxe_serving_seldon_metrics_port":            mxe_serving_seldon_metrics_port,
	"mxe_serving_model_service_owasp":            mxe_serving_model_service_owasp,
	"mxe_serving_seldon_webhook_port":            mxe_serving_seldon_webhook_port,
	"isExtCA":                                    isExtCA,
	"iam_ca_secret_name":                         iam_ca_secret_name,
	"hostNetwork":                                hostNetwork,
	"appArmorProfile_type":                       appArmorProfile_type,
	"seccompProfile_type":                        seccompProfile_type,
	"mxe_commons_rate_limit_enabled":             mxe_commons_rate_limit_enabled,
	"mxe_commons_encryption_enable_in_transit":   mxe_commons_encryption_enable_in_transit,
	"mxe_commons_service_mesh_version":           mxe_commons_service_mesh_version,
	"mxe_commons_service_mesh_namespace":         mxe_commons_service_mesh_namespace,
	"mxe_commons_legal_warning_message":          mxe_commons_legal_warning_message,
	"mxe_commons_mxe_admin_secret":               mxe_commons_mxe_admin_secret,
	"mxe_commons_mxe_user_secret":                mxe_commons_mxe_user_secret,
	"mxe_commons_container_registry_volume_size": mxe_commons_container_registry_volume_size,
	"mxe_commons_enable_gatekeeper_ha":           mxe_commons_enable_gatekeeper_ha,
	"mxe_exploration_singleUserProfileList":      mxe_exploration_singleUserProfileList,
}

// execute_tests executes the testcase for each parameter in the config.yaml
// It uses go routines to execute the tests parallely.
// It stores the results in the result_map and also prints the results in tabular format.
func Execute_tests(yaml_file string, ns string, deployer bool, showsuccess bool) bool {
	namespace = ns
	isDeployer = deployer
	show_success = showsuccess
	test_names := get_test_names(yaml_file)
	result := check_if_valid_cluster()
	if result == false {
		return false
	}
	var wg sync.WaitGroup
	wg.Add(len(test_names))
	for _, test := range test_names {
		go execute_func(&wg, test)
	}
	wg.Wait()

	print_results()
	if showsuccess {
		if result_count_map["success"] > 0 {
			fmt.Printf("%v of parameters validated successfully\n", result_count_map["success"])
		}
	}
	if result_count_map["fail"] > 0 {
		fmt.Printf("%v of parameters failed validation\n", result_count_map["fail"])
	}
	if result_count_map["warn"] > 0 {
		fmt.Printf("%v of parameters have warnings\n", result_count_map["warn"])
	}

	if result_count_map["fail"] > 0 {
		return false
	}
	return true
}

// check_if_valid_cluster verifies if the namespace exists in the cluster
func check_if_valid_cluster() bool {
	_, ret_code := exec_command("kubectl get ns |grep -w " + namespace)
	if ret_code == false {
		fmt.Println("Error Namespace " + namespace + " not present. Verify the cluster once")
		return false
	}
	return true
}

// execute_func executes the test case functions. This is called using go routine from execute_tests function
func execute_func(wg *sync.WaitGroup, test string) {
	defer wg.Done()
	test_function_name := strings.Replace(test, "-", "_", -1)
	parameter := strings.Replace(test, "-", ".", -1)
	test_function := reflect.ValueOf(functions[test_function_name])
	ret_value := get_value(test)
	param_value := make([]reflect.Value, 1)
	param_value[0] = reflect.ValueOf(ret_value)
	if test_function.Kind() == reflect.Func {
		ret_values := test_function.Call(param_value)
		ret_code := ret_values[0].String()
		ret_message := ret_values[1].String()

		if ret_code == "pass" {
			mutex.Lock()
			result_map[parameter] = "success|" + ret_message
			result_count_map["success"]++
			mutex.Unlock()
		} else if ret_code == "fail" {
			mutex.Lock()
			result_map[parameter] = "Fail|" + ret_message
			result_count_map["fail"]++
			mutex.Unlock()
		} else if ret_code == "warn" {
			mutex.Lock()
			result_map[parameter] = "Warn|" + ret_message
			result_count_map["warn"]++
			mutex.Unlock()
		}
	} else {
		mutex.Lock()
		result_map[parameter] = "Warning|Validate Function not available"
		result_count_map["warn"]++
		mutex.Unlock()
	}
}

// print_results displays the results in a tabular format in the console.
func print_results() {
	resultTable := table.NewWriter()
	resultTable.SetOutputMirror(os.Stdout)
	resultTable.AppendHeader(table.Row{"Test Name", "Result", "Message"})
	for key, value := range result_map {
		result := strings.Split(value, "|")
		if result[0] == "success" && show_success == false {
			continue
		}
		resultTable.AppendRows([]table.Row{
			{key, result[0], result[1]},
		})
	}
	resultTable.AppendSeparator()
	// resultTable.SetStyle(table.StyleLight)
	resultTable.Style().Options.SeparateRows = true
	resultTable.SortBy([]table.SortBy{
		{Name: "Result", Mode: table.Asc},
	})
	resultTable.SetAutoIndex(true)
	resultTable.SetColumnConfigs([]table.ColumnConfig{
		{
			Name:     "Message",
			WidthMax: 60,
		},
	})
	if result_count_map["fail"] != 0 || result_count_map["warn"] != 0 {
		resultTable.Render()
	}

}

// exec_command executes the given command in bash shell and returns the o/p and exit code .
func exec_command(command string) (string, bool) {
	cxt_bg := context.Background()

	cmd := exec.CommandContext(cxt_bg, "bash", "-c", command)

	out, err := cmd.CombinedOutput()
	output := strings.TrimRight(string(out), "\r\n")
	if err != nil {
		return err.Error(), false
	}

	return string(output), true
}

// check_true_false checks whether the given string is true or false.
func check_true_false(param_value string) bool {

	param_pattern := `true|false`
	param_regex := regexp.MustCompile(param_pattern)

	if param_regex.MatchString(param_value) {
		return true
	} else {
		return false
	}
}

// check_host_name verifies if the given host name is a valid hostname. A valid hostname should contain alphanumeric characters with or withour hypens separated by dots.
func check_host_name(param_value string) bool {
	param_pattern := `^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$`
	param_regex := regexp.MustCompile(param_pattern)

	if param_regex.MatchString(param_value) {
		return true
	} else {
		return false
	}
}

// check_if_vcluster is used to check if MXE is being installed in a vcluster environment. Based on this check certain parameter validation will be skipped.
func check_if_vcluster() bool {

	command := "kubectl config current-context|grep vcluster"
	_, ret_code := exec_command(command)
	if ret_code {
		return true
	} else {
		return false
	}
}

// check_tls_secret is used to verify the tls secrets in the config.yaml.
// A TLS Secret is valid if it satisfies below conditions
//   - A secret of that name exists
//   - The host name of the certificate in that secret matches with the corresponding hostname in config.yaml
//   - The certificate in the secret is valid for atleast next 7 days.
func check_tls_secret(param_value string, host_name string, cert_key string, check_host bool) (bool, string) {
	if param_value == "<nil>" {
		return false, "Secret Name cannot be empty"
	}
	check_if_exists := "kubectl get secrets -n " + namespace + " |grep -w " + param_value
	_, ret_code := exec_command(check_if_exists)
	if !ret_code {
		return false, "Secret does not exist"
	}
	get_cert_from_secret := "kubectl get secrets -n " + namespace + " " + param_value + " -o jsonpath='{.data." + cert_key + "}'|base64 -d > " + param_value + ".crt"
	out, ret_code := exec_command(get_cert_from_secret)
	if !ret_code {
		exec_command("rm -f " + param_value + ".crt")
		return false, "Unable to get certificate " + out
	}

	if check_host {
		check_hostname := "openssl x509 -checkhost " + host_name + " -noout -in ./" + param_value + ".crt | tr -d '\n'"
		host_out, host_ret_code := exec_command(check_hostname)

		if !host_ret_code {
			exec_command("rm -f " + param_value + ".crt")
			return false, "Error validating hostname" + host_out
		}

		host_regex := regexp.MustCompile(`does.*NOT.*match`)
		if host_regex.MatchString(host_out) {
			exec_command("rm -f " + param_value + ".crt")
			return false, host_out
		}

	}

	check_expiry := "openssl x509 -checkend 604800  -noout -in ./" + param_value + ".crt"

	exp_out, exp_ret_code := exec_command(check_expiry)

	if !exp_ret_code {
		exec_command("rm -f " + param_value + ".crt")
		return false, "Error : " + exp_out
	}
	exec_command("rm -f " + param_value + ".crt")
	return true, "Validated host and expiry details of certificate in secret " + param_value
}

// storage_class is valid if
// - A storage of that name exists in the k8s cluster.
// - Or if is a vcluster the validation will be skipped
func storage_class(param_value string) (string, string) {
	command := "kubectl get  storageclasses.storage.k8s.io -o custom-columns=':metadata.name'|grep -w " + param_value
	result, status := exec_command(command)
	if !status {
		if check_if_vcluster() {
			return "warn", "Skipping since this is vcluster"
		}
		return "fail", "Storage class " + param_value + " not found"
	}

	return "pass", "Storage Class defined in config.yaml " + param_value + " is present in the cluster " + result

}

// external_pypi_server is valid if
// - it is not empty
func external_pypi_server(param_value string) (string, string) {
	if param_value != "<nil>" {
		return "pass", "Successfully validated external pypi server " + param_value + "."
	} else {
		return "fail", "External PyPi Server cannot be empty"
	}
}

// mxe_deployer_external_gitops_domain is valid if
// - if Deployer is not configures in which case this test will skip and return true
// - Or the value is nil when internal gitops is enabled
// - Or the value is a proper domain name when internal gitops is disabled.
func mxe_deployer_external_gitops_domain(param_value string) (string, string) {

	if !isDeployer {
		return "pass", "Skipping since deployer is not enabled"
	}

	internal_git_ops := get_value("mxe_deployer-internal_gitops-enabled")

	if param_value != "<nil>" {
		if internal_git_ops == "true" {
			return "fail", "External GITOps should be filled only if mxe_deployer.internal_gitops.enabled is set to false"
		} else {
			// param_pattern := `^[A-Za-z_].*` // dotted domain check - done
			// param_regex := regexp.MustCompile(param_pattern)

			status := check_host_name(param_value)

			if status {
				return "pass", "MXE Deployer external domain name  " + param_value + " validated successfully"
			} else {
				return "fail", "The mxe deployer domain should begin with alphabet character"
			}
		}

	} else {
		if internal_git_ops == "true" {
			return "pass", "External gitopts domain validated successfully"
		} else {
			return "fail", "External gitops domain cannot be empty when internal gitops is set to false"
		}
	}

}

// mxe_deployer_external_gitops_url is valid if
// - if Deployer is not configures in which case this test will skip and return true
// - Or if it is a valid url and internal git ops is set to false.
// - Or if it is nil value and internal git ops is set to true
func mxe_deployer_external_gitops_url(param_value string) (string, string) {
	if !isDeployer {
		return "pass", "Skipping since deployer is not enabled"
	}
	internal_git_ops := get_value("mxe_deployer-internal_gitops-enabled")

	if param_value != "<nil>" {
		if internal_git_ops == "true" {
			return "fail", "External GITOps should be filled only if mxe_deployer.internal_gitops.enabled is set to false"
		}

		_, err := url.ParseRequestURI(param_value)
		if err == nil {
			return "pass", "External GitOps URL " + param_value + " is valid."
		} else {
			return "fail", "External GitOPS url is not a valid URL , " + err.Error()
		}

	} else {
		if internal_git_ops == "false" {
			return "fail", "External GITOps should not be empty if mxe_deployer.internal_gitops.enabled is set to false"
		} else {
			return "pass", "External GitOPS is empty since mxe_deployer.internal_gitops.enabled . Validated Successfully"
		}
	}
}

// mxe_deployer_gitops_repo_creds is valid if
// - if Deployer is not configures in which case this test will skip and return true
// - Or if deployer is enabled and below conditions are satisfied,
//   - A secret of that name exists
//   - The secret has username,password and url in its data
//   - The URL is configured to api-gitea-host when internal git ops is true
//     Or configured to mxe_deployer.external_gitops.url when external git ops is used.
//   - The labels metadata has the field "argocd.argoproj.io/secret-type"  with value "repo-creds"
func mxe_deployer_gitops_repo_creds(param_value string) (string, string) {
	if !isDeployer {
		return "pass", "Skipping since deployer is not enabled"
	}

	message := ""
	if param_value != "<nil>" {
		command := "kubectl -n " + namespace + " get secrets -o jsonpath='{.data}' " + param_value
		result, status := exec_command(command)
		if !status {
			message = "Error: gitops_repo_creds Secret " + param_value + " not present"
			return "fail", message
		}

		if !strings.Contains(result, "username") {
			return "fail", "Error: Key username not present in secret " + param_value
		}

		if !strings.Contains(result, "password") {
			return "fail", "Error: Key password not present in secret " + param_value
		}

		if !strings.Contains(result, "url") {
			return "fail", "Error: Key url not present in secret " + param_value
		}

		url_command := "kubectl -n " + namespace + " get secrets -o jsonpath='{.data.url}' " + param_value + "|base64 -d"
		url_result, _ := exec_command(url_command)

		is_internal_gitops := get_value("mxe_deployer-internal_gitops-enabled")

		if is_internal_gitops == "true" {
			api_gitea_host_name := get_value("api-gitea-host")
			url := "https://" + api_gitea_host_name + "/mxe"
			if strings.TrimSuffix(url_result, "/") != url{
				return "fail", "The url should be " + url + " if internal git ops is enabled."
			}
		} else if is_internal_gitops == "false" {
			external_url := get_value("mxe_deployer-external_gitops-url")
			if !strings.Contains(external_url, url_result) {
				return "fail", "The url when external gitops is used must be taken from mxe_deployer.external_gitops.url"
			}
		}

		meta_data := "kubectl -n " + namespace + " get secrets -o jsonpath='{.metadata.labels.argocd\\.argoproj\\.io\\/secret-type}' " + param_value
		meta_result, _ := exec_command(meta_data)

		if !strings.Contains(meta_result, "repo-creds") {
			return "fail", "Error: labels metadata  not proper in secret " + param_value
		}

		return "pass", "Successfully validated secret " + param_value + "."

	} else {
		return "fail", "gitops repo creds parameter value is empty."
	}

}

// mxe_deployer_commit_author_name is valid if
// - if Deployer is not configures in which case this test will skip and return true
// - Or if deployer is enabled and a valid string with alphabets is given,
func mxe_deployer_commit_author_name(param_value string) (string, string) {
	if !isDeployer {
		return "pass", "Skipping since deployer is not enabled"
	}

	//validUsernamePattern   := regexp.MustCompile(`^[\da-zA-Z][-.\w]*$`)
	//invalidUsernamePattern := regexp.MustCompile(`[-._]{2,}|[-._]$`)  // check gitea username conditions - https://github.com/go-gitea/gitea/pull/20136
	pattern := regexp.MustCompile(`[A-Za-z].*`)
	//if validUsernamePattern.MatchString(param_value) && ! invalidUsernamePattern.MatchString(param_value){
	if pattern.MatchString(param_value) {
		return "pass", "MXE Deployer commit author name " + param_value + " validated successfully"
	} else {
		return "fail", "The Commit author name should begin with "
	}
}

// mxe_deployer_commit_author_email is valid if
// - if Deployer is not configures in which case this test will skip and return true
// - Or if deployer is enabled and a valid mail address is provided.
func mxe_deployer_commit_author_email(param_value string) (string, string) {
	if !isDeployer {
		return "pass", "Skipping since deployer is not enabled"
	}

	_, ret_err := mail.ParseAddress(param_value)
	if ret_err != nil {
		return "fail", "Email should be of the form <ID>@<domain>"
	}
	return "pass", "Commit author mail " + param_value + " validated successfully."
}

// mxe_deployer_argocd_admin_creds is valid if
// - if Deployer is not configures in which case this test will skip and return true
// - Or if deployer is enabled and below conditions are satisfied,
// - The secret of that name exists
// - It has the username and password defined in the data field
func mxe_deployer_argocd_admin_creds(param_value string) (string, string) {
	if !isDeployer {
		return "pass", "Skipping since deployer is not enabled"
	}

	message := ""
	// fmt.Println("Aro",param_value)
	if param_value != "<nil>" {
		command := "kubectl -n " + namespace + " get secrets -o jsonpath='{.data}' " + param_value
		result, status := exec_command(command)
		if !status {
			message = "Error: Argo CD Admin Cred Secret " + param_value + " not present"
			return "fail", message
		}

		if !strings.Contains(result, "username") {
			return "fail", "Error: Key username not present in secret " + param_value
		}

		if !strings.Contains(result, "password") {
			return "fail", "Error: Key password not present in secret " + param_value
		}

		return "pass", "ArgoCD admin credentials secret " + param_value + " validated"
	} else {
		return "fail", "Argo CD admin creds value empty"
	}
}

// mxe_deployer_internal_gitops_enabled is valid if
// - if Deployer is not configures in which case this test will skip and return true
// - Or if deployer is enabled and if the value is either true or false,
func mxe_deployer_internal_gitops_enabled(param_value string) (string, string) {

	if !isDeployer {
		return "pass", "Skipping since deployer is not enabled"
	}
	result := check_true_false(param_value)

	if result {
		return "pass", "Internal Git Ops is set to " + param_value
	} else {
		return "fail", "Internal Git Ops must to set to true or false. Given value is " + param_value
	}
}

// mxe_deployer_argocd_redis_ha is valid if
// - if Deployer is not configured in which case this test will skip and return true
// - Or if deployer is enabled and if the value is either true or false,
func mxe_deployer_argocd_redis_ha(param_value string) (string, string) {

	if !isDeployer {
		return "pass", "Skipping since deployer is not enabled"
	}
	result := check_true_false(param_value)

	if result {
		return "pass", "Argocd redis ha is set to " + param_value
	} else {
		return "fail", "Argocd redis ha must to set to true or false. Given value is " + param_value
	}
}

// appArmorProfile_type is valid if it has one of the below values,
// - runtime/default
// - unconfined
// - localhost/<profile name> - The profile name can be any string.
func appArmorProfile_type(param_value string) (string, string) {
	param_pattern := `runtime/default|unconfined|localhost/*`
	param_regex := regexp.MustCompile(param_pattern)

	if param_regex.MatchString(param_value) {
		return "pass", "App Armor Profile type value " + param_value + " validated"
	} else {
		return "fail", "Error: App Armor Profile type value should be one of runtime/default or unconfined or localhost/<profile_name> . Given Value is " + param_value
	}
}

// seccompProfile_type is valid if it has one of the below values,
// - RuntimeDefault
// - Unconfined
// - Localhost/<profile name> - The profile name can be any string.
func seccompProfile_type(param_value string) (string, string) {
	param_pattern := `RuntimeDefault|Unconfined|Localhost/*`
	param_regex := regexp.MustCompile(param_pattern)

	if param_regex.MatchString(param_value) {
		return "pass", "Seccomp Profile type value " + param_value + " validated"
	} else {
		return "fail", "Error: Seccomp Profile type value should be one of RuntimeDefault or Unconfined or Localhost/<profile_name> . Given Value is " + param_value
	}
}

// mxe_commons_encryption_enable_in_transit is valid is the value is either true or false.
func mxe_commons_encryption_enable_in_transit(param_value string) (string, string) {
	result := check_true_false(param_value)

	if result {
		return "pass", "Encryption in Transit Enable is set to " + param_value
	} else {
		return "fail", "Encryption in Transit Enabled must to set to true or false. Given value is " + param_value
	}
}

// mxe_commons_service_mesh_version is valid if
// - The mesh controller version installed in the service mesh namespace is same as the one present in the config.yaml
func mxe_commons_service_mesh_version(param_value string) (string, string) {

	service_mesh_namespace := get_value("mxe_commons-service_mesh-namespace")
	command := "kubectl get pod -n " + service_mesh_namespace + " |grep -w eric-mesh-controller"
	_, stat := exec_command(command)
	if !stat {
		return "fail", "Error Service Mesh Controller not installed"
	}
	command = "kubectl get pod -n " + service_mesh_namespace + " --selector 'app.kubernetes.io/instance=eric-mesh-controller' -o jsonpath='{.items[0].metadata.labels.app\\.kubernetes\\.io\\/version}' 2> /dev/null |awk -F[_,-] '{print $1}'|tr -d '\\n'"
	result, status := exec_command(command)
	if !status {
		return "fail", "Mesh Version validation failed.Unable to get mesh version"
	}

	if result == param_value {
		return "pass", "Service mesh version " + result + " matches with version in config.yaml " + param_value
	}
	return "fail", "Service mesh version " + result + " does not match with version in config.yaml " + param_value
}

// mxe_commons_service_mesh_namespace is valid if a namespace of that name exists in the cluster.
func mxe_commons_service_mesh_namespace(param_value string) (string, string) {
	command := "kubectl get ns -o custom-columns=':metadata.name'|grep -w " + param_value
	result, status := exec_command(command)

	if !status {
		return "fail", "Error validating service mesh namespace.Unable to get list of namespaces"
	}
	return "pass", "Service Mesh namespace '" + result + "' matches the one present in config.yaml"
}

// mxe_commons_legal_warning_message is valid if the value is non zero
func mxe_commons_legal_warning_message(param_value string) (string, string) {
	if len(param_value) == 0 {
		return "fail", "Legal Warning message should not be null"
	}
	return "pass", "Legal warning message valid"
}

// mxe_commons_mxe_admin_secret is valid if it satisfies below conditions,
// - A secret of that name exists.
// - It contains kcadminid kcpassword in the fields,
// - Or its value is eric-mxe-gatekeeper-keycloak-access-creds-hooked, in which case default value will be used.
func mxe_commons_mxe_admin_secret(param_value string) (string, string) {
	message := ""
	if param_value == "<nil>" {
		return "fail", "MXE Admin Secret is empty."
	}

	if param_value == "eric-mxe-gatekeeper-keycloak-access-creds-hooked" {
		return "pass", "MXE Admin secret will be created with default values during installation , since the default secret name is mentioned"
	}
	command := "kubectl -n " + namespace + " get secrets -o jsonpath='{.data}' " + param_value
	result, status := exec_command(command)
	if !status {
		message = "Error: MXE Admin Secret " + param_value + " not present"
		return "fail", message
	}

	if !strings.Contains(result, "kcadminid") {
		return "fail", "Error: Key kcadminid not present in secret " + param_value
	}

	if !strings.Contains(result, "kcpasswd") {
		return "fail", "Error: Key kcpasswd not present in secret " + param_value
	}

	return "pass", "Successfully validated secret " + param_value + "."
}

// mxe_commons_mxe_user_secret is valid if it satisfies below conditions,
// - A secret of that name exists.
// - It contains username,password in the fields,
// - Or its value is null, in which case default value will be used.
func mxe_commons_mxe_user_secret(param_value string) (string, string) {
	message := ""
	if param_value != "<nil>" {
		command := "kubectl -n " + namespace + " get secrets -o jsonpath='{.data}' " + param_value
		result, status := exec_command(command)
		if !status {
			message = "Error MXE User Secret " + param_value + " not present"
			return "fail", message
		}

		fmt.Println(command, result)
		if !strings.Contains(result, "username") {
			return "fail", "Error: Key username not present in secret " + param_value
		}

		if !strings.Contains(result, "password") {
			return "fail", "Error: Key username not present in secret " + param_value
		}

		return "pass", "Successfully validated secret " + param_value + "."

	} else {
		return "pass", "MXE User Secret is empty . Default value will be used"
	}
}

// mxe_commons_container_registry_volume_size is valid is the value is in the format of ^[0-9]+[G|M]i . For Ex: 20Gi,1024 Mi
func mxe_commons_container_registry_volume_size(param_value string) (string, string) {
	param_pattern := `^[0-9]+[G|M]i` // regex to check only digits - done
	param_regex := regexp.MustCompile(param_pattern)

	if param_regex.MatchString(param_value) {
		return "pass", "Container Registry Size value " + param_value + " validated"
	} else {
		return "fail", "Error: Container Registry Size should be numeric followed by Gi or Mi. Given Value is " + param_value + " not according to the format"
	}
}

// mxe_commons_enable_gatekeeper_ha is valid if the value is either true or false.
func mxe_commons_enable_gatekeeper_ha(param_value string) (string, string) {
	result := check_true_false(param_value)

	if result {
		return "pass", "GateKeeper HA is set to " + param_value
	} else {
		return "fail", "Gatekeeper HA must to set to true or false. Given value is " + param_value
	}
}

// mxe_commons_rate_limit_enabled is valid if the value is either true or false.
func mxe_commons_rate_limit_enabled(param_value string) (string, string) {
	result := check_true_false(param_value)

	if result {
		return "pass", "Rate Limit Enabled is set to " + param_value
	} else {
		return "fail", "Rate Limit Enabled must to set to true or false. Given value is " + param_value
	}
}

// bro_label_key is valid if ,
// - if the brlabelKey is not set during BRO installation , then the value should be adpbrlabelkey
// - else it should be set to the value of brLabelKey parameter configured while installing the BRO Service
func bro_label_key(param_value string) (string, string) {
	command := "helm get values -n " + namespace + " eric-ctrl-bro -a 2>/dev/null| grep brlabelkey"
	_, status := exec_command(command)

	if !status {
		if param_value == "adpbrlabelkey" {
			return "pass", "BRO - label_key set to " + param_value
		} else {
			return "fail", "If brLabelKey is set during during BRO service installation the default value adpbrlabelkey should be set"
		}
	} else {
		command = "helm get values -n " + namespace + " eric-ctrl-bro -a 2>/dev/null| grep adp|awk -F: '{print $2}'"
		bro_label_key, cmd_status := exec_command(command)
		if cmd_status {
			if param_value == bro_label_key {
				return "pass", "BRO label_key set to " + bro_label_key
			} else {
				return "fail", "BRO label_key set to " + param_value + " in config.yaml. Actual value is " + bro_label_key
			}
		} else {
			return "fail", "Error: Unable to get bro label key value"
		}
	}
}

// hostNetwork is valid if the value is either true or false.
func hostNetwork(param_value string) (string, string) {
	result := check_true_false(param_value)

	if result {
		return "pass", "Host Network is set to " + param_value
	} else {
		return "fail", "Host Network must to set to true or false. Given value is " + param_value
	}
}

// api_argocd_host is valid if it is valid hostname as per the check_host_name function
func api_argocd_host(param_value string) (string, string) {
	result := check_host_name(param_value)

	if result {
		return "pass", "API ArgoCD hostname " + param_value + " validated. "
	} else {
		return "fail", "Host name should be in FQDN format"
	}
}

// api_argocd_tlsSecretName is valid if it is valid TLS Secert as per the check_tls_secret function
func api_argocd_tlsSecretName(param_value string) (string, string) {
	argo_cd_hostname := get_value("api-argocd-host")
	ret_code, message := check_tls_secret(param_value, argo_cd_hostname, "tls\\.crt", true)
	if ret_code != false {
		return "pass", message
	} else {
		return "fail", message
	}
}

// api_gitea_tlsSecretName is valid if it is valid TLS Secert as per the check_tls_secret function
func api_gitea_tlsSecretName(param_value string) (string, string) {
	gitea_hostname := get_value("api-gitea-host")
	ret_code, message := check_tls_secret(param_value, gitea_hostname, "tls\\.crt", true)

	if ret_code != false {
		return "pass", message
	} else {
		return "fail", message
	}
}

// api_gitea_host is valid if it is valid hostname as per the check_host_name function
func api_gitea_host(param_value string) (string, string) {
	result := check_host_name(param_value)

	if result != false {
		return "pass", "API GITEA hostname " + param_value + " validated. "
	} else {
		return "fail", "Host name should be in FQDN format"
	}
}

// api_mxe_host is valid if it is valid hostname as per the check_host_name function
func api_mxe_host(param_value string) (string, string) {
	result := check_host_name(param_value)

	if result != false {
		return "pass", "API MXE hostname " + param_value + " validated. "
	} else {
		return "fail", "Host name should be in FQDN format"
	}
}

// api_mxe_tlsSecretName is valid if it is valid TLS Secert as per the check_tls_secret function
func api_mxe_tlsSecretName(param_value string) (string, string) {
	mxe_hostname := get_value("api-mxe-host")
	ret_code, message := check_tls_secret(param_value, mxe_hostname, "tls\\.crt", true)

	if ret_code != false {
		return "pass", message
	} else {
		return "fail", message
	}
}

// api_deployer_host is valid if it is valid hostname as per the check_host_name function
func api_deployer_host(param_value string) (string, string) {

	if !isDeployer {
		return "pass", "Skipping since deployer is not enabled"
	}
	result := check_host_name(param_value)

	if result != false {
		return "pass", "API Deployer hostname " + param_value + " validated. "
	} else {
		return "fail", "Host name should be in FQDN format"
	}
}

// api_deployer_tlsSecretName is valid if it is valid TLS Secert as per the check_tls_secret function
func api_deployer_tlsSecretName(param_value string) (string, string) {

	if !isDeployer {
		return "pass", "Skipping since deployer is not enabled"
	}

	deployer_hostname := get_value("api-deployer-host")
	ret_code, message := check_tls_secret(param_value, deployer_hostname, "tls\\.crt", true)

	if ret_code != false {
		return "pass", message
	} else {
		return "fail", message
	}
}

// api_oauth_host is valid if it is valid hostname as per the check_host_name function
func api_oauth_host(param_value string) (string, string) {
	result := check_host_name(param_value)

	if result != false {
		return "pass", "API OAUTH hostname " + param_value + " validated. "
	} else {
		return "fail", "Host name should be in FQDN format"
	}
}

// api_oauth_tlsSecretName is valid if it is valid TLS Secert as per the check_tls_secret function
func api_oauth_tlsSecretName(param_value string) (string, string) {
	oauth_hostname := get_value("api-oauth-host")
	ret_code, message := check_tls_secret(param_value, oauth_hostname, "tls\\.crt", true)

	if ret_code {
		return "pass", message
	} else {
		return "fail", message

	}
}

// iam_ca_secret_name is valid if it is valid TLS Secert as per the check_tls_secret function.
// The hostname of the certificate wont be checked here since this is a root certificate
func iam_ca_secret_name(param_value string) (string, string) {
	ret_code, message := check_tls_secret(param_value, "NA", "ca\\.crt", false)

	if ret_code {
		return "pass", message
	} else {
		return "fail", message
	}
}

// mxe_commons_mxe_user_secret is valid if it satisfies below conditions,
// - A secret of that name exists.
// - It contains username,password,auth in the fields,
func docker_registry_secret(param_value string) (string, string) {
	message := ""
	if param_value != "<nil>" {
		command := "kubectl -n " + namespace + " get secrets -o jsonpath='{.data.\\.dockerconfigjson}' " + param_value + " |base64 -d"
		result, status := exec_command(command)
		if !status {
			message = "Error Docker Registry  Secret " + param_value + " not present"
			return "fail", message
		}

		if !strings.Contains(result, "username") {
			return "fail", "Error: Key username not present in secret " + param_value
		}

		if !strings.Contains(result, "password") {
			return "fail", "Error: Key password not present in secret " + param_value
		}

		pattern := `\bauth\b`
		auth_regex := regexp.MustCompile(pattern)

		if !auth_regex.MatchString(result) {
			return "fail", "Error : Key auth missing in the secret " + param_value
		}

		return "pass", "Successfully validated secret " + param_value + "."

	} else {
		return "fail", "Docker Registry cannot be empty"
	}
}

// docker_registry_host is valid if it is not null
func docker_registry_host(param_value string) (string, string) {
	if param_value != "<nil>" {
		return "pass", "Docker registry host " + param_value + " validated"
	} else {
		return "fail", "Docker registry host cannot be null"
	}
}

// docker_registry_mn_host is valid if it is not null
func docker_registry_mn_host(param_value string) (string, string) {
	if param_value != "<nil>" {
		return "pass", "Docker registry host for Minio" + param_value + " validated"
	} else {
		return "fail", "Docker registry host for Minio cannot be null"
	}
}

// docker_registry_ca_secret_name is valid if
// - if it is valid TLS Secert as per the check_tls_secret function. The hostname of the certificate wont be checked here since this is a root certificate.
// - Or the value is null in case if docker_registry.host parameter is set as armdocker.rnd.ericsson.se
func docker_registry_ca_secret_name(param_value string) (string, string) {
	docker_registry_hostname := get_value("docker_registry-host")
	if param_value != "<nil>" && len(param_value) != 0 {
		if docker_registry_hostname == "armdocker.rnd.ericsson.se" {
			return "fail", "Docker registry CA Secret name should be null when registry host is " + docker_registry_hostname
		} else {
			ret_code, message := check_tls_secret(param_value, "NA", "ca\\.crt", false)
			if ret_code {
				return "pass", message
			} else {
				return "fail", message
			}
		}
	} else {
		if docker_registry_hostname == "armdocker.rnd.ericsson.se" {
			return "pass", "Docker registry CA Secret name is null."
		} else {
			return "fail", "Docker registry CA Secret name cannot be null"
		}
	}
}

// mxe_serving_seldon_webhook_port is valid if the value is a port number between 1024 and 65535
func mxe_serving_seldon_webhook_port(param_value string) (string, string) {

	pattern := `^(102[4-9]|10[3-9]\d|1[1-9]\d{2}|[2-9]\d{3}|[1-5]\d{4}|6[0-4]\d{3}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$`

	port_regex := regexp.MustCompile(pattern)

	if !port_regex.MatchString(param_value) {
		return "fail", "Port number not valid. It must be in the range of 1024 to 65535"
	}
	return "pass", "Seldon Webhook port validated"
}

// mxe_serving_seldon_metrics_port is valid if the value is a port number between 1024 and 65535
func mxe_serving_seldon_metrics_port(param_value string) (string, string) {
	pattern := `^(102[4-9]|10[3-9]\d|1[1-9]\d{2}|[2-9]\d{3}|[1-5]\d{4}|6[0-4]\d{3}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$`

	port_regex := regexp.MustCompile(pattern)

	if !port_regex.MatchString(param_value) {
		return "fail", "Port number not valid. It must be in the range of 1024 to 65535"
	}
	return "pass", "Seldon Metrics port validated"
}

// mxe_serving_model_service_owasp is valid if the value is either true or false.
func mxe_serving_model_service_owasp(param_value string) (string, string) {
	result := check_true_false(param_value)

	if result {
		return "pass", "Service OWASP Enabled is set to " + param_value
	} else {
		return "fail", "Service OWASP Enabled must to set to true or false. Given value is " + param_value
	}
}

// isExtCA is valid if the value is either true or false.
func isExtCA(param_value string) (string, string) {
	result := check_true_false(param_value)

	if result {
		return "pass", "External CA Enabled is set to " + param_value
	} else {
		return "fail", "External CA Enabled must to set to true or false. Given value is " + param_value
	}
}

// encrypted_storage_class is valid if
// - A storage of that name exists in the k8s cluster.
// - Or if is a vcluster the validation will be skipped
// - Or the value is null in case SEP is not used.
func encrypted_storage_class(param_value string) (string, string) {
	if param_value != "<nil>" && len(param_value) != 0 {
		command := "kubectl get  storageclasses.storage.k8s.io -o custom-columns=':metadata.name'|grep -w " + param_value
		result, status := exec_command(command)
		if !status {
			if check_if_vcluster() {
				return "warn", "Skipping since this is vcluster"
			}
			return "fail", "Storage class " + param_value + " not found"
		}

		return "pass", "Storage Class defined in config.yaml " + param_value + " is present in the cluster " + result
	} else {
		return "pass", "Encrypted Storage class is null."
	}

}

// cluster_wide_ingress_class_name is valid if
// - A ingress class of that name exists in the k8s cluster.
// - Or if is a vcluster the validation will be skipped
// - Or if it is a ingress other than nginx then validation will be skipped.
func cluster_wide_ingress_class_name(param_value string) (string, string) {
	command := "kubectl get ingressclasses.networking.k8s.io -o custom-columns=':metadata.name'|grep -w " + param_value
	result, status := exec_command(command)
	if !status {
		if check_if_vcluster() {
			return "warn", "Skipping since this is vcluster"
		}
		if !strings.Contains(param_value, "nginx") { // regex - done
			return "warn", "Ingress used is not nginx. Kindly validate the ingress class and associated settings manually"
		}
		return "fail", "Ingress class " + param_value + " not found"
	}

	return "pass", "Ingress Class defined in config.yaml " + param_value + " is present in the cluster " + result
}

// bro_service_name is valid if a service of that name exists.
func bro_service_name(param_value string) (string, string) {
	command := "kubectl get service -n " + namespace + " -o custom-columns=':metadata.name' |grep -w " + param_value
	result, status := exec_command(command)

	if !status {
		return "fail", "BRO Service mentioned in yaml could not be found in cluster"
	}

	return "pass", "BRO Service " + result + " validated."
}

// bro_service_port is valid if the port mentioned in described the bro-service-name service.
func bro_service_port(param_value string) (string, string) {
	bro_service := get_value("bro-service_name")
	command := "kubectl get service -n " + namespace + " -o jsonpath='{range .spec.ports[*]}{.targetPort}{" + "\"\\n\"" + "}{end}' " + bro_service + "|grep -w " + param_value
	result, status := exec_command(command)
	if !status {
		return "fail", "BRO Service Port mentioned in yaml could not be found in service yaml"
	}

	return "pass", "BRO Service Port " + result + " validated."
}

// mxe_exploration_singleUserProfileList is valid ,
// - if the value of parameter is a yaml
// - the value has only the below fields and the field values corresponds to the regex defined in template map
//   - description
//   - kubespawner_override.mem_guarantee,
//   - kubespawner_override.mem_limit
//   - kubespawner_override.cpu_guarantee
//   - kubespawner_override.cpu_limit
//   - display_name
func mxe_exploration_singleUserProfileList(param_value string) (string, string) { // Values to be checked. - done
	var yaml_data []map[interface{}]interface{}
	err := yaml.Unmarshal([]byte(param_value), &yaml_data)
	if err != nil {
		return "fail", "Error parsing the value"
	}

	template_explorer_map := map[string]string{
		"description":                        `[A-Za-z].*`,
		"kubespawner_override-mem_guarantee": `[0-9]+G`,
		"kubespawner_override-mem_limit":     `[0-9]+G`,
		"kubespawner_override-cpu_guarantee": `^\d+$`,
		"kubespawner_override-cpu_limit":     `^\d+$`,
		"display_name":                       `[A-Za-z].*`,
	}
	for _, value := range yaml_data {
		temp_map := make(map[string]interface{})
		for k, v := range value {
			temp_map[k.(string)] = v
		}

		template_keys := make([]string, len(template_explorer_map))
		i := 0
		for k, _ := range template_explorer_map {
			template_keys[i] = k
			i = i + 1
		}

		keys := get_keys(temp_map)
		tempkeys_map := make(map[string]string)

		for _, key := range keys {
			if !strings.Contains(key, "-") {
				tempkeys_map[key] = temp_map[key].(string)
			} else {
				nkeys := strings.Split(key, "-")
				conf := temp_map
				for _, nk := range nkeys {
					if value, ok := conf[nk]; ok {
						if nMap, ok := value.(map[string]interface{}); ok {
							conf = nMap
						}
					}
				}
				tempkeys_map[key] = fmt.Sprintf("%v", conf[nkeys[len(nkeys)-1]])
			}
		}
		if len(tempkeys_map) == len(template_explorer_map) {
			for temp_key, temp_value := range tempkeys_map {
				res, err := template_explorer_map[temp_key]
				if !err {
					return "fail", fmt.Sprintf("The explorer data  %v is not according to described format. Please refer document for correct format", temp_map)
				}
				pattern := regexp.MustCompile(res)
				if !pattern.MatchString(temp_value) {
					return "fail", fmt.Sprintf("The explorer data  %v with key %v is not according to described format. Please refer document for correct format", temp_map, temp_key)
				}

			}
		} else {
			return "fail", fmt.Sprintf("The explorer data  %v does not match number of keys in template. Please refer document for correct format", temp_map)
		}
	}
	return "pass", "MXE Exploration Jupyter notebook profiles validated successfully"
}
