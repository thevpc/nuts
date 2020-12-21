#!/bin/nuts
ntemplate -p dir-template
ndocusaurus -d website build
here=`dirname $0`
cp -r $here/website/nuts/build $here/docs
_nuts_version=$(nuts nprops -f METADATA apiVersion)
cp ~/.m2/repository/net/vpc/theapp/nuts/nuts/${_nuts_version}/nuts-${_nuts_version}.jar nuts.jar

