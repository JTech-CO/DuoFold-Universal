param([string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
if (!$JavaHome) { throw 'Set JAVA_HOME to JDK 21 or pass -JavaHome.' }
$root = Split-Path $PSScriptRoot
$previousJavaHome = $env:JAVA_HOME
$env:JAVA_HOME = $JavaHome
Push-Location $root
try {
    # Direct arguments avoid the Compose plugin's Unicode jlink argfile problem on Windows.
    $runtime = Join-Path $root 'desktopApp/build/compose/tmp/main/runtime'
    if (!(Test-Path -LiteralPath $runtime)) {
        & (Join-Path $JavaHome 'bin/jlink.exe') --add-modules java.base,java.desktop,java.logging,jdk.crypto.ec --strip-debug --no-header-files --no-man-pages --strip-native-commands --output $runtime
        if ($LASTEXITCODE -ne 0) { throw 'Runtime image creation failed' }
    }
    & ./gradlew.bat :desktopApp:createDistributable -x :desktopApp:createRuntimeImage --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Desktop packaging failed' }
} finally { Pop-Location; $env:JAVA_HOME = $previousJavaHome }
