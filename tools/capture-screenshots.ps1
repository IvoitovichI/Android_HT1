param([string]$Adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe", [string]$Serial)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
if (-not (Test-Path -LiteralPath $Adb)) { throw 'Specify -Adb with the full path to adb.exe.' }
$deviceArgs = @()
if ($Serial) { $deviceArgs = @('-s', $Serial); $env:ANDROID_SERIAL = $Serial }
Push-Location $projectRoot
try {
    & .\gradlew.bat :app:connectedDebugAndroidTest --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Instrumented tests failed; screenshots were not copied.' }
    & $Adb @deviceArgs pull '/sdcard/Android/data/com.studentrpg.campus/files/screenshots/.' 'docs/screenshots/'
    if ($LASTEXITCODE -ne 0) { throw 'Could not copy screenshots from the device.' }
    Write-Host 'Screenshots saved to docs/screenshots. Review them before committing.'
} finally { Pop-Location }
