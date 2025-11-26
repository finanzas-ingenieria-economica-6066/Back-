package pe.edu.upc.bonotech.iam.interfaces.rest.resources;

import java.time.Instant;

public record UserResource(
    Long id, 
    String email,
    String fullName,  // AÑADIDO: Información completa
    Instant createdAt // AÑADIDO: Para auditoría
) {
}