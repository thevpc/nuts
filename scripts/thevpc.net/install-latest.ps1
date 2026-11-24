$ErrorActionPreference = "Stop"
$NUTS_VERSION = "1.2.0"
$JAR_URL = "https://maven.thevpc.net/net/thevpc/nuts/nuts-app/$NUTS_VERSION/nuts-app-$NUTS_VERSION.jar"
$OUT_FILE = "nuts.jar"

function Repair-NutsInstallation {
    param([string]$ApiVersion)

    $workspaceRoots = @()
    if (Test-Path "$env:APPDATA\nuts\bin\ws") {
        $workspaceRoots = @(Get-ChildItem "$env:APPDATA\nuts\bin\ws" -Directory -ErrorAction SilentlyContinue |
            ForEach-Object { Join-Path $_.FullName "id\net\thevpc\nuts\nuts" } |
            Where-Object { Test-Path $_ })
    }
    if ($workspaceRoots.Count -eq 0) { return }

    foreach ($nutsRoot in $workspaceRoots) {
        # Resolve the installed version folder (expected version, or newest installed).
        $versionDir = Join-Path $nutsRoot $ApiVersion
        if (-not (Test-Path $versionDir)) {
            $versionDir = @(Get-ChildItem $nutsRoot -Directory -ErrorAction SilentlyContinue |
                Where-Object { $_.Name -match '^\d+(\.\d+)*$' } |
                Sort-Object { [version]$_.Name } -Descending |
                Select-Object -First 1 -ExpandProperty FullName)
        }
        if (-not $versionDir) { continue }
        $installedVersion = Split-Path $versionDir -Leaf
        [version]$targetVersion = [version]'0.0'
        [void][version]::TryParse($installedVersion, [ref]$targetVersion)

        # Remove older launcher folders and collect their versions.
        $oldVersions = @()
        foreach ($d in @(Get-ChildItem $nutsRoot -Directory -ErrorAction SilentlyContinue)) {
            if ($d.Name -eq $installedVersion) { continue }
            [version]$v = [version]'0.0'
            if ([version]::TryParse($d.Name, [ref]$v) -and $v -lt $targetVersion) {
                $oldVersions += $d.Name
                Remove-Item -Recurse -Force $d.FullName -ErrorAction SilentlyContinue
            }
        }

        # Remove shortcuts that belong to an older version.
        if ($oldVersions.Count -gt 0) {
            $places = @(
                "$env:APPDATA\Microsoft\Windows\Start Menu\Programs",
                "C:\ProgramData\Microsoft\Windows\Start Menu\Programs",
                [Environment]::GetFolderPath('Desktop')
            ) | Where-Object { $_ -and (Test-Path $_) }
            foreach ($place in $places) {
                Get-ChildItem $place -Recurse -Filter '*.lnk' -ErrorAction SilentlyContinue | ForEach-Object {
                    if ($_.BaseName -like 'Nuts*') {
                        foreach ($v in $oldVersions) {
                            if ($_.BaseName -like "*$v*") {
                                Remove-Item -Force $_.FullName -ErrorAction SilentlyContinue
                                break
                            }
                        }
                    }
                }
            }
        }

        $bin = Join-Path $versionDir 'bin'
        # Compatibility shim: older builds reference bin/nuts.ps1 which is never generated.
        $cmd = Join-Path $bin 'nuts.cmd'
        $ps1 = Join-Path $bin 'nuts.ps1'
        if ((Test-Path $cmd) -and -not (Test-Path $ps1)) {
            Set-Content -Path $ps1 -Encoding UTF8 -Value ('& "' + $cmd + '" @args')
        }

        # Repair the generated PowerShell init: older builds replaced PATH instead of prepending.
        $init = Join-Path $versionDir 'inc\.nuts-init.ps1'
        if (Test-Path $init) {
            $text = Get-Content $init -Raw
            $fixed = $text.Replace('$env:Path=$NUTS_WORKSPACE_BINDIR;$env:Path',
                                   '$env:Path="$env:NUTS_WORKSPACE_BINDIR;$env:Path"')
            if ($fixed -ne $text) {
                Set-Content -Path $init -Encoding UTF8 -Value $fixed
            }
        }

        # Make the current launcher available in new shells and drop stale versioned entries.
        if (Test-Path $bin) {
            $userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
            $parts = @()
            if ($userPath) {
                $parts = @($userPath -split ';' | Where-Object {
                    $_ -and ($_ -notmatch '\\id\\net\.thevpc\.nuts\\nuts\\[^\\]+\\bin$')
                })
            }
            $parts += $bin
            [Environment]::SetEnvironmentVariable('Path', ($parts -join ';'), 'User')
        }
    }
}

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw "java was not found in PATH. Please install Java 17 or later and try again."
}

# Windows PowerShell 5.1 may default to an older TLS version.
try {
    [Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12
} catch { }

# 1. Download the jar cleanly using WebClient (fastest single-line download in PS)
for ($i = 1; $i -le 3; $i++) {
    try {
        (New-Object System.Net.WebClient).DownloadFile($JAR_URL, $OUT_FILE)
        break
    } catch {
        if ($i -eq 3) { throw }
        Start-Sleep -Seconds 2
    }
}

# 2. Execute Java, passing through all arguments dynamically.
# Network filters/proxies on Windows may truncate large downloads. Use a longer
# timeout and retry the bootstrap so a transient truncation does not fail the install.
$BOOT_ARGS = @("--local-maven=false", "--switch", "---connection-timeout=30000", "---connection-read-timeout=30000", "-Ny")
for ($i = 1; $i -le 3; $i++) {
    if ($null -ne $args) {
        & java -jar $OUT_FILE @BOOT_ARGS @args
    } else {
        & java -jar $OUT_FILE @BOOT_ARGS
    }
    if ($LASTEXITCODE -eq 0) { break }
    if ($i -eq 3) { throw "unable to bootstrap nuts (exit code $LASTEXITCODE)" }
    Start-Sleep -Seconds 2
}

# 3. Clean up launchers/shortcuts left behind by older installations and make
#    sure the freshly installed launcher is on PATH for new shells.
Repair-NutsInstallation -ApiVersion $NUTS_VERSION
