#!/bin/bash
docker run -d -p 9999:9000 -v /var/run/docker.sock:/var/run/docker.sock portainer/portainer --swarm
