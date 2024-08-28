package file

import "io"

type InputFile struct {
	Reader   io.Reader
	FileName string
	//ContentType string
	//Size     int64
}
