echo "running vars..."
thisDir='/data/public/git/nuts/dir-template/'
apiVersion=`nuts -y --bot -b nversion $thisDir/../core/nuts`;
implVersion=`nuts nversion $thisDir/../core/nuts-runtime`;
jarLocation="https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/${apiVersion}/nuts-${apiVersion}.jar";

echo apiVersion=$apiVersion
echo implVersion=$implVersion
echo jarLocation=$jarLocation
