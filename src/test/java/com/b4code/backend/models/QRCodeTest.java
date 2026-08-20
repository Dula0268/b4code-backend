package com.b4code.backend.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class QRCodeTest {

    @Test
    public void testQRCodeCreation() {
        QRCode qrCode = new QRCode();
        qrCode.setUniqueQrId("QR-12345");
        qrCode.setPropertyId(10L);
        qrCode.setLocation("Table 5");
        qrCode.setStatus("ACTIVE");

        assertEquals("QR-12345", qrCode.getUniqueQrId());
        assertEquals(10L, qrCode.getPropertyId());
        assertEquals("Table 5", qrCode.getLocation());
        assertEquals("ACTIVE", qrCode.getStatus());
    }
}
