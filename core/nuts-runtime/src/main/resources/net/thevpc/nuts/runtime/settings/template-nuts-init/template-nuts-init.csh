if ( -f "$$SCRIPT_NUTS_ENV$$" ) source "$$SCRIPT_NUTS_ENV$$"
if ( $?PATH ) then
    setenv PATH "${NUTS_WORKSPACE_BINDIR}:${PATH}"
else
    setenv PATH "${NUTS_WORKSPACE_BINDIR}"
endif

