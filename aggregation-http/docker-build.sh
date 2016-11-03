#!/bin/bash
docker build --build-arg MODULE=A --build-arg MOD=a --build-arg PORT=8080 --tag vertx-examples/agg-a .
docker build --build-arg MODULE=B --build-arg MOD=b --build-arg PORT=8081 --tag vertx-examples/agg-b .
docker build --build-arg MODULE=C --build-arg MOD=c --build-arg PORT=8082 --tag vertx-examples/agg-c .
docker build --build-arg MODULE=D --build-arg MOD=d --build-arg PORT=8083 --tag vertx-examples/agg-d .
