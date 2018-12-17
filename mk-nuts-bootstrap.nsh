#!/bin/nsh
base=$(dirname $0)
bootVersion=`properties read "project.version=" "$base/nuts/target/classes/META-INF/nuts/net.vpc.app.nuts/nuts/nuts.properties"`
runtimeVersion=`properties read "project.version=" "$base/nuts-core/target/classes/META-INF/nuts/net.vpc.app.nuts/nuts-core/nuts.properties"`
echo "detected nuts      version $bootVersion"
echo "detected nuts-core version $runtimeVersion"
vpc_public_nuts="$HOME/vpc-public-nuts"

bootPath="$vpc_public_nuts/net/vpc/app/nuts/nuts/$bootVersion"
bootLatestPath="$vpc_public_nuts/net/vpc/app/nuts/nuts/LATEST"

cp --mkdir "$base/nuts/target/classes/META-INF/nuts/net.vpc.app.nuts/nuts/nuts.properties" "$bootPath/nuts.properties"


echo $bootVersion > "$vpc_public_nuts/version.txt"


properties set "bootRuntimeId" "net.vpc.app.nuts:nuts-core#$runtimeVersion" "$bootPath/nuts.properties"
properties set "repositories" "http://repo.maven.apache.org/maven2/;https://raw.githubusercontent.com/thevpc/vpc-public-maven/master" "$bootPath/nuts.properties"

cp --mkdir "$bootPath/nuts.properties $bootLatestPath/nuts.properties"
cp --mkdir "$base/nuts-core/target/classes/META-INF/nuts/net.vpc.app.nuts/nuts-core/nuts.properties" "$vpc_public_nuts/net/vpc/app/nuts/nuts-core/LATEST/nuts.properties"
cp --mkdir "$base/nuts-core/target/classes/META-INF/nuts/net.vpc.app.nuts/nuts-core/nuts.properties" "$vpc_public_nuts/net/vpc/app/nuts/nuts-core/$runtimeVersion/nuts.properties"
nuts-admin reindex "$vpc_public_nuts"

##################################################################"

cp "$base/nuts/target/nuts-$bootVersion.jar" "$base/nuts-bootstrap/nuts.jar"

h=`pwd`
cd "$base/nuts-bootstrap/"
zip -cf "nuts-bundle.zip" "nuts" "nuts.jar" "version.txt"
cd $h

cp "$base/nuts-bootstrap/nuts.jar $HOME/bin/nuts.jar"
cp "$base/nuts-bootstrap/nuts $HOME/bin/nuts"
cp "$base/nuts-bootstrap/nuts-debug $HOME/bin/nuts-debug"
