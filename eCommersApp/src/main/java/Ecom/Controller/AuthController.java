package Ecom.Controller;

import Ecom.DTO.AuthResponse;
import Ecom.DTO.LoginRequest;
import Ecom.DTO.RefreshTokenRequest;
import Ecom.Model.User;
import Ecom.Entity.UserPermission;
import Ecom.Repository.UserPermissionRepository;
import Ecom.Repository.UserRepository;
import Ecom.SecurityConfig.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Authentication Controller
 * Handles login, token refresh, and user permission management
 * 
 * SECURITY FEATURES:
 * - JWT Bearer Token (15 minutes validity)
 * - Refresh Token (7 days validity)
 * - Role-based access control (ADMIN, USER)
 * - Table-level permission management
 * - Compatible with third-party middleware integration
 */
@RestController
@RequestMapping("/ecom/auth")
@AllArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "https://eccomers96.netlify.app"})
public class AuthController {

    private AuthenticationManager authenticationManager;
    private JwtTokenProvider tokenProvider;
    private UserRepository userRepository;
    private UserPermissionRepository permissionRepository;
    private PasswordEncoder passwordEncoder;

    /**
     * LOGIN ENDPOINT
     * POST /ecom/auth/login
     * 
     * Request:
     * {
     *   "email": "user@example.com",
     *   "password": "password123"
     * }
     * 
     * Response:
     * {
     *   "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
     *   "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
     *   "tokenType": "Bearer",
     *   "expiresIn": 900000,
     *   "refreshExpiresIn": 604800000,
     *   "userId": 1,
     *   "username": "user@example.com",
     *   "role": "ROLE_USER",
     *   "middlewareCompatible": true
     * }
     * 
     * Token Usage:
     * - Access Token: Valid for 15 minutes
     * - Refresh Token: Valid for 7 days (use to get new access token)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user from database
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate tokens
            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(loginRequest.getEmail());

            // Get user permissions for metadata
            List<UserPermission> permissions = permissionRepository.findByUserIdAndActiveTrue(user.getId());

            AuthResponse response = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(900000L) // 15 minutes
                    .refreshExpiresIn(604800000L) // 7 days
                    .userId(user.getId())
                    .username(user.getEmail())
                    .role(String.valueOf(user.getRole()))
                    .middlewareCompatible(true)
                    .message("Login successful. Access token valid for 15 minutes, refresh token for 7 days.")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid email or password"));
        }
    }

    /**
     * REFRESH TOKEN ENDPOINT
     * POST /ecom/auth/refresh
     * 
     * Request:
     * {
     *   "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
     * }
     * 
     * Response:
     * {
     *   "accessToken": "new JWT token",
     *   "refreshToken": "same refresh token",
     *   "expiresIn": 900000
     * }
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            // Validate refresh token
            if (!tokenProvider.validateToken(request.getRefreshToken())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "Invalid or expired refresh token"));
            }

            String username = tokenProvider.getUsernameFromJWT(request.getRefreshToken());
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Create new authentication for token generation
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username, null, user.getAuthorities()
            );

            // Generate new access token
            String newAccessToken = tokenProvider.generateAccessToken(authentication);

            AuthResponse response = AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(request.getRefreshToken())
                    .tokenType("Bearer")
                    .expiresIn(900000L)
                    .userId(user.getId())
                    .username(username)
                    .message("Token refreshed successfully")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Token refresh failed: " + e.getMessage()));
        }
    }

    /**
     * GET CURRENT USER ENDPOINT
     * GET /ecom/auth/me
     * 
     * Returns: Current authenticated user details
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Not authenticated"));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserPermission> permissions = permissionRepository.findByUserIdAndActiveTrue(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("permissions", permissions.stream()
                .map(p -> new HashMap<String, Object>() {{
                    put("resource", p.getResourceName());
                    put("permission", p.getPermissionType());
                }})
                .toList());

        return ResponseEntity.ok(response);
    }

    /**
     * GRANT PERMISSION ENDPOINT (ADMIN ONLY)
     * POST /ecom/auth/permissions
     * 
     * Request:
     * {
     *   "userId": 2,
     *   "resourceName": "PRODUCTS",
     *   "permissionType": "READ",
     *   "notes": "Can view products"
     * }
     */
    @PostMapping("/permissions")
    public ResponseEntity<?> grantPermission(@Valid @RequestBody Map<String, Object> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("error", "Only ADMIN can grant permissions"));
            }

            Long userId = Long.valueOf(request.get("userId").toString());
            String resourceName = request.get("resourceName").toString();
            String permissionType = request.get("permissionType").toString();

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserPermission permission = UserPermission.builder()
                    .user(user)
                    .resourceName(resourceName)
                    .permissionType(UserPermission.PermissionType.valueOf(permissionType))
                    .active(true)
                    .notes((String) request.get("notes"))
                    .createdAt(System.currentTimeMillis())
                    .build();

            UserPermission savedPermission = permissionRepository.save(permission);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Permission granted successfully");
            response.put("permissionId", savedPermission.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Failed to grant permission: " + e.getMessage()));
        }
    }

    /**
     * GET USER PERMISSIONS
     * GET /ecom/auth/permissions/{userId}
     */
    @GetMapping("/permissions/{userId}")
    public ResponseEntity<?> getUserPermissions(@PathVariable Long userId) {
        try {
            List<UserPermission> permissions = permissionRepository.findByUserIdAndActiveTrue(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("permissions", permissions.stream()
                    .map(p -> new HashMap<String, Object>() {{
                        put("permissionId", p.getId());
                        put("resource", p.getResourceName());
                        put("permission", p.getPermissionType());
                        put("active", p.getActive());
                    }})
                    .toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Failed to fetch permissions: " + e.getMessage()));
        }
    }

    /**
     * REVOKE PERMISSION (ADMIN ONLY)
     * DELETE /ecom/auth/permissions/{permissionId}
     */
    @DeleteMapping("/permissions/{permissionId}")
    public ResponseEntity<?> revokePermission(@PathVariable Long permissionId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("error", "Only ADMIN can revoke permissions"));
            }

            UserPermission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new RuntimeException("Permission not found"));

            permission.setActive(false);
            permission.setUpdatedAt(System.currentTimeMillis());
            permissionRepository.save(permission);

            return ResponseEntity.ok(Collections.singletonMap("message", "Permission revoked successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Failed to revoke permission: " + e.getMessage()));
        }
    }

    /**
     * GENERATE MIDDLEWARE TOKEN (ADMIN ONLY)
     * POST /ecom/auth/middleware-token
     * 
     * For third-party middleware integration
     * Request:
     * {
     *   "username": "user@example.com",
     *   "scopes": ["READ_PRODUCTS", "READ_ORDERS"]
     * }
     */
    @PostMapping("/middleware-token")
    public ResponseEntity<?> generateMiddlewareToken(@RequestBody Map<String, Object> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("error", "Only ADMIN can generate middleware tokens"));
            }

            String username = request.get("username").toString();
            List<String> scopes = (List<String>) request.get("scopes");

            String token = tokenProvider.generateMiddlewareToken(
                    username,
                    scopes.toArray(new String[0])
            );

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("tokenType", "Bearer");
            response.put("username", username);
            response.put("scopes", scopes);
            response.put("expiresIn", 900000);
            response.put("message", "Middleware token generated. Valid for 15 minutes");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Failed to generate middleware token: " + e.getMessage()));
        }
    }

    /**
     * LOGOUT ENDPOINT
     * POST /ecom/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Collections.singletonMap("message", "Logged out successfully"));
    }
}
