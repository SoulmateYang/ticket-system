# Auto-compile hook script
# Triggered by PostToolUse hook after Write/Edit on backend or frontend source files

$stdin = [Console]::In.ReadToEnd()

if (-not $stdin -or $stdin.Trim() -eq "") {
    exit 0
}

try {
    $json = $stdin | ConvertFrom-Json
    $filePath = $json.tool_input.file_path
} catch {
    exit 0
}

if (-not $filePath) {
    exit 0
}

# Backend compile (Java/Maven)
if ($filePath -match 'ticket-system-admin\\backend\\src') {
    Write-Host "[AUTO] Backend changed: $filePath"
    Set-Location "ticket-system-admin/backend"
    mvn compile -q 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[AUTO] Backend compiled OK"
    } else {
        Write-Host "[AUTO] Backend compile failed (exit: $LASTEXITCODE)"
    }
}
# Frontend compile (npm/vite)
elseif ($filePath -match 'ticket-system-admin\\frontend\\src') {
    Write-Host "[AUTO] Frontend changed: $filePath"
    Set-Location "ticket-system-admin/frontend"
    npm run build 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[AUTO] Frontend built OK"
    } else {
        Write-Host "[AUTO] Frontend build failed (exit: $LASTEXITCODE)"
    }
}
