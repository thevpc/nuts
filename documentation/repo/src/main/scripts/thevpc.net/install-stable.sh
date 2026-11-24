#!/usr/bin/env bash
NUTS_VERSION={{stableApiVersion}}
## using -N option (newer) to force existing nuts installations to redownload packages
curl -fsSL --retry 3 --retry-delay 2 https://maven.thevpc.net/net/thevpc/nuts/nuts-app/${NUTS_VERSION}/nuts-app-${NUTS_VERSION}.jar -o nuts.jar || exit 1
## force a fresh runtime/API download even if the cached jar looks fresh
if [ -d "$HOME/.local/share/nuts" ]; then
  find "$HOME/.local/share/nuts" -type f \
    \( -name "nuts-runtime-${NUTS_VERSION}.*.jar" -o -name "nuts-${NUTS_VERSION}.jar" \) \
    -delete 2>/dev/null || true
fi
for attempt in 1 2 3; do
  java -jar nuts.jar --switch ---connection-timeout=30000 ---connection-read-timeout=30000 -Ny "$@"
  code=$?
  if [ "$code" -eq 0 ]; then exit 0; fi
  if [ "$attempt" -eq 3 ]; then exit "$code"; fi
  echo "nuts bootstrap failed, retrying ($attempt/3)..." >&2
  sleep 2
done
