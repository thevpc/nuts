. "$$SCRIPT_NUTS_ENV$$"
PATH="${NUTS_WORKSPACE_BINDIR}:${PATH}"
export PATH

# completion: interactive bash shells only
case "$-" in
  *i*) [ -n "$BASH_VERSION" ] && [ -f "$$SCRIPT_NUTS_COMPLETION$$" ] && . "$$SCRIPT_NUTS_COMPLETION$$" ;;
esac