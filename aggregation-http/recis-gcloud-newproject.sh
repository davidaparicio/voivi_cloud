#!/bin/bash
echo "Creating Deployments"
kubectl create -f receiver.yaml
kubectl create -f issuer.yaml
echo "Creating Services"
kubectl expose -f receiver.yaml --type="LoadBalancer"
kubectl create -f issuer.yaml
