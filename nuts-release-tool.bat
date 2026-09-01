@echo off
rem Default fallback version for remote downloads (update when changing release versions)
set "NUTS_VERSION=1.0.0"

setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
set "NUTS_JAR="

rem 1. Check local build targets
for /f "delims=" %%I in ('dir /b /s /o-d "%SCRIPT_DIR%core\nuts-app\target\nuts-app-*.jar" 2^>nul') do (
    set "NUTS_JAR=%%I"
    goto :found
)
for /f "delims=" %%I in ('dir /b /s /o-d "%SCRIPT_DIR%core\nuts-app-full\target\nuts-app-full-*.jar" 2^>nul') do (
    set "NUTS_JAR=%%I"
    goto :found
)
if exist "%SCRIPT_DIR%nuts.jar" (
    set "NUTS_JAR=%SCRIPT_DIR%nuts.jar"
    goto :found
)

rem 2. Check local Maven repository (~/.m2)
if exist "%USERPROFILE%\.m2\repository\net\thevpc\nuts\nuts-app" (
    for /f "delims=" %%I in ('dir /b /s /o-d "%USERPROFILE%\.m2\repository\net\thevpc\nuts\nuts-app\*.jar" 2^>nul ^| findstr /v /i "sources javadoc"') do (
        set "NUTS_JAR=%%I"
        goto :found
    )
)

rem 3. Fallback download from maven.thevpc.net
if "%NUTS_JAR%"=="" (
    if exist "%SCRIPT_DIR%core\nuts-app\pom.xml" (
        for /f "tokens=3 delims=^<> " %%V in ('findstr /i "<version>" "%SCRIPT_DIR%core\nuts-app\pom.xml"') do (
            if not defined DETECTED_VER (
                set "NUTS_VERSION=%%V"
                set "DETECTED_VER=1"
            )
        )
    )
    
    set "REMOTE_JAR_URL=https://maven.thevpc.net/net/thevpc/nuts/nuts-app/!NUTS_VERSION!/nuts-app-!NUTS_VERSION!.jar"
    echo No compiled nuts JAR found locally. Downloading !NUTS_VERSION! from !REMOTE_JAR_URL!...
    if not exist "%SCRIPT_DIR%target" mkdir "%SCRIPT_DIR%target"
    set "NUTS_JAR=%SCRIPT_DIR%target\nuts-app-!NUTS_VERSION!.jar"
    powershell -Command "Invoke-WebRequest -Uri '!REMOTE_JAR_URL!' -OutFile '%SCRIPT_DIR%target\nuts-app-!NUTS_VERSION!.jar'"
)

:found
if not exist "%NUTS_JAR%" (
    echo Error: Could not locate or download nuts JAR. Please run 'mvn clean install' first.
    exit /b 1
)

echo Running nuts-release-tool using: %NUTS_JAR%
java -jar "%NUTS_JAR%" -ZyS --stacktrace --sandbox --verbose net.thevpc.nuts.installers:nuts-release-tool %*
