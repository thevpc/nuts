#!/usr/bin/env bash
NUTS_VERSION=0.8.7
## using -N option (newer) to force existing nuts installations to redownload packages
curl -sL https://maven.thevpc.net/net/thevpc/nuts/nuts-app/${NUTS_VERSION}/nuts-app-${NUTS_VERSION}.jar -o nuts.jar && java -jar nuts.jar -N "$@"
