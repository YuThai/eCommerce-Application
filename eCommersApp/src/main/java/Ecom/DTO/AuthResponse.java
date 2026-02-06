package Ecom.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication Response DTO
 * Contains both access and refresh tokens with metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    
    /**
     * JWT Access Token (short-lived, 15 minutes)
     * Use this in Authorization header: "Bearer <token>"
     */
    private String accessToken;
    
    /**
     * Refresh Token (long-lived, 7 days)
     * Use to obtain new access token when expired
     */
    private String refreshToken;
    
    /**
     * Token type (always "Bearer")
     */
    private String tokenType = "Bearer";
    
    /**
     * Access token expiration time in milliseconds
     */
    private Long expiresIn;
    
    /**
     * Refresh token expiration time in milliseconds
     */
    private Long refreshExpiresIn;
    
    /**
     * User ID
     */
    private Long userId;
    
    /**
     * Username
     */
    private String username;
    
    /**
     * User role
     */
    private String role;
    
    /**
     * Whether token is valid for middleware integration
     */
    private Boolean middlewareCompatible = true;
    
    /**
     * Success message
     */
    private String message;
}
