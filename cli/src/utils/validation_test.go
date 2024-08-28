package utils

import (
	"fmt"
	"reflect"
	"testing"
)

var f = func() error {
	fmt.Println("Test")
	return nil
}

func TestIsValidModelVersion(t *testing.T) {
	cases := []struct {
		version  string
		expected bool
	}{
		{"1.2.3", true},
		{"1.2", false},
		{"1.2.-1", false},
		{"1.2.a", false},
		{"2", false},
	}
	for _, c := range cases {
		out := IsValidModelVersion(c.version)
		if out != c.expected {
			t.Errorf("isValidVersion(%q), output: %t, expected %t", c.version, out, c.expected)
		}
	}
}

func TestIsValidModelId(t *testing.T) {
	cases := []struct {
		name     string
		expected bool
	}{
		{"name", true},
		{"name23nme4", true},
		{"na_me", false},
		{"na-me", false},
		{"0.5", true},
		{"0-5", false},
		{"Name", false},
		{"name;", false},
		{"name:", false},
	}
	for _, c := range cases {
		out := IsValidModelId(c.name)
		if out != c.expected {
			t.Errorf("isValidNameFormat(%s), output: %t, expected %t", c.name, out, c.expected)
		}
	}
}

func TestIsValidVersion(t *testing.T) {
	cases := []struct {
		version  string
		expected bool
	}{
		{"1.2.3", true},
		{"1.2", false},
		{"1.2.-1", false},
		{"1.2.a", false},
		{"2", false},
	}
	for _, c := range cases {
		out := IsValidModelVersion(c.version)
		if out != c.expected {
			t.Errorf("isValidVersion(%q), output: %t, expected %t", c.version, out, c.expected)
		}
	}
}

func TestIsValidName(t *testing.T) {
	cases := []struct {
		name     string
		expected bool
	}{
		{"name", true},
		{"name23nme4", true},
		{"na_me", false},
		{"na-me", true},
		{"na-me", true},
		{"0.5", false},
		{"0-5", true},
		{"Name", false},
		{"name;", false},
		{"name:", false},
	}
	for _, c := range cases {
		out := IsValidModelName(c.name)
		if out != c.expected {
			t.Errorf("isValidNameFormat(%s), output: %t, expected %t", c.name, out, c.expected)
		}
	}
}

func TestIsValidInstancesFormat(t *testing.T) {
	expectedOne := int(1);
	expectedZero := int(0);
	cases := []struct {
		instances      string
		expectedResult bool
		expectedValue  *int
	}{
		{"-1", false, &expectedZero},
		{"0", false, &expectedZero},
		{"a", false, &expectedZero},
		{"0.5", false, &expectedZero},
		{"1", true, &expectedOne},
	}
	for _, c := range cases {
		outResult, outValue := IsValidInstancesFormat(c.instances)
		if outResult != c.expectedResult {
			t.Errorf("isValidInstancesFormat(%q), result: %t, expected %t", c.instances, outResult, c.expectedResult)
		}
		if *outValue != *c.expectedValue {
			t.Errorf("isValidInstancesFormat(%q), value: %d, expected %d", c.instances, *outValue, *c.expectedValue)
		}
	}
}

func TestSeparateIdAndVersion(t *testing.T) {
	cases := []struct {
		combined string
		expected bool
		name     string
		version  string
	}{
		{"id:1.2.3", true, "id", "1.2.3"},
		{"id:1.2", true, "id", "1.2"},
		{"i;d:1.2.-1", true, "i;d", "1.2.-1"},
		{"i:d:1.2.3", true, "i:d", "1.2.3"},
		{"id1.2.3", false, "", ""},
		{"id:", false, "", ""},
		{":id", false, "", ""},
	}
	for _, c := range cases {
		b, name, version := SeparateIdAndVersion(c.combined)
		if b != c.expected || name != c.name || version != c.version {
			t.Errorf("separateIdAndVersion(%s), output: (%t, %s, %s), expected (%t, %s, %s)", c.combined, b, name, version, c.expected, c.name, c.version)
		}
	}
}

func TestSeparateWeights(t *testing.T) {
	var w1 = 0.9;
	var w2 = 0.1;
	var w3 = 1.0;
	cases := []struct {
		combined  string
		expected  bool
		separated []*float64
	}{
		{"abc", false, nil},
		{"0.9.0.1", false, nil},
		{"0.9,0.1", true, []*float64{&w1, &w2}},
		{"1.0", true, []*float64{&w3}},
	}
	for _, c := range cases {
		b, separated := SeparateWeights(c.combined)
		if b != c.expected || !reflect.DeepEqual(separated, c.separated) {
			t.Errorf("SeparateWeights(%s), output: (%t, %v), expected (%t, %v)", c.combined, b, separated, c.expected, c.separated)
		}
	}
}

func TestSeparateNameMetric(t *testing.T) {
	cases := []struct {
		metricName string
		expected   bool
		name       string
		metric     string
	}{
		{"cpuMilliCores", true, "cpu", "m"},
		{"memoryMegaBytes", true, "memory", "Mi"},
		{"unknown", false, "", ""},
	}

	for _, c := range cases {
		b, name, metric := SeparateNameMetric(c.metricName)
		if b != c.expected || !reflect.DeepEqual(name, c.name) || !reflect.DeepEqual(metric, c.metric) {
			t.Errorf("SeparateNameMetric(%s), output: (%t, %s, %s), expected (%t, %s, %s)", c.metricName, b, name, metric, c.expected, c.name, c.metric)
		}
	}
}
