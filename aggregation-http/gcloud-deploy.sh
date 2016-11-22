#!/bin/bash
export CONTAINER_VER="latest"
export CLOUDCONT_VER="gcloud"
export GREPO="eu.gcr.io"

echo "Starting deployment"
docker tag voivi/a:$CONTAINER_VER $GREPO/$PROJECT_ID/a:$CLOUDCONT_VER
docker tag voivi/b:$CONTAINER_VER $GREPO/$PROJECT_ID/b:$CLOUDCONT_VER
docker tag voivi/c:$CONTAINER_VER $GREPO/$PROJECT_ID/c:$CLOUDCONT_VER
docker tag voivi/d:$CONTAINER_VER $GREPO/$PROJECT_ID/d:$CLOUDCONT_VER
docker tag voivi/e:$CONTAINER_VER $GREPO/$PROJECT_ID/e:$CLOUDCONT_VER
gcloud docker push $GREPO/$PROJECT_ID/a:$CLOUDCONT_VER
gcloud docker push $GREPO/$PROJECT_ID/b:$CLOUDCONT_VER
gcloud docker push $GREPO/$PROJECT_ID/c:$CLOUDCONT_VER
gcloud docker push $GREPO/$PROJECT_ID/d:$CLOUDCONT_VER
gcloud docker push $GREPO/$PROJECT_ID/e:$CLOUDCONT_VER
echo "Deployment finished"
kubectl scale deployment a --replicas=0
kubectl scale deployment b --replicas=0
kubectl scale deployment c --replicas=0
kubectl scale deployment d --replicas=0
kubectl scale deployment e --replicas=0
echo "Updating to the lastest image..."
kubectl set image deployment/a a=$GREPO/$PROJECT_ID/a:$CLOUDCONT_VER
kubectl set image deployment/b b=$GREPO/$PROJECT_ID/b:$CLOUDCONT_VER
kubectl set image deployment/c c=$GREPO/$PROJECT_ID/c:$CLOUDCONT_VER
kubectl set image deployment/d d=$GREPO/$PROJECT_ID/d:$CLOUDCONT_VER
kubectl set image deployment/e e=$GREPO/$PROJECT_ID/e:$CLOUDCONT_VER
echo "Update finished"
kubectl scale deployment a --replicas=1
kubectl scale deployment b --replicas=3
kubectl scale deployment c --replicas=3
kubectl scale deployment d --replicas=3
kubectl scale deployment e --replicas=3

#<service-name>.<namespace-name>.svc.cluster.local
#a-vertx-microservice-example-aggregation-http.192.168.99.100.xip.io/assets/index.html
#http://kubernetes.io/docs/user-guide/namespaces/
#kubectl scale rc d-controller --replicas=0
