#!/bin/bash
CONTEXT_MSHIFT="mexample/192-168-99-100:8443/admin"
kubectl config use-context $CONTEXT_MSHIFT
oc login --username=admin --password=admin --insecure-skip-tls-verify=true
