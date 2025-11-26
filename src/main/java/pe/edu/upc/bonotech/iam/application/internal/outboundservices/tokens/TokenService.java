package pe.edu.upc.bonotech.iam.application.internal.outboundservices.tokens;

import org.springframework.stereotype.Service;

/**
 * TokenService interface
 * This interface is used to generate and validate tokens
 */
@Service
public interface TokenService {

    /**
     * Generate a token for a given username
     * 
     * @param username the username
     * @return String the token
     */
    String generateToken(String username);

    /**
     * Extract the username from a token
     * 
     * @param token the token
     * @return String the username
     */
    String getUsernameFromToken(String token);

    /**
     * Validate a token
     * 
     * @param token the token
     * @return boolean true if the token is valid, false otherwise
     */
    boolean validateToken(String token);
}