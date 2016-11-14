#!/bin/bash
minishift docker-env
#docker rmi $(docker images | grep "^<none>" | awk "{print $3}")
