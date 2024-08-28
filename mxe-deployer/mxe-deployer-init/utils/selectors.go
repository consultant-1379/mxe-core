package utils

import "k8s.io/apimachinery/pkg/labels"

var (
	ArgocdCMLabelSelector = labels.SelectorFromSet(labels.Set(map[string]string{
		"app.kubernetes.io/part-of": "argocd",
		"app.kubernetes.io/name":    "argocd-cm",
	})).String()

	ArgocdRBACCMLabelSelector = labels.SelectorFromSet(labels.Set(map[string]string{
		"app.kubernetes.io/part-of": "argocd",
		"app.kubernetes.io/name":    "argocd-rbac-cm",
	})).String()

	ArgocdSecretSelector = labels.SelectorFromSet(labels.Set(map[string]string{
		"app.kubernetes.io/part-of": "argocd",
		"app.kubernetes.io/name":    "argocd-secret",
	})).String()
)
