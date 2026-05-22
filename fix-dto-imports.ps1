# Fix the DTO imports in consolidated controllers to point to module-specific DTOs

$targetBasePath = "d:\PROJECTS\Software Project\b4code-backend\src\main\java\com\b4code\backend\rest"

# Map of controller to their original module for DTO imports
$dtoMappings = @{
    "AdminUserController.java" = "admin"
    "AnalyticsController.java" = "admin"
    "AuditLogController.java" = "admin"
    "DashboardController.java" = "admin"
    "FinanceController.java" = "admin"
    "ModerationController.java" = "admin"
    "PropertyController.java" = "admin"
    "PublicPropertyController.java" = "admin"
    "SettingsController.java" = "admin"
    "AuthController.java" = "auth"
    "BookingController.java" = "guest"
    "GuestMenuController.java" = "guest"
    "GuestOrderController.java" = "guest"
    "MessagingController.java" = "guest"
    "ReviewController.java" = "guest"
    "SearchController.java" = "guest"
    "TestCloudinaryController.java" = "guest"
    "OwnerStaffController.java" = "owner"
    "PaymentController.java" = "payment"
    "QRCodeController.java" = "qr"
    "MenuItemController.java" = "staff"
    "StaffController.java" = "staff"
    "StaffOrderController.java" = "staff"
    "UserController.java" = "user"
}

foreach ($controller in $dtoMappings.Keys) {
    $module = $dtoMappings[$controller]
    $filePath = Join-Path $targetBasePath $controller
    
    if (Test-Path $filePath) {
        $content = Get-Content $filePath -Raw
        
        # Revert DTO imports to point to module-specific packages
        $content = $content -replace "import com\.b4code\.backend\.dto\.([A-Za-z]+)\.;", "import com.b4code.backend.modules.$module.dto.`$1.;"
        $content = $content -replace "import com\.b4code\.backend\.dto\.([A-Za-z]+);", "import com.b4code.backend.modules.$module.dto.`$1;"
        
        # Fix wildcard imports
        $content = $content -replace "import com\.b4code\.backend\.dto\.([A-Za-z]+)\.\*;", "import com.b4code.backend.modules.$module.dto.`$1.*;"
        
        Set-Content -Path $filePath -Value $content
        Write-Host "Fixed imports for $controller -> module: $module"
    }
}

Write-Host "`nDTO import fixes complete!"
