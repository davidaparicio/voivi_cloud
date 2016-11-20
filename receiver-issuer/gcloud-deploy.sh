#!/bin/bash
export CONTAINER_VER="latest"
export CLOUDCONT_VER="v1"
export GREPO="eu.gcr.io"

echo "Starting deployment"
docker tag vertx-microservices-example/receiver:$CONTAINER_VER $GREPO/$PROJECT_ID/receiver:$CLOUDCONT_VER
docker tag vertx-microservices-example/issuer:$CONTAINER_VER $GREPO/$PROJECT_ID/issuer:$CLOUDCONT_VER
gcloud docker push $GREPO/$PROJECT_ID/receiver:$CLOUDCONT_VER
gcloud docker push $GREPO/$PROJECT_ID/issuer:$CLOUDCONT_VER
echo "Deployment finished"
kubectl scale deployment receiver --replicas=0
kubectl scale deployment issuer --replicas=0
echo "Updating to the lastest image..."
kubectl set image deployment/receiver receiver=$GREPO/$PROJECT_ID/receiver:$CLOUDCONT_VER
kubectl set image deployment/issuer issuer=$GREPO/$PROJECT_ID/issuer:$CLOUDCONT_VER
echo "Update finished"
kubectl scale deployment receiver --replicas=1
kubectl scale deployment issuer --replicas=1
