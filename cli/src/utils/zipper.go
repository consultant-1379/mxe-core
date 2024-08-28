package utils

import (
	"archive/zip"
	"io"
	"os"
	"path/filepath"
	"strings"
)

func CreateZip(source string) (zipFilename string, err error) {
	workingDir, err := os.Getwd()
	if err != nil {
		return
	}
	filenamePrefix := filepath.Base(source)
	if filenamePrefix == "." {
		filenamePrefix = filepath.Base(workingDir)
	}
	if filenamePrefix == "." {
		filenamePrefix = "model"
	}
	filenamePrefix = filenamePrefix + ".zip"

	zipfile, err := os.OpenFile(filenamePrefix, os.O_RDWR|os.O_CREATE|os.O_EXCL|os.O_TRUNC, 0666)
	if err != nil {
		return err.Error(), err
	}
	defer zipfile.Close()
	zipFilename = zipfile.Name()
	archive := zip.NewWriter(zipfile)
	defer archive.Close()

	err = filepath.Walk(source, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		if path == source {
			return err
		}
		if filepath.Base(zipFilename) == filepath.Base(path) {
			return err
		}

		header, err := zip.FileInfoHeader(info)
		if err != nil {
			return err
		}
		header.Name, err = filepath.Rel(source, path)
		if err != nil {
			return err
		}
		header.Name = strings.Replace(header.Name, string(filepath.Separator), `/`, -1)

		if info.IsDir() {
			header.Name += "/"
		} else {
			header.Method = zip.Deflate
		}

		writer, err := archive.CreateHeader(header)
		if err != nil {
			return err
		}

		if info.IsDir() {
			return nil
		}

		file, err := os.Open(path)
		if err != nil {
			return err
		}
		defer file.Close()
		_, err = io.Copy(writer, file)
		return err
	})

	return
}
