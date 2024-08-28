

# Absolute path to this script. /home/user/bin/foo.sh

case "$(uname -s)" in
    Darwin*) SCRIPT=$(greadlink -f $0) ;;
    *)       SCRIPT=$(readlink -f $0)
esac

# Absolute path this script is in. /home/user/bin
SCRIPTPATH=$(dirname $SCRIPT)
SERVICEROOT=$(dirname $SCRIPTPATH)

export DM_CONFIG_FILE=${SERVICEROOT}/resources/config.yaml
#export ARGOCD_CONFIG_FILE=${SERVICEROOT}/resources/argocdconfig.yaml

#sh -x ${SERVICEROOT}/scripts/get_argocd_config.sh

##export PATH=${PATH}:${ROOT}/hack

echo $PATH

#export PATH=$BASEPATH/hack/:$PATH
${SERVICEROOT}/bin/depmanager --kubeconfig $HOME/.kube/config --namespace mee-argocd

