# Comprehensive DTO import fix for consolidated controllers

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
        
        # Fix wildcard imports: import com.b4code.backend.dto.*;
        $content = $content -replace "import com\.b4code\.backend\.dto\.\*;", "import com.b4code.backend.modules.$module.dto.*;"
        
        # Fix specific class imports: import com.b4code.backend.dto.ClassName;
        $content = $content -replace "import com\.b4code\.backend\.dto\.([A-Za-z]+);", "import com.b4code.backend.modules.$module.dto.`$1;"
        
        Set-Content -Path $filePath -Value $content
        Write-Host "Fixed: $controller"
    }
}

Write-Host "`nAll imports fixed!"
