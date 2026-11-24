#!/usr/bin/env bash
NUTS_VERSION={{stableApiVersion}}
## using -N option (newer) to force existing nuts installations to redownload packages
curl -fsSL --retry 3 --retry-delay 2 https://maven.thevpc.net/net/thevpc/nuts/nuts-app/${NUTS_VERSION}/nuts-app-${NUTS_VERSION}.jar -o nuts.jar || exit 1
for attempt in 1 2 3; do
  java -jar nuts.jar --switch ---connection-timeout=30000 ---connection-read-timeout=30000 -Ny "$@"
  code=$?
  if [ "$code" -eq 0 ]; then exit 0; fi
  if [ "$attempt" -eq 3 ]; then exit "$code"; fi
  echo "nuts bootstrap failed, retrying ($attempt/3)..." >&2
  sleep 2
done
