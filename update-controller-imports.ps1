$restDir = "src\main\java\com\b4code\backend\rest"
$updated = 0
$skipped = 0

Write-Host "Processing controllers..."
Get-ChildItem $restDir -Filter "*Controller.java" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    $oldContent = $content
    
    $content = $content -replace "import com\.b4code\.backend\.modules\.admin\.dto\.\*;", "import com.b4code.backend.dto.*;"
    $content = $content -replace "import com\.b4code\.backend\.modules\.auth\.dto\.\*;", "import com.b4code.backend.dto.*;"
    $content = $content -replace "import com\.b4code\.backend\.modules\.guest\.dto\.\*;", "import com.b4code.backend.dto.*;"
    $content = $content -replace "import com\.b4code\.backend\.modules\.payment\.dto\.\*;", "import com.b4code.backend.dto.*;"
    $content = $content -replace "import com\.b4code\.backend\.modules\.qr\.dto\.\*;", "import com.b4code.backend.dto.*;"
    $content = $content -replace "import com\.b4code\.backend\.modules\.staff\.dto\.\*;", "import com.b4code.backend.dto.*;"
    
    if ($content -ne $oldContent) {
        Set-Content -Path $_.FullName -Value $content
        $updated++
        Write-Host "  ? $($_.Name)"
    } else {
        $skipped++
    }
}

Write-Host "`nSUMMARY: Updated $updated, Skipped $skipped"
