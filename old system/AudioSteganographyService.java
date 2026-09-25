package com.service;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AudioSteganographyService {

    private static final String MARKER = "##AUDIO##";

    public byte[] encode(MultipartFile file, String message) throws Exception {

        byte[] audioBytes = file.getBytes();
        byte[] msgBytes = (MARKER + message).getBytes();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(audioBytes);
        out.write(msgBytes);

        return out.toByteArray();
    }

    public String decode(MultipartFile file) throws Exception {

        String content = new String(file.getBytes());

        if (content.contains(MARKER)) {
            return content.split(MARKER)[1];
        }
        return "No hidden message found";
    }
}

