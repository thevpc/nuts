$ErrorActionPreference = "Stop"
$NUTS_VERSION = "{{stableApiVersion}}"
$NUTS_RUNTIME_VERSION = "$NUTS_VERSION.0"
$JAR_URL = "https://maven.thevpc.net/net/thevpc/nuts/nuts-app/$NUTS_VERSION/nuts-app-$NUTS_VERSION.jar"
$OUT_FILE = "nuts.jar"

function Get-NutsWorkspaceRoots {
    $roots = @()
    if (Test-Path "$env:APPDATA\nuts\bin\ws") {
        $roots = @(Get-ChildItem "$env:APPDATA\nuts\bin\ws" -Directory -ErrorAction SilentlyContinue |
            ForEach-Object { Join-Path $_.FullName "id\net\thevpc\nuts\nuts" } |
            Where-Object { Test-Path $_ })
    }
    return $roots
}

function Remove-NutsRuntimeCache {
    param([string]$ApiVersion, [string]$RuntimeVersion)

    $runtimePattern = '^nuts-runtime-' + [regex]::Escape($ApiVersion) + '\..*\.jar$'
    $apiPattern = '^nuts-' + [regex]::Escape($ApiVersion) + '\.jar$'
    $appPattern = '^nuts-app-' + [regex]::Escape($ApiVersion) + '\.jar$'

    # 1. cached jars used by the boot
    if (Test-Path "$env:APPDATA\nuts") {
        Get-ChildItem "$env:APPDATA\nuts" -Recurse -File -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -match $runtimePattern -or $_.Name -match $apiPattern -or $_.Name -match $appPattern } |
            ForEach-Object { Remove-Item -Force $_.FullName -ErrorAction SilentlyContinue }
    }

    # 2. previously seeded/stale local Maven artifacts. The generated
    #    launchers do not pass --local-maven=false, so an old jar here can be
    #    mixed with the new boot/API and breaks class loading.
    $m2Nuts = Join-Path $env:USERPROFILE '.m2\repository\net\thevpc\nuts'
    if (Test-Path $m2Nuts) {
        foreach ($p in @(
            (Join-Path $m2Nuts "nuts-runtime\$RuntimeVersion"),
            (Join-Path $m2Nuts "nuts\$ApiVersion"),
            (Join-Path $m2Nuts "nuts-boot\$ApiVersion"),
            (Join-Path $m2Nuts "nuts-app\$ApiVersion")
        )) {
            if (Test-Path $p) {
                Remove-Item -Recurse -Force $p -ErrorAction SilentlyContinue
            }
        }
        Get-ChildItem $m2Nuts -Recurse -File -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -match $runtimePattern -or $_.Name -match $apiPattern -or $_.Name -match $appPattern } |
            ForEach-Object { Remove-Item -Force $_.FullName -ErrorAction SilentlyContinue }
    }

    # 3. cached boot configs may describe an incomplete runtime classpath
    #    (runtime jar without the nuts API jar). Force a clean re-resolution.
    $confWs = Join-Path $env:APPDATA 'nuts\conf\ws'
    if (Test-Path $confWs) {
        Get-ChildItem $confWs -Directory -ErrorAction SilentlyContinue | ForEach-Object {
            $idRoot = Join-Path $_.FullName 'id\net\thevpc\nuts'
            foreach ($p in @(
                (Join-Path $idRoot "nuts-runtime\$RuntimeVersion"),
                (Join-Path $idRoot "nuts\$ApiVersion")
            )) {
                if (Test-Path $p) {
                    Remove-Item -Recurse -Force $p -ErrorAction SilentlyContinue
                }
            }
        }
    }
}

