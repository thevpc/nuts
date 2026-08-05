. "$$SCRIPT_NUTS_ENV$$"
$env:Path=$NUTS_WORKSPACE_BINDIR;$env:Path

# completion: interactive PowerShell sessions only
if ([Environment]::UserInteractive -and (Test-Path "$$SCRIPT_NUTS_COMPLETION$$")) {
    . "$$SCRIPT_NUTS_COMPLETION$$"
}
