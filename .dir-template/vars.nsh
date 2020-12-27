echo "running vars..."
buildTime=$(date --iso-8601=s --utc)
here=$(dirname $0)
echo vars file is $0
echo here vars is $here as $(dirname $0)
echo apiVersion=`nuts -y --bot -b nversion $here/../core/nuts`;
apiVersion=`nuts -y --bot -b nversion $here/../core/nuts`;
implVersion=`nuts nversion $here/../core/nuts-runtime`;
jarLocation="https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/${apiVersion}/nuts-${apiVersion}.jar";
