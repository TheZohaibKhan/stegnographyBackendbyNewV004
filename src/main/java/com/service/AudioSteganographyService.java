package com.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AudioSteganographyService {

    private static final String MARKER = "##AUDIO##";

    // ── SHA-256 hash helper ──────────────────────────────────────────────────
    private String hashPassword(String password) throws Exception {
        if (password == null || password.isBlank()) return "";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    public byte[] encode(MultipartFile file, String message, String password) throws Exception {

        byte[] audioBytes = file.getBytes();
        String hashedPwd = hashPassword(password);
        // Format appended: ##AUDIO##HASH::<hash>::<message>
        String payload = MARKER + "HASH::" + hashedPwd + "::" + message;
        byte[] msgBytes = payload.getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(audioBytes);
        out.write(msgBytes);

        return out.toByteArray();
    }

    public String decode(MultipartFile file, String password) throws Exception {

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        if (!content.contains(MARKER)) {
            return "No hidden message found";
        }

        String afterMarker = content.split(MARKER)[1];

        if (afterMarker.startsWith("HASH::")) {
            String[] parts = afterMarker.split("::", 3);
            String storedHash = parts[1];
            String actualMessage = parts[2];

            if (!storedHash.isBlank()) {
                String submittedHash = hashPassword(password);
                if (!submittedHash.equals(storedHash)) {
                    throw new RuntimeException("WRONG_PASSWORD");
                }
            }
            return actualMessage;
        }

        return afterMarker; // old format (no password)
    }
}