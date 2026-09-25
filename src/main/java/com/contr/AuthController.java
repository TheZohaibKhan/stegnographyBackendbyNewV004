package com.contr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.Dto.ApiResponse;
import com.Dto.ForgotPasswordRequest;
import com.Dto.OtpRequest;
import com.entity.User;
import com.service.OtpService;
import com.service.UserService;

@Controller
public class AuthController {

    // Logger for debugging OTP/email errors in Railway logs
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserService userService;

    // ─── Pages ────────────────────────────────────────────────────────────────
    @GetMapping("/register")
    public String registerPage() {
        return "redirect:/register.html";
    }

    // ─── Registration OTP ─────────────────────────────────────────────────────
    @PostMapping("/send-otp")
    @ResponseBody
    public ResponseEntity<ApiResponse> sendOtp(@RequestBody User user) {

        if (user.getUsername() == null || !user.getUsername().contains("@")) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Invalid email address"));
        }

        if (userService.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Email already registered. Please login."));
        }

        try {
            otpService.sendOtp(user.getUsername());

            return ResponseEntity.ok(
                    new ApiResponse(
                            true,
                            "OTP sent to your email. Valid for 10 minutes."
                    )
            );

        } catch (Exception e) {

            // Write the REAL SMTP error to Railway logs
            log.error(
                    "Failed to send OTP to {}",
                    user.getUsername(),
                    e
            );

            // Keep technical error hidden from the browser
            return ResponseEntity.internalServerError()
                    .body(
                            new ApiResponse(
                                    false,
                                    "Failed to send OTP. Please try again."
                            )
                    );
        }
    }

    // ─── Verify Registration OTP ──────────────────────────────────────────────
    @PostMapping("/verify-otp")
    @ResponseBody
    public ResponseEntity<ApiResponse> verifyOtp(
            @RequestBody OtpRequest req) {

        if (!otpService.verifyOtp(req.getUsername(), req.getOtp())) {
            return ResponseEntity.badRequest()
                    .body(
                            new ApiResponse(
                                    false,
                                    "Invalid or expired OTP. Please try again."
                            )
                    );
        }

        if (req.getPassword() == null ||
                !isValidPassword(req.getPassword())) {

            return ResponseEntity.badRequest()
                    .body(
                            new ApiResponse(
                                    false,
                                    "Password must be at least 8 characters and include uppercase, lowercase, number, and special character."
                            )
                    );
        }

        User user = new User();

        user.setUsername(req.getUsername());
        user.setName(req.getName());
        user.setMobileNo(req.getMobileNo());
        user.setPassword(req.getPassword());

        userService.register(user);

        otpService.clearOtp(req.getUsername());

        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        "Registration successful! You can now login."
                )
        );
    }

    // ─── Forgot Password ──────────────────────────────────────────────────────
    @PostMapping("/forgot-password/send-otp")
    @ResponseBody
    public ResponseEntity<ApiResponse> forgotSendOtp(
            @RequestBody ForgotPasswordRequest req) {

        if (req.getEmail() == null ||
                !req.getEmail().contains("@")) {

            return ResponseEntity.badRequest()
                    .body(
                            new ApiResponse(
                                    false,
                                    "Invalid email address"
                            )
                    );
        }

        if (!userService.existsByUsername(req.getEmail())) {

            return ResponseEntity.badRequest()
                    .body(
                            new ApiResponse(
                                    false,
                                    "No account found with this email."
                            )
                    );
        }

        try {

            otpService.sendForgotPasswordOtp(req.getEmail());

            return ResponseEntity.ok(
                    new ApiResponse(
                            true,
                            "Password reset OTP sent to your email."
                    )
            );

        } catch (Exception e) {

            // Log the actual error for Railway debugging
            log.error(
                    "Failed to send forgot-password OTP to {}",
                    req.getEmail(),
                    e
            );

            return ResponseEntity.internalServerError()
                    .body(
                            new ApiResponse(
                                    false,
                                    "Failed to send OTP."
                            )
                    );
        }
    }

    // ─── Forgot Password Verify OTP ──────────────────────────────────────────
    @PostMapping("/forgot-password/verify-otp")
    @ResponseBody
    public ResponseEntity<ApiResponse> forgotVerifyOtp(
            @RequestBody ForgotPasswordRequest req) {

        if (!otpService.verifyOtp(
                req.getEmail(),
                req.getOtp())) {

            return ResponseEntity.badRequest()
                    .body(
                            new ApiResponse(
                                    false,
                                    "Invalid or expired OTP."
                            )
                    );
        }

        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        "OTP verified. You may now set a new password."
                )
        );
    }

    // ─── Forgot Password Reset ────────────────────────────────────────────────
    @PostMapping("/forgot-password/reset")
    @ResponseBody
    public ResponseEntity<ApiResponse> forgotReset(
            @RequestBody ForgotPasswordRequest req) {

        if (!otpService.verifyOtp(
                req.getEmail(),
                req.getOtp())) {

            return ResponseEntity.badRequest()
                    .body(
                            new ApiResponse(
                                    false,
                                    "OTP expired. Please start over."
                            )
                    );
        }

        if (req.getNewPassword() == null ||
                !isValidPassword(req.getNewPassword())) {

            return ResponseEntity.badRequest()
                    .body(
                            new ApiResponse(
                                    false,
                                    "Password must be at least 8 characters and include uppercase, lowercase, number, and special character."
                            )
                    );
        }

        String result = userService.resetPassword(
                req.getEmail(),
                req.getNewPassword()
        );

        if (!"SUCCESS".equals(result)) {

            return ResponseEntity.badRequest()
                    .body(
                            new ApiResponse(
                                    false,
                                    result
                            )
                    );
        }

        otpService.clearOtp(req.getEmail());

        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        "Password reset successful! You can now login."
                )
        );
    }

    // ─── Password strength validator ──────────────────────────────────────────
    // Min 8 chars | 1 uppercase | 1 lowercase | 1 digit | 1 special char
    private boolean isValidPassword(String password) {

        if (password == null) {
            return false;
        }

        String regex =
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=])"
                + "[A-Za-z\\d@$!%*?&#^()_+\\-=]{8,}$";

        return password.matches(regex);
    }
}