package com.organicnow.backend.unit.service;

import com.lowagie.text.Image;
import com.organicnow.backend.service.QRCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class QRCodeServiceTest {

    private QRCodeService qr;

    @BeforeEach
    void setup() {
        qr = new QRCodeService();
    }

    // ----------------------------------------------------------
    // 🔥 1. generatePromptPayQRCode
    // ----------------------------------------------------------
    @Test
    void testGeneratePromptPayQRCode() {
        byte[] result = qr.generatePromptPayQRCode("0812345678", 25.50, "INV001");

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    // ----------------------------------------------------------
    // 🔥 2. generateQRCodeImage (normal success)
    // ----------------------------------------------------------
    @Test
    void testGenerateQRCodeImage_Success() {
        byte[] result = qr.generateQRCodeImage("HELLO", 200, 200);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    // ----------------------------------------------------------
    // 🔥 3. generateQRCodeImage fallback (force ZXing exception)
    // โดยใส่ข้อความยาวมากกว่า 5000 ตัวจน encode() ล้มเหลว
    // ----------------------------------------------------------
    @Test
    void testGenerateQRCodeImage_FallbackOnError() {
        String longText = "A".repeat(6000); // ทำให้ ZXing encode error

        byte[] result = qr.generateQRCodeImage(longText, 300, 300);

        assertNotNull(result);
        assertTrue(result.length > 0); // placeholder image
    }

    // ----------------------------------------------------------
    // 🔥 4. generateQRCodeForPDF (success)
    // ----------------------------------------------------------
    @Test
    void testGenerateQRCodeForPDF_Success() throws IOException {
        Image img = qr.generateQRCodeForPDF("PDF-QR", 200, 200);

        assertNotNull(img);
        assertTrue(img.getWidth() > 0);
        assertTrue(img.getHeight() > 0);
    }

    // ----------------------------------------------------------
    // 🔥 5. generateQRCodeForPDF fallback (force ZXing error)
    // ----------------------------------------------------------
    @Test
    void testGenerateQRCodeForPDF_Fallback() throws IOException {
        String longText = "B".repeat(7000);

        Image img = qr.generateQRCodeForPDF(longText, 200, 200);

        assertNotNull(img);
        assertTrue(img.getWidth() > 0);   // placeholder image created
    }
}
