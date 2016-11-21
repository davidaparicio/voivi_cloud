#!/bin/bash
echo "Starting clean"
echo "Deleting deployments"
kubectl delete deployment receiver
kubectl delete deployment issuer
echo "Deleting services"
kubectl delete service receiver
kubectl delete service issuer
echo "Clean done!"
