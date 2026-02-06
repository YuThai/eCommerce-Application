package Ecom.SecurityConfig;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT Token Provider - Handles token generation, validation, and refresh
 * Token validity: Access token = 15 minutes, Refresh token = 7 days
 * Supports middleware and third-party integrations via standard JWT format
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:secretsfhsfjhdkjngdfjkgfgjdlkfjsdkfjsd}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration:900000}") // 15 minutes default
    private int accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7 days default
    private int refreshTokenExpiration;

    /**
     * Generate Access Token (short-lived, contains user claims)
     * Valid for 15 minutes by default
     */
    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("authorities", authentication.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .toArray(String[]::new))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * Generate Refresh Token (long-lived, minimal claims)
     * Valid for 7 days by default
     * Can be stored in database for revocation management
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("tokenType", "REFRESH")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * Generate Token for third-party middleware integration
     * Contains scope claims for granular access control
     */
    public String generateMiddlewareToken(String username, String[] scopes) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("scopes", scopes)
                .claim("tokenType", "MIDDLEWARE")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * Extract username from JWT token
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Extract authorities from JWT token
     */
    public String[] getAuthoritiesFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        
        Object authoritiesObj = claims.get("authorities");
        if (authoritiesObj instanceof java.util.List) {
            return ((java.util.List<?>) authoritiesObj).stream()
                    .map(Object::toString)
                    .toArray(String[]::new);
        }
        return new String[]{};
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            throw new RuntimeException("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Expired JWT token: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("Unsupported JWT token: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("JWT claims string is empty: " + e.getMessage());
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Get token expiration time
     */
    public long getExpirationTime(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }
}
