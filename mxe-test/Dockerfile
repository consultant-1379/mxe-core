ARG TAG=latest 
FROM armdockerhub.rnd.ericsson.se/ubuntu:$TAG

LABEL maintainer="BCSS CNS CSE MLOps Arch & Design Unit <PDLSCSEMLO@pdl.internal.ericsson.com>" \
      description="Robot Framework in Docker for the product MXE"

# Set the reports directory environment variable
ENV ROBOT_REPORTS_DIR /opt/robotframework/reports

# Set the tests directory environment variable
ENV ROBOT_TESTS_DIR /opt/robotframework/tests

# Set the testdata directory environment variable
ENV TEST_DATA_DIR ${ROBOT_TESTS_DIR}/testdata

# Set the Jmeter directory environment variable
ENV JMETER_WORKING_DIR /opt/jmeter

# Set the working directory environment variable
ENV ROBOT_WORK_DIR /opt/robotframework/temp

# Set number of threads for parallel execution
# By default, no parallelisation
ENV ROBOT_THREADS 1

ENV DEBIAN_FRONTEND=noninteractive

# Setting Jmeter version
ARG JMETER_VERSION="5.6.3"

# setting mxe version and build number
ARG MXE_VERSION="2.0.0"
ARG MXE_BUILD_NUMBER="120"

ARG GITHUB_TOKEN 
ENV GITHUB_TOKEN ${GITHUB_TOKEN}

# Install system dependencies
RUN apt-get update \
   && apt-get install --quiet --assume-yes \
        python3-pip \
        unzip \
        firefox \
        wget \
        curl \
        vim \
        ca-certificates \
        git \
        jq \
        xvfb \
        default-jre

# Install nodejs
RUN curl -sL https://deb.nodesource.com/setup_18.x -o /tmp/nodesource_setup.sh \
    && chmod +x /tmp/nodesource_setup.sh                                       \
    && ./tmp/nodesource_setup.sh                                           \
    && apt-get install nodejs --assume-yes

# Install load geenrator tool: https://github.com/rakyll/hey
RUN wget https://hey-release.s3.us-east-2.amazonaws.com/hey_linux_amd64 \
    && chmod +x hey_linux_amd64 && mv hey_linux_amd64 /usr/local/bin/hey

# Install chrome package
RUN wget --no-verbose https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    dpkg --install google-chrome-stable_current_amd64.deb; apt-get --fix-broken --assume-yes install

#Install robotframework and required libraries from the requirements file
COPY requirements.txt .
RUN pip3 install --no-cache-dir -r requirements.txt

# Install webdrivers for chrome and firefox
COPY ./scripts/download_drivers.sh /tmp 
RUN chmod +x /tmp/download_drivers.sh && /tmp/download_drivers.sh

# Create the default report and work folders with the default user to avoid runtime issues
# These folders are writeable by anyone, to ensure the user can be changed on the command line.
RUN mkdir -p ${ROBOT_REPORTS_DIR} \
    && mkdir -p ${ROBOT_WORK_DIR} \
    && mkdir -p ${JMETER_WORKING_DIR} \
    && chmod ugo+w ${ROBOT_REPORTS_DIR} ${ROBOT_WORK_DIR} ${JMETER_WORKING_DIR}

# Installing Jmeter
RUN curl -L --silent https://dlcdn.apache.org/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz > /tmp/apache-jmeter-${JMETER_VERSION}.tgz \
    && tar -xzf /tmp/apache-jmeter-${JMETER_VERSION}.tgz -C ${JMETER_WORKING_DIR}


# Updating the MXE root CA certificate in "/etc/ssl/certs/ca-certificates.crt" for mxe-cli and in "/usr/local/lib/python3.10/site-packages/certifi/cacert.pem" for request library in robot framework
RUN curl -L http://pki.ericsson.se/CertData/EGADRootCA.crt >> /usr/local/share/ca-certificates/EGADRootCA.crt \
    && update-ca-certificates \
    && cat /usr/local/share/ca-certificates/EGADRootCA.crt >> /usr/local/lib/python3.10/dist-packages/certifi/cacert.pem

# Install Litmusctl and dependant tools for reliability tests
RUN wget https://litmusctl-production-bucket.s3.amazonaws.com/litmusctl-linux-amd64-1.1.0.tar.gz \
    && tar xvzf litmusctl-linux-amd64-1.1.0.tar.gz   \
    && cp ./litmusctl /usr/bin/

# Initialize robot frameworks browser library
RUN rfbrowser init

# install browser library system dependencies
RUN npx playwright install-deps

# Allow any user to write logs
RUN chmod ugo+w /var/log

# Remove apt-get cache
RUN apt-get --fix-broken --assume-yes install \
    && apt-get autoremove --assume-yes \
    && apt-get clean --assume-yes

# Update system path
ENV PYTHONPATH=${ROBOT_TESTS_DIR}

# A dedicated work folder to allow for the creation of temporary files
WORKDIR ${ROBOT_WORK_DIR}

# Running the CLI tests
CMD ["/bin/bash"]