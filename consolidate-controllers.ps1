# Consolidate all controller files to src/main/java/com/b4code/backend/rest/

$sourceBasePath = "d:\PROJECTS\Software Project\b4code-backend\src\main\java\com\b4code\backend\modules"
$targetBasePath = "d:\PROJECTS\Software Project\b4code-backend\src\main\java\com\b4code\backend\rest"

# Define all controller file paths to consolidate
$controllerPaths = @(
    "admin\rest\AdminUserController.java",
    "admin\rest\AnalyticsController.java",
    "admin\rest\AuditLogController.java",
    "admin\rest\DashboardController.java",
    "admin\rest\FinanceController.java",
    "admin\rest\ModerationController.java",
    "admin\rest\PropertyController.java",
    "admin\rest\PublicPropertyController.java",
    "admin\rest\SettingsController.java",
    "auth\controller\AuthController.java",
    "guest\rest\BookingController.java",
    "guest\rest\GuestMenuController.java",
    "guest\rest\GuestOrderController.java",
    "guest\rest\MessagingController.java",
    "guest\rest\ReviewController.java",
    "guest\rest\SearchController.java",
    "guest\rest\TestCloudinaryController.java",
    "owner\controller\OwnerStaffController.java",
    "payment\controller\PaymentController.java",
    "qr\controller\QRCodeController.java",
    "staff\controller\MenuItemController.java",
    "staff\controller\StaffController.java",
    "staff\controller\StaffOrderController.java",
    "user\controller\UserController.java"
)

$consolidatedCount = 0

foreach ($relativePath in $controllerPaths) {
    $sourceFile = Join-Path $sourceBasePath $relativePath
    $fileName = Split-Path $relativePath -Leaf
    $targetFile = Join-Path $targetBasePath $fileName
    
    if (Test-Path $sourceFile) {
        Write-Host "Processing: $fileName"
        
        # Read the file content
        $content = Get-Content $sourceFile -Raw
        
        # Update package declaration
        $content = $content -replace 'package com\.b4code\.backend\.modules\.[^;]+;', 'package com.b4code.backend.rest;'
        
        # Update imports - Models
        $content = $content -replace 'import com\.b4code\.backend\.modules\.([^.]+)\.models\.', 'import com.b4code.backend.models.'
        
        # Update imports - Services (remove module prefix)
        $content = $content -replace 'import com\.b4code\.backend\.modules\.([^.]+)\.service\.', 'import com.b4code.backend.service.'
        
        # Update imports - DTOs (remove module prefix)
        $content = $content -replace 'import com\.b4code\.backend\.modules\.([^.]+)\.dto\.', 'import com.b4code.backend.dto.'
        
        # Update imports - Repositories (remove module prefix if not admin.dao)
        $content = $content -replace 'import com\.b4code\.backend\.modules\.([^.]+)\.repository\.', 'import com.b4code.backend.dao.'
        
        # For admin.dao imports
        $content = $content -replace 'import com\.b4code\.backend\.modules\.admin\.dao\.', 'import com.b4code.backend.dao.'
        
        # Update imports - Exceptions
        $content = $content -replace 'import com\.b4code\.backend\.modules\.([^.]+)\.exceptions?\.', 'import com.b4code.backend.exceptions.'
        
        # Update imports - Infrastructure
        $content = $content -replace 'import com\.b4code\.backend\.modules\.([^.]+)\.infrastructure\.', 'import com.b4code.backend.infrastructure.'
        
        # Update imports - Enums
        $content = $content -replace 'import com\.b4code\.backend\.modules\.([^.]+)\.enums\.', 'import com.b4code.backend.models.enums.'
        
        # Update imports - Common
        $content = $content -replace 'import com\.b4code\.backend\.common\.', 'import com.b4code.backend.'
        
        # Remove @Qualifier annotations (optional cleanup)
        $content = $content -replace '@Qualifier\([^)]+\)\s*', ''
        
        # Write to consolidated location
        Set-Content -Path $targetFile -Value $content
        $consolidatedCount++
        Write-Host "  OK: $fileName"
    } else {
        Write-Host "  SKIP: $sourceFile"
    }
}

Write-Host "`nConsolidation Complete!"
Write-Host "Total controllers consolidated: $consolidatedCount"
