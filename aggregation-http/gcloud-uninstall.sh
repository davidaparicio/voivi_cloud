#!/bin/bash

echo "Starting clean"
echo "Deleting deployments"
kubectl delete deployment a
kubectl delete deployment b
kubectl delete deployment c
kubectl delete deployment d
kubectl delete deployment e
echo "Deleting services"
kubectl delete service a
kubectl delete service b
kubectl delete service c
kubectl delete service d
kubectl delete service e
echo "Clean done!"
