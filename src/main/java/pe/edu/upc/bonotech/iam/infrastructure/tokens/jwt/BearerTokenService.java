package pe.edu.upc.bonotech.iam.infrastructure.tokens.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

public interface BearerTokenService {
    String generateToken(String username);
    String generateToken(Authentication authentication);
    String getUsernameFromToken(String token);
    boolean validateToken(String token);
    String getBearerTokenFrom(HttpServletRequest request);
}
