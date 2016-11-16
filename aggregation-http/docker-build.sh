#!/bin/bash
docker build --build-arg MODULE=A --build-arg MOD=a --build-arg PORT=8080 --tag vertx-examples/agg-a .
docker build --build-arg MODULE=B --build-arg MOD=b --build-arg PORT=8081 --tag vertx-examples/agg-b .
docker build --build-arg MODULE=C --build-arg MOD=c --build-arg PORT=8082 --tag vertx-examples/agg-c .
docker build --build-arg MODULE=D --build-arg MOD=d --build-arg PORT=8083 --tag vertx-examples/agg-d .

#docker pull hello-world; docker pull alpine;docker pull seqvence/static-site
#docker pull microsoft/dotnet:1.0.0-preview1;docker pull node:5.11.0-slim;docker pull python:2.7-alpine;docker pull redis:alpine;docker pull postgres:9.4
#git clone git://github.com/jpetazzo/orchestration-workshop;cd orchestration-workshop/dockercoins;docker-compose build

#http://a-mexample.192.168.99.100.xip.io/A?name=I%20love%20tests%20and%20my%20PhD
#http://130.211.55.5:8080/A?name=I%20love%20tests%20and%20my%20PhD
#https://192.168.99.100:8443/console/project/mexample/overview
