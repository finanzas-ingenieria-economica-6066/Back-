package pe.edu.upc.bonotech.iam.infrastructure.hashing.bcrypt;

public interface BCryptHashingService {
    String encode(CharSequence rawPassword);
    boolean matches(CharSequence rawPassword, String encodedPassword);
}