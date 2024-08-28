package utils

import (
	"context"
	"fmt"
	"os"

	apiv1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/clientcmd"
	"mxe.ericsson/mxe-deploy-init/pkg/errors"
)

func GetKubeClientSet(kubeconfig string) (*kubernetes.Clientset, error) {
	var config *rest.Config
	var err error
	if _, incluster := os.LookupEnv("KUBERNETES_SERVICE_HOST"); incluster {
		config, err = rest.InClusterConfig()
	} else {
		config, err = clientcmd.BuildConfigFromFlags("", kubeconfig)
	}

	if err != nil {
		panic(err.Error())
	}
	kubeClientSet, err := kubernetes.NewForConfig(config)
	if err != nil {
		return nil, err
	}
	return kubeClientSet, nil
}

func GetSecretRefValue(namespace string, client *kubernetes.Clientset, secretSelector *apiv1.SecretKeySelector) (string, error) {

	secret, err := client.CoreV1().Secrets(namespace).Get(context.TODO(), secretSelector.Name, metav1.GetOptions{})
	if err != nil {
		return "", err
	}

	if data, ok := secret.Data[secretSelector.Key]; ok {
		return string(data), nil
	}
	return "", fmt.Errorf("key %s not found in secret %s", secretSelector.Key, secretSelector.Name)
}

func GetSecret(namespace string, client *kubernetes.Clientset, labelSelector string) (*apiv1.Secret, error) {

	listOptions := metav1.ListOptions{
		LabelSelector: labelSelector,
	}

	secretsList, err := client.CoreV1().Secrets(namespace).List(context.TODO(), listOptions)
	errors.CheckError(err)

	if len(secretsList.Items) != 1 {
		return nil, fmt.Errorf("found %d argocd-secrets matching label %s ", len(secretsList.Items), labelSelector)
	}
	return &secretsList.Items[0], nil
}

func UpdateSecret(namespace string, client *kubernetes.Clientset, secret *apiv1.Secret) (*apiv1.Secret, error) {

	return client.CoreV1().Secrets(namespace).Update(context.TODO(), secret, metav1.UpdateOptions{})
}

func GetConfigMap(namespace string, client *kubernetes.Clientset, labelSelector string) (*apiv1.ConfigMap, error) {

	listOptions := metav1.ListOptions{
		LabelSelector: labelSelector,
	}

	configMaps, err := client.CoreV1().ConfigMaps(namespace).List(context.TODO(), listOptions)
	errors.CheckError(err)

	if len(configMaps.Items) != 1 {
		return nil, fmt.Errorf("found %d argocd-cm matching label %s ", len(configMaps.Items), labelSelector)
	}
	return &configMaps.Items[0], nil
}

func UpdateConfigMap(namespace string, client *kubernetes.Clientset, cm *apiv1.ConfigMap) (*apiv1.ConfigMap, error) {

	return client.CoreV1().ConfigMaps(namespace).Update(context.TODO(), cm, metav1.UpdateOptions{})
}
