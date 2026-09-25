package com.contr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.Dto.ApiResponse;
import com.Dto.UpdateProfileRequest;
import com.entity.User;
import com.service.UserService;

@RestController
public class UserController {

    @Autowired private UserService userService;

    @Value("${app.upload.profile-pics:uploads/profile-pics}")
    private String uploadDir;

    // ✅ returns plain text NAME (not email) for sidebar/dashboard
    @GetMapping(value = "/api/current-user", produces = "text/plain")
    public ResponseEntity<String> currentUser(Authentication auth) {
        User u = userService.findByUsername(auth.getName());
        String name = (u != null && u.getName() != null && !u.getName().isBlank())
                ? u.getName()
                : auth.getName(); // fallback to email if name not set
        return ResponseEntity.ok(name);
    }

    // ✅ GET /api/profile — returns safe profile fields as JSON
    @GetMapping("/api/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        User u = userService.findByUsername(auth.getName());
        if (u == null) return ResponseEntity.notFound().build();

        Map<String, Object> profile = new HashMap<>();
        profile.put("name",          u.getName());
        profile.put("email",         u.getUsername());   // username = email
        profile.put("mobile",        u.getMobileNo());
        profile.put("bio",           u.getBio());
        profile.put("profilePicUrl", u.getProfilePic() != null
                ? "/user/profile/pic/" + u.getProfilePic()
                : null);
        return ResponseEntity.ok(profile);
    }

    // ✅ POST /api/profile/update — update name, mobile, bio
    @PostMapping("/api/profile/update")
    public ResponseEntity<ApiResponse> updateProfile(
            @RequestBody UpdateProfileRequest req,
            Authentication auth) {
        String result = userService.updateProfile(auth.getName(), req);
        if ("SUCCESS".equals(result))
            return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully!"));
        return ResponseEntity.badRequest().body(new ApiResponse(false, result));
    }

    // ✅ POST /api/profile/change-password
    @PostMapping("/api/profile/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @RequestBody UpdateProfileRequest req,
            Authentication auth) {
        String result = userService.updateProfile(auth.getName(), req);
        if ("SUCCESS".equals(result))
            return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully!"));
        return ResponseEntity.badRequest().body(new ApiResponse(false, result));
    }

    // ✅ POST /api/profile/upload-pic
    @PostMapping("/api/profile/upload-pic")
    public ResponseEntity<ApiResponse> uploadPic(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        try {
            String filename = userService.uploadProfilePic(auth.getName(), file);
            return ResponseEntity.ok(new ApiResponse(true, "Profile picture updated!",
                    "/user/profile/pic/" + filename));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Failed to upload image"));
        }
    }

    // ✅ Serve profile picture bytes
    @GetMapping("/user/profile/pic/{filename:.+}")
    public ResponseEntity<byte[]> getProfilePic(@PathVariable String filename) {
        try {
            byte[] image = Files.readAllBytes(Paths.get(uploadDir, filename));
            String contentType = filename.endsWith(".png") ? "image/png" : "image/jpeg";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(image);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}