#!/bin/bash


SPARK_OPERATOR_DEPLOYMENT_NAME=$(kubectl get deployments -n ${NAMESPACE} -lapp.kubernetes.io/name=spark-operator -o name)

kubectl rollout restart $SPARK_OPERATOR_DEPLOYMENT_NAME -n ${NAMESPACE}

kubectl rollout status $SPARK_OPERATOR_DEPLOYMENT_NAME  -n ${NAMESPACE}