package pe.edu.upc.bonotech.iam.interfaces.rest.transform;

import pe.edu.upc.bonotech.iam.domain.model.aggregates.User;
import pe.edu.upc.bonotech.iam.interfaces.rest.resources.UserResource;

public class UserResourceFromEntityAssembler {
    public static UserResource toResourceFromEntity(User entity) {
        return new UserResource(
            entity.getId(),
            entity.getEmail(),
            entity.getFullName(),  // AÑADIDO
            entity.getCreatedAt()  // AÑADIDO
        );
    }
}