echo "running vars..."
buildTime=$(date --iso-8601=s --utc)
vars_here=$(dirname $0)
# remember '-ybB' is equivalent to '--yes -embedded -bot'

latestApiVersion=`nuts -ybB nversion $vars_here/../core/nuts`;
latestRuntimeVersion=`nuts -ybB nversion $vars_here/../core/nuts-runtime`;
latestJarLocation="https://thevpc.net/maven/net/thevpc/nuts/nuts/${latestApiVersion}/nuts-${latestApiVersion}.jar";

stableApiVersion=0.8.3;
stableRuntimeVersion=0.8.3.0;
stableJarLocation="https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/${stableApiVersion}/nuts-${stableApiVersion}.jar";

jarLocation="${latestJarLocation}";
apiVersion="${latestApiVersion}";
runtimeVersion="${latestRuntimeVersion}";

echo --highlighter detected "latestApiVersion  ##$latestApiVersion##"
echo --highlighter detected "latestRuntimeVersion ##$latestRuntimeVersion##"
echo --highlighter detected "stableApiVersion  ##$stableApiVersion##"
echo --highlighter detected "stableRuntimeVersion ##$stableRuntimeVersion##"
