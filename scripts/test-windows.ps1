param([string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
if (!$JavaHome) { throw 'Set JAVA_HOME to JDK 21 or pass -JavaHome.' }
$root = Split-Path $PSScriptRoot
$previousJavaHome = $env:JAVA_HOME
$env:JAVA_HOME = $JavaHome
Push-Location $root
try {
    & ./gradlew.bat :duofold-core:writeDesktopTestClasspath :duofold-compose:writeDesktopTestClasspath --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
    $java = Join-Path $JavaHome 'bin/java.exe'
    foreach ($module in @('duofold-core','duofold-compose')) {
        $classpath = (Get-Content "$module/build/desktop-test-classpath.txt" -Raw).Trim()
        $classRoot = (Resolve-Path "$module/build/classes/kotlin/desktop/test").Path
        $classes = Get-ChildItem $classRoot -Recurse -Filter '*Test.class' | ForEach-Object {
            [IO.Path]::GetRelativePath($classRoot, $_.FullName).Replace('\','.').Replace('/','.').Replace('.class','')
        }
        if (!$classes) { throw "No test classes found for $module" }
        & $java -cp $classpath org.junit.runner.JUnitCore @classes
        if ($LASTEXITCODE -ne 0) { throw "$module tests failed" }
    }
} finally { Pop-Location; $env:JAVA_HOME = $previousJavaHome }
