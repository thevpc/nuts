#!/bin/bash
set -e

# --- Environment variables with defaults ---
NUTS_VERSION=${NUTS_VERSION:-0.8.7}
NUTS_ARGS=("$@")

if [ "${NUTS_CONTAINER_VERBOSE:-0}" = "1" ]; then
    echo "Bootstrapping container..."
    echo "Nuts version: $NUTS_VERSION"
    echo "Additional Nuts args: ${NUTS_ARGS[*]}"
fi


# --- Create non-root user if running as root ---
TARGET_USER=$(whoami)
TARGET_HOME="$HOME"
[ "${NUTS_CONTAINER_VERBOSE:-0}" = "1" ] && echo "Running as user: $TARGET_USER"


# --- Detect Java dynamically ---
JAVA_BIN=$(dirname "$(readlink -f "$(command -v java || true)")")
if [ -z "$JAVA_BIN" ]; then
    echo "⚠️  Java not found in PATH. Nuts installation may fail."
else
    [ "${NUTS_CONTAINER_VERBOSE:-0}" = "1" ] && echo "Detected Java path: $JAVA_BIN"
    # Update bashrc for target user if running as root
    if ! grep -q "$JAVA_BIN" "$TARGET_HOME/.bashrc" 2>/dev/null; then
        echo "export PATH=\"$JAVA_BIN:\$PATH\"" >> "$TARGET_HOME/.bashrc"
    fi
fi
mkdir -p "$TARGET_HOME/bin"

# --- Function to run Nuts installation ---
run_nuts() {
    set -e
    local PATH_TO_USE="$1"
    shift
    export PATH="$PATH_TO_USE"
    [ "${NUTS_CONTAINER_VERBOSE:-0}" = "1" ] && echo "Current PATH inside target user: $PATH"

    # Source bashrc if exists
    [ -f "$TARGET_HOME/.bashrc" ] && source "$TARGET_HOME/.bashrc"

    local NUTS_VERSION="$1"
    shift
    local NUTS_ARGS=("$@")

    [ "${NUTS_CONTAINER_VERBOSE:-0}" = "1" ] && echo "Installing Nuts version $NUTS_VERSION..."
    curl -sL "https://maven.thevpc.net/net/thevpc/nuts/nuts-app/$NUTS_VERSION/nuts-app-$NUTS_VERSION.jar" -o "$TARGET_HOME/bin/nuts.jar"

    local JAVA_ARGS=(-Ny "${NUTS_ARGS[@]}")
    [ "${NUTS_CONTAINER_VERBOSE:-0}" = "1" ] && echo "Launching Nuts with arguments: ${JAVA_ARGS[*]}"
    java -jar "$TARGET_HOME/bin/nuts.jar" "${JAVA_ARGS[@]}"

    # Reload bashrc after Nuts updates it
    [ -f "$TARGET_HOME/.bashrc" ] && source "$TARGET_HOME/.bashrc"

    # Drop into interactive shell
    exec bash
}



run_nuts "$PATH" "$NUTS_VERSION" "${NUTS_ARGS[@]}"
