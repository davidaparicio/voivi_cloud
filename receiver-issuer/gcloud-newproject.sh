#!/bin/bash
echo "Creating Deployments"
kubectl create -f kube-receiver-controller.yaml
kubectl create -f kube-issuer-controller.yaml
echo "Creating Services"
kubectl expose -f kube-receiver-service.yaml --type="LoadBalancer"
kubectl create -f kube-issuer-service.yaml
