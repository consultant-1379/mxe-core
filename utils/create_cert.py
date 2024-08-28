#!/bin/python3

#
# This script can be used for
#   a) generation of a ssl conf, and csr based on config
#   b) once a cert is created from ECS, merge intermediate CA and create tls secret on k8s
#
# FOR DETAILED USAGE INSTRUCTIONS: See section TLS CERT generation helper in utils/README.md
#
#

import asyncio
import codecs
import json
import os
import pathlib
import sys


class Endpoint:
    def __init__(self, domain_endpoint, app_name, ericsson_email_address) -> None:
        self.domain_endpoint = domain_endpoint
        self.app_name = app_name
        self.ericsson_email_address = ericsson_email_address
        self.conf = self.get_conf()

    def get_conf(self):
        return f"""
[req]
default_bits = 2048
distinguished_name = dn
prompt = no
req_extensions = req_ext

[dn]
CN = {self.domain_endpoint}
O = Ericsson
OU = IT Services
L = Stockholm
ST = Stockholm
C = SE
emailAddress = {self.ericsson_email_address}

[req_ext]
subjectAltName = DNS: {self.domain_endpoint}

"""


class Config:
    def __init__(self) -> None:
        self._config = self.init()
        self.cwd = self.get("cwd")
        self.namespace = self.get("namespace")
        self.endpoints = self.read_endpoints()

    def init(self):
        with open("cert_config.json", "r") as f:
            _config = json.load(f)
        return _config

    def read_endpoints(self):
        ericsson_email_address = self.get("ericsson_email_address")
        return [Endpoint(x["domain_address"], x["app_name"], ericsson_email_address) for x in self._config["endpoints"]]

    def get(self, key):
        return self._config.get(key, "")


config = Config()


async def _read_stream(stream, callback):
    while True:
        line = await stream.readline()
        if line:
            callback(line)
        else:
            break


async def run(command):
    print(f"Executing\n {command}")
    process = await asyncio.create_subprocess_shell(
        command, stdout=asyncio.subprocess.PIPE, stderr=asyncio.subprocess.PIPE,
        cwd=config.cwd
    )

    await asyncio.wait(
        [
            _read_stream(
                process.stdout,
                lambda x: print(
                    "STDOUT: {}".format(x.decode("UTF8"))
                ),
            ),
            _read_stream(
                process.stderr,
                lambda x: print(
                    "STDERR: {}".format(x.decode("UTF8"))
                ),
            ),
        ]
    )

    await process.wait()
    return process.returncode


async def write_conf(endpoint):
    folder = pathlib.Path(config.cwd)
    folder.mkdir(parents=True, exist_ok=True)
    filePath = folder.joinpath(f"{endpoint.app_name}.conf")
    x = filePath.write_text(endpoint.get_conf())
    if x <= 0:
        raise Exception("file is not written")
    print(filePath)


def handle(returncode, errorMsg, successMsg):
    print(returncode)
    if returncode != 0:
        raise Exception(errorMsg)
    else:
        print(successMsg)


async def make_csr():
    for endpoint in config.endpoints:
        await write_conf(endpoint)
        command = f"openssl req -new -nodes -keyout {endpoint.app_name}.key -out {endpoint.app_name}.csr -config {endpoint.app_name}.conf"
        returncode = await run(command)
        handle(returncode, "failed to create csr", "created csr")


# unused because each sed is platform native. use removeBomInplace instead
async def removeBom(endpoint):
    command = f"sed -i '1s/^\xEF\xBB\xBF//' {endpoint.app_name}.cer"
    returncode = await run(command)
    handle(returncode, "failed to remove bom", "removed bom")


async def mergeInterimCert(endpoint):
    command = f"curl -L http://pki.ericsson.se/CertData/EGADIssuingCA3.crt >> {endpoint.app_name}.cer"
    returncode = await run(command)
    handle(returncode, "failed to merge interim cert", "merged interim cert")


async def make_secret(endpoint):
    command = f"kubectl create secret tls {endpoint.app_name}-tls --key {config.cwd}/{endpoint.app_name}.key --cert {config.cwd}/{endpoint.app_name}.cer -n {config.namespace}"
    returncode = await run(command)
    handle(returncode, "failed to create secret cert", "made the tls secret")


def remove_bom_inplace(endpoint):
    """Removes BOM mark, if it exists, from a file and rewrites it in-place"""
    buffer_size = 4096
    bom_length = len(codecs.BOM_UTF8)
    path = pathlib.Path(
        config.cwd).joinpath(f"{endpoint.app_name}.cer")

    with open(path, "r+b") as fp:
        chunk = fp.read(buffer_size)
        if chunk.startswith(codecs.BOM_UTF8):
            i = 0
            chunk = chunk[bom_length:]
            while chunk:
                fp.seek(i)
                fp.write(chunk)
                i += len(chunk)
                fp.seek(bom_length, os.SEEK_CUR)
                chunk = fp.read(buffer_size)
            fp.seek(-bom_length, os.SEEK_CUR)
            fp.truncate()


async def make_cert(skip_steps=False):
    for endpoint in config.endpoints:
        if not skip_steps:
            remove_bom_inplace(endpoint)
            await mergeInterimCert(endpoint)
        await make_secret(endpoint)


async def main():
    if len(sys.argv) < 2:
        raise Exception(
            "outputtype param missing. Supply: csr/cert. See utils/README.md")
    else:
        option = sys.argv[1]
        if option not in ["cert", "csr"]:
            raise Exception(
                "valid options are csr or cert. See utils/README.md")

    if option == "csr":
        await make_csr()
    elif option == "cert":
        if len(sys.argv) > 2 and sys.argv[2] == "--skip-steps":
            print("only creating certs")
            skip_steps = True
        elif len(sys.argv) > 2:
            print("bad option:" + sys.argv[2])
        else:
            skip_steps = False
        print(skip_steps)
        await make_cert(skip_steps)

if __name__ == "__main__":
    loop = asyncio.get_event_loop()
    loop.run_until_complete(main())
