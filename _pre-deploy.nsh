#!/bin/nuts
here=$(dirname $0)
source $here/dir-template/vars.nsh
here=$(dirname $0)
echo run ntemplate
ntemplate   -p $here/dir-template
echo copy nuts.jar
echo cp ~/.m2/repository/net/thevpc/nuts/nuts/${apiVersion}/nuts-${apiVersion}.jar $here/website/static/nuts.jar
cp ~/.m2/repository/net/thevpc/nuts/nuts/${apiVersion}/nuts-${apiVersion}.jar $here/website/static/nuts.jar
echo run ndocusaurus
ndocusaurus -d website build


