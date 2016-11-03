#!/bin/bash
#https://www.ianlewis.org/en/using-kubernetes-namespaces-manage-environments
#http://kubernetes.io/docs/admin/namespaces/walkthrough/

#kubectl create -f kube-mexample.yaml
#export CONTEXT=$(kubectl config view | awk '/current-context/ {print $2}')
#kubectl config set-context $CONTEXT --namespace=<insert-namespace-name-here>
echo "Creating Deployments"
kubectl create -f kube-a-controller.yaml
kubectl create -f kube-b-controller.yaml
kubectl create -f kube-c-controller.yaml
kubectl create -f kube-d-controller.yaml
kubectl create -f kube-e-controller.yaml
echo "Creating Services"
kubectl expose -f kube-a-service.yaml --type="LoadBalancer"
kubectl create -f kube-b-service.yaml
kubectl create -f kube-c-service.yaml
kubectl create -f kube-d-service.yaml
kubectl create -f kube-e-service.yaml
#gcloud compute project-info describe --project $PROJECT_ID
#gcloud container clusters get-credentials cluster --zone $CLOUDSDK_COMPUTE_ZONE --project $PROJECT_ID
#gcloud container clusters get-credentials cluster-1 --zone europe-west1-c --project massive-plasma-161616

#echo "Creating Hazelcast Node"
#kubectl create -f kube-hazelcast-deployment.yaml
#kubectl create -f kube-hazelcast-service.yaml
#kubectl scale deployment hazelcast --replicas 10
