#!/usr/bin/env bash
NUTS_VERSION={{apiVersion}}
## using -N option (newer) to force existing nuts installations to redownload packages
curl -fsSL --retry 3 --retry-delay 2 https://maven.thevpc.net/net/thevpc/nuts/nuts-app/${NUTS_VERSION}/nuts-app-${NUTS_VERSION}.jar -o nuts.jar || exit 1
## force a fresh runtime/API download even if the cached jar looks fresh
if [ -d "$HOME/.local/share/nuts" ]; then
  find "$HOME/.local/share/nuts" -type f \
    \( -name "nuts-runtime-${NUTS_VERSION}.*.jar" -o -name "nuts-${NUTS_VERSION}.jar" \) \
    -delete 2>/dev/null || true
fi
for attempt in 1 2 3; do
  java -jar nuts.jar --local-maven=false --switch ---connection-timeout=30000 ---connection-read-timeout=30000 -Ny "$@"
  code=$?
  if [ "$code" -eq 0 ]; then break; fi
  if [ "$attempt" -eq 3 ]; then exit "$code"; fi
  echo "nuts bootstrap failed, retrying ($attempt/3)..." >&2
  sleep 2
done

## remove launchers and shortcuts left by older installations
NUTS_BIN_ROOT="$HOME/.local/share/nuts/bin/ws"
for nutsRoot in "$NUTS_BIN_ROOT"/*/id/net/thevpc/nuts/nuts; do
  [ -d "$nutsRoot" ] || continue
  for d in "$nutsRoot"/*; do
    [ -d "$d" ] || continue
    v=$(basename "$d")
    [ "$v" = "$NUTS_VERSION" ] && continue
    case "$v" in
      [0-9]*)
        for f in "$HOME/.local/share/applications"/*"$v"* "$HOME/.config/menus/applications-merged"/*"$v"*; do
          [ -f "$f" ] || continue
          case "$(basename "$f")" in
            *[Nn]uts*) rm -f "$f" ;;
          esac
        done
        rm -rf "$d"
        ;;
    esac
  done
done
exit 0
