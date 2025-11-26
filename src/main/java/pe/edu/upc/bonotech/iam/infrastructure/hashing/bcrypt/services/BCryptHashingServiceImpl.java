package pe.edu.upc.bonotech.iam.infrastructure.hashing.bcrypt.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pe.edu.upc.bonotech.iam.infrastructure.hashing.bcrypt.BCryptHashingService;

@Service
public class BCryptHashingServiceImpl extends BCryptPasswordEncoder implements BCryptHashingService {
    public BCryptHashingServiceImpl() {
        super();
    }
    public BCryptHashingServiceImpl(int strength) {
        super(strength);
    }
    @Override
    public String encode(CharSequence rawPassword) {
        return super.encode(rawPassword);
    }
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return super.matches(rawPassword, encodedPassword);
    }
}
