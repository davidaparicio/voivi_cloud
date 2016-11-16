#!/bin/bash
#DOCKER_OPTS="--bridge=cbr0 --iptables=false --ip-masq=false"
#http://kubernetes.io/docs/admin/networking/

echo "Scaling down all the Replication controller..."
oc scale rc aggregation-http-a --replicas=0
oc scale rc aggregation-http-b --replicas=0
oc scale rc aggregation-http-c --replicas=0
oc scale rc aggregation-http-d --replicas=0
oc scale rc aggregation-http-e --replicas=0
echo "Creation of the lastest image..."
cd A
mvn clean package docker:build fabric8:json fabric8:apply -Popenshift
cd ../D
mvn clean package docker:build fabric8:json fabric8:apply -Popenshift
cd ../E
mvn clean package docker:build fabric8:json fabric8:apply -Popenshift
cd ../B
mvn clean package docker:build fabric8:json fabric8:apply -Popenshift
cd ../C
mvn clean package docker:build fabric8:json fabric8:apply -Popenshift
echo "Update finished"

# minishift start --deploy-registry --deploy-router
# minishift ip
# oc login --username=admin --password=admin
# eval $(minishift docker-env)

# oc projects
# oc project <myproject>
# oc run hello-minishift \
# --image=gcr.io/google_containers/echoserver:1.4 \
# --port=8080 --expose \
# --service-overrides='{"apiVersion": "v1", "spec": {"type": "NodePort"}}'
# oc delete dc hello-minishift -n default
# oc cluster up --version=v1.3.0

# dig +short service.openshiftIP.xip.io
# docker exec -it <containerIdOrName> bash
# oc config set-context minishift
# minishift start --vm-driver=virtualbox --show-libmachine-logs --stderrthreshold=0
# mv ${HOME}/.kube/config ${HOME}/.kube/config.old
# kubectl config current-context

# oc delete dc hello-minishift -n default
# oc adm policy add-scc-to-user hostnetwork -z router
# oc adm router --create
# https://github.com/openshift/origin/blob/master/docs/generated/oc_by_example_content.adoc
# sudo tcpdump -i vboxnet1 -vv port bootps

# VirtualBox is the default hypervisor, and you'll probably need to disable its DHCP server
# VBoxManage dhcpserver remove --netname HostInterfaceNetworking-vboxnet0
