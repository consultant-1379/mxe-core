package utils

import (
	"archive/tar"
	"compress/gzip"
	"crypto"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/pem"
	"errors"
	"fmt"
	"io"
	"io/ioutil"
	"os"
)

var bitSize = 2048

func SignAndSave(tarFileName string, imageFileLocation string, imageId string, privateKeyPath string, publicKeyPath string) error {
	rng := rand.Reader

	rsaPrivateKey, err := loadPrivateKeyFromFile(privateKeyPath)
	if err != nil {
		return errors.New("failed to load private key from file")
	}

	hashed := sha256.Sum256([]byte(imageId))

	signature, err := rsa.SignPKCS1v15(rng, rsaPrivateKey, crypto.SHA256, hashed[:])

	if err != nil {
		return errors.New("failed to sign the file" + err.Error())
	}

	//create tarfile which contains the archive, the signature and the public key
	tarfile, err := os.Create(tarFileName + ".tgz")
	if err != nil {
		return errors.New("could not create signed tarball file")
	}
	defer tarfile.Close()

	var fileWriter io.WriteCloser = tarfile
	fileWriter = gzip.NewWriter(tarfile)
	defer fileWriter.Close()

	tw := tar.NewWriter(fileWriter)
	defer tw.Close()

	err = addFileToTar(PACKAGING_IMAGE_FILENAME, imageFileLocation, tw)
	if err != nil {
		return err
	}

	err = addDataToTar("mxe-image-id.sig", signature, tw)
	if err != nil {
		return err
	}

	err = addFileToTar("mxe-author.pub", publicKeyPath, tw)
	if err != nil {
		return err
	}

	return nil
}

func addDataToTar(fileName string, data []byte, tarWriter *tar.Writer) error {
	hdr := &tar.Header{
		Name: fileName,
		Mode: 0600,
		Size: int64(len(data)),
		}
	if err := tarWriter.WriteHeader(hdr); err != nil {
		return errors.New(fmt.Sprintf("could not write header for file %s", fileName))
	}
	if _, err := tarWriter.Write(data); err != nil {
		return errors.New("could not write data to the tarball")
	}
	return nil
}

func addFileToTar(fileName string, filePath string, tarWriter *tar.Writer) error {
	file, err := os.Open(filePath)
	if err != nil {
		return errors.New(fmt.Sprintf("could not open file %s", filePath))
	}
	defer file.Close()

	stat, err := file.Stat()
	if err != nil {
		return errors.New(fmt.Sprintf("could not get stat for file %s", filePath))
	}

	header := &tar.Header{
		Name:    fileName,
		Size:    stat.Size(),
		Mode:    int64(stat.Mode()),
	}

	err = tarWriter.WriteHeader(header)
	if err != nil {
		return errors.New(fmt.Sprintf("could not write header for file %s", filePath))
	}

	_, err = io.Copy(tarWriter, file)
	if err != nil {
		return errors.New(fmt.Sprintf("could not copy the file %s data to the tarball", filePath))
	}
	return nil
}

func loadPrivateKeyFromFile(privateKeyPath string) (*rsa.PrivateKey, error) {

	privateKeyBytes, err := ioutil.ReadFile(privateKeyPath)
	if err != nil {
		return nil, errors.New("failed to read private key file")
	}

	block, _ := pem.Decode(privateKeyBytes)
	if block == nil || block.Type != "RSA PRIVATE KEY" {
		return nil, errors.New("failed to decode private key file")
	}

	return x509.ParsePKCS1PrivateKey(block.Bytes)
}
