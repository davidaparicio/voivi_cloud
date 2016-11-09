#!/bin/bash
minishift start --deploy-registry --deploy-router \
                --vm-driver=virtualbox --cpus=2 \
                --disk-size=80g
echo "If you want to use the Docker inside the minishift"
echo "Run: eval $(minishift docker-env)"

# https://github.com/adlogix/docker-machine-nfs/issues/62#issuecomment-254035784g
# https://github.com/docker/machine/issues/1709#issuecomment-161026484
# 192.168.98.100
# docker-machine create -d virtualbox --virtualbox-hostonly-cidr "192.168.98.1/24" m98
#
# 192.168.97.100
# docker-machine create -d virtualbox --virtualbox-hostonly-cidr "192.168.97.1/24" m97
#
# 192.168.96.100
# docker-machine create -d virtualbox --virtualbox-hostonly-cidr "192.168.96.1/24" m96
#
# https://github.com/docker/machine/issues/1709#issuecomment-161026484
# echo "ifconfig eth1 192.168.99.50 netmask 255.255.255.0 broadcast 192.168.99.255 up" | docker-machine ssh prova-discovery sudo tee /var/lib/boot2docker/bootsync.sh > /dev/null
