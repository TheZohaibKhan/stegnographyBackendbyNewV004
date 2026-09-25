package com.contr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.Dto.ApiResponse;
import com.entity.UploadHistory;
import com.entity.User;
import com.repo.UploadHistoryRepository;
import com.service.UserService;

@RestController
@RequestMapping("/admin")
public class AdminRestController {

    @Autowired private UserService userService;
    @Autowired private UploadHistoryRepository historyRepo;

    // ─── Dashboard stats ─────────────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userService.countUsers());
        stats.put("totalAdmins", userService.countAdmins());
        stats.put("totalLogs", historyRepo.count());

        List<UploadHistory> all = historyRepo.findAll();

        long encodes = all.stream()
                .filter(h -> "ENCODE".equalsIgnoreCase(h.getActionType()))
                .count();

        long decodes = all.stream()
                .filter(h -> "DECODE".equalsIgnoreCase(h.getActionType()))
                .count();

        stats.put("totalEncodes", encodes);
        stats.put("totalDecodes", decodes);

        return ResponseEntity.ok(stats);
    }

    // ─── All users ───────────────────────────────────────────────
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    // ─── History ─────────────────────────────────────────────────
    @GetMapping("/history")
    public List<UploadHistory> getAllHistory() {
        return historyRepo.findAll();
    }

    // ✅ FIX: logs API
    @GetMapping("/logs")
    public List<UploadHistory> getLogs() {
        return historyRepo.findAll();
    }

    // ✅ FIX: recent activity API
    @GetMapping("/recent-activity")
    public List<UploadHistory> getRecentActivity() {
        return historyRepo.findAll()
                .stream()
                .sorted((a, b) -> b.getUploadTime().compareTo(a.getUploadTime()))
                .limit(10)
                .toList();
    }

    // ─── User history ────────────────────────────────────────────
    @GetMapping("/history/user/{username}")
    public List<UploadHistory> getUserHistory(@PathVariable String username) {
        return historyRepo.findByUsername(username);
    }

    // ─── Delete user ─────────────────────────────────────────────
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Failed to delete user"));
        }
    }

    // ─── Toggle active ───────────────────────────────────────────
    @PatchMapping("/users/{id}/toggle-active")
    public ResponseEntity<ApiResponse> toggleActive(@PathVariable Long id) {
        String result = userService.toggleActive(id);
        if (result.startsWith("Cannot") || result.equals("User not found"))
            return ResponseEntity.badRequest().body(new ApiResponse(false, result));
        return ResponseEntity.ok(new ApiResponse(true, result));
    }

    // ─── Promote ─────────────────────────────────────────────────
    @PatchMapping("/users/{id}/promote")
    public ResponseEntity<ApiResponse> promoteToAdmin(@PathVariable Long id) {
        String result = userService.promoteToAdmin(id);
        if (result.equals("User not found"))
            return ResponseEntity.badRequest().body(new ApiResponse(false, result));
        return ResponseEntity.ok(new ApiResponse(true, result));
    }

    // ─── Search users ────────────────────────────────────────────
    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(@RequestParam String q) {
        List<User> all = userService.findAll();
        List<User> filtered = all.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(q.toLowerCase())
                        || (u.getName() != null && u.getName().toLowerCase().contains(q.toLowerCase())))
                .toList();
        return ResponseEntity.ok(filtered);
    }

    // ✅ FIX: current user
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).body("Not logged in");
        }
        User u = userService.findByUsername(auth.getName());
        return ResponseEntity.ok(u != null ? u : auth.getName());
    }

    // ─── Clear logs ──────────────────────────────────────────────
    @DeleteMapping("/history/clear")
    public ResponseEntity<ApiResponse> clearHistory() {
        historyRepo.deleteAll();
        return ResponseEntity.ok(new ApiResponse(true, "All logs cleared successfully"));
    }
}