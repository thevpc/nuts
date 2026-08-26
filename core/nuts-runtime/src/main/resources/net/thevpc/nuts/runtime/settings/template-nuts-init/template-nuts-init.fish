. "$$SCRIPT_NUTS_ENV$$"
set PATH "$NUTS_WORKSPACE_BINDIR:$PATH"
export PATH

# completion: interactive fish shells only
if status is-interactive; and test -f "$$SCRIPT_NUTS_COMPLETION$$"
    source "$$SCRIPT_NUTS_COMPLETION$$"
end
