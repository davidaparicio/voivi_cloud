#!/bin/bash
export CONTAINER_VER="latest"
export CLOUDCONT_VER="v22"
export GREPO="eu.gcr.io"

echo "Starting deployment"
docker tag voivi/receiver:$CONTAINER_VER $GREPO/$PROJECT_ID/receiver:$CLOUDCONT_VER
docker tag voivi/issuer:$CONTAINER_VER $GREPO/$PROJECT_ID/issuer:$CLOUDCONT_VER
gcloud docker push $GREPO/$PROJECT_ID/receiver:$CLOUDCONT_VER
gcloud docker push $GREPO/$PROJECT_ID/issuer:$CLOUDCONT_VER
echo "Deployment finished"
kubectl scale deployment receiver --replicas=0
kubectl scale deployment issuer --replicas=0
echo "Updating to the lastest image..."
kubectl set image deployment/receiver receiver=$GREPO/$PROJECT_ID/receiver:$CLOUDCONT_VER
kubectl set image deployment/isssuer issuer=$GREPO/$PROJECT_ID/issuer:$CLOUDCONT_VER
echo "Update finished"
kubectl scale deployment receiver --replicas=1
kubectl scale deployment issuer --replicas=1
