#!/usr/bin/env python3

import argparse
import datetime
import os
import subprocess
import time
from kubernetes import client, config

def valid_file_path(file_path):
    if os.path.isfile(file_path):
        return file_path
    else:
        raise argparse.ArgumentTypeError('The value "' + file_path +
                                         '" provided is not a readable file')

def parse_args():
    parser = argparse.ArgumentParser(
        description='Test tool for HELM installation and upgrade')
    parser.add_argument('-k', '--kubernetes-admin-conf',
                        dest='kubernetes_admin_conf',
                        type=valid_file_path, required=True,
                        metavar="KUBECONFIG",
                        help="Kubernetes admin conf to use")

    parser.add_argument('-n', '--kubernetes-namespace',
                        dest='kubernetes_namespace', type=str, required=True,
                        metavar='NAMESPACE',
                        help='Kubernetes namespace to use')

    parser.add_argument('-p', '--pod',
                        dest='excl_service', type=str, required=False,
                        default='',
                        metavar='POD',
                        help='Pod to be excluded from monitoring')

    args = parser.parse_args()

    return args


def d(t0):
    return str(datetime.datetime.now() - t0)


def log(*message):
    now = datetime.datetime.now()
    print(now.date().isoformat() + ' ' + now.time().isoformat() +
          ': ' + str(*message))


class KubernetesClient:
    def __init__(self, kubernetes_admin_conf):
        config.load_kube_config(config_file=kubernetes_admin_conf)
        self.core_v1 = client.CoreV1Api()
        self.apps_v1 = client.AppsV1beta2Api()
        self.pods = []

    def wait_for_all_resources(self, namespace_name, pod_name):
        self.wait_for_all_pods_to_start(namespace_name, pod_name)
        self.wait_for_all_replica_set(namespace_name, pod_name)
        self.wait_for_all_deployments(namespace_name, pod_name)

    def wait_for_all_pods_to_start(self, namespace_name, pod_name):
        def format_containers(i):
            if i.status.container_statuses:
                return '\n'.join(['\n        Containername: %s'
                                  '\n                Ready: %s'
                                  '\n              Waiting: %s' %
                                  (c.name,
                                   c.ready,
                                   str(c.state.waiting).replace('\n', ''))
                                  for c in i.status.container_statuses])
            else:
                return 'No container status'

        log('Pods:')
        counter = 60
        while True:
            self.pods = []
            api_response = self.core_v1.list_namespaced_pod(namespace_name)

            if pod_name == '':
                self.pods = api_response.items
            else:
                for i in api_response.items:
                    if not i.metadata.name.__contains__(''.join(['-%s-' % 
                                                                 (pod_name)])):
                        self.pods.append(i)
 
            log('\n'.join(['\nPodname: %s'
                           '\n    Phase: %s'
                           '\n    Containers: %s' %
                           (i.metadata.name, i.status.phase,
                            format_containers(i))
                           for i in self.pods]))
            
            if all([i.status.phase == 'Running' and
                    i.status.container_statuses and
                    all([cs.ready for cs in i.status.container_statuses])
                    for i in self.pods]):
                break

            if counter > 0:
                counter = counter - 1
                time.sleep(10)
            else:
                raise ValueError('Timeout waiting for pods to reach '
                                 'Ready & Running')

    def wait_for_all_pods_to_terminate(self, namespace_name, pod_name):
        log('Pods:')
        counter = 60
        while True:
            self.pods = []
            api_response = self.core_v1.list_namespaced_pod(namespace_name)

            if pod_name == '':
                self.pods = api_response.items
            else:
                for i in api_response.items:
                    if not i.metadata.name.__contains__(''.join(['-%s-' %
                                                                 (pod_name)])):
                        self.pods.append(i)

            if not self.pods:
                break
            else:
                log('\n'.join(['\nPhase: %s  Podname: %s' %
                               (i.status.phase, i.metadata.name)
                               for i in self.pods]))

            if counter > 0:
                counter = counter - 1
                time.sleep(10)
            else:
                raise ValueError('Timeout waiting for pods to terminate')

    def wait_for_all_deployments(self, namespace_name, pod_name):
        api_response = self.apps_v1.list_namespaced_deployment(namespace_name)
        deploys = []
        if pod_name == '':
            deploys = api_response.items
        else:
            for i in api_response.items:
                if not i.metadata.name.__contains__(''.join(['-%s-' %
                                                                 (pod_name)])):
                    deploys.append(i)

        log('Deployments:')
        log([(i.metadata.name, 'Replicas ready/desired: (%s/%d)' %
              (str(i.status.ready_replicas), i.spec.replicas))
             for i in deploys])

    def wait_for_all_replica_set(self, namespace_name, pod_name):
        api_response = self.apps_v1.list_namespaced_replica_set(namespace_name)
        replica_set = []
        if pod_name == '':
            replica_set = api_response.items
        else:
            for i in api_response.items:
                if not i.metadata.name.__contains__(''.join(['-%s-' %
                                                                 (pod_name)])):
                    replica_set.append(i)

        log('Replica sets:')
        log([(i.metadata.name, 'Replicas ready/desired: (%s/%d)' %
              (str(i.status.ready_replicas), i.spec.replicas))
             for i in replica_set])


def main():
    args = parse_args()
    target_namespace_name = args.kubernetes_namespace
    excl_pod = args.excl_service
    kube = KubernetesClient(args.kubernetes_admin_conf)
    kube.wait_for_all_resources(target_namespace_name, excl_pod)

if __name__ == "__main__":
    main()
