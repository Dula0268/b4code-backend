package com.b4code.backend.service.impl;

import com.b4code.backend.dao.BankAccountRepository;
import com.b4code.backend.dao.PayoutRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.PayoutDto;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.BankAccount;
import com.b4code.backend.models.Payout;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.User;
import com.b4code.backend.models.enums.PayoutStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OwnerSettingsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PayoutRepository payoutRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private OwnerSettingsServiceImpl ownerSettingsService;

    private User testOwner;
    private Property testProperty;
    private BankAccount testBankAccount;

    @BeforeEach
    void setUp() {
        testOwner = new User();
        testOwner.setId(1L);
        testOwner.setEmail("owner@example.com");
        testOwner.setFirstName("John");
        testOwner.setLastName("Doe");

        testProperty = new Property();
        testProperty.setId(10L);
        testProperty.setName("Test Villa");
        testProperty.setOwnerId(testOwner.getId());

        testBankAccount = new BankAccount();
        testBankAccount.setId(100L);
        testBankAccount.setOwnerId(testOwner.getId());
        testBankAccount.setIsPrimary(true);
    }

    @Test
    void testRequestPayout_Success() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testOwner));
        when(payoutRepository.findRecentActiveByOwnerIdOrderByRequestedAtDesc(anyLong(), anyList(), any())).thenReturn(List.of());
        when(bankAccountRepository.findById(100L))
                .thenReturn(Optional.of(testBankAccount));
        when(propertyRepository.findById(testProperty.getId())).thenReturn(Optional.of(testProperty));

        Payout savedPayout = Payout.builder()
                .id(999L)
                .ownerId(testOwner.getId())
                .propertyName("Test Villa")
                .amount(new java.math.BigDecimal("50000.00"))
                .currency("LKR")
                .status(PayoutStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
        when(payoutRepository.save(any(Payout.class))).thenReturn(savedPayout);

        // Act
        PayoutDto result = ownerSettingsService.requestPayout("owner@example.com", 10L, 100L);

        // Assert
        assertNotNull(result);
        assertEquals(999L, result.getId());
        assertEquals("Test Villa", result.getPropertyName());
        assertEquals(PayoutStatus.PENDING, result.getStatus());
        verify(payoutRepository, times(1)).save(any(Payout.class));
    }

    @Test
    void testRequestPayout_NoBankAccountThrowsException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testOwner));
        when(payoutRepository.findRecentActiveByOwnerIdOrderByRequestedAtDesc(anyLong(), anyList(), any())).thenReturn(List.of());
        when(bankAccountRepository.findById(100L))
                .thenReturn(Optional.empty()); // Empty

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            ownerSettingsService.requestPayout("owner@example.com", 10L, 100L);
        });
        
        assertTrue(exception.getMessage().contains("Bank account not found"));
        verify(payoutRepository, never()).save(any(Payout.class));
    }

    @Test
    void testRequestPayout_AlreadyPendingThrowsException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testOwner));
        
        Payout pendingPayout = new Payout();
        pendingPayout.setStatus(PayoutStatus.PENDING);
        when(payoutRepository.findRecentActiveByOwnerIdOrderByRequestedAtDesc(anyLong(), anyList(), any()))
                .thenReturn(List.of(pendingPayout));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            ownerSettingsService.requestPayout("owner@example.com", 10L, 100L);
        });
        
        assertTrue(exception.getMessage().contains("already pending"));
        verify(payoutRepository, never()).save(any(Payout.class));
    }
}
