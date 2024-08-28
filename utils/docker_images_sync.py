#!/bin/python3

# This script uses skopeo to sync specific set of 3PP source images to user desired destination in arm
#
# FOR USAGE INSTRUCTIONS: See section Sync Docker images from Dockerhub section in utils/README.md
#

import asyncio
import os

# pre-requisite : install skopeo. See https://github.com/containers/skopeo/blob/master/install.md

mxeRegistry = "armdocker.rnd.ericsson.se/proj-mxe"

# master list of source and destination
# add all sources and destinations here
syncMap = {

    "spark-operator": {
        "gcr.io/spark-operator/spark-operator:v1beta2-1.2.0-3.0.0": f"{mxeRegistry}/spark/spark-operator:v1beta2-1.2.0-3.0.0",
    },
    "seldon-core-operator": {

        "seldonio/seldon-core-executor:1.9.0": f"{mxeRegistry}/seldonio/seldon-core-executor:1.9.0",
        "seldonio/seldon-core-operator:1.9.0": f"{mxeRegistry}/seldonio/seldon-core-operator:1.9.0",
    },

    "argo-workflow": {
        "quay.io/argoproj/argocli:v3.3.8": f"{mxeRegistry}/quay.io/argoproj/argocli:v3.3.8",
        "quay.io/argoproj/workflow-controller:v3.3.8": f"{mxeRegistry}/quay.io/argoproj/workflow-controller:v3.3.8"
    }

}

# Set keys to newly added dict entries here, only these would be synced
keysToSync = ["argo-workflow"]


async def _read_stream(stream, callback):
    while True:
        line = await stream.readline()
        if line:
            callback(line)
        else:
            break


async def run(command):
    process = await asyncio.create_subprocess_shell(
        command, stdout=asyncio.subprocess.PIPE, stderr=asyncio.subprocess.PIPE
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


def sanitize(uri):
    if not uri.startswith("docker:"):
        return f"docker://{uri}"
    return uri


class DockerCredentials:
    def __init__(self) -> None:
        self.username = os.getenv("DOCKER_USER")
        self.password = os.getenv("DOCKER_PASSWORD")
        self.validate()

    def validate(self):
        if not self.username or not self.password:
            raise Exception("Docker credentials not passed")


class SyncCommand:
    def __init__(self, dockercreds, source, destination) -> None:
        self.source = source
        self.destination = destination
        self.dockercreds = dockercreds
        self.cmd = self.command()
        self.return_code = -1

    def command(self):
        return f"skopeo copy --all --dest-creds {self.dockercreds.username}:{self.dockercreds.password} {sanitize(self.source)} {sanitize(self.destination)}"

    def printStatus(self):
        print(
            f"\nsource: {self.source} destination: {self.destination} sync_return_code: {self.return_code}")


async def main():
    docker_creds = DockerCredentials()
    sync_commands = []
    for key in keysToSync:
        print(f"Syncing ...  {syncMap[key]}")
        for source, destination in syncMap[key].items():
            sync_command = SyncCommand(
                docker_creds, source=source, destination=destination)
            sync_commands.append(sync_command)
            sync_command.return_code = await run(command=sync_command.cmd)

    for cmds in sync_commands:
        cmds.printStatus()


if __name__ == "__main__":
    loop = asyncio.get_event_loop()
    loop.run_until_complete(main())
