package cmd

import (
	"bufio"
	"fmt"
	"io"
	"io/fs"
	"os"
	"path"
	"strings"
	"text/template"

	"github.com/imdario/mergo"
	log "github.com/sirupsen/logrus"
	"github.com/spf13/cobra"
	"mxe.ericsson/mxe-generate/utils/errors"
	"mxe.ericsson/mxe-generate/validator"
	templateUtils "mxe.ericsson/mxe-generate/utils/template"
)

var defaultTemplateOptions = []string{"missingkey=default"}
func NewValidateCommand() *cobra.Command {
	var cfgFile string
	

	var command = &cobra.Command{
		Use:   "validate",
		Short: "Validate config file",
		Run: func(c *cobra.Command, args []string) {
			// log.Infof("mxe-deployer-values.yaml file generation is %s", deployerEnabledStr())
			validator.Execute_tests(cfgFile, namespace, renderDeployer,true)
		},
	}
	command.Flags().StringVar(&cfgFile, "cfg-file", "", "A yaml `FILE` from which to read variables")
	command.MarkFlagRequired("cfg-file")
	// command.Flags().StringVar(&namespace,"namespace","mxe","MXE namespace")
	// command.Flags().BoolVar(&renderDeployer,"renderDeployer",true,"Set to false if deployer is disabled")

	return command
}
func NewGenerateValuesFromConfigCommand() *cobra.Command {
	var cfgFile, outputDir string
	var genPreReq,skipValidate bool
	var setVar []string
	

	
	var command = &cobra.Command{
		Use:   "values",
		Short: "Create values files from a config file",
		Run: func(c *cobra.Command, args []string) {
			log.Infof("mxe-deployer-values.yaml file generation is %s", deployerEnabledStr())
			createValuesFiles(cfgFile, setVar, outputDir, genPreReq,skipValidate)
		},
	}
	command.Flags().StringArrayVarP(&setVar, "set-var", "s", []string{}, "A `KEY=VALUE` pair variable")
	command.Flags().StringVar(&cfgFile, "cfg-file", "", "A json or yaml `FILE` from which to read variables")
	currentDir, err := os.Getwd()
	errors.CheckError(err)
	command.Flags().StringVar(&outputDir, "output-dir", currentDir, "The `DIR` to write the generated files to")
	command.Flags().BoolVar(&skipValidate, "skipValidate", false, "set to true to skip validation of config.yaml")
	command.MarkFlagRequired("cfg-file")
	return command
}

func createValuesFiles(cfgFile string, setVars []string, outputDir string, genPreReq bool,skipValidate bool) {
	if !skipValidate {
		log.Infof("Validating given "+cfgFile+" file")
		result := validator.Execute_tests(cfgFile,namespace,renderDeployer,false)
		if ! result {
			log.Errorf(cfgFile+" validation failed.Check the parameters that failed validation and retry again.")
			os.Exit(1)
		}
	}
	vars, err := loadVariables(cfgFile, setVars)
	errors.CheckError(err)
	embedFS := getFileSystem()
	for tplFileKey, tplFile := range templatesMap {
		if render(tplFileKey) {
			err := run(tplFile, vars, embedFS, outputDir)
			errors.CheckError(err)
		}
	}
}

func loadInputVarsFile(varsFilePath string) (map[string]interface{}, error) {
	var vars map[string]interface{}

	if varsFilePath != "" {
		v, err := templateUtils.LoadVarsFile(varsFilePath)
		if err != nil {
			return nil, err
		}
		vars = v
	} else {
		vars = make(map[string]interface{})
	}

	return vars, nil
}

func loadInputVarsOptions(setVars []string) (map[string]interface{}, error) {

	vars := make(map[string]interface{})

	for _, varStr := range setVars {
		key, val := templateUtils.GetKeyVal(varStr)
		varMap := templateUtils.KeyValToMap(key, val)

		err := mergo.Merge(&vars, varMap, mergo.WithOverride)
		if err != nil {
			return nil, err
		}
	}

	return vars, nil
}

func loadVariables(cfgFile string, setVars []string) (map[string]interface{}, error) {

	vars, err := loadInputVarsFile(cfgFile)
	if err != nil {
		return nil, err
	}

	envVars := templateUtils.Env()
	err = mergo.Merge(&vars, envVars, mergo.WithOverride)
	if err != nil {
		return nil, err
	}

	optVars, err := loadInputVarsOptions(setVars)
	if err != nil {
		return nil, err
	}

	err = mergo.Merge(&vars, optVars, mergo.WithOverride)
	if err != nil {
		return nil, err
	}

	vars["MXE_VERSION"] = MXE_CLI_VERSION

	return vars, nil
}

func executeTemplate(valuesIn map[string]interface{}, out io.Writer, tpl *template.Template) error {
	tpl.Option(defaultTemplateOptions...)
	err := tpl.Execute(out, valuesIn)
	if err != nil {
		return fmt.Errorf("Failed to parse standard input: %v", err)
	}
	return nil
}

func run(tplPath string, vars map[string]interface{}, embedFS fs.FS, outputDir string) error {
	tpl, err := templateUtils.LoadTemplateFile(tplPath, embedFS)
	if err != nil {
		return err
	}

	if _, err := os.Stat(outputDir); os.IsNotExist(err) {
		os.MkdirAll(outputDir, os.ModePerm)
	}
	outputFileName := strings.Replace(path.Base(tplPath), ".gotmpl", "", -1)
	absOutputFileName := path.Join(outputDir, outputFileName)
	outputFile, err := os.Create(absOutputFileName)
	errors.CheckError(err)

	err = executeTemplate(vars, outputFile, tpl)
	if err != nil {
		return err
	}

	ok := isRenderedProperly(absOutputFileName)
	if !ok {
		return fmt.Errorf("Error rendering %s", outputFileName)
	}

	fmt.Printf("Generated %s\n", absOutputFileName)

	return nil
}

func isRenderedProperly(outputFile string) bool {
	f, err := os.Open(outputFile)
	errors.CheckError(err)
	defer f.Close()

	scanner := bufio.NewScanner(f)
	lineNo := 1
	invalidLines := []string{}

	for scanner.Scan() {
		line := scanner.Text()
		if strings.Contains(line, "<no value>") {
			invalidLines = append(invalidLines, fmt.Sprintf("%s:%d: %s\n", outputFile, lineNo, line))
		}
		lineNo++
	}

	if len(invalidLines) > 0 {
		fmt.Println("Generated file:" + outputFile + " contains keys whose value is set to <no value>. This is caused by missing entries in config file")
		fmt.Println(invalidLines)
		fmt.Println("Please fix the configuration file and try again")
		return false
	}
	return true
}
