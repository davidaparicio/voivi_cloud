#!/bin/bash
CONTEXT_MSHIFT="mexample/192-168-99-100:8443/admin"
CONTEXT_GCLOUD="gke_massive-plasma-161616_europe-west1-c_cluster-1"
./pipeline-openshift.sh
./openshift-deploy.sh
kubectl config use-context $CONTEXT_GCLOUD
./gcloud-deploy.sh

#gcloud container clusters get-credentials cluster --zone=$CLOUDSDK_COMPUTE_ZONE --project=$PROJECT_ID
#kubectl config view
#kubectl cluster-info
#open http://localhost:8001/ui
open http://localhost:8001/api/v1/proxy/namespaces/kube-system/services/kubernetes-dashboard/#/workload?namespace=mexample
#kubectl proxy
