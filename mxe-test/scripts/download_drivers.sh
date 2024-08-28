#! /usr/bin/env bash
set -ex

#SETUP Chromedriver

CHROMEDRIVER_VERSION=`wget --no-verbose --output-document - https://chromedriver.storage.googleapis.com/LATEST_RELEASE`

wget --no-verbose --output-document /tmp/chromedriver_linux64.zip "http://chromedriver.storage.googleapis.com/${CHROMEDRIVER_VERSION}/chromedriver_linux64.zip"

unzip -qq /tmp/chromedriver_linux64.zip -d /opt/chromedriver

chmod +x /opt/chromedriver/chromedriver

ln -fs /opt/chromedriver/chromedriver /usr/local/bin/chromedriver

## Setup Gecko Driver

if [ -n "${GITHUB_TOKEN}" ]; then
    GECKODRIVER_VERSION=$(curl -sS -H "Authorization: token ${GITHUB_TOKEN}" https://api.github.com/repos/mozilla/geckodriver/releases/latest | grep tag_name | cut -d'"' -f4)
else
    GECKODRIVER_VERSION=$(curl -sS https://api.github.com/repos/mozilla/geckodriver/releases/latest | grep tag_name | cut -d'"' -f4)
fi

wget --no-verbose --output-document /tmp/geckodriver.tar.gz \
    "https://github.com/mozilla/geckodriver/releases/download/${GECKODRIVER_VERSION}/geckodriver-${GECKODRIVER_VERSION}-linux64.tar.gz"

tar --directory /opt -zxf /tmp/geckodriver.tar.gz

chmod +x /opt/geckodriver

ln -fs /opt/geckodriver /usr/local/bin/geckodriver