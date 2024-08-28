# Absolute path to this script. /home/user/bin/foo.sh
#!/usr/bin/env bash
case "$(uname -s)" in
    Darwin*) SCRIPT=$(greadlink -f $0) ;;
    *)       SCRIPT=$(readlink -f $0)
esac

# Absolute path this script is in. /home/user/bin
echo "script path:" $SCRIPT

SCRIPTPATH=`dirname $SCRIPT`

echo "script path:" $SCRIPT

RESOURCES_PATH=$(cd $SCRIPTPATH && cd .. && pwd)/resources

echo "load into arcgocd yaml path:" ${RESOURCES_PATH}/argocdconfig.yaml

kubectl config use-context aks-gaiasi-dev-sea-03
kubectl get cm argocd-cm -n argocd -o yaml | yq eval '.data' - > ${RESOURCES_PATH}/argocdconfig.yaml