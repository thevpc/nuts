$ErrorActionPreference = "Stop"

function Find-NutsAppJar {
    foreach ($p in @(
        (Join-Path (Get-Location) 'nuts.jar'),
        (Join-Path $env:USERPROFILE 'nuts.jar')
    )) {
        if (Test-Path $p) {
            return (Resolve-Path -LiteralPath $p).Path
        }
    }
    $f = Get-ChildItem "$env:APPDATA\nuts" -Recurse -Filter 'nuts-app-*.jar' -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($f) { return $f.FullName }
    return $null
}

$appJar = Find-NutsAppJar
if (-not $appJar) {
    throw "nuts app jar not found (looked in the current folder, $env:USERPROFILE, and $env:APPDATA\nuts). Re-run the installer first."
}

# Remove previously seeded/stale local Maven nuts artifacts. The launcher does
# not pass --local-maven=false, so an old runtime jar here can be mixed with the
# new boot/API and fail with ServiceConfigurationError.
$apiVersion = "1.2.0"
if ($appJar -match 'nuts-app-([0-9][0-9.]*)\.jar') {
    $apiVersion = $Matches[1]
}
$m2Nuts = Join-Path $env:USERPROFILE '.m2\repository\net\thevpc\nuts'
if (Test-Path $m2Nuts) {
    foreach ($p in @(
        (Join-Path $m2Nuts "nuts-runtime\$apiVersion.0"),
        (Join-Path $m2Nuts "nuts\$apiVersion"),
        (Join-Path $m2Nuts "nuts-boot\$apiVersion"),
        (Join-Path $m2Nuts "nuts-app\$apiVersion")
    )) {
        if (Test-Path $p) {
            Remove-Item -Recurse -Force $p -ErrorAction SilentlyContinue
        }
    }
    Get-ChildItem $m2Nuts -Recurse -File -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match "^nuts-runtime-$([regex]::Escape($apiVersion))\." -or $_.Name -eq "nuts-$apiVersion.jar" } |
        ForEach-Object { Remove-Item -Force $_.FullName -ErrorAction SilentlyContinue }
}

# Drop cached boot configs: they can describe a runtime classpath without the
# nuts API jar, which breaks with ClassNotFoundException NWorkspace.
$confWs = Join-Path $env:APPDATA 'nuts\conf\ws'
if (Test-Path $confWs) {
    Get-ChildItem $confWs -Directory -ErrorAction SilentlyContinue | ForEach-Object {
        $idRoot = Join-Path $_.FullName 'id\net\thevpc\nuts'
        foreach ($p in @(
            (Join-Path $idRoot "nuts-runtime\$apiVersion.0"),
            (Join-Path $idRoot "nuts\$apiVersion")
        )) {
            if (Test-Path $p) {
                Remove-Item -Recurse -Force $p -ErrorAction SilentlyContinue
            }
        }
    }
}

$bin = "$env:APPDATA\nuts\bin"
$inc = "$env:APPDATA\nuts\inc"
New-Item -ItemType Directory -Force -Path $bin, $inc | Out-Null

Copy-Item -Force $appJar (Join-Path $bin 'nuts-app.jar')

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

$userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
$parts = @()
if ($userPath) {
    $parts = @($userPath -split ';' | Where-Object {
        $_ -and ($_ -notmatch '\\id\\net\.thevpc\.nuts\\nuts\\[^\\]+\\bin$') -and ($_ -notmatch '\\nuts\\bin$')
    })
}
$parts += $bin
[Environment]::SetEnvironmentVariable('Path', ($parts -join ';'), 'User')

$menu = "$env:APPDATA\Microsoft\Windows\Start Menu\Programs"
New-Item -ItemType Directory -Force -Path $menu | Out-Null
$powershell = Join-Path $env:SystemRoot 'System32\WindowsPowerShell\v1.0\powershell.exe'
$targets = @(Join-Path $menu 'Nuts Terminal Powershell.lnk')
$desktop = [Environment]::GetFolderPath('Desktop')
if ($desktop) {
    $targets += (Join-Path $desktop 'Nuts Terminal Powershell.lnk')
}
$wsh = New-Object -ComObject WScript.Shell
foreach ($lnk in $targets) {
    try {
        $sc = $wsh.CreateShortcut($lnk)
        $sc.TargetPath = $powershell
        $sc.Arguments = "-NoExit -ExecutionPolicy Bypass -Command `". '$termInit'`""
        $sc.WorkingDirectory = $env:USERPROFILE
        $sc.IconLocation = "$powershell,0"
        $sc.Save()
        Write-Host "shortcut         : $lnk"
    } catch {
        Write-Warning "unable to create shortcut $lnk : $($_.Exception.Message)"
    }
}

Write-Host "app jar          : $appJar"
Write-Host "launcher         : $cmd"
Write-Host "terminal init    : $termInit"
Write-Host "PATH entry added : $bin"
Write-Host ""
Write-Host "Open a NEW PowerShell and run: nuts --version"
