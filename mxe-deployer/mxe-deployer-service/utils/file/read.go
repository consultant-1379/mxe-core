package file

import (
	"archive/zip"
	"mime/multipart"

	"github.com/pkg/errors"
)

func getFileSize(f multipart.File) (int64, error) {
	fileSize, err := f.Seek(0, 2) //2 = from end
	if err != nil {
		return 0, err
	}
	_, err = f.Seek(0, 0)
	if err != nil {
		return 0, err
	}
	return fileSize, err
}

func UncompressArchive(archive InputFile, destinationDir string, relDir string) error {

	if IsTarArchive(archive) {
		err := Untar(destinationDir, archive.Reader)
		if err != nil {
			return errors.Wrapf(err, "Error while untaring archive %s to %s", archive.FileName, relDir)
		}

	} else if IsZipArchive(archive) {
		file := archive.Reader.(multipart.File)
		size, err := getFileSize(file)
		if err != nil {
			return errors.Wrap(err, "Unable to get file size for zip file")
		}

		zReader, err := zip.NewReader(file, size)
		if err != nil {
			return errors.Wrap(err, "Failed to read zip file")
		}
		err = Unzip(destinationDir, zReader)
		if err != nil {
			return errors.Wrapf(err, "Error while untaring archive %s to %s", archive.FileName, relDir)
		}
	}
	return nil
}
