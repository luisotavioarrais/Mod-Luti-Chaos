param([string]$JdkHome = $env:JAVA_HOME, [string]$ServerJar)
$ErrorActionPreference = 'Stop'
if (!$JdkHome) { throw 'Defina JAVA_HOME para um JDK 25 ou informe -JdkHome.' }
$env:JAVA_HOME = $JdkHome
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    $buildArgs = @('clean', 'dist', '--console=plain')
    if ($ServerJar) { $buildArgs += "-PhytaleServerJar=$ServerJar" }
    & .\gradlew.bat @buildArgs
    if ($LASTEXITCODE -ne 0) { throw 'Gradle falhou.' }
} finally { Pop-Location }
