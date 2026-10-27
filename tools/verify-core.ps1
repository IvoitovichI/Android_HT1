param(
    [Parameter(Mandatory = $true)][string]$KotlinHome,
    [string]$JavaHome = $env:JAVA_HOME
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$java = Join-Path $JavaHome 'bin\java.exe'
$compilerLib = Join-Path $KotlinHome 'lib'
Push-Location $projectRoot
try {
    New-Item -ItemType Directory -Force '.verification' | Out-Null
    $sources = @('Character', 'Student', 'Lesson', 'Question', 'Quiz', 'LessonCatalog', 'Exhaustion') |
        ForEach-Object { "src/main/kotlin/$_.kt" }
    $sources += (Get-ChildItem 'game-core/src/main/kotlin/campus/*.kt').FullName
    $sources += 'verification/CoreChecks.kt'
    & $java -cp "$compilerLib\*" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler -kotlin-home $KotlinHome -jvm-target 17 -d '.verification/core.jar' @sources
    if ($LASTEXITCODE -ne 0) { throw 'Kotlin compilation failed.' }
    & $java -cp ".verification/core.jar;$compilerLib\kotlin-stdlib.jar" CoreChecksKt
    if ($LASTEXITCODE -ne 0) { throw 'Core checks failed.' }
} finally { Pop-Location }
