#!/usr/bin/env bash
NUTS_VERSION=1.0.0
## using -N option (newer) to force existing nuts installations to redownload packages
curl -sL https://maven.thevpc.net/net/thevpc/nuts/nuts-app/${NUTS_VERSION}/nuts-app-${NUTS_VERSION}.jar -o nuts.jar && java -jar nuts.jar  -Ny "$@"
