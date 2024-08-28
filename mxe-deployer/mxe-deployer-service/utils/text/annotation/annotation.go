package annotation

import (
	"fmt"
	"strings"
)

const annotationFieldDelimiter = "="

func Parse(annotations []string) (map[string]string, error) {
	var selectedAnnotations map[string]string = map[string]string{}
	if annotations != nil {
		for _, r := range annotations {
			fields := strings.Split(r, annotationFieldDelimiter)
			if len(fields) != 2 {
				return nil, fmt.Errorf("annotations should have key%svalue, but instead got: %s", annotationFieldDelimiter, r)
			}
			selectedAnnotations[fields[0]] = fields[1]
		}
	}
	return selectedAnnotations, nil
}
