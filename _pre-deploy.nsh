#!/bin/nuts
ntemplate -p dir-template
ndocusaurus -d website build
cp website/nuts/build docs
_nuts_version=$(nuts nprops -f METADATA apiVersion)
cp ~/.m2/repository/net/vpc/app/nuts/nuts/${_nuts_version}/nuts-${_nuts_version}.jar nuts.jar

