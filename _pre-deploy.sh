# generates
nuts ntemplate -p dir-template
nuts ndocusaurus -d website/nuts build
nuts nsh cp --sync website/nuts/build docs
_nuts_version=`nuts nprops -f METADATA apiVersion`
nuts nsh cp --sync ~/.m2/repository/net/vpc/app/nuts/nuts/${_nuts_version}/nuts-${_nuts_version}.jar nuts.jar

