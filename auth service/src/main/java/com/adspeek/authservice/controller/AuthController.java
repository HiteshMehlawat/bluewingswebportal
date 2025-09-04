package com.adspeek.authservice.controller;

import com.adspeek.authservice.dto.UserDTO;
import com.adspeek.authservice.dto.StaffActivityDTO;
import com.adspeek.authservice.entity.User;
import com.adspeek.authservice.entity.Staff;
import com.adspeek.authservice.repository.UserRepository;
import com.adspeek.authservice.repository.StaffRepository;
import com.adspeek.authservice.security.JwtTokenProvider;
import com.adspeek.authservice.service.StaffActivityService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final StaffActivityService staffActivityService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            UserDTO userDTO = userOpt.map(u -> UserDTO.builder()
                    .id(u.getId())
                    .email(u.getEmail())
                    .role(u.getRole())
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .phone(u.getPhone())
                    .isActive(u.getIsActive())
                    .emailVerified(u.getEmailVerified())
                    .createdAt(u.getCreatedAt())
                    .updatedAt(u.getUpdatedAt())
                    .build()).orElse(null);

            // Log activity if user is a staff member
            if (userDTO != null && userDTO.getRole() == User.Role.STAFF) {
                try {
                    Staff staff = staffRepository.findByUserId(userDTO.getId()).orElse(null);
                    if (staff != null) {
                        staffActivityService.logLogin(staff.getId());
                    }
                } catch (Exception e) {
                    // Log error but don't fail the login
                    System.err.println("Error logging staff activity: " + e.getMessage());
                }
            }

            return ResponseEntity.ok(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "user", userDTO));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
            }
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token refresh failed"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            // Get current user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Get user from authentication context only (secure approach)
            String email = null;
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getName())) {
                email = authentication.getName();
            }

            if (email != null) {
                Optional<User> userOpt = userRepository.findByEmail(email);

                if (userOpt.isPresent() && userOpt.get().getRole() == User.Role.STAFF) {
                    try {
                        Staff staff = staffRepository.findByUserId(userOpt.get().getId()).orElse(null);
                        if (staff != null) {
                            staffActivityService.logLogout(staff.getId());
                        }
                    } catch (Exception e) {
                        // Log error but don't fail the logout
                        System.err.println("Error logging staff logout activity: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("No email found, skipping logout tracking");
            }

            // Clear security context
            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Logout failed"));
        }
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class RefreshRequest {
        private String refreshToken;
    }

}