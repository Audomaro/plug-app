# Ir al directorio actual del script (opcional)
Set-Location -Path $PSScriptRoot

# Obtener todos los subdirectorios que contienen un pom.xml
$modules = Get-ChildItem -Directory | Where-Object { Test-Path "$($_.FullName)\pom.xml" }

# Lista para guardar rutas de reportes jacoco
$jacocoReports = @()

foreach ($module in $modules) {
    Write-Host "📦 Ejecutando pruebas en módulo: $($module.Name)" -ForegroundColor Cyan
    Push-Location $module.FullName

    try {
        # Ejecutar pruebas y generar reporte jacoco
        .\mvnw clean install verify

        $reportPath = Join-Path -Path "target\site\jacoco" -ChildPath "index.html"
        if (Test-Path $reportPath) {
            Write-Host "✅ Reporte generado: $($module.FullName)\$reportPath" -ForegroundColor Green
            # Guardar ruta completa
            $jacocoReports += (Join-Path -Path $module.FullName -ChildPath $reportPath)
        } else {
            Write-Host "⚠️ No se generó el reporte para: $($module.Name)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ Error en módulo: $($module.Name)" -ForegroundColor Red
    }

    Pop-Location
}

# Mostrar todos los reportes jacoco generados
Write-Host "`n📊 Reportes JaCoCo generados:" -ForegroundColor Magenta
if ($jacocoReports.Count -gt 0) {
    foreach ($report in $jacocoReports) {
        Write-Host $report
    }

    foreach ($report in $jacocoReports) {
        Start-Process $report
    }
} else {
    Write-Host "No se encontraron reportes jacoco." -ForegroundColor Yellow
}
