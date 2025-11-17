package pe.edu.upc.bonotech.iam.interfaces.rest.resources;

public record AuthenticatedUserResource(
    String token, 
    Long id, 
    String fullName,
    String email,  // AÑADIDO: Para el frontend
    String tokenType // AÑADIDO: "Bearer" explícito
) {
    public AuthenticatedUserResource {
        tokenType = "Bearer"; // Siempre será Bearer para JWT
    }
}