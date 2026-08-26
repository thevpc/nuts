$NUTS_VERSION = "1.0.0"
$JAR_URL = "https://maven.thevpc.net/net/thevpc/nuts/nuts-app/$NUTS_VERSION/nuts-app-$NUTS_VERSION.jar"
$OUT_FILE = "nuts.jar"

# 1. Download the jar cleanly using WebClient (fastest single-line download in PS)
(New-Object System.Net.WebClient).DownloadFile($JAR_URL, $OUT_FILE)

# 2. Execute Java, passing through all arguments dynamically
if ($null -ne $args) {
    java -jar $OUT_FILE -Ny @args
} else {
    java -jar $OUT_FILE -Ny
}
