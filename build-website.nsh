#!/bin/nuts
## call this script (you need to be located in nuts github repository root folder) using the following command:
##  java -jar core/nuts/target/nuts-0.8.3.jar -ZSby --bot -w development-build ./build-website.nsh
## in order to build nuts website. This assumes that nuts is built and installed in the current machine
## Note that this will create a new workspace 'development-build' (and reset it) to use it
## if not, you need at least 'mvn clean install' first
## N.B.:  please make sure to replace '0.8.3' with the actual nuts version

## install dependencies
nuts -y install ntemplate ndocusaurus

## load variables and versions
source $(dirname $0)/.dir-template/vars.nsh
here=$(dirname $0)

## update github README and METADATA files
echo run ntemplate
ntemplate -p $here/.dir-template

## update nuts.jar
echo copy nuts.jar
echo cp ~/.m2/repository/net/thevpc/nuts/nuts/${apiVersion}/nuts-${apiVersion}.jar $here/website/static/nuts.jar
cp ~/.m2/repository/net/thevpc/nuts/nuts/${apiVersion}/nuts-${apiVersion}.jar $here/website/static/nuts.jar


## update docusaurus website
echo run ndocusaurus
ndocusaurus -d website pdf build


