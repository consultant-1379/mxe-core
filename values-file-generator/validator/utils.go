package validator

import (
	"fmt"
	"io"
	"os"
	"strings"
	"errors"
	"gopkg.in/yaml.v3"
)

var config map[string]interface{}

// parse_config_yaml is for parsing a yaml file and return a string map
func parse_config_yaml(yaml_file string) (map[string]interface{}, int) {

	if _, err := os.Stat(yaml_file); errors.Is(err, os.ErrNotExist) {
		fmt.Println("Error file "+yaml_file+" does not exist")
		os.Exit(1)
	}
	config_file, error := os.Open(yaml_file)
	

	if error != nil {
		fmt.Println("Error opening file:", error)
		return nil, 1
	}
	defer config_file.Close()

	yaml_data, error := io.ReadAll(config_file)
	if error != nil {
		fmt.Println(error)
		return nil, 1
	}

	
	err := yaml.Unmarshal(yaml_data, &config)
	if err != nil {
		fmt.Println(err)
		return nil, 1
	}

	return config, 0
}

// get_keys is a recursive function which returns keys of a map
// in case of a nested map the nested keys are joined with parent key with - as separator.
func get_keys(yaml_data map[string]interface{}) []string {

	keys := []string{}
	for key, value := range yaml_data {
		if key == "singleUserProfileList" {
			keys = append(keys, key)
			continue
		}
		if nestedMap, ok := value.(map[string]interface{}); ok {
			nestedKeys := get_keys(nestedMap)
			for k := 0; k < len(nestedKeys); k++ {
				nestedKeys[k] = key + "-" + nestedKeys[k]
			}
			keys = append(keys, nestedKeys...)
		} else {
			keys = append(keys, key)
		}
	}
	return keys
}

// get_test_names is for generating test function names from the keys of the yaml file
func get_test_names(yaml_file string) []string {
	yaml_data, errorCode := parse_config_yaml(yaml_file)
	if errorCode != 0 {
		fmt.Println("ERROR")
		fmt.Println(errorCode)
	}

	test_names := get_keys(yaml_data)
	return test_names
}

// get_value returns the value for given string. For nested keys the child and parent keys
// are separated by a hypen(-)
func get_value(test_name string) string {
	test_name_keys := strings.Split(test_name, "-")
	conf := config
	for _, key := range test_name_keys {
		if value, ok := conf[key]; ok {
			if nMap, ok := value.(map[string]interface{}); ok {
				conf = nMap
			}
		}
	}
	if test_name_keys[0] == "singleUserProfileList" {
		for key := range conf {
			return fmt.Sprintf("%v", conf[key])
		}
	}

	return fmt.Sprintf("%v", conf[test_name_keys[len(test_name_keys)-1]])
}
