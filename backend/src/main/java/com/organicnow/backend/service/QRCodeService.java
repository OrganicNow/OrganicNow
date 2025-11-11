package com.organicnow.backend.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QRCodeService {

    /**
     * สร้าง QR Code สำหรับ PromptPay
     */
    public byte[] generatePromptPayQRCode(String promptPayId, double amount, String reference) {
        try {
            // สร้าง PromptPay QR Code payload
            String qrData = String.format("00020101021129370016A000000677010111%02d%s540654%02d%.2f5802TH62%02d%s6304",
                    promptPayId.length(), promptPayId,
                    String.valueOf(amount).length(), amount,
                    reference.length(), reference);

            return generateQRCodeImage(qrData, 200, 200);
            
        } catch (Exception e) {
            System.err.println("Error generating PromptPay QR Code: " + e.getMessage());
            return generatePlaceholderQRCode();
        }
    }

    /**
     * สร้าง QR Code ธรรมดาจาก text
     */
    public byte[] generateQRCodeImage(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            bufferedImage.createGraphics();

            Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (bitMatrix.get(j, i)) {
                        graphics.fillRect(j, i, 1, 1);
                    }
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", baos);
            return baos.toByteArray();

        } catch (WriterException | IOException e) {
            System.err.println("Error generating QR Code: " + e.getMessage());
            return generatePlaceholderQRCode();
        }
    }

    /**
     * สร้าง QR Code สำหรับ PDF (สำหรับ OpenPDF) - วิธีใหม่ที่ปลอดภัย
     */
    public Image generateQRCodeForPDF(String text, int width, int height) throws IOException {
        try {
            // สร้าง QR Code เป็น BufferedImage ขนาดเล็ก
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            // สร้าง BufferedImage แบบ RGB
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = bufferedImage.createGraphics();
            
            // เพิ่ม anti-aliasing
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // ตั้งค่าสี background เป็นสีขาว
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            
            // ตั้งค่าสี QR Code เป็นสีดำ
            graphics.setColor(Color.BLACK);

            // วาด QR Code ทีละจุด
            int pixelSize = Math.max(1, width / bitMatrix.getWidth());
            for (int y = 0; y < bitMatrix.getHeight(); y++) {
                for (int x = 0; x < bitMatrix.getWidth(); x++) {
                    if (bitMatrix.get(x, y)) {
                        graphics.fillRect(x * pixelSize, y * pixelSize, pixelSize, pixelSize);
                    }
                }
            }
            graphics.dispose();

            // แปลงเป็น byte array ในรูปแบบ JPEG (แทน PNG)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "JPEG", baos);
            byte[] imageBytes = baos.toByteArray();
            
            // สร้าง Image สำหรับ PDF
            Image pdfImage = Image.getInstance(imageBytes);
            pdfImage.setAlignment(Element.ALIGN_CENTER);
            
            return pdfImage;
            
        } catch (Exception e) {
            System.err.println("Error generating QR Code for PDF: " + e.getMessage());
            e.printStackTrace();
            // สร้าง placeholder image แทน
            return createPlaceholderImage(width, height);
        }
    }
    
    /**
     * สร้าง placeholder image เมื่อ QR Code สร้างไม่ได้
     */
    private Image createPlaceholderImage(int width, int height) throws IOException {
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = placeholder.createGraphics();
        
        // พื้นหลังสีขาว
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        // กรอบและข้อความ
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(5, 5, width-10, height-10);
        g.setColor(Color.DARK_GRAY);
        
        // ข้อความ "QR Code"
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        String text = "QR Code";
        int x = (width - fm.stringWidth(text)) / 2;
        int y = height / 2;
        g.drawString(text, x, y);
        
        g.dispose();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(placeholder, "PNG", baos);
        return Image.getInstance(baos.toByteArray());
    }

    /**
     * สร้าง placeholder QR Code เมื่อมีข้อผิดพลาด
     */
    private byte[] generatePlaceholderQRCode() {
        try {
            BufferedImage placeholder = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = placeholder.createGraphics();
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, 200, 200);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(10, 10, 180, 180);
            g.drawString("QR Code", 80, 100);
            g.drawString("Error", 85, 115);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(placeholder, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}