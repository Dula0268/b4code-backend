package com.b4code.backend.modules.staff.qr.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

@Component
@Slf4j
public class QRImageGenerator {

    private static final int QR_CODE_SIZE = 300;
    private static final String IMAGE_FORMAT = "PNG";

    /**
     * Generate QR code image as byte array (PNG format)
     * @param qrContent The content to encode in QR code (typically a UUID or QR ID)
     * @return Byte array of PNG image
     */
    public byte[] generateQRImage(String qrContent) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);
            
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, IMAGE_FORMAT, outputStream);
            
            log.info("QR code generated successfully for content: {}", qrContent);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error generating QR code for content: {}", qrContent, e);
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }

    /**
     * Generate QR code image as BufferedImage
     * @param qrContent The content to encode in QR code
     * @return BufferedImage of PNG format
     */
    public BufferedImage generateQRBufferedImage(String qrContent) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);
            
            log.info("QR BufferedImage generated successfully for content: {}", qrContent);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (Exception e) {
            log.error("Error generating QR BufferedImage for content: {}", qrContent, e);
            throw new RuntimeException("Failed to generate QR BufferedImage: " + e.getMessage(), e);
        }
    }
}
