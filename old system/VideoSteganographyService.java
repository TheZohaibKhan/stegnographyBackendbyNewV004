package com.service;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoSteganographyService {

    private static final String MARKER = "##VIDEO##";

    public byte[] encode(MultipartFile file, String message) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(file.getBytes());
        out.write((MARKER + message).getBytes());

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

