package path

import (
	"fmt"
	"io/ioutil"
	"os"
	"path"
	"path/filepath"
)

func MakeDir(dirName string) error {
	if _, serr := os.Stat(dirName); serr != nil {
		merr := os.MkdirAll(dirName, os.ModePerm)
		if merr != nil {
			return merr
		}
	}
	return nil
}

func GetFileFor(basedir string, pathSegments ...string) (string, *os.File, error) {
	relativePath := filepath.Join(pathSegments...)
	filePath := filepath.Join(basedir, relativePath)
	dirName := filepath.Dir(filePath)
	mkerr := MakeDir(dirName)
	if mkerr != nil {
		return "", nil, mkerr
	}
	fmt.Printf("\n\n filePath %s", filePath)
	file, err := os.Create(filePath)
	return relativePath, file, err
}

func HasSubDirectories(dirName string) (bool, error) {

	files, err := ioutil.ReadDir(dirName)
	if err != nil {
		return false, err
	}

	for _, info := range files {
		if info.IsDir() {
			return true, nil
		}
	}
	return false, nil
}

func Cleanup(dirName string) error {
	dir, err := ioutil.ReadDir(dirName)
	if err != nil {
		return err
	}
	for _, d := range dir {
		os.RemoveAll(path.Join([]string{dirName, d.Name()}...))
	}
	return nil
}
