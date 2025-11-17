package pe.edu.upc.bonotech.iam.infrastructure.tokens.jwt.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import pe.edu.upc.bonotech.iam.infrastructure.tokens.jwt.BearerTokenService;

import java.security.Key;
import java.util.Date;

@Service
public class BearerTokenServiceImpl implements BearerTokenService {

    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_TYPE = "Bearer";
    
    @Value("${authorization.jwt.secret}")
    private String jwtSecret;
    
    @Value("${authorization.jwt.expiration.days}")
    private int jwtExpirationDays;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @Override
    public String generateToken(String username) {
        return generateTokenFromUsername(username);
    }

    @Override
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateTokenFromUsername(userPrincipal.getUsername());
    }

    private String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationDays * 24 * 60 * 60 * 1000L))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    @Override
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            System.err.println("Token JWT inválido: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("Token JWT expirado: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("Token JWT no soportado: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Claims JWT vacíos: " + e.getMessage());
        }
        return false;
    }

    @Override
    public String getBearerTokenFrom(HttpServletRequest request) {
        String headerAuth = request.getHeader(AUTH_HEADER);
        
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(AUTH_TYPE + " ")) {
            return headerAuth.substring(7);
        }
        
        return null;
    }
}