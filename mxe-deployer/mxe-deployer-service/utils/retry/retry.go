package retry

import (
	"fmt"
	"math"
	"strconv"
	"time"

	log "github.com/sirupsen/logrus"
)

func Ordinalize(num int) string {

	var ordinalDictionary = map[int]string{
		0: "th",
		1: "st",
		2: "nd",
		3: "rd",
		4: "th",
		5: "th",
		6: "th",
		7: "th",
		8: "th",
		9: "th",
	}

	// math.Abs() is to convert negative number to positive
	floatNum := math.Abs(float64(num))
	positiveNum := int(floatNum)

	if ((positiveNum % 100) >= 11) && ((positiveNum % 100) <= 13) {
		return strconv.Itoa(num) + "th"
	}

	return strconv.Itoa(num) + ordinalDictionary[positiveNum]

}

func Retry(noOfRetries int, sleep time.Duration, f func() error, operation string) (err error) {
	var msg string
	for i := 0; i <= noOfRetries; i++ {
		msg = ""
		err = f()
		if err == nil {
			if i == 0 {
				msg = fmt.Sprintf("%s ran successfully", operation)
			} else {
				msg = fmt.Sprintf("%s ran successfully on %s retry", operation, Ordinalize(i))
			}
			log.Info(msg)
			return
		}

		if i == 0 {
			msg = fmt.Sprintf("%s raised error : %v", operation, err)
		} else {
			msg = fmt.Sprintf("%s raised error : %v on %s retry", operation, err, Ordinalize(i))
		}
		log.Info(msg)

		if i < noOfRetries {
			log.Info(operation, " will be retried")
		}
		time.Sleep(sleep)
	}
	return fmt.Errorf("after %d retries, last error: %s", noOfRetries, err)
}
