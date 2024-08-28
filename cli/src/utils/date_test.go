package utils

import (
	"fmt"
	"strconv"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
)

func TestDateConversion(t *testing.T) {
	now := time.Now()

	assert.Equal(t, FormatCreatedDate("2018-12-06T12:02:32Z"), "2018 Dec06 12:02",
		"Date from yesteryear should be printed in format YYYY MMDD HH:MM")

	// Converting day to 2 character format
	var dayNum = now.Day()%31 + 1
	var dayString = formatDateElement(dayNum)
	assert.Equal(t, FormatCreatedDate(fmt.Sprintf("%d-01-%sT12:16:32Z", now.Year(), dayString)), fmt.Sprintf("Jan%s 12:16", dayString),
		"Date from this year should be printed in format MMDD HH:MM")

	// Converting month to 2 character format
	var monthNum = int(now.Month())
	var monthString = formatDateElement(monthNum)
	// Converting day to 2 character format
	dayNum = now.Day()
	dayString = formatDateElement(dayNum)
	assert.Equal(t, FormatCreatedDate(fmt.Sprintf("%d-%s-%sT12:16:32Z", now.Year(), monthString, dayString)), "12:16",
		"Date from today should be printed in format HH:MM")
}

func formatDateElement(dateElement int) string {
	if dateElement < 10 {
		return fmt.Sprintf("0%d", dateElement)
	} else {
		LogInfo(strconv.Itoa(dateElement))
		return strconv.Itoa(dateElement)
	}
}
