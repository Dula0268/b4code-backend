package com.b4code.backend.service;

import com.b4code.backend.dao.*;
import com.b4code.backend.dto.FinanceSummaryDto;
import com.b4code.backend.dto.PayoutDto;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Payout;
import com.b4code.backend.models.PlatformConfig;
import com.b4code.backend.models.enums.PayoutStatus;
import com.b4code.backend.service.impl.FinanceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FinanceServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private RefundRepository refundRepository;
    @Mock
    private PayoutRepository payoutRepository;
    @Mock
    private PlatformConfigRepository platformConfigRepository;
    @Mock
    private BankAccountRepository bankAccountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private FinanceServiceImpl financeService;

    @Test
    void testGetFinanceSummary() {
        when(refundRepository.sumApprovedRefunds()).thenReturn(new BigDecimal("100.00"));
        when(transactionRepository.sumTotalRevenue()).thenReturn(new BigDecimal("5000.00"));
        when(transactionRepository.sumPlatformCommission()).thenReturn(new BigDecimal("1000.00"));
        when(payoutRepository.sumProcessedPayouts()).thenReturn(new BigDecimal("3000.00"));
        when(refundRepository.countPendingRefunds()).thenReturn(5L);
        when(payoutRepository.count()).thenReturn(20L);
        when(payoutRepository.countByStatus(PayoutStatus.PENDING)).thenReturn(2L);

        FinanceSummaryDto result = financeService.getFinanceSummary();

        assertNotNull(result);
        assertEquals(new BigDecimal("5000.00"), result.getTotalRevenue());
        assertEquals(new BigDecimal("1000.00"), result.getPlatformCommission());
        assertEquals(new BigDecimal("3000.00"), result.getTotalPayouts());
        assertEquals(new BigDecimal("100.00"), result.getTotalRefunds());
        assertEquals(5L, result.getPendingRefunds());
        assertEquals(20L, result.getAllPayoutsCount());
        assertEquals(2L, result.getPendingPayouts());
        assertEquals("LKR", result.getCurrency());
    }

    @Test
    void testGetCommissionRate_Found() {
        PlatformConfig config = new PlatformConfig();
        config.setConfigValue("15.50");
        when(platformConfigRepository.findByConfigKey("COMMISSION_RATE")).thenReturn(Optional.of(config));

        BigDecimal result = financeService.getCommissionRate();

        assertNotNull(result);
        assertEquals(new BigDecimal("15.50"), result);
    }

    @Test
    void testGetCommissionRate_NotFound() {
        when(platformConfigRepository.findByConfigKey("COMMISSION_RATE")).thenReturn(Optional.empty());

        BigDecimal result = financeService.getCommissionRate();

        assertNotNull(result);
        assertEquals(new BigDecimal("20.00"), result);
    }

    @Test
    void testUpdateCommissionRate_Valid() {
        PlatformConfig config = new PlatformConfig();
        config.setConfigValue("20.00");
        when(platformConfigRepository.findByConfigKey("COMMISSION_RATE")).thenReturn(Optional.of(config));

        BigDecimal newRate = new BigDecimal("25.00");
        BigDecimal result = financeService.updateCommissionRate(newRate);

        assertNotNull(result);
        assertEquals(new BigDecimal("25.00"), result);
        verify(platformConfigRepository, times(1)).save(config);
        assertEquals("25.00", config.getConfigValue());
    }

    @Test
    void testUpdateCommissionRate_InvalidLow() {
        CustomException exception = assertThrows(CustomException.class, () -> {
            financeService.updateCommissionRate(new BigDecimal("-1.00"));
        });
        assertEquals("Commission rate must be between 0 and 100.", exception.getMessage());
    }
}
