package pe.edu.upc.bonotech.iam.infrastructure.hashing.bcrypt.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

// 1. IMPORTANTE: Importamos la interfaz del "Application Layer" que Spring está buscando
import pe.edu.upc.bonotech.iam.application.internal.outboundservices.hashing.HashingService;

// 2. Importamos la interfaz de infraestructura (si también la necesitas)
import pe.edu.upc.bonotech.iam.infrastructure.hashing.bcrypt.BCryptHashingService;

@Service
// 3. Implementamos AMBAS interfaces:
public class BCryptHashingServiceImpl extends BCryptPasswordEncoder implements BCryptHashingService, HashingService {

    public BCryptHashingServiceImpl() {
        super();
    }

    public BCryptHashingServiceImpl(int strength) {
        super(strength);
    }

    // Al extender BCryptPasswordEncoder, los métodos encode() y matches() ya existen.
    // Sin embargo, es buena práctica sobreescribirlos para asegurar que cumplen con las interfaces.

    @Override
    public String encode(CharSequence rawPassword) {
        return super.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return super.matches(rawPassword, encodedPassword);
    }
}