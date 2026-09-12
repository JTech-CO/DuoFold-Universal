param([string]$Manifest = "$PSScriptRoot/../validation/release-readiness.json")
$ErrorActionPreference = 'Stop'
$data = Get-Content -LiteralPath $Manifest -Raw | ConvertFrom-Json
$issues = [Collections.Generic.List[string]]::new()
if ($data.sourceRevision -notmatch '^[0-9a-f]{40}$') { $issues.Add('Missing source commit hash') }
foreach ($gate in @('licenseReview', 'apiReview')) {
    if ($data.$gate.status -ne 'passed' -or [string]::IsNullOrWhiteSpace($data.$gate.evidence)) { $issues.Add("Pending $gate") }
}
foreach ($id in @('galaxy-phone','galaxy-tab','galaxy-z-fold','galaxy-z-flip','windows','macos')) {
    $entry = @($data.platforms | Where-Object id -eq $id)
    if ($entry.Count -ne 1 -or $entry[0].status -ne 'passed' -or
        [string]::IsNullOrWhiteSpace($entry[0].evidence) -or
        [string]::IsNullOrWhiteSpace($entry[0].model) -or
        [string]::IsNullOrWhiteSpace($entry[0].os)) { $issues.Add("Pending device evidence: $id") }
}
if ($issues.Count) {
    $issues | ForEach-Object { Write-Output "BLOCKED: $_" }
    exit 1
}
Write-Output 'Evidence index complete. Maintainer must verify linked reports before signing/releasing.'
