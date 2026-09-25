package com.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Service
public class ImageSteganographyService {

    private static final String END_MARKER = "#END";

    /* ================= ENCODE ================= */
    public byte[] encode(MultipartFile file, String message) throws Exception {

        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) {
            throw new RuntimeException("Unsupported image format");
        }

        // ✅ Convert ANY image to PNG-compatible format
        BufferedImage image = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = image.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();

        byte[] msgBytes = (message + END_MARKER)
                .getBytes(StandardCharsets.UTF_8);

        int msgBitIndex = 0;
        int totalBits = msgBytes.length * 8;

        outer:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {

                if (msgBitIndex >= totalBits)
                    break outer;

                int rgb = image.getRGB(x, y);

                int byteIndex = msgBitIndex / 8;
                int bitIndex = 7 - (msgBitIndex % 8);
                int bit = (msgBytes[byteIndex] >> bitIndex) & 1;

                rgb = (rgb & 0xFFFFFFFE) | bit;
                image.setRGB(x, y, rgb);

                msgBitIndex++;
            }
        }

        if (msgBitIndex < totalBits) {
            throw new RuntimeException("Message too large for image");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out); // 🔥 always PNG output
        return out.toByteArray();
    }

    /* ================= DECODE ================= */
    public String decode(MultipartFile file) throws Exception {

        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new RuntimeException("Unsupported image format");
        }

        StringBuilder result = new StringBuilder();
        int currentByte = 0;
        int bitCount = 0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {

                int bit = image.getRGB(x, y) & 1;
                currentByte = (currentByte << 1) | bit;
                bitCount++;

                if (bitCount == 8) {
                    char c = (char) currentByte;
                    result.append(c);

                    if (result.toString().endsWith(END_MARKER)) {
                        return result.substring(
                                0,
                                result.length() - END_MARKER.length()
                        );
                    }

                    bitCount = 0;
                    currentByte = 0;
                }
            }
        }

        return "No hidden message found";
    }
}
