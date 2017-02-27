#!/bin/bash
docker run -d -p 9000:9000 -p 9092:9092 --name sonarqube library/sonarqube:alpine
