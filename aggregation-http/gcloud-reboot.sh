#!/bin/bash
kubectl scale rc a-controller --replicas=0
kubectl scale rc b-controller --replicas=0
kubectl scale rc c-controller --replicas=0
kubectl scale rc d-controller --replicas=0
kubectl scale rc e-controller --replicas=0
echo "Waiting scaling"
sleep 5 #wait 5 seconds
kubectl scale rc a-controller --replicas=1
kubectl scale rc b-controller --replicas=1
kubectl scale rc c-controller --replicas=1
kubectl scale rc d-controller --replicas=1
kubectl scale rc e-controller --replicas=1
echo "Reboot finished"
