#!/bin/nuts
ntemplate -p dir-template
#ndocusaurus -d website build
here=`dirname $0`
cp -r $here/website/build $here/docs
_nuts_version=$(nuts nsh -c props get apiVersion --props $here/METADATA)
cp ~/.m2/repository/net/vpc/theapp/nuts/nuts/${_nuts_version}/nuts-${_nuts_version}.jar $here/nuts.jar

