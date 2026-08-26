. "$$SCRIPT_NUTS_ENV$$"
PATH="${NUTS_WORKSPACE_BINDIR}:${PATH}"
export PATH

# completion: interactive zsh shells only
if [[ -o interactive ]] && [ -f "$$SCRIPT_NUTS_COMPLETION$$" ]; then
    source "$$SCRIPT_NUTS_COMPLETION$$"
fi