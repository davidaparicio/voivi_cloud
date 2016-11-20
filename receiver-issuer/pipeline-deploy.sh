#!/bin/bash
CONTEXT_MSHIFT="mexample/192-168-99-100:8443/admin"
CONTEXT_GCLOUD="gke_massive-plasma-161616_europe-west1-c_cluster-1"
kubectl config use-context $CONTEXT_MSHIFT
oc login --username=admin --password=admin --insecure-skip-tls-verify=true
./openshift-deploy.sh
kubectl config use-context $CONTEXT_GCLOUD
./gcloud-deploy.sh
