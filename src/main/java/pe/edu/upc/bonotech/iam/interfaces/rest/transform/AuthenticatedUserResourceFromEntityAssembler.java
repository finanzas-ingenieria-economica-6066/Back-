package pe.edu.upc.bonotech.iam.interfaces.rest.transform;

import pe.edu.upc.bonotech.iam.domain.model.aggregates.AuthenticatedUser;
import pe.edu.upc.bonotech.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(AuthenticatedUser authenticatedUser) {
        return new AuthenticatedUserResource(
            authenticatedUser.token(),
            authenticatedUser.user().getId(),
            authenticatedUser.user().getFullName(),
            authenticatedUser.user().getEmail(),  // AÑADIDO
            "Bearer" // AÑADIDO
        );
    }
}