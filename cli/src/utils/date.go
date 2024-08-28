package utils

import "time"

/**
Formats an RFC3339 formatted date string to our format
*/
func FormatCreatedDate(date string) string {
	if len(date) == 0 {
		return ""
	}
	now := time.Now()
	created, err := time.Parse(time.RFC3339, date)
	if err != nil {
		// utils.LogError(err.Error())
		LogError("Failed to parse creation date received from the server!")
		Exit(1)
		return ""
	}

	if now.Year() == created.Year() && now.Month() == created.Month() && now.Day() == created.Day() {
		return created.Format("15:04")
	} else if now.Year() == created.Year() {
		return created.Format("Jan02 15:04")
	} else {
		return created.Format("2006 Jan02 15:04")
	}
}
