package com.service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.Dto.UpdateProfileRequest;
import com.entity.UploadHistory;
import com.entity.User;
import com.repo.UploadHistoryRepository;
import com.repo.UserRepository;

@Service
public class UserService {

    @Autowired private UserRepository repo;
    @Autowired private PasswordEncoder encoder;
    @Autowired private UploadHistoryRepository historyRepo;

    @Value("${app.upload.profile-pics:uploads/profile-pics}")
    private String uploadDir;

    // ─── Register ───────────────────────────────────────────────────────────
    public void register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        user.setVerified(true);
        user.setActive(true);
        repo.save(user);
    }

    // ─── Finders ────────────────────────────────────────────────────────────
    public User findByUsername(String username) { return repo.findByUsername(username); }

    public boolean existsByUsername(String username) { return repo.findByUsername(username) != null; }

    public List<User> findAll() { return repo.findAll(); }

    public User findById(Long id) { return repo.findById(id).orElse(null); }

    // ─── Update last login ───────────────────────────────────────────────────
    public void updateLastLogin(String username) {
        User u = repo.findByUsername(username);
        if (u != null) { u.setLastLogin(LocalDateTime.now()); repo.save(u); }
    }

    // ─── Update Profile (name, mobile, bio) ─────────────────────────────────
    public String updateProfile(String username, UpdateProfileRequest req) {
        User u = repo.findByUsername(username);
        if (u == null) return "User not found";

        // Update fields if provided
        if (req.getName() != null && !req.getName().isBlank()) u.setName(req.getName());
        if (req.getMobileNo() != null) u.setMobileNo(req.getMobileNo());
        if (req.getBio() != null) u.setBio(req.getBio());

        // Change password flow
        if (req.getNewPassword() != null && !req.getNewPassword().isBlank()) {
            if (req.getCurrentPassword() == null || req.getCurrentPassword().isBlank())
                return "Current password is required";
            if (!encoder.matches(req.getCurrentPassword(), u.getPassword()))
                return "Current password is incorrect";
            if (!isValidPassword(req.getNewPassword()))
                return "Password must be at least 8 characters and include uppercase, lowercase, number, and special character.";
            u.setPassword(encoder.encode(req.getNewPassword()));
        }

        repo.save(u);
        return "SUCCESS";
    }

    // ─── Upload Profile Picture ──────────────────────────────────────────────
    public String uploadProfilePic(String username, MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("File is empty");

        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) throw new IllegalArgumentException("Only image files allowed");
        if (file.getSize() > 5 * 1024 * 1024) throw new IllegalArgumentException("File too large (max 5MB)");

        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);

        String ext = file.getOriginalFilename() != null
            ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'))
            : ".jpg";
        String filename = UUID.randomUUID() + ext;
        Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

        User u = repo.findByUsername(username);
        if (u != null) {
            // Delete old pic
            if (u.getProfilePic() != null) {
                try { Files.deleteIfExists(dir.resolve(u.getProfilePic())); } catch (Exception ignored) {}
            }
            u.setProfilePic(filename);
            repo.save(u);
        }
        return filename;
    }

    // ─── Reset Password (forgot) ─────────────────────────────────────────────
    public String resetPassword(String email, String newPassword) {
        User u = repo.findByUsername(email);
        if (u == null) return "Email not found";
        if (!isValidPassword(newPassword)) return "Password must be at least 8 characters and include uppercase, lowercase, number, and special character.";
        u.setPassword(encoder.encode(newPassword));
        repo.save(u);
        return "SUCCESS";
    }

    // ─── Admin: toggle active ────────────────────────────────────────────────
    public String toggleActive(Long id) {
        User u = repo.findById(id).orElse(null);
        if (u == null) return "User not found";
        if ("ROLE_ADMIN".equals(u.getRole())) return "Cannot deactivate admin";
        u.setActive(!u.isActive());
        repo.save(u);
        return u.isActive() ? "User activated" : "User deactivated";
    }

    // ─── Admin: promote to admin ──────────────────────────────────────────────
    public String promoteToAdmin(Long id) {
        User u = repo.findById(id).orElse(null);
        if (u == null) return "User not found";
        u.setRole("ROLE_ADMIN");
        repo.save(u);
        return "User promoted to admin";
    }

    // ─── Admin: delete ────────────────────────────────────────────────────────
    public void deleteUser(Long id) { repo.deleteById(id); }

    // ─── Stats ────────────────────────────────────────────────────────────────
    public long countUsers() { return repo.countByRole("ROLE_USER"); }
    public long countAdmins() { return repo.countByRole("ROLE_ADMIN"); }
    
    // ─── Get user activity history ─────────────────────────────────────────────
    public List<UploadHistory> findByUsernameForHi(String username) {
        return historyRepo.findByUsernameOrderByUploadTimeDesc(username);
    }

    // ─── Password strength validator ──────────────────────────────────────────
    // Min 8 chars | 1 uppercase | 1 lowercase | 1 digit | 1 special char
    private boolean isValidPassword(String password) {
        if (password == null) return false;
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=])[A-Za-z\\d@$!%*?&#^()_+\\-=]{8,}$";
        return password.matches(regex);
    }
}
