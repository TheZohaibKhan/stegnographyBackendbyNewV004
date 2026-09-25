package com.contr;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.entity.UploadHistory;
import com.repo.UploadHistoryRepository;
import com.service.AudioSteganographyService;
import com.service.ImageSteganographyService;
import com.service.VideoSteganographyService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class SteganographyController {

    @Autowired private ImageSteganographyService imageService;
    @Autowired private AudioSteganographyService audioService;
    @Autowired private VideoSteganographyService videoService;
    @Autowired private UploadHistoryRepository historyRepo;

    // Safe history save
    private void saveHistory(String fileType, String actionType, String fileName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null) {
            return; // don't save if not logged in
        }

        UploadHistory history = new UploadHistory();
        history.setUsername(auth.getName());
        history.setFileType(fileType);
        history.setActionType(actionType);
        history.setFileName(fileName);
        history.setUploadTime(LocalDateTime.now());

        historyRepo.save(history);
    }

    // My History with auth check
    @GetMapping("/my-history")
    public ResponseEntity<?> myHistory(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }
        return ResponseEntity.ok(historyRepo.findByUsername(auth.getName()));
    }

    // NOTE: /api/current-user is handled by UserController — returns name from DB

    /* ── IMAGE ── */
    @PostMapping("/image/encode")
    public ResponseEntity<byte[]> encodeImage(@RequestParam MultipartFile file,
                                              @RequestParam String message) throws Exception {

        byte[] result = imageService.encode(file, message);
        saveHistory("IMAGE", "ENCODE", file.getOriginalFilename());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=encoded-image.png")
                .header(HttpHeaders.CONTENT_TYPE, "image/png")
                .body(result);
    }

    @PostMapping("/image/decode")
    public ResponseEntity<String> decodeImage(@RequestParam MultipartFile file) throws Exception {

        saveHistory("IMAGE", "DECODE", file.getOriginalFilename());
        return ResponseEntity.ok(imageService.decode(file));
    }

    /* ── AUDIO ── */
    @PostMapping("/audio/encode")
    public ResponseEntity<byte[]> encodeAudio(@RequestParam MultipartFile file,
                                              @RequestParam String message) throws Exception {

        byte[] result = audioService.encode(file, message);
        saveHistory("AUDIO", "ENCODE", file.getOriginalFilename());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/audio/decode")
    public ResponseEntity<String> decodeAudio(@RequestParam MultipartFile file) throws Exception {

        saveHistory("AUDIO", "DECODE", file.getOriginalFilename());
        return ResponseEntity.ok(audioService.decode(file));
    }

    /* ── VIDEO ── */
    @PostMapping("/video/encode")
    public ResponseEntity<byte[]> encodeVideo(@RequestParam MultipartFile file,
                                              @RequestParam String message) throws Exception {

        byte[] result = videoService.encode(file, message);
        saveHistory("VIDEO", "ENCODE", file.getOriginalFilename());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/video/decode")
    public ResponseEntity<String> decodeVideo(@RequestParam MultipartFile file) throws Exception {

        saveHistory("VIDEO", "DECODE", file.getOriginalFilename());
        return ResponseEntity.ok(videoService.decode(file));
    }
}