function Ensure-NutsTerminalShortcut {
    param([string]$TermInit)

    if (-not (Test-Path $TermInit)) { return $false }
    $menu = "$env:APPDATA\Microsoft\Windows\Start Menu\Programs"
    New-Item -ItemType Directory -Force -Path $menu -ErrorAction SilentlyContinue | Out-Null
    $ps = Join-Path $env:SystemRoot 'System32\WindowsPowerShell\v1.0\powershell.exe'
    if (-not (Test-Path $ps)) { return $false }
    $targets = @(Join-Path $menu 'Nuts Terminal Powershell.lnk')
    $desktop = [Environment]::GetFolderPath('Desktop')
    if ($desktop) {
        $targets += (Join-Path $desktop 'Nuts Terminal Powershell.lnk')
    }
    $wsh = New-Object -ComObject WScript.Shell
    $created = $false
    foreach ($lnk in $targets) {
        try {
            $sc = $wsh.CreateShortcut($lnk)
            $sc.TargetPath = $ps
            $sc.Arguments = "-NoExit -ExecutionPolicy Bypass -Command `". '$TermInit'`""
            $sc.WorkingDirectory = $env:USERPROFILE
            $sc.IconLocation = "$ps,0"
            $sc.Save()
            $created = $true
        } catch {
            Write-Warning "unable to create shortcut $lnk : $($_.Exception.Message)"
        }
    }
    return $created
}

function Add-NutsBinToUserPath {
    param([string]$Bin)

    if (-not (Test-Path $Bin)) { return }
    $userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
    $parts = @()
    if ($userPath) {
        $parts = @($userPath -split ';' | Where-Object {
            $_ -and ($_ -notmatch '\\id\\net\.thevpc\.nuts\\nuts\\[^\\]+\\bin$') -and ($_ -notmatch '\\nuts\\bin$')
        })
    }
    $parts += $Bin
    [Environment]::SetEnvironmentVariable('Path', ($parts -join ';'), 'User')
}

# Stable launcher used when NDI did not create a per-version launcher at all.
function Ensure-NutsFallbackLauncher {
    param([string]$AppJar)

    if (-not $AppJar -or -not (Test-Path $AppJar)) { return $false }

    $bin = "$env:APPDATA\nuts\bin"
    $inc = "$env:APPDATA\nuts\inc"
    New-Item -ItemType Directory -Force -Path $bin, $inc -ErrorAction SilentlyContinue | Out-Null

    Copy-Item -Force $AppJar (Join-Path $bin 'nuts-app.jar')

    $cmd = Join-Path $bin 'nuts.cmd'
    @'
@echo off
setlocal
set "_java=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "_java=%JAVA_HOME%\bin\java.exe"
"%_java%" -jar "%~dp0nuts-app.jar" --boot-repositories=https://maven.thevpc.net --boot-repositories=https://repo.maven.apache.org/maven2 %*
'@ | Set-Content -Path $cmd -Encoding ASCII

    $ps1 = Join-Path $bin 'nuts.ps1'
    '& "$PSScriptRoot\nuts.cmd" @args' | Set-Content -Path $ps1 -Encoding UTF8

    $init = Join-Path $inc 'nuts-init.ps1'
    '$env:Path="$env:APPDATA\nuts\bin;$env:Path"' | Set-Content -Path $init -Encoding UTF8

    $termInit = Join-Path $inc 'nuts-term-init.ps1'
    @'
if (Test-Path $PROFILE) { . $PROFILE }
. "$env:APPDATA\nuts\inc\nuts-init.ps1"
& "$env:APPDATA\nuts\bin\nuts.cmd" welcome
'@ | Set-Content -Path $termInit -Encoding UTF8

    Add-NutsBinToUserPath -Bin $bin
    Ensure-NutsTerminalShortcut -TermInit $termInit | Out-Null
    return $true
}

function Repair-NutsInstallation {
    param([string]$ApiVersion, [string]$AppJar)

    $workspaceRoots = Get-NutsWorkspaceRoots
    if ($workspaceRoots.Count -eq 0) {
        Ensure-NutsFallbackLauncher -AppJar $AppJar | Out-Null
        return
    }

    foreach ($nutsRoot in $workspaceRoots) {
        # Resolve the installed version folder (expected version, or newest installed).
        $versionDir = Join-Path $nutsRoot $ApiVersion
        if (-not (Test-Path $versionDir)) {
            $versionDir = @(Get-ChildItem $nutsRoot -Directory -ErrorAction SilentlyContinue |
                Where-Object { $_.Name -match '^\d+(\.\d+)*$' } |
                Sort-Object { [version]$_.Name } -Descending |
                Select-Object -First 1 -ExpandProperty FullName)
        }
        if (-not $versionDir) {
            Ensure-NutsFallbackLauncher -AppJar $AppJar | Out-Null
            continue
        }
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

        # Remove old shortcuts from every location Windows may use, including
        # pinned taskbar/start tiles. Match both the shortcut name and its
        # target/arguments (stable names may point to an old version folder).
        if ($oldVersions.Count -gt 0) {
            $places = @(
                "$env:APPDATA\Microsoft\Windows\Start Menu\Programs",
                "C:\ProgramData\Microsoft\Windows\Start Menu\Programs",
                [Environment]::GetFolderPath('Desktop'),
                "$env:APPDATA\Microsoft\Internet Explorer\Quick Launch\User Pinned\TaskBar",
                "$env:APPDATA\Microsoft\Internet Explorer\Quick Launch\User Pinned\StartMenu"
            ) | Where-Object { $_ -and (Test-Path $_) }
            $wsh = New-Object -ComObject WScript.Shell
            foreach ($place in $places) {
                Get-ChildItem $place -Recurse -Filter '*.lnk' -ErrorAction SilentlyContinue | ForEach-Object {
                    $lnk = $_
                    $remove = $false
                    if ($lnk.BaseName -like 'Nuts*') {
                        foreach ($v in $oldVersions) {
                            if ($lnk.BaseName -like "*$v*") { $remove = $true; break }
                        }
                    }
                    if (-not $remove) {
                        try {
                            $sc = $wsh.CreateShortcut($lnk.FullName)
                            $cmd = "$($sc.TargetPath) $($sc.Arguments)"
                            foreach ($v in $oldVersions) {
                                if ($cmd -match [regex]::Escape("\$v\")) { $remove = $true; break }
                            }
                        } catch { }
                    }
                    if ($remove) {
                        Remove-Item -Force $lnk.FullName -ErrorAction SilentlyContinue
                    }
                }
            }
        }

        $bin = Join-Path $versionDir 'bin'
        $inc = Join-Path $versionDir 'inc'
        $init = Join-Path $inc '.nuts-term-init.ps1'

        # Compatibility shim: older builds reference bin/nuts.ps1 which is never generated.
        $cmd = Join-Path $bin 'nuts.cmd'
        $ps1 = Join-Path $bin 'nuts.ps1'
        if ((Test-Path $cmd) -and -not (Test-Path $ps1)) {
            Set-Content -Path $ps1 -Encoding UTF8 -Value ('& "' + $cmd + '" @args')
        }

        # Compatibility fix: older nuts-term.ps1 launched a nested PowerShell,
        # which breaks PSReadLine as soon as the user types.
        $term = Join-Path $bin 'nuts-term.ps1'
        if ((Test-Path $term) -and (Test-Path $init)) {
            $text = Get-Content $term -Raw
            if ($text -match 'NUTS_SYS_TERM_START\s*=\s*"powershell\.exe') {
                Set-Content -Path $term -Encoding UTF8 -Value ('. "' + $init + '"')
            }
        }

        # Repair the generated PowerShell init: older builds replaced PATH instead of prepending.
        $initFile = Join-Path $inc '.nuts-init.ps1'
        if (Test-Path $initFile) {
            $text = Get-Content $initFile -Raw
            $fixed = $text.Replace('$env:Path=$NUTS_WORKSPACE_BINDIR;$env:Path',
                                   '$env:Path="$env:NUTS_WORKSPACE_BINDIR;$env:Path"')
            if ($fixed -ne $text) {
                Set-Content -Path $initFile -Encoding UTF8 -Value $fixed
            }
        }

        # Make sure a working terminal shortcut exists even if NDI could not
        # create/update one (for example when mslinks is unavailable).
        if (Test-Path $init) {
            if (Test-Path $bin) {
                Add-NutsBinToUserPath -Bin $bin
            }
            if (-not (Ensure-NutsTerminalShortcut -TermInit $init)) {
                Ensure-NutsFallbackLauncher -AppJar $AppJar | Out-Null
            }
        } else {
            # NDI never generated the scripts; use the stable fallback launcher.
            Ensure-NutsFallbackLauncher -AppJar $AppJar | Out-Null
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
$APP_JAR = (Resolve-Path -LiteralPath $OUT_FILE -ErrorAction SilentlyContinue).Path
if (-not $APP_JAR) { $APP_JAR = Join-Path (Get-Location) $OUT_FILE }

# 2. Force a fresh runtime/API download. -N alone is not enough when the cached
# jar has a timestamp that makes the boot consider it fresh.
Remove-NutsRuntimeCache -ApiVersion $NUTS_VERSION -RuntimeVersion $NUTS_RUNTIME_VERSION

# 3. Execute Java, passing through all arguments dynamically.
# Network filters/proxies on Windows may truncate large downloads. Use a longer
# timeout and retry the bootstrap so a transient truncation does not fail the install.
$BOOT_ARGS = @("--switch", "--boot-repositories=https://maven.thevpc.net", "--boot-repositories=https://repo.maven.apache.org/maven2", "---connection-timeout=30000", "---connection-read-timeout=30000", "-Ny")
$BOOT_EXIT = 0
for ($i = 1; $i -le 3; $i++) {
    if ($null -ne $args) {
        & java -jar $OUT_FILE @BOOT_ARGS @args
    } else {
        & java -jar $OUT_FILE @BOOT_ARGS
    }
    $BOOT_EXIT = $LASTEXITCODE
    if ($BOOT_EXIT -eq 0) { break }
    if ($i -eq 3) { break }
    Start-Sleep -Seconds 2
}

# 4. Always clean up/repair launchers, even if the bootstrap command returned
#    a non-zero exit code after downloading its artifacts.
Repair-NutsInstallation -ApiVersion $NUTS_VERSION -AppJar $APP_JAR

if ($BOOT_EXIT -ne 0) {
    throw "unable to bootstrap nuts (exit code $BOOT_EXIT)"
}
