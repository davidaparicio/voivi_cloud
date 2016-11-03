#!/bin/bash
oc login --username=admin --password=admin --insecure-skip-tls-verify
oc new-project mexample --display-name="Cloud Development" --description="Microservice aggregation"
oc policy add-role-to-user admin admin -n mexample
oc policy add-role-to-group view system:serviceaccounts -n mexample
