package com.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class ImageSteganographyService {

    private static final String END_MARKER = "#END";

    // ── SHA-256 hash helper ──────────────────────────────────────────────────
    private String hashPassword(String password) throws Exception {
        if (password == null || password.isBlank()) return "";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    /* ================= ENCODE ================= */
    public byte[] encode(MultipartFile file, String message, String password) throws Exception {

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

        // Build payload: HASH::<sha256hash>::<message>#END
        String hashedPwd = hashPassword(password);
        String payload = "HASH::" + hashedPwd + "::" + message;

        byte[] msgBytes = (payload + END_MARKER)
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
    public String decode(MultipartFile file, String password) throws Exception {

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
                        String full = result.substring(0, result.length() - END_MARKER.length());

                        // New format: HASH::<hash>::<message>
                        if (full.startsWith("HASH::")) {
                            String[] parts = full.split("::", 3);
                            String storedHash = parts[1];
                            String actualMessage = parts[2];

                            if (!storedHash.isBlank()) {
                                // File is password-protected — verify
                                String submittedHash = hashPassword(password);
                                if (!submittedHash.equals(storedHash)) {
                                    throw new RuntimeException("WRONG_PASSWORD");
                                }
                            }
                            return actualMessage;
                        }

                        return full; // old format (no password)
                    }

                    bitCount = 0;
                    currentByte = 0;
                }
            }
        }

        return "No hidden message found";
    }
}