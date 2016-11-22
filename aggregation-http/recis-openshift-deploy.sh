#!/bin/bash
./pipeline-openshift.sh
echo "Scaling down all the Replication controller..."
oc scale rc receiver --replicas=0
oc scale rc issuer --replicas=0
echo "Creation of the lastest image..."
cd receiver
mvn clean package docker:build fabric8:json fabric8:apply -Popenshift
cd ../issuer
mvn clean package docker:build fabric8:json fabric8:apply -Popenshift
echo "Update finished"
