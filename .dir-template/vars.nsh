echo "running vars..."
buildTime=$(date --iso-8601=s --utc)
here=$(dirname $0)
# remember '-ybB' is equivalent to '--yes -embedded -bot'

latestApiVersion=`nuts -ybB nversion $here/../core/nuts`;
latestImplVersion=`nuts -ybB nversion $here/../core/nuts-runtime`;
latestJarLocation="http://thevpc.net/maven/net/thevpc/nuts/nuts/${latestApiVersion}/nuts-${latestApiVersion}.jar";

stableApiVersion=0.8.0;
stableImplVersion=0.8.0.0;
stableJarLocation="https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/${stableApiVersion}/nuts-${stableApiVersion}.jar";

jarLocation="${latestJarLocation}";
apiVersion="${latestApiVersion}";
implVersion="${latestImplVersion}";
